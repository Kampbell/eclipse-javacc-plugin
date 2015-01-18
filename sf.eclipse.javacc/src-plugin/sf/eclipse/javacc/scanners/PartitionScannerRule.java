package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Partition scanner rule for a code content partition.
 * <p>
 * Returns a non empty partition starting with anything except a block or line comment. <br>
 * The content partition ends at the next comment, or EOF.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014
 */
public class PartitionScannerRule implements IPredicateRule {

  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings

  /** The success token */
  protected final IToken jSuccessToken;

  /**
   * Instantiates a new partition scanner rule.
   * 
   * @param successToken - the success token
   */
  public PartitionScannerRule(final IToken successToken) {
    jSuccessToken = successToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken getSuccessToken() {
    return jSuccessToken;
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    return evaluate(scanner, false);
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
    IToken returnToken = Token.UNDEFINED;

    int c1 = scanner.read();
    int c2 = scanner.read();
    for (;; c1 = c2, c2 = scanner.read()) {

      if (c2 == ICharacterScanner.EOF) {
        scanner.unread();
        if (c1 == ICharacterScanner.EOF) {
          scanner.unread();
          return Token.UNDEFINED; // empty partition
        }
        return jSuccessToken; // final code partition
      }

      if (c1 == '/' && (c2 == '/' || c2 == '*')) {
        scanner.unread();
        scanner.unread();
        return returnToken; // comment partition, not code
      }

      returnToken = jSuccessToken; // Code partition has at least one character 

      if (c1 == '"') { // strings may contain comment delimiters
        for (;; c1 = c2, c2 = scanner.read()) {

          if (c2 == ICharacterScanner.EOF) {
            scanner.unread();
            return jSuccessToken; // final content partition
          }

          if ((c2 == '"' && c1 != '\\') || c2 == '\r' || c2 == '\n') {
            c2 = ' '; // do not re-recognize as beginning quote
            break;
          }
        }
      }
    }
  }
}
