package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JavaCCParserConstants;

/**
 * Content Assistant action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editorActions"> as an Action in Editor<br>
 * <extension point="org.eclipse.ui.commands"> as a Command<br>
 * <extension point="org.eclipse.ui.bindings"> as a key binding<br>
 * 
 * @author Remi Koutcherawy 2003-2009 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJContentAssist implements IEditorActionDelegate, JavaCCParserConstants {

  /*
   * MMa 11/09 : javadoc and formatting revision
   */
  /** the current editor */
  static JJEditor editor;

  /**
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    editor = (JJEditor) targetEditor;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action,
                               @SuppressWarnings("unused") final ISelection selection) {
    // not used
  }

  /**
   * Performs Content Assist.<br>
   * The assistant is created by javacc.editors.JJSourceViewerConfiguration.getContentAssistant()<br>
   * The processor is defined by javacc.editors.JJCompletionProcessor<br>
   * 
   * @param action the corresponding action
   * @seee org.eclipse.jdt.internal.ui.text.java.JavaContentAssistHandler<br>
   * @seee org.eclipse.jdt.internal.ui.javaeditor.SpecificContentAssistExecutor<br>
   */
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