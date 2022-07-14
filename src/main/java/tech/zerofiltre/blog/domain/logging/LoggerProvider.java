package tech.zerofiltre.blog.domain.logging;

import tech.zerofiltre.blog.domain.logging.model.*;

public interface LoggerProvider {
    void log(LogEntry logEntry);

}
