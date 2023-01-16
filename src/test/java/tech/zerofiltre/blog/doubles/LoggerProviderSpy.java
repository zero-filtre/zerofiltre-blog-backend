package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.logging.model.*;

public class LoggerProviderSpy implements LoggerProvider {

    public boolean logCalled;


    @Override
    public void log(LogEntry logEntry) {
        logCalled = true;
    }
}

