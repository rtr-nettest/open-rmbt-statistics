package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.service.UuidGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidGeneratorImpl implements UuidGenerator {

    @Override
    public UUID generateNewUuid() {
        return UUID.randomUUID();
    }
}
