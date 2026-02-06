package com.example.apiorchestrator.domain.port.out;

import com.example.proto.CommandRequest;
import com.google.protobuf.Empty;

public interface RouterManagerClient {

    Empty sendCommand(CommandRequest commandRequest);
}
