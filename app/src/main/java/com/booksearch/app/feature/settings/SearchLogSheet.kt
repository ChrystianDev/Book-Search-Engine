package com.booksearch.app.feature.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.theme.spacing
import com.booksearch.app.ui.theme.statusColors
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.model.SearchLogEntry
import com.booksearch.domain.model.SearchOutcome
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * "Peticiones / Búsquedas → Info estado": the session's searches with their outcome.
 * Each row expands (like a classic ExpandableListView group) to show the details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLogSheet(
    onDismiss: () -> Unit,
    viewModel: SearchLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SearchLogContent(uiState, onClear = viewModel::onClear)
    }
}

@Composable
private fun SearchLogContent(uiState: SearchLogUiState, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.search_log_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            val hasEntries = uiState is SearchLogUiState.Content && uiState.entries.isNotEmpty()
            TextButton(onClick = onClear, enabled = hasEntries) {
                Text(stringResource(R.string.search_log_clear))
            }
        }
        Text(
            text = stringResource(R.string.search_log_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
        )
        if (uiState is SearchLogUiState.Content && uiState.showFailureHint) {
            Text(
                text = stringResource(R.string.search_log_failure_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            )
        }
        Box(Modifier.padding(top = MaterialTheme.spacing.md)) {
            when (uiState) {
                SearchLogUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is SearchLogUiState.Content -> if (uiState.entries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_log_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.lg),
                    )
                } else {
                    LazyColumn {
                        items(uiState.entries, key = { it.id }) { entry ->
                            SearchLogRow(entry)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

/** Collapsed: status, query and time. Tapping it expands the details below. */
@Composable
private fun SearchLogRow(entry: SearchLogEntry) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
    val status = entry.outcome.status()
    val stateLabel = stringResource(if (expanded) R.string.search_log_expanded else R.string.search_log_collapsed)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(
                    onClickLabel = stringResource(
                        if (expanded) R.string.search_log_collapse else R.string.search_log_expand,
                    ),
                ) { expanded = !expanded }
                .semantics(mergeDescendants = true) { stateDescription = stateLabel }
                .padding(vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            // Fixed-width column so every query starts at the same x, whatever the label length.
            Box(Modifier.width(STATUS_COLUMN_WIDTH)) { StatusPill(status) }
            Text(
                text = entry.query,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = remember(entry.startedAtMillis) { TIME_FORMAT.format(Instant.ofEpochMilli(entry.startedAtMillis)) },
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(arrowRotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            SearchLogDetails(entry.outcome, status)
        }
    }
}

@Composable
private fun SearchLogDetails(outcome: SearchOutcome, status: Status) {
    Column(
        modifier = Modifier.padding(start = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        val noValue = stringResource(R.string.search_log_no_value)
        when (outcome) {
            is SearchOutcome.Success -> {
                DetailLine(R.string.search_log_field_status, stringResource(status.label), status.color())
                DetailLine(R.string.search_log_field_books, outcome.bookCount.toString())
                DetailLine(R.string.search_log_field_duration, stringResource(R.string.search_log_millis, outcome.durationMillis))
                DetailLine(
                    R.string.search_log_field_source,
                    stringResource(if (outcome.fromCache) R.string.search_log_source_cache else R.string.search_log_source_network),
                )
            }

            is SearchOutcome.Cancelled -> {
                DetailLine(R.string.search_log_field_status, stringResource(status.label), status.color())
                DetailLine(R.string.search_log_field_cancelled_after, stringResource(R.string.search_log_millis, outcome.afterMillis))
                DetailLine(R.string.search_log_field_books, noValue)
            }

            is SearchOutcome.Failed -> {
                DetailLine(
                    R.string.search_log_field_status,
                    "${stringResource(status.label)} · ${outcome.error.label()}",
                    status.color(),
                )
                DetailLine(R.string.search_log_field_duration, stringResource(R.string.search_log_millis, outcome.durationMillis))
                DetailLine(R.string.search_log_field_books, noValue)
            }
        }
    }
}

@Composable
private fun DetailLine(@StringRes label: Int, value: String, valueColor: Color = Color.Unspecified) {
    Row {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = valueColor,
        )
    }
}

/** Coloured dot + label: the colour is never the only cue. */
@Composable
private fun StatusPill(status: Status) {
    val color = status.color()
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = stringResource(status.label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private enum class Status(@StringRes val label: Int) {
    SUCCESS(R.string.search_log_status_success),
    CANCELLED(R.string.search_log_status_cancelled),
    FAILED(R.string.search_log_status_failed),
}

private fun SearchOutcome.status(): Status = when (this) {
    is SearchOutcome.Success -> Status.SUCCESS
    is SearchOutcome.Cancelled -> Status.CANCELLED
    is SearchOutcome.Failed -> Status.FAILED
}

@Composable
private fun Status.color(): Color = when (this) {
    Status.SUCCESS -> MaterialTheme.statusColors.success
    Status.CANCELLED -> MaterialTheme.statusColors.cancelled
    Status.FAILED -> MaterialTheme.statusColors.failed
}

@Composable
private fun DomainError.label(): String = when (this) {
    is DomainError.Server -> stringResource(R.string.search_log_error_http, httpCode)
    DomainError.NoConnection -> stringResource(R.string.search_log_error_no_connection)
    DomainError.Timeout -> stringResource(R.string.search_log_error_timeout)
    DomainError.Unknown -> stringResource(R.string.search_log_error_unknown)
}

private val STATUS_COLUMN_WIDTH = 108.dp

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
