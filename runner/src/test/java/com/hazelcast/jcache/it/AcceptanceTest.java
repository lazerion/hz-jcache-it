package com.hazelcast.jcache.it;

import com.hazelcast.jcache.it.utils.ClientContainer;
import com.hazelcast.jcache.it.utils.ComposeCli;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

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
    public void shouldWorkWithMembersConfiguredWithJCache11() throws IOException, InterruptedException {
        cli.up("deployment-1.yaml").scale("hazelcast", 3);
        ClientContainer client = new ClientContainer();

        await().atMost(20, SECONDS).untilAsserted(() -> assertThat(client.statistics(), not(isEmptyOrNullString())));
    }
}
