package zelifkudos

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate

class ChatWebSocketService {

    SimpMessagingTemplate brokerMessagingTemplate

    ChatService chatService

    @MessageMapping("/chat.send")
    void sendMessage(@Payload Map message, SimpMessageHeaderAccessor headerAccessor) {
        String content = message.content?.toString()?.trim()
        if (!content || content.length() > 500) return

        Long userId = headerAccessor.sessionAttributes?.get('userId') as Long

        ChatMessage saved = chatService.saveMessage(content, userId)

        brokerMessagingTemplate.convertAndSend("/topic/chat", [
            content: saved.content,
            timestamp: saved.dateCreated.time
        ])
    }
}
