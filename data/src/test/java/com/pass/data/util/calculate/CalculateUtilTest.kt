package com.pass.data.util.calculate

import com.pass.data.di.DateTimeProvider
import com.pass.data.util.CalculateUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CalculateUtilTest {

    private val mockDateTimeProvider = mockk<DateTimeProvider>()
    private val calculateUtil = CalculateUtil(mockDateTimeProvider)
    private val testFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    private val mockTime = "20240601224430"

    @Test
    fun testSuccessCalculateAgoTime10Second() {
        val testTime = LocalDateTime.parse("20240601224440", testFormatter)

        every { mockDateTimeProvider.dateTimeFormatterOfPattern() } returns DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        every { mockDateTimeProvider.localDateTimeParse(any(), any()) } returns LocalDateTime.parse(mockTime, testFormatter)
        every { mockDateTimeProvider.localDateTimeNow() } returns testTime
        every { mockDateTimeProvider.durationBetween(any(), any()) } returns Duration.between(
            mockDateTimeProvider.localDateTimeParse(mockTime, testFormatter),
            mockDateTimeProvider.localDateTimeNow()
        )

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "10초 전")
    }

    @Test
    fun testSuccessCalculateAgoTime10Minute() {
        val testTime = LocalDateTime.parse("20240601225440", testFormatter)

        every { mockDateTimeProvider.dateTimeFormatterOfPattern() } returns DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        every { mockDateTimeProvider.localDateTimeParse(any(), any()) } returns LocalDateTime.parse(mockTime, testFormatter)
        every { mockDateTimeProvider.localDateTimeNow() } returns testTime
        every { mockDateTimeProvider.durationBetween(any(), any()) } returns Duration.between(
            mockDateTimeProvider.localDateTimeParse(mockTime, testFormatter),
            mockDateTimeProvider.localDateTimeNow()
        )

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "10분 전")
    }
}