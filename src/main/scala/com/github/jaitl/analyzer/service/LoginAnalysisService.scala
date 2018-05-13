package com.github.jaitl.analyzer.service

import java.time.Instant

import com.github.jaitl.analyzer.model.IpLoginSeries
import com.github.jaitl.analyzer.model.LoginDate
import com.github.jaitl.analyzer.model.LoginInfo

import scala.util.Try

/**
 * Сервис для анализа количества входов с одного ip за заданное окно.
 *
 * Алгоритм работы:
 * 1. Выполняется запрос к сервису timeWindowService для нарезки loginCollection на окна.
 * 2. Внутри каждого окна выполняется группировка фактов входа по ip.
 * 3. Отбираются ip у которого больше одного факта входа в заданном окне.
 * 4. Вычисляется первый и последний факт входа, а так же список логинов и фактов входа.
 *
 * @param timeWindowService сервис выполняющий нарезку коллекции фактов входа на окна
 */
class LoginAnalysisService(timeWindowService: TimeWindowService) {
  def analyze(loginCollection: Seq[LoginInfo], fromDate: Option[Instant]): Try[Seq[IpLoginSeries]] =
    for {
      series <- timeWindowService.computeWindows(loginCollection, fromDate)
      res = for {
        curSeries <- series
        (ip, login) <- curSeries.loginInfo.groupBy(_.ip) if login.size > 1
        start = login.map(_.date).head
        end = login.map(_.date).last
        users = login.map(l => LoginDate(l.login, l.date))
      } yield IpLoginSeries(ip, start, end, users)
    } yield res
}
