package com.faridul.vitala.ui.library

import com.faridul.vitala.data.model.Disease

object DiseaseFilter {
    fun filter(diseases: List<Disease>, query: String, category: String?): List<Disease> =
        diseases
            .filter { category == null || it.category == category }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }

    fun categories(diseases: List<Disease>): List<String> =
        diseases.map { it.category }.distinct().sorted()
}
