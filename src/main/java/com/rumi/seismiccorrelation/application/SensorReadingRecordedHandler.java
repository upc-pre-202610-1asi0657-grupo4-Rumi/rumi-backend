package com.rumi.seismiccorrelation.application;

import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SensorReadingRecordedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorReadingRecordedHandler.class);

    public void handle(SensorReadingRecorded event) {
        LOGGER.info(
                "Sensor reading event {} received for seismic correlation",
                event.eventId()
        );
    }
}
