// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.AppKit.NSEvent
import platform.AppKit.NSEventMaskKeyDown

@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun getWindowSize(): WindowSize {
    val window = LocalWindowInfo.current
    val windowSize by remember(window) {
        derivedStateOf {
            WindowSize(
                width = window.containerSize.width,
                height = window.containerSize.height
            )
        }
    }
    return windowSize
}

actual fun platform(): Platform = Platform.MacOS

@Composable
actual fun getRoundedCorner(): Dp = 0.dp

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        val monitor = NSEvent.addLocalMonitorForEventsMatchingMask(NSEventMaskKeyDown) { event ->
            if (event?.keyCode == 53.toUShort()) {
                currentOnBack()
                null
            } else {
                event
            }
        }

        onDispose {
            if (monitor != null) {
                NSEvent.removeMonitor(monitor)
            }
        }
    }
}

@Composable
actual fun PredictiveBackHandler(
    enabled: Boolean,
    onBackStarted: ((BackEventCompat) -> Unit)?,
    onBackProgressed: ((BackEventCompat) -> Unit)?,
    onBackCancelled: (() -> Unit)?,
    onBack: () -> Unit
) {
    // macOS doesn't support predictive back gesture, fallback to simple BackHandler
    BackHandler(enabled = enabled, onBack = onBack)
}
