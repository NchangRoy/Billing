package com.example.account.modules.tiers.domain.port.output;

import com.example.account.modules.tiers.dto.ClientResponse;

import java.util.UUID;

public interface ClientEventPort {
    void publishClientCreated(ClientResponse client);
    void publishClientUpdated(ClientResponse client);
    void publishClientDeleted(UUID clientId);
}
