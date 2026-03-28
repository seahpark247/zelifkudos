package zelifkudos

import grails.gorm.transactions.Transactional

@Transactional
class ChatService {

    ChatMessage saveMessage(String content, Long userId = null) {
        new ChatMessage(content: content.trim(), userId: userId).save(failOnError: true)
    }

    @Transactional(readOnly = true)
    List<ChatMessage> getRecentMessages(int max) {
        ChatMessage.executeQuery(
            "from ChatMessage cm order by cm.dateCreated asc",
            [max: max, offset: Math.max(0, ChatMessage.count() - max)]
        )
    }

    void deleteAllMessages() {
        ChatMessage.executeUpdate("delete from ChatMessage")
    }
}
