package sf.eclipse.javacc.scanners;

import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * The {@link JavaCCCodeRule} Class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014
 */
class JavaCCCodeRule implements IRule {

  // BF  05/2012 : created
  // MMa 09/2012 : modified colors for ":" ; added "!" as a JavaCC punctuation character (JTB specific syntax)
  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings ; added OUTPUT_LANGUAGE option

  /** The preference store */
  protected final IPreferenceStore     jStore = AbstractActivator.getDefault().getPreferenceStore();

  /** The color map */
  protected final Map<String, Color>   jColorMap;

  /** The attribute map */
  protected final Map<String, Integer> jAtrMap;

  /** The token map */
  protected final Map<String, IToken>  jTokenMap;

  /** The Java code rule */
  protected final JavaCodeRule         jJavaCodeRule;

  /** The alternate Java code rule */
  protected final JavaCodeRule         jJavaCodeAltRule;

  /** The JavaCC keyword rule */
  protected WordRule                   jJavaCCKeywordRule;

  /** The options keyword rule */
  protected WordRule                   jOptionsKeywordRule;

  /** The string rule */
  protected SingleLineRule             jStringRule;

  /** The numeric rule */
  protected NumericLiteralRule         jNumericRule;

  /** The regular expression punctuation rule */
  protected WordRule                   jRegExPunctuationRule;

  /** The lexical definition name rule */
  protected WordRule                   jLexicalDefinitionNameRule;

  /** The protected lexical definition name rule */
  protected WordRule                   jPrivateLexicalDefinitionNameRule;

  /** The expansion choices punctuation rule */
  protected WordRule                   jBnfPunctuationRule;

  /** The whitespace rule */
  protected WhitespaceRule             jWhitespaceRule;

  /** The unexpected character rule */
  protected SimpleRule                 jUnexpectedCharacterRule;

  /** The begin state rules array */
  protected IRule[]                    jOptionBlockRules;

  /** The parser name rules array */
  protected IRule[]                    jParserNameRules;

  /** The TOKEN_MGR_DECLS rules array */
  protected IRule[]                    jTokenMgrDeclsRules;

  /** The regular expression production rules array */
  protected IRule[]                    jRegularExprProductionRules;

  /** The regular expression specification rules array */
  protected IRule[]                    jRegExprSpecRules;

  /** The regular expression rules array */
  protected IRule[]                    jRegularExpressionRules;

  /** The BNF production rules array */
  protected IRule[]                    jBnfProductionRules;

  /** The BNF expansion choice rules array */
  protected IRule[]                    jBnfChoiceRules;

  /** The whitespace rules array */
  protected IRule[]                    jWhitespaceRules;

  /** The JJTree node rules array */
  protected IRule[]                    jJjtreeNodeRules;

  /** The protected name flag */
  protected boolean                    jPrivateName;

  /** The argument list flag */
  protected boolean                    jArgList;

  /** The scanner */
  protected CodeScanner                jScanner;

  /** The state stack */
  protected final Stack<Context>       jStateStack;

  /**
   * Instantiates a new JavaCC code rule.
   * 
   * @param fColorMap2 - the HashMap for the colors
   * @param fAtrMap2 - the HashMap for the font attributes
   */
  public JavaCCCodeRule(final Map<String, Color> fColorMap2, final Map<String, Integer> fAtrMap2) {
    jColorMap = fColorMap2;
    jAtrMap = fAtrMap2;
    jTokenMap = new HashMap<String, IToken>();

    jJavaCodeRule = new JavaCodeRule(fColorMap2, fAtrMap2, P_JAVA_BACKGROUND);
    jJavaCodeAltRule = new JavaCodeRule(fColorMap2, fAtrMap2, P_JAVACC_BACKGROUND);
    updateRules(null);

    jStateStack = new Stack<Context>();
    initialize();
  }

  /**
   * Initializes the context to the start of the file.
   */
  public void initialize() {
    jStateStack.clear();
    pushState(Context.AT_NEXT_PRODUCTION);
  }

  /**
   * Updates the rules after a preference change.
   * 
   * @param preferenceName - the changed preference name
   */
  public void updateRules(final String preferenceName) {
    final boolean all = (preferenceName == null || preferenceName == P_JAVACC_BACKGROUND);
    final Color background = jColorMap.get(P_JAVACC_BACKGROUND);
    String prefName = null;

    if (all) {
      prefName = P_JAVACC_BACKGROUND;
      jTokenMap.put(prefName, new Token(new TextAttribute(null, background, SWT.NONE)));
    }

    for (int i = 0; i < sJavaCCColorPrefNames.length; i++) {
      if (all || preferenceName == sJavaCCColorPrefNames[i] || preferenceName == sJavaCCAtrPrefNames[i]) {
        prefName = sJavaCCColorPrefNames[i];
        jTokenMap.put(prefName,
                      new Token(new TextAttribute(jColorMap.get(prefName), background,
                                                  jAtrMap.get(sJavaCCAtrPrefNames[i]).intValue())));
        if (!all) {
          break;
        }
      }
    }

    // The java block braces use the java background color
    if (jStore.getBoolean(P_JAVA_BLOCK_BRACE_ALT_BG)) {
      jTokenMap.put(P_JAVA_BLOCK_BRACE, new Token(new TextAttribute(jColorMap.get(P_JAVA_BLOCK_BRACE),
                                                                    jColorMap.get(P_JAVA_BACKGROUND),
                                                                    jAtrMap.get(P_JAVA_BLOCK_BRACE_ATR)
                                                                           .intValue())));
    }

    jWhitespaceRule = new WhitespaceRule(new WhitespaceDetector(), jTokenMap.get(P_JAVACC_BACKGROUND));
    jStringRule = new SingleLineRule("\"", "\"", jTokenMap.get(P_JAVACC_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    jNumericRule = new NumericLiteralRule(jTokenMap.get(P_JAVACC_NUMERIC));
    jUnexpectedCharacterRule = new SimpleRule("", jTokenMap.get(P_JAVACC_DEFAULT_TEXT)); //$NON-NLS-1$ 

    jLexicalDefinitionNameRule = new WordRule(new WordDetector(), jTokenMap.get(P_TOKEN_LABEL_DEF));
    jPrivateLexicalDefinitionNameRule = new WordRule(new WordDetector(),
                                                     jTokenMap.get(P_TOKEN_LABEL_PRIVATE_DEF));

    jJavaCCKeywordRule = new WordRule(new WordDetector());
    for (final String key : sJavaCCKeywords) {
      jJavaCCKeywordRule.addWord(key, jTokenMap.get(P_JAVACC_KEYWORD));
    }

    jRegExPunctuationRule = new WordRule(new SingleCharDetector());
    for (int i = 0; i < sPunctuationChars.length; i++) {
      jRegExPunctuationRule.addWord(sPunctuationChars[i], jTokenMap.get(sRegexPunctuationPrefs[i]));
    }

    jBnfPunctuationRule = new WordRule(new SingleCharDetector());
    for (int i = 0; i < sPunctuationChars.length; i++) {
      jBnfPunctuationRule.addWord(sPunctuationChars[i], jTokenMap.get(sBnfPunctuationPrefs[i]));
    }

    jOptionsKeywordRule = new WordRule(new WordDetector());
    for (final String opt : sJjOptionsKeywords) {
      jOptionsKeywordRule.addWord(opt, jTokenMap.get(P_JAVACC_OPTION));
    }
    for (final String opt : sJjtOptionsKeywords) {
      jOptionsKeywordRule.addWord(opt, jTokenMap.get(P_JAVACC_OPTION));
    }
    for (final String opt : sJtbOptionsKeywords) {
      jOptionsKeywordRule.addWord(opt, jTokenMap.get(P_JAVACC_OPTION));
    }
    for (final String key : sJjdocOptionsKeywords) {
      jOptionsKeywordRule.addWord(key, jTokenMap.get(P_JAVACC_OPTION));
    }

    jOptionBlockRules = new IRule[] {
        jWhitespaceRule, // 
        new SimpleRule("options", jTokenMap.get(P_JAVACC_KEYWORD)), //$NON-NLS-1$ 
        new SimpleRule("{", jTokenMap.get(P_JAVACC_OPTION_BRACE)), //$NON-NLS-1$ 
        jOptionsKeywordRule, //
        new SimpleRule("}", jTokenMap.get(P_JAVACC_OPTION_BRACE)), //$NON-NLS-1$ 
        // new SimpleRule("=", fTokenMap.get(P_JAVACC_)), // Left as Java punctuation $NON-NLS-1$ 
        // new SimpleRule(";", fTokenMap.get(P_JAVACC_)), // Left as Java punctuation $NON-NLS-1$ 
        jJavaCodeAltRule, //
    };

    jParserNameRules = new IRule[] {
        jWhitespaceRule, //
        new WordRule(new WordDetector(), jTokenMap.get(P_JAVACC_PARSER_NAME)), //
        new SimpleRule("(", jTokenMap.get(P_JAVACC_PARSER_NAME_PAREN)), //$NON-NLS-1$ 
        new SimpleRule(")", jTokenMap.get(P_JAVACC_PARSER_NAME_PAREN)), //$NON-NLS-1$ 
        jUnexpectedCharacterRule, //
    };

    jTokenMgrDeclsRules = new IRule[] {
        jWhitespaceRule, //
        new SimpleRule(":", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        jUnexpectedCharacterRule, //
    };

    jRegularExprProductionRules = new IRule[] {
        jWhitespaceRule, //
        jJavaCCKeywordRule, //
        new WordRule(new WordDetector(), jTokenMap.get(P_LEXICAL_STATE)), //
        new SimpleRule("*", jTokenMap.get(P_LEXICAL_STATE)), //$NON-NLS-1$ 
        new SimpleRule("<", jTokenMap.get(P_LEXICAL_STATE_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule(">", jTokenMap.get(P_LEXICAL_STATE_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule("[", jTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule("]", jTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        // MMa one line modified
        //        new SimpleRule(":", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule(":", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule(",", jTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        jUnexpectedCharacterRule, //
    };

    jRegExprSpecRules = new IRule[] {
        jWhitespaceRule, //
        new WordRule(new WordDetector(), jTokenMap.get(P_LEXICAL_STATE_NEXT)), //
        new SimpleRule("|", jTokenMap.get(P_REG_EX_CHOICE_PUNCT)), //$NON-NLS-1$ 
        // MMa added 1 line
        new SimpleRule("!", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        // MMa one line modified
        //        new SimpleRule(":", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule(":", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        jStringRule, //
        jUnexpectedCharacterRule, //
    };

    jRegularExpressionRules = new IRule[] {
        jWhitespaceRule, //
        new SimpleRule("EOF", jTokenMap.get(P_JAVACC_KEYWORD)), //$NON-NLS-1$ 
        new WordRule(new WordDetector(), jTokenMap.get(P_TOKEN_LABEL)), //
        jNumericRule, //
        // new SimpleRule(":", fTokenMap.get(P_REGEX_OTHER_PUNCT)), //$NON-NLS-1$ 
        // new SimpleRule(",", fTokenMap.get(P_REGEX_OTHER_PUNCT)), //$NON-NLS-1$ 
        jRegExPunctuationRule, //
        jStringRule, //
        jUnexpectedCharacterRule, //
    };

    jBnfProductionRules = new IRule[] {
        jWhitespaceRule, //
        // MMa : 1 line added
        new SimpleRule("!", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        new SimpleRule(":", jTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        jJavaCodeAltRule, //
    };

    jBnfChoiceRules = new IRule[] {
        jWhitespaceRule, //
        // new JJSimpleRule(",", fTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        jBnfPunctuationRule, //
        jNumericRule, //
        jStringRule, //
        jUnexpectedCharacterRule, //
    };

    jWhitespaceRules = new IRule[] {
        jWhitespaceRule, //
        jUnexpectedCharacterRule, //
    };

    jJjtreeNodeRules = new IRule[] {
        jWhitespaceRule, //
        new SimpleRule("(", jTokenMap.get(P_JJTREE_NODE_EXPR_PAREN)), //$NON-NLS-1$ 
        new SimpleRule(")", jTokenMap.get(P_JJTREE_NODE_EXPR_PAREN)), //$NON-NLS-1$ 
        new WordRule(new WordDetector(), jTokenMap.get(P_JJTREE_NODE_NAME)), //        
        jUnexpectedCharacterRule, //                                   
    };

    // Update the Java code rules
    jJavaCodeRule.updateRules(preferenceName);
    jJavaCodeAltRule.updateRules(preferenceName);
  }

  // ------------------ FSM begins here ----------------------------------------

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    jScanner = (CodeScanner) scanner;

    while (!isAtEOF() && !jStateStack.empty()) {

      switch (jStateStack.peek()) {

        case AT_NEXT_PRODUCTION: {

          if (isAt("options") && nextDelim() == '{') { //$NON-NLS-1$
            pushState(Context.AT_OPTIONS_BLOCK);
            return nextToken(jOptionBlockRules);
          }
          if (isAt("PARSER_BEGIN", true)) { //$NON-NLS-1$
            pushState(Context.AFTER_PARSER_BEGIN_KEYWORD);
            return nextToken(P_JAVACC_KEYWORD);
          }
          if (isAt("JAVACODE", true)) { //$NON-NLS-1$
            pushState(Context.AT_JAVACODE_PRODUCTION);
            return nextToken(P_JAVACC_KEYWORD);
          }
          if (isAt("TOKEN_MGR_DECLS", true)) { //$NON-NLS-1$
            pushState(Context.AT_TOKEN_MANAGER_DECLS_PRODUCTION);
            return nextToken(P_JAVACC_KEYWORD);
          }
          if (isAt("<") // //$NON-NLS-1$
              || isAt("TOKEN") || isAt("SPECIAL_TOKEN") // //$NON-NLS-1$ //$NON-NLS-2$ 
              || isAt("SKIP") || isAt("MORE")) { //$NON-NLS-1$ //$NON-NLS-2$ 
            pushState(Context.AT_REGULAR_EXPRESSION_PRODUCTION);
            continue;
          }
          if (isAtIdentifier()) {
            pushState(Context.AT_BNF_PRODUCTION);
            continue;
          }
          return nextToken(jWhitespaceRules);
        }

        case AT_OPTIONS_BLOCK: {
          if (isAt("}")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jOptionBlockRules);
        }

        case AFTER_PARSER_BEGIN_KEYWORD: {
          if (isAt(")")) { //$NON-NLS-1$
            resetState(Context.IN_JAVA_COMPILATION_UNIT);
          }
          return nextToken(jParserNameRules);
        }

        case IN_JAVA_COMPILATION_UNIT: {
          if (isAt("PARSER_END", true)) { //$NON-NLS-1$
            resetState(Context.AT_PARSER_END_KEYWORD);
            return nextToken(P_JAVACC_KEYWORD);
          }
          return nextToken(jJavaCodeRule);
        }

        case AT_PARSER_END_KEYWORD: {
          if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jParserNameRules);
        }

        case AT_JAVACODE_PRODUCTION: {
          if (isAt("{")) { //$NON-NLS-1$
            resetState(Context.IN_JAVA_CODE);
          }
          return nextToken(jJavaCodeRule);
        }

        case IN_JAVA_CODE: {
          if (isAt("{")) { //$NON-NLS-1$
            pushState(Context.IN_JAVA_CODE);
          }
          else if (isAt("}")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jJavaCodeRule);
        }

        case AT_TOKEN_MANAGER_DECLS_PRODUCTION: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_JAVA_BLOCK);
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          return nextToken(jTokenMgrDeclsRules);
        }

        case AT_JAVA_BLOCK: {
          if (isAt("}", true)) { //$NON-NLS-1$
            popState();
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          if (isAt("{")) { //$NON-NLS-1$
            pushState(Context.IN_JAVA_CODE);
          }
          return nextToken(jJavaCodeRule);
        }

        case AT_REGULAR_EXPRESSION_PRODUCTION: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_REGULAR_EXPRESSION_SPECIFICATION);
            return nextToken(P_REG_EX_BRACE);
          }
          return nextToken(jRegularExprProductionRules);
        }

        case AT_REGULAR_EXPRESSION_SPECIFICATION: {
          if (isAt("<", true)) { //$NON-NLS-1$
            if (nextDelim() == '>') {
              pushState(Context.AT_REGULAR_EXPRESSION_LABEL);
              return nextToken(P_TOKEN_LABEL_PUNCT);
            }
            pushState(Context.AT_REGULAR_EXPRESSION);
            return nextToken(P_REG_EX_BRACKET);
          }
          if (isAt(">", true)) { //$NON-NLS-1$
            return nextToken(P_REG_EX_BRACKET);
          }
          if (isAt("{", true)) { //$NON-NLS-1$
            pushState(Context.AT_JAVA_BLOCK);
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          if (isAt("}", true)) { //$NON-NLS-1$
            popState();
            return nextToken(P_REG_EX_BRACE);
          }
          return nextToken(jRegExprSpecRules);
        }

        case AT_REGULAR_EXPRESSION: {
          if (isAtIdentifier()) {
            if (jPrivateName) {
              jPrivateName = false;
              return nextToken(jPrivateLexicalDefinitionNameRule);
            }
            if (nextDelim() == ':') {
              return nextToken(jLexicalDefinitionNameRule);
            }
          }
          if (isAt(">")) { //$NON-NLS-1$
            popState();
            continue;
          }
          if (isAt("#")) { //$NON-NLS-1$
            jPrivateName = true;
          }
          else if (isAt("<")) { //$NON-NLS-1$
            pushState(Context.AT_REGULAR_EXPRESSION_LABEL);
          }
          return nextToken(jRegularExpressionRules);
        }

        case AT_REGULAR_EXPRESSION_LABEL: {
          if (isAt(">")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jRegularExpressionRules);
        }

        case AT_BNF_PRODUCTION: {
          if (isAt("#", true)) { //$NON-NLS-1$
            pushState(Context.AT_JJTREE_NODE_NAME);
            return nextToken(P_JJTREE_NODE_NAME_PUNCT);
          }

          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_EXPANSION_CHOICE_BLOCK);
            pushState(Context.AT_JAVA_BLOCK);
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          return nextToken(jBnfProductionRules);
        }

        case AT_EXPANSION_CHOICE_BLOCK: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_EXPANSION_CHOICES);
            jArgList = false;
            return nextToken(P_JAVACC_EXPANSION_BRACE);
          }
          return nextToken(jWhitespaceRules);
        }

        case AT_EXPANSION_CHOICES: {
          if (isAt("{", true)) { //$NON-NLS-1$
            pushState(Context.AT_JAVA_BLOCK);
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          if (isAt("}", true)) { //$NON-NLS-1$
            popState();
            return nextToken(P_JAVACC_EXPANSION_BRACE);
          }
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.AT_EXPANSION_CHOICES);
            if (jArgList) {
              jArgList = false;
              resetState(Context.AFTER_BNF_JAVA_RIGHT_PAREN);
              pushState(Context.AT_BNF_JAVA_LEFT_PAREN);
              return nextToken(jJavaCodeAltRule);
            }
            return nextToken(jBnfChoiceRules);
          }
          if (isAt(")", true)) { //$NON-NLS-1$
            popState();
            return nextToken(P_JAVACC_CHOICE_PUNCT);
          }
          if (isAt("<", true)) { //$NON-NLS-1$
            if (nextDelim() == '>') {
              pushState(Context.AT_REGULAR_EXPRESSION_LABEL);
              return nextToken(P_TOKEN_LABEL_PUNCT);
            }
            pushState(Context.AT_REGULAR_EXPRESSION);
            return nextToken(P_REG_EX_BRACKET);
          }
          if (isAt("#", true)) { //$NON-NLS-1$
            pushState(Context.AT_JJTREE_NODE_NAME);
            return nextToken(P_JJTREE_NODE_NAME_PUNCT);
          }
          if (isAt("LOOKAHEAD", true)) { //$NON-NLS-1$
            pushState(Context.AT_BNF_LOOKAHEAD);
            return nextToken(P_JAVACC_KEYWORD);
          }

          if (isAt("try")) { //$NON-NLS-1$
            pushState(Context.AT_EXPANSION_CHOICE_BLOCK);
            return nextToken(jJavaCodeAltRule);
          }

          if (isAtIdentifier()) {
            if (nextDelim() == '(') {
              jArgList = true;
            }
            return nextToken(jJavaCodeAltRule);
          }
          if (isAt("=")) { //$NON-NLS-1$
            jArgList = false;
            return nextToken(jJavaCodeAltRule);
          }
          return nextToken(jBnfChoiceRules);
        }

        case AT_BNF_JAVA_LEFT_PAREN: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.AT_BNF_JAVA_LEFT_PAREN);
          }
          else if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jJavaCodeAltRule);
        }

        case AFTER_BNF_JAVA_RIGHT_PAREN: {
          popState();
          continue;
        }

        case AT_BNF_LOOKAHEAD: {
          if (isAt("(", true)) { //$NON-NLS-1$
            resetState(Context.AFTER_BNF_JAVA_RIGHT_PAREN);
            pushState(Context.AT_EXPANSION_CHOICES);
            return nextToken(P_JAVACC_CHOICE_PUNCT);
          }
          return nextToken(jWhitespaceRules);
        }

        case AT_JJTREE_NODE_NAME: {
          if (isAtIdentifier()) {
            resetState(Context.AFTER_JJTREE_NODE_NAME);
            if (nextDelim() != '(') {
              popState();
            }
          }
          else if (isAt("(")) { //$NON-NLS-1$
            resetState(Context.IN_JJTREE_EXPR);
          }
          return nextToken(jJjtreeNodeRules);
        }

        case AFTER_JJTREE_NODE_NAME: {
          if (isAtIdentifier() || nextDelim() != '(') {
            popState();
            continue;
          }
          if (isAt("(")) { //$NON-NLS-1$
            resetState(Context.IN_JJTREE_EXPR);
          }
          return nextToken(jJjtreeNodeRules);
        }

        case IN_JJTREE_EXPR: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.IN_JJTREE_EXPR_NEST);
          }
          if (isAt(")")) { //$NON-NLS-1$
            popState();
            return nextToken(jJjtreeNodeRules);
          }
          return nextToken(jJavaCodeRule);
        }

        case IN_JJTREE_EXPR_NEST: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.IN_JJTREE_EXPR_NEST);
          }
          if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(jJavaCodeRule);
        }

      }
    }
    return Token.EOF;
  }

  // ----------------------- utility methods -----------------------------------

  /**
   * Checks if is at a specified string.
   * 
   * @param text - the text
   * @param noUnreadIfFound - the do not unread if found flag
   * @return true, if is at the specified string
   */
  private boolean isAt(final String text, final boolean noUnreadIfFound) {
    int index = 0;
    int c = 0;

    final int textLen = text.length();
    for (c = jScanner.read(); index < textLen; c = jScanner.read(), index++) {
      if ((char) c != text.charAt(index)) {
        break;
      }
    }
    jScanner.unread();

    final boolean result = (index == textLen);
    if (result && noUnreadIfFound) {
      return result;
    }

    for (; index > 0; index--) {
      jScanner.unread();
    }
    return result;
  }

  /**
   * Checks if is at a specified string.
   * 
   * @param text - the text
   * @return true, if is at the specified string
   */
  private boolean isAt(final String text) {
    return isAt(text, false);
  }

  /**
   * Checks if is at an identifier.
   * 
   * @return true, if is at an identifier
   */
  private boolean isAtIdentifier() {
    final char ch = (char) jScanner.read();
    jScanner.unread();
    return Character.isJavaIdentifierStart(ch);
  }

  /**
   * Checks if is at EOF.
   * 
   * @return true, if is at EOF
   */
  private boolean isAtEOF() {
    final int c = jScanner.read();
    jScanner.unread();
    return c == ICharacterScanner.EOF;
  }

  /**
   * Looks ahead and returns the next delimiter character.
   * <p>
   * A delimiter character is not an identifier character or whitespace.
   * 
   * @return the char
   */
  private char nextDelim() {
    char ch = (char) jScanner.read();
    int count = 1;
    while (Character.isWhitespace(ch) || Character.isJavaIdentifierPart(ch)) {
      ch = (char) jScanner.read();
      count++;
    }
    for (; count > 0; count--) {
      jScanner.unread();
    }
    return ch;
  }

  // --------------- Convenience methods ---------------------------------------

  /**
   * Resets the current state.
   * 
   * @param state - the new current state
   */
  private void resetState(final Context state) {
    popState();
    pushState(state);
  }

  /**
   * Pushes the current state.
   * 
   * @param state - the state to be pushed
   */
  private void pushState(final Context state) {
    jStateStack.push(state);
  }

  /**
   * Pops the current state.
   */
  private void popState() {
    jStateStack.pop();
  }

  /**
   * Evaluates a rule and return the token.
   * 
   * @param rule - the rule
   * @return the token for the rule
   */
  private IToken nextToken(final IRule rule) {
    return jScanner.nextToken(rule);
  }

  /**
   * Evaluates a set of rules and return the token.
   * 
   * @param rules - the rules
   * @return the token for the rules
   */
  private IToken nextToken(final IRule[] rules) {
    return jScanner.nextToken(rules);
  }

  /**
   * Returns the token for a specified preference.
   * 
   * @param preference - the preference
   * @return the token for the preference
   */
  private IToken nextToken(final String preference) {
    return jTokenMap.get(preference);
  }

  //-------------------------- Static constants --------------------------------

  /** The Context (state) enumeration */
  protected static enum Context {

    /** At options block state */
    AT_OPTIONS_BLOCK, //

    /** At parser begin keyword state */
    AFTER_PARSER_BEGIN_KEYWORD, //

    /** In Java compilation unit state */
    IN_JAVA_COMPILATION_UNIT, //

    /** At parser end keyword state */
    AT_PARSER_END_KEYWORD, //

    /** At next production state */
    AT_NEXT_PRODUCTION, //

    /** At JAVACODE production state */
    AT_JAVACODE_PRODUCTION, //

    /** At TOKEN MANAGER DECLS production state */
    AT_TOKEN_MANAGER_DECLS_PRODUCTION, //

    /** At Java block state */
    AT_JAVA_BLOCK, //

    /** In Java code state */
    IN_JAVA_CODE, //

    /** At regular expression production state */
    AT_REGULAR_EXPRESSION_PRODUCTION, //

    /** At regular expression specification state */
    AT_REGULAR_EXPRESSION_SPECIFICATION, //

    /** At regular expression state */
    AT_REGULAR_EXPRESSION, //

    /** At regular expression label */
    AT_REGULAR_EXPRESSION_LABEL, //

    /** At BNF production state */
    AT_BNF_PRODUCTION, //

    /** At expansion choice block state */
    AT_EXPANSION_CHOICE_BLOCK, //

    /** At expansion choices state */
    AT_EXPANSION_CHOICES, //

    /** At BNF lookahead state */
    AT_BNF_LOOKAHEAD, //

    /** At BNF Java left parenthesis state */
    AT_BNF_JAVA_LEFT_PAREN, //

    /** At BNF Java post parenthesis state */
    AFTER_BNF_JAVA_RIGHT_PAREN, //

    /** At JJTree node name state */
    AT_JJTREE_NODE_NAME, //

    /** After JJTree node name state */
    AFTER_JJTREE_NODE_NAME, // 

    /** After JJTree nested expression state */
    IN_JJTREE_EXPR_NEST, //

    /** In JJTree node description expression */
    IN_JJTREE_EXPR, //
  }

  /** The JavaCC color preference names */
  protected static String[]       sJavaCCColorPrefNames  = {
      // Order significant, matching pairs
      P_JAVACC_KEYWORD, //
      P_JAVACC_EXPANSION_BRACE, //
      P_JAVACC_CHOICE_PUNCT, //
      P_JAVACC_NUMERIC, //
      P_JAVACC_OPTION, //
      P_JAVACC_OPTION_BRACE, //
      P_JAVACC_PARSER_NAME, //
      P_JAVACC_PARSER_NAME_PAREN, //
      P_JAVACC_OTHER_PUNCT, //
      P_JAVACC_DEFAULT_TEXT, //
      P_JAVA_BLOCK_BRACE, //
      P_TOKEN_LABEL, //
      P_TOKEN_LABEL_PUNCT, //
      P_TOKEN_LABEL_DEF, //
      P_TOKEN_LABEL_PRIVATE_DEF, //
      P_TOKEN_LABEL_PRIVATE_DEF_PUNCT, //
      P_LEXICAL_STATE, //
      P_LEXICAL_STATE_PUNCT, //
      P_LEXICAL_STATE_NEXT, //
      P_REG_EX_BRACE, //
      P_JAVACC_STRING, //
      P_REG_EX_BRACKET, //
      P_REG_EX_TOKEN_PUNCT, //
      P_REG_EX_CHOICE_PUNCT, //
      P_REG_EX_OTHER_PUNCT, //
      P_JJTREE_NODE_NAME_PUNCT, //
      P_JJTREE_NODE_NAME, //
      P_JJTREE_NODE_EXPR_PAREN, //
                                                         };

  /** The JavaCC font attribute preference names */
  protected static String[]       sJavaCCAtrPrefNames    = {
      // Order significant, matching pairs
      P_JAVACC_KEYWORD_ATR, //
      P_JAVACC_EXPANSION_BRACE_ATR, //
      P_JAVACC_CHOICE_PUNCT_ATR, //
      P_JAVACC_NUMERIC_ATR, //
      P_JAVACC_OPTION_ATR, //
      P_JAVACC_OPTION_BRACE_ATR, //
      P_JAVACC_PARSER_NAME_ATR, //
      P_JAVACC_PARSER_NAME_PAREN_ATR, //
      P_JAVACC_OTHER_PUNCT_ATR, //
      P_JAVACC_DEFAULT_TEXT_ATR, //
      P_JAVA_BLOCK_BRACE_ATR, //
      P_TOKEN_LABEL_ATR, //
      P_TOKEN_LABEL_PUNCT_ATR, //
      P_TOKEN_LABEL_DEF_ATR, //
      P_TOKEN_LABEL_PRIVATE_DEF_ATR, //
      P_TOKEN_LABEL_PRIVATE_DEF_PUNCT_ATR, //
      P_LEXICAL_STATE_ATR, //
      P_LEXICAL_STATE_PUNCT_ATR, //
      P_LEXICAL_STATE_NEXT_ATR, //
      P_REG_EX_BRACE_ATR, //
      P_JAVACC_STRING_ATR, //
      P_REG_EX_BRACKET_ATR, //
      P_REG_EX_TOKEN_PUNCT_ATR, //
      P_REG_EX_CHOICE_PUNCT_ATR, //
      P_REG_EX_OTHER_PUNCT_ATR, //
      P_JJTREE_NODE_NAME_PUNCT_ATR, //
      P_JJTREE_NODE_NAME_ATR, //
      P_JJTREE_NODE_EXPR_PAREN_ATR, //
                                                         };

  /** Punctuation characters */
  protected static String[]       sPunctuationChars      = {
      // Order significant, matching pairs
      "{", //$NON-NLS-1$
      "}", //$NON-NLS-1$
      "[", //$NON-NLS-1$
      "]", //$NON-NLS-1$
      "<", //$NON-NLS-1$
      ">", //$NON-NLS-1$
      "(", //$NON-NLS-1$
      "|", //$NON-NLS-1$
      ")", //$NON-NLS-1$
      "*", //$NON-NLS-1$
      "+", //$NON-NLS-1$
      "?", //$NON-NLS-1$
      "-", //$NON-NLS-1$
      "~", //$NON-NLS-1$
      ":", //$NON-NLS-1$
      ",", //$NON-NLS-1$
      "#", //$NON-NLS-1$
      "!", //$NON-NLS-1$
                                                         };
  // "=", //$NON-NLS-1$
  // ";", //$NON-NLS-1$
  // ".", //$NON-NLS-1$
  // "/", //$NON-NLS-1$
  // "\\", //$NON-NLS-1$
  // "&", //$NON-NLS-1$
  // "^", //$NON-NLS-1$
  // "%", //$NON-NLS-1$
  // "$", //$NON-NLS-1$
  // "_", //$NON-NLS-1$
  // "'", //$NON-NLS-1$
  // "\"", //$NON-NLS-1$
  // "@", //$NON-NLS-1$
  // "`", //$NON-NLS-1$

  /** Regular expression punctuation preferences */
  protected static String[]       sRegexPunctuationPrefs = {
      // Order significant, matching pairs
      P_REG_EX_CHOICE_PUNCT, //       {  as in {1,2}
      P_REG_EX_CHOICE_PUNCT, //       }
      P_REG_EX_TOKEN_PUNCT, //        [
      P_REG_EX_TOKEN_PUNCT, //        ]
      P_TOKEN_LABEL_PUNCT, //         < 
      P_TOKEN_LABEL_PUNCT, //         >
      P_REG_EX_CHOICE_PUNCT, //       (
      P_REG_EX_CHOICE_PUNCT, //       |
      P_REG_EX_CHOICE_PUNCT, //       )
      P_REG_EX_CHOICE_PUNCT, //       *
      P_REG_EX_CHOICE_PUNCT, //       +
      P_REG_EX_CHOICE_PUNCT, //       ?
      P_REG_EX_TOKEN_PUNCT, //        -
      P_REG_EX_TOKEN_PUNCT, //        ~
      P_REG_EX_OTHER_PUNCT, //        :
      P_REG_EX_OTHER_PUNCT, //        ,
      P_TOKEN_LABEL_PRIVATE_DEF_PUNCT, // #
      P_REG_EX_OTHER_PUNCT, //        !
                                                         };

  /** BNF expansion punctuation preferences */
  protected static String[]       sBnfPunctuationPrefs   = {
      // Order significant, matching pairs
      P_JAVACC_EXPANSION_BRACE, //    {
      P_JAVACC_EXPANSION_BRACE, //    }
      P_JAVACC_CHOICE_PUNCT, //       [
      P_JAVACC_CHOICE_PUNCT, //       ]
      P_JAVACC_OTHER_PUNCT, //        <
      P_JAVACC_OTHER_PUNCT, //        >
      P_JAVACC_CHOICE_PUNCT, //       (
      P_JAVACC_CHOICE_PUNCT, //       |
      P_JAVACC_CHOICE_PUNCT, //       )
      P_JAVACC_CHOICE_PUNCT, //       *
      P_JAVACC_CHOICE_PUNCT, //       +
      P_JAVACC_CHOICE_PUNCT, //       ?
      P_JAVACC_OTHER_PUNCT, //        -
      P_JAVACC_OTHER_PUNCT, //        ~
      P_JAVACC_OTHER_PUNCT, //        :
      P_JAVACC_OTHER_PUNCT, //        ,
      P_JAVACC_OTHER_PUNCT, //        #
      P_JAVACC_OTHER_PUNCT, //        !
                                                         };

  /** The JavaCC Options keywords */
  protected static final String[] sJjOptionsKeywords     = {
      "BUILD_PARSER", //$NON-NLS-1$
      "BUILD_TOKEN_MANAGER", //$NON-NLS-1$
      "CACHE_TOKENS", //$NON-NLS-1$
      "CHOICE_AMBIGUITY_CHECK", //$NON-NLS-1$
      "COMMON_TOKEN_ACTION", //$NON-NLS-1$
      "DEBUG_LOOKAHEAD", //$NON-NLS-1$
      "DEBUG_PARSER", //$NON-NLS-1$
      "DEBUG_TOKEN_MANAGER", //$NON-NLS-1$
      "ERROR_REPORTING", //$NON-NLS-1$
      "FORCE_LA_CHECK", //$NON-NLS-1$
      "GENERATE_ANNOTATIONS", //$NON-NLS-1$   undocumented
      "GENERATE_CHAINED_EXCEPTION", //$NON-NLS-1$   undocumented
      "GENERATE_GENERICS", //$NON-NLS-1$   undocumented
      "GENERATE_STRING_BUILDER", //$NON-NLS-1$   undocumented
      "GRAMMAR_ENCODING", //$NON-NLS-1$   undocumented
      "IGNORE_CASE", //$NON-NLS-1$
      "JAVA_UNICODE_ESCAPE", //$NON-NLS-1$
      "JDK_VERSION", //$NON-NLS-1$   undocumented
      "KEEP_LINE_COLUMN", //$NON-NLS-1$   undocumented
      "LOOKAHEAD", //$NON-NLS-1$
      "OTHER_AMBIGUITY_CHECK", //$NON-NLS-1$
      "OUTPUT_DIRECTORY", //$NON-NLS-1$
      "OUTPUT_LANGUAGE", //$NON-NLS-1$
      "SANITY_CHECK", //$NON-NLS-1$
      "STATIC", //$NON-NLS-1$
      "SUPPORT_CLASS_VISIBILITY_PUBLIC", //$NON-NLS-1$
      "TOKEN_EXTENDS", //$NON-NLS-1$
      "TOKEN_FACTORY", //$NON-NLS-1$
      "TOKEN_MANAGER_USES_PARSER", //$NON-NLS-1$
      "UNICODE_INPUT", //$NON-NLS-1$
      "USER_CHAR_STREAM", //$NON-NLS-1$
      "USER_TOKEN_MANAGER", //$NON-NLS-1$
                                                         };

  /** The JJTree Options keywords */
  protected static final String[] sJjtOptionsKeywords    = {
      "BUILD_NODE_FILES", //$NON-NLS-1$
      "JJTREE_OUTPUT_DIRECTORY", //$NON-NLS-1$
      "MULTI", //$NON-NLS-1$
      "NODE_CLASS", //$NON-NLS-1$
      "NODE_DEFAULT_VOID", //$NON-NLS-1$
      "NODE_EXTENDS", //$NON-NLS-1$
      "NODE_FACTORY", //$NON-NLS-1$
      "NODE_PACKAGE", //$NON-NLS-1$
      "NODE_PREFIX", //$NON-NLS-1$
      "NODE_SCOPE_HOOK", //$NON-NLS-1$
      "NODE_USES_PARSER", //$NON-NLS-1$
      "OUTPUT_FILE", //$NON-NLS-1$   undocumented
      "OUTPUT_LANGUAGE", //$NON-NLS-1$
      "TRACK_TOKENS", //$NON-NLS-1$
      "VISITOR", //$NON-NLS-1$
      "VISITOR_DATA_TYPE", //$NON-NLS-1$
      "VISITOR_EXCEPTION", //$NON-NLS-1$
      "VISITOR_RETURN_TYPE", //$NON-NLS-1$
                                                         };

  /** The JTB options keywords */
  protected static final String[] sJtbOptionsKeywords    = {
      //      JTB keywords
      "JTB_CL", //$NON-NLS-1$
      "JTB_D", //$NON-NLS-1$
      "JTB_DL", //$NON-NLS-1$
      "JTB_E", //$NON-NLS-1$
      "JTB_F", //$NON-NLS-1$
      "JTB_IA", //$NON-NLS-1$
      "JTB_JD", //$NON-NLS-1$
      "JTB_ND", //$NON-NLS-1$
      "JTB_NP", //$NON-NLS-1$
      "JTB_NPFX", //$NON-NLS-1$
      "JTB_NSFX", //$NON-NLS-1$
      "JTB_NS", //$NON-NLS-1$
      "JTB_O", //$NON-NLS-1$
      "JTB_P", //$NON-NLS-1$
      "JTB_PP", //$NON-NLS-1$
      "JTB_PRINTER", //$NON-NLS-1$
      "JTB_SCHEME", //$NON-NLS-1$
      "JTB_TK", //$NON-NLS-1$
      "JTB_VA", //$NON-NLS-1$
      "JTB_VD", //$NON-NLS-1$
      "JTB_VP", //$NON-NLS-1$
      "JTB_W", //$NON-NLS-1$
               //      "STATIC", //$NON-NLS-1$ (duplicated)
                                                         };

  /** The jjdoc options (not used) */
  protected static final String[] sJjdocOptionsKeywords  = {
      "BNF", //$NON-NLS-1$
      "CSS", //$NON-NLS-1$
      "OUTPUT_FILE", //$NON-NLS-1$ (duplicated from JJOptionsKeywords)
      "ONE_TABLE", //$NON-NLS-1$
      "TEXT", //$NON-NLS-1$
                                                         };

  /** The JavaCC reserved keywords */
  protected static final String[] sJavaCCKeywords        = {
      // "options", //$NON-NLS-1$ not considered as a JavaCC keyword
      "EOF", //$NON-NLS-1$
      "IGNORE_CASE", //$NON-NLS-1$
      "JAVACODE", //$NON-NLS-1$
      "LOOKAHEAD", //$NON-NLS-1$
      "MORE", //$NON-NLS-1$
      "PARSER_BEGIN", //$NON-NLS-1$
      "PARSER_END", //$NON-NLS-1$
      "SKIP", //$NON-NLS-1$
      "SPECIAL_TOKEN", //$NON-NLS-1$
      "TOKEN", //$NON-NLS-1$
      "TOKEN_MGR_DECLS", //$NON-NLS-1$
                                                         };

}
