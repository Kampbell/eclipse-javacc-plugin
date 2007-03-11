/* Generated By:JJTree&JavaCC: Do not edit this line. JavaCCParserConstants.java */
package sf.eclipse.javacc.parser;

public @SuppressWarnings("all")interface JavaCCParserConstants {

  int EOF = 0;
  int _OPTIONS = 1;
  int _LOOKAHEAD = 2;
  int _IGNORE_CASE = 3;
  int _PARSER_BEGIN = 4;
  int _PARSER_END = 5;
  int _JAVACODE = 6;
  int _TOKEN = 7;
  int _SPECIAL_TOKEN = 8;
  int _MORE = 9;
  int _SKIP = 10;
  int _TOKEN_MGR_DECLS = 11;
  int _EOF = 12;
  int SINGLE_LINE_COMMENT = 21;
  int FORMAL_COMMENT = 22;
  int MULTI_LINE_COMMENT = 23;
  int ABSTRACT = 25;
  int BOOLEAN = 26;
  int BREAK = 27;
  int BYTE = 28;
  int CASE = 29;
  int CATCH = 30;
  int CHAR = 31;
  int CLASS = 32;
  int CONST = 33;
  int CONTINUE = 34;
  int _DEFAULT = 35;
  int DO = 36;
  int DOUBLE = 37;
  int ELSE = 38;
  int EXTENDS = 39;
  int FALSE = 40;
  int FINAL = 41;
  int FINALLY = 42;
  int FLOAT = 43;
  int FOR = 44;
  int GOTO = 45;
  int IF = 46;
  int IMPLEMENTS = 47;
  int IMPORT = 48;
  int INSTANCEOF = 49;
  int INT = 50;
  int INTERFACE = 51;
  int LONG = 52;
  int NATIVE = 53;
  int NEW = 54;
  int NULL = 55;
  int PACKAGE = 56;
  int PRIVATE = 57;
  int PROTECTED = 58;
  int PUBLIC = 59;
  int RETURN = 60;
  int SHORT = 61;
  int STATIC = 62;
  int SUPER = 63;
  int SWITCH = 64;
  int SYNCHRONIZED = 65;
  int THIS = 66;
  int THROW = 67;
  int THROWS = 68;
  int TRANSIENT = 69;
  int TRUE = 70;
  int TRY = 71;
  int VOID = 72;
  int VOLATILE = 73;
  int WHILE = 74;
  int INTEGER_LITERAL = 75;
  int DECIMAL_LITERAL = 76;
  int HEX_LITERAL = 77;
  int OCTAL_LITERAL = 78;
  int FLOATING_POINT_LITERAL = 79;
  int EXPONENT = 80;
  int CHARACTER_LITERAL = 81;
  int STRING_LITERAL = 82;
  int LPAREN = 83;
  int RPAREN = 84;
  int LBRACE = 85;
  int RBRACE = 86;
  int LBRACKET = 87;
  int RBRACKET = 88;
  int SEMICOLON = 89;
  int COMMA = 90;
  int DOT = 91;
  int ASSIGN = 92;
  int GT = 93;
  int LT = 94;
  int BANG = 95;
  int TILDE = 96;
  int HOOK = 97;
  int COLON = 98;
  int EQ = 99;
  int LE = 100;
  int GE = 101;
  int NE = 102;
  int SC_OR = 103;
  int SC_AND = 104;
  int INCR = 105;
  int DECR = 106;
  int PLUS = 107;
  int MINUS = 108;
  int STAR = 109;
  int SLASH = 110;
  int BIT_AND = 111;
  int BIT_OR = 112;
  int XOR = 113;
  int REM = 114;
  int PLUSASSIGN = 115;
  int MINUSASSIGN = 116;
  int STARASSIGN = 117;
  int SLASHASSIGN = 118;
  int ANDASSIGN = 119;
  int ORASSIGN = 120;
  int XORASSIGN = 121;
  int REMASSIGN = 122;
  int IDENTIFIER = 132;
  int LETTER = 133;
  int PART_LETTER = 134;

  int DEFAULT = 0;
  int IN_SINGLE_LINE_COMMENT = 1;
  int IN_FORMAL_COMMENT = 2;
  int IN_MULTI_LINE_COMMENT = 3;

  String[] tokenImage = {
    "<EOF>",
    "\"options\"",
    "\"LOOKAHEAD\"",
    "\"IGNORE_CASE\"",
    "\"PARSER_BEGIN\"",
    "\"PARSER_END\"",
    "\"JAVACODE\"",
    "\"TOKEN\"",
    "\"SPECIAL_TOKEN\"",
    "\"MORE\"",
    "\"SKIP\"",
    "\"TOKEN_MGR_DECLS\"",
    "\"EOF\"",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\"//\"",
    "<token of kind 19>",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 24>",
    "\"abstract\"",
    "\"boolean\"",
    "\"break\"",
    "\"byte\"",
    "\"case\"",
    "\"catch\"",
    "\"char\"",
    "\"class\"",
    "\"const\"",
    "\"continue\"",
    "\"default\"",
    "\"do\"",
    "\"double\"",
    "\"else\"",
    "\"extends\"",
    "\"false\"",
    "\"final\"",
    "\"finally\"",
    "\"float\"",
    "\"for\"",
    "\"goto\"",
    "\"if\"",
    "\"implements\"",
    "\"import\"",
    "\"instanceof\"",
    "\"int\"",
    "\"interface\"",
    "\"long\"",
    "\"native\"",
    "\"new\"",
    "\"null\"",
    "\"package\"",
    "\"private\"",
    "\"protected\"",
    "\"public\"",
    "\"return\"",
    "\"short\"",
    "\"static\"",
    "\"super\"",
    "\"switch\"",
    "\"synchronized\"",
    "\"this\"",
    "\"throw\"",
    "\"throws\"",
    "\"transient\"",
    "\"true\"",
    "\"try\"",
    "\"void\"",
    "\"volatile\"",
    "\"while\"",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"=\"",
    "\">\"",
    "\"<\"",
    "\"!\"",
    "\"~\"",
    "\"?\"",
    "\":\"",
    "\"==\"",
    "\"<=\"",
    "\">=\"",
    "\"!=\"",
    "\"||\"",
    "\"&&\"",
    "\"++\"",
    "\"--\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"&\"",
    "\"|\"",
    "\"^\"",
    "\"%\"",
    "\"+=\"",
    "\"-=\"",
    "\"*=\"",
    "\"/=\"",
    "\"&=\"",
    "\"|=\"",
    "\"^=\"",
    "\"%=\"",
    "\"#\"",
    "\"strictfp\"",
    "\"enum\"",
    "\"...\"",
    "\"<<=\"",
    "\">>=\"",
    "\">>>=\"",
    "\"assert\"",
    "\"@\"",
    "<IDENTIFIER>",
    "<LETTER>",
    "<PART_LETTER>",
  };

}
