package com.faridul.vitala.data.local

import android.content.Context
import com.faridul.vitala.data.model.Disease
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DiseaseAssetLoader {
    fun loadForLanguage(context: Context, languageTag: String): List<Disease> {
        val fileName = if (languageTag == "bn") "diseases_bn.json" else "diseases.json"
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Disease>>() {}.type
        return Gson().fromJson(json, listType)
    }
}
