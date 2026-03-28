package zelifkudos

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatWebSocketController {

    @Autowired
    SimpMessagingTemplate messagingTemplate

    @Autowired
    ChatService chatService

    @MessageMapping("/chat.send")
    void sendMessage(@Payload Map message) {
        String content = message.content?.toString()?.trim()
        if (!content || content.length() > 500) return

        ChatMessage saved = chatService.saveMessage(content)

        messagingTemplate.convertAndSend("/topic/chat", [
            content: saved.content,
            timestamp: saved.dateCreated.time
        ])
    }
}
