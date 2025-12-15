package com.example.apiorchestrator.domain.dto.request.filter;

public record EventParameter(

        EventFilter eventFilter,

        String deviceId,

        Integer page,

        Integer pageSize
) {

    public static EventParameter of(EventFilter eventFilter, String deviceId, Integer page, Integer pageSize) {
        return new EventParameter(eventFilter, deviceId, page, pageSize);
    }

    public Long fromTimestamp() {
        return eventFilter.fromTimestamp();
    }

    public Long toTimestamp() {
        return eventFilter.toTimestamp();
    }

    public String type() {
        return eventFilter.type();
    }
}
