package com.example.deviceservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainType {

    DEVICE("device"),
    COMMON("common");

    private final String name;
}
