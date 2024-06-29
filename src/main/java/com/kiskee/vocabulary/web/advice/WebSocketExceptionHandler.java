package com.kiskee.vocabulary.web.advice;

import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    @SendToUser("/queue/errors")
    @MessageExceptionHandler({RepetitionException.class, ResourceNotFoundException.class})
    public WSException handleRepetitionException(Exception exception) {
        log.error("Repetition exception occurred: {}", exception.getMessage());
        return new WSException(exception.getMessage());
    }
}
