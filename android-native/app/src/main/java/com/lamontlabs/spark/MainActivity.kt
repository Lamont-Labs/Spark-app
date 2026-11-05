package com.lamontlabs.spark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lamontlabs.spark.design.SparkTheme
import com.lamontlabs.spark.navigation.SparkNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SparkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SparkNavigation()
                }
            }
        }
    }
}
