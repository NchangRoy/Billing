package com.example.account.modules.tiers.adapter.output.messaging;

import com.example.account.modules.tiers.domain.port.output.ClientEventPort;
import com.example.account.modules.tiers.domain.port.output.FournisseurEventPort;
import com.example.account.modules.tiers.dto.ClientResponse;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NoOpTiersEventAdapter implements ClientEventPort, FournisseurEventPort {

    @Override
    public void publishClientCreated(ClientResponse client) {}

    @Override
    public void publishClientUpdated(ClientResponse client) {}

    @Override
    public void publishClientDeleted(UUID clientId) {}

    @Override
    public void publishFournisseurCreated(FournisseurResponse fournisseur) {}

    @Override
    public void publishFournisseurUpdated(FournisseurResponse fournisseur) {}

    @Override
    public void publishFournisseurDeleted(UUID fournisseurId) {}
}
