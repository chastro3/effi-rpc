package io.effi.rpc.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoggerFactoryTest {

    @AfterEach
    void resetAdapter() {
        LoggerFactory.clearAdapter();
    }

    @Test
    void usesInstalledAdapter() {
        RecordingLogger logger = new RecordingLogger();
        LoggerFactory.setAdapter(new RecordingLoggerAdapter(logger));
        logger.messages().clear();

        LoggerFactory.getLogger(LoggerFactoryTest.class).info("hello {}", "world");

        assertEquals(List.of("hello world"), logger.messages());
    }

    @Test
    void skipsDisabledLevels() {
        RecordingLogger logger = new RecordingLogger();
        logger.enabled = false;
        LoggerFactory.setAdapter(new RecordingLoggerAdapter(logger));

        LoggerFactory.getLogger(LoggerFactoryTest.class).info("hidden");

        assertFalse(logger.messages().iterator().hasNext());
    }

    @Test
    void clearAdapterRestoresSelection() {
        LoggerFactory.setAdapter(new RecordingLoggerAdapter(new RecordingLogger()));
        LoggerFactory.clearAdapter();

        assertNotNull(LoggerFactory.getLogger(LoggerFactoryTest.class));
    }

    @Test
    void logsAdapterSwitchAtDebug() {
        RecordingLogger logger = new RecordingLogger();

        LoggerFactory.setAdapter(new RecordingLoggerAdapter(logger));

        assertEquals(
                List.of("[effi-rpc] Logging adapter switched: recording"),
                logger.messages()
        );
    }

    private static final class RecordingLoggerAdapter implements LoggerAdapter {

        private final RecordingLogger logger;

        private RecordingLoggerAdapter(RecordingLogger logger) {
            this.logger = logger;
        }

        @Override
        public String name() {
            return "recording";
        }

        @Override
        public Logger getLogger(String name) {
            return logger;
        }
    }

    private static final class RecordingLogger extends AbstractLogger {

        private final List<String> messages = new ArrayList<>();

        private boolean enabled = true;

        private List<String> messages() {
            return messages;
        }

        @Override
        public boolean isTraceEnabled() {
            return enabled;
        }

        @Override
        public boolean isDebugEnabled() {
            return enabled;
        }

        @Override
        public boolean isInfoEnabled() {
            return enabled;
        }

        @Override
        public boolean isWarnEnabled() {
            return enabled;
        }

        @Override
        public boolean isErrorEnabled() {
            return enabled;
        }

        @Override
        protected void trace(String msg, Throwable e) {
            messages.add(msg);
        }

        @Override
        protected void debug(String msg, Throwable e) {
            messages.add(msg);
        }

        @Override
        protected void info(String msg, Throwable e) {
            messages.add(msg);
        }

        @Override
        protected void warn(String msg, Throwable e) {
            messages.add(msg);
        }

        @Override
        protected void error(String msg, Throwable e) {
            messages.add(msg);
        }
    }
}
