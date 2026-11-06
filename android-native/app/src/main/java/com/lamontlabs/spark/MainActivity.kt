package com.lamontlabs.spark

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.lamontlabs.spark.privacy.PrivacyPreferences
import com.lamontlabs.spark.reminders.ReminderScheduler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lamontlabs.spark.data.ReflectionRepository
import com.lamontlabs.spark.data.SparkDatabase
import com.lamontlabs.spark.design.SparkTheme
import com.lamontlabs.spark.navigation.SparkNavigation
import com.lamontlabs.spark.editor.ReflectionEditor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.lamontlabs.spark.onboarding.FirstLightScreen
import com.lamontlabs.spark.onboarding.OnboardingPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ReflectionRepository(SparkDatabase.get(applicationContext).reflectionDao())
        setContent {
            SparkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val onboarding = remember(context) { OnboardingPreferences(context) }
                    val completed by onboarding.completed.collectAsState(initial = null)
                    val step by onboarding.step.collectAsState(initial = 0)
                    val focus by onboarding.focus.collectAsState(initial = "EVERYTHING")
                    val scope = rememberCoroutineScope()
                    val reminderPreferences = remember(context) { PrivacyPreferences(context) }
                    val remindersEnabled by reminderPreferences.remindersEnabled.collectAsState(initial = false)
                    val reminderHour by reminderPreferences.reminderHour.collectAsState(initial = 19)
                    var reminderMessage by rememberSaveable { mutableStateOf<String?>(null) }
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) {
                            scope.launch {
                                reminderPreferences.setRemindersEnabled(true)
                                ReminderScheduler.configure(context, true, reminderHour)
                                reminderMessage = null
                            }
                        } else {
                            reminderMessage = "Notifications are off. You can enable them later in preferences."
                        }
                    }
                    var onboardingEditor by rememberSaveable { mutableStateOf<String?>(null) }
                    when (completed) {
                        null -> Unit
                        true -> SparkNavigation(repository)
                        false -> if (onboardingEditor != null) {
                            ReflectionEditor(
                                kind = onboardingEditor!!,
                                repository = repository,
                                onSaved = { onboardingEditor = null; scope.launch { onboarding.setStep(3) } },
                                onCancel = { onboardingEditor = null }
                            )
                        } else FirstLightScreen(
                            step = step,
                            focus = focus,
                            onFocus = { selected -> scope.launch { onboarding.setFocus(selected) } },
                            onWrite = { kind -> onboardingEditor = kind },
                            remindersEnabled = remindersEnabled,
                            reminderHour = reminderHour,
                            onReminderToggle = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    scope.launch {
                                        reminderPreferences.setRemindersEnabled(enabled)
                                        ReminderScheduler.configure(context, enabled, reminderHour)
                                        reminderMessage = null
                                    }
                                }
                            },
                            onReminderHour = { hour ->
                                scope.launch {
                                    reminderPreferences.setReminderHour(hour)
                                    if (remindersEnabled) ReminderScheduler.configure(context, true, hour)
                                }
                            },
                            reminderMessage = reminderMessage,
                            onNext = { scope.launch { if (step >= 4) onboarding.finish() else onboarding.setStep(step + 1) } },
                            onBack = { scope.launch { onboarding.setStep(step - 1) } },
                            onSkip = { scope.launch { onboarding.finish() } }
                        )
                    }
                }
            }
        }
    }
}
