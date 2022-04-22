package io.manbang.gravity.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 调整类加载器加载机制，优先从当前类加载器加载资源，针对guava等容易依赖冲突得第三方包，打包时不用再shade修改包名等处理
 * 可能出现得问题：
 * 如果插件打包时，引入了多余得jar包可能出现如下问题
 * 1.spring等框架根据注解扫描得方式失效
 * 2.SPI初始化异常
 * 3.常量获取不对，常见用ThreadLocal传递上下文
 * 4.。。。。
 * 原因主要是，目标业务使用得class是由业务自身得类加载器加载，在gravity内部得class是由AgentPluginClassLoader加载，相同得class加载两次导致异常，所以插件打包时，要排除多余得jar
 *
 * @author duoliang.zhang
 */
public class AgentPluginClassLoader extends URLClassLoader {
    private static final AtomicReference<URL[]> AGENT_JAR_URLS = new AtomicReference<>();
    private static Set<String> FILTER_RESOURCES = new HashSet<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private boolean useParent = true;

    /**
     * 默认类加载器useParent = true,因为影响io.manbang.gravity.plugin.ttl.TtlPluginDefine#prepare等需要对jdk源码织入的插件
     */
    public AgentPluginClassLoader() {
        super(getAgentJarUrls());
    }

    public AgentPluginClassLoader(ClassLoader parent) {
        this(parent, false);
    }

    public AgentPluginClassLoader(ClassLoader parent, boolean useParent) {
        super(getAgentJarUrls(), parent);
        this.useParent = useParent;
    }

    public static void addFilterResources(String... names) {
        FILTER_RESOURCES.addAll(Arrays.asList(names));
    }

    private static URL[] getAgentJarUrls() {
        URL[] urls = AGENT_JAR_URLS.get();
        if (urls != null) {
            return urls;
        }

        urls = GravityHome.INSTANCE.listFileUrls(JarType.AGENT);
        AGENT_JAR_URLS.compareAndSet(null, urls);
        return urls;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (useParent) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException ignore) {
                    //
                }
                if (c == null) {
                    c = super.loadClass(name, resolve);
                } else if (resolve) {
                    resolveClass(c);
                }

            }
            return c;
        }
    }

    @Override
    public URL getResource(String name) {
        if (!FILTER_RESOURCES.contains(name)) {
            return super.getResource(name);
        }
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (!FILTER_RESOURCES.contains(name)) {
            return super.getResources(name);
        }
        return findResources(name);
    }
}
