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
 * @author Marc Mazas 2009-2010
 */
public class JJCharacterPairMatcher implements ICharacterPairMatcher {

  // MMa 12/2009 : javadoc and formatting revision ; renamed from ParentMatcher ; some refactoring
  // MMa 02/2010 : formatting and javadoc revision

  /** The document */
  protected IDocument     fDocument;
  /** The opening peer character position */
  protected int           fStartPos;
  /** The closing peer character position */
  protected int           fEndPos;
  /** The anchor (left / right) */
  protected int           fAnchor;
  /** The matchable character pairs */
  protected static char[] fPairs = {
      '{', '}', '<', '>', '[', ']', '(', ')' };

  /**
   * Standard constructor.
   */
  public JJCharacterPairMatcher() {
    // does nothing
  }

  /**
   * @see ICharacterPairMatcher#match(IDocument, int)
   */
  public IRegion match(final IDocument aDocument, final int aOffset) {

    if (aOffset < 0) {
      return null;
    }
    fDocument = aDocument;
    if (fDocument != null && tryMatchPair(aOffset) && fStartPos != fEndPos) {
      return new Region(fStartPos, fEndPos - fStartPos + 1);
    }
    return null;
  }

  /**
   * @see ICharacterPairMatcher#getAnchor()
   */
  public int getAnchor() {
    return fAnchor;
  }

  /**
   * @see ICharacterPairMatcher#dispose()
   */
  public void dispose() {
    clear();
    fDocument = null;
  }

  /**
   * @see ICharacterPairMatcher#clear()
   */
  public void clear() {
    // does nothing
  }

  /**
   * Tries to match a pair of characters just before a character position.
   * 
   * @param aOffset a character position
   * @return true if successful, false otherwise
   */
  protected boolean tryMatchPair(final int aOffset) {
    int i;
    int pairIndex1 = fPairs.length;
    int pairIndex2 = fPairs.length;

    fStartPos = -1;
    fEndPos = -1;
    // get the character just before the character position
    try {
      final char prevChar = fDocument.getChar(Math.max(aOffset - 1, 0));
      // search for the opening peer character next to the activation point
      for (i = 0; i < fPairs.length; i = i + 2) {
        if (prevChar == fPairs[i]) {
          fStartPos = aOffset - 1;
          pairIndex1 = i;
          break;
        }
      }
      // search for the closing peer character next to the activation point
      for (i = 1; i < fPairs.length; i = i + 2) {
        if (prevChar == fPairs[i]) {
          fEndPos = aOffset - 1;
          pairIndex2 = i;
          break;
        }
      }
      if (fEndPos > -1) {
        fAnchor = RIGHT;
        fStartPos = searchForOpeningPeer(fEndPos, fPairs[pairIndex2 - 1], fPairs[pairIndex2]);
        if (fStartPos > -1) {
          return true;
        }
        fEndPos = -1;
      }
      else if (fStartPos > -1) {
        fAnchor = LEFT;
        fEndPos = searchForClosingPeer(fStartPos, fPairs[pairIndex1], fPairs[pairIndex1 + 1]);
        if (fEndPos > -1) {
          return true;
        }
        fStartPos = -1;
      }

    } catch (final BadLocationException x) {
      // swallowed
    }
    return false;
  }

  /**
   * Searches for ClosingPeer.
   * 
   * @param aStart the starting position
   * @param aOpening the opening character
   * @param aClosing the closing character
   * @return the ClosingPeer paired character position
   */
  protected int searchForClosingPeer(final int aStart, final char aOpening, final char aClosing) {
    try {
      int depth = 1;
      int fPos = aStart + 1;
      char fChar = 0;
      while (true) {
        while (fPos < fDocument.getLength()) {
          fChar = fDocument.getChar(fPos);
          if (fChar == '"') {
            boolean found = false;
            char old = '"';
            while (!found) {
              final char curr = fDocument.getChar(++fPos);
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
        if (fPos == fDocument.getLength()) {
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
   * @param aStart the starting position
   * @param aOpening the opening character
   * @param aClosing the closing character
   * @return the OpeningPeer paired character position
   */
  protected int searchForOpeningPeer(final int aStart, final char aOpening, final char aClosing) {
    try {
      int depth = 1;
      int fPos = aStart - 1;
      char fChar = 0;
      while (true) {
        while (fPos > -1) {
          fChar = fDocument.getChar(fPos);
          if (fChar == '"') {
            while (true) {
              if (fDocument.getChar(--fPos) == '"') {
                if (fDocument.getChar(fPos - 1) != '\\') {
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
