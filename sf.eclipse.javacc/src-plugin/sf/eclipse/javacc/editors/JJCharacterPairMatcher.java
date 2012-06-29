package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

/**
 * Helper class to match pairs of characters.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJCharacterPairMatcher implements ICharacterPairMatcher {

  // MMa 12/2009 : javadoc and formatting revision ; renamed from ParentMatcher ; some refactoring
  // MMa 02/2010 : formatting and javadoc revision

  /** The document */
  protected IDocument     jDocument;
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
  public JJCharacterPairMatcher() {
    // does nothing
  }

  /** {@inheritDoc} */
  @Override
  public IRegion match(final IDocument aDoc, final int aOffset) {

    if (aOffset < 0) {
      return null;
    }
    jDocument = aDoc;
    if (jDocument != null && tryMatchPair(aOffset) && startPos != endPos) {
      return new Region(startPos, endPos - startPos + 1);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public int getAnchor() {
    return anchor;
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    clear();
    jDocument = null;
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    // does nothing
  }

  /**
   * Tries to match a pair of characters just before a character position.
   * 
   * @param aOffset - a character position
   * @return true if successful, false otherwise
   */
  protected boolean tryMatchPair(final int aOffset) {
    int i;
    int pairIndex1 = sPairs.length;
    int pairIndex2 = sPairs.length;

    startPos = -1;
    endPos = -1;
    // get the character just before the character position
    try {
      final char prevChar = jDocument.getChar(Math.max(aOffset - 1, 0));
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
        startPos = searchForOpeningPeer(endPos, sPairs[pairIndex2 - 1], sPairs[pairIndex2]);
        if (startPos > -1) {
          return true;
        }
        endPos = -1;
      }
      else if (startPos > -1) {
        anchor = LEFT;
        endPos = searchForClosingPeer(startPos, sPairs[pairIndex1], sPairs[pairIndex1 + 1]);
        if (endPos > -1) {
          return true;
        }
        startPos = -1;
      }

    } catch (final BadLocationException x) {
      // swallowed
    }
    return false;
  }

  /**
   * Searches for ClosingPeer.
   * 
   * @param aStart - the starting position
   * @param aOpening - the opening character
   * @param aClosing - the closing character
   * @return the ClosingPeer paired character position
   */
  protected int searchForClosingPeer(final int aStart, final char aOpening, final char aClosing) {
    try {
      int depth = 1;
      int fPos = aStart + 1;
      char fChar = 0;
      while (true) {
        while (fPos < jDocument.getLength()) {
          fChar = jDocument.getChar(fPos);
          if (fChar == '"') {
            boolean found = false;
            char old = '"';
            while (!found) {
              final char curr = jDocument.getChar(++fPos);
              if (curr == '"' && old != '\\') {
                found = true;
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
        if (fPos == jDocument.getLength()) {
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
      return -1;
    }
  }

  /**
   * Searches for OpeningPeer.
   * 
   * @param aStart - the starting position
   * @param aOpening - the opening character
   * @param aClosing - the closing character
   * @return the OpeningPeer paired character position
   */
  protected int searchForOpeningPeer(final int aStart, final char aOpening, final char aClosing) {
    try {
      int depth = 1;
      int fPos = aStart - 1;
      char fChar = 0;
      while (true) {
        while (fPos > -1) {
          fChar = jDocument.getChar(fPos);
          if (fChar == '"') {
            while (true) {
              if (jDocument.getChar(--fPos) == '"') {
                if (jDocument.getChar(fPos - 1) != '\\') {
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
      return -1;
    }
  }
}
