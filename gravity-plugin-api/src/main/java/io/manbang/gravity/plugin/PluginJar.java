package io.manbang.gravity.plugin;

import lombok.Data;

import java.util.jar.JarFile;

/**
 * 插件Jar包
 *
 * @author duoliang.zhang
 * @since 2020/9/10 16:23
 */
@Data
public class PluginJar {
    private String name;
    private String type;
    private String url;
    private JarFile file;
}
