
package io.github.padconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.padconnect.transport.TransportManager
import io.github.padconnect.utils.DiscoverySender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GPEmulationViewModel : ViewModel() {
    private val _lastLatency = MutableStateFlow<Double?>(null)
    val lastLatency: StateFlow<Double?> = _lastLatency

    private var lastUiUpdate = 0L
    private val uiIntervalNs = 1000_000_000L // 1000ms = 1Hz

    var transport: TransportManager? = null

    val onLatencyStatsReceive: (Double) -> Unit = { latency ->
        val now = System.nanoTime()
        if (now - lastUiUpdate > uiIntervalNs) {
            lastUiUpdate = now
            _lastLatency.value = latency
        }
    }

    init {
        viewModelScope.launch {
            val result = DiscoverySender.discoverReceiver()
            val host = result?.host ?: "192.168.1.5"
            val port = result?.port ?: 8082
            transport = TransportManager(host, port, onLatencyStatsReceive)
            withContext(Dispatchers.Main) {
                Log.i("IP & PORT:", "$host $port")
            }
            transport!!.start()
        }
    }

    override fun onCleared() {
        transport?.stop()
    }
}