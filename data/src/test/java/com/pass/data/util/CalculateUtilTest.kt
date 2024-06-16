package com.pass.data.util

import com.pass.data.di.DateTimeProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CalculateUtilTest {

    private val mockTime = "20240601224430"
    private val mockParseTime = LocalDateTime.parse(mockTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    private val mockLocalDateTimeParse = mockk<(String) -> LocalDateTime>()
    private val mockDurationBetween = mockk<(LocalDateTime, LocalDateTime) -> Duration>()
    private val mockFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    private val mockDateTimeProvider = mockk<DateTimeProvider>()

    private val calculateUtil = CalculateUtil(dateTimeProvider = mockDateTimeProvider)

    @Test
    fun testSuccessCalculateAgoTime10Second() {
        val testParseTime = LocalDateTime.parse("20240601224440", mockFormatter)

        every { mockLocalDateTimeParse(any()) } returns LocalDateTime.parse(mockTime, mockFormatter)
        every { mockDurationBetween(any(), any()) } returns Duration.between(
            mockLocalDateTimeParse(mockTime),
            testParseTime
        )

        every { mockDateTimeProvider.durationBetweenNow(any()) } answers {
            Duration.between(mockParseTime, testParseTime)
        }

        println(mockParseTime)
        println(testParseTime)
        println(Duration.between(mockParseTime, testParseTime))

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "10초 전")
    }

    @Test
    fun testSuccessCalculateAgoTime10Minute() {
        val testParseTime = LocalDateTime.parse("20240601225440", mockFormatter)

        every { mockLocalDateTimeParse(any()) } returns LocalDateTime.parse(mockTime, mockFormatter)
        every { mockDurationBetween(any(), any()) } returns Duration.between(
            mockLocalDateTimeParse(mockTime),
            testParseTime
        )

        every { mockDateTimeProvider.durationBetweenNow(any()) } answers {
            Duration.between(mockParseTime, testParseTime)
        }

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "10분 전")
    }

    @Test
    fun testSuccessCalculateAgoTime1Hours() {
        val testParseTime = LocalDateTime.parse("20240601234430", mockFormatter)

        every { mockLocalDateTimeParse(any()) } returns LocalDateTime.parse(mockTime, mockFormatter)
        every { mockDurationBetween(any(), any()) } returns Duration.between(
            mockLocalDateTimeParse(mockTime),
            testParseTime
        )

        every { mockDateTimeProvider.durationBetweenNow(any()) } answers {
            Duration.between(mockParseTime, testParseTime)
        }

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "1시간 전")
    }

    @Test
    fun testSuccessCalculateAgoTime1Days() {
        val testParseTime = LocalDateTime.parse("20240602224430", mockFormatter)

        every { mockLocalDateTimeParse(any()) } returns LocalDateTime.parse(mockTime, mockFormatter)
        every { mockDurationBetween(any(), any()) } returns Duration.between(
            mockLocalDateTimeParse(mockTime),
            testParseTime
        )

        every { mockDateTimeProvider.durationBetweenNow(any()) } answers {
            Duration.between(mockParseTime, testParseTime)
        }

        val result = calculateUtil.calculateAgoTime(mockTime)
        assertEquals(result, "1일 전")
    }
}