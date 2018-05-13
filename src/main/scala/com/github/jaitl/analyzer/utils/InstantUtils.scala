package com.github.jaitl.analyzer.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InstantUtils {
  def instantFrom(date: String, timeZone: ZoneId): Instant = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(timeZone)
    Instant.from(formatter.parse(date))
  }
}
