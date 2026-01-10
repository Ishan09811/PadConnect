
package io.github.padconnect.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.serialization.json.Json
import java.util.UUID

class BleTransport(
    private val context: Context
) : GamepadTransport {

    val SERVICE_UUID: UUID =
        UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")

    val INPUT_UUID: UUID =
        UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val adapter = bluetoothManager.adapter

    private var gattServer: BluetoothGattServer? = null
    private var inputCharacteristic: BluetoothGattCharacteristic? = null
    private val connectedDevices = mutableSetOf<BluetoothDevice>()

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(
            device: BluetoothDevice,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(device)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            gattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                characteristic.value
            )
        }
    }


    override fun isAvailable(): Boolean =
        adapter?.isEnabled == true && gattServer != null

    override fun send(event: GamepadEvent) {
        if (connectedDevices.isEmpty()) return

        val payload = Json.encodeToString(event).toByteArray()
        inputCharacteristic?.value = payload

        connectedDevices.forEach { device ->
            gattServer?.notifyCharacteristicChanged(
                device,
                inputCharacteristic,
                false
            )
        }
    }

    fun start() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        val service = BluetoothGattService(
            SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        inputCharacteristic = BluetoothGattCharacteristic(
            INPUT_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val cccd = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or
                    BluetoothGattDescriptor.PERMISSION_WRITE
        )

        inputCharacteristic?.addDescriptor(cccd)
        service.addCharacteristic(inputCharacteristic)
        gattServer?.addService(service)
    }
}


