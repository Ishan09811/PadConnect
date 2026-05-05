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
import io.github.padconnect.dialogs.AlertDialogQueue
import io.github.padconnect.dialogs.AppDialog
import io.github.padconnect.transport.TransportManager
import io.github.padconnect.utils.DiscoverySender
import io.github.padconnect.utils.DiscoverySender.buildClientFeatures
import io.github.padconnect.utils.Logger
import io.github.padconnect.utils.settings.GlobalConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GPEmulationViewModel : ViewModel() {
    private val _lastLatency = MutableStateFlow<Double?>(null)
    val lastLatency: StateFlow<Double?> = _lastLatency

    private val _isTransportConnected = MutableStateFlow(false)
    val isTransportConnected: StateFlow<Boolean> = _isTransportConnected

    private val _isReceiverActive = MutableStateFlow(false)
    val isReceiverActive: StateFlow<Boolean> = _isReceiverActive

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

    private var receiverJob: Job? = null

    private fun observeReceiverState() {
        receiverJob?.cancel()
        receiverJob = viewModelScope.launch {
            while (isActive) {
                val active = transport?.isReceiverActive() == true

                if (_isReceiverActive.value != active) {
                    _isReceiverActive.value = active
                }

                delay(500)
            }
        }
    }

    init {
        Logger.info("GPEmulationViewModel", "Initialized")
        searchReceiver()
        Logger.info("GPEmulationViewModel", "Searching Receiver...")
        observeReceiverState()
        observeFeatures()
    }

    fun searchReceiver() {
        viewModelScope.launch {
            val result = DiscoverySender.discoverReceiver(
                buildClientFeatures(
                    enableRumble = GlobalConfig.ENABLE_RUMBLE.boolean,
                    showLatency = GlobalConfig.SHOW_LATENCY.boolean
                )
            )

            if (result != null && result.host != null) {
                transport = TransportManager(result.host, result.port, onLatencyStatsReceive)
                Log.i("DISCOVERED:", "$result.host, $result.port, features:${result.features}, agreedVersion:${result.agreedVersion}")
                if (result.agreedVersion < DiscoverySender.MIN_SUPPORTED_VERSION) {
                    withContext(Dispatchers.Main) {
                        AlertDialogQueue.show(
                            AppDialog.Message(
                                title = "Receiver Update Required",
                                message = "This receiver version is not supported anymore.\n\nPlease update the receiver on your PC."
                            )
                        )
                    }
                    return@launch
                }
                _isTransportConnected.value = true
                transport!!.start()
                Logger.info("DiscoverySender", "Successfully connected")
            } else {
                searchReceiver()
            }
        }
    }

    private fun observeFeatures() {
        viewModelScope.launch {
            combine(
                GlobalConfig.enableRumbleFlow,
                GlobalConfig.showLatencyFlow
            ) { enableRumble, showLatency ->
                enableRumble to showLatency
            }
                .distinctUntilChanged()
                .collect { (enableRumble, showLatency) ->

                    val result = DiscoverySender.discoverReceiver(
                        buildClientFeatures(
                            enableRumble = enableRumble,
                            showLatency = showLatency
                        )
                    )

                    Logger.info("Config", "Features updated: ${result?.features}")
                }
        }
    }

    override fun onCleared() {
        transport?.stop()
    }
}