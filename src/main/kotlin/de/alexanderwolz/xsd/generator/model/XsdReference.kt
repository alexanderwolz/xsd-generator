package de.alexanderwolz.xsd.generator.model

data class XsdReference(
    val type: String,
    val schemaLocation: String,
    val namespace: String?
)