package com.familytrip.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.familytrip.companion.data.local.PreferencesManager
import com.familytrip.companion.ui.theme.FamilyTripCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefsManager = PreferencesManager(applicationContext)
        val uri = intent?.data
        val deepLinkToken = when {
            uri?.path?.startsWith("/parent/") == true -> {
                uri.path!!.removePrefix("/parent/").trim()
            }
            uri?.getQueryParameter("token") != null -> {
                uri.getQueryParameter("token")!!.trim()
            }
            else -> null
        }
        setContent {
            FamilyTripCompanionTheme(prefsManager = prefsManager) {
                com.familytrip.companion.ui.navigation.NavGraph(deepLinkToken = deepLinkToken)
            }
        }
    }
}
