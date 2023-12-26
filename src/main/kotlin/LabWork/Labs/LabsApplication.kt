package LabWork.Labs

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main(args: Array<String>) {

    TelegramBotsApi(DefaultBotSession::class.java).apply {
        registerBot(Bot())
    }
}
