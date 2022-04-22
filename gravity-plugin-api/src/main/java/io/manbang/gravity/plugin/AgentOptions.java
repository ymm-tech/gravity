package io.manbang.gravity.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理配置选项
 *
 * @author dzhang
 * @since 2020/08/18
 */
public enum AgentOptions {
    INSTANCE;

    private final String argument;
    private final Map<String, String> options;

    AgentOptions() {
        this.argument = System.getProperty("agent.options");
        this.options = doParse(argument);
    }

    private Map<String, String> doParse(String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> optionMap = new HashMap<>(8);

        String[] arguments = argument.split("\\s*,\\s*");
        for (String arg : arguments) {
            String[] keyValue = arg.split("\\s*=\\s*");

            if (keyValue.length == 0) {
                continue;
            }

            if (keyValue.length == 1) {
                optionMap.put(keyValue[0], null);
            }

            if (keyValue.length == 2) {
                optionMap.put(keyValue[0], keyValue[1]);
            }
        }
        optionMap.forEach((k, v) -> System.setProperty("gravity." + k, v));
        return optionMap;
    }

    public boolean isLocalDebug() {
        return getBoolean("localDebug", false);
    }

    public String getString(String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return options.containsKey(key) ? Boolean.parseBoolean(options.get(key)) : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        return options.containsKey(key) ? Integer.parseInt(options.get(key)) : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        return options.containsKey(key) ? Long.parseLong(options.get(key)) : defaultValue;
    }

    public String getString(String key) {
        return options.get(key);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(options.get(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(options.get(key));
    }

    public long getLong(String key) {
        return Long.parseLong(options.get(key));
    }

    public String getArgument() {
        return argument;
    }

    public String getAppName() {
        return getString("appName");
    }

    public String getAppType() {
        return getString("appType");
    }

    public String getBaseUrl() {
        return getString("baseUrl");
    }
}
