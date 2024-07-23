package io.freefair.dmsdsl

import io.freefair.dmsdsl.antlr.DslVisitor
import io.freefair.dmsdsl.antlr.dms_dslLexer
import io.freefair.dmsdsl.antlr.dms_dslParser
import io.freefair.dmsdsl.generator.JsonGenerator
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>) {
    val parser = ArgParser("aws-dms-dsl")
    val input by parser.argument(ArgType.String, "input", "Input file to process")
    val output by parser.argument(ArgType.String, "output",  "Output file to write json to")

    parser.parse(args)

    val fileInputStream = FileInputStream(input)
    val antlrInputStream = ANTLRInputStream(fileInputStream)
    val lexer = dms_dslLexer(antlrInputStream)
    val commonTokenStream = CommonTokenStream(lexer)
    val dslParser = dms_dslParser(commonTokenStream)
    val ast = dslParser.main()
    val rules = DslVisitor().visit(ast)
    val json = JsonGenerator().generateJsonString(rules)

    File(output).writeText(json)
}
