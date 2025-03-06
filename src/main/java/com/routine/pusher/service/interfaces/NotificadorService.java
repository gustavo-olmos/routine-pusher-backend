package com.routine.pusher.service.interfaces;

import com.routine.pusher.model.dto.LembreteDTO;

public interface NotificadorService
{
    void notificar( Class jobClass, LembreteDTO dto );
}
