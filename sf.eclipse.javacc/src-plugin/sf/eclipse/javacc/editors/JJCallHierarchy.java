package sf.eclipse.javacc.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.actions.JJOpenCallHierarchy;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Main view for the Call hierarchy referenced by plugin.xml.<br>
 * Builds a tree of callers/callees and allows the user to double click on an entry to go to the selected
 * method.<bR>
 * Inspired from org.eclipse.jdt.internal.ui.callhierarchy and simplified to the minimum required.
 * 
 * @author Remi Koutcherawy 2008-2009 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
@SuppressWarnings("restriction")
public class JJCallHierarchy extends ViewPart implements ISelectionChangedListener, IJJConstants {

  // MMa 11/09 : javadoc and formatting revision ; added automatic expansion when selection is changed ; managed JJEditor / JTBEditor

  /** callers mode */
  static final int                       CALLERS = 0;
  /** callees mode */
  static final int                       CALLEES = 1;
  /** the view's parent */
  private Composite                      fParent;
  /** the created parent's tree viewer */
  private TreeViewer                     fTreeViewer;
  /** the created call hierarchy content provider */
  private JJCallHierarchyContentProvider fContentProvider;
  /** the selected node */
  private JJNode                         fNode;
  /** the selected node's editor */
  private JJEditor                       fJJEditor;
  /** the edited file */
  private IFile                          fFile;

  /**
   * @see WorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(final Composite parent) {
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

  /**
   * @see WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    fParent.setFocus();
  }

  /**
   * Fills the action bars.
   */
  private void fillActionBars() {
    final IActionBars actionBars = getViewSite().getActionBars();

    final IAction fRefreshAction = new RefreshAction();
    actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);

    final ToggleCallModeAction toggleCallers = new ToggleCallModeAction(CALLERS);
    final ToggleCallModeAction toggleCallees = new ToggleCallModeAction(CALLEES);
    toggleCallers.setChecked(true);

    final IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(fRefreshAction);
    toolBar.add(toggleCallers);
    toolBar.add(toggleCallees);
  }

  /**
   * Called when selection changes.
   * 
   * @param event the event having changed the selection
   * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(final SelectionChangedEvent event) {
    final ISelection selection = event.getSelection();
    if (selection.isEmpty()) {
      fJJEditor.resetHighlightRange();
    }
    else {
      final JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
      // fJJEditor.setSelection(node); // This is OK only if JJEditor is up
      showInJJEditor(node); // Bring up JJEditor and select the node 
      // Add children and expand one level
      node.buildCalleeMap();
      node.buildCallerMap();
      fTreeViewer.expandToLevel(node, 1);
      fTreeViewer.refresh();
    }
  }

  /**
   * Brings back JJEditor if it was closed and selects the node.
   * 
   * @param node the node to select
   */
  public void showInJJEditor(final JJNode node) {
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        try {
          // Open the editor on the file we got when this JJCallHierary was opened
          final String edid = "jtb".equals(fFile.getFileExtension()) ? JTBEDITOR_ID : JJEDITOR_ID; //$NON-NLS-1$
          final IEditorPart editorPart = page.openEditor(new FileEditorInput(fFile), edid, true);
          final JJEditor jjEditor = (JJEditor) editorPart;
          final IEditorInput input = editorPart.getEditorInput();
          final IDocumentProvider provider = jjEditor.getDocumentProvider();
          provider.connect(input);
          // Select the node
          jjEditor.setSelection(node);
          provider.disconnect(input);
        } catch (final PartInitException e) {
          e.printStackTrace();
        } catch (final CoreException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Called from {@link JJOpenCallHierarchy#run(IAction)} on an action and from {@link #refresh()} when input
   * or mode is changed.
   * 
   * @param node the selected node
   * @param editor the selected node's editor
   */
  public void setSelection(final JJNode node, final JJEditor editor) {
    fNode = node;
    fJJEditor = editor;
    final IEditorInput editorInput = editor.getEditorInput();
    fFile = ((IFileEditorInput) editorInput).getFile();

    // Need a root which is not displayed
    final JJNode root = new JJNode(0);

    // Add the node to the root as a caller and a callee
    root.addCaller(node);
    root.addCallee(node);
    node.buildCalleeMap();
    node.buildCallerMap();
    fTreeViewer.setInput(root);
  }

  /**
   * Rebuilds the whole AST from the root.
   */
  public void refresh() {
    setSelection(fNode, fJJEditor);
  }

  /**
   * Sets the given mode and refreshes the view.
   * 
   * @param mode the mode to set
   */
  public void setCallMode(final int mode) {
    fContentProvider.setCallMode(mode);
    fTreeViewer.refresh();
  }

  /**
   * Refresh Action. Calls JJCallHierarchy.refresh()
   */
  class RefreshAction extends Action {

    /**
     * Creates a refresh action.
     */
    public RefreshAction() {
      setText(CallHierarchyMessages.RefreshAction_text);
      setToolTipText(CallHierarchyMessages.RefreshAction_tooltip);
      JavaPluginImages.setLocalImageDescriptors(this, "refresh_nav.gif"); //$NON-NLS-1$
      setActionDefinitionId("org.eclipse.ui.project.cleanAction"); //$NON-NLS-1$
      PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                                                        IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_ACTION);
    }

    /**
     * @see Action#run()
     */
    @Override
    public void run() {
      JJCallHierarchy.this.refresh();
    }
  }

  /**
   * Toggle action. Toggles the call direction of the call hierarchy (caller / callee). Calls
   * {@link JJCallHierarchy#setCallMode(int)} with {@linkJJHierachy.CALLERS} or {@linkJJHierachy.CALLEES}
   */
  class ToggleCallModeAction extends Action {

    /** the caller / callee mode */
    private final int fMode;

    /**
     * Toggles the call mode.
     * 
     * @param mode the caller or calle mode
     */
    public ToggleCallModeAction(final int mode) {
      super("", AS_RADIO_BUTTON); //$NON-NLS-1$
      if (mode == CALLERS) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callers_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callers_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callers_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.gif"); //$NON-NLS-1$
      }
      else if (mode == CALLEES) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callees_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callees_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callees_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callees.gif"); //$NON-NLS-1$
      }
      else {
        Assert.isTrue(false);
      }
      fMode = mode;
      PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_TOGGLE_CALL_MODE_ACTION);
    }

    /**
     * @see Action#run()
     */
    @Override
    public void run() {
      // Each button will pass it's value of fMode
      JJCallHierarchy.this.setCallMode(fMode);
    }
  }
}
