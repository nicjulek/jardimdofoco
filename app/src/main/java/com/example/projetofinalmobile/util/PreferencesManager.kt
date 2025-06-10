package com.example.projetofinalmobile.util

import android.content.Context
import android.content.SharedPreferences

//SharedPreferences que controla o tempo da chamada de rede para a API de citações

object PreferencesManager {
    private const val PREFS_NAME = "StudyPlantPrefs"
    private const val KEY_LAST_QUOTE_FETCH_TIME = "last_quote_fetch_time"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // salva o timestamp (em milissegundos) da última busca por citação
    fun setLastQuoteFetchTime(context: Context, time: Long) {
        getPreferences(context).edit().putLong(KEY_LAST_QUOTE_FETCH_TIME, time).apply()
    }

    // recupera o timestamp da última busca por citação
    fun getLastQuoteFetchTime(context: Context): Long {
        return getPreferences(context).getLong(KEY_LAST_QUOTE_FETCH_TIME, 0L)
    }
}