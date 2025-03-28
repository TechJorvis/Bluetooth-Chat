package com.ayush.bluetoothchat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.ayush.bluetoothchat.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        macAddress = address
    )
}