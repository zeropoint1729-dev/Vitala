package com.faridul.vitala.ui.tips

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.faridul.vitala.R
import com.faridul.vitala.VitalaApplication
import com.faridul.vitala.data.model.DailyTip
import com.faridul.vitala.notification.NotificationScheduler
import kotlinx.coroutines.launch

private data class TimeOption(val label: String, val hour: Int, val minute: Int)

private val timeOptions = listOf(
    TimeOption("7:00 am", 7, 0),
    TimeOption("8:00 am", 8, 0),
    TimeOption("9:00 pm", 21, 0)
)

@Composable
fun TipsHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as VitalaApplication
    val scope = rememberCoroutineScope()

    val tips by app.tipRepository.observeAll().collectAsState(initial = emptyList())
    val todayTip by app.tipRepository.observeTodayTip().collectAsState(initial = null)

    val persistedHour by app.preferencesManager.reminderHour.collectAsState(initial = 8)
    val persistedMinute by app.preferencesManager.reminderMinute.collectAsState(initial = 0)
    val reminderEnabled by app.preferencesManager.reminderEnabled.collectAsState(initial = false)

    var selected by remember { mutableStateOf(timeOptions[1]) }
    var permissionDenied by remember { mutableStateOf(false) }

    // Keep the selected pill in sync with whatever was persisted (e.g. on first load).
    LaunchedEffect(persistedHour, persistedMinute) {
        timeOptions.find { it.hour == persistedHour && it.minute == persistedMinute }?.let {
            selected = it
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            NotificationScheduler.schedule(context, selected.hour, selected.minute)
            scope.launch { app.preferencesManager.setReminderTime(selected.hour, selected.minute) }
        } else {
            permissionDenied = true
        }
    }

    fun enableReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationScheduler.schedule(context, selected.hour, selected.minute)
            scope.launch { app.preferencesManager.setReminderTime(selected.hour, selected.minute) }
        }
    }

    val reminderStatusText = when {
        permissionDenied -> stringResource(R.string.tips_permission_denied)
        reminderEnabled -> stringResource(R.string.tips_reminder_set, selected.label)
        else -> stringResource(R.string.tips_enable_reminder)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.tips_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.tips_reminder_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timeOptions.forEach { option ->
                    val isSelected = option == selected
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                            .clickable { selected = option }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { enableReminder() }
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = reminderStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.tips_all_tips),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))

        tips.forEach { tip ->
            TipRow(tip = tip, isToday = tip.id == todayTip?.id)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TipRow(tip: DailyTip, isToday: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = tip.category,
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = tip.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
