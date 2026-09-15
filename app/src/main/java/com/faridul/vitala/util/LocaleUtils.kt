package com.faridul.vitala.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object LocaleUtils {
    fun currentLanguageTag(context: Context): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val tag = if (!appLocales.isEmpty) {
            appLocales[0]?.language
        } else {
            context.resources.configuration.locales[0]?.language
        }
        return tag ?: "en"
    }
}
