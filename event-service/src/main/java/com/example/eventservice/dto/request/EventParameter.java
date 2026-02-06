package com.example.eventservice.dto.request;

public record EventParameter(

        EventFilter eventFilter,

        String deviceId,

        Integer page,

        Integer pageSize
) {

    public static EventParameter of(EventFilter eventFilter, String deviceId, Integer page, Integer pageSize) {
        return new EventParameter(eventFilter, deviceId, page, pageSize);
    }
}
