package sf.eclipse.javacc.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Manages annotation hovers for info, error and warning MarkerAnnotation for JavaCC / JTB code and
 * SpellingAnnotation for JavaCC comments.<br>
 * These annotation hovers are shown on the vertical ruler (on the editor left side) if enabled for the
 * corresponding annotation type in the Preferences > General > Editors > TextEditors > Annotations page.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class AnnotationHover implements IAnnotationHover {

  // MMa 12/2009 : renamed (?) ; added spelling annotations case
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : renamed
  // MMa 10/2014 : updated for summarizable annotations

  /** The platform line separator */
  final String LS = System.getProperty("line.separator"); //$NON-NLS-1$

  /**
   * Returns the text (to be shown in the hover) corresponding to the annotation at a given line (if null no
   * hover is shown).
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getHoverInfo(final ISourceViewer aSourceViewer, final int aLineNumber) {
    String hoverInfo = null;
    final int ln = aLineNumber + 1; // different start numbers
    int lineStart = -1;
    int lineEnd = -1;
    boolean isProjAnnCollapsed = false;
    // first find the enclosing projection annotation, to see if it is collapsed or not
    final IDocument doc = aSourceViewer.getDocument();
    final IAnnotationModel projAnnModel = ((ProjectionViewer) aSourceViewer).getProjectionAnnotationModel();
    if (projAnnModel == null) {
      // can be when drag and drop
      return null;
    }
    final Iterator<?> projAnnIter = projAnnModel.getAnnotationIterator();
    while (projAnnIter.hasNext()) {
      final Object obj = projAnnIter.next();
      if (obj instanceof ProjectionAnnotation) {
        final ProjectionAnnotation projAnn = (ProjectionAnnotation) obj;
        final Position pamPos = projAnnModel.getPosition(projAnn);
        try {
          lineStart = doc.getLineOfOffset(pamPos.offset);
          lineEnd = doc.getLineOfOffset(pamPos.offset + pamPos.length);
        } catch (final BadLocationException e) {
          AbstractActivator.logBug(e, pamPos.offset, pamPos.length);
          return null;
        }
        if (ln >= lineStart && ln <= lineEnd) {
          isProjAnnCollapsed = projAnn.isCollapsed();
          break;
        }
      }
    }
    // then find the marker(s)
    final IAnnotationModel annModel = aSourceViewer.getAnnotationModel();
    final Iterator<?> annIter = annModel.getAnnotationIterator();
    while (annIter.hasNext()) {
      final Object obj = annIter.next();
      if (obj instanceof MarkerAnnotation) {
        // info and errors and warnings problems
        final MarkerAnnotation markerAnn = (MarkerAnnotation) obj;
        final IMarker marker = markerAnn.getMarker();
        try {
          final int line = ((Integer) marker.getAttribute(IMarker.LINE_NUMBER)).intValue();
          if (isProjAnnCollapsed) {
            // in a collapsed range
            if (line >= lineStart && line <= lineEnd) {
              hoverInfo = (hoverInfo == null ? "- " + markerAnn.getText() : hoverInfo + LS + "- " //$NON-NLS-1$ //$NON-NLS-2$
                                                                            + markerAnn.getText());
            }
          }
          else {
            // not in a collapsed range
            if (line == ln) {
              hoverInfo = (hoverInfo == null ? markerAnn.getText() : hoverInfo + LS + markerAnn.getText());
            }
          }
        } catch (final CoreException e) {
          AbstractActivator.logBug(e);
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
          AbstractActivator.logBug(e, offset);
          return null;
        }
      }
    }
    return hoverInfo;
  }
}