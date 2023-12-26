package LabWork.Labs

data class Question(
    val id:Int,
    val Text:String,
    val FormID:Int
)
data class Answer(
    val id:Int,
    val Text:String,
    val QuestionID:Int,
    val Status:Boolean
)