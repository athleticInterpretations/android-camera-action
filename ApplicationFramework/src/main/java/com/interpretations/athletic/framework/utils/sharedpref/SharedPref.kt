package com.interpretations.athletic.framework.utils.sharedpref

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

/**
 * @param context  Interface to global information about an application environment
 * @param prefName Name of preference
 */
@SuppressLint("CommitPrefEdits")
class SharedPref constructor(
        context: Context,
        prefName: String?
) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefName, Activity.MODE_PRIVATE)
    private val prefsEditor: Editor

    /**
     * Method for clearing all data of preference
     */
    fun clearAllPreferences() {
        prefsEditor.clear()
        prefsEditor.commit()
    }

    /**
     * Method for remove data of preference
     *
     * @param key The name of the preference to remove
     */
    fun removePreference(key: String) {
        prefsEditor.remove(key)
        prefsEditor.commit()
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     */
    fun setPref(key: String, value: String) {
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     */
    fun setPref(key: String, value: Int) {
        prefsEditor.putInt(key, value)
        prefsEditor.commit()
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     */
    fun setPref(key: String, value: Long) {
        prefsEditor.putLong(key, value)
        prefsEditor.commit()
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     */
    fun setPref(key: String, value: Boolean) {
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     * @return String value
     */
    fun getStringPref(key: String, value: String): String? {
        return sharedPreferences.getString(key, value)
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     * @return int value
     */
    fun getIntPref(key: String, value: Int): Int {
        return sharedPreferences.getInt(key, value)
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     * @return boolean value
     */
    fun getBooleanPref(key: String, value: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, value)
    }

    /**
     * Set shared preference with (key,value) pair
     *
     * @param key   The name of the preference to remove
     * @param value The new value for the preference
     * @return long value
     */
    fun getLongPref(key: String, value: Long): Long {
        return sharedPreferences.getLong(key, value)
    }

    init {
        prefsEditor = sharedPreferences.edit()
    }
}