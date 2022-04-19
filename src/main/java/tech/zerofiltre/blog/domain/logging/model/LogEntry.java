package tech.zerofiltre.blog.domain.logging.model;

public class LogEntry {

    private final Level level;
    private final Throwable error;
    private final String message;
    private final Class<?> clazz;

    public LogEntry(Level level, String message, Throwable error, Class<?> clazz) {
        this.level = level;
        this.message = message;
        this.error = error;
        this.clazz = clazz;
    }

    public Level getLevel() {
        return level;
    }

    public Throwable getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
}
