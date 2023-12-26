package LabWork.Labs

import java.sql.Connection


fun AddForm(instance:Form, connection:Connection):Int{
    var statement=String.format(
        "INSERT INTO forms (name, author) " +
                "VALUES ('%s', '%s');", instance.Name, instance.Author
    );
    val query = connection.prepareStatement(statement)
    query.execute()
    statement=String.format(
        "SELECT MAX(id) from forms"
    );
    val ps=connection.createStatement()
    ps.maxRows=1
    val rs=ps.executeQuery(statement)
    rs.next()
    return rs.getInt(1)
}
fun AddQuestion(instance:Question, connection:Connection):Int{
    var statement=String.format(
        "INSERT INTO questions (text, form_id) " +
                "VALUES ('%s', %d);", instance.Text, instance.FormID
    );
    val query = connection.prepareStatement(statement)
    query.execute()
    statement=String.format(
        "SELECT MAX(id) from questions"
    );
    val ps=connection.createStatement()
    ps.maxRows=1
    val rs=ps.executeQuery(statement)
    rs.next()
    return rs.getInt(1)
}

fun AddAnswer(instance:Answer, connection:Connection){
    var statement=String.format(
        "INSERT INTO answers (text, question_id, status) " +
                "VALUES ('%s', %d, %b);", instance.Text, instance.QuestionID, instance.Status
    );
    val query = connection.prepareStatement(statement)
    query.execute()
}

fun ShowForms(connection: Connection):MutableMap<Int, String>{
    var res= mutableMapOf<Int, String>()
    var statement=String.format(
        "Select id, name from forms"
    );
    val ps=connection.createStatement()
    val rs=ps.executeQuery(statement)
    while(rs.next()){
        res.put(rs.getInt("id"), rs.getString("name"))
    }
    return res
}
fun ShowQuestions(connection: Connection):MutableMap<Int, String>{
    var res= mutableMapOf<Int, String>()
    var statement=String.format(
        "Select id, text from questions"
    );
    val ps=connection.createStatement()
    val rs=ps.executeQuery(statement)
    while(rs.next()){
        res.put(rs.getInt("id"), rs.getString("text"))
    }
    return res
}

fun Quiz(connection: Connection, form:Int): MutableMap<Question, List<Answer>>{
    var quiz= mutableMapOf<Question, List<Answer>>()
    var res= mutableListOf<Question>()
    var statement=String.format(
        "Select id, text from questions where form_id=%d", form
    );
    val ps=connection.createStatement()
    var rs=ps.executeQuery(statement)
    while(rs.next()){
        res.add(Question(rs.getInt("id"), rs.getString("text"), form))
    }
    res.forEach{
        statement=String.format("Select * from answers where question_id=%d", it.id)
        rs=ps.executeQuery(statement)
        val ans= mutableListOf<Answer>()
        while(rs.next()){
            ans.add(Answer(rs.getInt("id"), rs.getString("text"), it.id, rs.getBoolean("status")))
        }
        quiz.put(it, ans)
    }
    return quiz
}