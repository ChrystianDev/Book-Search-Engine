package com.booksearch.data.remote.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Open Library text fields (e.g. `description`) come in two shapes:
 * - a plain string: `"description": "..."`
 * - a typed object: `"description": { "type": "/type/text", "value": "..." }`
 *
 * Both are decoded to a plain [String].
 */
object TextValueSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OpenLibraryTextValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("TextValueSerializer only supports JSON")

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content
            is JsonObject -> element["value"]?.jsonPrimitive?.content
                ?: throw SerializationException("Text object without 'value': $element")
            else -> throw SerializationException("Unexpected text value: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}
