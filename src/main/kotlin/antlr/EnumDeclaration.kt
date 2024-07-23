package io.freefair.dmsdsl.antlr

data class EnumDeclaration(
    val name: String,
    val fields: List<EnumField>
)

data class EnumField(
    val name: String,
    val value: String
)