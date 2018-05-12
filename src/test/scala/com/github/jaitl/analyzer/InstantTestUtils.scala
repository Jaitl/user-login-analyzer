package com.github.jaitl.analyzer

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InstantTestUtils {
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    .withZone(ZoneId.of("UTC"))

  def instantFrom(date: String): Instant = {
    Instant.from(formatter.parse(date))
  }
}
