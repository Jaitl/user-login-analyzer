package com.github.jaitl.analyzer

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.github.jaitl.analyzer.csv.CsvReader
import com.github.jaitl.analyzer.csv.CsvWriter
import com.github.jaitl.analyzer.exception.InputFileNotExistException
import com.github.jaitl.analyzer.exception.OutputFileAlreadyExistsException
import com.github.jaitl.analyzer.service.LoginAnalysisService
import com.github.jaitl.analyzer.service.TimeWindowService
import com.typesafe.scalalogging.StrictLogging
import org.rogach.scallop.ScallopConf
import org.rogach.scallop.ScallopOption

import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

/**
 * Находит IP с которых происходило больше одного входа за заданное временное окно.
 *
 * Входные аргументы:
 * --input-file - путь до файла с информацией о входах
 * --output-file - путь до файла с результатами анализа
 * --window-size - размер окна в формате scala.concurrent.duration.Duration
 *                 например: "1 minute", "2 hours", "1 day"
 * --time-zone (опциональный, по умолчанию UTC) - временная зона в которой находятся даты из входного файла
 * --start-date (опциональны, формат: 2015-11-30T23:00:00) - опциональный параметр для указания момента времени
 *                 с которого начинается первое окно.
 *                 Если параметр не указан, то берется самое первое время и окна считаются от него.
 *                 Используется в случае если первый логин в коллекции произошел, например в "00:12:11", а окно
 *                 нужно начать считать с "00:00:00"
 */
object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    if (Files.notExists(Paths.get(conf.inputFile()))) {
      throw new InputFileNotExistException(s"Input file [${conf.inputFile()}] not exists")
    }
    if (Files.exists(Paths.get(conf.outputFile()))) {
      throw new OutputFileAlreadyExistsException(s"Output file [${conf.outputFile()}] already exists")
    }

    val timeZone = conf.timeZone.toOption match {
      case Some(zone) => ZoneId.of(zone)
      case None => ZoneId.of("UTC")
    }
    logger.info(s"timeZone: $timeZone")
    logger.info(s"windowSize: ${conf.windowSize()}")

    val windowSize = Duration(conf.windowSize())

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(timeZone)
    val startDate = conf.startDate.toOption.map(date => Instant.from(formatter.parse(date)))
    logger.info(s"startDate: $startDate")

    val reader = new CsvReader(timeZone)
    val writer = new CsvWriter(timeZone)
    val loginAnalysisService = new LoginAnalysisService(new TimeWindowService(windowSize))

    logger.info(s"Start read input file: ${conf.inputFile()}")

    val analysisResult = for {
      loginCollection <- reader.read(new File(conf.inputFile()))
      sortedLoginCollection = loginCollection.sortBy(_.date)
      _ = logger.info(s"Input file size: ${sortedLoginCollection.size} rows")
      _ = logger.info(s"First login: ${sortedLoginCollection.head}")
      _ = logger.info(s"Last login: ${sortedLoginCollection.last}")

      multipleLoginInfo <- loginAnalysisService.analyze(loginCollection, startDate)
      _ = logger.info(s"Number of multiple logins: ${multipleLoginInfo.size}")

      _ <- writer.write(multipleLoginInfo, new File(conf.outputFile()))
    } yield ()

    analysisResult match {
      case Success(_) => logger.info(s"The analysis results saved in ${conf.outputFile()}")
      case Failure(ex) => logger.error("Exception during analysis", ex)
    }
  }
}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val inputFile: ScallopOption[String] = opt[String](required = true)
  val outputFile: ScallopOption[String] = opt[String](required = true)
  val windowSize: ScallopOption[String] = opt[String](required = true)
  val timeZone: ScallopOption[String] = opt[String]()
  val startDate: ScallopOption[String] = opt[String]()
  verify()
}
