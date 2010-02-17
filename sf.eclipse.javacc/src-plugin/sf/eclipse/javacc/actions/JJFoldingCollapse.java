package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJEditor;

/**
 * Collapse all folding Action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editorActions"><br>
 * <action label="%JavaCC_FoldingCollapseAll" class="sf.eclipse.javacc.actions.JJFoldingCollapse"
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJFoldingCollapse implements IEditorActionDelegate {

  // MMa 02/2010 : formatting and javadoc revision

  /** the current editor */
  static JJEditor fEditor;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    fEditor = (JJEditor) targetEditor;
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run(@SuppressWarnings("unused") final IAction action) {
    final ISourceViewer sourceViewer = fEditor.getSourceViewerPlease();
    if (sourceViewer instanceof ProjectionViewer) {
      final ProjectionViewer pv = (ProjectionViewer) sourceViewer;
      if (pv.isProjectionMode()) {
        if (pv.canDoOperation(ProjectionViewer.COLLAPSE_ALL)) {
          pv.doOperation(ProjectionViewer.COLLAPSE_ALL);
        }
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action,
                               @SuppressWarnings("unused") final ISelection selection) {
    //  not used
  }
}
