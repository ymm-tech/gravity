package io.manbang.gravity.plugin;

import lombok.Builder;
import lombok.Data;

/**
 * Maven jar POM GAV(groupId:artifactId:version)
 *
 * @author duoliang.zhang
 * @since 2020/8/25 12:16
 */
@Data
@Builder
public class GAV {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public boolean match(GAV gav) {
        if (gav == null) {
            return true;
        }

        return (gav.groupId == null || this.groupId.equals(gav.groupId))
                && (gav.artifactId == null || this.artifactId.equals(gav.artifactId))
                && (gav.version == null || this.version.startsWith(gav.version));
    }
}
