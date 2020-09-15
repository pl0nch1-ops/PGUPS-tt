package com.example.testapp

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class Timetable(excelSource: XSSFWorkbook){
    var days:MutableMap<String, MutableMap<Int, MutableList<Lesson>>> = mutableMapOf()
    init{
        val sheet = excelSource.getSheetAt(0)
        val rows = sheet.iterator()
        var lessonDuration = "00-00"
        var lessonCounter = 0
        var currentDOW = "" //Day of week

        //Избавляемся от проблемы с слитыми клетками
        for (f in 0..sheet.numMergedRegions-1) {
            val a = sheet.getMergedRegion(f)
            if (a.containsColumn(2)){
                val lr = a.lastRow
                val fr = a.firstRow
                sheet.getRow(lr).getCell(2).setCellValue(sheet.getRow(fr).getCell(2).stringCellValue)
            }
        }
        //

        // Инициализация объекта расписания группы
        while (rows.hasNext()) {
            val currentRow = rows.next()
            val cellsInRow = currentRow.iterator()
            var cellCounter = 1
            while (cellsInRow.hasNext()) {
                val currentCell = cellsInRow.next()
                if (currentCell.getCellTypeEnum() === CellType.STRING) {
                    when(cellCounter){
                        1 -> {
                            currentDOW = currentCell.stringCellValue
                            days.put(currentDOW, mutableMapOf())
                            lessonCounter = 0
                        }
                        2 -> {
                            lessonDuration = currentCell.stringCellValue
                            lessonCounter++
                            days[currentDOW]?.put(lessonCounter, mutableListOf())
                        }
                        3 -> {
                            try {
                                if (currentDOW!=""){
                                    var lessonGroup = days[currentDOW]?.get(lessonCounter) //lessonGroup - занятие(1 - верхнее и 2 - нижнее)
                                    lessonGroup!!.add(Lesson(lessonDuration, currentCell.stringCellValue))
                                    days[currentDOW]!!.put(lessonCounter, lessonGroup)
                                }
                            }
                            catch (e:Exception) {println("error")}
                        }
                    }
                } else if (cellCounter==3) {
                    try {
                        if (currentDOW != "") {
                            var lessonGroup = days[currentDOW]?.get(lessonCounter)
                            lessonGroup!!.add(Lesson(lessonDuration, currentCell.stringCellValue))
                            days[currentDOW]?.put(lessonCounter, lessonGroup)
                        }
                    }
                    catch (e:Exception) {println("error")}
                }
                cellCounter++
            }
        }
        excelSource.close()
    }
}