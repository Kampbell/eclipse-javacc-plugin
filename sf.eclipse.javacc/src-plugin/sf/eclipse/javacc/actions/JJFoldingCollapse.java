package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import sf.eclipse.javacc.editors.JJEditor;
/**
 * Collapse all folding Action referenced by plugin.xml
 * <extension point="org.eclipse.ui.editorActions">
 * <action  label="%JavaCC_FoldingCollapseAll"
 *          class="sf.eclipse.javacc.actions.JJFoldingCollapse"
 * 
 * @author Remi Koutcherawy 2003-2009
 * CeCILL license http://www.cecill.info/index.en.html
 */
public class JJFoldingCollapse implements IEditorActionDelegate {
  static JJEditor editor;
  
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
  }

  public void run(IAction action) {
    ISourceViewer sourceViewer= editor.getSourceViewer2();
    if (sourceViewer instanceof ProjectionViewer) {
      ProjectionViewer pv= (ProjectionViewer) sourceViewer;
      if (pv.isProjectionMode()) {
        if (pv.canDoOperation(ProjectionViewer.COLLAPSE_ALL))
          pv.doOperation(ProjectionViewer.COLLAPSE_ALL);
      }
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {
  //  not used
  }
}
