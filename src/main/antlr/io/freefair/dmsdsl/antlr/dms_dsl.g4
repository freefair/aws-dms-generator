grammar dms_dsl;

@header {
package io.freefair.dmsdsl.antlr;
}

main
    : ( selectStatement | transformStatement | COMMENT | enumDeclaration )*
    ;

enumDeclaration
    : 'enum' identifier '{' (enumField)* '}'
    ;

enumField
    : identifier '=' number ';'
    ;

selectStatement
    : 'select' exclude? name (arg)*
    ;

transformStatement
    : 'transform' type=transformType subtype? name value* (arg)*
    ;

selectAction
    : 'select'
    ;

transformAction
    : 'transform'
    ;

transformType
    : 'lowercase'
    | 'uppercase'
    | 'prefix'
    | 'suffix'
    | 'rename'
    | 'column'
    | 'enum'
    ;

subtype
    : 'remove'
    | 'add'
    | 'replace'
    ;

exclude
    : 'exclude'
    ;

arg
    : argName '=' argValue
    ;

name:
    identifier ('.' identifier)*
    ;

identifier:
    IDENTIFIER
    ;

number:
    NUMERIC
    ;

argName
    : IDENTIFIER
    ;

argValue
    : IDENTIFIER
    ;

value
    : STRING
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;

IDENTIFIER
    : [a-zA-Z_%][a-zA-Z_0-9\-%]*
    ;

NUMERIC
    : [0-9]+
    ;

STRING
    : '"' ('\\' . | ~('\\' | '"'))* '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
