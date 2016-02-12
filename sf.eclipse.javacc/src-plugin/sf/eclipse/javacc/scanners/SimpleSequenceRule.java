package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule to match specific sequence of characters.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014-2015-2016
 */
class SimpleSequenceRule implements IRule {

  // BF  05/2012 : created
  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings
  // MMa 02/2016 : some renamings ; renamed from SimpleRule

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
   * @param aText - the text string to be matched
   * @param aToken - the token to be returned
   */
  public SimpleSequenceRule(final String aText, final IToken aToken) {
    jText = (aText == null) ? "" : aText; //$NON-NLS-1$
    jToken = (aToken == null) ? Token.UNDEFINED : aToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner aScanner) {
    int ix = 0;
    int c = 0;
    char ch;
    final int textLen = jText.length();

    for (c = aScanner.read(), ch = (char) c; ix < textLen; c = aScanner.read(), ch = (char) c, ix++) {
      if (ch != jText.charAt(ix)) {
        break;
      }
    }
    if (textLen == 0 && c != ICharacterScanner.EOF) {
      // considered as a match, so do not unread
      return jToken;
    }
    if (ix == textLen) {
      // unread last character, outside the range
      aScanner.unread();
      return jToken;
    }
    // unread remaining characters
    for (ix++; ix > 0; ix--) {
      aScanner.unread();
    }
    return Token.UNDEFINED;
  }
}
