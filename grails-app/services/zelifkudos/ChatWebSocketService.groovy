package zelifkudos

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate

class ChatWebSocketService {

    SimpMessagingTemplate brokerMessagingTemplate

    ChatService chatService

    private final Map<String, Long> lastSendTime = [:].asSynchronized()
    private final Map<String, String> lastMessage = [:].asSynchronized()

    @MessageMapping("/chat.send")
    void sendMessage(@Payload Map message, SimpMessageHeaderAccessor headerAccessor) {
        String content = message.content?.toString()?.trim()
        if (!content || content.length() > 500) return

        String sessionId = headerAccessor.sessionId
        long now = System.currentTimeMillis()

        // 3-second cooldown
        Long lastTime = lastSendTime.get(sessionId)
        if (lastTime && now - lastTime < 3000) return

        // Block identical consecutive message
        String lastMsg = lastMessage.get(sessionId)
        if (lastMsg == content) return

        lastSendTime.put(sessionId, now)
        lastMessage.put(sessionId, content)

        Long userId = headerAccessor.sessionAttributes?.get('userId') as Long

        ChatMessage saved = chatService.saveMessage(content, userId)

        brokerMessagingTemplate.convertAndSend("/topic/chat", [
            content: saved.content,
            timestamp: saved.dateCreated.time
        ])
    }
}
