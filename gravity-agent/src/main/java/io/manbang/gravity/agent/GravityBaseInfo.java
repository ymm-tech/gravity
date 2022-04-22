package io.manbang.gravity.agent;

import lombok.extern.java.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author weilong.hu
 * @since 2021/09/17 11:38
 */
@Log
class GravityBaseInfo {

    public static String baseInfo(String argument) {
        Map<String, String> baseInfo = new HashMap<>(64);
        System.getProperties().forEach((k, v) -> baseInfo.put(getString(k), getString(v)));
//        System.getenv().forEach((k, v) -> baseInfo.put(getString(k), getString(v)));
        baseInfo.put("os.all.ip", getString(getAllLocalIp()));
        baseInfo.put("agent.argument", getString(argument));
        baseInfo.put("agent.proxy.version", GravityBaseInfo.class.getPackage().getImplementationVersion());
        return info(baseInfo);
    }

    private static String getString(Object o) {
        String str = String.valueOf(o);
        str = str.replaceAll("\t", "");
        str = str.replaceAll("\r|\n", "");
        str = str.replace("\"", "'");
        str = str.replace("\\", "\\\\");
        return str;
    }

    private static String info(Map<String, String> info) {
        StringBuilder sb = new StringBuilder("{");
        info.forEach((k, v) -> {
            sb.append('"').append(k).append('"').append(":");
            sb.append('"').append(v).append('"').append(",");
        });
        sb.append('"').append("GravityAgentProxy").append('"').append(":");
        sb.append('"').append("满帮技术平台中间件").append('"');
        sb.append("}");
        return sb.toString();
    }

    public static List<String> getAllLocalIp() {
        List<String> noLoopbackAddresses = new ArrayList<>();
        List<InetAddress> allInetAddresses = getAllLocalAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                noLoopbackAddresses.add(address.getHostAddress());
            }
        }

        return noLoopbackAddresses;
    }

    public static List<InetAddress> getAllLocalAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            List<InetAddress> addresses = new ArrayList<>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            log.warning(e.toString());
            return Collections.emptyList();
        }
    }
}
