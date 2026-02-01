package com.segunda.xreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.segunda.xreport.data.RegisterPreferences
import com.segunda.xreport.ui.ReportScreen
import com.segunda.xreport.ui.SettingsScreen
import com.segunda.xreport.ui.theme.XReportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = RegisterPreferences(applicationContext)
        setContent {
            XReportTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    var showSettings by remember { mutableStateOf(false) }
                    var hiddenRegisters by remember { mutableStateOf(prefs.getHiddenRegisters()) }
                    if (showSettings) {
                        SettingsScreen(
                            prefs = prefs,
                            onBack = { showSettings = false; hiddenRegisters = prefs.getHiddenRegisters() },
                            onHiddenChanged = { hiddenRegisters = prefs.getHiddenRegisters() },
                        )
                    } else {
                        ReportScreen(
                            prefs = prefs,
                            hiddenRegisters = hiddenRegisters,
                            onOpenSettings = { showSettings = true },
                        )
                    }
                }
            }
        }
    }
}
