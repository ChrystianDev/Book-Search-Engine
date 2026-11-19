package com.booksearch.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.booksearch.app.R

/** Nunito Sans (SIL OFL), bundled in res/font: geometric-humanist, close in spirit to Avenir. */
val NunitoSans = FontFamily(
    Font(R.font.nunito_sans_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_medium, FontWeight.Medium),
    Font(R.font.nunito_sans_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_sans_bold, FontWeight.Bold),
)

private val Baseline = Typography()

private fun TextStyle.nunito(weight: FontWeight) = copy(fontFamily = NunitoSans, fontWeight = weight)

/** Full Material 3 type scale on Nunito Sans. */
val BookSearchTypography = Typography(
    displayLarge = Baseline.displayLarge.nunito(FontWeight.SemiBold),
    displayMedium = Baseline.displayMedium.nunito(FontWeight.SemiBold),
    displaySmall = Baseline.displaySmall.nunito(FontWeight.SemiBold),
    headlineLarge = Baseline.headlineLarge.nunito(FontWeight.Bold),
    headlineMedium = Baseline.headlineMedium.nunito(FontWeight.Bold),
    headlineSmall = Baseline.headlineSmall.nunito(FontWeight.Bold),
    titleLarge = Baseline.titleLarge.nunito(FontWeight.Bold),
    titleMedium = Baseline.titleMedium.nunito(FontWeight.SemiBold),
    titleSmall = Baseline.titleSmall.nunito(FontWeight.SemiBold),
    bodyLarge = Baseline.bodyLarge.nunito(FontWeight.Normal),
    bodyMedium = Baseline.bodyMedium.nunito(FontWeight.Normal),
    bodySmall = Baseline.bodySmall.nunito(FontWeight.Normal),
    labelLarge = Baseline.labelLarge.nunito(FontWeight.SemiBold),
    labelMedium = Baseline.labelMedium.nunito(FontWeight.SemiBold),
    labelSmall = Baseline.labelSmall.nunito(FontWeight.Medium),
)
