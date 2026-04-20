package com.familytrip.companion

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.familytrip.companion.data.local.PreferencesManager
import com.familytrip.companion.ui.theme.FamilyTripCompanionTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private val TOKEN_REGEX = Regex("^[a-zA-Z0-9_-]{1,128}$")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefsManager = PreferencesManager(applicationContext)
        val uri = intent?.data
        var deepLinkToken: String? = null
        when {
            uri?.path?.startsWith("/parent/") == true -> {
                val raw = uri.path!!.removePrefix("/parent/").trim()
                if (TOKEN_REGEX.matches(raw)) {
                    deepLinkToken = raw
                } else {
                    Log.w(TAG, "Invalid deep link token rejected: length=${raw.length}")
                }
            }
            uri?.getQueryParameter("token") != null -> {
                val raw = uri.getQueryParameter("token")!!.trim()
                if (TOKEN_REGEX.matches(raw)) {
                    deepLinkToken = raw
                } else {
                    Log.w(TAG, "Invalid query token rejected: length=${raw.length}")
                }
            }
        }
        setContent {
            FamilyTripCompanionTheme(prefsManager = prefsManager) {
                com.familytrip.companion.ui.navigation.NavGraph(deepLinkToken = deepLinkToken)
            }
        }
    }
}
