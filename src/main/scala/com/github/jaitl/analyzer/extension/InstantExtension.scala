package com.github.jaitl.analyzer.extension

import java.time.Instant

/**
  * Синтаксический сахар для улучшения читаемости кода,
  * упрощает сравнение Instant классов
  */
object InstantExtension {
  implicit class InstExtension(first: Instant) {
    // scalastyle:off method.name

    def >=(second: Instant): Boolean = {
      first.compareTo(second) >= 0
    }

    def >(second: Instant): Boolean = {
      first.compareTo(second) > 0
    }

    def <(second: Instant): Boolean = {
      first.compareTo(second) < 0
    }

    def <=(second: Instant): Boolean = {
      first.compareTo(second) <= 0
    }

    // scalastyle:on method.name
  }
}
