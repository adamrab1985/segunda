package com.segunda.xreport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segunda.xreport.data.RegisterPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefs: RegisterPreferences, onBack: () -> Unit, onHiddenChanged: () -> Unit) {
    val storeNames = remember { prefs.getLastStoreNames() }
    var hidden by remember { mutableStateOf(prefs.getHiddenRegisters()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registers", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Show or hide registers on the report. Hidden = removed from list.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
            if (storeNames.isEmpty()) Text("Fetch a report first (Refresh on main screen), then come back.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
            else storeNames.forEach { name ->
                val isVisible = !hidden.contains(name)
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Switch(checked = isVisible, onCheckedChange = { prefs.setVisible(name, it); hidden = prefs.getHiddenRegisters(); onHiddenChanged() }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("To add a register: add it in scripts/caspit_xreport.py ACCOUNTS, run script, refresh app.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}
