package com.segunda.xreport.api
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface XReportApi {
    @GET("api/xreport")
    suspend fun getLatestReport(): Response<XReportResponse>
    
    @POST("api/trigger-scrape")
    suspend fun triggerScrape(): Response<TriggerResponse>
}

data class TriggerResponse(
    val ok: Boolean,
    val message: String? = null,
    val error: String? = null
)
