@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.example.appmoviles

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appmoviles.bluetooth.BluetoothClientManager
import com.example.appmoviles.bluetooth.BluetoothServerManager
import com.example.appmoviles.cache.VideoCacheManager
import com.example.appmoviles.data.local.AppDatabase
import com.example.appmoviles.data.repository.FavoritesRepository
import com.example.appmoviles.data.repository.HistoryRepository
import com.example.appmoviles.player.PlayerManager
import com.example.appmoviles.ui.ClientViewModelFactory
import com.example.appmoviles.ui.ServerViewModelFactory
import com.example.appmoviles.ui.client.ClientViewModel
import com.example.appmoviles.ui.client.DevicePickerScreen
import com.example.appmoviles.ui.client.FavoritesScreen
import com.example.appmoviles.ui.client.HistoryScreen
import com.example.appmoviles.ui.client.PlayerScreen
import com.example.appmoviles.ui.client.SearchScreen
import com.example.appmoviles.ui.home.ModeSelectorScreen
import com.example.appmoviles.ui.server.ServerScreen
import com.example.appmoviles.ui.server.ServerViewModel
import com.example.appmoviles.ui.theme.AppMovilesTheme
import com.example.appmoviles.ui.theme.AppTheme
import com.example.appmoviles.video.MockYouTubeProvider
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import java.io.File
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppMovilesRoot() }
    }
}

private val requiredPermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    else emptyArray()

@SuppressLint("MissingPermission")
@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun AppMovilesRoot() {
    var theme by remember { mutableStateOf(AppTheme.GUINDA) }
    var forceDark by remember { mutableStateOf<Boolean?>(null) }
    var permissionsGranted by remember { mutableStateOf(requiredPermissions.isEmpty()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> permissionsGranted = result.values.all { it } }

    LaunchedEffect(Unit) {
        if (requiredPermissions.isNotEmpty()) permissionLauncher.launch(requiredPermissions)
    }

    AppMovilesTheme(appTheme = theme, forceDarkMode = forceDark) {
        if (!permissionsGranted) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Se necesitan permisos de Bluetooth para continuar.\nActívalos en Ajustes.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@AppMovilesTheme
        }

        val context = LocalContext.current
        val navController = rememberNavController()

        val bluetoothManager = remember {
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        val adapter: BluetoothAdapter = remember { bluetoothManager.adapter }
        val videoProvider = remember { MockYouTubeProvider() }
        val cache = remember { VideoCacheManager(File(context.cacheDir, "video_cache")) }
        val db = remember { AppDatabase.getInstance(context) }
        val historyRepo = remember { HistoryRepository(db.historyDao()) }
        val favoritesRepo = remember { FavoritesRepository(db.favoriteDao()) }

        // ClientViewModel compartido entre todas las pantallas del Cliente
        var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
        val clientViewModel: ClientViewModel? = remember(selectedDevice) {
            Log.d("DIAG_MAIN", "selectedDevice=$selectedDevice")
            selectedDevice?.let { device ->
                Log.d("DIAG_MAIN", "Creando ClientViewModel para device=$device")
                val viewModel = ClientViewModelFactory(
                    BluetoothClientManager(device, adapter),
                    PlayerManager(context),
                    historyRepo,
                    favoritesRepo
                ).create(ClientViewModel::class.java)
                Log.d("DIAG_MAIN", "ClientViewModel creado exitosamente")
                viewModel
            }
        }

        NavHost(navController = navController, startDestination = "home") {

            composable("home") {
                ModeSelectorScreen(
                    currentTheme = theme,
                    onThemeChange = { theme = it },
                    forceDarkMode = forceDark,
                    onDarkModeChange = { forceDark = it },
                    onSelectServer = { navController.navigate("server") },
                    onSelectClient = { navController.navigate("device_picker") }
                )
            }

            composable("server") {
                val serverManager = remember {
                    BluetoothServerManager(adapter, videoProvider, cache)
                }
                val viewModel: ServerViewModel = composeViewModel(
                    factory = ServerViewModelFactory(
                        serverManager,
                        context = context   // <-- context declarado en línea 99
                    )
                )
                ServerScreen(viewModel)
            }

            // Pantalla de selección de dispositivo (NUEVO)
            composable("device_picker") {
                DevicePickerScreen(
                    adapter = adapter,
                    onDeviceSelected = { device ->
                        selectedDevice = device
                        navController.navigate("search") {
                            popUpTo("device_picker") { inclusive = true }
                        }
                    }
                )
            }

            composable("search") {
                val vm = clientViewModel
                if (vm == null) {
                    navController.navigate("device_picker")
                } else {
                    LaunchedEffect(Unit) { vm.connect() }
                    SearchScreen(
                        viewModel = vm,
                        onVideoSelected = { navController.navigate("player") }
                    )
                }
            }

            composable("player") {
                val vm = clientViewModel
                if (vm == null) navController.navigate("device_picker")
                else PlayerScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("history") {
                val vm = clientViewModel
                if (vm == null) navController.navigate("device_picker")
                else HistoryScreen(vm)
            }

            composable("favorites") {
                val vm = clientViewModel
                if (vm == null) navController.navigate("device_picker")
                else FavoritesScreen(vm)
            }
        }
    }
}