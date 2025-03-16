package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.SharingRepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.RepetitionHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.RepetitionMessageHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.impl.RepetitionMessageHandlerImpl;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.RepetitionStateHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.impl.OwnerRepetitionStateHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.impl.SharingRepetitionStateHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RepetitionServiceFactoryTest {

    @InjectMocks
    private RepetitionServiceFactory repetitionServiceFactory;

    @Mock
    private Map<Class<? extends RepetitionRequest>, ? extends RepetitionHandler> handlers;

    @Mock
    private List<RepetitionHandler> repetitionHandlers;

    @Mock
    private OwnerRepetitionStateHandler defaultRepetitionHandler;

    @Mock
    private SharingRepetitionStateHandler sharingRepetitionStateHandler;

    @Mock
    private RepetitionMessageHandlerImpl repetitionMessageHandler;

    @BeforeEach
    public void setUp() {
        List<RepetitionHandler> handlers = new ArrayList<>();
        handlers.add(defaultRepetitionHandler);
        doReturn(RepetitionStartFilterRequest.class)
                .when(defaultRepetitionHandler)
                .getRequestType();
        handlers.add(sharingRepetitionStateHandler);
        doReturn(SharingRepetitionStartFilterRequest.class)
                .when(sharingRepetitionStateHandler)
                .getRequestType();
        handlers.add(repetitionMessageHandler);
        doReturn(WSRequest.class).when(repetitionMessageHandler).getRequestType();

        this.handlers =
                handlers.stream().collect(Collectors.toMap(RepetitionHandler::getRequestType, Function.identity()));
        ReflectionTestUtils.setField(
                repetitionServiceFactory, "defaultRepetitionHandler", this.defaultRepetitionHandler);
        ReflectionTestUtils.setField(repetitionServiceFactory, "handlers", this.handlers);
    }

    @Test
    void testExecuteIsRepetitionRunning_WhenRepetitionIsNotRunning_ThenReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(new RepetitionRunningStatus(false, false));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).isRepetitionRunning());

        assertThat(repetitionRunningStatus.isRunning()).isFalse();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void testExecuteIsRepetitionRunning_WhenRepetitionIsRunning_ThenReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(new RepetitionRunningStatus(true, false));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).isRepetitionRunning());

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void testExecuteIsRepetitionRunning_WhenRepetitionIsPaused_ThenReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.isRepetitionRunning()).thenReturn(new RepetitionRunningStatus(true, true));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).isRepetitionRunning());

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isTrue();
    }

    @Test
    void
            tesExecuteStart_WhenRepetitionRequestIsRepetitionStartFilterRequest_ThenExecuteStartByOwnerRepetitionStateHandler() {
        RepetitionStartFilterRequest repetitionStartFilterRequest = mock(RepetitionStartFilterRequest.class);
        long dictionaryId = 1L;

        when(defaultRepetitionHandler.start(dictionaryId, repetitionStartFilterRequest))
                .thenReturn(new RepetitionRunningStatus(true, false));

        RepetitionRunningStatus repetitionRunningStatus = repetitionServiceFactory.execute(
                repetitionStartFilterRequest,
                service -> ((RepetitionStateHandler) service).start(dictionaryId, repetitionStartFilterRequest));

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void
            testExecuteStart_WhenRepetitionRequestIsSharingRepetitionStartFilterRequest_ThenExecuteStartBySharingRepetitionStateHandler() {
        SharingRepetitionStartFilterRequest repetitionStartFilterRequest =
                mock(SharingRepetitionStartFilterRequest.class);
        long dictionaryId = 1L;

        when(sharingRepetitionStateHandler.start(dictionaryId, repetitionStartFilterRequest))
                .thenReturn(new RepetitionRunningStatus(true, false));

        RepetitionRunningStatus repetitionRunningStatus = repetitionServiceFactory.execute(
                repetitionStartFilterRequest,
                service -> ((RepetitionStateHandler) service).start(dictionaryId, repetitionStartFilterRequest));

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void testExecutePause_WhenRepetitionIsRunning_ThenStartPauseAndReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.pause()).thenReturn(new RepetitionRunningStatus(true, true));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).pause());

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isTrue();
    }

    @Test
    void testExecutePause_WhenRepetitionIsPaused_ThenThrowRepetitionException() {
        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Pause already started"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(
                        () -> repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).pause()))
                .withMessage("Pause already started");
    }

    @Test
    void testExecutePause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        when(defaultRepetitionHandler.pause()).thenThrow(new RepetitionException("Repetition is not running"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(
                        () -> repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).pause()))
                .withMessage("Repetition is not running");
    }

    @Test
    void testExecuteUnpause_WhenRepetitionIsPaused_ThenStopPauseAndReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.unpause()).thenReturn(new RepetitionRunningStatus(true, false));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).unpause());

        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void testExecuteUnpause_WhenRepetitionIsNotPaused_ThenThrowRepetitionException() {
        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("No pause to end"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(
                        () -> repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).unpause()))
                .withMessage("No pause to end");
    }

    @Test
    void testExecuteUnpause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        when(defaultRepetitionHandler.unpause()).thenThrow(new RepetitionException("Repetition is not running"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(
                        () -> repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).unpause()))
                .withMessage("Repetition is not running");
    }

    @Test
    void testExecuteStop_WhenRepetitionIsRunning_ThenStopRepetitionAndReturnRepetitionRunningStatus() {
        when(defaultRepetitionHandler.stop()).thenReturn(new RepetitionRunningStatus(false, false));

        RepetitionRunningStatus repetitionRunningStatus =
                repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).stop());

        assertThat(repetitionRunningStatus.isRunning()).isFalse();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void testExecuteStop_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        when(defaultRepetitionHandler.stop()).thenThrow(new RepetitionException("Repetition is not running"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(
                        () -> repetitionServiceFactory.execute(service -> ((RepetitionStateHandler) service).stop()))
                .withMessage("Repetition is not running");
    }

    @ParameterizedTest
    @MethodSource("provideWSRequestOperations")
    void testExecuteHandleRepetitionMessage_WhenRepetitionIsRunning_ThenHandleRepetitionMessage(
            WSRequest.Operation operation) {
        WSRequest wsRequest = new WSRequest(null, operation);
        Authentication authentication = mock(Authentication.class);

        when(repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .thenReturn(mock(WSResponse.class));

        WSResponse wsResponse =
                repetitionServiceFactory.execute(wsRequest, service -> ((RepetitionMessageHandler) service)
                        .handleRepetitionMessage(authentication, wsRequest));

        assertThat(wsResponse).isInstanceOf(WSResponse.class);
    }

    @ParameterizedTest
    @MethodSource("provideWSRequestOperations")
    void testExecuteHandleRepetitionMessage_WhenRepetitionIsNotRunning_ThenThrowRepetitionException(
            WSRequest.Operation operation) {
        WSRequest wsRequest = new WSRequest(null, operation);
        Authentication authentication = mock(Authentication.class);

        when(repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .thenThrow(new RepetitionException("Repetition is not running"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() ->
                        repetitionServiceFactory.execute(wsRequest, service -> ((RepetitionMessageHandler) service)
                                .handleRepetitionMessage(authentication, wsRequest)))
                .withMessage("Repetition is not running");
    }

    @ParameterizedTest
    @MethodSource("provideWSRequestOperations")
    void testExecuteHandleRepetitionMessage_WhenNextWordIsNull_ThenThrowRepetitionException(
            WSRequest.Operation operation) {
        WSRequest wsRequest = new WSRequest(null, operation);
        Authentication authentication = mock(Authentication.class);
        when(repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .thenThrow(new RepetitionException("No more words to repeat"));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() ->
                        repetitionServiceFactory.execute(wsRequest, service -> ((RepetitionMessageHandler) service)
                                .handleRepetitionMessage(authentication, wsRequest)))
                .withMessage("No more words to repeat");
    }

    private static Stream<WSRequest.Operation> provideWSRequestOperations() {
        return Stream.of(WSRequest.Operation.START, WSRequest.Operation.SKIP, WSRequest.Operation.CHECK);
    }
}
