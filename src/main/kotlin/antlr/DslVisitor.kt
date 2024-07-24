package io.freefair.dmsdsl.antlr

import io.freefair.dmsdsl.DmsDataType
import io.freefair.dmsdsl.DmsObjectLocator
import io.freefair.dmsdsl.DmsRule

class DslVisitor : dms_dslBaseVisitor<List<DmsRule>>() {
    private val enumDeclarations = mutableListOf<EnumDeclaration>()

    val enums:List<EnumDeclaration> = enumDeclarations

    override fun visitMain(ctx: dms_dslParser.MainContext): List<DmsRule> {
        return ctx.children.map { visit(it) }.flatten()
    }

    override fun visitEnumDeclaration(ctx: dms_dslParser.EnumDeclarationContext): List<DmsRule> {
        val name = ctx.identifier().text
        val fields = ctx.enumField().map { mapField(it) }
        enumDeclarations += EnumDeclaration(name, fields)
        return emptyList()
    }

    private fun mapField(ctx: dms_dslParser.EnumFieldContext): EnumField {
        val name = ctx.identifier().text
        val value = ctx.number().text
        return EnumField(name, value)
    }

    override fun visitSelectStatement(ctx: dms_dslParser.SelectStatementContext): List<DmsRule> {
        val exclude = ctx.exclude() != null
        val objectLocator = ctx.name().toObjectLocator(ctx.arg())

        return listOf(DmsRule(
            "select ${objectLocator.fullName}",
                "selection",
            objectLocator,
            if(exclude) "exclude" else "include"
        ))
    }

    override fun visitTransformStatement(ctx: dms_dslParser.TransformStatementContext): List<DmsRule> {
        val type = ctx.transformType().text
        val objectLocator = ctx.name().toObjectLocator(ctx.arg())

        var value: String? = null
        var oldValue: String? = null

        if(ctx.value().size > 1) {
            value = ctx.value(1).text.unescape()
            oldValue = ctx.value(0).text.unescape()
        } else if(ctx.value().size == 1) {
            value = ctx.value().first().text.unescape()
        }

        if(type == "enum") {
            return createEnumRule(objectLocator, value, oldValue)
        }

        val subtype = ctx.subtype()?.text

        return listOf(DmsRule(
            "$type ${objectLocator.fullName}",
            "transformation",
            objectLocator,
            type.toRuleAction(subtype),
            objectLocator.target,
            value,
            oldValue
        ))
    }

    private fun createEnumRule(
        objectLocator: DmsObjectLocator,
        value: String?,
        oldValue: String?
    ): List<DmsRule> {
        val enum = enumDeclarations.firstOrNull { it.name == value }
        if(enum == null) throw IllegalArgumentException("$value is not found as enum")
        if(objectLocator.columnName?.contains("%") != false) throw IllegalArgumentException("Column name is required to have no wildcard for enum mapping")
        var expression = "CASE \$${objectLocator.columnName} "
        enum.fields.forEach { expression += " WHEN ${it.value} THEN '${it.name}' " }
        expression += " END"

        val dmsDataType = DmsDataType("string", enum.fields.maxOf { it.name.length }.toString())

        val creationRule = DmsRule(
            "enum ${objectLocator.fullName} $value",
            "transformation",
            DmsObjectLocator(objectLocator.schemaName, objectLocator.tableName),
            "add-column",
            "column",
            oldValue,
            null,
            expression,
            dmsDataType
        )

        val deleteRule = DmsRule(
            "delete ${objectLocator.fullName}",
            "transformation",
            objectLocator,
            "remove-column",
            "column",
            null,
            null,
            null,
            null
        )

        return listOf(creationRule, deleteRule)
    }
}
