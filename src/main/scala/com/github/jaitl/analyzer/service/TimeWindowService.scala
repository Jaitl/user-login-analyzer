package com.github.jaitl.analyzer.service

import java.time.Instant

import com.github.jaitl.analyzer.exception.EmptyLoginCollectionException
import com.github.jaitl.analyzer.exception.WrongInitialDateException
import com.github.jaitl.analyzer.model.LoginInfo
import com.github.jaitl.analyzer.model.WindowLoginInfo

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * Сервис нарезающий коллекцию с фактами логина LoginInfo на окна по заданному размеру окна.
 *
 * @param windowSize размер временного окна.
 */
class TimeWindowService(windowSize: Duration) {

  import com.github.jaitl.analyzer.extension.InstantExtension._

  /**
   * Метод, выполняющий нарезку по окнам.
   * В случае если в окно не попадает ни одного факта логина, информация о нем не сохраняется.
   *
   * Алгоритм работы:
   * 1. Проверяется попадает ли факт логина в текущие окно, если попадает, то он сохраняется в текущее окно.
   * 2. Если факт логина не попадает в текущее окно, то текущее окно сохраняется в общий результат и вычисляется
   *    диапазон следующего временного окна. Факт логина сохраняется в новое окно, с этого момента новое окно считается
   *    текущем.
   *
   * @param loginCollection - коллекция с информацией о фактах логина.
   * @param fromDate - опциональный параметр для указания момента времени с которого начинается первое окно.
   *                 Если параметр не указан, то берется самое первое время и окна считаются от него.
   *                 Используется в случае если первый логин в коллекции произошел, например в "00:12:11", а окно
   *                 нужно начать считать с "00:00:00"
   * @return сегментированные факты логина и информация о начали и окончании окна.
   */
  def computeWindows(loginCollection: Seq[LoginInfo], fromDate: Option[Instant]): Try[Seq[WindowLoginInfo]] = Try {
    if (loginCollection.isEmpty) {
      throw new EmptyLoginCollectionException("empty login collection")
    }

    val sortedLogin = loginCollection.sortBy(_.date)

    fromDate.foreach { date =>
      if (date > sortedLogin.head.date) {
        throw new WrongInitialDateException("fromDate more than the minimum date in loginCollection")
      }
    }

    val startDate = fromDate match {
      case Some(date) => date
      case None => sortedLogin.head.date
    }

    val endDate = startDate.plusMillis(windowSize.toMillis)
    val currentWindow = WindowLoginInfo(startDate, endDate, Seq.empty)

    val (windows, lastWindow) = sortedLogin.foldLeft((Seq.empty[WindowLoginInfo], currentWindow)) {
      case ((res, cur), login) if login.date >= cur.startDate && login.date < cur.endDate =>
        (res, cur.copy(loginInfo = cur.loginInfo :+ login))

      case ((res, cur), login) =>
        val (startDate, endDate) = computeNextWindowRange(cur.endDate, login.date)
        val newWindow = WindowLoginInfo(
          startDate = startDate,
          endDate = endDate,
          loginInfo = login :: Nil
        )
        val newRes = if (cur.loginInfo.nonEmpty) res :+ cur else res
        (newRes, newWindow)
    }

    windows :+ lastWindow
  }

  /**
   * Вычисляет диапазон следующего временного окна в который попадает факт лоигна,
   * позволяет избежать некорректного поведения системы, если в окно не попадает ни одного факта логина.
   *
   * @param fromDate время с которого треюуется начать поиск диападона для loginDate.
   * @param loginDate время в которое произошел факт логина.
   * @return диапазон, в течении которого произошел факт логина loginDate.
   */
  @tailrec
  private def computeNextWindowRange(fromDate: Instant, loginDate: Instant): (Instant, Instant) = {
    val toDate = fromDate.plusMillis(windowSize.toMillis)
    if (loginDate >= fromDate && loginDate < toDate) {
      (fromDate, toDate)
    } else {
      computeNextWindowRange(toDate, loginDate)
    }
  }
}
