package com.booksearch.app.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.booksearch.app.R

@Composable
fun HomeScreen() {
    Surface(Modifier.fillMaxSize()) { Text(stringResource(R.string.app_name)) }
}
