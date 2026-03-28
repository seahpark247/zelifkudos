package zelifkudos

import grails.converters.JSON

class ChatController {

    ChatService chatService

    def recent() {
        List<ChatMessage> messages = chatService.getRecentMessages(50)
        render messages.collect { [content: it.content, timestamp: it.dateCreated.time] } as JSON
    }
}
