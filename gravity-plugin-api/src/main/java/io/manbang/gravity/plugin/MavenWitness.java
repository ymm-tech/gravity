package io.manbang.gravity.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class MavenWitness implements Witness {
    private final GAV gav;

    MavenWitness(GAV gav) {
        this.gav = gav;
    }

    @Override
    public boolean saw(ClassLoader classLoader) {
        InputStream pomStream = classLoader.getResourceAsStream(String.format("META-INF/maven/%s/%s/pom.properties",
                gav.getGroupId(), gav.getArtifactId()));
        if (pomStream == null) {
            return false;
        }

        Properties pomProperties = new Properties();
        try {
            pomProperties.load(pomStream);
        } catch (IOException e) {
            return false;
        }

        return GAV.builder()
                .groupId(pomProperties.getProperty("groupId", ""))
                .artifactId(pomProperties.getProperty("artifactId", ""))
                .version(pomProperties.getProperty("version", ""))
                .build()
                .match(gav);
    }
}
