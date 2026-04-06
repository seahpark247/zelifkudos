package zelifkudos

import grails.gorm.transactions.Transactional

@Transactional
class ChatService {

    static final List<String> ANIMALS = [
        "Penguin", "Otter", "Fox", "Panda", "Koala",
        "Dolphin", "Owl", "Rabbit", "Hedgehog", "Sloth",
        "Raccoon", "Flamingo", "Seal", "Duckling", "Hamster",
        "Capybara", "Quokka", "Red Panda", "Axolotl", "Narwhal",
        "Alpaca", "Corgi", "Puffin", "Chinchilla", "Orca",
        "Chameleon", "Toucan", "Jellyfish", "Seahorse", "Firefly",
        "Meerkat", "Peacock", "Platypus", "Starfish", "Ladybug",
        "Hummingbird", "Crab", "Frog", "Parrot", "Turtle",
        "Bee", "Butterfly", "Kitten", "Puppy", "Sparrow",
        "Squirrel", "Deer", "Swan", "Pelican", "Octopus"
    ]

    static final List<String> COLORS = [
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
        "#F8C471", "#82E0AA", "#F1948A", "#AED6F1", "#D7BDE2",
        "#A3E4D7", "#FAD7A0", "#A9CCE3", "#D5F5E3", "#FADBD8"
    ]

    AnimalNickname getOrAssignNickname(Long userId) {
        User user = User.get(userId)
        if (!user) return null

        AnimalNickname existing = AnimalNickname.findByUser(user)
        if (existing) return existing

        // Pick a random animal not yet assigned this week (if possible)
        List<String> taken = AnimalNickname.list()*.animalName
        List<String> available = ANIMALS - taken
        String animal = available ? available[new Random().nextInt(available.size())]
                                  : ANIMALS[new Random().nextInt(ANIMALS.size())]

        // Pick a color — if animal is duplicated, ensure different color
        List<String> takenColors = AnimalNickname.findAllByAnimalName(animal)*.colorHex
        List<String> availableColors = COLORS - takenColors
        String color = availableColors ? availableColors[new Random().nextInt(availableColors.size())]
                                       : COLORS[new Random().nextInt(COLORS.size())]

        new AnimalNickname(user: user, animalName: animal, colorHex: color).save(failOnError: true)
    }

    ChatMessage saveMessage(String content, Long userId = null, String nickname = null, String colorHex = null) {
        new ChatMessage(content: content.trim(), userId: userId, nickname: nickname, colorHex: colorHex).save(failOnError: true)
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

    void deleteAllNicknames() {
        AnimalNickname.executeUpdate("delete from AnimalNickname")
    }
}
