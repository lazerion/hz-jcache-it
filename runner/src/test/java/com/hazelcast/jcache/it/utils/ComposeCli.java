package com.hazelcast.jcache.it.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

public class ComposeCli {

    private File project;

    public ComposeCli up(String deployment) throws IOException {
        if (StringUtils.isBlank(deployment)) {
            throw new IllegalArgumentException("Deployment can not be blank");
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource(deployment);
        File file = new File(url.getPath());

        if (!file.exists()) {
            throw new IllegalArgumentException("Deployment file does not exist");
        }

        this.project = file;

        String line = String.format("docker-compose -f %s up -d", this.project.getAbsoluteFile());

        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        int status = executor.execute(cmdLine);
        assertTrue(status == 0);

        return this;
    }

    public ComposeCli down() throws IOException {
        String line = String.format("docker-compose -f %s down", this.project.getAbsoluteFile());
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        int status = executor.execute(cmdLine);
        assertTrue(status == 0);
        return this;
    }

    public ComposeCli scale(String service, int count) throws IOException {
        String line = String.format("docker-compose -f %s scale %s=%d", this.project.getAbsoluteFile(), service, count);
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        int status = executor.execute(cmdLine);
        assertTrue(status == 0);
        return this;
    }

    public ComposeCli upgrade(String upgrade, String service) throws IOException {
        if (StringUtils.isBlank(upgrade)) {
            throw new IllegalArgumentException("invalid upgrade file");
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource(upgrade);
        File file = new File(url.getPath());

        if (!file.exists()) {
            throw new IllegalArgumentException("Upgrade file does not exist");
        }

        // uses default time out 10 secs
        String line = String.format("docker-compose -f %s -f %s up -d --no-deps --no-build %s",
                this.project.getAbsoluteFile(),
                file.getAbsoluteFile(),
                service);

        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        int status = executor.execute(cmdLine);
        assertTrue(status == 0);

        return this;
    }
}
