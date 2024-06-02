package com.pass.data.di

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DateTimeProvider @Inject constructor() {

    fun durationBetweenNow(time: String?): Duration {
        return Duration.between(localDateTimeParse(time), localDateTimeNow())
    }

    fun localDateTimeNowFormat(): String {
        return localDateTimeNow().format(dateTimeFormatterOfPattern())
    }

    private fun dateTimeFormatterOfPattern(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    private fun localDateTimeNow(): LocalDateTime {
        return LocalDateTime.now()
    }

    private fun localDateTimeParse(time: String?): LocalDateTime {
        return LocalDateTime.parse(time, dateTimeFormatterOfPattern())
    }
}