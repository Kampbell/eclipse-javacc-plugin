package sf.eclipse.javacc.scanners;

import java.util.HashMap;
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

import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.preferences.IPrefConstants;

/**
 * The JJJavaCCCodeRule Class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJJavaCCCodeRule implements IRule, IPrefConstants {

  // BF  05/2012 : created

  /** The preference store */
  private final IPreferenceStore         preferenceStore;

  /** The color map */
  private final HashMap<String, Color>   fColorMap;

  /** The attribute map */
  private final HashMap<String, Integer> fAtrMap;

  /** The token map */
  private final HashMap<String, IToken>  fTokenMap;

  /** The Java code rule */
  private final JJJavaCodeRule           javaCodeRule;

  /** The alternate Java code rule */
  private final JJJavaCodeRule           javaCodeAltRule;

  /** The JavaCC keyword rule */
  private WordRule                       javaCCKeywordRule;

  /** The options keyword rule */
  private WordRule                       optionsKeywordRule;

  /** The string rule */
  private SingleLineRule                 stringRule;

  /** The numeric rule */
  private JJNumericLiteralRule           numericRule;

  /** The regular expression punctuation rule */
  private WordRule                       regExPunctuationRule;

  /** The lexical definition name rule */
  private WordRule                       lexicalDefinitionNameRule;

  /** The private lexical definition name rule */
  private WordRule                       privateLexicalDefinitionNameRule;

  /** The expansion choices punctuation rule */
  private WordRule                       bnfPunctuationRule;

  /** The whitespace rule */
  private WhitespaceRule                 whitespaceRule;

  /** The unexpected character rule */
  private JJSimpleRule                   unexpectedCharacterRule;

  /** The begin state rules array */
  private IRule[]                        optionBlockRules;

  /** The parser name rules array */
  private IRule[]                        parserNameRules;

  /** The TOKEN_MGR_DECLS rules array */
  private IRule[]                        tokenMgrDeclsRules;

  /** The regular expression production rules array */
  private IRule[]                        regExProductionRules;

  /** The regular expression specification rules array */
  private IRule[]                        regExSpecificationRules;

  /** The regular expression rules array */
  private IRule[]                        regExExpressionRules;

  /** The BNF production rules array */
  private IRule[]                        bnfProductionRules;

  /** The BNF expansion choice rules array */
  private IRule[]                        bnfChoiceRules;

  /** The unexpected character rules array */
  private IRule[]                        whitespaceRules;

  /** The JJTree node rules array */
  private IRule[]                        jjtreeNodeRules;

  /** The private name flag */
  private boolean                        privateName;

  /** The argument list flag */
  private boolean                        argList;

  /** The scanner */
  private JJCodeScanner                  fScanner;

  /** The state stack */
  private final Stack<Context>           fStateStack;

  /**
   * Instantiates a new JavaCC code rule.
   * 
   * @param colorMap - the HashMap for the colors
   * @param atrMap - the HashMap for the font attributes
   */
  public JJJavaCCCodeRule(final HashMap<String, Color> colorMap, final HashMap<String, Integer> atrMap) {
    fColorMap = colorMap;
    fAtrMap = atrMap;
    fTokenMap = new HashMap<String, IToken>();

    preferenceStore = Activator.getDefault().getPreferenceStore();

    javaCodeRule = new JJJavaCodeRule(colorMap, atrMap, P_JAVA_BACKGROUND);
    javaCodeAltRule = new JJJavaCodeRule(colorMap, atrMap, P_JAVACC_BACKGROUND);
    updateRules(null);

    fStateStack = new Stack<Context>();
    initialize();
  }

  /**
   * Initialize to color from the start of the file.
   */
  public void initialize() {
    fStateStack.clear();
    pushState(Context.AT_NEXT_PRODUCTION);
  }

  /**
   * Update the rules after a preference change.
   * 
   * @param preferenceName - the changed preference name
   */
  public void updateRules(final String preferenceName) {
    final boolean all = (preferenceName == null || preferenceName == P_JAVACC_BACKGROUND);
    final Color background = fColorMap.get(P_JAVACC_BACKGROUND);
    String prefName = null;

    if (all) {
      prefName = P_JAVACC_BACKGROUND;
      fTokenMap.put(prefName, new Token(new TextAttribute(null, background, SWT.NONE)));
    }

    for (int i = 0; i < javaCCColorPrefNames.length; i++) {
      if (all || preferenceName == javaCCColorPrefNames[i] || preferenceName == javaCCAtrPrefNames[i]) {
        prefName = javaCCColorPrefNames[i];
        fTokenMap.put(prefName,
                      new Token(new TextAttribute(fColorMap.get(prefName), background,
                                                  fAtrMap.get(javaCCAtrPrefNames[i]).intValue())));
        if (!all) {
          break;
        }
      }
    }

    // The java block braces use the java background color
    if (preferenceStore.getBoolean(P_JAVA_BLOCK_BRACE_ALT_BG)) {
      fTokenMap.put(P_JAVA_BLOCK_BRACE, new Token(new TextAttribute(fColorMap.get(P_JAVA_BLOCK_BRACE),
                                                                    fColorMap.get(P_JAVA_BACKGROUND),
                                                                    fAtrMap.get(P_JAVA_BLOCK_BRACE_ATR)
                                                                           .intValue())));
    }

    whitespaceRule = new WhitespaceRule(new JJWhitespaceDetector(), fTokenMap.get(P_JAVACC_BACKGROUND));
    stringRule = new SingleLineRule("\"", "\"", fTokenMap.get(P_JAVACC_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    numericRule = new JJNumericLiteralRule(fTokenMap.get(P_JAVACC_NUMERIC));
    unexpectedCharacterRule = new JJSimpleRule("", fTokenMap.get(P_JAVACC_DEFAULT_TEXT)); //$NON-NLS-1$ 

    lexicalDefinitionNameRule = new WordRule(new JJWordDetector(), fTokenMap.get(P_TOKEN_LABEL_DEF));
    privateLexicalDefinitionNameRule = new WordRule(new JJWordDetector(),
                                                    fTokenMap.get(P_TOKEN_LABEL_PRIVATE_DEF));

    javaCCKeywordRule = new WordRule(new JJWordDetector());
    for (final String key : sJavaCCKeywords) {
      javaCCKeywordRule.addWord(key, fTokenMap.get(P_JAVACC_KEYWORD));
    }

    regExPunctuationRule = new WordRule(new JJSingleCharDetector());
    for (int i = 0; i < punctuationChars.length; i++) {
      regExPunctuationRule.addWord(punctuationChars[i], fTokenMap.get(regexPunctuationPrefs[i]));
    }

    bnfPunctuationRule = new WordRule(new JJSingleCharDetector());
    for (int i = 0; i < punctuationChars.length; i++) {
      bnfPunctuationRule.addWord(punctuationChars[i], fTokenMap.get(bnfPunctuationPrefs[i]));
    }

    optionsKeywordRule = new WordRule(new JJWordDetector());
    for (final String opt : sOptionsKeywords) {
      optionsKeywordRule.addWord(opt, fTokenMap.get(P_JAVACC_OPTION));
    }
    for (final String opt : sJTBKeywords) {
      optionsKeywordRule.addWord(opt, fTokenMap.get(P_JAVACC_OPTION));
    }
    for (final String key : sJJdocKeywords) {
      optionsKeywordRule.addWord(key, fTokenMap.get(P_JAVACC_OPTION));
    }

    optionBlockRules = new IRule[] {
        whitespaceRule, // 
        new JJSimpleRule("options", fTokenMap.get(P_JAVACC_KEYWORD)), //$NON-NLS-1$ 
        new JJSimpleRule("{", fTokenMap.get(P_JAVACC_OPTION_BRACE)), //$NON-NLS-1$ 
        optionsKeywordRule, //
        new JJSimpleRule("}", fTokenMap.get(P_JAVACC_OPTION_BRACE)), //$NON-NLS-1$ 
        // new JJSimpleRule("=", fTokenMap.get(P_JAVACC_)), // Left as Java punctuation $NON-NLS-1$ 
        // new JJSimpleRule(";", fTokenMap.get(P_JAVACC_)), // Left as Java punctuation $NON-NLS-1$ 
        javaCodeAltRule, //
    };

    parserNameRules = new IRule[] {
        whitespaceRule, //
        new WordRule(new JJWordDetector(), fTokenMap.get(P_JAVACC_PARSER_NAME)), //
        new JJSimpleRule("(", fTokenMap.get(P_JAVACC_PARSER_NAME_PAREN)), //$NON-NLS-1$ 
        new JJSimpleRule(")", fTokenMap.get(P_JAVACC_PARSER_NAME_PAREN)), //$NON-NLS-1$ 
        unexpectedCharacterRule, //
    };

    tokenMgrDeclsRules = new IRule[] {
        whitespaceRule, //
        new JJSimpleRule(":", fTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        unexpectedCharacterRule, //
    };

    regExProductionRules = new IRule[] {
        whitespaceRule, //
        javaCCKeywordRule, //
        new WordRule(new JJWordDetector(), fTokenMap.get(P_LEXICAL_STATE)), //
        new JJSimpleRule("*", fTokenMap.get(P_LEXICAL_STATE)), //$NON-NLS-1$ 
        new JJSimpleRule("<", fTokenMap.get(P_LEXICAL_STATE_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule(">", fTokenMap.get(P_LEXICAL_STATE_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule("[", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule("]", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule(":", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule(",", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        unexpectedCharacterRule, //
    };

    regExSpecificationRules = new IRule[] {
        whitespaceRule, //
        new WordRule(new JJWordDetector(), fTokenMap.get(P_LEXICAL_STATE_NEXT)), //
        new JJSimpleRule("|", fTokenMap.get(P_REG_EX_CHOICE_PUNCT)), //$NON-NLS-1$ 
        new JJSimpleRule(":", fTokenMap.get(P_REG_EX_OTHER_PUNCT)), //$NON-NLS-1$ 
        stringRule, //
        unexpectedCharacterRule, //
    };

    regExExpressionRules = new IRule[] {
        whitespaceRule, //
        new JJSimpleRule("EOF", fTokenMap.get(P_JAVACC_KEYWORD)), //$NON-NLS-1$ 
        new WordRule(new JJWordDetector(), fTokenMap.get(P_TOKEN_LABEL)), //
        numericRule, //
        // new JJSimpleRule(":", fTokenMap.get(P_REGEX_OTHER_PUNCT)), //$NON-NLS-1$ 
        // new JJSimpleRule(",", fTokenMap.get(P_REGEX_OTHER_PUNCT)), //$NON-NLS-1$ 
        regExPunctuationRule, //
        stringRule, //
        unexpectedCharacterRule, //
    };

    bnfProductionRules = new IRule[] {
        whitespaceRule, //
        new JJSimpleRule(":", fTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        javaCodeAltRule, //
    };

    bnfChoiceRules = new IRule[] {
        whitespaceRule, //
        // new JJSimpleRule(",", fTokenMap.get(P_JAVACC_OTHER_PUNCT)), //$NON-NLS-1$ 
        bnfPunctuationRule, //
        numericRule, //
        stringRule, //
        unexpectedCharacterRule, //
    };

    whitespaceRules = new IRule[] {
        whitespaceRule, //
        unexpectedCharacterRule, //
    };

    jjtreeNodeRules = new IRule[] {
        whitespaceRule, //
        new JJSimpleRule("(", fTokenMap.get(P_JJTREE_NODE_EXPR_PAREN)), //$NON-NLS-1$ 
        new JJSimpleRule(")", fTokenMap.get(P_JJTREE_NODE_EXPR_PAREN)), //$NON-NLS-1$ 
        new WordRule(new JJWordDetector(), fTokenMap.get(P_JJTREE_NODE_NAME)), //        
        unexpectedCharacterRule, //                                   
    };

    // Update the Java code rules
    javaCodeRule.updateRules(preferenceName);
    javaCodeAltRule.updateRules(preferenceName);
  }

  // ------------------ FSM begins here ----------------------------------------

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    fScanner = (JJCodeScanner) scanner;

    while (!isAtEOF() && !fStateStack.empty()) {

      switch (fStateStack.peek()) {

        case AT_NEXT_PRODUCTION: {

          if (isAt("options") && nextDelim() == '{') { //$NON-NLS-1$
            pushState(Context.AT_OPTIONS_BLOCK);
            return nextToken(optionBlockRules);
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
          return nextToken(whitespaceRules);
        }

        case AT_OPTIONS_BLOCK: {
          if (isAt("}")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(optionBlockRules);
        }

        case AFTER_PARSER_BEGIN_KEYWORD: {
          if (isAt(")")) { //$NON-NLS-1$
            resetState(Context.IN_JAVA_COMPILATION_UNIT);
          }
          return nextToken(parserNameRules);
        }

        case IN_JAVA_COMPILATION_UNIT: {
          if (isAt("PARSER_END", true)) { //$NON-NLS-1$
            resetState(Context.AT_PARSER_END_KEYWORD);
            return nextToken(P_JAVACC_KEYWORD);
          }
          return nextToken(javaCodeRule);
        }

        case AT_PARSER_END_KEYWORD: {
          if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(parserNameRules);
        }

        case AT_JAVACODE_PRODUCTION: {
          if (isAt("{")) { //$NON-NLS-1$
            resetState(Context.IN_JAVA_CODE);
          }
          return nextToken(javaCodeRule);
        }

        case IN_JAVA_CODE: {
          if (isAt("{")) { //$NON-NLS-1$
            pushState(Context.IN_JAVA_CODE);
          }
          else if (isAt("}")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(javaCodeRule);
        }

        case AT_TOKEN_MANAGER_DECLS_PRODUCTION: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_JAVA_BLOCK);
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          return nextToken(tokenMgrDeclsRules);
        }

        case AT_JAVA_BLOCK: {
          if (isAt("}", true)) { //$NON-NLS-1$
            popState();
            return nextToken(P_JAVA_BLOCK_BRACE);
          }
          if (isAt("{")) { //$NON-NLS-1$
            pushState(Context.IN_JAVA_CODE);
          }
          return nextToken(javaCodeRule);
        }

        case AT_REGULAR_EXPRESSION_PRODUCTION: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_REGULAR_EXPRESSION_SPECIFICATION);
            return nextToken(P_REG_EX_BRACE);
          }
          return nextToken(regExProductionRules);
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
          return nextToken(regExSpecificationRules);
        }

        case AT_REGULAR_EXPRESSION: {
          if (isAtIdentifier()) {
            if (privateName) {
              privateName = false;
              return nextToken(privateLexicalDefinitionNameRule);
            }
            if (nextDelim() == ':') {
              return nextToken(lexicalDefinitionNameRule);
            }
          }
          if (isAt(">")) { //$NON-NLS-1$
            popState();
            continue;
          }
          if (isAt("#")) { //$NON-NLS-1$
            privateName = true;
          }
          else if (isAt("<")) { //$NON-NLS-1$
            pushState(Context.AT_REGULAR_EXPRESSION_LABEL);
          }
          return nextToken(regExExpressionRules);
        }

        case AT_REGULAR_EXPRESSION_LABEL: {
          if (isAt(">")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(regExExpressionRules);
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
          return nextToken(bnfProductionRules);
        }

        case AT_EXPANSION_CHOICE_BLOCK: {
          if (isAt("{", true)) { //$NON-NLS-1$
            resetState(Context.AT_EXPANSION_CHOICES);
            argList = false;
            return nextToken(P_JAVACC_EXPANSION_BRACE);
          }
          return nextToken(whitespaceRules);
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
            if (argList) {
              argList = false;
              resetState(Context.AFTER_BNF_JAVA_RIGHT_PAREN);
              pushState(Context.AT_BNF_JAVA_LEFT_PAREN);
              return nextToken(javaCodeAltRule);
            }
            return nextToken(bnfChoiceRules);
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
            return nextToken(javaCodeAltRule);
          }

          if (isAtIdentifier()) {
            if (nextDelim() == '(') {
              argList = true;
            }
            return nextToken(javaCodeAltRule);
          }
          if (isAt("=")) { //$NON-NLS-1$
            argList = false;
            return nextToken(javaCodeAltRule);
          }
          return nextToken(bnfChoiceRules);
        }

        case AT_BNF_JAVA_LEFT_PAREN: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.AT_BNF_JAVA_LEFT_PAREN);
          }
          else if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(javaCodeAltRule);
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
          return nextToken(whitespaceRules);
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
          return nextToken(jjtreeNodeRules);
        }

        case AFTER_JJTREE_NODE_NAME: {
          if (isAtIdentifier() || nextDelim() != '(') {
            popState();
            continue;
          }
          if (isAt("(")) { //$NON-NLS-1$
            resetState(Context.IN_JJTREE_EXPR);
          }
          return nextToken(jjtreeNodeRules);
        }

        case IN_JJTREE_EXPR: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.IN_JJTREE_EXPR_NEST);
          }
          if (isAt(")")) { //$NON-NLS-1$
            popState();
            return nextToken(jjtreeNodeRules);
          }
          return nextToken(javaCodeRule);
        }

        case IN_JJTREE_EXPR_NEST: {
          if (isAt("(")) { //$NON-NLS-1$
            pushState(Context.IN_JJTREE_EXPR_NEST);
          }
          if (isAt(")")) { //$NON-NLS-1$
            popState();
          }
          return nextToken(javaCodeRule);
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

    for (c = fScanner.read(); index < text.length(); c = fScanner.read(), index++) {
      if ((char) c != text.charAt(index)) {
        break;
      }
    }
    fScanner.unread();

    final boolean result = (index == text.length());
    if (result && noUnreadIfFound) {
      return result;
    }

    for (; index > 0; index--) {
      fScanner.unread();
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
    final char ch = (char) fScanner.read();
    fScanner.unread();
    return Character.isJavaIdentifierStart(ch);
  }

  /**
   * Checks if is at EOF.
   * 
   * @return true, if is at EOF
   */
  private boolean isAtEOF() {
    final int c = fScanner.read();
    fScanner.unread();
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
    char ch = (char) fScanner.read();
    int count = 1;
    while (Character.isWhitespace(ch) || Character.isJavaIdentifierPart(ch)) {
      ch = (char) fScanner.read();
      count++;
    }
    for (; count > 0; count--) {
      fScanner.unread();
    }
    return ch;
  }

  // --------------- Convenience methods ---------------------------------------

  /**
   * Reset the current state.
   * 
   * @param state - the new current state
   */
  private void resetState(final Context state) {
    popState();
    pushState(state);
  }

  /**
   * Push the current state.
   * 
   * @param state - the state to be pushed
   */
  private void pushState(final Context state) {
    fStateStack.push(state);
  }

  /**
   * Pop the current state.
   */
  private void popState() {
    fStateStack.pop();
  }

  /**
   * Evaluate a rule and return the token.
   * 
   * @param rule - the rule
   * @return the token for the rule
   */
  private IToken nextToken(final IRule rule) {
    return fScanner.nextToken(rule);
  }

  /**
   * Evaluate a set of rules and return the token.
   * 
   * @param rules - the rules
   * @return the token for the rules
   */
  private IToken nextToken(final IRule[] rules) {
    return fScanner.nextToken(rules);
  }

  /**
   * Return the token for a specified preference.
   * 
   * @param preference - the preference
   * @return the token for the preference
   */
  private IToken nextToken(final String preference) {
    return fTokenMap.get(preference);
  }

  //-------------------------- Static constants --------------------------------

  /** The Context (state) enumeration */
  private static enum Context {

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
  private static String[] javaCCColorPrefNames  = {
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
  private static String[] javaCCAtrPrefNames    = {
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
  private static String[] punctuationChars      = {
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
                                                };
  // "=", //$NON-NLS-1$
  // ";", //$NON-NLS-1$
  // ".", //$NON-NLS-1$
  // "/", //$NON-NLS-1$
  // "\\", //$NON-NLS-1$
  // "!", //$NON-NLS-1$
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
  private static String[] regexPunctuationPrefs = {
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
                                                };

  /** BNF expansion punctuation preferences */
  private static String[] bnfPunctuationPrefs   = {
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
                                                };

  /** The JavaCC reserved keywords */
  public static String[]  sJavaCCKeywords       = {
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

  /** The Options keywords */
  public static String[]  sOptionsKeywords      = {
      // JavaCC options
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
      "SANITY_CHECK", //$NON-NLS-1$
      "STATIC", //$NON-NLS-1$
      "SUPPORT_CLASS_VISIBILITY_PUBLIC", //$NON-NLS-1$
      "TOKEN_EXTENDS", //$NON-NLS-1$      
      "TOKEN_FACTORY", //$NON-NLS-1$     
      "TOKEN_MANAGER_USES_PARSER", //$NON-NLS-1$
      "UNICODE_INPUT", //$NON-NLS-1$
      "USER_CHAR_STREAM", //$NON-NLS-1$
      "USER_TOKEN_MANAGER", //$NON-NLS-1$

      // JJTree options
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
      "TRACK_TOKENS", //$NON-NLS-1$
      "VISITOR", //$NON-NLS-1$
      "VISITOR_DATA_TYPE", //$NON-NLS-1$
      "VISITOR_EXCEPTION", //$NON-NLS-1$
      "VISITOR_RETURN_TYPE", //$NON-NLS-1$
                                                };

  /** The JTB options */
  public static String[]  sJTBKeywords          = {
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
      "STATIC", //$NON-NLS-1$ (dup)
                                                };
  /** The jjdoc options */
  public static String[]  sJJdocKeywords        = {
      "BNF", //$NON-NLS-1$
      "CSS", //$NON-NLS-1$
      "EOL", //$NON-NLS-1$
      "OUTPUT_FILE", //$NON-NLS-1$ (dup)
      "ONE_TABLE", //$NON-NLS-1$
      "TEXT", //$NON-NLS-1$
                                                };

  /** The content assist keywords */
  public static String[]  sJavaCCAssistKeywords = {
      // Note: This table is used for content assist, left here for convenience                                                  
      "BNF", //$NON-NLS-1$ 
      "BUILD_NODE_FILES", //$NON-NLS-1$
      "BUILD_PARSER", //$NON-NLS-1$
      "BUILD_TOKEN_MANAGER", //$NON-NLS-1$
      "CACHE_TOKENS", //$NON-NLS-1$
      "CHOICE_AMBIGUITY_CHECK", //$NON-NLS-1$
      "COMMON_TOKEN_ACTION", //$NON-NLS-1$
      "CSS", //$NON-NLS-1$
      "DEBUG_LOOKAHEAD", //$NON-NLS-1$
      "DEBUG_PARSER", //$NON-NLS-1$
      "DEBUG_TOKEN_MANAGER", //$NON-NLS-1$
      "EOF", //$NON-NLS-1$
      "EOL", //$NON-NLS-1$   (why is this here? it is not a known keyword)
      "ERROR_REPORTING", //$NON-NLS-1$
      "FORCE_LA_CHECK", //$NON-NLS-1$
      "GENERATE_ANNOTATIONS", //$NON-NLS-1$
      "GENERATE_CHAINED_EXCEPTION", //$NON-NLS-1$
      "GENERATE_GENERICS", //$NON-NLS-1$
      "GENERATE_STRING_BUILDER", //$NON-NLS-1$
      "GRAMMAR_ENCODING", //$NON-NLS-1$
      "IGNORE_CASE", //$NON-NLS-1$
      "JAVACODE", //$NON-NLS-1$
      "JAVA_UNICODE_ESCAPE", //$NON-NLS-1$
      "JDK_VERSION", //$NON-NLS-1$
      "JJTREE_OUTPUT_DIRECTORY", //$NON-NLS-1$
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
      "KEEP_LINE_COLUMN", //$NON-NLS-1$
      "LOOKAHEAD", //$NON-NLS-1$
      "MORE", //$NON-NLS-1$
      "MULTI", //$NON-NLS-1$
      "NODE_CLASS", //$NON-NLS-1$
      "NODE_DEFAULT_VOID", //$NON-NLS-1$
      "NODE_EXTENDS", //$NON-NLS-1$
      "NODE_FACTORY", //$NON-NLS-1$
      "NODE_PACKAGE", //$NON-NLS-1$
      "NODE_PREFIX", //$NON-NLS-1$
      "NODE_SCOPE_HOOK", //$NON-NLS-1$
      "NODE_USES_PARSER", //$NON-NLS-1$
      "ONE_TABLE", //$NON-NLS-1$
      /* "options" is not an official JavaCC keyword *///
      "OTHER_AMBIGUITY_CHECK", //$NON-NLS-1$
      "OUTPUT_DIRECTORY", //$NON-NLS-1$
      "OUTPUT_FILE", //$NON-NLS-1$
      "PARSER_BEGIN", //$NON-NLS-1$
      "PARSER_END", //$NON-NLS-1$
      "SANITY_CHECK", //$NON-NLS-1$
      "SKIP", //$NON-NLS-1$
      "SPECIAL_TOKEN", //$NON-NLS-1$
      "STATIC", //$NON-NLS-1$
      "SUPPORT_CLASS_VISIBILITY_PUBLIC", //$NON-NLS-1$
      "TEXT", //$NON-NLS-1$
      "TOKEN", //$NON-NLS-1$
      "TOKEN_EXTENDS", //$NON-NLS-1$
      "TOKEN_FACTORY", //$NON-NLS-1$
      "TOKEN_MANAGER_USES_PARSER", //$NON-NLS-1$
      "TOKEN_MGR_DECLS", //$NON-NLS-1$
      "TRACK_TOKENS", //$NON-NLS-1$
      "UNICODE_INPUT", //$NON-NLS-1$
      "USER_CHAR_STREAM", //$NON-NLS-1$
      "USER_TOKEN_MANAGER", //$NON-NLS-1$
      "VISITOR", //$NON-NLS-1$
      "VISITOR_DATA_TYPE", //$NON-NLS-1$
      "VISITOR_EXCEPTION", //$NON-NLS-1$
      "VISITOR_RETURN_TYPE", //$NON-NLS-1$
                                                };
}
