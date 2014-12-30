package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * The {@link SimpleRule} Class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014
 */
class SimpleRule implements IRule {

  // BF  05/2012 : created
  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings

  /** The text */
  protected String jText;

  /** The token */
  protected IToken jToken;

  /**
   * Creates a new simple rule which returns the specified token when a specific string is matched, or returns
   * Token.UNDEFINED
   * <p>
   * If the specified token is null or has zero length, the next single character is matched and the token is
   * returned.
   * 
   * @param text - the text string to be matched
   * @param token - the token to be returned
   */
  public SimpleRule(final String text, final IToken token) {
    jText = (text == null) ? "" : text; //$NON-NLS-1$
    jToken = (token == null) ? Token.UNDEFINED : token;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    int index = 0;
    int c = scanner.read();
    final int textLen = jText.length();
    for (; index < textLen; c = scanner.read(), index++) {
      if ((char) c != jText.charAt(index)) {
        break;
      }
    }
    if (textLen == 0 && c != ICharacterScanner.EOF) {
      return jToken;
    }
    if (index == textLen) {
      scanner.unread();
      return jToken;
    }
    for (index++; index > 0; index--) {
      scanner.unread();
    }
    return Token.UNDEFINED;
  }
}
