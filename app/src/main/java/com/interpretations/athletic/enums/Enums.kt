package com.interpretations.athletic.enums

/**
 * Enumeration for camera types. CameraInfo was deprecated in v2. Using
 * enumeration for replacement.
 *
 * @property value String The String value of 0 indicates back facing camera, and
 * 1 indicates front facing camera.
 * @constructor
 */
enum class CameraInfo(private val value: String) {
    BACK_FACING_CAMERA("0"),
    FRONT_FACING_CAMERA("1");

    override fun toString(): String {
        return value
    }
}