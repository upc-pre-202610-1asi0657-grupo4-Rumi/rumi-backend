package com.rumi.seismiccorrelation.infrastructure.external.igp;

@FunctionalInterface
public interface IgpFeedOperation {

    IgpSeismicEventResponse fetchLatest();
}
