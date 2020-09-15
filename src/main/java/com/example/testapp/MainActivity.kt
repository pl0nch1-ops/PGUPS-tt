package com.example.testapp

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.coroutines.*
import android.webkit.WebView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

import org.apache.poi.xssf.usermodel.XSSFWorkbook

import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE: Int = 1000
    private val GROUP:String = "ИВБ-015"
    private val BaseUrl:String ="https://rasp.pgups.ru/files/xls_files/"
    private val Courses:MutableMap<String?, MutableMap<String?, MutableSet<String>>> = mutableMapOf() //Структура университета
    private lateinit var Schedule:Timetable //Здесь хранится расписание запрошенной группы

    override fun onCreate(savedInstanceState: Bundle?) {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //---------------------------------------------------
        downloadBtn.setOnClickListener()
        {
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
                }
                else
                {
                    sendGet()
                }
            }else
            {
                sendGet()
            }
        }
    }

    fun sendGet() {
        parseGroupsHtml()
        val url = BaseUrl + "/${findViewById<TextView>(R.id.courseView).text.toString()}/${URLEncoder.encode(findViewById<TextView>(R.id.groupView).text.toString(), "utf-8")}.xlsx"

        GlobalScope.launch {
            Schedule = loadExcel(url)
        }

    }

    fun loadExcel(url:String):Timetable{
        val excelFile = URL(url).openStream()//FileInputStream(File(pathname))
        val workbook = XSSFWorkbook(excelFile)

        val parsedTimetable:Timetable = Timetable(workbook)

        workbook.close()
        excelFile.close()

        for ((dayW,v) in parsedTimetable.days)
        {
            println(dayW)
            for ((num, lessons) in v) {
                for ( lesson in lessons) {
                    println(num.toString() +" | " + lesson.Duration + " | " + lesson.Discipline)
                }
            }
        }
        return parsedTimetable
    }

    private fun parseGroupsHtml(){
        Thread(Runnable {
            val stringBuilder = StringBuilder()
            try {
                val doc: Document = Jsoup.connect("https://rasp.pgups.ru/schedule/group").get()
                val title: String = doc.title()
                val links: Elements = doc.select(".tab-pane:not(#tab_fbfo):not(#pgups_resources_tab)")
                val translatedIdOfCources:Map<String,String> = mapOf("course_1" to "1 курс", "course_2" to "2 курс", "course_3" to "3 курс", "course_4" to "4 курс", "course_5" to "5 курс", "tab_mag" to "Магистратура")

                for (course in links){
                    val faculties = course.select(".mb-4")
                    Courses.put(translatedIdOfCources.get(course.id()), mutableMapOf<String?, MutableSet<String>>())

                    for (faculty in faculties){
                        val groups = faculty.select(".btn")
                        val faculty_name = faculty.select(".mb-3").text()
                        Courses.get(translatedIdOfCources.get(course.id()))?.put(faculty_name, mutableSetOf<String>())
                        for (group in groups){
                            Courses.get(translatedIdOfCources.get(course.id()))?.get(faculty_name)?.add(group.text())
                        }
                    }
                }
                ////////////////////////////////////
                ////////////////////////////////////
                for ((course, faculties) in Courses){
                    for((faculty, groups) in faculties){
                        for (group in groups){
                            println("${course} ${faculty} ${group}") // Как использовать структуру курсов
                        }
                    }
                }
                ///////////////////////////////////
                ///////////////////////////////////
            } catch (e: IOException) {
                stringBuilder.append("Error : ").append(e.message).append("\n")
            }
        }).start()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode)
        {
            STORAGE_PERMISSION_CODE->{
                if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    sendGet()
                }
                else
                {

                }
            }
        }
    }
}