package com.example.apiorchestrator.domain.mapper;

import com.example.proto.CommandRequest;
import com.example.proto.CommandType;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Component
public class RouterManagerMapper {

    public CommandRequest toProtoCommand(com.example.apiorchestrator.domain.dto.request.CommandRequest requestIn) {
        return CommandRequest.newBuilder()
                .setRouterId(defaultIfEmpty(requestIn.routerId(), EMPTY))
                .setPayload(defaultIfEmpty(requestIn.payload(), EMPTY))
                .setCommandType(toProtoCommandType(requestIn))
                .build();
    }

    private CommandType toProtoCommandType(com.example.apiorchestrator.domain.dto.request.CommandRequest requestIn) {
        return CommandType.valueOf(
                requestIn.commandType().name()
        );
    }
}
