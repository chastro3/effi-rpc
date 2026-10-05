package io.effi.rpc.exception;

/**
 * Defines predefined error codes with their messages.
 */
public enum PredefinedErrorCode implements ErrorCode {
    COMMON("0001", "{}"),
    SERIALIZE("0002", "Failed to serialize object '{}'"),
    DESERIALIZE("0003", "Failed to deserialize bytes to '{}'"),
    ENCODE("0004", "Failed to encode object '{}' from '{}'"),
    DECODE("0005", "Failed to decode object '{}' from '{}'"),
    COMPRESS("0006", "Failed to compress bytes"),
    DECOMPRESS("0007", "Failed to decompress bytes"),
    BIND("0008", "Failed to bind server to '{}'"),
    CONNECT("0009", "Failed to connect to '{}'"),
    CLOSE_RESOURCE("0010", "Failed to close '{}' resource"),
    CLOSE_CHANNEL("0011", "Failed to close remote channel to '{}'"),
    CLOSE_SERVER("0012", "Failed to close server at '{}'"),
    TIMEOUT("0013", "Failed to call remote service: Timeout after '{}' milliseconds,future id is'{}'"),
    NOT_FOUND_CALLEE("0014", "Callee not found for '{}'"),
    READ_CERT_FILE("0015", "Failed to read certificate file '{}'"),
    CREATE_SSL("0016", "Failed to create SSL context for {}"),
    INVOKE_SERVICE("0017", "Failed to invoke service: '{}'"),
    CALL_CALLER("0018", "Failed to call caller: '{}'"),
    FETCH_CHANNEL("0019", "Failed to fetch channel to '{}' over ({}) protocol"),
    HANDLE_EVENT("0020", "Failed to handle event in '{}' for event of type '{}'"),
    NOT_FOUND_SERVICE("0021", "Service(s) not found for '{}'"),
    REGISTRY_REGISTER("0022", "Failed to register service(s) for '{}' in registry at '{}'"),
    REGISTRY_DEREGISTER("0023", "Failed to deregister service(s) for '{}' in registry at '{}'"),
    REGISTRY_DISCOVER("0024", "Failed to discover service(s) for '{}' in registry at '{}'"),
    REGISTRY_SUBSCRIBE("0025", "Failed to subscribe services(s) for '{}' in registry at '{}'"),
    PROXY_CREATE("0026", "Failed to create proxy for '{}'"),
    CHANNEL_WRITE("0027", "Failed to write data to channel '{}'"),
    CHANNEL_READ("0028", "Failed to read data from channel '{}'"),
    CALL_CANCELLED("0029", "Call was cancelled: '{}'"),
    DEADLINE_EXCEEDED("0030", "Call deadline exceeded after '{}'"),
    SERVICE_UNAVAILABLE("0031", "Remote service is unavailable: '{}'");

    private final String code;

    private final String message;

    PredefinedErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
