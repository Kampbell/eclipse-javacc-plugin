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
 * Content Assistant action
 * Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.editorActions">  as an Action in Editor
 * <extension point="org.eclipse.ui.commands">       as a Command
 * <extension point="org.eclipse.ui.bindings">       as a key binding
 *  
 * @author Remi Koutcherawy 2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class JJContentAssist implements IEditorActionDelegate, JavaCCParserConstants {
  static JJEditor editor;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // not used
  }
  
  /**
   * Perform Content Assist
   * The assistant is created by javacc.editors.JJSourceViewerConfiguration.getContentAssistant()
   * The processor is defined by javacc.editors.JJCompletionProcessor
   * @seee org.eclipse.jdt.internal.ui.text.java.JavaContentAssistHandler
   * @seee org.eclipse.jdt.internal.ui.javaeditor.SpecificContentAssistExecutor
   */
  public void run(IAction action) {
    if (editor == null)
      return;
    ITextOperationTarget target = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
    if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS))
      target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
    return;
  }
}