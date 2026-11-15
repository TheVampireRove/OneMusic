package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BluetoothAudioHelper {
    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>("Bluetooth Audio")
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private var audioManager: AudioManager? = null
    private var isCallbackRegistered = false

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            updateBluetoothStatus()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            updateBluetoothStatus()
        }
    }

    fun init(context: Context) {
        try {
            if (audioManager == null) {
                audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isCallbackRegistered) {
                audioManager?.registerAudioDeviceCallback(audioDeviceCallback, null)
                isCallbackRegistered = true
            }

            updateBluetoothStatus()
        } catch (_: Exception) {}
    }

    fun updateBluetoothStatus() {
        try {
            val am = audioManager ?: return
            var btConnected = false
            var deviceName = "Bluetooth Audio"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = try {
                    am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                } catch (_: Exception) {
                    emptyArray<AudioDeviceInfo>()
                }
                for (device in devices) {
                    if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && device.type == AudioDeviceInfo.TYPE_BLE_HEADSET) ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
                    ) {
                        btConnected = true
                        val name = try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                device.productName?.toString() ?: "Bluetooth Audio"
                            } else "Bluetooth Audio"
                        } catch (_: SecurityException) {
                            "Bluetooth Audio"
                        } catch (_: Exception) {
                            "Bluetooth Audio"
                        }
                        if (name.isNotBlank()) {
                            deviceName = name
                        }
                        break
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                btConnected = try { am.isBluetoothA2dpOn || am.isBluetoothScoOn } catch (_: Exception) { false }
            }

            _isBluetoothConnected.value = btConnected
            _connectedDeviceName.value = if (btConnected) deviceName else null
        } catch (_: Exception) {}
    }

    fun openBluetoothSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
