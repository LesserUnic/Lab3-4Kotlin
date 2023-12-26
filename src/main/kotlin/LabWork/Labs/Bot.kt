package LabWork.Labs

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.sql.Connection
import java.sql.DriverManager


class Bot(): TelegramLongPollingBot()
{
    var chatid=""
    var cur_form=0
    var cur_quest=0
    var total_score=0
    var quiz= mutableMapOf<Question, List<Answer>>()
    val dbURL = "jdbc:postgresql://localhost:5432/TGBot"
    val connection: Connection = DriverManager.getConnection(dbURL, "postgres", "1")

    var status=0
    override fun getBotToken()
    override fun getBotUsername()
    override fun onUpdateReceived(update: Update) {

        if(update.hasCallbackQuery()){
            if (update.callbackQuery.data.startsWith("F")) {
                cur_form=update.callbackQuery.data.substring(1).toInt()
                execute(SendMessage(update.callbackQuery.message.chatId.toString(), "Form selected"))
            }
            if (update.callbackQuery.data.startsWith("Q")) {
                cur_quest=update.callbackQuery.data.substring(1).toInt()
                execute(SendMessage(update.callbackQuery.message.chatId.toString(), "Question selected"))
            }
            if (update.callbackQuery.data.startsWith("SQ")) {
                val keys = quiz.keys
                var iter = 0
                if (update.callbackQuery.data.substring(update.callbackQuery.data.lastIndexOf('A') + 1).toBoolean()){
                    total_score += 1
                }

                if(update.callbackQuery.data.substring(2, update.callbackQuery.data.lastIndexOf('A')).toInt()==keys.size){
                    execute(SendMessage(update.callbackQuery.message.chatId.toString(), "THX for taking quiz, Your total=${total_score}/${keys.size}"))
                }
                else {
                    keys.forEach {
                        if (iter == update.callbackQuery.data.substring(2, update.callbackQuery.data.lastIndexOf('A')).toInt()) {
                            var res = mutableListOf<List<InlineKeyboardButton>>()
                            quiz[it]!!.forEach {
                                res.add(listOf(InlineKeyboardButton(it.Text).apply {
                                    callbackData = "SQ${(iter + 1)}A${it.Status}"
                                }))
                            }
                            execute(SendMessage(update.callbackQuery.message.chatId.toString(), it.Text).apply {
                                replyMarkup = InlineKeyboardMarkup().apply {
                                    keyboard = res

                                }
                            })
                        }
                        iter++
                    }
                }
            }
        }
        if(update.hasMessage()) {
            val message:String=update.message.text
            val chatid:String=update.message.chatId.toString()
            val name:String=update.message.from.userName
            when{

                message=="/NewForm"-> {
                    status=1
                    execute(SendMessage(chatid, "Send from name"))
                }
                message=="/AddQuestion"-> {
                    status=2
                    execute(SendMessage(chatid, "Send question"))
                }
                message=="/AddAnswer"-> {
                    status=3
                    execute(SendMessage(chatid, "Send answer"))
                }
                message=="/SelectFrom"-> {
                    status=4
                    execute(SendMessage(chatid, "Select Form"))
                    val forms= ShowForms(connection)
                    var key= InlineKeyboardMarkup().apply {}
                    var list= mutableListOf<List<InlineKeyboardButton>>()
                    forms.forEach() {
                        list.add(listOf(InlineKeyboardButton(it.value).apply {
                            callbackData = "F" + it.key.toString()
                        }))
                    }
                    key.keyboard= list
                    val exec=SendMessage(chatid, "Forms")
                    exec.replyMarkup=key
                    execute(exec)
                }
                message=="/SelectQuestion"-> {
                    status=5
                    execute(SendMessage(chatid, "Select question"))
                    val forms= ShowQuestions(connection)
                    var key= InlineKeyboardMarkup().apply {}
                    var list= mutableListOf<List<InlineKeyboardButton>>()
                    forms.forEach() {
                        list.add(listOf(InlineKeyboardButton(it.value).apply {
                            callbackData = "Q" + it.key.toString()
                        }))
                    }
                    key.keyboard= list
                    val exec=SendMessage(chatid, "Questions")
                    exec.replyMarkup=key
                    execute(exec)
                }
                message=="/TakeTheTest"->{
                    status=6
                    execute(SendMessage(chatid, "Start").apply {
                        replyMarkup=InlineKeyboardMarkup().apply {
                            keyboard=listOf(
                                listOf(InlineKeyboardButton("Begin").apply {
                                    callbackData = "SQ0A0"
                                })
                            )
                        }
                    })
                    quiz=Quiz(connection, cur_form)
                    total_score=0
                }

                else->{

                    when{
                        status==0->execute(SendMessage(chatid, "Unknown command"))
                        status==1->{
                            cur_form=AddForm(Form(0, message, name), connection)
                            execute(SendMessage(chatid, "Form ${message} added"))
                            status=0
                        }
                        status==2->{
                            cur_quest = AddQuestion(Question(0, message, cur_form), connection)
                            execute(SendMessage(chatid, "Question ${message} added"))
                            status=0
                        }
                        status==3->{
                            AddAnswer(Answer(0, message.subSequence(0, message.length-2).toString(), cur_quest, message.last()=='T'), connection)
                            execute(SendMessage(chatid, "Answer added"))
                            status=0
                        }
                        status==4->{
                            status=0
                        }
                        status==5->{
                            status=0
                        }
                    }
                }
            }
        }
    }
}