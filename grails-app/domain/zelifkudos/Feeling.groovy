package zelifkudos

class Feeling {

    User user
    String message
    Date dateCreated

    static constraints = {
        user unique: true
        message maxSize: 100
    }
}
