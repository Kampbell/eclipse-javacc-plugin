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

import sf.eclipse.javacc.actions.JJOpenCallHierarchy;
import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Main view for the Call hierarchy referenced by plugin.xml.<br>
 * Builds a tree of callers/callees and allows the user to double click on an entry to go to the selected
 * method.<bR>
 * Inspired from org.eclipse.jdt.internal.ui.callhierarchy and simplified to the minimum required.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
@SuppressWarnings("restriction")
public class JJCallHierarchyView extends ViewPart implements ISelectionChangedListener, IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision ; added automatic expansion when selection is changed ; managed JJEditor / JTBEditor
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : fixed NPE in setSelection() and selectionChanged()

  /** Callers mode */
  static final int                       CALLERS = 0;
  /** Callees mode */
  static final int                       CALLEES = 1;
  /** The view's parent */
  private Composite                      jParent;
  /** The created parent's tree viewer */
  private TreeViewer                     jTreeViewer;
  /** The created call hierarchy content provider */
  private JJCallHierarchyContentProvider jJJCallHierarchyContentProvider;
  /** The selected node */
  private JJNode                         jJJNode;
  /** The selected node's editor */
  private JJEditor                       jJJEditor;
  /** The edited file */
  private IFile                          jFile;

  /**
   * @see WorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(final Composite aParent) {
    jParent = aParent;
    aParent.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));
    jTreeViewer = new TreeViewer(aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    jTreeViewer.setAutoExpandLevel(2);
    jTreeViewer.addSelectionChangedListener(this);
    jJJCallHierarchyContentProvider = new JJCallHierarchyContentProvider();
    jTreeViewer.setContentProvider(jJJCallHierarchyContentProvider);
    jTreeViewer.setLabelProvider(new JJLabelProvider());
    fillActionBars();
    setFocus();
  }

  /**
   * @see WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    jParent.setFocus();
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
   * @param aEvent the event having changed the selection
   * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  @Override
  public void selectionChanged(final SelectionChangedEvent aEvent) {
    final ISelection selection = aEvent.getSelection();
    if (selection.isEmpty()) {
      jJJEditor.resetHighlightRange();
    }
    else {
      final JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
      if (node != null && node != JJNode.getOohsjjnode()) {
        // This is OK only if JJEditor is up
        // fJJEditor.setSelection(node); 
        // Bring up JJEditor and select the node 
        showInJJEditor(node);
        // add children and expand one level
        node.buildCallees();
        node.buildCallers();
        jTreeViewer.expandToLevel(node, 1);
        jTreeViewer.refresh();
      }
    }
  }

  /**
   * Brings back JJEditor if it was closed and selects the node.
   * 
   * @param aJJNode the node to select
   */
  public void showInJJEditor(final JJNode aJJNode) {
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        try {
          // open the editor on the file we got when this JJCallHierary was opened
          final String edid = "jtb".equals(jFile.getFileExtension()) ? JTBEDITOR_ID : JJEDITOR_ID; //$NON-NLS-1$
          final IEditorPart editorPart = page.openEditor(new FileEditorInput(jFile), edid, true);
          final JJEditor jjEditor = (JJEditor) editorPart;
          final IEditorInput input = editorPart.getEditorInput();
          final IDocumentProvider provider = jjEditor.getDocumentProvider();
          provider.connect(input);
          // select the node
          jjEditor.setSelection(aJJNode);
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
   * @param aJJNode the selected node
   * @param aJJEditor the selected node's editor
   */
  public void setSelection(final JJNode aJJNode, final JJEditor aJJEditor) {
    jJJNode = aJJNode;
    jJJEditor = aJJEditor;
    if (aJJEditor == null) {
      return;
    }
    final IEditorInput editorInput = aJJEditor.getEditorInput();
    jFile = ((IFileEditorInput) editorInput).getFile();

    // need a root which is not displayed
    final JJNode root = new JJNode(0);

    if (aJJNode == null) {
      // empty view
      root.clearCallers();
      root.clearCallees();
    }
    else {
      // add the node to the root as a caller and a callee
      root.addCaller(aJJNode, true);
      root.addCallee(aJJNode);
      aJJNode.buildCallees();
      aJJNode.buildCallers();
    }
    jTreeViewer.setInput(root);
  }

  /**
   * Rebuilds the whole AST from the root.
   */
  public void refresh() {
    setSelection(jJJNode, jJJEditor);
  }

  /**
   * Sets the given mode and refreshes the view.
   * 
   * @param aMode the mode to set
   */
  public void setCallMode(final int aMode) {
    jJJCallHierarchyContentProvider.setCallMode(aMode);
    jTreeViewer.refresh();
  }

  /**
   * Refresh Action. Calls JJCallHierarchy.refresh()
   */
  class RefreshAction extends Action {

    /**
     * Creates a refresh action.
     */
    public RefreshAction() {
      // for Eclipse 3.5.x
      //      setText(CallHierarchyMessages.RefreshAction_text);
      // for Eclipse 3.6+
      setText(CallHierarchyMessages.RefreshViewAction_text);
      // for Eclipse 3.5.x
      //      setToolTipText(CallHierarchyMessages.RefreshAction_tooltip);
      // for Eclipse 3.6+
      setToolTipText(CallHierarchyMessages.RefreshViewAction_tooltip);
      JavaPluginImages.setLocalImageDescriptors(this, "refresh_nav.gif"); //$NON-NLS-1$
      setActionDefinitionId("org.eclipse.ui.project.cleanAction"); //$NON-NLS-1$
      // for Eclipse 3.5.x
      //      PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
      //                                                        IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_ACTION);
      // for Eclipse 3.6+
      PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_VIEW_ACTION);
    }

    /**
     * @see Action#run()
     */
    @Override
    public void run() {
      JJCallHierarchyView.this.refresh();
    }
  }

  /**
   * Toggle action. Toggles the call direction of the call hierarchy (caller / callee). Calls
   * {@link JJCallHierarchyView#setCallMode(int)} with {@linkJJHierachy.CALLERS} or {@linkJJHierachy.CALLEES}
   */
  class ToggleCallModeAction extends Action {

    /** The caller / callee mode */
    private final int mode;

    /**
     * Toggles the call mode.
     * 
     * @param aMode the caller or callee mode
     */
    public ToggleCallModeAction(final int aMode) {
      super("", AS_RADIO_BUTTON); //$NON-NLS-1$
      if (aMode == CALLERS) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callers_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callers_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callers_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.gif"); //$NON-NLS-1$
      }
      else if (aMode == CALLEES) {
        setText(CallHierarchyMessages.ToggleCallModeAction_callees_label);
        setDescription(CallHierarchyMessages.ToggleCallModeAction_callees_description);
        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callees_tooltip);
        JavaPluginImages.setLocalImageDescriptors(this, "ch_callees.gif"); //$NON-NLS-1$
      }
      else {
        Assert.isTrue(false);
      }
      mode = aMode;
      PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_TOGGLE_CALL_MODE_ACTION);
    }

    /**
     * @see Action#run()
     */
    @Override
    public void run() {
      // each button will pass it's value of mode
      JJCallHierarchyView.this.setCallMode(mode);
    }
  }
}
