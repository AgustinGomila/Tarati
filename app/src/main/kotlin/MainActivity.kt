package com.agustin.tarati

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.agustin.tarati.ui.screens.MainScreen
import com.agustin.tarati.ui.theme.TaratiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaratiTheme {
                MainScreen()
            }
        }
    }
}