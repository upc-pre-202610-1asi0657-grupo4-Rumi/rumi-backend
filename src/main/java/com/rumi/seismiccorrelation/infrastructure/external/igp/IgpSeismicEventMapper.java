package com.rumi.seismiccorrelation.infrastructure.external.igp;

import com.rumi.seismiccorrelation.domain.model.SeismicEvent;
import org.springframework.stereotype.Component;

@Component
public class IgpSeismicEventMapper {

    public SeismicEvent toDomain(IgpSeismicEventResponse response) {
        return new SeismicEvent(
                response.code(),
                response.magnitude(),
                response.timestamp(),
                response.latitude(),
                response.longitude(),
                response.depthKm()
        );
    }
}
