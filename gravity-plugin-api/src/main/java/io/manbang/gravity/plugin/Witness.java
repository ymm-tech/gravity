package io.manbang.gravity.plugin;

/**
 * 目击证人，用于判断当前插件是否支持，目标应用的代码拦截；一般通过构建路径是否存在指定全限定名的类，或者Jar的版本号
 *
 * @author dzhang
 * @since 2020/08/28
 */
public interface Witness {
    /**
     * 总看见的目击者，说明对应的插件可以不用关心植入目标的版本的
     *
     * @return 目击者
     */
    static Witness always() {
        return AlwaysWitness.INSTANCE;
    }

    /**
     * 通过比对Jar的META-INF来确定，是否支持拦截
     *
     * @param metaInfo Jar的META-INF信息
     * @return 目击者
     */
    static Witness metaInfo(MetaInfo metaInfo) {
        return new MetaInfoWitness(metaInfo);
    }

    /**
     * 目标构建路径中必须存在指定类名列表的类型，任意一个不存，都不算目击
     *
     * @param classes 类名列表
     * @return 目击者
     */
    static Witness classes(String... classes) {
        return new ClassesWitness(classes);
    }

    /**
     * Maven打包的Jar，通过指定GAV，来判断，是否匹配目标构建路径的Jar版本号；groupId和artifactId都是相等匹配，version是前缀匹配
     *
     * @param gav groupId:artifactId:version
     * @return 目击者
     */
    static Witness maven(GAV gav) {
        return new MavenWitness(gav);
    }

    /**
     * 看看指定类加载中，是否有我们关注的东西
     *
     * @param classLoader 负责载入应用系统的类加载器
     * @return 如果看到自己关注的东西，就返回<code>true</code>
     */
    boolean saw(ClassLoader classLoader);
}
