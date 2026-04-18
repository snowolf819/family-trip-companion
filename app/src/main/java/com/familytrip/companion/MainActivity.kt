package com.familytrip.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.familytrip.companion.ui.theme.FamilyTripCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyTripCompanionTheme {
                com.familytrip.companion.ui.navigation.NavGraph()
            }
        }
    }
}
