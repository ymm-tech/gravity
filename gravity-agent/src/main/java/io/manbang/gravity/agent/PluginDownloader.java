package io.manbang.gravity.agent;

import io.manbang.gravity.fastjson.JSON;
import io.manbang.gravity.okhttp3.Cache;
import io.manbang.gravity.okhttp3.Call;
import io.manbang.gravity.okhttp3.MediaType;
import io.manbang.gravity.okhttp3.OkHttpClient;
import io.manbang.gravity.okhttp3.Request;
import io.manbang.gravity.okhttp3.RequestBody;
import io.manbang.gravity.okhttp3.Response;
import io.manbang.gravity.okhttp3.ResponseBody;
import io.manbang.gravity.plugin.AgentOptions;
import io.manbang.gravity.plugin.GravityHome;
import io.manbang.gravity.plugin.JarType;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static io.manbang.gravity.agent.GravityBaseInfo.baseInfo;

/**
 * @author duoliang.zhang
 * @since 2020/8/21 17:15
 */
@Log
public class PluginDownloader {
    private static final int MAX_RETRIES = 3;
    private static final OkHttpClient http;
    private static final Set<String> DOWNLOADED_PLUGINS = new HashSet<>();

    static {
        Path cacheDirPath = GravityHome.INSTANCE.resolvePath("cache");
        File cacheDir = cacheDirPath.toFile();
        boolean created = cacheDir.mkdirs();
        if (created) {
            log.info("创建插件缓存目录成功");
        }

        Cache cache = new Cache(cacheDir, Integer.MAX_VALUE);
        String version = PluginDownloader.class.getPackage().getImplementationVersion();
        final String env = AgentOptions.INSTANCE.getString("env", "None");
        log.info("Gravity Version: " + version);

        http = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("User-Agent", "Gravity")
                            .header("Gravity-Version", version)
                            .header("Gravity-Env", env)
                            .header("App-Name", AgentOptions.INSTANCE.getAppName())
                            .build();
                    return chain.proceed(request);
                })
                .cache(cache)
                .build();
    }

    private PluginDownloader() {
        throw new UnsupportedOperationException();
    }

    /**
     * 下载插件
     *
     * @param instrumentation instrumentation
     * @param options         代理的配置参数
     */
    public static void downloadPlugins(Instrumentation instrumentation, AgentOptions options) {
        PluginVo[] plugins;
        try {
            plugins = getPlugins(options);
        } catch (Exception e) {
            log.log(Level.WARNING, e, () -> "获取插件列表URL地址失败，业务不受影响");
            plugins = new PluginVo[0];
        }
        downloadPluginsConcurrently(plugins);
        for (JarType type : JarType.values()) {
            type.appendClassPath(instrumentation, GravityHome.INSTANCE.getHome());
        }
    }

    public static boolean checkDownloadedPlugins() {
        String checkUrl = String.format("%s/gravity/check", AgentOptions.INSTANCE.getBaseUrl());
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), JSON.toJSONBytes(DOWNLOADED_PLUGINS));
        Request checkRequest = new Request.Builder().url(checkUrl).post(body).build();

        int retries = 1;

        do {
            try (Response response = http.newCall(checkRequest).execute()) {
                int code = response.code();
                // 404 服务器访问是正常的 404
                // 500 本地访问会出现
                if (code == 404 || code == 500) {
                    return true;
                }

                return Optional.ofNullable(response.body()).map(b -> {
                    try {
                        return b.string();
                    } catch (IOException ignore) {
                        return "false";
                    }
                }).map(Boolean::parseBoolean).orElse(false);
            } catch (IOException e) {
                log.log(Level.WARNING, "检查插件Gravity依赖关系失败，不影响业务。", e);
            }
        } while (retries++ < MAX_RETRIES);

        return false;
    }

    private static void downloadPluginsConcurrently(PluginVo[] plugins) {
        CountDownLatch latch = new CountDownLatch(plugins.length);
        ExecutorService executor = new ThreadPoolExecutor(10, 10, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new GravityThreadFactory());

        for (PluginVo plugin : plugins) {
            executor.execute(() -> {
                downloadPluginWithRetry(plugin);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.log(Level.WARNING, "download plugin latch await", e);
        }
        // 用完了，直接关停
        executor.shutdown();
    }

    private static void downloadPluginWithRetry(PluginVo plugin) {
        int retries = 1;
        do {
            try {
                downloadPlugin(plugin);
                DOWNLOADED_PLUGINS.add(plugin.getFilename());
                break;
            } catch (Exception e) {
                log.log(Level.WARNING, e, () -> "下载Jar失败，不影响业务：" + plugin);
            }
        } while (retries++ < MAX_RETRIES);
    }

    private static void downloadPlugin(PluginVo plugin) throws IOException {
        String filename = plugin.getFilename();
        // 先放到临时目录
        File tempFile = writeToTempFile(plugin);
        // 然后得到Jar的配置清单，得到Jar包类型
        JarType[] jarTypes = getJarTypes(plugin);

        for (JarType jarType : jarTypes) {
            // 然后将Jar放置到指定目标目录
            String dir = jarType.name().toLowerCase();
            Path target = GravityHome.INSTANCE.resolvePath(String.format("%s/%s", dir, filename));
            Files.copy(tempFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            log.info(() -> String.format("%s(%s)", filename, jarType));
        }
    }

    private static JarType[] getJarTypes(PluginVo plugin) {
        if (plugin.getJarTypes().length == 0) {
            return new JarType[]{JarType.APP};
        }
        return Arrays.stream(plugin.getJarTypes())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(JarType::valueOf)
                .toArray(JarType[]::new);
    }

    private static File writeToTempFile(PluginVo plugin) throws IOException {
        File tempFile;
        String pluginUrl = String.format("%s/gravity%s", AgentOptions.INSTANCE.getBaseUrl(), plugin.getPath());
        Request downloadRequest = new Request.Builder().url(pluginUrl).build();

        try (Response response = http.newCall(downloadRequest).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("下载插件失败：" + pluginUrl);
            }

            tempFile = File.createTempFile(plugin.getFilename(), ".tmp");
            Files.copy(body.byteStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    private static PluginVo[] getPlugins(AgentOptions options) throws IOException {
        if (options.isLocalDebug()) {
            return new PluginVo[0];
        }

        String appName = options.getAppName();
        String baseUrl = options.getBaseUrl();

        log.info(() -> String.format("获取【%s】插件列表", appName));
        String url = String.format("%s/gravity/agent/plugins?appName=%s", baseUrl, appName);

        final String argument = System.getProperty("agent.options");
        final String baseInfo = baseInfo(argument);
        final Request request = new Request.Builder().post(RequestBody.create(MediaType.get("application/json"), baseInfo)).url(url).build();
        Call call = http.newCall(request);

        try (Response response = call.execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                return new PluginVo[0];
            }

            return JSON.parseObject(body.byteStream(), PluginVo[].class);
        }
    }

    private static class GravityThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private GravityThreadFactory() {
            namePrefix = "gravity-plugin-download-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}

