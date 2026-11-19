package com.boat.hearables.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boat.hearables.pro.ui.screens.CockpitScreen
import com.boat.hearables.pro.ui.screens.ControlsScreen
import com.boat.hearables.pro.ui.screens.DiagnosticsScreen
import com.boat.hearables.pro.ui.screens.EqualizerScreen
import com.boat.hearables.pro.ui.screens.SettingsScreen
import com.boat.hearables.pro.ui.theme.BoatHearablesProTheme
import com.boat.hearables.pro.viewmodel.HearablesViewModel

private data class NavTab(
    val title: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestBluetoothPermissionsIfNeeded()

        setContent {
            val vm: HearablesViewModel = viewModel()
            val uiState by vm.uiState.collectAsState()
            val telemetry by vm.telemetry.collectAsState()
            val pairedDevices by vm.pairedDevices.collectAsState()
            val packetLogs by vm.packetLogs.collectAsState()

            BoatHearablesProTheme(
                accentTheme = uiState.accentTheme,
                oledPitchBlack = uiState.oledPitchBlack
            ) {
                HearablesProApp(
                    viewModel = vm,
                    uiState = uiState,
                    telemetry = telemetry,
                    pairedDevices = pairedDevices,
                    packetLogs = packetLogs
                )
            }
        }
    }

    private fun requestBluetoothPermissionsIfNeeded() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
private fun HearablesProApp(
    viewModel: HearablesViewModel,
    uiState: com.boat.hearables.pro.viewmodel.HearablesUiState,
    telemetry: com.boat.hearables.pro.protocol.LiveTelemetry,
    pairedDevices: List<com.boat.hearables.pro.protocol.PairedBtDevice>,
    packetLogs: List<com.boat.hearables.pro.protocol.ProtocolPacketLog>
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        NavTab("Cockpit", Icons.Default.Dashboard),
        NavTab("Studio EQ", Icons.Default.GraphicEq),
        NavTab("Controls", Icons.Default.TouchApp),
        NavTab("Lab & Find", Icons.Default.Science),
        NavTab("Settings", Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CockpitScreen(
                    uiState = uiState,
                    telemetry = telemetry,
                    pairedDevices = pairedDevices,
                    viewModel = viewModel,
                    onNavigateToEq = { selectedTab = 1 }
                )
                1 -> EqualizerScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                2 -> ControlsScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                3 -> DiagnosticsScreen(
                    uiState = uiState,
                    telemetry = telemetry,
                    packetLogs = packetLogs,
                    viewModel = viewModel
                )
                4 -> SettingsScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}
