package tech.zerofiltre.blog.infra.providers.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.metrics.*;
import tech.zerofiltre.blog.domain.metrics.model.*;

@Component
public class SimpleMetricsProvider implements MetricsProvider {
    @Autowired
    MeterRegistry meterRegistry;


    @Override
    public void incrementCounter(CounterSpecs specs) {
        Counter.builder(specs.getName())
                .tags(specs.getTags())
                .register(meterRegistry).increment();
    }
}
