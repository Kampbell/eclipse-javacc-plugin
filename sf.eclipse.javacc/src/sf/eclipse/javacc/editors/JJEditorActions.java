package sf.eclipse.javacc.editors;

import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.IUpdate;

import sf.eclipse.javacc.JJBuilder;
import sf.eclipse.javacc.JavaccPlugin;
import sf.eclipse.javacc.parser.SimpleNode;

/**
 * Provide basic actions for .jj .jjt .jtb files
 * These actions are shared among all JJ editors,
 * and are only visible when a JJ editor is active.
 * 
 * This class is declared in plugin.xml as a contributorClass
 * for the editor, this gives buttons in the Toolbar
 * <extension point="org.eclipse.ui.editors">
 *   <editor
 *     contributorClass="sf.eclipse.javacc.editors.JJEditorActions"
 * 
 * This class includes inner-classes for actions.
 * 
 * This class also contains actions used in popupMenu
 * theses are registered by JJEditor.editorContextMenuAboutToShow()  
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJEditorActions extends BasicTextEditorActionContributor {
  public static CompileAction compileAction;
  public static JJDocAction gendocAction;
  public static GotoRuleAction gotoRuleAction;
  public static GoBackAction goBackAction;
  
//  private HashMap historyHashMap = new HashMap();
  static LinkedList history;

  public IResource res;
  public JJEditor ed;

  /**
   * Action to compile the .jj or .jjt file
   */
  class CompileAction extends Action {
    public CompileAction(String label) {
      super(label);
    }
    /**
     * Compile the .jj or .jjt file
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      try {
      	// Save the file. This triggers a new Compilation.
        ed.doSave(null);
        // Compile
        JJBuilder.CompileJJResource(res);
        // Refresh the whole project
        res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Action to call JJDoc on the file
   */
  class JJDocAction extends Action {
    public JJDocAction(String label) {
      super(label);
    }
    /**
     * Generate doc for the .jj or .jjt file
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      try {
      	// Save the file. This triggers a new Compilation.
        ed.doSave(null);
        // Call JJDoc
        JJBuilder.GenDocForJJResource(res);
      	// Refresh Project
        res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Action to jump to rule declaration
   */
  class GotoRuleAction extends Action {
    public GotoRuleAction() {
      super(JavaccPlugin.getResourceString("JJEditorActions.Goto_Declaration")); //$NON-NLS-1$
      setToolTipText(JavaccPlugin.getResourceString("JJEditorActions.Goto_Declaration_Tooltip")); //$NON-NLS-1$
    }

    /**
     * Get Selection from Editor, search matching node in AST
     * then select node corresponding text.
     * Put last selection in History
     * @see Action#run()
     */
    public void run() {
      ITextSelection selection = (ITextSelection)
        ed.getSelectionProvider().getSelection();
      if (!selection.isEmpty()) {
        String text = selection.getText();
        SimpleNode node = ed.search(text);
        if (node != null){
          IRegion range = ed.getHighlightRange();
          history.addLast(selection);
          history.addLast(range);
          goBackAction.update();
          ed.setSelection(node);
        }
      }
    }
  }
  
  /**
   * Action to jump to last Selection
   * Bug prone : the list is static, not remembering which file
   */
  class GoBackAction extends Action implements IUpdate {
    public GoBackAction() {
      super(JavaccPlugin.getResourceString("JJEditorActions.Back_to_last_selection")); //$NON-NLS-1$
      setToolTipText(JavaccPlugin.getResourceString("JJEditorActions.Back_to_last_selection_Tooltip")); //$NON-NLS-1$
      update();
    }

    /**
     * Retrieve Selection from History
     * then goto last Selection
     * @see Action#run()
     */
    public void run() {
      IRegion range = (IRegion) history.removeLast();
      ITextSelection sel = (ITextSelection) history.removeLast();
      ed.setSelection(range, sel);
      update();
    }

    /**
     * Update the action
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update() {
      if (history.isEmpty())
        setEnabled(false);
      else
        setEnabled(true);
    }
  }

  /**
   * Creates a new JJEditorActions contributor.
   */
  public JJEditorActions() {
    super();
    JavaccPlugin jpg = JavaccPlugin.getDefault();
    
    // compile Action set up on Toolbar
    compileAction = new CompileAction(JavaccPlugin.getResourceString("JJEditorActions.Compile_Action")); //$NON-NLS-1$
    compileAction.setToolTipText(JavaccPlugin.getResourceString("JJEditorActions.Compile_Action_Tooltip")); //$NON-NLS-1$
    ImageDescriptor desc = jpg.getResourceImageDescriptor("jj_compile.gif"); //$NON-NLS-1$
    compileAction.setImageDescriptor(desc);
    
    //  genDoc Action set up on Toolbar
    gendocAction = new JJDocAction(JavaccPlugin.getResourceString("JJEditorActions.Generate_Doc_Action")); //$NON-NLS-1$
    gendocAction.setToolTipText(JavaccPlugin.getResourceString("JJEditorActions.Generate_Doc_Action_Tooltip")); //$NON-NLS-1$
    desc = jpg.getResourceImageDescriptor("jj_gendoc.gif"); //$NON-NLS-1$
    gendocAction.setImageDescriptor(desc);
// Seems that Eclipse 3.0 doesn't take that into account
//    desc = jpg.getResourceImageDescriptor("jj_gendoc_h.gif"); //$NON-NLS-1$
//    gendocAction.setHoverImageDescriptor(desc);
    
    // goto Rule Action set up on Toolbar
    gotoRuleAction = new GotoRuleAction();
    
    // go to last Selection Action
    history = new LinkedList();
    goBackAction = new GoBackAction();
 }

  /** (non-Javadoc)
   * Method declared on EditorActionBarContributor
   */
  public void contributeToToolBar(IToolBarManager toolBarManager) {
    super.contributeToToolBar(toolBarManager);

    // Editor-specific toolbar actions.
    toolBarManager.add(new Separator(JavaccPlugin.getResourceString("JJEditorActions.JJEditor_Separator"))); //$NON-NLS-1$
    toolBarManager.add(compileAction);
    toolBarManager.add(gendocAction);
  }

  /**
   * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(IEditorPart)
   */
  public void setActiveEditor(IEditorPart aEditor) {
    super.setActiveEditor(aEditor);
    ed = (JJEditor) aEditor;
    IEditorInput input = ed.getEditorInput();
    res = (IResource) input.getAdapter(IResource.class);
    
    // Editor Dependant Actions 
    // This doesn't work has expected ...
//    // Search corresponding history
//    LinkedList hist = (LinkedList) historyHashMap.get(res);
//    if (hist == null) {
//      hist = new LinkedList();
//      historyHashMap.put(res, hist);
//      history = hist;
//    }
    history.clear();
    goBackAction.update();
  }
}
