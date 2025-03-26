package com.ayush.bluetoothchat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.ayush.bluetoothchat.domain.chat.BluetoothController
import com.ayush.bluetoothchat.domain.chat.BluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    private val TAG = "AndroidBluetoothControl"

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        Log.i(TAG, "foundDeviceReceiver: device = $device")

        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    init {
        updatePairedDevices()
    }

    override fun startDiscovery() {
        val methodTag = "startDiscovery"

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Log.i(TAG, "$methodTag: Don't have BLUETOOTH_SCAN permission")
            return
        }

        Log.i(TAG, "$methodTag: starting discovery...")

        context.registerReceiver(foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        val methodTag = "stopDiscovery"

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Log.i(TAG, "$methodTag: Don't have BLUETOOTH_SCAN permission")
            return
        }

        Log.i(TAG, "$methodTag: stopping discovery...")

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun updatePairedDevices() {
        val methodTag = "updatePairedDevices"

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.i(TAG, "$methodTag: Don't have BLUETOOTH_SCAN permission")
            return
        }

        Log.i(TAG, "$methodTag: updating paired devices...")

        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}