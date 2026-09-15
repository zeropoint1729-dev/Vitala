package com.faridul.vitala.data.remote

import com.google.gson.JsonObject

data class PubMedSearchResponse(
    val esearchresult: EsearchResult?
)

data class EsearchResult(
    val idlist: List<String>?
)

data class PubMedSummaryResponse(
    val result: JsonObject?
)
