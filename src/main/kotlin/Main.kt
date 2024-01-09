package ua.helpme

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.FileWriter
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

data class Lesson(val title: String, val other: String)

data class TimetableDay(val date: Date, val lessons: MutableList<Lesson> = mutableListOf())

fun main() {
    val consoleReader = System.`in`.bufferedReader()
    println("Enter file name with your timetable")
    val filename = consoleReader.readLine()
    val fileInputStream = try {
        Path.of(filename).toFile().inputStream()
    } catch (ex: Exception) {
        ex.printStackTrace()
        return
    }

    val timetable = mutableListOf<TimetableDay>()
    val dateRegex = "^(\\d{2})\\.(\\d{2})\\.(\\d{2})\\s([А-Яа-я]{2})".toRegex()
    val lessonRegex = "^(\\d+)-\\s+(\\S.*?),\\s+(.*?)\$".toRegex()

    for (rawLine in fileInputStream.bufferedReader().lines()) {
        if (rawLine.isEmpty()) continue
        val line = rawLine.trim()
        if (dateRegex.matches(line)) {
            val (day, month, year, d) = dateRegex.find(line)!!.destructured
            val date = GregorianCalendar(2000 + year.toInt(), month.toInt() - 1, day.toInt()).time
            timetable.add(TimetableDay(date))
        } else if (lessonRegex.matches(line)) {
            val (lessonNumber, lessonName, other) = lessonRegex.find(line)!!.destructured
            if (timetable.isEmpty()) {
                println("Timetable is somewhy empty")
                return
            }
            val timetableDay = timetable.last()
            timetableDay.lessons.add(Lesson("$lessonNumber - $lessonName", other))
        }
    }

    println("Skip outdated? 1/0")
    val skipOutdated = consoleReader.readLine()!!.contentEquals("1")

    if (skipOutdated) {
        println("skipping")
        val currentDate = Calendar.getInstance().time
        timetable.removeIf { it.date.before(currentDate) }
    }

    println("Enter date format or leave EMPTY ")
    val timeFormat = consoleReader.readLine().ifEmpty { "dd/MM/YYYY (EEEE)" }
    println("Using $timeFormat")
    val simpleTimeFormat = SimpleDateFormat(timeFormat, Locale.forLanguageTag("ru-RU"))
    val htmlFile = FileWriter("timetable.html")

    htmlFile.appendHTML().html {
        head {
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            meta(charset = "utf-8")
            link(
                href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css",
                rel = "stylesheet"
            )
            style {
                text(
                    """
                        body {
                        background: radial-gradient(circle at center, #1a1f25, #30404d);
                        font-family: 'Comic Sans MS', cursive, sans-serif;
                        font-size: 20px;
                        color: #ffffff;
                        }
                        table {
                        table-layout: fixed; 
                        }
                        .table-bordered {
                        border-top-right-radius: 15px;
                        border-top-left-radius: 15px;
                        }
                    """
                )
            }
        }
        body {


            val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

            div(classes = "container-md text-center") {

                timetable.groupBy {
                    val calendar = Calendar.getInstance()
                    calendar.time = it.date
                    calendar.get(Calendar.WEEK_OF_YEAR)
                }.forEach { (week, timetables) ->
                    details {
                        summary {
                            if (week == currentWeek)
                                text("${simpleTimeFormat.format(timetables.first().date)} ($week) (current week)")
                            else
                                text("${simpleTimeFormat.format(timetables.first().date)} ($week)")
                        }
                        timetables.forEach { timetableDay ->
                            a(classes = "text-info") {
                                text(simpleTimeFormat.format(timetableDay.date))
                            }
                            table(classes = "table table-dark table-hover table-md mx-auto w-auto table-bordered border-success") {

                                tbody {
                                    timetableDay.lessons.forEach { lesson ->
                                        tr {
                                            td {
                                                text(lesson.title)
                                            }
                                            td {
                                                text(lesson.other)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    htmlFile.close()
}
