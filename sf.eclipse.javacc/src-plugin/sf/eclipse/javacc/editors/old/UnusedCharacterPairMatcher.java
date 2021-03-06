package sf.eclipse.javacc.editors.old;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Helper class to match pairs of characters.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 */
class UnusedCharacterPairMatcher implements ICharacterPairMatcher {

  // MMa 12/2009 : javadoc and formatting revision ; renamed from ParentMatcher ; some refactoring
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : renamed
  // MMa 12/2014 : fixed 
  // MMa 02/2016 : manage the cases in code or in comments

  /** The opening peer character position */
  protected int           startPos;
  /** The closing peer character position */
  protected int           endPos;
  /** The anchor (left / right) */
  protected int           anchor;
  /** The matchable character pairs */
  protected static char[] sPairs = {
      '{', '}', '<', '>', '[', ']', '(', ')' };

  /**
   * Standard constructor.
   */
  public UnusedCharacterPairMatcher() {
    // does nothing
  }

  /** {@inheritDoc} */
  @Override
  public IRegion match(final IDocument aDoc, final int aOffset) {

    if (aDoc == null || aOffset <= 0) {
      return null;
    }
    if (tryMatchPair(aDoc, aOffset) && startPos != endPos) {
      return new Region(startPos, endPos - startPos + 1);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public final int getAnchor() {
    return anchor;
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    clear();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    // does nothing
  }

  /**
   * Tries to match a pair of characters just before a character position.
   * 
   * @param aDoc - the document
   * @param aOffset - a character position
   * @return true if successful, false otherwise
   */
  private boolean tryMatchPair(final IDocument aDoc, final int aOffset) {
    int i;
    int pairIndex1 = sPairs.length;
    int pairIndex2 = sPairs.length;

    startPos = -1;
    endPos = -1;
    // get the character just before the character position
    try {
      final char prevChar = aDoc.getChar(Math.max(aOffset - 1, 0));
      // search for the opening peer character next to the activation point
      for (i = 0; i < sPairs.length; i = i + 2) {
        if (prevChar == sPairs[i]) {
          startPos = aOffset - 1;
          pairIndex1 = i;
          break;
        }
      }
      // search for the closing peer character next to the activation point
      for (i = 1; i < sPairs.length; i = i + 2) {
        if (prevChar == sPairs[i]) {
          endPos = aOffset - 1;
          pairIndex2 = i;
          break;
        }
      }
      if (endPos > -1) {
        anchor = RIGHT;
        startPos = searchForOpeningPeer(aDoc, endPos, sPairs[pairIndex2 - 1], sPairs[pairIndex2]);
        if (startPos > -1) {
          return true;
        }
        endPos = -1;
      }
      else if (startPos > -1) {
        anchor = LEFT;
        endPos = searchForClosingPeer(aDoc, startPos, sPairs[pairIndex1], sPairs[pairIndex1 + 1]);
        if (endPos > -1) {
          return true;
        }
        startPos = -1;
      }

    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, aOffset);
    }
    return false;
  }

  /**
   * Searches for ClosingPeer.
   * 
   * @param aDoc - the document
   * @param aStart - the starting position
   * @param aOpening - the opening character
   * @param aClosing - the closing character
   * @return the ClosingPeer paired character position
   */
  private static int searchForClosingPeer(final IDocument aDoc, final int aStart, final char aOpening,
                                          final char aClosing) {
    int fPos = aStart + 1;
    try {
      int depth = 1;
      char fChar = 0;
      while (true) {
        while (fPos < aDoc.getLength() - 1) {
          fChar = aDoc.getChar(fPos);
          if (fChar == '"') {
            boolean foundNextDoubleQuote = false;
            char old = '"';
            while (!foundNextDoubleQuote) {
              final char curr = aDoc.getChar(++fPos);
              if (curr == '"' && old != '\\') {
                foundNextDoubleQuote = true;
              }
              else {
                // take care of "\\" strings ...
                old = (old == '\\' ? ' ' : curr);
              }
            }
          }
          if (fChar == aOpening || fChar == aClosing) {
            break;
          }
          fPos++;
        }
        if (fPos == aDoc.getLength()) {
          return -1;
        }
        if (fChar == aOpening) {
          depth++;
        }
        else {
          depth--;
        }
        if (depth == 0) {
          return fPos;
        }
        fPos++;
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, fPos);
      return -1;
    }
  }

  /**
   * Searches for OpeningPeer.
   * 
   * @param aDoc - the document
   * @param aStart - the starting position
   * @param aOpening - the opening character
   * @param aClosing - the closing character
   * @return the OpeningPeer paired character position
   */
  private static int searchForOpeningPeer(final IDocument aDoc, final int aStart, final char aOpening,
                                          final char aClosing) {
    int fPos = -5;
    try {
      int depth = 1;
      fPos = aStart - 1;
      char fChar = 0;
      while (true) {
        while (fPos > -1) {
          fChar = aDoc.getChar(fPos);
          if (fChar == '"') {
            while (fPos > 0) {
              if (aDoc.getChar(--fPos) == '"') {
                if (fPos > 0 && aDoc.getChar(fPos - 1) != '\\') {
                  break;
                }
              }
            }
          }
          if (fChar == aOpening || fChar == aClosing) {
            break;
          }
          fPos--;
        }
        if (fPos == -1) {
          return -1;
        }
        if (fChar == aClosing) {
          depth++;
        }
        else {
          depth--;
        }
        if (depth == 0) {
          return fPos;
        }
        fPos--;
      }

    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, fPos);
      return -1;
    }
  }
}
