package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule to match integer or floating point literal as defined by the Java Language Specification (Third
 * Edition). It includes all forms of integer and floating point literals.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014-2015-2016
 */
class NumericLiteralRule implements IRule {

  // BF  05/2012 : created
  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings
  // MMa 02/2016 : some renamings

  /** The token */
  protected IToken jToken;

  /**
   * Creates a new rule which returns a numeric literal token, or Token.UNDEFINED.
   * <p>
   * The token, if returned, is an integer or floating point literal as defined by the Java Language
   * Specification (Third Edition). It includes all forms of integer and floating point literals.
   * <p>
   * If the literal is ill formed, such as having an exponent without following digits or a hexadecimal
   * literal missing a required exponent, Token.UNDEFINED is returned.
   * 
   * @param aToken - the Token to be return if a numeric literal is found.
   */
  public NumericLiteralRule(final IToken aToken) {
    jToken = (aToken == null) ? Token.UNDEFINED : aToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner aScanner) {
    boolean hex = false;
    boolean floating = false;
    char ch = (char) aScanner.read();
    int count = 1;

    while (true) { // no loop, just convenient
      if (ch == '0') {
        final char ch2 = (char) aScanner.read();
        if (ch2 == 'x' || ch2 == 'X') {
          hex = true;
          ch = (char) aScanner.read();
          count += 2;
        }
        else {
          aScanner.unread();
        }
      }
      if (ch != '.') {
        if (!isGoodDigit(ch, hex)) {
          break;
        }
        for (; isGoodDigit(ch, hex); ch = (char) aScanner.read(), count++) {
          //
        }
      }
      if (ch == '.') {
        floating = true;
        for (ch = (char) aScanner.read(), count++; isGoodDigit(ch, hex); ch = (char) aScanner.read(), count++) {
          // documented empty block
        }
      }
      if (hex && floating && ch != 'p' && ch != 'P') {
        break;
      }
      if ((hex && (ch == 'p' || ch == 'P')) || (!hex && (ch == 'e' || ch == 'E'))) {
        floating = true;
        ch = (char) aScanner.read();
        count += 1;
        if (ch == '+' || ch == '-') {
          ch = (char) aScanner.read();
          count += 1;
        }
        if (!(Character.isDigit(ch))) {
          break;
        }
        for (ch = (char) aScanner.read(); Character.isDigit(ch); ch = (char) aScanner.read()) {
          // documented empty block
        }
      }
      if (!((floating && (ch == 'f' || ch == 'F' || ch == 'd' || ch == 'D')) || (!floating && (ch == 'L' || ch == 'l')))) {
        aScanner.unread();
      }
      return jToken;
    }
    for (; count > 0; count--) {
      aScanner.unread();
    }
    return Token.UNDEFINED;
  }

  /**
   * Checks if is good digit character.
   * 
   * @param aCh - the character
   * @param aHex - true if the digit may be hexadecimal
   * @return true, if is good digit
   */
  private static boolean isGoodDigit(final char aCh, final boolean aHex) {
    return ((aCh >= '0' && aCh <= '9') || (aHex && ((aCh >= 'A' && aCh <= 'F') || (aCh >= 'a' && aCh <= 'f'))));
  }
}
