package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A JavaCC word detector.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 * @author Bill Fenlason 2012
 */
class WordDetector implements IWordDetector {

  // MMa 03/2010 : created by extracting from other classes
  // BF  06/2012 : added inheritDoc tags
  // MMa 10/2012 : renamed

  /**
   * {@inheritDoc}
   * 
   * @param aCh - the character
   * @return true if aCh can be the first character of a java identifier, false otherwise
   */
  @Override
  public boolean isWordStart(final char aCh) {
    return Character.isJavaIdentifierStart(aCh);
  }

  /**
   * {@inheritDoc}
   * 
   * @param aCh - the character
   * @return true if aCh can be a character of a java identifier, false otherwise
   */
  @Override
  public boolean isWordPart(final char aCh) {
    return Character.isJavaIdentifierPart(aCh);
  }
}
