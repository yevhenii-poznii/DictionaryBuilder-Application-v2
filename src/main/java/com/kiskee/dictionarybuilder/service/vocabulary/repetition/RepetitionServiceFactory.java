package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RepetitionServiceFactory {

    private final Map<Class<? extends RepetitionRequest>, ? extends RepetitionHandler> handlers;
    private final RepetitionHandler defaultRepetitionHandler;

    public <T extends RepetitionHandler> RepetitionServiceFactory(List<T> repetitionServices) {
        this.defaultRepetitionHandler = repetitionServices.getFirst();
        this.handlers = repetitionServices.stream()
                .collect(Collectors.toMap(RepetitionHandler::getRequestType, Function.identity()));
    }

    public <R, RR extends RepetitionRequest> R execute(RR request, Function<RepetitionHandler, R> action) {
        RepetitionHandler service = handlers.get(request.getClass());
        return action.apply(service);
    }

    public <R> R execute(Function<RepetitionHandler, R> action) {
        return action.apply(defaultRepetitionHandler);
    }
}
