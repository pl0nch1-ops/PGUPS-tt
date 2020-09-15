package com.example.testapp

class Lesson(time:String, data:String)
{
    private var time = "00-00"
    private var discipline = "default"
    private var type = "default"
    private var hall = "NaN" //toDo:divide discipline into parametrers

    init{
        discipline = data
        this.time=time
    }

    val Discipline: String
        get() = discipline

    val Duration: String
        get() = time
}
