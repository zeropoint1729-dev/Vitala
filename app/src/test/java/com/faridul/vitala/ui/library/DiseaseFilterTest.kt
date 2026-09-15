package com.faridul.vitala.ui.library

import com.faridul.vitala.data.model.Disease
import org.junit.Assert.assertEquals
import org.junit.Test

class DiseaseFilterTest {

    private val sample = listOf(
        Disease("d1", "Type 2 diabetes", "endocrine", "o", listOf("s1"), "t", "src"),
        Disease("d2", "Hypertension", "cardiovascular", "o", listOf("s1"), "t", "src"),
        Disease("d3", "Dengue fever", "infectious", "o", listOf("s1"), "t", "src")
    )

    @Test
    fun `no filters returns everything`() {
        assertEquals(3, DiseaseFilter.filter(sample, "", null).size)
    }

    @Test
    fun `category filter narrows results`() {
        val result = DiseaseFilter.filter(sample, "", "infectious")
        assertEquals(1, result.size)
        assertEquals("Dengue fever", result.first().name)
    }

    @Test
    fun `query matches name case-insensitively`() {
        val result = DiseaseFilter.filter(sample, "hyper", null)
        assertEquals(1, result.size)
        assertEquals("Hypertension", result.first().name)
    }

    @Test
    fun `query and category combine`() {
        val result = DiseaseFilter.filter(sample, "fever", "infectious")
        assertEquals(1, result.size)

        val miss = DiseaseFilter.filter(sample, "fever", "endocrine")
        assertEquals(0, miss.size)
    }

    @Test
    fun `categories are distinct and sorted`() {
        val cats = DiseaseFilter.categories(sample)
        assertEquals(listOf("cardiovascular", "endocrine", "infectious"), cats)
    }
}
