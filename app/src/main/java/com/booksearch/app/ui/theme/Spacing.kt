package com.booksearch.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing tokens on a 4 dp grid. Use these instead of raw dp values in screens. */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
)

/** Cover sizes, kept as tokens so every row of books looks consistent. */
@Immutable
data class CoverSizes(
    val small: Dp = 56.dp,
    val medium: Dp = 112.dp,
    val large: Dp = 156.dp,
    val detail: Dp = 200.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalCoverSizes = staticCompositionLocalOf { CoverSizes() }

val MaterialTheme.spacing: Spacing
    @Composable @ReadOnlyComposable get() = LocalSpacing.current

val MaterialTheme.coverSizes: CoverSizes
    @Composable @ReadOnlyComposable get() = LocalCoverSizes.current
