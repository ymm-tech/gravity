package io.manbang.gravity.plugin;

import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dzhang
 */
@Log
public class GravityUtils {
    private static final Map<String, byte[]> BYTE_CODES = new HashMap<>();
    private static final AtomicReference<Instrumentation> INSTRUMENTATION_REF = new AtomicReference<>();
    private static final AtomicReference<AgentPluginClassLoader> AGENT_PLUGIN_CLASS_LOADER_REF = new AtomicReference<>();

    private GravityUtils() {
        throw new UnsupportedOperationException();
    }

    public static Instrumentation getInstrumentation() {
        return INSTRUMENTATION_REF.get();
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        INSTRUMENTATION_REF.compareAndSet(null, instrumentation);
    }

    public static void setAgentPluginClassLoader(AgentPluginClassLoader classLoader) {
        AGENT_PLUGIN_CLASS_LOADER_REF.compareAndSet(null, classLoader);
    }

    public static AgentPluginClassLoader getAgentPluginClassLoader() {
        return AGENT_PLUGIN_CLASS_LOADER_REF.get();
    }

    public static void putByteCodes(String typeName, byte[] bytes) {
        BYTE_CODES.put(typeName, bytes);
    }

    public static byte[] getByteCodes(String typeName) {
        return BYTE_CODES.get(typeName);
    }

    public static void clearByteCodes() {
        BYTE_CODES.clear();
    }

    public static Map<String, byte[]> getAllByteCodes() {
        return Collections.unmodifiableMap(BYTE_CODES);
    }

    public static byte[] getAdviceTemplateByteCodes(String adviceClassName) {
        return getByteCodes(adviceClassName);
    }

    public static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            log.severe("uri地址转换异常：" + uri);
            e.printStackTrace();
            return null;
        }
    }

    public static URL toUrl(String uri) {
        try {
            return URI.create(uri).toURL();
        } catch (MalformedURLException e) {
            log.severe("uri地址转换异常：" + uri);
            throw new IllegalArgumentException(uri, e);
        }
    }

    public static byte[] getByteCodes(String className, ClassLoader classLoader) {
        InputStream stream = classLoader.getResourceAsStream(className.replace(".", "/").concat(".class"));
        if (stream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int r = stream.read(buffer);
                if (r == -1) {
                    break;
                }
                out.write(buffer, 0, r);
            } catch (IOException e) {
                throw new GravityException(e);
            }
        }

        return out.toByteArray();
    }

    public static String getUriQueryParamValue(String uri, String paramName) {
        URL url = toUrl(uri);

        String query = url.getQuery();
        if (query == null) {
            return url.getPath().substring(url.getPath().lastIndexOf('/'));
        }

        String[] parameters = query.split("&");
        for (String parameter : parameters) {
            String[] nameValue = parameter.split("=");
            if (nameValue.length == 2 && paramName.equals(nameValue[0])) {
                return nameValue[1];
            }
        }

        return url.getPath().substring(url.getPath().lastIndexOf('/'));
    }

    public static String getStackTrace(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
