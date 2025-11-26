package com.gosnow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gosnow.app.ui.ShangXueApp
import com.gosnow.app.ui.theme.GosnowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GosnowTheme {
                ShangXueApp()
            }
        }
    }
}
