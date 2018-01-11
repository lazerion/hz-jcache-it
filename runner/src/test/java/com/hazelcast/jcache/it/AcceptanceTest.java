package com.hazelcast.jcache.it;

import com.hazelcast.jcache.it.utils.CacheStats;
import com.hazelcast.jcache.it.utils.ClientContainer;
import com.hazelcast.jcache.it.utils.ComposeCli;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

        await().atMost(20, SECONDS).untilAsserted(() -> {
                    Optional<CacheStats> stats = client.statistics();
                    assertTrue(stats.isPresent());
                    logger.info("Stats {}", stats);
                    assertThat(stats.get().getHits(), is(stats.get().getMisses()));
                    assertThat(stats.get().getPuts(), is(stats.get().getMisses()));
                    assertThat(stats.get().getRemovals(), is(stats.get().getPuts()));
                }
        );
    }

    @Test
    public void shouldWorkWithClientOnCache10AndMembersOnCache11() throws IOException, InterruptedException {
        cli.up("deployment-2.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        verifyStatsForCache10(client);
    }

    @Test
    public void shouldRollingUpgradeSuccessful() throws IOException {
        cli.up("deployment-3.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        verifyStatsForCache10(client);


    }

    private void verifyStatsForCache10(ClientContainer client) {
        await().atMost(20, SECONDS).untilAsserted(() -> {
                    Optional<CacheStats> stats = client.statistics();
                    assertTrue(stats.isPresent());
                    assertThat(stats.get().getHits(), is(stats.get().getPuts()));
                    // Bug is on JCache 1.0
                    assertThat(stats.get().getMisses(), is(0));
                    assertThat(stats.get().getRemovals(), is(stats.get().getPuts()));
                }
        );
    }
}
