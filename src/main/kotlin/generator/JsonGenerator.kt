package io.freefair.dmsdsl.generator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.freefair.dmsdsl.DmsObjectLocator
import io.freefair.dmsdsl.DmsRule

class JsonGenerator {
    fun generateJsonString(rules: List<DmsRule>): String {
        val mapper = jacksonObjectMapper()
        return mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(DmsJsonWrapper(generate(rules)))
    }

    fun generate(rules: List<DmsRule>): List<DmsJsonRule> {
        return rules.mapIndexed { i, it ->
            mapToJsonRule(i, it)
        }
    }

    private fun mapToJsonRule(i: Int, it: DmsRule): DmsJsonRule {
        return DmsJsonRule(
            "$i",
            it.ruleName,
            it.ruleType,
            if(it.ruleType == "selection") null else it.ruleTarget,
            mapToJsonLocator(it.objectLocator),
            it.ruleAction,
            it.value,
            it.oldValue,
            it.expression,
            it.dataType?.let {
                DmsJsonDataType(it.type, it.length, it.scale)
            }
        )
    }

    private fun mapToJsonLocator(objectLocator: DmsObjectLocator): DmsJsonObjectLocator {
        return DmsJsonObjectLocator(
            objectLocator.schemaName,
            objectLocator.tableName,
            objectLocator.columnName,
            objectLocator.dataType
        )
    }
}

data class DmsJsonWrapper(
    val rules: List<DmsJsonRule>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DmsJsonRule(
    @JsonProperty("rule-id")
    val ruleId: String,
    @JsonProperty("rule-name")
    val ruleName: String,
    @JsonProperty("rule-type")
    val ruleType: String,
    @JsonProperty("rule-target")
    val ruleTarget: String?,
    @JsonProperty("object-locator")
    val objectLocator: DmsJsonObjectLocator,
    @JsonProperty("rule-action")
    val ruleAction: String,
    @JsonProperty("value")
    val value: String?,
    @JsonProperty("old-value")
    val oldValue: String?,
    @JsonProperty("expression")
    val expression: String?,
    @JsonProperty("data-type")
    val dataType: DmsJsonDataType?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DmsJsonDataType(
    @JsonProperty("type")
    val type: String?,
    @JsonProperty("length")
    val length: String?,
    @JsonProperty("scale")
    val scale: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DmsJsonObjectLocator(
    @JsonProperty("schema-name")
    val schemaName: String,
    @JsonProperty("table-name")
    val tableName: String?,
    @JsonProperty("column-name")
    val columnName: String?,
    @JsonProperty("data-type")
    val dataType: String?
)