package io.github.padconnect.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.util.Log
import io.github.padconnect.utils.GamepadKey
import java.util.concurrent.Executors

private val HID_REPORT_MAP = byteArrayOf(
    0x05, 0x01,        // Usage Page (Generic Desktop)
    0x09, 0x05,        // Usage (Gamepad)
    0xA1.toByte(), 0x01,  // Collection (Application)

    0x85.toByte(), 0x01,  // Report ID 1

    // 8 Buttons (1 byte)
    0x05, 0x09,
    0x19, 0x01,
    0x29, 0x08,
    0x15, 0x00,
    0x25, 0x01,
    0x75, 0x01,
    0x95.toByte(), 0x08,
    0x81.toByte(), 0x02,

    // Axes (LX, LY, RX, RY)
    0x05, 0x01,
    0x15, 0x81.toByte(),  // Logical min -127
    0x25, 0x7F,           // Logical max 127
    0x75, 0x08,
    0x95.toByte(), 0x04,
    0x09, 0x30,           // X
    0x09, 0x31,           // Y
    0x09, 0x33,           // RX
    0x09, 0x34,           // RY
    0x81.toByte(), 0x02,

    0xC0.toByte()
)

class BleTransport(
    context: Context
) : GamepadTransport {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val adapter = bluetoothManager.adapter
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    private var appRegistered: Boolean = false

    private var buttons = 0
    private var lx = 0
    private var ly = 0
    private var rx = 0
    private var ry = 0

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }

    init {
        adapter.getProfileProxy(
            context,
            profileListener,
            BluetoothProfile.HID_DEVICE
        )
    }

    override fun isAvailable(): Boolean =
        adapter?.isEnabled == true && hidDevice != null && appRegistered


    @SuppressLint("MissingPermission")
    private fun registerApp() {
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "PadConnect",
            "PadConnect Virtual Gamepad",
            "Ishan09811",
            0,
            HID_REPORT_MAP
        )

        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED,
            800,
            9,
            0,
            11250,
            11250
        )

        try {
            hidDevice?.registerApp(
                sdp,
                null,
                qos,
                Executors.newSingleThreadExecutor(),
                hidCallback
            )
        } catch (_: SecurityException) {}
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(
            pluggedDevice: BluetoothDevice?,
            registered: Boolean
        ) {
            appRegistered = registered
            Log.d("HID", "Registered: $registered")
        }

        override fun onConnectionStateChanged(
            device: BluetoothDevice,
            state: Int
        ) {
            connectedDevice = if (state == BluetoothProfile.STATE_CONNECTED) {
                device
            } else {
                null
            }
        }
    }

    override fun setButton(mask: Int, down: Boolean) {
        buttons = if (down)
            buttons or (1 shl mapButton(mask))
        else buttons and (1 shl mapButton(mask)).inv()
        sendReport()
    }

    override fun setLeftAxis(x: Float, y: Float) {
        lx = (x * 127f).toInt().coerceIn(-127, 127)
        ly = (y * 127f).toInt().coerceIn(-127, 127)
        sendReport()
    }

    override fun setRightAxis(x: Float, y: Float) {
        // TODO
    }

    private fun mapButton(key: Int?): Int {
        return when (key) {
            GamepadKey.A.id -> 0
            GamepadKey.B.id -> 1
            GamepadKey.X.id -> 2
            GamepadKey.Y.id -> 3
            GamepadKey.LB.id -> 4
            GamepadKey.RB.id -> 5
            GamepadKey.SELECT.id -> 6
            GamepadKey.START.id -> 7
            else -> 0
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendReport() {

        val device = connectedDevice ?: return

        val report = byteArrayOf(
            buttons.toByte(),
            lx.toByte(),
            ly.toByte(),
            rx.toByte(),
            ry.toByte()
        )

        val success = hidDevice?.sendReport(device, 1, report)
        Log.d("HID", "Report sent: $success  buttons=$buttons lx=$lx ly=$ly")
    }
}
