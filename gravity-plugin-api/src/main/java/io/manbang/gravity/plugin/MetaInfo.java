package io.manbang.gravity.plugin;

import lombok.Builder;
import lombok.Data;

/**
 * @author duoliang.zhang
 * @since 2020/9/2 15:56
 */
@Data
@Builder
public class MetaInfo {
    private final String specificationTitle;
    private final String specificationVersion;
    private final String implementationTitle;
    private final String implementationVersion;

    public boolean match(MetaInfo metaInfo) {
        if (metaInfo == null) {
            return true;
        }

        return (metaInfo.specificationTitle == null || this.specificationTitle.equals(metaInfo.specificationTitle))
                && (metaInfo.specificationVersion == null || this.specificationVersion.startsWith(metaInfo.specificationVersion))
                && (metaInfo.implementationTitle == null || this.implementationTitle.equals(metaInfo.implementationTitle))
                && (metaInfo.implementationVersion == null || this.implementationVersion.startsWith(metaInfo.implementationVersion));
    }
}
