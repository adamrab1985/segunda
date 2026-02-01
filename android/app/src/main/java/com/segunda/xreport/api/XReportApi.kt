package com.segunda.xreport.api
import retrofit2.Response
import retrofit2.http.GET

interface XReportApi {
    @GET("api/xreport")
    suspend fun getLatestReport(): Response<XReportResponse>
}
