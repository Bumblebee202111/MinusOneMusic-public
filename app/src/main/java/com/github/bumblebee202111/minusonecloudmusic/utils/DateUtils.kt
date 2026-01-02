package com.github.bumblebee202111.minusonecloudmusic.utils

import java.util.Calendar

object DateUtils {

    fun getCurrentMonthDisplayText(): String {
        return (Calendar.getInstance()[2] + 1).toString().padStart(2, '0')
    }

    fun getCurrentDayDisplayText(): String {
        return Calendar.getInstance()[5].toString().padStart(2, '0')
    }
}