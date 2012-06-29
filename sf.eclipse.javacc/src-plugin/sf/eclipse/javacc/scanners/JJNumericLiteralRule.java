package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * The JJNumericLiteralRule class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJNumericLiteralRule implements IRule {

  // BF  05/2012 : created

  /** The token */
  IToken fToken;

  /**
   * Creates a new rule which returns a numeric literal token, or Token.UNDEFINED.
   * <p>
   * The token, if returned, is an integer or floating point literal as defined by the Java Language
   * Specification (Third Edition). It includes all forms of integer and floating point literals.
   * <p>
   * If the literal is ill formed, such as having an exponent without following digits or a hexadecimal
   * literal missing a required exponent, Token.UNDEFINED is returned.
   * 
   * @param token - the Token to be return if a numeric literal is found.
   */
  public JJNumericLiteralRule(final IToken token) {
    fToken = (token == null) ? Token.UNDEFINED : token;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    boolean hex = false;
    boolean floating = false;
    char ch = (char) scanner.read();
    int count = 1;

    while (true) { // no loop, just convenient
      if (ch == '0') {
        final char ch2 = (char) scanner.read();
        if (ch2 == 'x' || ch2 == 'X') {
          hex = true;
          ch = (char) scanner.read();
          count += 2;
        }
        else {
          scanner.unread();
        }
      }
      if (ch != '.') {
        if (!isGoodDigit(ch, hex)) {
          break;
        }
        for (; isGoodDigit(ch, hex); ch = (char) scanner.read(), count++) {
          //
        }
      }
      if (ch == '.') {
        floating = true;
        for (ch = (char) scanner.read(), count++; isGoodDigit(ch, hex); ch = (char) scanner.read(), count++) {
          // documented empty block
        }
      }
      if (hex && floating && ch != 'p' && ch != 'P') {
        break;
      }
      if ((hex && (ch == 'p' || ch == 'P')) || (!hex && (ch == 'e' || ch == 'E'))) {
        floating = true;
        ch = (char) scanner.read();
        count += 1;
        if (ch == '+' || ch == '-') {
          ch = (char) scanner.read();
          count += 1;
        }
        if (!(Character.isDigit(ch))) {
          break;
        }
        for (ch = (char) scanner.read(); Character.isDigit(ch); ch = (char) scanner.read()) {
          // documented empty block
        }
      }
      if (!((floating && (ch == 'f' || ch == 'F' || ch == 'd' || ch == 'D')) || (!floating && (ch == 'L' || ch == 'l')))) {
        scanner.unread();
      }
      return fToken;
    }
    for (; count > 0; count--) {
      scanner.unread();
    }
    return Token.UNDEFINED;
  }

  /**
   * Checks if is good digit character.
   * 
   * @param ch - the character
   * @param hex - true if the digit may be hexadecimal
   * @return true, if is good digit
   */
  private boolean isGoodDigit(final char ch, final boolean hex) {
    return ((ch >= '0' && ch <= '9') || (hex && ((ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f'))));
  }
}
