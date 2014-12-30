package sf.eclipse.javacc.preferences;

import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Preference constants.
 * 
 * @author Bill Fenlason 2012
 */
public interface IPrefConstants {

  // BF  05/2012 : code moved from PreferencesInitializer and PreferencesPage
  // BF  06/2012 : rewritten, added new preference names as necessary

  // -------------------- indentation ------------------------------------------

  /** JavaCC no automatic indentation preference */
  public static final String P_NO_ADV_AUTO_INDENT                    = "JavaCCNoAdvAutoIndentPref";                                     //$NON-NLS-1$

  /** JavaCC indentation character preference */
  public static final String P_INDENT_CHAR                           = "JavaCCIndentCharPref";                                          //$NON-NLS-1$

  /** JavaCC number of indentation character preference */
  public static final String P_INDENT_CHAR_NB                        = "JavaCCIndentCharNbPref";                                        //$NON-NLS-1$

  // -------------------- comments ---------------------------------------------

  /** Line comment color preference */
  public static final String P_COMMENT_LINE                          = "CommentLineColorPref";                                          //$NON-NLS-1$

  /** Line comment font attribute preference */
  public static final String P_COMMENT_LINE_ATR                      = "CommentLineFontAtrPref";                                        //$NON-NLS-1$

  /** Block comment color preference */
  public static final String P_COMMENT_BLOCK                         = "CommentBlockColorPref";                                         //$NON-NLS-1$

  /** Block comment font attribute preference */
  public static final String P_COMMENT_BLOCK_ATR                     = "CommentBlockFontAtrPref";                                       //$NON-NLS-1$

  /** Javadoc comment color preference */
  public static final String P_COMMENT_JAVADOC                       = "CommentJavadocColorPref";                                       //$NON-NLS-1$

  /** Javadoc comment font attribute preference */
  public static final String P_COMMENT_JAVADOC_ATR                   = "CommentJavadocFontAtrPref";                                     //$NON-NLS-1$

  /** Comment background color preference */
  public static final String P_COMMENT_BACKGROUND                    = "CommentBackgroundColorPref";                                    //$NON-NLS-1$

  /** Spell checking preference */
  public static final String P_NO_SPELL_CHECKING                     = "NoCommentSpellCheckingPref";                                    //$NON-NLS-1$

  // -------------------- Java -------------------------------------------------

  /** Java block braces color preference */
  public static final String P_JAVA_BLOCK_BRACE                      = "JavaBlockBraceColorPref";                                       //$NON-NLS-1$

  /** Java block braces font attribute preference */
  public static final String P_JAVA_BLOCK_BRACE_ATR                  = "JavaBlockBraceFontAtrPref";                                     //$NON-NLS-1$

  /** The Java block brace alternate background flag */
  public static final String P_JAVA_BLOCK_BRACE_ALT_BG               = "JavaBlockBraceAlternateBackgroundPref";                         //$NON-NLS-1$

  /** Java keyword color preference */
  public static final String P_JAVA_KEYWORD                          = "JavaKeywordColorPref";                                          //$NON-NLS-1$

  /** Java keyword font attribute preference */
  public static final String P_JAVA_KEYWORD_ATR                      = "JavaKeywordFontAtrPref";                                        //$NON-NLS-1$

  /** Java identifier color preference */
  public static final String P_JAVA_IDENTIFIER                       = "JavaIdentifierColorPref";                                       //$NON-NLS-1$

  /** Java identifier font attribute preference */
  public static final String P_JAVA_IDENTIFIER_ATR                   = "JavaIdentifierFontAtrPref";                                     //$NON-NLS-1$

  /** Java String color preference */
  public static final String P_JAVA_STRING                           = "JavaStringColorPref";                                           //$NON-NLS-1$

  /** Java String font attribute preference */
  public static final String P_JAVA_STRING_ATR                       = "JavaStringFontAtrPref";                                         //$NON-NLS-1$

  /** Numeric color preference */
  public static final String P_JAVA_NUMERIC                          = "JavaNumericColorPref";                                          //$NON-NLS-1$

  /** Numeric font attribute preference */
  public static final String P_JAVA_NUMERIC_ATR                      = "JavaNumericFontAtrPref";                                        //$NON-NLS-1$

  /** Java punctuation color preference */
  public static final String P_JAVA_PUNCTUATION                      = "JavaPunctuationColorPref";                                      //$NON-NLS-1$

  /** Java punctuation font attribute preference */
  public static final String P_JAVA_PUNCTUATION_ATR                  = "JavaPunctuationFontAtrPref";                                    //$NON-NLS-1$

  /** Java default color preference */
  public static final String P_JAVA_DEFAULT_TEXT                     = "JavaDefaultTextColorPref";                                      //$NON-NLS-1$

  /** Java default font attribute preference */
  public static final String P_JAVA_DEFAULT_TEXT_ATR                 = "JavaDefaultTextFontAtrPref";                                    //$NON-NLS-1$

  /** Background color preference for Java code */
  public static final String P_JAVA_BACKGROUND                       = "JavaBackgroundColorPref";                                       //$NON-NLS-1$

  //-------------------- JavaCC ------------------------------------------------

  /** JavaCC keyword color preference */
  public static final String P_JAVACC_KEYWORD                        = "JavaCCKeywordColorPref";                                        //$NON-NLS-1$

  /** JavaCC keyword font attribute preference */
  public static final String P_JAVACC_KEYWORD_ATR                    = "JavaCCKeywordFontAtrPref";                                      //$NON-NLS-1$

  /** JavaCC expansion braces color preference */
  public static final String P_JAVACC_EXPANSION_BRACE                = "JavaCCExpansionBraceColorPref";                                 //$NON-NLS-1$

  /** JavaCC expansion braces font attribute preference */
  public static final String P_JAVACC_EXPANSION_BRACE_ATR            = "JavaCCExpansionBracesFontAtrPref";                              //$NON-NLS-1$

  /** JavaCC choices punctuation color preference */
  public static final String P_JAVACC_CHOICE_PUNCT                   = "JavaCCChoiceColorPref";                                         //$NON-NLS-1$

  /** JavaCC choices punctuation font attribute preference */
  public static final String P_JAVACC_CHOICE_PUNCT_ATR               = "JavaCCChoiceFontAtrPref";                                       //$NON-NLS-1$

  /** JavaCC string color preference */
  public static final String P_JAVACC_STRING                         = "JavaCCStringColorPref";                                         //$NON-NLS-1$

  /** JavaCC string font attribute preference */
  public static final String P_JAVACC_STRING_ATR                     = "JavaCCStringFontAtrPref";                                       //$NON-NLS-1$

  /** JavaCC numeric color preference */
  public static final String P_JAVACC_NUMERIC                        = "JavaCCNumericColorPref";                                        //$NON-NLS-1$

  /** JavaCC numeric font attribute preference */
  public static final String P_JAVACC_NUMERIC_ATR                    = "JavaCCNumericFontAtrPref";                                      //$NON-NLS-1$

  /** JavaCC other punctuation color preference */
  public static final String P_JAVACC_OTHER_PUNCT                    = "JavaCCOtherPunctColorPref";                                     //$NON-NLS-1$

  /** JavaCC other punctuation font attribute preference */
  public static final String P_JAVACC_OTHER_PUNCT_ATR                = "JavaCCOtherPunctFontAtrPref";                                   //$NON-NLS-1$

  /** JavaCC default color preference */
  public static final String P_JAVACC_DEFAULT_TEXT                   = "JavaCCDefaultTextColorPref";                                    //$NON-NLS-1$

  /** JavaCC default font attribute preference */
  public static final String P_JAVACC_DEFAULT_TEXT_ATR               = "JavaCCDefaultTextFontAtrPref";                                  //$NON-NLS-1$

  /** JavaCC background color preference */
  public static final String P_JAVACC_BACKGROUND                     = "JavaCCBackgroundColorPref";                                     //$NON-NLS-1$

  // -------------------- labels -----------------------------------------------

  /** Normal label identifier color preference */
  public static final String P_TOKEN_LABEL                           = "TokenLabelColorPref";                                           //$NON-NLS-1$

  /** Normal label identifier font attribute preference */
  public static final String P_TOKEN_LABEL_ATR                       = "TokenLabelFontAtrPref";                                         //$NON-NLS-1$

  /** Normal label identifier color preference */
  public static final String P_TOKEN_LABEL_PUNCT                     = "TokenLabelPunctColorPref";                                      //$NON-NLS-1$

  /** Normal label identifier font attribute preference */
  public static final String P_TOKEN_LABEL_PUNCT_ATR                 = "TokenLabelPunctFontAtrPref";                                    //$NON-NLS-1$

  /** Normal label identifier color preference */
  public static final String P_TOKEN_LABEL_DEF                       = "TokenLabelDefColorPref";                                        //$NON-NLS-1$

  /** Normal label identifier font attribute preference */
  public static final String P_TOKEN_LABEL_DEF_ATR                   = "TokenLabelDefFontAtrPref";                                      //$NON-NLS-1$

  /** Private label identifier color preference */
  public static final String P_TOKEN_LABEL_PRIVATE_DEF               = "TokenLabelPrivateDefColorPref";                                 //$NON-NLS-1$

  /** Private label identifier font attribute preference */
  public static final String P_TOKEN_LABEL_PRIVATE_DEF_ATR           = "TokenLabelPrivateDefFontAtrPref";                               //$NON-NLS-1$

  /** Private label identifier color preference */
  public static final String P_TOKEN_LABEL_PRIVATE_DEF_PUNCT         = "TokenLabelPrivateDefPunctColorPref";                            //$NON-NLS-1$

  /** Private label identifier font attribute preference */
  public static final String P_TOKEN_LABEL_PRIVATE_DEF_PUNCT_ATR     = "TokenLabelPrivateDefPunctFontAtrPref";                          //$NON-NLS-1$

  // -------------------- states -----------------------------------------------

  /** Lexical state list or lexical state identifier color preference */
  public static final String P_LEXICAL_STATE                         = "LexicalStateColorPref";                                         //$NON-NLS-1$

  /** Lexical state list or lexical state identifier font attribute preference */
  public static final String P_LEXICAL_STATE_ATR                     = "LexicalStateFontAtrPref";                                       //$NON-NLS-1$

  /** Lexical state list or lexical state identifier color preference */
  public static final String P_LEXICAL_STATE_PUNCT                   = "LexicalStatePunctColorPref";                                    //$NON-NLS-1$

  /** Lexical state list or lexical state identifier font attribute preference */
  public static final String P_LEXICAL_STATE_PUNCT_ATR               = "LexicalStatePunctFontAtrPref";                                  //$NON-NLS-1$

  /** Lexical state list or lexical state identifier color preference */
  public static final String P_LEXICAL_STATE_NEXT                    = "LexicalStateNextColorPref";                                     //$NON-NLS-1$

  /** Lexical state list or lexical state identifier font attribute preference */
  public static final String P_LEXICAL_STATE_NEXT_ATR                = "LexicalStateNextFontAtrPref";                                   //$NON-NLS-1$

  // -------------------- regular expressions ----------------------------------

  /** Regular_expression punctuation color preference */
  public static final String P_REG_EX_BRACE                          = "RegExBraceColorPref";                                           //$NON-NLS-1$

  /** Regular_expression punctuation font attribute preference */
  public static final String P_REG_EX_BRACE_ATR                      = "RegExBraceFontAtrPref";                                         //$NON-NLS-1$

  /** Regular_expression punctuation color preference */
  public static final String P_REG_EX_BRACKET                        = "RegExBracketColorPref";                                         //$NON-NLS-1$

  /** Regular_expression punctuation font attribute preference */
  public static final String P_REG_EX_BRACKET_ATR                    = "RegExBracketFontAtrPref";                                       //$NON-NLS-1$

  /** Regular_expression punctuation color preference */
  public static final String P_REG_EX_TOKEN_PUNCT                    = "RegExTokenPunctColorPref";                                      //$NON-NLS-1$

  /** Regular_expression punctuation font attribute preference */
  public static final String P_REG_EX_TOKEN_PUNCT_ATR                = "RegExTokenPunctFontAtrPref";                                    //$NON-NLS-1$

  /** Regular_expression punctuation color preference */
  public static final String P_REG_EX_CHOICE_PUNCT                   = "RegExChoicePunctColorPref";                                     //$NON-NLS-1$

  /** Regular_expression punctuation font attribute preference */
  public static final String P_REG_EX_CHOICE_PUNCT_ATR               = "RegExChoicePunctFontAtrPref";                                   //$NON-NLS-1$

  /** Choices enclosing punctuation color preference */
  public static final String P_REG_EX_OTHER_PUNCT                    = "RegExOtherPunctColorPref";                                      //$NON-NLS-1$

  /** Choices enclosing punctuation font attribute preference */
  public static final String P_REG_EX_OTHER_PUNCT_ATR                = "RegExOtherPunctFontAtrPref";                                    //$NON-NLS-1$

  // -------------------- JJTree -----------------------------------------------

  /** JJTree node name punctuation color preference */
  public static final String P_JJTREE_NODE_NAME_PUNCT                = "JJTreeNodeNamePunctPref";                                       //$NON-NLS-1$

  /** JJTree node name punctuation font attribute preference */
  public static final String P_JJTREE_NODE_NAME_PUNCT_ATR            = "JJTreeNodeNamePunctFontAtrPref";                                //$NON-NLS-1$

  /** JJTree node name color preference */
  public static final String P_JJTREE_NODE_NAME                      = "JJTreeNodeNamePref";                                            //$NON-NLS-1$

  /** JJTree node name font attribute preference */
  public static final String P_JJTREE_NODE_NAME_ATR                  = "JJTreeNodeNameFontAtrPref";                                     //$NON-NLS-1$

  /** JJTree node expression parentheses color preference */
  public static final String P_JJTREE_NODE_EXPR_PAREN                = "JJTreeNodeExprParenPref";                                       //$NON-NLS-1$

  /** JJTree node expression parentheses font attribute preference */
  public static final String P_JJTREE_NODE_EXPR_PAREN_ATR            = "JJTreeNodeExprParenFontAtrPref";                                //$NON-NLS-1$

  // -------------------- others -----------------------------------------------

  /** JavaCC parser name color preference */
  public static final String P_JAVACC_OPTION                         = "JavaCCOptionColorPref";                                         //$NON-NLS-1$

  /** JavaCC parser name font attribute preference */
  public static final String P_JAVACC_OPTION_ATR                     = "JavaCCOptionFontAtrPref";                                       //$NON-NLS-1$

  /** JavaCC parser name color preference */
  public static final String P_JAVACC_OPTION_BRACE                   = "JavaCCOptionBraceColorPref";                                    //$NON-NLS-1$

  /** JavaCC parser name font attribute preference */
  public static final String P_JAVACC_OPTION_BRACE_ATR               = "JavaCCOptionBraceFontAtrPref";                                  //$NON-NLS-1$

  /** JavaCC parser name color preference */
  public static final String P_JAVACC_PARSER_NAME                    = "JavaCCParserNameColorPref";                                     //$NON-NLS-1$

  /** JavaCC parser name font attribute preference */
  public static final String P_JAVACC_PARSER_NAME_ATR                = "JavaCCParserNameFontAtrPref";                                   //$NON-NLS-1$

  /** JavaCC parser name color preference */
  public static final String P_JAVACC_PARSER_NAME_PAREN              = "JavaCCParserNameParenColorPref";                                //$NON-NLS-1$

  /** JavaCC parser name font attribute preference */
  public static final String P_JAVACC_PARSER_NAME_PAREN_ATR          = "JavaCCParserNameParenFontAtrPref";                              //$NON-NLS-1$

  /** Matching opening/closing character color preference */
  public static final String P_MATCHING_CHAR                         = "MatchingCharColorPref";                                         //$NON-NLS-1$

  /** Console command color preference */
  public static final String P_CONSOLE_COMMAND                       = "ConsoleCommandColorPref";                                       //$NON-NLS-1$

  /** Console command font attribute preference */
  public static final String P_CONSOLE_COMMAND_ATR                   = "ConsoleCommandFontAtrPref";                                     //$NON-NLS-1$

  /** Apply changes on tab switch preference */
  public static final String P_APPLY_ON_TAB_SWITCH                   = "ApplyChangesOnTabSwitchPref";                                   //$NON-NLS-1$

  /** Do not show preference tool tips preference */
  public static final String P_NO_PREFERENCE_TOOL_TIPS               = "DoNotShowPreferenceToolTips";                                   //$NON-NLS-1$

  /** The Hyperlink color preference */
  public static final String P_HYPERLINK_COLOR                       = DefaultHyperlinkPresenter.HYPERLINK_COLOR;

  /** The Hyperlink color system default preference */
  public static final String P_HYPERLINK_COLOR_SYSTEM_DEFAULT        = DefaultHyperlinkPresenter.HYPERLINK_COLOR_SYSTEM_DEFAULT;

  // -------------------- externalized NLS strings -----------------------------

  /** The Constant NLS_FONT_ATR_B */
  public static final String NLS_FONT_ATR_B                          = AbstractActivator.getMsg("PrefPage.Font_atr_B");                    //$NON-NLS-1$

  /** The Constant NLS_FONT_ATR_BI */
  public static final String NLS_FONT_ATR_BI                         = AbstractActivator.getMsg("PrefPage.Font_atr_BI");                   //$NON-NLS-1$

  /** The Constant NLS_FONT_ATR_I */
  public static final String NLS_FONT_ATR_I                          = AbstractActivator.getMsg("PrefPage.Font_atr_I");                    //$NON-NLS-1$

  /** The Constant NLS_GROUP_BNF_PROD */
  public static final String NLS_GROUP_BNF_PROD                      = AbstractActivator.getMsg("PrefPage.Group_bnf_prod");                //$NON-NLS-1$

  /** The Constant NLS_GROUP_COMMENTS */
  public static final String NLS_GROUP_COMMENTS                      = AbstractActivator.getMsg("PrefPage.Group_comments");                //$NON-NLS-1$

  /** The Constant NLS_GROUP_CONSOLE */
  public static final String NLS_GROUP_CONSOLE                       = AbstractActivator.getMsg("PrefPage.Group_console");                 //$NON-NLS-1$

  /** The Constant NLS_GROUP_HYPERLINK */
  public static final String NLS_GROUP_HYPERLINK                     = AbstractActivator.getMsg("PrefPage.Group_hyperlink");               //$NON-NLS-1$

  /** The Constant NLS_GROUP_INDENTATION */
  public static final String NLS_GROUP_INDENTATION                   = AbstractActivator.getMsg("PrefPage.Group_indentation");             //$NON-NLS-1$

  /** The Constant NLS_GROUP_JAVA */
  public static final String NLS_GROUP_JAVA                          = AbstractActivator.getMsg("PrefPage.Group_java");                    //$NON-NLS-1$

  /** The Constant NLS_GROUP_JAVA_BLOCK */
  public static final String NLS_GROUP_JAVA_BLOCK                    = AbstractActivator.getMsg("PrefPage.Group_java_block");              //$NON-NLS-1$

  /** The Constant NLS_GROUP_JAVACC */
  public static final String NLS_GROUP_JAVACC                        = AbstractActivator.getMsg("PrefPage.Group_javaCC");                  //$NON-NLS-1$

  /** The Constant NLS_GROUP_JJTREE */
  public static final String NLS_GROUP_JJTREE                        = AbstractActivator.getMsg("PrefPage.Group_jjtree");                  //$NON-NLS-1$

  /** The Constant NLS_GROUP_LABELS */
  public static final String NLS_GROUP_LABELS                        = AbstractActivator.getMsg("PrefPage.Group_labels");                  //$NON-NLS-1$

  /** The Constant NLS_GROUP_MATCHING */
  public static final String NLS_GROUP_MATCHING                      = AbstractActivator.getMsg("PrefPage.Group_matching");                //$NON-NLS-1$

  /** The Constant NLS_GROUP_NO_TOOL_TIPS */
  public static final String NLS_GROUP_TOOL_TIPS                     = AbstractActivator.getMsg("PrefPage.Group_tool_tips");               //$NON-NLS-1$

  /** The Constant NLS_GROUP_OPTIONS */
  public static final String NLS_GROUP_OPTIONS                       = AbstractActivator.getMsg("PrefPage.Group_options");                 //$NON-NLS-1$

  /** The Constant NLS_GROUP_REGEX */
  public static final String NLS_GROUP_REGEX                         = AbstractActivator.getMsg("PrefPage.Group_regex");                   //$NON-NLS-1$

  /** The Constant NLS_GROUP_SPELLING */
  public static final String NLS_GROUP_SPELLING                      = AbstractActivator.getMsg("PrefPage.Group_spelling");                //$NON-NLS-1$

  /** The Constant NLS_GROUP_STATES */
  public static final String NLS_GROUP_STATES                        = AbstractActivator.getMsg("PrefPage.Group_states");                  //$NON-NLS-1$  

  /** The Constant NLS_HYPERLINK_COLOR */
  public static final String NLS_HYPERLINK_COLOR                     = AbstractActivator.getMsg("PrefPage.Label_hyperlink");               //$NON-NLS-1$  

  /** The Constant NLS_LABEL_APPLY_ON_TAB_SWITCH */
  public static final String NLS_LABEL_APPLY_ON_TAB_SWITCH           = AbstractActivator.getMsg("PrefPage.Label_apply_on_tab");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_COMMENT_BACKGROUND */
  public static final String NLS_LABEL_COMMENT_BACKGROUND            = AbstractActivator.getMsg("PrefPage.Label_comment_bg");              //$NON-NLS-1$

  /** The Constant NLS_LABEL_COMMENT_LINE */
  public static final String NLS_LABEL_COMMENT_LINE                  = AbstractActivator.getMsg("PrefPage.Label_comment_line");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_COMMENT_BLOCK */
  public static final String NLS_LABEL_COMMENT_BLOCK                 = AbstractActivator.getMsg("PrefPage.Label_comment_block");           //$NON-NLS-1$

  /** The Constant NLS_LABEL_COMMENT_JAVADOC */
  public static final String NLS_LABEL_COMMENT_JAVADOC               = AbstractActivator.getMsg("PrefPage.Label_comment_Javadoc");         //$NON-NLS-1$

  /** The Constant NLS_LABEL_CONSOLE_COMMAND */
  public static final String NLS_LABEL_CONSOLE_COMMAND               = AbstractActivator.getMsg("PrefPage.Label_console_command");         //$NON-NLS-1$

  /** The Constant NLS_LABEL_INDENT_CHAR */
  public static final String NLS_LABEL_INDENT_CHAR                   = AbstractActivator.getMsg("PrefPage.Label_indent_char");             //$NON-NLS-1$

  /** The Constant NLS_LABEL_INDENT_CHAR_NB */
  public static final String NLS_LABEL_INDENT_CHAR_NB                = AbstractActivator.getMsg("PrefPage.Label_indent_char_nb");          //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_BACKGROUND */
  public static final String NLS_LABEL_JAVA_BACKGROUND               = AbstractActivator.getMsg("PrefPage.Label_java_bg");                 //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_BLOCK */
  public static final String NLS_LABEL_JAVA_BLOCK                    = AbstractActivator.getMsg("PrefPage.Label_java_block");              //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_BLOCK_BRACE_ALT_BG */
  public static final String NLS_LABEL_JAVA_BLOCK_BRACE_ALT_BG       = AbstractActivator.getMsg("PrefPage.Label_java_block_brace_bg");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_DEFAULT */
  public static final String NLS_LABEL_JAVA_DEFAULT_TEXT             = AbstractActivator.getMsg("PrefPage.Label_java_default_text");       //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_IDENTIFIER */
  public static final String NLS_LABEL_JAVA_IDENTIFIER               = AbstractActivator.getMsg("PrefPage.Label_java_identifier");         //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_KEYWORD */
  public static final String NLS_LABEL_JAVA_KEYWORD                  = AbstractActivator.getMsg("PrefPage.Label_java_keyword");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_NUMERIC */
  public static final String NLS_LABEL_JAVA_NUMERIC                  = AbstractActivator.getMsg("PrefPage.Label_java_numeric");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_PUNCTUATION */
  public static final String NLS_LABEL_JAVA_PUNCTUATION              = AbstractActivator.getMsg("PrefPage.Label_java_punctuation");        //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVA_STRING */
  public static final String NLS_LABEL_JAVA_STRING                   = AbstractActivator.getMsg("PrefPage.Label_java_string");             //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_BACKGROUND */
  public static final String NLS_LABEL_JAVACC_BACKGROUND             = AbstractActivator.getMsg("PrefPage.Label_javacc_bg");               //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_CHOICE_PUNCT */
  public static final String NLS_LABEL_JAVACC_CHOICE_PUNCT           = AbstractActivator.getMsg("PrefPage.Label_javacc_choice_punct");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_DEFAULT_TEXT */
  public static final String NLS_LABEL_JAVACC_DEFAULT_TEXT           = AbstractActivator.getMsg("PrefPage.Label_javacc_default_text");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_EXPANSION_BRACE */
  public static final String NLS_LABEL_JAVACC_EXPANSION_BRACE        = AbstractActivator.getMsg("PrefPage.Label_javacc_expansion_brace");  //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_KEYWORD */
  public static final String NLS_LABEL_JAVACC_KEYWORD                = AbstractActivator.getMsg("PrefPage.Label_javacc_keyword");          //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_NUMERIC */
  public static final String NLS_LABEL_JAVACC_NUMERIC                = AbstractActivator.getMsg("PrefPage.Label_javacc_numeric");          //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_OPTION */
  public static final String NLS_LABEL_JAVACC_OPTION                 = AbstractActivator.getMsg("PrefPage.Label_javacc_option");           //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_OPTION_BRACE */
  public static final String NLS_LABEL_JAVACC_OPTION_BRACE           = AbstractActivator.getMsg("PrefPage.Label_javacc_option_brace");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_OTHER_PUNCT */
  public static final String NLS_LABEL_JAVACC_OTHER_PUNCT            = AbstractActivator.getMsg("PrefPage.Label_javacc_other_punct");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_PARSER_NAME */
  public static final String NLS_LABEL_JAVACC_PARSER_NAME            = AbstractActivator.getMsg("PrefPage.Label_javacc_parser_name");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_PARSER_NAME_PAREN */
  public static final String NLS_LABEL_JAVACC_PARSER_NAME_PAREN      = AbstractActivator.getMsg("PrefPage.Label_javacc_parser_name_paren"); //$NON-NLS-1$

  /** The Constant NLS_LABEL_JAVACC_STRING */
  public static final String NLS_LABEL_JAVACC_STRING                 = AbstractActivator.getMsg("PrefPage.Label_javacc_string");           //$NON-NLS-1$

  /** The Constant NLS_LABEL_JJTREE_NODE_EXPR_PAREN */
  public static final String NLS_LABEL_JJTREE_NODE_EXPR_PAREN        = AbstractActivator.getMsg("PrefPage.Label_jjtree_node_expr_paren");  //$NON-NLS-1$

  /** The Constant NLS_LABEL_JJTREE_NODE_NAME */
  public static final String NLS_LABEL_JJTREE_NODE_NAME              = AbstractActivator.getMsg("PrefPage.Label_jjtree_node_name");        //$NON-NLS-1$

  /** The Constant NLS_LABEL_JJTREE_NODE_NAME_PUNCT */
  public static final String NLS_LABEL_JJTREE_NODE_NAME_PUNCT        = AbstractActivator.getMsg("PrefPage.Label_jjtree_node_name_punct");  //$NON-NLS-1$

  /** The Constant NLS_LABEL_LEXICAL_STATE */
  public static final String NLS_LABEL_LEXICAL_STATE                 = AbstractActivator.getMsg("PrefPage.Label_lexical_state");           //$NON-NLS-1$

  /** The Constant NLS_LABEL_LEXICAL_STATE_NEXT */
  public static final String NLS_LABEL_LEXICAL_STATE_NEXT            = AbstractActivator.getMsg("PrefPage.Label_lexical_state_next");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_LEXICAL_STATE_PUNCT */
  public static final String NLS_LABEL_LEXICAL_STATE_PUNCT           = AbstractActivator.getMsg("PrefPage.Label_lexical_state_punct");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_MATCHING_CHAR */
  public static final String NLS_LABEL_MATCHING_CHAR                 = AbstractActivator.getMsg("PrefPage.Label_matching_char");           //$NON-NLS-1$

  /** The Constant NLS_LABEL_NO_ADV_AUTO_INDENT */
  public static final String NLS_LABEL_NO_ADV_AUTO_INDENT            = AbstractActivator.getMsg("PrefPage.Label_no_adv_auto_indent");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_NO_SPELL_CHECKING */
  public static final String NLS_LABEL_NO_SPELL_CHECKING             = AbstractActivator.getMsg("PrefPage.Label_no_spell_checking");       //$NON-NLS-1$

  /** The Constant NLS_LABEL_NO_TOOL_TIPS */
  public static final String NLS_LABEL_NO_TOOL_TIPS                  = AbstractActivator.getMsg("PrefPage.Label_no_tool_tips");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_REG_EX_BRACE */
  public static final String NLS_LABEL_REG_EX_BRACE                  = AbstractActivator.getMsg("PrefPage.Label_reg_ex_brace");            //$NON-NLS-1$

  /** The Constant NLS_LABEL_REG_EX_BRACKET */
  public static final String NLS_LABEL_REG_EX_BRACKET                = AbstractActivator.getMsg("PrefPage.Label_reg_ex_bracket");          //$NON-NLS-1$

  /** The Constant NLS_LABEL_REG_EX_CHOICE_PUNCT */
  public static final String NLS_LABEL_REG_EX_CHOICE_PUNCT           = AbstractActivator.getMsg("PrefPage.Label_reg_ex_choice_punct");     //$NON-NLS-1$

  /** The Constant NLS_LABEL_REG_EX_OTHER_PUNCT */
  public static final String NLS_LABEL_REG_EX_OTHER_PUNCT            = AbstractActivator.getMsg("PrefPage.Label_reg_ex_other_punct");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_REG_EX_TOKEN_PUNCT */
  public static final String NLS_LABEL_REG_EX_TOKEN_PUNCT            = AbstractActivator.getMsg("PrefPage.Label_reg_ex_token_punct");      //$NON-NLS-1$

  /** The Constant NLS_LABEL_TOKEN_LABEL */
  public static final String NLS_LABEL_TOKEN_LABEL                   = AbstractActivator.getMsg("PrefPage.Label_token");                   //$NON-NLS-1$

  /** The Constant NLS_LABEL_TOKEN_LABEL_DEF */
  public static final String NLS_LABEL_TOKEN_LABEL_DEF               = AbstractActivator.getMsg("PrefPage.Label_token_def");               //$NON-NLS-1$

  /** The Constant NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF */
  public static final String NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF       = AbstractActivator.getMsg("PrefPage.Label_token_priv_def");          //$NON-NLS-1$

  /** The Constant NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF_PUNCT */
  public static final String NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF_PUNCT = AbstractActivator.getMsg("PrefPage.Label_token_priv_def_punct");    //$NON-NLS-1$

  /** The Constant NLS_LABEL_TOKEN_LABEL_PUNCT */
  public static final String NLS_LABEL_TOKEN_LABEL_PUNCT             = AbstractActivator.getMsg("PrefPage.Label_token_punct");             //$NON-NLS-1$

  /** The Constant NLS_TAB_JAVA */
  public static final String NLS_TAB_JAVA                            = AbstractActivator.getMsg("PrefPage.Tab_java");                      //$NON-NLS-1$

  /** The Constant NLS_TAB_JAVACC */
  public static final String NLS_TAB_JAVACC                          = AbstractActivator.getMsg("PrefPage.Tab_javacc");                    //$NON-NLS-1$

  /** The Constant NLS_TAB_OTHER */
  public static final String NLS_TAB_OTHER                           = AbstractActivator.getMsg("PrefPage.Tab_other");                     //$NON-NLS-1$

  /** The Constant NLS_TAB_PRODUCTIONS */
  public static final String NLS_TAB_PRODUCTIONS                     = AbstractActivator.getMsg("PrefPage.Tab_productions");               //$NON-NLS-1$

  /** The Constant NLS_TITLE_PREF_PAGE */
  public static final String NLS_TITLE_PREF_PAGE                     = AbstractActivator.getMsg("PrefPage.Title_pref_page");               //$NON-NLS-1$

  /** The Constant NLS_TT_APPLY_ON_TAB_SWITCH */
  public static final String NLS_TT_APPLY_ON_TAB_SWITCH              = AbstractActivator.getMsg("PrefPage.Tt_apply_on_tab");               //$NON-NLS-1$

  /** The Constant NLS_TT_BUTTON_APPLY */
  public static final String NLS_TT_BUTTON_APPLY                     = AbstractActivator.getMsg("PrefPage.Tt_button_apply");               //$NON-NLS-1$

  /** The Constant NLS_TT_BUTTON_DEFAULTS */
  public static final String NLS_TT_BUTTON_DEFAULTS                  = AbstractActivator.getMsg("PrefPage.Tt_button_defaults");            //$NON-NLS-1$

  /** The Constant NLS_TT_COMMENT_BACKGROUND */
  public static final String NLS_TT_COMMENT_BACKGROUND               = AbstractActivator.getMsg("PrefPage.Tt_comment_bg");                 //$NON-NLS-1$

  /** The Constant NLS_TT_COMMENT_BACKGROUND_BUTTON */
  public static final String NLS_TT_COMMENT_BACKGROUND_BUTTON        = AbstractActivator.getMsg("PrefPage.Tt_comment_bg_button");          //$NON-NLS-1$

  /** The Constant NLS_TT_COMMENT_BLOCK */
  public static final String NLS_TT_COMMENT_BLOCK                    = AbstractActivator.getMsg("PrefPage.Tt_comment_block");              //$NON-NLS-1$

  /** The Constant NLS_TT_COMMENT_LINE */
  public static final String NLS_TT_COMMENT_LINE                     = AbstractActivator.getMsg("PrefPage.Tt_comment_line");               //$NON-NLS-1$

  /** The Constant NLS_TT_COMMENT_JAVADOC */
  public static final String NLS_TT_COMMENT_JAVADOC                  = AbstractActivator.getMsg("PrefPage.Tt_comment_javadoc");            //$NON-NLS-1$

  /** The Constant NLS_TT_CONSOLE_COMMAND */
  public static final String NLS_TT_CONSOLE_COMMAND                  = AbstractActivator.getMsg("PrefPage.Tt_console_command");            //$NON-NLS-1$

  /** The Constant NLS_TT_FONT_ATR */
  public static final String NLS_TT_FONT_ATR                         = AbstractActivator.getMsg("PrefPage.Tt_font_atr");                   //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_BNF_PROD */
  public static final String NLS_TT_GROUP_BNF_PROD                   = AbstractActivator.getMsg("PrefPage.Tt_group_bnf_prod");             //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_COMMENTS */
  public static final String NLS_TT_GROUP_COMMENTS                   = AbstractActivator.getMsg("PrefPage.Tt_group_comments");             //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_CONSOLE */
  public static final String NLS_TT_GROUP_CONSOLE                    = AbstractActivator.getMsg("PrefPage.Tt_group_console");              //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_HYPERLINK */
  public static final String NLS_TT_GROUP_HYPERLINK                  = AbstractActivator.getMsg("PrefPage.Tt_group_hyperlink");            //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_INDENTATION */
  public static final String NLS_TT_GROUP_INDENTATION                = AbstractActivator.getMsg("PrefPage.Tt_group_indentation");          //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_JAVA */
  public static final String NLS_TT_GROUP_JAVA                       = AbstractActivator.getMsg("PrefPage.Tt_group_java");                 //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_JAVA_BLOCK */
  public static final String NLS_TT_GROUP_JAVA_BLOCK                 = AbstractActivator.getMsg("PrefPage.Tt_group_java_block");           //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_JAVACC */
  public static final String NLS_TT_GROUP_JAVACC                     = AbstractActivator.getMsg("PrefPage.Tt_group_javacc");               //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_JJTREE */
  public static final String NLS_TT_GROUP_JJTREE                     = AbstractActivator.getMsg("PrefPage.Tt_group_jjtree");               //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_LABELS */
  public static final String NLS_TT_GROUP_LABELS                     = AbstractActivator.getMsg("PrefPage.Tt_group_labels");               //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_MATCHING */
  public static final String NLS_TT_GROUP_MATCHING                   = AbstractActivator.getMsg("PrefPage.Tt_group_matching");             //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_NO_TOOL_TIPS */
  public static final String NLS_TT_GROUP_TOOL_TIPS                  = AbstractActivator.getMsg("PrefPage.Tt_group_tool_tips");            //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_OPTIONS */
  public static final String NLS_TT_GROUP_OPTIONS                    = AbstractActivator.getMsg("PrefPage.Tt_group_options");              //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_REGEX */
  public static final String NLS_TT_GROUP_REGEX                      = AbstractActivator.getMsg("PrefPage.Tt_group_regex");                //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_SPELLING */
  public static final String NLS_TT_GROUP_SPELLING                   = AbstractActivator.getMsg("PrefPage.Tt_group_spelling");             //$NON-NLS-1$

  /** The Constant NLS_TT_GROUP_STATES */
  public static final String NLS_TT_GROUP_STATES                     = AbstractActivator.getMsg("PrefPage.Tt_group_states");               //$NON-NLS-1$

  /** The Constant NLS_TT_HYPERLINK_COLOR */
  public static final String NLS_TT_HYPERLINK_COLOR                  = AbstractActivator.getMsg("PrefPage.Tt_hyperlink");                  //$NON-NLS-1$

  /** The Constant NLS_TT_INDENT_CHAR */
  public static final String NLS_TT_INDENT_CHAR                      = AbstractActivator.getMsg("PrefPage.Tt_indent_char");                //$NON-NLS-1$

  /** The Constant NLS_TT_INDENT_CHAR_NB */
  public static final String NLS_TT_INDENT_CHAR_NB                   = AbstractActivator.getMsg("PrefPage.Tt_indent_char_nb");             //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_BACKGROUND */
  public static final String NLS_TT_JAVA_BACKGROUND                  = AbstractActivator.getMsg("PrefPage.Tt_java_background");            //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_BACKGROUND_BUTTON */
  public static final String NLS_TT_JAVA_BACKGROUND_BUTTON           = AbstractActivator.getMsg("PrefPage.Tt_java_background_button");     //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_BLOCK */
  public static final String NLS_TT_JAVA_BLOCK                       = AbstractActivator.getMsg("PrefPage.Tt_java_block");                 //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_BLOCK_BRACE_ALT_BG */
  public static final String NLS_TT_JAVA_BLOCK_BRACE_ALT_BG          = AbstractActivator.getMsg("PrefPage.Tt_java_block_brace_alt_bg");    //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_DEFAULT */
  public static final String NLS_TT_JAVA_DEFAULT_TEXT                = AbstractActivator.getMsg("PrefPage.Tt_java_default_text");          //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_KEYWORD */
  public static final String NLS_TT_JAVA_KEYWORD                     = AbstractActivator.getMsg("PrefPage.Tt_java_keyword");               //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_IDENTIFIER */
  public static final String NLS_TT_JAVA_IDENTIFIER                  = AbstractActivator.getMsg("PrefPage.Tt_java_identifier");            //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_NUMERIC */
  public static final String NLS_TT_JAVA_NUMERIC                     = AbstractActivator.getMsg("PrefPage.Tt_java_numeric");               //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_PUNCTUATION */
  public static final String NLS_TT_JAVA_PUNCTUATION                 = AbstractActivator.getMsg("PrefPage.Tt_java_punctuation");           //$NON-NLS-1$

  /** The Constant NLS_TT_JAVA_STRING */
  public static final String NLS_TT_JAVA_STRING                      = AbstractActivator.getMsg("PrefPage.Tt_java_string");                //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_BACKGROUND */
  public static final String NLS_TT_JAVACC_BACKGROUND                = AbstractActivator.getMsg("PrefPage.Tt_Javacc_background");          //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_BACKGROUND_BUTTON */
  public static final String NLS_TT_JAVACC_BACKGROUND_BUTTON         = AbstractActivator.getMsg("PrefPage.Tt_Javacc_background_button");   //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_CHOICE_PUNCT */
  public static final String NLS_TT_JAVACC_CHOICE_PUNCT              = AbstractActivator.getMsg("PrefPage.Tt_Javacc_choice_punct");        //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_DEFAULT_TEXT */
  public static final String NLS_TT_JAVACC_DEFAULT_TEXT              = AbstractActivator.getMsg("PrefPage.Tt_Javacc_default_text");        //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_EXPANSION_BRACE */
  public static final String NLS_TT_JAVACC_EXPANSION_BRACE           = AbstractActivator.getMsg("PrefPage.Tt_Javacc_expansion_brace");     //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_KEYWORD */
  public static final String NLS_TT_JAVACC_KEYWORD                   = AbstractActivator.getMsg("PrefPage.Tt_Javacc_keyword");             //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_NUMERIC */
  public static final String NLS_TT_JAVACC_NUMERIC                   = AbstractActivator.getMsg("PrefPage.Tt_Javacc_numeric");             //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_OPTION */
  public static final String NLS_TT_JAVACC_OPTION                    = AbstractActivator.getMsg("PrefPage.Tt_Javacc_option");              //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_OPTION_BRACE */
  public static final String NLS_TT_JAVACC_OPTION_BRACE              = AbstractActivator.getMsg("PrefPage.Tt_Javacc_option_brace");        //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_OTHER_PUNCT */
  public static final String NLS_TT_JAVACC_OTHER_PUNCT               = AbstractActivator.getMsg("PrefPage.Tt_Javacc_other_punct");         //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_PARSER_NAME */
  public static final String NLS_TT_JAVACC_PARSER_NAME               = AbstractActivator.getMsg("PrefPage.Tt_Javacc_parser_name");         //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_PARSER_NAME_PAREN */
  public static final String NLS_TT_JAVACC_PARSER_NAME_PAREN         = AbstractActivator.getMsg("PrefPage.Tt_Javacc_parser_name_paren");   //$NON-NLS-1$

  /** The Constant NLS_TT_JAVACC_STRING */
  public static final String NLS_TT_JAVACC_STRING                    = AbstractActivator.getMsg("PrefPage.Tt_Javacc_string");              //$NON-NLS-1$

  /** The Constant NLS_TT_JJTREE_NODE_NAME */
  public static final String NLS_TT_JJTREE_NODE_NAME                 = AbstractActivator.getMsg("PrefPage.Tt_jjtree_node_name");           //$NON-NLS-1$

  /** The Constant NLS_TT_JJTREE_NODE_NAME_PUNCT */
  public static final String NLS_TT_JJTREE_NODE_NAME_PUNCT           = AbstractActivator.getMsg("PrefPage.Tt_jjtree_node_name_punct");     //$NON-NLS-1$

  /** The Constant NLS_TT_JJTREE_NODE_EXPR_PAREN */
  public static final String NLS_TT_JJTREE_NODE_EXPR_PAREN           = AbstractActivator.getMsg("PrefPage.Tt_jjtree_node_expr_paren");     //$NON-NLS-1$

  /** The Constant NLS_TT_LEXICAL_STATE */
  public static final String NLS_TT_LEXICAL_STATE                    = AbstractActivator.getMsg("PrefPage.Tt_lexical_state");              //$NON-NLS-1$

  /** The Constant NLS_TT_LEXICAL_STATE_NEXT */
  public static final String NLS_TT_LEXICAL_STATE_NEXT               = AbstractActivator.getMsg("PrefPage.Tt_lexical_state_next");         //$NON-NLS-1$

  /** The Constant NLS_TT_LEXICAL_STATE_PUNCT */
  public static final String NLS_TT_LEXICAL_STATE_PUNCT              = AbstractActivator.getMsg("PrefPage.Tt_lexical_state_punct");        //$NON-NLS-1$

  /** The Constant NLS_TT_MATCHING_CHAR */
  public static final String NLS_TT_MATCHING_CHAR                    = AbstractActivator.getMsg("PrefPage.Tt_matching_char");              //$NON-NLS-1$

  /** The Constant NLS_TT_NO_ADV_AUTO_INDENT */
  public static final String NLS_TT_NO_ADV_AUTO_INDENT               = AbstractActivator.getMsg("PrefPage.Tt_no_adv_auto_ident");          //$NON-NLS-1$

  /** The Constant NLS_TT_NO_SPELL_CHECKING */
  public static final String NLS_TT_NO_SPELL_CHECKING                = AbstractActivator.getMsg("PrefPage.Tt_no_spell_checking");          //$NON-NLS-1$

  /** The Constant NLS_TT_NO_TOOL_TIPS */
  public static final String NLS_TT_NO_TOOL_TIPS                     = AbstractActivator.getMsg("PrefPage.Tt_no_tool_tips");               //$NON-NLS-1$

  /** The Constant NLS_TT_REG_EX_BRACE */
  public static final String NLS_TT_REG_EX_BRACE                     = AbstractActivator.getMsg("PrefPage.Tt_reg_ex_brace");               //$NON-NLS-1$

  /** The Constant NLS_TT_REG_EX_BRACKET */
  public static final String NLS_TT_REG_EX_BRACKET                   = AbstractActivator.getMsg("PrefPage.Tt_reg_ex_bracket");             //$NON-NLS-1$

  /** The Constant NLS_TT_REG_EX_CHOICE_PUNCT */
  public static final String NLS_TT_REG_EX_CHOICE_PUNCT              = AbstractActivator.getMsg("PrefPage.Tt_reg_ex_choice_punct");        //$NON-NLS-1$

  /** The Constant NLS_TT_REG_EX_OTHER_PUNCT */
  public static final String NLS_TT_REG_EX_OTHER_PUNCT               = AbstractActivator.getMsg("PrefPage.Tt_reg_ex_other_punct");         //$NON-NLS-1$

  /** The Constant NLS_TT_REG_EX_TOKEN_PUNCT */
  public static final String NLS_TT_REG_EX_TOKEN_PUNCT               = AbstractActivator.getMsg("PrefPage.Tt_reg_ex_token_punct");         //$NON-NLS-1$

  /** The Constant NLS_TT_TAB_JAVACC */
  public static final String NLS_TT_TAB_JAVACC                       = AbstractActivator.getMsg("PrefPage.Tt_tab_javacc");                 //$NON-NLS-1$

  /** The Constant NLS_TT_TAB_JAVA */
  public static final String NLS_TT_TAB_JAVA                         = AbstractActivator.getMsg("PrefPage.Tt_tab_java");                   //$NON-NLS-1$

  /** The Constant NLS_TT_TAB_OTHER */
  public static final String NLS_TT_TAB_OTHER                        = AbstractActivator.getMsg("PrefPage.Tt_tab_other");                  //$NON-NLS-1$

  /** The Constant NLS_TT_TAB_PRODUCTIONS */
  public static final String NLS_TT_TAB_PRODUCTIONS                  = AbstractActivator.getMsg("PrefPage.Tt_tab_productions");            //$NON-NLS-1$

  /** The Constant NLS_TT_TOKEN_LABEL */
  public static final String NLS_TT_TOKEN_LABEL                      = AbstractActivator.getMsg("PrefPage.Tt_token_label");                //$NON-NLS-1$

  /** The Constant NLS_TT_TOKEN_LABEL_DEF */
  public static final String NLS_TT_TOKEN_LABEL_DEF                  = AbstractActivator.getMsg("PrefPage.Tt_token_label_def");            //$NON-NLS-1$

  /** The Constant NLS_TT_TOKEN_LABEL_PRIVATE_DEF */
  public static final String NLS_TT_TOKEN_LABEL_PRIVATE_DEF          = AbstractActivator.getMsg("PrefPage.Tt_token_label_priv_def");       //$NON-NLS-1$

  /** The Constant NLS_TT_TOKEN_LABEL_PRIVATE_DEF_PUNCT */
  public static final String NLS_TT_TOKEN_LABEL_PRIVATE_DEF_PUNCT    = AbstractActivator.getMsg("PrefPage.Tt_token_label_priv_def_punct"); //$NON-NLS-1$

  /** The Constant NLS_TT_TOKEN_LABEL_PUNCT */
  public static final String NLS_TT_TOKEN_LABEL_PUNCT                = AbstractActivator.getMsg("PrefPage.Tt_token_label_punct");          //$NON-NLS-1$
}
