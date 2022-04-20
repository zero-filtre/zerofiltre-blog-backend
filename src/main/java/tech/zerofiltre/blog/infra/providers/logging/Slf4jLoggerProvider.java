package tech.zerofiltre.blog.infra.providers.logging;

import org.slf4j.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;


@Component
public class Slf4jLoggerProvider implements LoggerProvider {

    @Override
    public void log(LogEntry logEntry) {
        Throwable error = logEntry.getError();
        LogEntry.Level level = logEntry.getLevel();
        String message = logEntry.getMessage();
        Logger logger = LoggerFactory.getLogger(logEntry.getClazz());

        switch (level) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case WARN:
                logger.warn(message, error);
                break;
            case ERROR:
                logger.error(message, error);
                break;
            default:
                logger.info(message);
        }
    }
}
