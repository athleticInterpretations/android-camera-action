package com.interpretations.athletic.utils

import android.util.Size

/**
 * Utility to compare two provided sizes by their area.
 *
 * <p>Used primarily for finding the largest and smallest supported camera sizes.</p>
 */
internal class CompareSizesByArea : Comparator<Size> {

    override fun compare(o1: Size, o2: Size): Int {
        // we cast here to ensure the multiplications won't overflow
        return java.lang.Long.signum(
            o1.width.toLong() * o1.height -
                    o2.width.toLong() * o2.height
        )
    }
}