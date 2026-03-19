package org.lazy.wanandroid.feature.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith

/**
 * Defines a custom enter transition for list items.
 *
 * This transition combines a vertical slide-in from the middle of the container
 * with a fade-in effect. It uses a spring animation with low bounciness and
 * low stiffness for a smooth, natural feel. The exit transition is a simple fade-out.
 *
 * @return A [ContentTransform] representing the combined enter and exit transitions.
 */
fun listEnterTransition() = ( slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight / 2 },
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
    )
) + fadeIn(animationSpec = tween())).togetherWith(fadeOut(animationSpec = tween(90)))

/**
 * Defines a default content transition typically used for screen or container changes.
 *
 * The enter transition combines a fade-in and a slight scale-in (from 92% size) with a
 * brief delay to allow for smooth layering. The exit transition is a quick fade-out.
 *
 * @return A [ContentTransform] representing the combined enter and exit transitions.
 */
fun defaultTransition() = (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
    .togetherWith(fadeOut(animationSpec = tween(90)))
