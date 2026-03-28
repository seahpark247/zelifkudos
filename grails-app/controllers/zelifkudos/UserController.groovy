package zelifkudos

class UserController {

    static allowedMethods = [updateFeeling: 'POST', toggleAdmin: 'POST']

    KudosService kudosService
    FeelingService feelingService

    def list() {
        User currentUser = request.currentUser
        Map<Long, Integer> kudosCounts = kudosService.countKudosForAllUsers()
        Map<Long, Integer> sentCounts = kudosService.countSentForAllUsers()
        List<User> users = User.list().sort { -(sentCounts[it.id] ?: 0) }
        int myKudosCount = kudosCounts[currentUser.id] ?: 0
        List<Kudos> recentMessages = kudosService.getRecentKudosForUser(currentUser.id, 3).findAll { it.message }
        Map<Long, String> feelings = feelingService.getAllFeelings()
        [users: users, kudosCounts: kudosCounts, isAdmin: currentUser.admin, currentUserId: currentUser.id,
         myKudosCount: myKudosCount, recentMessages: recentMessages, feelings: feelings]
    }

    def updateFeeling() {
        User currentUser = request.currentUser
        String message = params.feeling?.trim()
        if (message) {
            feelingService.saveFeeling(currentUser, message)
        } else {
            feelingService.deleteFeeling(currentUser)
        }
        redirect(action: 'list')
    }

    def toggleAdmin() {
        User currentUser = request.currentUser
        if (currentUser.email == 'seah@zelifcam.net') {
            feelingService.toggleAdmin(currentUser)
        }
        redirect(controller: 'user', action: 'list')
    }

    def index() { redirect(action: 'list') }
}
