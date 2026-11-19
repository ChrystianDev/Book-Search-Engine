package com.booksearch.app.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.theme.colorSchemeFor
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onPaletteSelected = viewModel::onColorPaletteSelected,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onDemoLatencyChanged = viewModel::onDemoLatencyChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onPaletteSelected: (ColorPalette) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDemoLatencyChanged: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.lg)
                .padding(bottom = MaterialTheme.spacing.xxl),
        ) {
            SectionHeader(R.string.settings_palette)
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                ColorPalette.entries.forEach { palette ->
                    PaletteOption(
                        palette = palette,
                        selected = palette == uiState.colorPalette,
                        onClick = { onPaletteSelected(palette) },
                    )
                }
            }

            SectionHeader(R.string.settings_theme)
            ThemeModeSelector(selected = uiState.themeMode, onSelected = onThemeModeSelected)

            if (uiState.showDemoLatency) {
                SectionHeader(R.string.settings_developer)
                DemoLatencyOption(enabled = uiState.demoLatencyEnabled, onChanged = onDemoLatencyChanged)
            }
        }
    }
}

@Composable
private fun SectionHeader(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = MaterialTheme.spacing.xl, bottom = MaterialTheme.spacing.md)
            .semantics { heading() },
    )
}

@Composable
private fun PaletteOption(palette: ColorPalette, selected: Boolean, onClick: () -> Unit) {
    // Preview each palette in the brightness currently in use by the app.
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val preview = colorSchemeFor(palette, darkTheme)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                Swatch(preview.primary)
                Swatch(preview.secondary)
                Swatch(preview.surface)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(palette.nameRes()), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(palette.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .padding(1.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun ThemeModeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    val options = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(mode.labelRes()))
            }
        }
    }
}

@Composable
private fun DemoLatencyOption(enabled: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .toggleable(value = enabled, onValueChange = onChanged, role = Role.Switch)
            .padding(vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_demo_latency), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.settings_demo_latency_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = enabled, onCheckedChange = null)
    }
}

private fun ColorPalette.nameRes(): Int = when (this) {
    ColorPalette.LIBRARY -> R.string.palette_library
    ColorPalette.FOREST -> R.string.palette_forest
    ColorPalette.GRAPHITE -> R.string.palette_graphite
}

private fun ColorPalette.descriptionRes(): Int = when (this) {
    ColorPalette.LIBRARY -> R.string.palette_library_description
    ColorPalette.FOREST -> R.string.palette_forest_description
    ColorPalette.GRAPHITE -> R.string.palette_graphite_description
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
    ThemeMode.SYSTEM -> R.string.theme_system
}
