package sf.eclipse.javacc.scanners;

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
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class UnusedTokenRule implements IRule {

  // MMa 04/2009 : modified : added different rule tokens and changed algorithm by keeping state between invocations
  // MMa 11/2009 : fixed syntax coloring issues in java code
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : minor refactoring / renamings
  // BF  05/2012 : old class, replaced by JavaCodeRule and JavaCCCodeRule, currently unused
  // MMa 10/2012 : renamed

  /** Normal label identifier rule token */
  protected final IToken         normalLabel;
  /** Special label identifier rule token */
  protected final IToken         privateLabel;
  /** Lexical state rule token */
  protected final IToken         lexicalState;
/** Regular_expression punctuation ('<', '>' and ':') rule token */
  protected final IToken         regexPunct;
  /**
   * Choices enclosing punctuation ('(', ')', '*', '+' and '?') rule token (in complex_regular_expression_unit
   * and expansion_choices)
   */
  protected final IToken         choicesPunct;
  /** Found previously a ':', to search a lexical state identifier (reset to false when not the case) */
  protected boolean              foundCOLON;
/** Found previously a '<' (reset to false when found a matching '>' or in Java code) */
  protected boolean              foundLT;
  /** JJTree Node parenthesis level : -1 : outside ; 0 : found the node ; 1, 2 ... level */
  protected int                  jnParenLevel;
  /**
   * Found previously a private label identifier prefix ('#' before a label identifier) (reset to false when
   * found a ':'
   */
  protected boolean              foundPLIP;
  /** Found an expansion unit 'try' */
  protected boolean              foundEuTry;
  /** Angle brackets nesting level */
  protected int                  angleBracketsLevel;
  /** Braces nesting level */
  protected int                  bracesLevel;
  /** Is after a label identifier in a regular_expression */
  protected boolean              isAfterLabelIdentifier;
  /** Is after a Java identifier */
  protected boolean              isAfterJavaIdentifier;
  /** Stack used to store same level angle brackets flag */
  protected final Stack<Boolean> angStack;
  /** Stack used to store same level parenthesis flag */
  protected final Stack<Boolean> parStack;

  /**
   * Standard constructor.
   * 
   * @param aNormalLabel - the normal token rule token
   * @param aPrivateLabel - the special token rule token
   * @param aLexicalState - the lexical state rule token
   * @param aRegexPunct - the regular_expression punctuation rule token
   * @param aChoicesPunct - the choices enclosing punctuation rule token
   */
  public UnusedTokenRule(final IToken aNormalLabel, final IToken aPrivateLabel, final IToken aLexicalState,
                         final IToken aRegexPunct, final IToken aChoicesPunct) {
    normalLabel = aNormalLabel;
    privateLabel = aPrivateLabel;
    lexicalState = aLexicalState;
    regexPunct = aRegexPunct;
    choicesPunct = aChoicesPunct;
    foundCOLON = false;
    foundLT = false;
    foundPLIP = false;
    foundEuTry = false;
    angleBracketsLevel = 0;
    bracesLevel = 0;
    jnParenLevel = -1;
    isAfterLabelIdentifier = false;
    isAfterJavaIdentifier = false;
    angStack = new Stack<Boolean>();
    parStack = new Stack<Boolean>();
  }

  /**
   * Reinitializes state.
   */
  public void reinit() {
    foundCOLON = false;
    foundLT = false;
    foundPLIP = false;
    foundEuTry = false;
    angleBracketsLevel = 0;
    bracesLevel = 0;
    jnParenLevel = -1;
    isAfterLabelIdentifier = false;
    isAfterJavaIdentifier = false;
    angStack.clear();
    parStack.clear();
  }

  /**
   * Evaluates the {@link IToken} rule token of the subsequent characters/tokens read from the given
   * {@link ICharacterScanner}.
   * <p>
   * {@link IRule#evaluate(ICharacterScanner)} is called repeatedly by {@link RuleBasedScanner#nextToken()}
   * (called itself by {@link DefaultDamagerRepairer#createPresentation(TextPresentation, ITypedRegion)} ) on
   * the different rules set in {link CodeScanner#updateRules()}.<br>
   * So, for {@link #evaluate(ICharacterScanner)}, scans begin with the first non whitespace character after a
   * comment or a string.<br>
   * Also, when modifying a document, {@link IRule#evaluate(ICharacterScanner)} is called only on the tokens
   * of the modified line.<br>
   * We memorize some internal state information between each call through class fields.<br>
   * We must return whitespaces and punctuation (word separators) as soon as they are encountered (and without
   * consuming them) in order for the other rules to process the comments anywhere they may appear.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    /** Found a normal label identifier */
    boolean isNoLa = false;
    /** Found a private label identifier */
    boolean isPrLa = false;
    /** Found a lexical state identifier after a ':' or in a lexical state list */
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
    if (ic == '<') {
      if (!isAfterJavaIdentifier && (jnParenLevel < 0)) {
        // regular_expression case
        foundCOLON = false;
        incrementAngleBrackets();
        angStack.push(Boolean.valueOf(isAfterJavaIdentifier));
        return regexPunct;
      }
      // Java code case
      foundLT = false;
      return Token.UNDEFINED;
    }

    if (ic == '>') {
      final boolean isInRegExp = foundLT;
      decrementAngleBrackets();
      if (!angStack.isEmpty()) {
        // this can occur for example when inserting before a '>' ...
        isAfterJavaIdentifier = angStack.pop().booleanValue();
      }
      if (isInRegExp && !isAfterJavaIdentifier) {
        // regular_expression case
        return regexPunct;
      }
      // Greater-than nodes "(>1)" or Java code case
      return Token.UNDEFINED;
    }

    if (ic == ':') {
      if (foundLT) {
        // ':' inside a '< ... >', so it is after a label identifier in a regular_expression,
        // so it cannot be before a lexical state identifier in a regexpr_spec
        isAfterLabelIdentifier = true;
        foundPLIP = false;
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

    if (isChoicesPunct(ic)) {
      // '(', ')', '*', '+', '?'
      boolean oldIsAfterJavaIdentifier = isAfterJavaIdentifier;
      if (ic == '(') {
        parStack.push(Boolean.valueOf(isAfterJavaIdentifier));
        if (jnParenLevel >= 0) {
          jnParenLevel++;
        }
      }
      else if (ic == ')') {
        if (!parStack.isEmpty()) {
          // this can occur for example when inserting before a ')' ...
          oldIsAfterJavaIdentifier = parStack.pop().booleanValue();
          if (jnParenLevel >= 0) {
            jnParenLevel--;
            if (jnParenLevel == 0) {
              jnParenLevel = -1;
            }
          }
        }
      }
      isAfterJavaIdentifier = false;
      // case '*' as the all lexical states symbol
      if (ic == '*' && foundLT && bracesLevel == 0) {
        return lexicalState;
      }
      // case in an expansion unit 'try'
      if (foundEuTry && bracesLevel == 2 && !oldIsAfterJavaIdentifier) {
        return choicesPunct;
      }
      // case '(' or ')' in java code or conditional node
      if ((ic == '(' || ic == ')') && oldIsAfterJavaIdentifier || jnParenLevel > 0) {
        scanner.unread();
        return Token.UNDEFINED;
      }
      // other cases
      if (foundLT || (bracesLevel == 1 && (!oldIsAfterJavaIdentifier || jnParenLevel < 0))) {
        if (ic == '+') {
          ic = scanner.read();
          if (ic == '+') {
            scanner.unread();
            return Token.UNDEFINED;
          }
          scanner.unread();
        }
        return choicesPunct;
      }
      // other cases, not a choices enclosing punctuation
      scanner.unread();
      return Token.UNDEFINED;
    }

    if (ic == '{') {
      bracesLevel++;
      isAfterJavaIdentifier = false;
      scanner.unread();
      return Token.UNDEFINED;
    }

    if (ic == '}') {
      bracesLevel--;
      isAfterJavaIdentifier = false;
      if (bracesLevel == 0) {
        foundEuTry = false;
      }
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
        foundPLIP = true;
        return privateLabel;
      }
      else if (bracesLevel > 0) {
        jnParenLevel = 0;
      }
      // otherwise continue with the next characters (of a node descriptor expression)
    }

    if (!foundLT && !foundCOLON) {
      // other Java punctuation plus JavaCC and Java keywords plus Java identifiers
      if (Character.isJavaIdentifierStart(ic)) {
        isAfterJavaIdentifier = true;
      }
      else {
        isAfterJavaIdentifier = false;
      }
      if (!foundEuTry && bracesLevel == 1) {
        // search for expansion unit 'try' ; don't bother with EOF
        if (ic == 't') {
          ic = scanner.read();
          if (ic == 'r') {
            ic = scanner.read();
            if (ic == 'y') {
              foundEuTry = true;
              isAfterJavaIdentifier = false;
            }
            scanner.unread();
          }
          scanner.unread();
        }
      }
      scanner.unread();
      return Token.UNDEFINED;
    }

    // here we are with fountLT or foundCOLON, process the character within the loop
    scanner.unread();
    // process next characters (can be if not JavaCC punctuation and if foundLT == true or foundCOLON == true)
    while ((ic = scanner.read()) != ICharacterScanner.EOF) {
      if (foundLT) {
        if (isAfterJavaIdentifier) {
          // Java code case
          scanner.unread();
          return Token.UNDEFINED;
        }
        // regular_expression case, inside a '< ... >'
        if (isWhitespaceOrComment1stChar(ic)) {
          // stop if whitespaces or beginning of a comment
          scanner.unread();
          break;
        }
        else if (ic == '>' || ic == ',') {
          // case '>' and ','
          if (bracesLevel == 0) {
            // stop if we are in a lexical state list (not inside braces)
            scanner.unread();
            return lexicalState;
          }
          scanner.unread();
          break;
        }
        else if (isChoicesPunct(ic)) {
          // stop if reached choices enclosing punctuation
          scanner.unread();
          break;
        }
        else if (ic == '[' || ic == ']' || ic == '|' || ic == '~' || ic == ':' || ic == '-' || ic == '\"') {
          // stop if reached some other punctuation or the beginning of a string
          scanner.unread();
          break;
        }
        else {
          // other characters found : should be in a normal label identifier
          // if not in a private label identifier or in a lexical state list
          if (foundPLIP) {
            isPrLa = true;
          }
          else if (bracesLevel == 0) {
            isLxSt = true;
          }
          else {
            isNoLa = true;
          }
        }
      } // end if (foundLT)
      else if (foundCOLON) {
        // case after a ':' in a regexpr_spec
        if (isWhitespaceOrComment1stChar(ic)) {
          // stop if whitespaces or beginning of a comment, and terminate colon search
          // if already found a lexical state identifier
          scanner.unread();
          if (isLxSt) {
            foundCOLON = false;
          }
          break;
        }
        else if (ic == '}' || ic == '|') {
          // stop if reached a '}', '|', and terminate colon search
          // (must have encountered before a lexical state identifier, so isLxSt should be true)
          scanner.unread();
          foundCOLON = false;
          break;
        }
        else if (Character.isJavaIdentifierPart(ic) || ic == '*') {
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
   * @param ic - the character
   * @return true if the character is a whitespace, false otherwise
   */
  public static boolean isWhitespace(final int ic) {
    return (ic == ' ' || (ic >= '\t' && ic <= '\r'));
  }

  /**
   * Returns whether the given character is a whitespace or the beginning of a comment:<br>
   * ' ', '\t', '\n', '\r', '\f', '/'.
   * 
   * @param ic - the character
   * @return true if the character is a whitespace or the beginning of a comment, false otherwise
   */
  public static boolean isWhitespaceOrComment1stChar(final int ic) {
    return (isWhitespace(ic) || ic == '/');
  }

  /**
   * Returns whether the given character is a choices enclosing punctuation:<br>
   * '(', ')', '*', '+', '?'.
   * 
   * @param ic - the character
   * @return true if the character is a choices enclosing punctuation, false otherwise
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
