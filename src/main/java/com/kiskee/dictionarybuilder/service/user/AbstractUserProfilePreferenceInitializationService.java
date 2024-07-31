package com.kiskee.dictionarybuilder.service.user;

import com.kiskee.dictionarybuilder.model.dto.registration.RegistrationRequest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstractUserProfilePreferenceInitializationService<E> {

    protected abstract JpaRepository<E, UUID> getRepository();

    protected abstract <R extends RegistrationRequest> E buildEntityToSave(R registrationRequest);

    protected <R extends RegistrationRequest> E initEntityAndSave(R registrationRequest) {
        E entity = buildEntityToSave(registrationRequest);
        return getRepository().save(entity);
    }
}
