package io.manbang.gravity.agent.proxy;

import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static io.manbang.gravity.agent.proxy.GravityBaseInfo.baseInfo;

/**
 * 下载agent
 *
 * @author weilong.hu
 * @since 2021/09/17 13:48
 */
@Log
public class AgentDownloader {

    public static Path retryDownloadAgent(int retry) throws Throwable {
        final String baseUrl = AgentProxyOptions.INSTANCE.getBaseUrl();
        final String agentUrl = String.format("%s/gravity/agent", baseUrl);
        final String argument = System.getProperty("agent.options");
        final String baseInfo = baseInfo(argument);
        Throwable t = null;
        Path agent = null;
        for (int i = 1; i <= retry; i++) {
            try {
                log.info(String.format("Download agent for the %s time", i));
                agent = downloadAgent(agentUrl, baseInfo);
                break;
            } catch (Throwable suppress) {
                t = suppress;
            }
        }
        if (agent != null) {
            return agent;
        }
        if (t != null) {
            throw t;
        }
        throw new IllegalStateException("Failed to download agent!");
    }

    private static Path downloadAgent(String agentUrl, String baseInfo) throws IOException {
        final Path agent = GravityProxyHome.INSTANCE.resolvePath("base/gravity-agent.jar");
        if (AgentProxyOptions.INSTANCE.isLocalDebug() && agent.toFile().exists()) {
            log.info("Currently in debug mode, and there is already an agent locally, ignore this download.");
            return agent;
        }
        log.info(String.format("The agent url : %s", agentUrl));
        final URL url = new URL(agentUrl);
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.getOutputStream().write(baseInfo.getBytes(StandardCharsets.UTF_8));
            //因安全要求调整 gravity域名变成https 但是阿里云的oss地址是http http与https之间没办法自动重定向 于是禁用重定向 自己手动处理
            conn.setInstanceFollowRedirects(false);
            final int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                inputStream = new URL(conn.getHeaderField("Location")).openStream();
                Files.copy(inputStream, agent, StandardCopyOption.REPLACE_EXISTING);
                log.info("The download of the agent successfully.");
                return agent;
            } else {
                log.info(String.format("The download of the agent failed, response code:%s.", responseCode));
                return null;
            }
        } finally {
            if (Objects.nonNull(conn)) {
                conn.disconnect();
            }
            if (Objects.nonNull(inputStream)) {
                inputStream.close();
            }
        }

    }
}
