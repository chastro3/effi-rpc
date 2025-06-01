package io.effi.rpc.config.transport;

public interface TrafficShapingConfig {

    long inboundGlobalBandwidth();

    long outboundGlobalBandwidth();

    long peakOutboundGlobalBandwidth();

    long maxDelayToWait();

    long checkIntervalForStats();
}
