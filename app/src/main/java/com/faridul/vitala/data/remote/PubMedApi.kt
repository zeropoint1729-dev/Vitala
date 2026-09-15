package com.faridul.vitala.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PubMedApi {
    @GET("esearch.fcgi")
    suspend fun search(
        @Query("term") term: String,
        @Query("db") db: String = "pubmed",
        @Query("retmode") retmode: String = "json",
        @Query("retmax") retmax: Int = 5,
        @Query("sort") sort: String = "date"
    ): PubMedSearchResponse

    @GET("esummary.fcgi")
    suspend fun summary(
        @Query("id") ids: String,
        @Query("db") db: String = "pubmed",
        @Query("retmode") retmode: String = "json"
    ): PubMedSummaryResponse
}
