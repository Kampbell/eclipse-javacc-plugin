package sf.eclipse.javacc.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyMessages;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Main view for the Call hierarchy referenced by plugin.xml 
 * builds a tree of callers/callees
 * and allows the user to double click an entry to go to the selected method.
 * 
 * Inspired from org.eclipse.jdt.internal.ui.callhierarchy
 * and simplified to the minimum required.
 * 
 * @author Remi Koutcherawy 2008-2009
 * CeCILL License http://www.cecill.info/index.en.html
 */
@SuppressWarnings("restriction")
public class JJCallHierarchy extends ViewPart implements ISelectionChangedListener, IJJConstants {
  static final int CALLERS = 0;
  static final int CALLEES = 1;
  private Composite fParent;
  private TreeViewer fTreeViewer;
  private JJCallHierarchyContentProvider fContentProvider;
  private JJNode fNode;
  private JJEditor fJJEditor;
  private IFile fFile;

  public void createPartControl(Composite parent) {
    fParent = parent;
    parent.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));
    
    fTreeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    fTreeViewer.setAutoExpandLevel(2);
    fTreeViewer.addSelectionChangedListener(this);
    fContentProvider = new JJCallHierarchyContentProvider();
    fTreeViewer.setContentProvider(fContentProvider);
    fTreeViewer.setLabelProvider(new JJLabelProvider());
    fillActionBars();
    setFocus();
  }
  public void setFocus() {
    fParent.setFocus();
  }
  private void fillActionBars() {
    IActionBars actionBars = getViewSite().getActionBars();
    
    IAction fRefreshAction = new RefreshAction();
    actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
    
    ToggleCallModeAction toggleCallers = new ToggleCallModeAction(CALLERS);
    ToggleCallModeAction toggleCallees = new ToggleCallModeAction(CALLEES);
    toggleCallers.setChecked(true);
    
    IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(fRefreshAction);
    toolBar.add(toggleCallers);
    toolBar.add(toggleCallees);
  }
  /**
   * Part of ISelectionChangedListener
   * Called when selection changes
   */
  public void selectionChanged(SelectionChangedEvent event) {
    ISelection selection = event.getSelection();
    if (selection.isEmpty())
      fJJEditor.resetHighlightRange();
    else {
      JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
      // fJJEditor.setSelection(node); // This is OK only if JJEditor is up
      showInJJEditor(node); // Brings up JJEditor and select the node 
      // Add children to allow development
      node.buildCalleeMap();
      node.buildCallerMap();
      fTreeViewer.refresh();
    }
  }
  /**
   * Bring back JJEditor if it was closed
   * and select the node
   */
  public void showInJJEditor(JJNode node) {
    IWorkbenchWindow window = Activator.getDefault().getWorkbench()
        .getActiveWorkbenchWindow();
    if (window != null) {
      IWorkbenchPage page = window.getActivePage();
      if (page != null) 
        try {
          // Open the editor on the file we got when this JJCallHierary was opened
          IEditorPart editorPart = page.openEditor(new FileEditorInput(fFile), EDITOR_ID, true);
          JJEditor jjEditor = (JJEditor) editorPart;
          IEditorInput input = editorPart.getEditorInput();
          IDocumentProvider provider = jjEditor.getDocumentProvider();
          provider.connect(input);
          // Select the node
          jjEditor.setSelection(node);
          provider.disconnect(input);
        } catch (PartInitException e) {
          e.printStackTrace();
        } catch (CoreException e) {
          e.printStackTrace();
        }
    }
  }
  /**
   * Called from Action JJOpenCallHierarchy with selected JJNode and JJEditor
   * Also called from refresh when input or mode changed
   */
  public void setSelection(JJNode node, JJEditor editor) {
    fNode = node;
    fJJEditor = editor;
    IEditorInput editorInput = editor.getEditorInput();
    fFile = ((IFileEditorInput) editorInput).getFile();
    
    // Need a root which is not displayed
    JJNode root = new JJNode(0);
    
    // Add the node to the root as a caller but also as a callee
    root.addCaller(node); 
    root.addCallee(node); 
    node.buildCalleeMap();
    node.buildCallerMap();
    fTreeViewer.setInput(root);
  }
  public void refresh() {
    // Rebuild whole AST from root
    setSelection(fNode, fJJEditor);
  }
  public void setCallMode(int mode) {
    fContentProvider.setCallMode(mode);
    fTreeViewer.refresh();
  }
  
  /**
   * Refresh Action.
   * calls JJCallHierarchy.refresh()
   */
  class RefreshAction extends Action {
    public RefreshAction() {
      setText(CallHierarchyMessages.RefreshAction_text);
      setToolTipText(CallHierarchyMessages.RefreshAction_tooltip);
      JavaPluginImages.setLocalImageDescriptors(this, "refresh_nav.gif");//$NON-NLS-1$
      setActionDefinitionId("org.eclipse.ui.project.cleanAction"); //$NON-NLS-1$
      PlatformUI.getWorkbench().getHelpSystem().setHelp(this, 
          IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_ACTION);
    }
    public void run() {
      JJCallHierarchy.this.refresh();
    }
  }
  
  /**
   * Toggles the call direction of the call hierarchy (caller callee)
   * calls JJHierarchy.setCallMode() with JJHierachy.CALLEES or CALLERS
   */
  class ToggleCallModeAction extends Action {
    private int             fMode;

    public ToggleCallModeAction(int mode) {
      super("", AS_RADIO_BUTTON); //$NON-NLS-1$
      if (mode == CALLERS) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callers_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callers_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callers_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.gif"); //$NON-NLS-1$
      } else if (mode == CALLEES) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callees_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callees_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callees_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callees.gif"); //$NON-NLS-1$
      } else {
        Assert.isTrue(false);
      }
      fMode = mode;
      PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
          IJavaHelpContextIds.CALL_HIERARCHY_TOGGLE_CALL_MODE_ACTION);
    }
    public void run() {
      // Each button will pass it's value of fMode
      JJCallHierarchy.this.setCallMode(fMode);
    }
  }
}
