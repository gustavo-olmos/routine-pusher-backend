package com.routine.pusher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/web-socket")
public class WebSocketController
{
    @SendTo("/topic/notifications")
    @MessageMapping("/send-notification")
    public ResponseEntity<String> sendNotification(String message )
    {
        return ResponseEntity.ok("Notificação recebida: " + message);
    }
}
