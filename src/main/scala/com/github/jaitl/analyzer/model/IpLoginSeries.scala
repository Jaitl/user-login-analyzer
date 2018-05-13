package com.github.jaitl.analyzer.model

import java.time.Instant

case class LoginDate(login: String, date: Instant)
case class IpLoginSeries(ip: String, start: Instant, stop: Instant, users: Seq[LoginDate])
