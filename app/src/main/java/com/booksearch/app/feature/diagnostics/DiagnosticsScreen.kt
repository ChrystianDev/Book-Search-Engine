package com.booksearch.app.feature.diagnostics

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.booksearch.app.R

/**
 * Diagnóstico: read-only views of what the app is doing, kept apart from Settings (which only
 * holds preferences). Each tab has its own ViewModel; only the selected tab is composed, so its
 * live updates (e.g. the cache countdown) run only while it is visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(DiagnosticsTab.CACHE.ordinal) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diagnostics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                DiagnosticsTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = { Text(stringResource(tab.label)) },
                    )
                }
            }
            when (DiagnosticsTab.entries[selectedTab]) {
                DiagnosticsTab.CACHE -> CacheStatusPane()
                DiagnosticsTab.SEARCHES -> SearchLogPane()
            }
        }
    }
}

private enum class DiagnosticsTab(@StringRes val label: Int) {
    CACHE(R.string.diagnostics_tab_cache),
    SEARCHES(R.string.diagnostics_tab_searches),
}
