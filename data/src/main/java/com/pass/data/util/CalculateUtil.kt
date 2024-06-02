package com.pass.data.util

import com.pass.data.di.DateTimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculateUtil @Inject constructor(
    private val dateTimeProvider: DateTimeProvider
) {

    fun calculateAgoTime(time: String?): String {
        if (time == null) return "0초 전"

        // 두 LocalDateTime의 차이를 계산
        val duration = dateTimeProvider.durationBetweenNow(time)

        // 차이를 일, 시간, 분, 초로 변환하여 출력
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        return if (days != 0L) {
            "${days}일 전"
        } else if (hours != 0L) {
            "${hours}시간 전"
        } else if (minutes != 0L) {
            "${minutes}분 전"
        } else {
            "${seconds}초 전"
        }
    }
}