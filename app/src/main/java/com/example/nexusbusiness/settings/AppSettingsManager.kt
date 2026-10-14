package com.example.nexusbusiness.settings

import android.content.Context

class AppSettingsManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun isScreenSlideEnabled(): Boolean {
        return preferences.getBoolean(KEY_SCREEN_SLIDE_ENABLED, true)
    }

    fun setScreenSlideEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_SCREEN_SLIDE_ENABLED, enabled)
            .apply()
    }

    fun areBottomMenuLabelsEnabled(): Boolean {
        return preferences.getBoolean(KEY_BOTTOM_MENU_LABELS_ENABLED, true)
    }

    fun setBottomMenuLabelsEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_BOTTOM_MENU_LABELS_ENABLED, enabled)
            .apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "nexusbusiness_settings"
        private const val KEY_SCREEN_SLIDE_ENABLED = "screen_slide_enabled"
        private const val KEY_BOTTOM_MENU_LABELS_ENABLED = "bottom_menu_labels_enabled"
    }
}
