package io.effi.rpc.common.exception;

/**
 * Predefined error codes with associated messages.
 */
public enum PredefinedErrorCode implements ErrorCode {
    COMMON("0001", "{}"),
    SERIALIZE("0002", "Failed to serialize object '{}'"),
    DESERIALIZE("0003", "Failed to deserialize bytes to '{}'"),
    ENCODE("0004", "Failed to encode object '{}' from '{}'"),
    DECODE("0005", "Failed to decode object '{}' from '{}'"),
    COMPRESS("0006", "Failed to compress bytes"),
    DECOMPRESS("0007", "Failed to decompress bytes"),
    BIND("0008", "Failed to bind server to '{}' using protocol '{}'"),
    CONNECT("0009", "Failed to connect to '{}' using protocol '{}'"),
    CLOSE("0010", "Failed to close to '{}' using protocol '{}'"),
    TIMEOUT("0011", "\"Failed to call remote service: Timeout after '{}' milliseconds"),
    NOT_FOUND_CALLEE("0011", "Callee not found for '{}'"),
    READ_CERT_RESOURCE("0012", "Failed to read certificate resource"),
    SSL("0013", "SSL error while processing {}"),
    INVOKE_SERVICE("0014", "Failed to invoke service: '{}'"),
    CALL_CALLER("0015", "Failed to call caller: '{}'"),
    ACQUIRE_CHANNEL("0016", "Failed to acquire connection to {} over {} channel"),
    HANDLE_EVENT("0017", "Failed to handle event in '{}' for event of type '{}'"),
    NOT_FOUND_SERVICE("0018", "Service(s) not found for '{}'"),
    REGISTRY_REGISTER("0019", "Failed to register service(s) for '{}' in registry at '{}'"),
    REGISTRY_DEREGISTER("0020", "Failed to deregister service(s) for '{}' in registry at '{}'"),
    REGISTRY_DISCOVER("0021", "Failed to discover service(s) for '{}' in registry at '{}'"),
    REGISTRY_SUBSCRIBE("0022", "Failed to subscribe services(s) for '{}' in registry at '{}'"),
    PROXY_CREATE("0023", "Failed to create proxy for '{}'"),
    CHANNEL_WRITE("0024", "Failed to write data to channel '{}'"),
    CHANNEL_READ("0025", "Failed to read data from channel '{}'");

    private final String code;

    private final String message;

    PredefinedErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the code.
     *
     * @return the code
     */
    @Override
    public String code() {
        return code;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    @Override
    public String message() {
        return message;
    }

}
