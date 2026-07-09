@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.appmoviles.bluetooth.BluetoothClientManager
import com.example.appmoviles.bluetooth.BluetoothServerManager
import com.example.appmoviles.controller.ClientController
import com.example.appmoviles.controller.ServerController
import com.example.appmoviles.player.PlayerManager
import com.example.appmoviles.transfer.VideoTransferManager
import com.example.appmoviles.ui.ClientViewModelFactory
import com.example.appmoviles.ui.ServerViewModelFactory
import com.example.appmoviles.ui.client.ClientViewModel
import com.example.appmoviles.ui.client.DevicePickerScreen
import com.example.appmoviles.ui.client.PlayerScreen
import com.example.appmoviles.ui.client.SearchScreen
import com.example.appmoviles.ui.home.ModeSelectorScreen
import com.example.appmoviles.ui.server.ServerScreen
import com.example.appmoviles.ui.server.ServerViewModel
import com.example.appmoviles.ui.theme.AppMovilesTheme
import com.example.appmoviles.ui.theme.AppTheme
import com.example.appmoviles.video.VideoProvider
import com.example.appmoviles.video.provider.NewPipeVideoProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppMovilesRoot()
        }
    }
}

private val requiredPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        emptyArray()
    }

@SuppressLint("MissingPermission")
@OptIn(
    UnstableApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun AppMovilesRoot() {

    var theme by remember {
        mutableStateOf(AppTheme.GUINDA)
    }

    var forceDark by remember {
        mutableStateOf<Boolean?>(null)
    }

    var permissionsGranted by remember {
        mutableStateOf(requiredPermissions.isEmpty())
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            permissionsGranted = result.values.all { it }
        }

    LaunchedEffect(Unit) {
        if (requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    AppMovilesTheme(
        appTheme = theme,
        forceDarkMode = forceDark
    ) {

        if (!permissionsGranted) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Se requieren permisos Bluetooth.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            return@AppMovilesTheme
        }

        val context = LocalContext.current

        val navController = rememberNavController()

        val bluetoothManager = remember {
            context.getSystemService(
                Context.BLUETOOTH_SERVICE
            ) as BluetoothManager
        }

        val adapter = remember {
            bluetoothManager.adapter
        }

        val transferManager = remember {
            VideoTransferManager()
        }

        val provider: VideoProvider = remember {
            NewPipeVideoProvider()
        }

        var selectedDevice by remember {
            mutableStateOf<BluetoothDevice?>(null)
        }
        val clientViewModel: ClientViewModel? =
            remember(selectedDevice) {

                selectedDevice?.let { device ->

                    val bluetooth =
                        BluetoothClientManager(
                            device = device,
                            adapter = adapter
                        )

                    val controller =
                        ClientController(
                            bluetooth = bluetooth,
                            transfer = transferManager
                        )

                    ClientViewModelFactory(
                        controller = controller,
                        playerManager = PlayerManager(context)
                    ).create(ClientViewModel::class.java)
                }
            }

        val serverViewModel: ServerViewModel =
            composeViewModel(
                factory = ServerViewModelFactory(
                    controller = ServerController(
                        bluetooth = BluetoothServerManager(adapter),
                        transfer = transferManager,
                        videoProvider = provider
                    ),
                    provider = provider
                )
            )

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {

                ModeSelectorScreen(
                    currentTheme = theme,
                    onThemeChange = { theme = it },
                    forceDarkMode = forceDark,
                    onDarkModeChange = { forceDark = it },
                    onSelectServer = {
                        navController.navigate("server")
                    },
                    onSelectClient = {
                        navController.navigate("device_picker")
                    }
                )
            }

            composable("server") {

                ServerScreen(
                    viewModel = serverViewModel
                )
            }

            composable("device_picker") {

                DevicePickerScreen(
                    adapter = adapter,
                    onDeviceSelected = { device ->

                        selectedDevice = device

                        navController.navigate("search") {

                            popUpTo("device_picker") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("search") {

                val vm = clientViewModel

                if (vm == null) {

                    LaunchedEffect(Unit) {
                        navController.navigate("device_picker")
                    }

                } else {

                    LaunchedEffect(Unit) {
                        vm.connect()
                    }

                    SearchScreen(
                        viewModel = vm,
                        onVideoSelected = {
                            navController.navigate("player")
                        }
                    )
                }
            }

            composable("player") {

                val vm = clientViewModel

                if (vm == null) {

                    LaunchedEffect(Unit) {
                        navController.navigate("device_picker")
                    }

                } else {

                    PlayerScreen(
                        viewModel = vm,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
