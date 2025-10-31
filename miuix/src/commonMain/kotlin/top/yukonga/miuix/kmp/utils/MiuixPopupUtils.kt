// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import top.yukonga.miuix.kmp.anim.AccelerateEasing
import top.yukonga.miuix.kmp.anim.DecelerateEasing
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A util class for show popup and dialog.
 */
class MiuixPopupUtils {

    @Stable
    class PopupState(
        val showState: MutableState<Boolean>,
        val zIndex: Float
    ) {
        var enterTransition by mutableStateOf<EnterTransition?>(null)
        var exitTransition by mutableStateOf<ExitTransition?>(null)
        var enableWindowDim by mutableStateOf(true)
        var dimEnterTransition by mutableStateOf<EnterTransition?>(null)
        var dimExitTransition by mutableStateOf<ExitTransition?>(null)
        var transformOrigin by mutableStateOf({ TransformOrigin.Center })
        var content by mutableStateOf<@Composable () -> Unit>({})
    }

    @Stable
    class DialogState(
        val showState: MutableState<Boolean>,
        val zIndex: Float
    ) {
        var enterTransition by mutableStateOf<EnterTransition?>(null)
        var exitTransition by mutableStateOf<ExitTransition?>(null)
        var enableWindowDim by mutableStateOf(true)
        var enableAutoLargeScreen by mutableStateOf(true)
        var dimEnterTransition by mutableStateOf<EnterTransition?>(null)
        var dimExitTransition by mutableStateOf<ExitTransition?>(null)
        var dimAlpha by mutableStateOf<MutableState<Float>?>(null)
        var content by mutableStateOf<@Composable () -> Unit>({})
    }

    companion object {
        private var nextZIndex = 1f

        private val DialogDimEnter: EnterTransition =
            fadeIn(animationSpec = tween(300, easing = DecelerateEasing(1.5f)))
        private val DialogDimExit: ExitTransition =
            fadeOut(animationSpec = tween(250, easing = DecelerateEasing(1.5f)))

        private val PopupDimEnter: EnterTransition =
            fadeIn(animationSpec = tween(150, easing = DecelerateEasing(1.5f)))
        private val PopupDimExit: ExitTransition =
            fadeOut(animationSpec = tween(150, easing = DecelerateEasing(1.5f)))

        @Composable
        private fun rememberDefaultDialogEnterTransition(largeScreen: Boolean): EnterTransition {
            return remember(largeScreen) {
                if (largeScreen) {
                    fadeIn(
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 900f)
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(0.73f, 900f)
                    )
                } else {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = spring(dampingRatio = 0.92f, stiffness = 400f)
                    )
                }
            }
        }

        @Composable
        private fun rememberDefaultDialogExitTransition(largeScreen: Boolean): ExitTransition {
            return remember(largeScreen) {
                if (largeScreen) {
                    fadeOut(
                        animationSpec = tween(200, easing = DecelerateEasing(1.5f))
                    ) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(200, easing = DecelerateEasing(1.5f))
                    )
                } else {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(200, easing = DecelerateEasing(1.5f))
                    )
                }
            }
        }

        @Composable
        private fun rememberDefaultPopupEnterTransition(transformOrigin: () -> TransformOrigin): EnterTransition {
            return remember(transformOrigin()) {
                fadeIn(
                    animationSpec = tween(durationMillis = 120, easing = DecelerateEasing(1.5f))
                ) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 500f,
                        visibilityThreshold = 0.001f
                    ),
                    transformOrigin = transformOrigin()
                )
            }
        }

        @Composable
        private fun rememberDefaultPopupExitTransition(transformOrigin: () -> TransformOrigin): ExitTransition {
            return remember(transformOrigin()) {
                fadeOut(
                    animationSpec = tween(durationMillis = 120, easing = AccelerateEasing(1.5f))
                ) + scaleOut(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = 500f,
                        visibilityThreshold = 0.001f
                    ),
                    transformOrigin = transformOrigin()
                )
            }
        }

        /**
         * Create a dialog layout.
         *
         * @param visible The show state controller for this specific dialog.
         * @param enterTransition Optional, custom enter animation for dialog content.
         * @param exitTransition Optional, custom exit animation for dialog content.
         * @param enableWindowDim Whether to dim the window behind the dialog.
         * @param enableAutoLargeScreen Whether to automatically detect large screen and adjust animations.
         * @param dimEnterTransition Optional, custom enter animation for dim layer.
         * @param dimExitTransition Optional, custom exit animation for dim layer.
         * @param dimAlpha Optional, a mutable state to dynamically control the dim layer alpha (0f-1f).
         *   When provided, the dim layer will use this alpha value instead of default animations.
         * @param content The [Composable] content of the dialog.
         */
        @Composable
        fun DialogLayout(
            visible: MutableState<Boolean>,
            enterTransition: EnterTransition? = null,
            exitTransition: ExitTransition? = null,
            enableWindowDim: Boolean = true,
            enableAutoLargeScreen: Boolean = true,
            dimEnterTransition: EnterTransition? = null,
            dimExitTransition: ExitTransition? = null,
            dimAlpha: MutableState<Float>? = null,
            content: (@Composable () -> Unit)? = null,
        ) {
            if (content == null) {
                if (visible.value) visible.value = false
                return
            }

            val dialogStates = LocalDialogStates.current

            val state = remember(visible) {
                DialogState(showState = visible, zIndex = nextZIndex++)
            }

            val latestEnter by rememberUpdatedState(enterTransition)
            val latestExit by rememberUpdatedState(exitTransition)
            val latestEnableDim by rememberUpdatedState(enableWindowDim)
            val latestEnableAutoLargeScreen by rememberUpdatedState(enableAutoLargeScreen)
            val latestDimEnter by rememberUpdatedState(dimEnterTransition)
            val latestDimExit by rememberUpdatedState(dimExitTransition)
            val latestDimAlpha by rememberUpdatedState(dimAlpha)
            val latestContent by rememberUpdatedState(content)

            SideEffect {
                state.enterTransition = latestEnter
                state.exitTransition = latestExit
                state.enableWindowDim = latestEnableDim
                state.enableAutoLargeScreen = latestEnableAutoLargeScreen
                state.dimEnterTransition = latestDimEnter
                state.dimExitTransition = latestDimExit
                state.dimAlpha = latestDimAlpha
                state.content = latestContent
            }

            DisposableEffect(state.showState) {
                if (dialogStates.none { it.showState === state.showState }) {
                    dialogStates.add(state)
                }
                onDispose {
                    if (state.showState.value) {
                        dialogStates.removeAll { it.showState === state.showState }
                    }
                }
            }
        }

        /**
         * Create a popup layout.
         *
         * @param visible The show state controller for this specific popup.
         * @param enterTransition Optional, custom enter animation for popup content.
         * @param exitTransition Optional, custom exit animation for popup content.
         * @param enableWindowDim Whether to dim the window behind the popup.
         * @param dimEnterTransition Optional, custom enter animation for dim layer.
         * @param dimExitTransition Optional, custom exit animation for dim layer.
         * @param transformOrigin The pivot point in terms of fraction of the overall size,
         *   used for scale transformations. By default it's [TransformOrigin.Center].
         * @param content The [Composable] content of the popup.
         */
        @Composable
        fun PopupLayout(
            visible: MutableState<Boolean>,
            enterTransition: EnterTransition? = null,
            exitTransition: ExitTransition? = null,
            enableWindowDim: Boolean = true,
            dimEnterTransition: EnterTransition? = null,
            dimExitTransition: ExitTransition? = null,
            transformOrigin: (() -> TransformOrigin) = { TransformOrigin.Center },
            content: (@Composable () -> Unit)? = null,
        ) {
            if (content == null) {
                if (visible.value) visible.value = false
                return
            }

            val popupStates = LocalPopupStates.current

            val state = remember(visible) {
                PopupState(showState = visible, zIndex = nextZIndex++)
            }

            val latestEnter by rememberUpdatedState(enterTransition)
            val latestExit by rememberUpdatedState(exitTransition)
            val latestEnableDim by rememberUpdatedState(enableWindowDim)
            val latestDimEnter by rememberUpdatedState(dimEnterTransition)
            val latestDimExit by rememberUpdatedState(dimExitTransition)
            val latestOrigin by rememberUpdatedState(transformOrigin)
            val latestContent by rememberUpdatedState(content)

            SideEffect {
                state.enterTransition = latestEnter
                state.exitTransition = latestExit
                state.enableWindowDim = latestEnableDim
                state.dimEnterTransition = latestDimEnter
                state.dimExitTransition = latestDimExit
                state.transformOrigin = latestOrigin
                state.content = latestContent
            }

            DisposableEffect(state.showState) {
                if (popupStates.none { it.showState === state.showState }) {
                    popupStates.add(state)
                }
                onDispose {
                    if (state.showState.value) {
                        popupStates.removeAll { it.showState === state.showState }
                    }
                }
            }
        }

        @Composable
        private fun DialogEntry(
            dialogState: DialogState,
            largeScreen: Boolean
        ) {
            val visibleState = remember { MutableTransitionState(false) }
            var pendingOpen by remember { mutableStateOf(false) }
            var lastTarget by remember { mutableStateOf(false) }
            LaunchedEffect(dialogState.showState.value) {
                val newTarget = dialogState.showState.value
                if (newTarget) {
                    if (!visibleState.isIdle && !lastTarget) {
                        pendingOpen = true
                    } else {
                        visibleState.targetState = true
                    }
                } else {
                    pendingOpen = false
                    visibleState.targetState = false
                }
                lastTarget = newTarget
            }
            LaunchedEffect(visibleState.isIdle, pendingOpen) {
                if (pendingOpen && visibleState.isIdle) {
                    pendingOpen = false
                    visibleState.targetState = true
                }
            }
            val dialogStates = LocalDialogStates.current

            val effectiveLargeScreen = largeScreen && dialogState.enableAutoLargeScreen

            if (dialogState.enableWindowDim) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    modifier = Modifier.zIndex(dialogState.zIndex - 0.001f),
                    enter = dialogState.dimEnterTransition ?: DialogDimEnter,
                    exit = dialogState.dimExitTransition ?: DialogDimExit
                ) {
                    val baseColor = MiuixTheme.colorScheme.windowDimming
                    val dimColor = dialogState.dimAlpha?.value?.let { alphaMultiplier ->
                        baseColor.copy(alpha = (baseColor.alpha * alphaMultiplier.coerceIn(0f, 1f)))
                    } ?: baseColor

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(dimColor)
                    )
                }
            }

            AnimatedVisibility(
                visibleState = visibleState,
                modifier = Modifier.zIndex(dialogState.zIndex),
                enter = dialogState.enterTransition ?: rememberDefaultDialogEnterTransition(effectiveLargeScreen),
                exit = dialogState.exitTransition ?: rememberDefaultDialogExitTransition(effectiveLargeScreen)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    dialogState.content()
                }
                DisposableEffect(dialogState.showState) {
                    onDispose {
                        if (!dialogState.showState.value) {
                            dialogStates.removeAll { it.showState === dialogState.showState }
                        }
                    }
                }
            }
        }

        @Composable
        private fun PopupEntry(
            popupState: PopupState,
        ) {
            val visibleState = remember { MutableTransitionState(false) }
            var pendingOpen by remember { mutableStateOf(false) }
            var lastTarget by remember { mutableStateOf(false) }
            LaunchedEffect(popupState.showState.value) {
                val newTarget = popupState.showState.value
                if (newTarget) {
                    if (!visibleState.isIdle && !lastTarget) {
                        pendingOpen = true
                    } else {
                        visibleState.targetState = true
                    }
                } else {
                    pendingOpen = false
                    visibleState.targetState = false
                }
                lastTarget = newTarget
            }
            LaunchedEffect(visibleState.isIdle, pendingOpen) {
                if (pendingOpen && visibleState.isIdle) {
                    pendingOpen = false
                    visibleState.targetState = true
                }
            }
            val popupStates = LocalPopupStates.current

            if (popupState.enableWindowDim) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    modifier = Modifier.zIndex(popupState.zIndex - 0.001f),
                    enter = popupState.dimEnterTransition ?: PopupDimEnter,
                    exit = popupState.dimExitTransition ?: PopupDimExit
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MiuixTheme.colorScheme.windowDimming)
                    )
                }
            }

            AnimatedVisibility(
                visibleState = visibleState,
                modifier = Modifier.zIndex(popupState.zIndex),
                enter = popupState.enterTransition ?: rememberDefaultPopupEnterTransition(popupState.transformOrigin),
                exit = popupState.exitTransition ?: rememberDefaultPopupExitTransition(popupState.transformOrigin)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    popupState.content()
                }
                DisposableEffect(popupState.showState) {
                    onDispose {
                        if (!popupState.showState.value) {
                            popupStates.removeAll { it.showState === popupState.showState }
                        }
                    }
                }
            }
        }

        /**
         * A host for show popup and dialog. Already added to the [Scaffold] by default.
         */
        @Composable
        fun MiuixPopupHost() {
            val density = LocalDensity.current
            val windowSize = rememberUpdatedState(getWindowSize()).value
            val windowWidth by remember(windowSize, density) {
                derivedStateOf { windowSize.width.dp / density.density }
            }
            val windowHeight by remember(windowSize, density) {
                derivedStateOf { windowSize.height.dp / density.density }
            }
            val largeScreen by remember(windowWidth, windowHeight) {
                derivedStateOf { (windowHeight >= 480.dp && windowWidth >= 840.dp) }
            }

            val dialogStates = LocalDialogStates.current
            val popupStates = LocalPopupStates.current

            for (state in dialogStates) {
                key(state.showState) {
                    DialogEntry(
                        dialogState = state,
                        largeScreen = largeScreen
                    )
                }
            }

            for (state in popupStates) {
                key(state.showState) {
                    PopupEntry(
                        popupState = state,
                    )
                }
            }

            LaunchedEffect(dialogStates.size, popupStates.size) {
                if (dialogStates.isEmpty() && popupStates.isEmpty()) {
                    nextZIndex = 1f
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    dialogStates.clear()
                    popupStates.clear()
                    nextZIndex = 1f
                }
            }
        }
    }
}

internal val LocalPopupStates = staticCompositionLocalOf { mutableStateListOf<MiuixPopupUtils.PopupState>() }
internal val LocalDialogStates = staticCompositionLocalOf { mutableStateListOf<MiuixPopupUtils.DialogState>() }
