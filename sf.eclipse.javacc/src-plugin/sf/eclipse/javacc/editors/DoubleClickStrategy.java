package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Double click strategy aware of JavaCC identifier syntax rules. Allows the viewer to select the JavaCC
 * identifier around the first character of the selected text.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
class DoubleClickStrategy implements ITextDoubleClickStrategy {

  // MMa 12/2009 : javadoc and formatting revision ; some refactoring
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : added Outline Page updating on double-click
  // MMa 10/2012 : renamed
  // MMa 11/2014 : renamed and refactored methods (node determination done now in JJEditor)
  // MMa 12/2014 : fixed computations on empty document and on initial or last word

  /** {@inheritDoc} */
  @Override
  public void doubleClicked(final ITextViewer aTextViewer) {
    // select the word
    final int selectionStartPos = aTextViewer.getSelectedRange().x;
    if (selectionStartPos < 0) {
      return;
    }
    selectWord(aTextViewer, selectionStartPos);
    // selection has changed : forward the change
    forwardChange(aTextViewer);

    return;
  }

  /**
   * @param aTextViewer - the current viewer
   * @param aCharPos - a character position
   * @return the whole word around the character position
   */
  private String selectWord(final ITextViewer aTextViewer, final int aCharPos) {
    final IDocument doc = aTextViewer.getDocument();
    final int length = doc.getLength();
    // dummy initializations for the logBug message
    int startPos = -99;
    int endPos = -99;
    try {
      int pos = aCharPos;
      if (pos == length) {
        pos--;
      }
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        --pos;
      }
      startPos = pos;
      pos = aCharPos;
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        ++pos;
      }
      endPos = pos;
      final int offset = startPos + 1;
      final int len = endPos - offset;
      if (len > 0) {
        aTextViewer.setSelectedRange(offset, len);
      }
      if (startPos < 0) {
        startPos = 0;
      }
      return doc.get(startPos, endPos - startPos);
    } catch (final BadLocationException e) {
      // do nothing, except returning null
      AbstractActivator.logBug(e, aCharPos, length, startPos, endPos);
    }
    return null;
  }

  /**
   * Finds the JJEditor and forwards the change to it.
   * 
   * @param aTextViewer - the current viewer
   */
  private void forwardChange(final ITextViewer aTextViewer) {
    // get the editor showing the active document (should have a better / more direct way to get it !)
    JJEditor jjeditor = null;
    final IDocument currentDocument = aTextViewer.getDocument();
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final IEditorReference editorReferences[] = window.getActivePage().getEditorReferences();
    for (int i = 0; i < editorReferences.length; i++) {
      final IEditorPart editor = editorReferences[i].getEditor(false); // don't create!
      if (editor instanceof JJEditor) {
        jjeditor = (JJEditor) editor;
        final IEditorInput input = jjeditor.getEditorInput();
        final IDocument doc = jjeditor.getDocumentProvider().getDocument(input);
        if (currentDocument.equals(doc)) {
          // we got the current JJEditor for the current Document
          break;
        }
      }
    }
    if (jjeditor == null) {
      // should not occur
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.Null_problem")); //$NON-NLS-1$ 
      return;
    }
    jjeditor.resetHighlightRange();
    jjeditor.updateCallHierarchyView();
  }

}