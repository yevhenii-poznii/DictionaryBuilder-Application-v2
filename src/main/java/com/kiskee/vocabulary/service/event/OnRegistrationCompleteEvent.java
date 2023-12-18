package com.kiskee.vocabulary.service.event;

import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
@EqualsAndHashCode(callSuper = false)
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private final UserSecureProjection userInfo;

    public OnRegistrationCompleteEvent(UserSecureProjection userInfo) {
        super(userInfo);
        this.userInfo = userInfo;
    }

}
