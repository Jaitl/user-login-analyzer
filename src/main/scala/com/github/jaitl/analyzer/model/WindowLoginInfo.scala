package com.github.jaitl.analyzer.model

import java.time.Instant

case class WindowLoginInfo(startDate: Instant, endDate: Instant, loginInfo: Seq[LoginInfo])
