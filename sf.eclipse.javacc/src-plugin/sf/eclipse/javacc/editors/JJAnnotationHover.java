package sf.eclipse.javacc.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

/**
 * Manages annotation hovers for info, error and warning MarkerAnnotation for JavaCC / JTB code and
 * SpellingAnnotation for JavaCC comments.<br>
 * These annotation hovers are shown on the vertical ruler (on the editor left side) if enabled for the
 * corresponding annotation type in the Preferences > General > Editors > TextEditors > Annotations page.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
class JJAnnotationHover implements IAnnotationHover {

  // MMa 12/2009 : renamed (?) ; added spelling annotations case
  // MMa 02/2010 : formatting and javadoc revision

  /**
   * Returns the text (to be shown in the hover) corresponding to the annotation at a given line (if null no
   * hover is shown).
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getHoverInfo(final ISourceViewer aSourceViewer, final int aLineNumber) {
    String hoverInfo = null;
    final IAnnotationModel model = aSourceViewer.getAnnotationModel();
    final Iterator<?> iter = model.getAnnotationIterator();
    while (iter.hasNext()) {
      final Object obj = iter.next();
      if (obj instanceof MarkerAnnotation) {
        // info and errors and warnings problems
        final MarkerAnnotation annotation = (MarkerAnnotation) obj;
        final IMarker marker = annotation.getMarker();
        try {
          final Integer line = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
          if (line.intValue() == aLineNumber + 1) { // different start numbers
            hoverInfo = annotation.getText();
            break;
          }
        } catch (final CoreException e) {
          e.printStackTrace();
          return null;
        }
      }
      else if (obj instanceof SpellingAnnotation) {
        // spelling problems
        final SpellingAnnotation annotation = (SpellingAnnotation) obj;
        final int offset = annotation.getSpellingProblem().getOffset();
        try {
          final int line = aSourceViewer.getDocument().getLineOfOffset(offset);
          if (line == aLineNumber) { // same start numbers
            hoverInfo = annotation.getText();
            break;
          }
        } catch (final BadLocationException e) {
          e.printStackTrace();
          return null;
        }
      }
    }
    return hoverInfo;
  }
}