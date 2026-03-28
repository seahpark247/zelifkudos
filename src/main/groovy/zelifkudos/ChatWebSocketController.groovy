package zelifkudos

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
class ChatWebSocketController {

    @Autowired
    SimpMessagingTemplate messagingTemplate

    @Autowired
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

        // Block identical consecutive message within 10 seconds
        String lastMsg = lastMessage.get(sessionId)
        if (lastMsg == content && lastTime && now - lastTime < 10000) return

        lastSendTime.put(sessionId, now)
        lastMessage.put(sessionId, content)

        Long userId = headerAccessor.sessionAttributes?.get('userId') as Long

        ChatMessage saved = chatService.saveMessage(content, userId)

        messagingTemplate.convertAndSend("/topic/chat", [
            content: saved.content,
            timestamp: saved.dateCreated.time
        ])
    }

    @EventListener
    void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.sessionId
        lastSendTime.remove(sessionId)
        lastMessage.remove(sessionId)
    }
}
