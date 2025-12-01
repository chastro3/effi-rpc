package io.effi.rpc.component.event.v2;

public abstract class TimestampedEvent implements Event {

    private final long timestamp;

    public TimestampedEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long timestamp() {
        return timestamp;
    }

}
