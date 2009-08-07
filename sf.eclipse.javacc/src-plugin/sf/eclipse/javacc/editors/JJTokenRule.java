package sf.eclipse.javacc.editors;

import java.util.Stack;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * A special IRule for JavaCC syntax.
 *
 * @author Remi Koutcherawy 2003-2008 - CeCILL License http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
/*
 * ModMMa : modified : added different rule tokens and changed algorithm by keeping state between invocations
 */
public class JJTokenRule implements IRule {
  /** normal label identifier rule token */
  final IToken         normalLabel;
  /** special label identifier rule token */
  final IToken         privateLabel;
  /** lexical state rule token */
  final IToken         lexicalState;
/** regular_expression punctuation ('<', '>' and ':') rule token */
  final IToken         regexPunct;
  /**
   * choices enclosing punctuation ('(', ')', '*', '+' and '?') rule token (in complex_regular_expression_unit
   * and expansion_choices)
   */
  final IToken         choicesPunct;
  /** found previously a ':', to search a lexical state identifier (reset to false when not the case) */
  boolean              foundCOLON;
/** found previously a '<' (reset to false when found a matching '>' */
  boolean              foundLT;
  /** found previously a '#' (private label identifier prefix) (reset to false when found a ':' */
  boolean              foundNB;
  /** angle brackets nesting level */
  int                  angleBracketsLevel;
  /** braces nesting level */
  int                  bracesLevel;
  /** is after a label identifier in a regular_expression */
  boolean              isAfterLabelIdentifier;
  /** is after a Java identifier */
  boolean              isAfterJavaIdentifier;
  /** stack used to store same level parenthesis flag */
  final Stack<Boolean> parStack;

  /**
   * Standard constructor
   *
   * @param aNormalLabel the normal token rule token
   * @param aPrivateLabel the special token rule token
   * @param aLexicalState the lexical state rule token
   * @param aRegexPunct the regular_expression punctuation rule token
   * @param aChoicesPunct the choices enclosing punctuation rule token
   */
  public JJTokenRule(final IToken aNormalLabel, final IToken aPrivateLabel, final IToken aLexicalState, final IToken aRegexPunct, final IToken aChoicesPunct) {
    normalLabel = aNormalLabel;
    privateLabel = aPrivateLabel;
    lexicalState = aLexicalState;
    regexPunct = aRegexPunct;
    choicesPunct = aChoicesPunct;
    foundCOLON = false;
    foundLT = false;
    foundNB = false;
    angleBracketsLevel = 0;
    bracesLevel = 0;
    isAfterLabelIdentifier = false;
    isAfterJavaIdentifier = false;
    parStack = new Stack<Boolean>();
  }

  /**
   * Reinitializes state.
   */
  public void reinit() {
    foundCOLON = false;
    foundLT = false;
    foundNB = false;
    angleBracketsLevel = 0;
    bracesLevel = 0;
    isAfterLabelIdentifier = false;
    isAfterJavaIdentifier = false;
    parStack.clear();
  }

  /**
   * Evaluates the {@link IToken} rule token of the subsequent characters/tokens read from the given
   * {@link ICharacterScanner}.
   * <p>
   * {@link IRule#evaluate(ICharacterScanner)} is called repeatedly by {@link RuleBasedScanner#nextToken()}
   * (called itself by {@link DefaultDamagerRepairer#createPresentation(TextPresentation, ITypedRegion)} ) on
   * the different rules set in {@link JJCodeScanner#createRules()}.<br>
   * So, for {@link #evaluate(ICharacterScanner)}, scans begin with the first non whitespace character after a
   * comment or a string.<br>
   * Also, when modifying a document, {@link IRule#evaluate(ICharacterScanner)} is called only on the tokens
   * of the modified line.<br>
   * We memorize some internal state information between each call through class fields.<br>
   * We must return whitespaces and punctuation (word separators) as soon as they are encountered (and without
   * consuming them) in order for the other rules to process the comments anywhereby they may appear.
   *
   * @see IRule#evaluate(ICharacterScanner)
   * @param scanner the character scanner
   * @return the rule token
   */
  public IToken evaluate(final ICharacterScanner scanner) {
    /** found a normal label identifier */
    boolean isNoLa = false;
    /** found a private label identifier */
    boolean isPrLa = false;
    /** found a lexical state identifier after a ':' or in a lexical state list */
    boolean isLxSt = false;
    int ic = scanner.read();
    // returns initial whitespaces if any
    int w = 0;
    while (ic != ICharacterScanner.EOF) {
      if (!isWhitespace(ic)) {
        break;
      }
      ic = scanner.read();
      w++;
    }
    if (w > 0) {
      // while (w > 0) {
      scanner.unread();
      // w--;
      // }
      return Token.WHITESPACE;
    }
    if (ic == ICharacterScanner.EOF) {
      return Token.UNDEFINED;
    }
    // process first character if no initial whitespaces
    // TODO for generics syntax in java code parts, we should not show a regexPunct
    if (ic == '<') {
      incrementAngleBrackets();
      foundCOLON = false;
      return regexPunct;
    }
    if (ic == '>') {
      final boolean isInRegExp = foundLT;
      decrementAngleBrackets();
      if (isInRegExp) {
        // for regular_expression cases
        return regexPunct;
      }
      // for Greater-than nodes "(>1)" cases
      return Token.UNDEFINED;
    }
    if (ic == ':') {
      if (foundLT) {
        // ':' inside a '< ... >', so it is after a label identifier in a regular_expression,
        // so it cannot be before a lexical state identifier in a regexpr_spec
        isAfterLabelIdentifier = true;
        foundNB = false;
        return regexPunct;
      }
      // ':' not inside a '< ... >' (of a regular_expression),
      // so can be in a bnf_production, regular_expr_production, token_manager_decls, regexpr_spec only if
      // the braces level is 0 and can be before a lexical state identifier in a regexpr_spec only if
      // the braces level is 1, so ...
      if (bracesLevel == 1) {
        foundCOLON = true;
      }
      scanner.unread();
      return Token.UNDEFINED;
    }
    // other characters
    if (isChoicesPunct(ic)) {
      boolean oldIsAfterJavaIdentifier = isAfterJavaIdentifier;
      if (ic == '(') {
        parStack.push(Boolean.valueOf(isAfterJavaIdentifier));
      } else if (ic == ')') {
        if (!parStack.isEmpty()) {
          // this can occur for example when inserting before a ')' ...
          oldIsAfterJavaIdentifier = parStack.pop().booleanValue();
        }
      }
      isAfterJavaIdentifier = false;
      // choices enclosing punctuation
      // TODO after a try { in an expansion_unit, which leads to bracesLevel == 2,
      // we should show a choicesPunct (ex : JavaCC15.jjt, javacc_getAST())
      if (foundLT || (bracesLevel == 1 && !oldIsAfterJavaIdentifier)) {
        return choicesPunct;
      }
      // if not a choices enclosing punctuation
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (ic == '{') {
      bracesLevel++;
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (ic == '}') {
      bracesLevel--;
      isAfterJavaIdentifier = false;
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (ic == '|') {
      // some other punctuation
      isAfterJavaIdentifier = false;
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (ic == '[' || ic == ']' || ic == '~' || ic == '-' || ic == ',') {
      // some other punctuation
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (ic == '#') {
      if (foundLT) {
        // if before a private label identifier (may be whitespaces and comments between !)
        foundNB = true;
        return privateLabel;
      }
      // otherwise continue with the next characters (of a node descriptor expression)
    }
    if (!foundLT && !foundCOLON) {
      // other Java punctuation plus JavaCC and Java keywords
      isAfterJavaIdentifier = true;
      scanner.unread();
      return Token.UNDEFINED;
    }
    // here we are with fountLT or foundCOLON, process the character within the loop
    scanner.unread();
    // process next characters (can be if not JavaCC punctuation and if foundLT == true or foundCOLON == true)
    while ((ic = scanner.read()) != ICharacterScanner.EOF) {
      if (foundLT) {
        // case inside a '< ... >'
        if (isWhitespaceOrComment1stChar(ic)) {
          // stop if whitespaces or beginning of a comment
          scanner.unread();
          break;
        } else if (ic == '>' || ic == ',') {
          // case '>' and ','
          if (bracesLevel == 0) {
            // stop if we are in a lexical state list (not inside braces)
            scanner.unread();
            return lexicalState;
          }
          scanner.unread();
          break;
        } else if (isChoicesPunct(ic)) {
          // stop if reached choices enclosing punctuation
          scanner.unread();
          break;
        } else if (ic == '[' || ic == ']' || ic == '|' || ic == '~' || ic == ':' || ic == '-' || ic == '\"') {
          // stop if reached some other punctuation or the beginning of a string
          scanner.unread();
          break;
        } else {
          // other characters found : should be in a normal label identifier
          // if not in a private label identifier or in a lexical state list
          if (foundNB) {
            isPrLa = true;
          } else if (bracesLevel == 0) {
            isLxSt = true;
          } else {
            isNoLa = true;
          }
        }
      } else if (foundCOLON) {
        // case after a ':' in a regexpr_spec
        if (isWhitespaceOrComment1stChar(ic)) {
          // stop if whitespaces or beginning of a comment
          scanner.unread();
          break;
        } else if (ic == '}' || ic == '|') {
          // stop if reached a '}', '|', and terminate colon search
          // (must have encountered before a lexical state identifier, so isLxSt should be true)
          scanner.unread();
          foundCOLON = false;
          break;
        } else if (Character.isJavaIdentifierPart(ic) || ic == '*') {
          // java identifier characters or star found : should be the beginning of a lexical state identifier
          isAfterJavaIdentifier = true;
          isLxSt = true;
        }
      } // end else if (foundCOLON)
    } // end while ((ic = scanner.read()) != ICharacterScanner.EOF)
    if (ic == ICharacterScanner.EOF) {
      scanner.unread();
    }
    // order is important
    if (isPrLa) {
      return privateLabel;
    }
    if (isLxSt) {
      return lexicalState;
    }
    if (isNoLa) {
      return normalLabel;
    }
    return Token.UNDEFINED;
  }

  /**
   * Returns whether the given character is a whitespace:<br>
   * ' ', '\t', '\n', '\r', '\f'.
   *
   * @param ic the character
   * @return true if the caracter is a whitespace, false otherwise
   */
  public static boolean isWhitespace(final int ic) {
    return (ic == ' ' || (ic >= '\t' && ic <= '\r'));
  }

  /**
   * Returns whether the given character is a whitespace or the beginning of a comment:<br>
   * ' ', '\t', '\n', '\r', '\f', '/'.
   *
   * @param ic the character
   * @return true if the character is a whitespace or the beginning of a comment, false otherwise
   */
  public static boolean isWhitespaceOrComment1stChar(final int ic) {
    return (isWhitespace(ic) || ic == '/');
  }

  /**
   * Returns whether the given character is a choices enclosing punctuation:<br>
   * '(', ')', '*', '+', '?'.
   *
   * @param ic the character
   * @return true if the caracter is a choices enclosing punctuation, false otherwise
   */
  public static boolean isChoicesPunct(final int ic) {
    return ((ic >= '(' && ic <= '+') || ic == '?');
  }

  /**
   * Increments the angle brackets level and sets the corresponding flag.
   */
  void incrementAngleBrackets() {
    foundLT = true;
    angleBracketsLevel++;
  }

  /**
   * Decrements the angle brackets level and resets the corresponding flags.
   */
  void decrementAngleBrackets() {
    // need to take in account Greater-than nodes "(>1)"
    if (angleBracketsLevel > 0) {
      angleBracketsLevel--;
    }
    // we are still in a "< ... < ... > >" if not at the last '>'
    foundLT = (angleBracketsLevel > 0 ? true : false);
    // we are still after the ':' in a "< ... : ... >" if we were already there and if not at the last '>'
    if (isAfterLabelIdentifier) {
      isAfterLabelIdentifier = foundLT;
    }
  }
}
