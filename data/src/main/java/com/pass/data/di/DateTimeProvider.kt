package com.pass.data.di

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DateTimeProvider @Inject constructor() {

    fun localDateTimeNow(): LocalDateTime {
        return LocalDateTime.now()
    }

    fun dateTimeFormatterOfPattern(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    fun localDateTimeParse(time: String?, formatter: DateTimeFormatter): LocalDateTime {
        return LocalDateTime.parse(time, formatter)
    }

    fun durationBetween(parseTime: LocalDateTime, nowTime: LocalDateTime): Duration {
        return Duration.between(parseTime, nowTime)
    }
}