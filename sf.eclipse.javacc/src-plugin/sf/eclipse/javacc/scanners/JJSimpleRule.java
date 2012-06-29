package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * The JJSimpleRule Class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJSimpleRule implements IRule {

  // BF  05/2012 : created

  /** The text */
  String fText;

  /** The token */
  IToken fToken;

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
  public JJSimpleRule(final String text, final IToken token) {
    fText = (text == null) ? "" : text; //$NON-NLS-1$
    fToken = (token == null) ? Token.UNDEFINED : token;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    int index = 0;
    int c = scanner.read();
    for (; index < fText.length(); c = scanner.read(), index++) {
      if ((char) c != fText.charAt(index)) {
        break;
      }
    }
    if (fText.length() == 0 && c != ICharacterScanner.EOF) {
      return fToken;
    }
    if (index == fText.length()) {
      scanner.unread();
      return fToken;
    }
    for (index++; index > 0; index--) {
      scanner.unread();
    }
    return Token.UNDEFINED;
  }
}
