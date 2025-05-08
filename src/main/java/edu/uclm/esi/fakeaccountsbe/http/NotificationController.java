package edu.uclm.esi.fakeaccountsbe.http;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/notificar")
    @SendTo("/topic/actualizar")
    public String enviarMensaje(String mensaje) {
        return mensaje;
    }
}
