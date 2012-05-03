package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A JavaCC word detector.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJWordDetector implements IWordDetector {

  // MMa 03/2010 : created by extracting from other classes

  /**
   * @param aCh the character
   * @return true if aCh can be the first character of a java identifier, false otherwise
   * @see IWordDetector#isWordStart
   */
  @Override
  public boolean isWordStart(final char aCh) {
    return Character.isJavaIdentifierStart(aCh);
  }

  /**
   * @param aCh the character
   * @return true if aCh can be a character of a java identifier, false otherwise
   * @see IWordDetector#isWordPart
   */
  @Override
  public boolean isWordPart(final char aCh) {
    return Character.isJavaIdentifierPart(aCh);
  }
}
