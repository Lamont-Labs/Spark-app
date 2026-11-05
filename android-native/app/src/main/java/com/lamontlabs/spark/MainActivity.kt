package com.lamontlabs.spark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lamontlabs.spark.data.ReflectionRepository\nimport com.lamontlabs.spark.data.SparkDatabase\nimport com.lamontlabs.spark.design.SparkTheme
import com.lamontlabs.spark.navigation.SparkNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ReflectionRepository(SparkDatabase.get(applicationContext).reflectionDao())\n        setContent {
            SparkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SparkNavigation(repository)
                }
            }
        }
    }
}
