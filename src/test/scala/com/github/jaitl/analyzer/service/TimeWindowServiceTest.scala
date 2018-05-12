package com.github.jaitl.analyzer.service

import com.github.jaitl.analyzer.InstantTestUtils
import com.github.jaitl.analyzer.exception.EmptyLoginCollection
import com.github.jaitl.analyzer.model.LoginInfo
import com.github.jaitl.analyzer.model.WindowLoginInfo
import org.scalatest.FunSuite
import org.scalatest.Matchers

class TimeWindowServiceTest extends FunSuite with Matchers {

  import InstantTestUtils._

  import scala.concurrent.duration._

  test("empty loginCollection") {
    val timeWindowService = new TimeWindowService(4.seconds)
    val result = timeWindowService.computeWindows(Seq.empty, None)

    result.failed.get.getClass shouldBe classOf[EmptyLoginCollection]
  }

  test("loginCollection with a window size of 4 seconds") {
    val dates = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:01")),
      LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:02")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:05")),
      LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:00:07")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:09")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:10"))
    )

    val timeWindowService = new TimeWindowService(4.seconds)

    val result = timeWindowService.computeWindows(dates, Some(instantFrom("2018-01-01 00:00:00"))).get

    result should have size 3

    val firstWindow = result.head
    firstWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:00"),
      endDate = instantFrom("2018-01-01 00:00:04"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:01")),
        LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:02"))
      )
    )

    val secondWindow = result(1)
    secondWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:04"),
      endDate = instantFrom("2018-01-01 00:00:08"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:05")),
        LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:00:07"))
      )
    )

    val thirdWindow = result(2)
    thirdWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:08"),
      endDate = instantFrom("2018-01-01 00:00:12"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:09")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:10"))
      )
    )
  }

  test("cases around the beginning and end of the window size - window size of 4 seconds") {
    val dates = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
      LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:03")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:04")),
      LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:00:07")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:08")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:11"))
    )

    val timeWindowService = new TimeWindowService(4.seconds)

    val result = timeWindowService.computeWindows(dates, None).get

    result should have size 3

    val firstWindow = result.head
    firstWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:00"),
      endDate = instantFrom("2018-01-01 00:00:04"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
        LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:03"))
      )
    )

    val secondWindow = result(1)
    secondWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:04"),
      endDate = instantFrom("2018-01-01 00:00:08"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:04")),
        LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:00:07"))
      )
    )

    val thirdWindow = result(2)
    thirdWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:08"),
      endDate = instantFrom("2018-01-01 00:00:12"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:08")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:11"))
      )
    )
  }

  test("cases around the beginning and end of the window size - window size of 1 minute") {
    val dates = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
      LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:59")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:01:00")),
      LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:01:59")),
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:02:00")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:02:59"))
    )

    val timeWindowService = new TimeWindowService(1.minute)

    val result = timeWindowService.computeWindows(dates, None).get

    result should have size 3

    val firstWindow = result.head
    firstWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:00"),
      endDate = instantFrom("2018-01-01 00:01:00"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
        LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:59"))
      )
    )

    val secondWindow = result(1)
    secondWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:01:00"),
      endDate = instantFrom("2018-01-01 00:02:00"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:01:00")),
        LoginInfo("user3", "123.11.11.11", instantFrom("2018-01-01 00:01:59"))
      )
    )

    val thirdWindow = result(2)
    thirdWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:02:00"),
      endDate = instantFrom("2018-01-01 00:03:00"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:02:00")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:02:59"))
      )
    )
  }

  test("loginCollection with omissions with a window size of 4 seconds") {
    val dates = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
      LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:02")),
      // omit from 2018-01-01 00:00:04 to 2018-01-01 00:00:08
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:09")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:11")),
      // omit from 2018-01-01 00:00:12 to 2018-01-01 00:00:16
      // omit from 2018-01-01 00:00:20 to 2018-01-01 00:00:24
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:25")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:27"))
    )

    val timeWindowService = new TimeWindowService(4.seconds)

    val result = timeWindowService.computeWindows(dates, None).get

    result should have size 3

    val firstWindow = result.head
    firstWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:00"),
      endDate = instantFrom("2018-01-01 00:00:04"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:00")),
        LoginInfo("user2", "123.11.11.11", instantFrom("2018-01-01 00:00:02"))
      )
    )

    val secondWindow = result(1)
    secondWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:08"),
      endDate = instantFrom("2018-01-01 00:00:12"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:09")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:11"))
      )
    )

    val thirdWindow = result(2)
    thirdWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:24"),
      endDate = instantFrom("2018-01-01 00:00:28"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:25")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:27"))
      )
    )
  }

  test("dates with omissions in the beginning") {
    val dates = Seq(
      LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:25")),
      LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:27"))
    )

    val timeWindowService = new TimeWindowService(4.seconds)

    val result = timeWindowService.computeWindows(dates, Some(instantFrom("2018-01-01 00:00:00"))).get

    result should have size 1

    val firstWindow = result.head
    firstWindow shouldBe WindowLoginInfo(
      startDate = instantFrom("2018-01-01 00:00:24"),
      endDate = instantFrom("2018-01-01 00:00:28"),
      loginInfo = Seq(
        LoginInfo("user1", "123.11.11.11", instantFrom("2018-01-01 00:00:25")),
        LoginInfo("user4", "123.11.11.11", instantFrom("2018-01-01 00:00:27"))
      )
    )
  }
}
