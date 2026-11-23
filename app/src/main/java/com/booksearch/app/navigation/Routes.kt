package com.booksearch.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object HomeRoute

@Serializable
data class DetailRoute(val workId: String)

@Serializable
data object SettingsRoute

@Serializable
data object DiagnosticsRoute
