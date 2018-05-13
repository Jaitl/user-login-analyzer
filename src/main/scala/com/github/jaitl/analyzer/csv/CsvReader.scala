package com.github.jaitl.analyzer.csv

import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.github.jaitl.analyzer.model.LoginInfo
import com.github.tototoshi.csv.CSVReader

import scala.util.Try

/**
 * Считывает csv файл с LoginInfo.
 *
 * Формат файла:
 * "TheRealJJ","77.92.76.250","2015-11-30 23:11:40"
 * "loginAuthTest","37.48.80.201","2015-11-30 23:11:51"
 * "ksiriusr","123.108.246.205","2015-11-30 23:11:55"
 * "Swed3n","83.250.54.3","2015-11-30 23:12:21"
 */
class CsvReader(timeZone: ZoneId) {
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(timeZone)

  def read(file: File): Try[Seq[LoginInfo]] = Try {
    val reader = CSVReader.open(file)

    val rows = reader.all().map(row => LoginInfo(row.head, row(1), Instant.from(formatter.parse(row(2)))))

    reader.close()

    rows
  }
}
