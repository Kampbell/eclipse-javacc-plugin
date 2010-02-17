package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJCompletionProcessor;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.editors.JJSourceViewerConfiguration;

/**
 * Content Assistant action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editorActions"> as an Action in Editor<br>
 * <extension point="org.eclipse.ui.commands"> as a Command<br>
 * <extension point="org.eclipse.ui.bindings"> as a key binding<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJContentAssist implements IEditorActionDelegate {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 12/2009 : removed unused superclass

  /** the current editor */
  static JJEditor editor;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    editor = (JJEditor) targetEditor;
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action,
                               @SuppressWarnings("unused") final ISelection selection) {
    // not used
  }

  /**
   * Performs Content Assist.<br>
   * The assistant is created by {@link JJSourceViewerConfiguration#getContentAssistant(ISourceViewer)}<br>
   * The processor is defined by {@link JJCompletionProcessor}<br>
   * 
   * @param action the corresponding action
   * @see org.eclipse.jdt.internal.ui.text.java.JavaContentAssistHandler
   * @see org.eclipse.jdt.internal.ui.javaeditor.SpecificContentAssistExecutor
   */
  @SuppressWarnings("restriction")
  public void run(@SuppressWarnings("unused") final IAction action) {
    if (editor == null) {
      return;
    }
    final ITextOperationTarget target = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
    if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
      target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
    }
    return;
  }
}