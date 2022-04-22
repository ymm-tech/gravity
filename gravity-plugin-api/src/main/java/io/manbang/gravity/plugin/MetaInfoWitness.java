package io.manbang.gravity.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author duoliang.zhang
 * @since 2020/9/2 15:53
 */
class MetaInfoWitness implements Witness {
    private final MetaInfo metaInfo;

    public MetaInfoWitness(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    @Override
    public boolean saw(ClassLoader classLoader) {
        Enumeration<URL> metaInfos;
        try {
            metaInfos = classLoader.getResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            return false;
        }

        while (metaInfos.hasMoreElements()) {
            URL url = metaInfos.nextElement();
            Properties properties = new Properties();
            try (InputStream metaInfoStream = url.openStream()) {
                properties.load(metaInfoStream);
            } catch (IOException e) {
                continue;
            }

            boolean matched = MetaInfo.builder()
                    .specificationTitle(properties.getProperty("Specification-Title", ""))
                    .specificationVersion(properties.getProperty("Specification-Version", ""))
                    .implementationTitle(properties.getProperty("Implementation-Title", ""))
                    .implementationVersion(properties.getProperty("Implementation-Version", ""))
                    .build().match(metaInfo);

            if (matched) {
                return true;
            }
        }

        return false;
    }
}
