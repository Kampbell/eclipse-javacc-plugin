package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Simple word detector to return any single character except EOF.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc 2016
 */
class SingleCharDetector implements IWordDetector {

  // MMa 10/2012 : renamed
  // MMa 02/2016 : some renamings

  /** {@inheritDoc} */
  @Override
  public boolean isWordStart(final char aCh) {
    return (aCh != ICharacterScanner.EOF);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isWordPart(@SuppressWarnings("unused") final char aCh) {
    return false;
  }
}
