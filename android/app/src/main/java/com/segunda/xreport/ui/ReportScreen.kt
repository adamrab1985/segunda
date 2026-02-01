package com.segunda.xreport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segunda.xreport.api.ApiModule
import com.segunda.xreport.api.XReportResponse
import com.segunda.xreport.data.RegisterPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(prefs: RegisterPreferences, hiddenRegisters: Set<String>, onOpenSettings: () -> Unit) {
    var report by remember { mutableStateOf<XReportResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        loading = true
        error = null
        withContext(Dispatchers.IO) {
            try {
                val response = ApiModule.xReportApi.getLatestReport()
                if (response.isSuccessful) {
                    val body = response.body()
                    report = body
                    body?.stores?.let { prefs.saveLastStoreNames(it.map { s -> s.name }) }
                } else {
                    val bodyStr = response.errorBody()?.string()
                    val detail = bodyStr?.let { s -> try { org.json.JSONObject(s).optString("detail", "").takeIf { it.isNotEmpty() } } catch (_: Exception) { null } }
                    error = if (detail != null) "Error: $detail" else "No report yet (${response.code()})"
                }
            } catch (e: Exception) { error = e.message ?: "Network error" }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segunda X Report", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Registers", tint = Color.White) }
                    IconButton(onClick = { refreshTrigger++ }) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading && report == null -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Loading report…", color = MaterialTheme.colorScheme.onSurface)
                }
                error != null && report == null -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    Text("Run the Python script, then tap refresh.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                report != null -> ReportContent(report!!, hiddenRegisters, Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(PaddingValues(16.dp)))
            }
        }
    }
}

@Composable
private fun ReportContent(report: XReportResponse, hiddenRegisters: Set<String>, modifier: Modifier = Modifier) {
    val visibleStores = report.stores.filter { it.name !in hiddenRegisters }
    Column(modifier) {
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Report", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                Text("${report.reportDate}  ${report.reportTime}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Stores", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        if (visibleStores.isEmpty()) Text("All registers hidden. Tap gear to show.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
        visibleStores.forEach { store ->
            StoreCard(name = store.name, daily = store.daily, monthly = store.monthly)
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Totals", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                TotalRow("Today (incl. VAT)", report.totals.dailyInclVat)
                TotalRow("Today (excl. VAT)", report.totals.dailyExclVat)
                TotalRow("Month (incl. VAT)", report.totals.monthlyInclVat)
                TotalRow("Month (excl. VAT)", report.totals.monthlyExclVat)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StoreCard(name: String, daily: String, monthly: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text("Today: ₪$daily", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp)
            Text("Month: ₪$monthly", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF1B5E20), fontSize = 14.sp)
        Text("₪${"%,.2f".format(value)}", fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20), fontSize = 14.sp)
    }
}
