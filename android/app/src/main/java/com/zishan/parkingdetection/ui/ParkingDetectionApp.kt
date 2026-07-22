package com.zishan.parkingdetection.ui

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.text.DateFormat
import java.util.Date

@Composable
fun ParkingDetectionApp() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen(onHistory = { navController.navigate("history") }, onSettings = { navController.navigate("settings") }, onPermissions = { navController.navigate("permissions") }) }
                composable("history") { HistoryScreen(onBack = { navController.popBackStack() }) }
                composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
                composable("permissions") { PermissionsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onPermissions: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val preciseLocationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.saveManualParking()
        else viewModel.onPreciseLocationPermissionDenied()
    }
    val saveParking = {
        val hasPreciseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPreciseLocation) viewModel.saveManualParking()
        else preciseLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Parking Detection") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detection status", fontWeight = FontWeight.Bold)
                        Text(if (state.settings.automaticDetectionEnabled) "Automatic detection enabled" else "Automatic detection disabled")
                        Text("Activity: ${state.currentActivity}")
                        Text("Bluetooth: ${state.bluetoothStatus}")
                    }
                }
            }
            item { ParkingDetailsCard(state, viewModel) }
            item {
                Button(
                    onClick = saveParking,
                    enabled = !state.isSavingManualParking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSavingManualParking) "Getting precise location…" else "Save Parking Location")
                }
                state.manualSaveMessage?.let { message ->
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::openNavigation, enabled = state.current != null) { Text("Navigate") }
                    OutlinedButton(onClick = onHistory) { Text("History") }
                    OutlinedButton(onClick = onSettings) { Text("Settings") }
                }
            }
            item { OutlinedButton(onClick = onPermissions, modifier = Modifier.fillMaxWidth()) { Text("Permission onboarding") } }
        }
    }
}

@Composable
private fun ParkingDetailsCard(state: HomeUiState, viewModel: HomeViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Saved parking", fontWeight = FontWeight.Bold)
            val parking = state.current
            if (parking == null) {
                Text("No parking location saved yet. Manual saving works without background permissions.")
                Text("Map preview placeholder: the app opens external maps through a geo intent.")
            } else {
                Text(parking.address ?: "Address pending")
                Text("Parked: ${DateFormat.getDateTimeInstance().format(Date(parking.parkedAtEpochMillis))}")
                Text("Coordinates: %.5f, %.5f".format(parking.latitude, parking.longitude))
                Text("Detection: ${parking.detectionMethod} • confidence ${parking.confidence}%")
                Text("Distance: calculated when current location is available • walk time is approximate")
                Text("Map preview placeholder with accuracy radius")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::shareLocation) { Text("Share") }
                    OutlinedButton(onClick = viewModel::deleteCurrent) { Text("Delete") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HomeViewModel = hiltViewModel(), onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Parking History") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedButton(onClick = onBack) { Text("Back") } }
            item { OutlinedButton(onClick = viewModel::clearHistory, enabled = state.history.isNotEmpty()) { Text("Clear all history") } }
            items(state.history) { parking ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(DateFormat.getDateTimeInstance().format(Date(parking.parkedAtEpochMillis)), fontWeight = FontWeight.Bold)
                        Text(parking.address ?: "Address pending")
                        Text("${parking.detectionMethod} • ${parking.confidence}%")
                        Text("%.5f, %.5f".format(parking.latitude, parking.longitude))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: HomeViewModel = hiltViewModel(), onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Detection Settings") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            SettingSwitch("Automatic detection", state.settings.automaticDetectionEnabled, viewModel::setAutomaticDetection)
            SettingSwitch("Bluetooth-based detection", state.settings.bluetoothDetectionEnabled, viewModel::setBluetoothDetection)
            Text("Confidence threshold: ${state.settings.confidenceThreshold}%")
            Slider(value = state.settings.confidenceThreshold.toFloat(), onValueChange = {}, valueRange = 1f..100f, enabled = false)
            Text("Cooldown: ${state.settings.cooldownMinutes} minutes")
            Divider()
            Text("Vehicle Bluetooth configuration")
            Text("Paired-device selection is shown after Bluetooth permission is granted. Missing permission is handled without crashing.")
            Text("Privacy: parking history stays local. Delete all history from the History screen.")
            Text("Battery: high accuracy is used only for manual saves or short confirmation windows.")
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION to "Foreground location saves the parked vehicle coordinate.")
        add(Manifest.permission.ACTIVITY_RECOGNITION to "Activity recognition detects vehicle-to-still and walking transitions.")
        if (Build.VERSION.SDK_INT >= 31) add(Manifest.permission.BLUETOOTH_CONNECT to "Bluetooth identifies configured vehicle devices.")
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS to "Notifications explain when parking was saved.")
        add(Manifest.permission.ACCESS_BACKGROUND_LOCATION to "Optional background location improves automatic detection; request separately.")
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Permissions") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { OutlinedButton(onClick = onBack) { Text("Back") } }
            items(permissions) { (permission, reason) ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(permission.substringAfterLast('.'), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(reason)
                    }
                }
            }
        }
    }
}
