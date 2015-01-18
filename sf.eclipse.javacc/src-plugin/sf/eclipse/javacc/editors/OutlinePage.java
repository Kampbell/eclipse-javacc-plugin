package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.OUTLINE_SYNC;
import static sf.eclipse.javacc.base.IConstants.PLUGIN_QN;
import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Content Outline Page for the JJEditor.<br>
 * The chosen synchronization mode is stored under the project of the current JJEdited file.
 * 
 * @see "http://www.eclipse.org/articles/Article-TreeViewer/TreeViewerArticle.htm"
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class OutlinePage extends ContentOutlinePage {

  // MMa 11/2009 : javadoc and formatting revision ; changed sorting categories ;
  // ........... : added javacode and token_mgr_decls entries ; moved lexical states at the kind's right
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : added new expand buttons / actions
  // BF  06/2012 : added NON-NLS tag
  // MMa 10/2012 : used static import ; renamed ; added revealing of surrounding node ; updated icons ;
  //               use node instead of document as input ; added toggle synchronization and refresh actions ;
  //               renamed
  // MMa 10/2014 : added call to  actionBars.updateActionBars()
  // MMa 12/2014 : changed to always reveal a node

  /** The editor corresponding to the document */
  final JJEditor                       jEditor;

  /** The label provider to use */
  private NodeLabelProvider            jNLP;

  /** The content provider to use */
  private final OutlineContentProvider jOCP;

  /** Flag to tell whether the user wants to synchronize the page with the editor */
  boolean                              jSyncWithEditor = true;

  /** Flag to tell whether we are revealing a node or not (in order to avoid cyclic events) */
  protected boolean                    jRevealing      = false;

  /** The current expand level : {@link #LEVEL_ALL} : all, {@link #LEVEL_ONE} : first level, .. */
  int                                  ejExpandLevel   = LEVEL_ONE;

  /** The expand level corresponding to all */
  protected static final int           LEVEL_ALL       = 0;

  /** The expand level corresponding to everything collapsed = first level */
  protected static final int           LEVEL_ONE       = 1;

  /**
   * Creates a content Outline Page using the given editor (and a newly created content provider).
   * 
   * @param aTextEditor - the given editor
   */
  public OutlinePage(final ITextEditor aTextEditor) {
    super();
    jEditor = (JJEditor) aTextEditor;
    jOCP = new OutlineContentProvider();

  }

  /** {@inheritDoc} */
  @Override
  public void createControl(final Composite aParent) {
    // creates the TreeViewer and adds the selection changed listener
    super.createControl(aParent);
    final TreeViewer tv = getTreeViewer();
    tv.setContentProvider(jOCP);
    jNLP = new NodeLabelProvider();
    tv.setLabelProvider(jNLP);
    tv.setUseHashlookup(true);

    final IActionBars actionBars = getSite().getActionBars();
    // add the refresh button to global actions
    final IAction fRefreshAction = new RefreshAction();
    actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
    actionBars.updateActionBars();
    // add the buttons to the viewer's toolbar
    final IToolBarManager toolBar = actionBars.getToolBarManager();
    toolBar.add(new RefreshAction());
    toolBar.add(new CollapseAllAction(tv));
    toolBar.add(new Expand1LevelAction(tv));
    toolBar.add(new ExpandAllAction(tv));
    toolBar.add(new AlphabeticSortingAction(tv));
    final ToggleSynchronizingAction tsa = new ToggleSynchronizingAction();
    tsa.setChecked(jSyncWithEditor);
    toolBar.add(tsa);

    // update the TreeViewer (each time the control is recreated)
    final JJNode root = jEditor.getAstRoot();
    setNodes(root, root);
  }

  /** {@inheritDoc} */
  @Override
  public void selectionChanged(final SelectionChangedEvent aEvent) {
    if (!jRevealing) {
      // event comes from the Outline Page
      if (jSyncWithEditor) {
        final ISelection selection = aEvent.getSelection();
        if (selection.isEmpty()) {
          jEditor.resetHighlightRange();
        }
        else {
          final JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
          jEditor.selectNode(node);
        }
      }
    }
    else {
      // event comes from the editor
    }
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
   * Reveals a node in the Outline Page.
   * 
   * @param aNode - the node to be revealed
   */
  private void revealNode(final JJNode aNode) {
    if (jSyncWithEditor) {
      final TreeViewer tv = getTreeViewer();
      if (tv != null) {
        if (aNode != null) {
          jRevealing = true;
          tv.setSelection(new StructuredSelection(aNode), true);
          jRevealing = false;
        }
        else {
          // case selection between nodes (ie blank lines) : do not show any node
          jRevealing = true;
          tv.setSelection(null, false);
          jRevealing = false;
        }
      }
    }
  }

  /**
   * Sets the input of the Outline Page and reveals a node.<br>
   * The {@link TreeViewer#setInput(Object)} will trigger
   * {@link OutlineContentProvider#inputChanged(Viewer, Object, Object)}.
   * 
   * @param aRoot - the AST root node
   * @param aRevealed - the node to reveal
   */
  protected void setNodes(final JJNode aRoot, final JJNode aRevealed) {
    if (aRoot == null) {
      return;
    }
    final TreeViewer tv = getTreeViewer();
    if (tv != null) {
      // aRoot should be the same as the one from the jEditor
      //      final JJNode edRoot = jEditor.getAstRoot();
      final JJNode tcpRoot = jOCP.getAstRoot();
      if (!aRoot.equals(tcpRoot)) {
        jRevealing = true;
        tv.setInput(aRoot);
        final Control control = tv.getControl();
        if (control != null && !control.isDisposed()) {
          control.setRedraw(false);
          tv.refresh(aRoot, true);
          control.setRedraw(true);
        }
        else {
          tv.refresh(aRoot, true);
        }
      }
      revealNode(aRevealed);
    }
  }

  /**
   * @return the syncFromEditor
   */
  protected final boolean isSyncWithEditor() {
    return jSyncWithEditor;
  }

  /**
   * Ask the editor to rebuild the outline.
   */
  void refresh() {
    jEditor.updateOutlinePage();
  }

  /**
   * Inner class to collapse the tree.
   */
  class CollapseAllAction extends Action {

    /** The tree viewer to use */
    protected final TreeViewer jTreeViewer;

    /**
     * Collapses all.
     * 
     * @param aTreeViewer - the tree viewer to use
     */
    CollapseAllAction(final TreeViewer aTreeViewer) {
      super(AbstractActivator.getMsg("OutlinePage.Collapse_all_Action")); //$NON-NLS-1$
      jTreeViewer = aTreeViewer;
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_collapse_all.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_collapse_all.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_collapse_all.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Collapse_all_TT")); //$NON-NLS-1$
    }

    /**
     * Runs the action (collapse all).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      jTreeViewer.collapseAll();
      ejExpandLevel = LEVEL_ONE;
    }
  }

  /**
   * Inner class to fully expand the tree.
   */
  class ExpandAllAction extends Action {

    /** The tree viewer to use */
    protected final TreeViewer jTreeViewer;

    /**
     * Expands all.
     * 
     * @param aTreeViewer - the tree viewer to use
     */
    ExpandAllAction(final TreeViewer aTreeViewer) {
      super(AbstractActivator.getMsg("OutlinePage.Expand_all_Action")); //$NON-NLS-1$
      jTreeViewer = aTreeViewer;
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_expand_all.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_expand_all.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_expand_all.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Expand_all_TT")); //$NON-NLS-1$
    }

    /**
     * Runs the action (expand all).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      jTreeViewer.expandAll();
      ejExpandLevel = LEVEL_ALL;
    }
  }

  /**
   * Inner class to expand to one level deeper the tree.
   */
  class Expand1LevelAction extends Action {

    /** The tree viewer to use */
    protected final TreeViewer jTreeViewer;

    /**
     * Expands to one level deeper.
     * 
     * @param aTreeViewer - the tree viewer to use
     */
    Expand1LevelAction(final TreeViewer aTreeViewer) {
      super(AbstractActivator.getMsg("OutlinePage.Expand_1_Action")); //$NON-NLS-1$
      jTreeViewer = aTreeViewer;
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_expand_1.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_expand_1.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_expand_1.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Expand_1_TT")); //$NON-NLS-1$
    }

    /**
     * Runs the action (expand to one level deeper).
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      if (ejExpandLevel != LEVEL_ALL) {
        ejExpandLevel++;
      }
      jTreeViewer.expandToLevel(ejExpandLevel);
    }
  }

  /**
   * An Action to sort the tree alphabetically.
   */
  class AlphabeticSortingAction extends Action {

    /** The viewer sorter to use */
    protected final ViewerSorter     jViewerSorter = new OutlineSorter();
    /** The structured viewer to use */
    protected final StructuredViewer jStructuredViewer;
    /** The previous check status for the action */
    protected boolean                oldState      = false;

    /**
     * Constructor for LexicalSortingAction.
     * 
     * @param aTreeViewer - the structured viewer to use
     */
    AlphabeticSortingAction(final StructuredViewer aTreeViewer) {
      super(AbstractActivator.getMsg("OutlinePage.Sort_Action"), AS_RADIO_BUTTON); //$NON-NLS-1$
      jStructuredViewer = aTreeViewer;
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_alphab_sort_co.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_alphab_sort_co.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_alphab_sort_co.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Sort_TT")); //$NON-NLS-1$
    }

    /**
     * Runs the action.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      if (oldState == true) {
        jStructuredViewer.setSorter(null);
        setChecked(false);
      }
      else {
        jStructuredViewer.setSorter(jViewerSorter);
      }
      oldState = isChecked();
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
      super(AbstractActivator.getMsg("OutlinePage.Toggle_Sync_Action"), AS_CHECK_BOX); //$NON-NLS-1$
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_synced.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_synced.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_synced.gif")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Toggle_Sync_TT")); //$NON-NLS-1$
      if (jEditor != null) {
        final IEditorInput editorInput = jEditor.getEditorInput();
        if (editorInput != null && editorInput instanceof IFileEditorInput) {
          final IFile file = ((IFileEditorInput) editorInput).getFile();
          if (file != null) {
            final IEclipsePreferences prefs = new ProjectScope(file.getProject()).getNode(PLUGIN_QN);
            // set according to project persistent properties
            jSyncWithEditor = prefs.getBoolean(OUTLINE_SYNC, true);
          }
        }
      }
    }

    /**
     * Runs the action.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
      jSyncWithEditor = isChecked();
      if (jEditor != null) {
        final IEditorInput editorInput = jEditor.getEditorInput();
        if (editorInput != null && editorInput instanceof IFileEditorInput) {
          final IFile file = ((IFileEditorInput) editorInput).getFile();
          if (file != null) {
            final IEclipsePreferences prefs = new ProjectScope(file.getProject()).getNode(PLUGIN_QN);
            // set to project persistent properties
            prefs.putBoolean(OUTLINE_SYNC, jSyncWithEditor);
            try {
              prefs.flush();
            } catch (final BackingStoreException e) {
              AbstractActivator.logBug(e);
            }
          }
        }
      }
    }

  }

  /**
   * Refresh Action.
   */
  class RefreshAction extends Action {

    /**
     * Creates a refresh action.
     */
    RefreshAction() {
      // Refresh view
      super(AbstractActivator.getMsg("OutlinePage.Refresh_Action")); //$NON-NLS-1$
      setToolTipText(AbstractActivator.getMsg("OutlinePage.Refresh_Action_TT")); //$NON-NLS-1$
      setImageDescriptor(AbstractActivator.getImageDescriptor("en_refresh_nav.gif")); //$NON-NLS-1$
      setHoverImageDescriptor(AbstractActivator.getImageDescriptor("en_refresh_nav.gif")); //$NON-NLS-1$
      setDisabledImageDescriptor(AbstractActivator.getImageDescriptor("di_refresh_nav.gif")); //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
      refresh();
    }

  }

  /**
   * Outline Sorter to perform the real sorting.
   */
  static class OutlineSorter extends ViewerSorter {

    // sorting is most of the time useful for bnf productions, not token definitions
    /** The rules outline category */
    public static final int RULES           = -1;
    /** The options outline category */
    public static final int OPTIONS         = 0;
    /** The parser outline category */
    public static final int PARSER          = 1;
    /** The javacode outline category */
    public static final int JAVACODE        = 2;
    /** The token_mgr_decls outline category */
    public static final int TOKEN_MGR_DECLS = 3;
    /** The regexpr_kind outline category */
    public static final int REGEXPR_KIND    = 4;

    /** {@inheritDoc} */
    @Override
    public int category(final Object aElement) {
      int category = 0;
      final JJNode node = (JJNode) aElement;
      final int ndId = node.getId();
      if (ndId == JJTJAVACC_OPTIONS) {
        category = OPTIONS;
      }
      else if (ndId == JJTPARSER_BEGIN) {
        category = PARSER;
      }
      else if (ndId == JJTJAVACODE_PROD) {
        category = JAVACODE;
      }
      else if (ndId == JJTTOKEN_MANAGER_DECLS) {
        category = TOKEN_MGR_DECLS;
      }
      else if (ndId == JJTREGULAR_EXPR_PROD) {
        category = REGEXPR_KIND;
      }
      else {
        category = RULES;
      }
      return category;
    }

    /** {@inheritDoc} */
    @Override
    public int compare(final Viewer aViewer, final Object aObject1, final Object aObject2) {
      int result;
      final int cat1 = category(aObject1);
      final int cat2 = category(aObject2);
      if (cat1 < cat2) {
        result = -1;
      }
      else if (cat1 > cat2) {
        result = 1;
      }
      else {
        final ILabelProvider lprov = (ILabelProvider) ((ContentViewer) aViewer).getLabelProvider();
        result = lprov.getText(aObject1).compareTo(lprov.getText(aObject2));
      }
      return result;
    }
  }

}
