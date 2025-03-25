package io.effi.rpc.common.util;

import java.net.*;
import java.util.Enumeration;

/**
 * Utility class for net operations.
 */
public final class NetUtil {

    private static volatile InetAddress LOCAL_ADDRESS = null;

    private static volatile String LOCAL_HOST = null;

    private NetUtil() {
    }

    /**
     * Validates the given IP and port string. If valid, returns the corresponding InetSocketAddress;
     * otherwise, returns null.
     *
     * @param address The string representing IP address and port in the format "IP:Port".
     * @return An InetSocketAddress if the input is valid; null otherwise.
     */
    public static InetSocketAddress validateAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // Find the last colon to separate IP and port
        int colonIndex = address.lastIndexOf(':');
        if (colonIndex == -1 || colonIndex == 0 || colonIndex == address.length() - 1) {
            return null; // No port or no IP part
        }

        String ip = address.substring(0, colonIndex);
        String portStr = address.substring(colonIndex + 1);

        // Validate IP
        if (!isValidIP(ip)) {
            return null;
        }

        // Validate port
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                return null; // Port must be in the range of 1-65535
            }
        } catch (NumberFormatException e) {
            return null; // Invalid port
        }

        return new InetSocketAddress(ip, port);
    }

    /**
     * Determine if a given port number is within the valid range (1-65535).
     *
     * @param port
     * @return
     */
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * Determine if a given string is a valid IP address.
     *
     * @param ip
     * @return
     */
    public static boolean isValidIP(String ip) {
        return isValidIPv4(ip) || isValidIPv6(ip);
    }

    /**
     * Determine if a given string is a legitimate IPV4 address.
     *
     * @param ip
     * @return
     */
    public static boolean isValidIPv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if a given string is a legitimate IPV6 address.
     *
     * @param ip
     * @return
     */
    public static boolean isValidIPv6(String ip) {
        // Split by colon (IPv6 segments are separated by colons)
        String[] parts = ip.split(":");
        if (parts.length != 8) {
            return false;
        }

        for (String part : parts) {
            // Check if each part has between 1 to 4 hexadecimal characters
            if (part.isEmpty() || part.length() > 4) {
                return false;
            }
            try {
                // Parse each part as a hexadecimal number
                int num = Integer.parseInt(part, 16);
                if (num < 0 || num > 0xFFFF) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a given InetSocketAddress object to a string representation.
     *
     * @param address
     * @return
     */
    public static String toAddress(InetSocketAddress address) {
        return (isLoopbackAddress(address.getHostString())
                ? toAddress(defaultHost(), address.getPort())
                : toAddress(address.getHostString(), address.getPort()));
    }

    /**
     * Determine whether two InetSocketAddress objects are the same.
     *
     * @param addr1
     * @param addr2
     * @return
     */
    public static boolean isSameAddress(InetSocketAddress addr1, InetSocketAddress addr2) {
        if (addr1 == null || addr2 == null) {
            return false;
        }
        return addr1.getAddress().equals(addr2.getAddress()) && addr1.getPort() == addr2.getPort();
    }

    /**
     * If it is a loopback address.
     *
     * @param host
     * @return
     */
    public static boolean isLoopbackAddress(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Converts the given string to an InetSocketAddress object.
     *
     * @param address
     * @return
     */
    public static InetSocketAddress toInetSocketAddress(String address) {
        InetSocketAddress socketAddress = validateAddress(address);
        if (socketAddress == null) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        return socketAddress;
    }

    /**
     * Converts the given hostname and port number into a string representation.
     *
     * @param host
     * @param port
     * @return
     */
    public static String toAddress(String host, int port) {
        return host + ":" + port;
    }

    /**
     * Get the local host.
     *
     * @return
     */
    public static String defaultHost() {
        if (LOCAL_HOST != null) {
            return LOCAL_HOST;
        }

        InetAddress localAddress = getFirstLocalAddress();
        if (localAddress != null) {
            return localAddress.getHostAddress();
        }
        return null;
    }

    private static InetAddress getFirstLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // Exclude loopback interfaces and disabled interfaces
                if (networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        // Exclude loopback addresses and IPv6 addresses
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
                            LOCAL_ADDRESS = inetAddress;
                            LOCAL_HOST = inetAddress.getHostAddress();
                            return LOCAL_ADDRESS;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
