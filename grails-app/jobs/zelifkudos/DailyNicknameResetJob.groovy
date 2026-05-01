package zelifkudos

import groovy.util.logging.Slf4j

@Slf4j
class DailyNicknameResetJob {

    ChatService chatService

    static triggers = {
        // Every day at midnight Central Time
        cron name: 'dailyNicknameResetTrigger', cronExpression: "0 0 0 * * ?", timeZone: TimeZone.getTimeZone('America/Chicago')
    }

    def execute() {
        log.info("========== Daily nickname reset job triggered ==========")
        chatService.deleteAllNicknames()
        log.info("========== Daily nickname reset job completed ==========")
    }
}
