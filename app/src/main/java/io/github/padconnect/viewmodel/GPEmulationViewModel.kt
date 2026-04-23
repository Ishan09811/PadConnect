/*
 * Copyright (C) 2026 Ishan
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed without any warranty. See the GNU General Public License for more details.
 */


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

    private val _isTransportConnected = MutableStateFlow(false)
    val isTransportConnected: StateFlow<Boolean> = _isTransportConnected

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
        searchReceiver()
    }

    fun searchReceiver() {
        viewModelScope.launch {
            val result = DiscoverySender.discoverReceiver()
            if (result != null && result.host != null && result.port != null) {
                transport = TransportManager(result.host, result.port, onLatencyStatsReceive)
                withContext(Dispatchers.Main) {
                    Log.i("IP & PORT:", "$result.host $result.port")
                    _isTransportConnected.value = true
                }
                transport!!.start()
            } else {
                searchReceiver()
            }
        }
    }

    override fun onCleared() {
        transport?.stop()
    }
}