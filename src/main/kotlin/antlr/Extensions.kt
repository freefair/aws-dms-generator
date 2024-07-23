package io.freefair.dmsdsl.antlr

import io.freefair.dmsdsl.DmsObjectLocator

fun dms_dslParser.NameContext.toObjectLocator(arg: MutableList<dms_dslParser.ArgContext>): DmsObjectLocator {
    val schemaName: String = identifier().first().text
    var tableName: String? = null
    var columnName: String? = null
    var dataType: String? = null

    if(identifier().size > 1) {
        tableName = identifier(1).text
    }

    if(identifier().size > 2) {
        columnName = identifier(2).text
    }

    if(arg.isNotEmpty()) {
        val dataTypeArg = arg.firstOrNull { it.argName().text == "data-type" }
        if(dataTypeArg != null)
            dataType = dataTypeArg.argValue().text
    }

    return DmsObjectLocator(schemaName, tableName, columnName, dataType)
}

fun String.unescape(): String {
    return this.trim('"').replace("\\", "")
}

fun String.toRuleAction(subAction: String? = null): String {
    return when(this) {
        "lowercase" -> "convert-lowercase"
        "uppercase" -> "convert-uppercase"
        "prefix" -> when(subAction) {
            "add" -> "add-prefix"
            "remove" -> "remove-prefix"
            "replace" -> "replace-prefix"
            else -> throw IllegalArgumentException("$subAction not known as action for prefix")
        }
        "suffix" -> when(subAction) {
            "add" -> "add-suffix"
            "remove" -> "remove-suffix"
            "replace" -> "replace-suffix"
            else -> throw IllegalArgumentException("$subAction not known as action for suffix")
        }
        "column" -> when(subAction) {
            "add" -> "add-column"
            "remove" -> "remove-column"
            else -> throw IllegalArgumentException("$subAction not known as action for column")
        }
        "rename" -> "rename"
        else -> throw IllegalArgumentException("$this not known as action")
    }
}