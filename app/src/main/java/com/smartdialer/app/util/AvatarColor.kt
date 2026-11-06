package com.smartdialer.app.util

object AvatarColor {

    private val palette = intArrayOf(
        0xFFEB0029.toInt(),
        0xFFE07A1F.toInt(),
        0xFF2E9E5B.toInt(),
        0xFF2D7FF9.toInt(),
        0xFF8E44AD.toInt(),
        0xFFC2185B.toInt(),
        0xFF00897B.toInt(),
        0xFF5C6BC0.toInt()
    )

    fun colorFor(key: String): Int {
        if (key.isEmpty()) return palette[0]
        val index = Math.abs(key.hashCode()) % palette.size
        return palette[index]
    }
}
