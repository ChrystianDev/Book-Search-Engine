package com.booksearch.data.remote

import kotlinx.serialization.json.Json

object NetworkJson {
    /** Lenient on purpose: the API adds fields over time and many fields are optional. */
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }
}
