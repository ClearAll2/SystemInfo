package com.lkonlesoft.displayinfo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.lkonlesoft.displayinfo.R

// Set of Material typography styles to start with
val gg_sf_font = FontFamily(
    Font(R.font.gg_sf_regular,  FontWeight.Normal),
    Font(R.font.gg_sf_medium, FontWeight.Medium),
    Font(R.font.gg_sf_bold, FontWeight.Bold),
    Font(R.font.gg_sf_black, FontWeight.Black),
    Font(R.font.gg_sf_semi_bold, FontWeight.SemiBold),
    Font(R.font.gg_sf_extra_bold, FontWeight.ExtraBold)
)

val defaultTypography = Typography()
val customTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = gg_sf_font),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = gg_sf_font),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = gg_sf_font),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = gg_sf_font),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = gg_sf_font),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = gg_sf_font),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = gg_sf_font),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = gg_sf_font),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = gg_sf_font),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = gg_sf_font),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = gg_sf_font),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = gg_sf_font),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = gg_sf_font),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = gg_sf_font),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = gg_sf_font)
)