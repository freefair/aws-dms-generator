package antlr

import io.freefair.dmsdsl.antlr.DslVisitor
import io.freefair.dmsdsl.antlr.dms_dslLexer
import io.freefair.dmsdsl.antlr.dms_dslParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class DslVisitorTests {
    val cut = DslVisitor()

    @ParameterizedTest
    @MethodSource("simpleSelectTestSource")
    fun simpleSelectTest(selection: String, exclude: Boolean, expectedSchema: String, expectedTable: String?, expectedColumn: String?, expectedDataType: String?) {
        // Arrange
        val select = "select ${if(exclude) "exclude" else ""} $selection".toAst()

        // Act
        val result = cut.visit(select)

        // Assert
        assert(result.size == 1)
        assert(result.first().ruleType == "selection")
        assert(result.first().ruleAction == if(exclude) "exclude" else "include")
        assert(result.first().objectLocator.schemaName == expectedSchema)
        assert(result.first().objectLocator.tableName == expectedTable)
        assert(result.first().objectLocator.columnName == expectedColumn)
        assert(result.first().objectLocator.dataType == expectedDataType)
    }

    @ParameterizedTest
    @MethodSource("simpleTransformTestSource")
    fun simpleTransformTest(expression: String, expectedAction: String?, expectedSchema: String, expectedTable: String?, expectedColumn: String?, expectedTarget: String?, expectedValue: String?, expectedOldValue: String?) {
        // Arrange
        val select = "transform $expression".toAst()

        // Act
        val result = cut.visit(select)

        // Assert
        assert(result.size == 1)
        assert(result.first().ruleType == "transformation")
        assert(result.first().ruleAction == expectedAction)
        assert(result.first().objectLocator.schemaName == expectedSchema)
        assert(result.first().objectLocator.tableName == expectedTable)
        assert(result.first().objectLocator.columnName == expectedColumn)
        assert(result.first().objectLocator.target == expectedTarget)
        assert(result.first().value == expectedValue)
        assert(result.first().oldValue == expectedOldValue)
    }

    @Test
    fun simpleEnumTest() {
        val str = ("enum test-enum { test-field-1=1; test-field-2=2; }\n" +
                "transform enum schema1.table1.column1 \"test-enum\"").toAst()

        val result = cut.visit(str)

        assert(cut.enums.size == 1)
        assert(cut.enums.first().name == "test-enum")
        assert(cut.enums.first().fields.size == 2)
        assert(cut.enums.first().fields.first().name == "test-field-1")
        assert(cut.enums.first().fields.first().value == "1")

        assert(result.size == 1)
        assert(result.first().expression == "CASE {{column-name}}  WHEN 1 THEN 'test-field-1'  WHEN 2 THEN 'test-field-2'  END")
    }

    companion object {
        @JvmStatic
        fun simpleSelectTestSource(): Stream<Arguments> = Stream.of(
            Arguments.of("%", false, "%", null, null, null),
            Arguments.of("schema1", false, "schema1", null, null, null),
            Arguments.of("schema1.%", false, "schema1", "%", null, null),
            Arguments.of("schema1.table1", false, "schema1", "table1", null, null),
            Arguments.of("schema1.table1.%", false, "schema1", "table1", "%", null),
            Arguments.of("schema1.table1.column1", false, "schema1", "table1", "column1", null),
            Arguments.of("schema1.%.column1", false, "schema1", "%", "column1", null),
            Arguments.of("schema1.table%.column1", false, "schema1", "table%", "column1", null),
            Arguments.of("schema1.%", true, "schema1", "%", null, null),
            Arguments.of("schema1.% data-type=int8", true, "schema1", "%", null, "int8"),
            Arguments.of("schema1.% data-byte=int8", true, "schema1", "%", null, null),
        )

        @JvmStatic
        fun simpleTransformTestSource(): Stream<Arguments> = Stream.of(
            Arguments.of("lowercase schema1", "convert-lowercase", "schema1", null, null, "schema", null, null),
            Arguments.of("lowercase schema1.table1", "convert-lowercase", "schema1", "table1", null, "table", null, null),
            Arguments.of("lowercase schema1.table1.column1", "convert-lowercase", "schema1", "table1", "column1", "column", null, null),
            Arguments.of("prefix remove % \"pref_\"", "remove-prefix", "%", null, null, "schema", "pref_", null),
            Arguments.of("prefix add % \"pref_old\" \"pref_\"", "add-prefix", "%", null, null, "schema", "pref_", "pref_old"), // this is nonsence, just for testing the second value option
            Arguments.of("rename % \"pref_{{column-name}}\"", "rename", "%", null, null, "schema", "pref_{{column-name}}", null)
        )
    }
}

fun String.toAst(): dms_dslParser.MainContext? {
    return dms_dslParser(CommonTokenStream(dms_dslLexer(ANTLRInputStream(this)))).main()
}