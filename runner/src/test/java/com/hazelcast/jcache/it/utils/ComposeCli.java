package com.hazelcast.jcache.it.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class ComposeCli {

    static final String IP_ADDRESS_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";


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
        int status =  executor.execute(cmdLine);
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

    public String ip(String name) throws IOException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("container name can not be blank");
        }
        String line = String.format("docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' %s", name);
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        executor.execute(cmdLine);
        return filterIp(outputStream.toString());
    }

    private String filterIp(String raw){

        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            return matcher.group();
        } else{
            throw new IllegalArgumentException("unable to find client ip");
        }
    }
}
