package io.freefair.dmsdsl

data class DmsRule(
    val ruleName: String,
    val ruleType: String,
    val objectLocator: DmsObjectLocator,
    val ruleAction: String,
    val ruleTarget: String? = null,
    val value: String? = null,
    val oldValue: String? = null,
    val expression: String? = null,
    val dataType: DmsDataType? = null
)

data class DmsDataType(
    val type: String,
    val length: String? = null,
    val scale: String? = null
)

data class DmsObjectLocator(
    val schemaName: String,
    val tableName: String? = null,
    val columnName: String? = null,
    val dataType: String? = null
) {
    var fullName: String
        private set

    var target: String
        private set

    init {
        fullName = schemaName
        if(tableName != null) fullName += ".$tableName"
        if(columnName != null) fullName += ".$columnName"
        if(dataType != null) fullName += "(data-type=$dataType)"

        target = "schema"
        if(tableName != null) target = "table"
        if(columnName != null) target = "column"
    }
}
