package com.interpretations.athletic.utils

import android.util.Size

internal class CompareSizesByArea : Comparator<Size> {

    override fun compare(o1: Size, o2: Size): Int {
        // We cast here to ensure the multiplications won't overflow
        return java.lang.Long.signum(
            o1.width.toLong() * o1.height -
                    o2.width.toLong() * o2.height
        )
    }
}