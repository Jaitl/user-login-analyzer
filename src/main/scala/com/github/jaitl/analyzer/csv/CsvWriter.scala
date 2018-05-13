package com.github.jaitl.analyzer.csv

import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.github.jaitl.analyzer.model.IpLoginSeries
import com.github.tototoshi.csv.CSVWriter

import scala.util.Try

/**
 * Сохраняет файл с результатами анализа в формате csv.
 */
class CsvWriter(timeZone: ZoneId) {
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(timeZone)

  private val headers: Seq[String] = Seq("IP address", "Start", "Stop", "Users")

  def write(data: Seq[IpLoginSeries], file: File): Try[Unit] = Try {
    val writer = CSVWriter.open(file, append = false)

    writer.writeRow(headers)

    val writeData = for {
      series <- data
      ip = series.ip
      start = formatter.format(series.start)
      stop = formatter.format(series.stop)
      users = series.users.map(u => s"${u.login}:${formatter.format(u.date)}").mkString(",")
    } yield Seq(ip, start, stop, users)

    writer.writeAll(writeData)

    writer.close()
  }
}
