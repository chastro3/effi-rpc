package io.effi.rpc.util;

import io.effi.rpc.constant.SystemKeys;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Provides net operations.
 */
public final class NetUtil {

    private static volatile InetAddress LOCAL_ADDRESS = null;

    private static volatile String LOCAL_HOST = null;

    /**
     * Validates the given IP and port string.
     * Returns the corresponding InetSocketAddress if valid, null otherwise.
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

        return InetSocketAddress.createUnresolved(ip, port);
    }

    /**
     * Checks if the given port number is within the valid range (1-65535).
     */
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * Checks if the given string is a valid IP address (IPv4 or IPv6).
     */
    public static boolean isValidIP(String ip) {
        if (StringUtil.isBlank(ip)) return false;
        return isValidIPv4(ip) || isValidIPv6(ip);
    }

    /**
     * Checks if the given string is a valid IPv4 address.
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
     * Checks if the given string is a valid IPv6 address.
     */
    public static boolean isValidIPv6(String ip) {
        // Split by colon (IPv6 segments are separated by colons)
        String[] parts = ip.split(":");
        if (parts.length != 8) {
            return false;
        }

        for (String part : parts) {
            // Check if each part has between 1 and 4 hexadecimal characters
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
     * Resolves the given address if it is unresolved.
     */
    public static InetSocketAddress resolveIfUnresolved(InetSocketAddress address) {
        if (!address.isUnresolved()) {
            return address;
        }
        try {
            InetAddress resolved = InetAddress.getByName(address.getHostString());
            return new InetSocketAddress(resolved, address.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to resolve host: " + address.getHostString(), e);
        }
    }


    /**
     * Converts an InetSocketAddress to its string representation.
     */
    public static String toAddress(InetSocketAddress address) {
        return (isLoopbackAddress(address.getHostString())
                ? toAddress(localHost(), address.getPort())
                : toAddress(address.getHostString(), address.getPort()));
    }

    /**
     * Checks if two InetSocketAddress objects are the same.
     */
    public static boolean isSameAddress(InetSocketAddress addr1, InetSocketAddress addr2) {
        if (addr1 == null || addr2 == null) {
            return false;
        }
        return addr1.getAddress().equals(addr2.getAddress()) && addr1.getPort() == addr2.getPort();
    }

    /**
     * Checks if the given host is a loopback address.
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
     * Converts the given string to an InetSocketAddress.
     */
    public static InetSocketAddress toInetSocketAddress(String address) {
        InetSocketAddress socketAddress = validateAddress(address);
        if (socketAddress == null) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
        return socketAddress;
    }

    /**
     * Converts the given hostname and port into a string representation.
     */
    public static String toAddress(String host, int port) {
        return host + ":" + port;
    }

    /**
     *  Gets the local host address.
     */
    public static String localHost() {
        if (LOCAL_HOST != null) return LOCAL_HOST;
        String configuredHost = System.getProperty(SystemKeys.LOCAL_HOST);
        if (isValidIP(configuredHost)) {
            return LOCAL_HOST = configuredHost;
        }
        InetAddress localAddress = findFirstIPv4();
        if (localAddress != null) {
            LOCAL_ADDRESS = localAddress;
            return LOCAL_HOST = localAddress.getHostAddress();
        }
        return null;
    }

    private static InetAddress findFirstIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) continue;
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (isUsableIP(address)) return address;
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException("Failed to get local host", e);
        }
        return null;
    }

    private static boolean isUsableIP(InetAddress address) {
        return address != null && !address.isLoopbackAddress() && address instanceof Inet4Address;
    }

    private NetUtil() {
    }
}
