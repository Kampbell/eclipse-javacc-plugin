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
   * @param c the character
   * @return true if c can be the first character of a java identifier, false otherwise
   * @see IWordDetector#isWordStart
   */
  public boolean isWordStart(final char c) {
    return Character.isJavaIdentifierStart(c);
  }

  /**
   * @param c the character
   * @return true if c can be a character of a java identifier, false otherwise
   * @see IWordDetector#isWordPart
   */
  public boolean isWordPart(final char c) {
    return Character.isJavaIdentifierPart(c);
  }
}
