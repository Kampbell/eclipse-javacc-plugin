package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Partition scanning rule for a code content partition.
 * <p>
 * Returns the token corresponding to a non empty code partition, i.e. starting with anything except a block
 * or line comment. <br>
 * The code partition ends at the next comment, or EOF.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014-2015-2016
 */
public class CodePartitionRule implements IPredicateRule {

  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings
  // MMa 02/2016 : some renamings ; renamed from PartitionScannerRule

  /** The success token */
  protected final IToken jSuccessToken;

  /**
   * Instantiates a new partition scanner rule.
   * 
   * @param aSuccessToken - the success token
   */
  public CodePartitionRule(final IToken aSuccessToken) {
    jSuccessToken = aSuccessToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken getSuccessToken() {
    return jSuccessToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner aScanner) {
    return evaluate(aScanner, false);
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner aScanner, @SuppressWarnings("unused") final boolean aResume) {
    IToken lastToken = Token.UNDEFINED;

    int c1 = aScanner.read();
    int c2 = aScanner.read();
    for (;; c1 = c2, c2 = aScanner.read()) {

      if (c2 == ICharacterScanner.EOF) {
        aScanner.unread();
        if (c1 == ICharacterScanner.EOF) {
          aScanner.unread();
          return Token.UNDEFINED; // empty partition
        }
        return jSuccessToken; // final code partition TODO why code and not comment ?
        //        return lastToken; // final ; previous was a code or comment partition
      }

      if (c1 == '/' && (c2 == '/' || c2 == '*')) {
        // entering a comment partition
        aScanner.unread();
        aScanner.unread();
        return lastToken; // previous was a code or comment partition
      }

      // entered or continued a code partition
      lastToken = jSuccessToken;

      // strings may contain comment delimiters, so skip them
      if (c1 == '"') {
        for (;; c1 = c2, c2 = aScanner.read()) {
          if (c2 == ICharacterScanner.EOF) {
            aScanner.unread();
            return jSuccessToken; // final content partition
          }
          if ((c2 == '"' && c1 != '\\') || c2 == '\r' || c2 == '\n') {
            c2 = ' '; // do not re-recognize as beginning quote
            break;
          }
        } // end for
      } // end if

    } // end for
  } // end evaluate

}
