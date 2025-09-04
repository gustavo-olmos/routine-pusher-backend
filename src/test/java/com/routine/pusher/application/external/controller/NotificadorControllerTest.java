package com.routine.pusher.application.external.controller;

import com.routine.pusher.application.usecase.NotificacaoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class NotificadorControllerTest
{
    @Mock
    private NotificacaoUseCase useCase;

    @InjectMocks
    private NotificadorSSEController controller;

    @Test
    @DisplayName("Testa o fluxo de notificações via Server-Side-Event")
    void test_Stream_01( )
    {
    }
}
