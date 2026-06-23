package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.FinTrackApp
import com.example.ui.FinTrackViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support configuration
        enableEdgeToEdge()
        
        // Initialize high-performance ViewModel
        val viewModel = ViewModelProvider(this)[FinTrackViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                FinTrackApp(viewModel = viewModel)
            }
        }
    }
}
