package tech.zerofiltre.blog.domain.metrics;

import tech.zerofiltre.blog.domain.metrics.model.*;

public interface MetricsProvider {

    void incrementCounter(CounterSpecs specs);
}
