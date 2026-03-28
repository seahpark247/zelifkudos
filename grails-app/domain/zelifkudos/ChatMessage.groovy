package zelifkudos

class ChatMessage {

    String content
    Long userId
    Date dateCreated

    static constraints = {
        content maxSize: 500
        userId nullable: true
    }
}
