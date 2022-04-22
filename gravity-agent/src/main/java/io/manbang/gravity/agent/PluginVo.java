package io.manbang.gravity.agent;

import lombok.Data;

/**
 * 插件VO对象
 *
 * @author duoliang.zhang
 * @since 2020/11/18 11:12
 */
@Data
public class PluginVo {
    private String path;
    private String[] jarTypes;
    private String filename;
}
