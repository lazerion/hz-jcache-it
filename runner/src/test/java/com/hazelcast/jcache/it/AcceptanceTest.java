package com.hazelcast.jcache.it;

import com.hazelcast.jcache.it.utils.CacheStats;
import com.hazelcast.jcache.it.utils.ClientContainer;
import com.hazelcast.jcache.it.utils.ComposeCli;
import com.hazelcast.jcache.it.utils.Snapshot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AcceptanceTest {
    private final Logger logger = LoggerFactory.getLogger(AcceptanceTest.class);
    private ComposeCli cli;

    @Before
    public void before() {
        cli = new ComposeCli();
    }

    @After
    public void after() {
        try {
            cli.down();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithClientAndMembersOnCache11() throws IOException, InterruptedException {
        cli.up("deployment-1.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        verifyStatsForCache11(client);
        // getCacheNames should throw IllegalStateException
        await().atMost(20, SECONDS).untilAsserted(() -> assertTrue(client.ensureOpenForGetCacheNames()));
    }

    @Test
    public void shouldWorkWithClientOnCache10AndMembersOnCache11() throws IOException, InterruptedException {
        cli.up("deployment-2.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        verifyStatsForCache10(client);
    }

    @Test
    public void shouldWorkWithClientOnCache11AndMembersOnCache11() throws IOException, InterruptedException {
        cli.up("deployment-3.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        verifyStatsForCache11(client);
    }

    @Test
    public void shouldRollingUpgradeSuccessfulFrom_3_9_2_To_3_10() throws IOException, InterruptedException {
        cli.up("deployment-4.yaml");
        ClientContainer client = new ClientContainer();

        Arrays.asList("hazelcast-1", "hazelcast-2")
                .forEach(it -> {
                    try {
                        verifyStatsForCache10(client);
                        cli.upgrade("upgrade-3.10.yaml", it);
                        TimeUnit.SECONDS.sleep(20);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException("Upgrade failure");
                    }
                });

        verifyStatsForCache10(client);
        cli.upgrade("upgrade-3.10.yaml", "hazelcast-client");
        verifyStatsForCache11(client);
    }

    @Test
    public void shouldCacheDataIntactUpgradeFrom_3_9_2_To_3_10() throws IOException, InterruptedException {
        cli.up("deployment-4.yaml");
        ClientContainer client = new ClientContainer();

        with().pollInterval(1, TimeUnit.SECONDS).await().atMost(20, SECONDS)
                .untilAsserted(() -> assertNotNull(client.snapshot()));

        final Snapshot snapshot = client.snapshot();
        assertNotNull(snapshot);

        Arrays.asList("hazelcast-1", "hazelcast-2")
                .forEach(it -> {
                    try {
                        cli.upgrade("upgrade-3.10.yaml", it);
                        TimeUnit.SECONDS.sleep(20);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException("Upgrade failure");
                    }
                });

        with().pollInterval(1, TimeUnit.SECONDS).await().atMost(20, SECONDS)
                .untilAsserted(() -> assertTrue(client.verify(snapshot, "1.0")));

        cli.upgrade("upgrade-3.10.yaml", "hazelcast-client");

        with().pollInterval(1, TimeUnit.SECONDS).await().atMost(20, SECONDS)
                .untilAsserted(() -> assertTrue(client.verify(snapshot, "1.1")));
    }

    @Test
    public void shouldRollingUpgradeSuccessfulFrom_3_9_2_To_3_9_3() throws IOException, InterruptedException {
        cli.up("deployment-4.yaml");
        ClientContainer client = new ClientContainer();

        Arrays.asList("hazelcast-1", "hazelcast-2")
                .forEach(it -> {
                    try {
                        verifyStatsForCache10(client);
                        cli.upgrade("upgrade-3.9.3.yaml", it);
                        TimeUnit.SECONDS.sleep(20);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        verifyStatsForCache10(client);
        cli.upgrade("upgrade-3.9.3.yaml", "hazelcast-client");
        verifyStatsForCache11(client);
    }

    private void verifyStatsForCache10(ClientContainer client) {
        await().atMost(30, SECONDS).untilAsserted(() -> {
                    Optional<CacheStats> stats = client.statistics();
                    assertTrue(stats.isPresent());
                    assertThat(stats.get().getHits(), is(stats.get().getPuts()));
                    // Bug is on JCache 1.0
                    assertThat(stats.get().getMisses(), is(0));
                    assertThat(stats.get().getRemovals(), is(stats.get().getPuts()));
                }
        );
    }

    private void verifyStatsForCache11(ClientContainer client) {
        await().atMost(30, SECONDS).untilAsserted(() -> {
                    Optional<CacheStats> stats = client.statistics();
                    assertTrue(stats.isPresent());
                    logger.info("Stats {}", stats);
                    assertThat(stats.get().getHits(), is(stats.get().getMisses()));
                    assertThat(stats.get().getPuts(), is(stats.get().getMisses()));
                    assertThat(stats.get().getRemovals(), is(stats.get().getPuts()));
                }
        );
    }
}
