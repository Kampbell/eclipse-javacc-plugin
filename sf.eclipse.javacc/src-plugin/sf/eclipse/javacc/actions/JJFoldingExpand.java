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
 * Expand all folding Action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editorActions"><br>
 * <action label="%FoldingExpandAll" class="sf.eclipse.javacc.actions.JJFoldingExpand"
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJFoldingExpand implements IEditorActionDelegate {

  // MMa 02/2010 : formatting and javadoc revision

  /** The current editor */
  static JJEditor sJJEditor;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    sJJEditor = (JJEditor) aTargetEditor;
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    final ISourceViewer sourceViewer = sJJEditor.getSourceViewerPlease();
    if (sourceViewer instanceof ProjectionViewer) {
      final ProjectionViewer pv = (ProjectionViewer) sourceViewer;
      if (pv.isProjectionMode()) {
        if (pv.canDoOperation(ProjectionViewer.EXPAND_ALL)) {
          pv.doOperation(ProjectionViewer.EXPAND_ALL);
        }
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction,
                               @SuppressWarnings("unused") final ISelection aSelection) {
    //  not used
  }
}
