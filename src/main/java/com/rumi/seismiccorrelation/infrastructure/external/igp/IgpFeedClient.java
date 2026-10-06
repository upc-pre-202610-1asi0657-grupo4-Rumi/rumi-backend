package com.rumi.seismiccorrelation.infrastructure.external.igp;

import com.rumi.seismiccorrelation.domain.model.SeismicEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IgpFeedClient {

    private final IgpSeismicEventMapper mapper;

    public IgpFeedClient(IgpSeismicEventMapper mapper) {
        this.mapper = mapper;
    }

    public SeismicEvent processResponse(IgpSeismicEventResponse response) {
        return mapper.toDomain(response);
    }

    @CircuitBreaker(name = "igpFeed", fallbackMethod = "igpUnavailableFallback")
    public Optional<SeismicEvent> fetchLatestEvent(IgpFeedOperation feedOperation) {
        return Optional.ofNullable(feedOperation.fetchLatest())
                .map(this::processResponse);
    }

    public Optional<SeismicEvent> igpUnavailableFallback(
            IgpFeedOperation feedOperation,
            Throwable failure
    ) {
        return Optional.empty();
    }
}
