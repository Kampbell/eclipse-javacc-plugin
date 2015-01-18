package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.CALL_HIERARCHY_SYNC;
import static sf.eclipse.javacc.base.IConstants.JJEDITOR_ID;
import static sf.eclipse.javacc.base.IConstants.PLUGIN_QN;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.handlers.ShowCallHierarchy;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Main view for the {@link ShowCallHierarchy} referenced by plugin.xml.<br>
 * Builds trees of callers/callees and allows the user to double click on an entry to go to the selected
 * method.<br>
 * Inspired from org.eclipse.jdt.internal.ui.callhierarchy and simplified to the minimum required.<br>
 * The chosen synchronization mode is stored under the project of the current JJEdited file.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class CallHierarchyView extends ViewPart implements ISelectionChangedListener {

  // MMa 11/2009 : javadoc and formatting revision ; added automatic expansion when selection is changed ;
  //                managed JJEditor / JTBEditor
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : fixed NPE in setSelection() and selectionChanged()
  // MMa 09/2012 : localized tooltip texts
  // MMa 10/2012 : modified to get rid of restrictions on jdt.internal packages by extracting icons and
  //                messages from them ; added toggle synchronization action ; renamed
  // MMa 10/2014 : removed reference to JTBEDITOR_ID no more defined
  // MMa 11/2014 : fixed synchronization with JJEditor ; added dispose() ; added some final modifiers ;
  //               fixed behavior with drag and dropped files ; fixed reopening a closed editor

  /** Callers mode */
  protected static final int             CALLERS        = 0;

  /** Callees mode */
  protected static final int             CALLEES        = 1;

  /** The view's parent */
  protected Composite                    jParent;

  /** The created parent's tree viewer */
  protected TreeViewer                   jTreeViewer;

  /** The created Call Hierarchy content provider */
  protected CallHierarchyContentProvider jCHCP;

  /** The label provider to use */
  protected NodeLabelProvider            jNLP;

  /** The selected node */
  protected JJNode                       jNode;

  /** The selected node's editor */
  protected JJEditor                     jEditor;

  /** The edited file */
  protected IFile                        jFile;

  /** Flag to tell whether the user wants to synchronize the view with the editor */
  protected boolean                      syncWithEditor = true;

  /** The synchronization action */
  ToggleSynchronizingAction              jTsa;

  /** {@inheritDoc} */
  @Override
  public void createPartControl(final Composite aParent) {
    jParent = aParent;
    aParent.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));
    jTreeViewer = new TreeViewer(aParent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
    jTreeViewer.setAutoExpandLevel(2);
    jTreeViewer.addSelectionChangedListener(this);
    jCHCP = new CallHierarchyContentProvider();
    jTreeViewer.setContentProvider(jCHCP);
    jNLP = new NodeLabelProvider();
    jTreeViewer.setLabelProvider(jNLP);
    fillActionBars();
    setFocus();
  }

  /** {@inheritDoc} */
  @Override
  public final void setFocus() {
    jParent.setFocus();
  }

  /**
   * Fills the action bars.
   */
  private void fillActionBars() {
    final IActionBars actionBars = getViewSite().getActionBars();
    // add the refresh button to global actions
    final IAction fRefreshAction = new RefreshAction();
    actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
    actionBars.updateActionBars();
    // add the buttons to the viewer's toolbar
    final ToggleCallModeAction toggleCallers = new ToggleCallModeAction(CALLERS);
    final ToggleCallModeAction toggleCallees = new ToggleCallModeAction(CALLEES);
    toggleCallers.setChecked(true);
    final IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(fRefreshAction);
    toolBar.add(toggleCallers);
    toolBar.add(toggleCallees);
    jTsa = new ToggleSynchronizingAction();
    toolBar.add(jTsa);
  }

  /**
   * Called when selection changes.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void selectionChanged(final SelectionChangedEvent aEvent) {
    if (syncWithEditor) {
      final ISelection selection = aEvent.getSelection();
      if (selection.isEmpty()) {
        jEditor.resetHighlightRange();
      }
      else {
        final JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
        if (node != null && node != JJNode.getOohsjjnode()) {
          // find or bring up JJEditor and select the node 
          showNodeInJJEditor(node);
          // add children and expand one level
          node.buildCallees(jEditor.getElements());
          node.buildCallers();
          jTreeViewer.expandToLevel(node, 1);
          jTreeViewer.refresh();
        }
      }
    }
  }

  /**
   * Finds or brings back the JJEditor if it was closed on a project file and selects the node.
   * 
   * @param aJJNode - the node to select
   */
  private void showNodeInJJEditor(final JJNode aJJNode) {
    final IWorkbenchWindow window = AbstractActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        try {
          JJEditor editor = jEditor;
          if (editor == null) {
            if (jFile == null) {
              // could be after a drag and drop
              AbstractActivator.logErr("null file"); //$NON-NLS-1$
              return;
            }
            // open the editor on the file we got when this CallHierary was opened
            final IEditorPart editorPart = page.openEditor(new FileEditorInput(jFile), JJEDITOR_ID, true);
            jEditor = editor = (JJEditor) editorPart;
          }
          final IEditorInput input = editor.getEditorInput();
          final IDocumentProvider provider = editor.getDocumentProvider();
          provider.connect(input);
          // select the node
          editor.selectNode(aJJNode);
          provider.disconnect(input);
        } catch (final PartInitException e) {
          AbstractActivator.logBug(e);
        } catch (final CoreException e) {
          AbstractActivator.logBug(e);
        }
      }
    }
  }

  /**
   * Called from {@link ShowCallHierarchy#execute(ExecutionEvent)} on an action and from
   * {@link JJEditor#updateCallHierarchyView()} and from {@link #refresh()} when input or mode is changed.
   * 
   * @param aJJNode - the selected node
   * @param aJJEditor - the selected node's editor
   */
  public void setSelection(final JJNode aJJNode, final JJEditor aJJEditor) {
    if (aJJEditor == null) {
      return;
    }
    final IEditorInput editorInput = aJJEditor.getEditorInput();
    // case project file
    if (editorInput instanceof IFileEditorInput) {
      jFile = ((IFileEditorInput) editorInput).getFile();
      // see if needed to initialize the synchronization from last saved state
      if ((jEditor == null)) {
        jTsa.initSyncFlag();
      }
      jEditor = aJJEditor;
      setSelection(aJJNode);
    }
    else if (editorInput instanceof FileStoreEditorInput) {
      // case dragged and dropped file
      jEditor = aJJEditor;
      setSelection(aJJNode);
    }
  }

  /**
   * Called from {@link CallHierarchyView#setSelection(JJNode, JJEditor)}.
   * 
   * @param aJJNode - the node to reveal
   */
  void setSelection(final JJNode aJJNode) {
    jNode = aJJNode;
    // need a root which is not displayed
    final JJNode root = new JJNode(0);
    if (aJJNode == null) {
      // show an "empty" view
      root.clearCallers();
      root.clearCallees();
    }
    else {
      // add the node to the root as a caller and a callee
      root.addCaller(aJJNode, true);
      root.addCallee(aJJNode);
      aJJNode.buildCallees(jEditor.getElements());
      aJJNode.buildCallers();
    }
    jTreeViewer.setInput(root);
  }

  /**
   * Rebuilds the whole AST from the root.
   */
  void refresh() {
    setSelection(jNode, jEditor);
  }

  /**
   * Sets the given mode and refreshes the view.
   * 
   * @param aMode - the mode to set
   */
  void setCallMode(final int aMode) {
    jCHCP.setCallMode(aMode);
    jTreeViewer.refresh();
  }

  /**
   * @param aFile - the edited file
   * @return the syncFromEditor
   */
  protected final boolean isSyncWithEditor(final IFile aFile) {
    if (jFile != aFile) {
      jTsa.initSyncFlag();
    }
    return syncWithEditor;
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    if (jNLP != null) {
      jNLP.dispose();
      jNLP = null;
    }
    super.dispose();
  }

  /**
   * Refresh Action. Calls CallHierarchy.refresh()
   */
  class RefreshAction extends Action {

    /**
     * Creates a refresh action.
     */
    RefreshAction() {
      // changed, our own tooltip text
      //      // for Eclipse 3.5.x
      //      //      setText(CallHierarchyMessages.RefreshAction_text);
      //      // for Eclipse 3.6+
      //      setText(CallHierarchyMessages.RefreshViewAction_text);
      //      // for Eclipse 3.5.x
      //      //      setToolTipText(CallHierarchyMessages.RefreshAction_TT);
      //      // for Eclipse 3.6+
      //      setToolTipText(CallHierarchyMessages.RefreshViewAction_TT);
      // Refresh view
      setToolTipText(AbstractActivator.getMsg("CallHier.RefreshViewAction_TT")); //$NON-NLS-1$
      //      JavaPluginImages.setLocalImageDescriptors(this, "refresh_nav.gif"); //$NON-NLS-1$
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_refresh_nav.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_refresh_nav.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_refresh_nav.gif")); //$NON-NLS-1$
      // for Eclipse 3.5.x
      //      PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
      //                                                        IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_ACTION);
      // for Eclipse 3.6+
      //      PlatformUI.getWorkbench().getHelpSystem()
      //                .setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_VIEW_ACTION);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
      refresh();
    }

  }

  /**
   * Toggle action. Toggles the call direction of the Call Hierarchy (caller / callee). Calls
   * {@link CallHierarchyView#setCallMode(int)} with {@link CallHierarchyView#CALLERS} or
   * {@link CallHierarchyView#CALLEES}.
   */
  class ToggleCallModeAction extends Action {

    /** The caller / callee mode */
    protected final int mode;

    /**
     * Toggles the call mode.
     * 
     * @param aMode - the caller or callee mode
     */
    ToggleCallModeAction(final int aMode) {
      super("", AS_RADIO_BUTTON); //$NON-NLS-1$
      if (aMode == CALLERS) {
        // Caller Hierarchy
        //        setText(CallHierarchyMessages.ToggleCallModeAction_callers_label);
        // Show the Caller Hierarchy
        //        setDescription(CallHierarchyMessages.ToggleCallModeAction_callers_description);
        //        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callers_TT);
        setToolTipText(AbstractActivator.getMsg("CallHier.TCAM_callers_TT")); //$NON-NLS-1$
        //        JavaPluginImages.setLocalImageDescriptors(this, "ch_callers.gif"); //$NON-NLS-1$
        setImageDescriptor(AbstractActivator.getImageDescriptor("en_ch_callers.gif")); //$NON-NLS-1$
        setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_ch_callers.gif")); //$NON-NLS-1$
        setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_ch_callers.gif")); //$NON-NLS-1$
      }
      else if (aMode == CALLEES) {
        // Callee Hierarchy
        //        setText(CallHierarchyMessages.ToggleCallModeAction_callees_label);
        // Show the Callee Hierarchy
        //        setDescription(CallHierarchyMessages.ToggleCallModeAction_callees_description);
        //        setToolTipText(CallHierarchyMessages.ToggleCallModeAction_callees_TT);
        setToolTipText(AbstractActivator.getMsg("CallHier.TCAM_callees_TT")); //$NON-NLS-1$
        //        JavaPluginImages.setLocalImageDescriptors(this, "ch_callees.gif"); //$NON-NLS-1$
        setImageDescriptor(AbstractActivator.getImageDescriptor("en_ch_callees.gif")); //$NON-NLS-1$
        setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_ch_callees.gif")); //$NON-NLS-1$
        setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_ch_callees.gif")); //$NON-NLS-1$
      }
      else {
        Assert.isTrue(false);
      }
      mode = aMode;
      //      PlatformUI.getWorkbench().getHelpSystem()
      //                .setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_TOGGLE_CALL_MODE_ACTION);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
      // each button will pass it's value of mode
      setCallMode(mode);
    }
  }

  /**
   * An Action to toggle the synchronization with the editor.
   */
  class ToggleSynchronizingAction extends Action {

    /**
     * Constructor.
     */
    ToggleSynchronizingAction() {
      super(AbstractActivator.getMsg("CallHier.Toggle_Sync_Action"), AS_CHECK_BOX); //$NON-NLS-1$
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_synced.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_synced.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_synced.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("CallHier.Toggle_Sync_TT")); //$NON-NLS-1$
    }

    /**
     * Initializes the synchronize from the editor flag and the widget from the project preferences.
     */
    void initSyncFlag() {
      if (jFile != null) {
        final IEclipsePreferences prefs = new ProjectScope(jFile.getProject()).getNode(PLUGIN_QN);
        // get from project persistent properties
        syncWithEditor = prefs.getBoolean(CALL_HIERARCHY_SYNC, false);
        jTsa.setChecked(syncWithEditor);
      }
    }

    /**
     * Runs the action.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      syncWithEditor = isChecked();
      if (jFile != null) {
        final IEclipsePreferences prefs = new ProjectScope(jFile.getProject()).getNode(PLUGIN_QN);
        // set to project persistent properties
        prefs.putBoolean(CALL_HIERARCHY_SYNC, syncWithEditor);
        try {
          prefs.flush();
        } catch (final BackingStoreException e) {
          AbstractActivator.logBug(e);
        }
      }
    }

  }

}
