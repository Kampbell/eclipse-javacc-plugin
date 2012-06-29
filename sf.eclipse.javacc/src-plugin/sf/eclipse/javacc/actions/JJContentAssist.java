package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
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
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJContentAssist implements IEditorActionDelegate {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 12/2009 : removed unused superclass

  /** The current editor */
  static JJEditor sJJEditor;

  /** {@inheritDoc} */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    sJJEditor = (JJEditor) aTargetEditor;
  }

  /** {@inheritDoc} */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction,
                               @SuppressWarnings("unused") final ISelection aSelection) {
    // not used
  }

  /**
   * Performs Content Assist.<br>
   * The assistant is created by {@link JJSourceViewerConfiguration#getContentAssistant(ISourceViewer)}<br>
   * The processor is defined by {@link JJCompletionProcessor}<br>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    if (sJJEditor == null) {
      return;
    }
    final ITextOperationTarget target = (ITextOperationTarget) sJJEditor.getAdapter(ITextOperationTarget.class);
    if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
      target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
    }
    return;
  }
}