package com.segunda.xreport.api
import com.google.gson.annotations.SerializedName

data class XReportResponse(
    val id: String,
    @SerializedName("reportDate") val reportDate: String,
    @SerializedName("reportTime") val reportTime: String,
    val stores: List<StoreReport>,
    val totals: Totals,
    @SerializedName("createdAt") val createdAt: String? = null,
)
data class StoreReport(val name: String, val daily: String, val monthly: String)
data class Totals(
    @SerializedName("dailyInclVat") val dailyInclVat: Double,
    @SerializedName("dailyExclVat") val dailyExclVat: Double,
    @SerializedName("monthlyInclVat") val monthlyInclVat: Double,
    @SerializedName("monthlyExclVat") val monthlyExclVat: Double,
)
