package io.manbang.gravity.agent.proxy;

import io.manbang.gravity.agent.GravityAgent;
import lombok.extern.java.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.jar.JarFile;

/**
 * @author weilong.hu
 * @since 2021/09/17 09:48
 */
@Log
public class GravityAgentProxy {

    public static final String IGNORE_AGENT_DOWNLOAD = "true";

    private GravityAgentProxy() {
        throw new UnsupportedOperationException("插件代理不支持实例化");
    }

    public static void premain(String argument, Instrumentation instrumentation) {
        instrument(argument, instrumentation);
    }

    public static void agentmain(String argument, Instrumentation instrumentation) {
        instrument(argument, instrumentation);
    }

    private static void instrument(String argument, Instrumentation instrumentation) {
        log.info("GravityAgentProxy start......");
        System.setProperty("agent.options", argument);

        instrumentAgent(argument, instrumentation);
        log.info("GravityAgentProxy end......");
    }

    private static void instrumentAgent(String argument, Instrumentation instrumentation) {
        final String ignoreAgentDownload = System.getProperty("gravity.ignore.agent.download");
        if (IGNORE_AGENT_DOWNLOAD.equals(ignoreAgentDownload)) {
            //文案直白点 方便业务同学直接看懂
            log.info("忽略gravity agent下载, 无任何增强.");
            return;
        }
        try {
            final Path agentPath = AgentDownloader.retryDownloadAgent(3);
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(agentPath.toFile()));
            GravityAgent.premain(argument, instrumentation);
        } catch (Throwable throwable) {
            log.warning(String.format("instrument agent fail , terminates the current process:%s", getStackTrace(throwable)));
            //文案直白点 方便业务同学及时发现问题 联系定位
            log.warning("Gravity终止当前进程, 请联系Gravity负责人定位.");
            System.exit(0);
        }
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
