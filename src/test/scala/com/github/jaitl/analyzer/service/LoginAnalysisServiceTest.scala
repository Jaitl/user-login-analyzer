package com.github.jaitl.analyzer.service

import com.github.jaitl.analyzer.InstantTestUtils
import com.github.jaitl.analyzer.model.IpLoginSeries
import com.github.jaitl.analyzer.model.LoginDate
import com.github.jaitl.analyzer.model.LoginInfo
import org.scalatest.FunSuite
import org.scalatest.Matchers

class LoginAnalysisServiceTest extends FunSuite with Matchers {

  import InstantTestUtils._
  import scala.concurrent.duration._

  test("detect multiple login from one ip with a window size of 1 minute") {
    val loginCollection = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:22")),
      LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:43")),
      LoginInfo("user1", "123.11.11.12", instantFrom("2018-01-01 00:01:11")),
      LoginInfo("user3", "123.11.11.13", instantFrom("2018-01-01 00:01:33")),
      LoginInfo("user1", "123.11.11.14", instantFrom("2018-01-01 00:02:33")),
      LoginInfo("user4", "123.11.11.14", instantFrom("2018-01-01 00:02:44"))
    )

    val loginAnalysisService = new LoginAnalysisService(new TimeWindowService(1.minute))

    val result = loginAnalysisService.analyze(loginCollection, Some(instantFrom("2018-01-01 00:00:00"))).get

    result should have size 2

    val firstSeries = result.head
    firstSeries shouldBe IpLoginSeries(
      ip = "123.11.11.11",
      start = instantFrom("2018-01-01 00:00:22"),
      stop = instantFrom("2018-01-01 00:00:43"),
      users = Seq(
        LoginDate("user1", instantFrom("2018-01-01 00:00:22")),
        LoginDate("user2", instantFrom("2018-01-01 00:00:43"))
      )
    )

    val secondSeries = result(1)
    secondSeries shouldBe IpLoginSeries(
      ip = "123.11.11.14",
      start = instantFrom("2018-01-01 00:02:33"),
      stop = instantFrom("2018-01-01 00:02:44"),
      users = Seq(
        LoginDate("user1", instantFrom("2018-01-01 00:02:33")),
        LoginDate("user4", instantFrom("2018-01-01 00:02:44"))
      )
    )
  }
}
