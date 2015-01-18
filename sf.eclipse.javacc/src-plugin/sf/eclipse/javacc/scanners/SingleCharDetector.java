package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Simple word detector to return any single character except EOF.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
class SingleCharDetector implements IWordDetector {

  // MMa 10/2012 : renamed

  /** {@inheritDoc} */
  @Override
  public boolean isWordStart(final char character) {
    return (character != ICharacterScanner.EOF);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isWordPart(final char character) {
    return false;
  }
}
