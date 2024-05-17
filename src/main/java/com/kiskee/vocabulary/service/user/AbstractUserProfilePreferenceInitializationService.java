package com.kiskee.vocabulary.service.user;

import com.kiskee.vocabulary.model.dto.registration.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public abstract class AbstractUserProfilePreferenceInitializationService<E> {

    protected abstract JpaRepository<E, UUID> getRepository();

    protected abstract <R extends RegistrationRequest> E buildEntityToSave(R registrationRequest);

    protected <R extends RegistrationRequest> E initEntityAndSave(R registrationRequest) {
        E entity = buildEntityToSave(registrationRequest);
        return getRepository().save(entity);
    }

}
