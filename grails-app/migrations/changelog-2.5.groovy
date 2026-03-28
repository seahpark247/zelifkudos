databaseChangeLog = {

    changeSet(author: "seah", id: "2.5-1-create-chat-message") {
        createTable(tableName: "chat_message") {
            column(name: "id", type: "BIGINT") {
                constraints(primaryKey: true, nullable: false)
            }
            column(name: "version", type: "BIGINT") {
                constraints(nullable: false)
            }
            column(name: "content", type: "VARCHAR(500)") {
                constraints(nullable: false)
            }
            column(name: "user_id", type: "BIGINT")
            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: false)
            }
        }
    }
}
