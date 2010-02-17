package sf.eclipse.javacc.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * Content outline page for the JJEditor.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJOutlinePage extends ContentOutlinePage {

  // MMa 11/2009 : javadoc and formatting revision ; changed sorting categories ;
  // ........... : added javacode and token_mgr_decls entries ; moved lexical states at the kind's right
  // MMa 02/2010 : formatting and javadoc revision

  /** The document to outline */
  protected IDocument                        fDocument;
  /** The editor to outline */
  protected JJEditor                         fEditor;
  /** The label provider to use */
  private JJLabelProvider                    fLabelProvider;
  /** The content provider to use */
  private final JJOutlinePageContentProvider fContentProvider;

  /**
   * Inner class to compact the tree
   */
  public class CollapseAllAction extends Action {

    /** The tree viewer to use */
    private final TreeViewer fTreeViewer;

    /**
     * Collapses all a branch.
     * 
     * @param aViewer the tree viewer to use
     */
    public CollapseAllAction(final TreeViewer aViewer) {
      super(Activator.getString("JJOutlinePage.Collapse_all_Action")); //$NON-NLS-1$
      fTreeViewer = aViewer;
      final ImageDescriptor desc = Activator.getImageDescriptor("jj_collapse.gif"); //$NON-NLS-1$
      setImageDescriptor(desc);
      setToolTipText(Activator.getString("JJOutlinePage.Collapse_all_Tooltip")); //$NON-NLS-1$
    }

    /**
     * Runs the action (collapse all).
     */
    @Override
    public void run() {
      fTreeViewer.collapseAll();
    }
  }

  /**
   * An Action to sort the tree alphabetically.
   */
  public class AlphabeticSortingAction extends org.eclipse.jface.action.Action {

    /** The viewer sorter to use */
    private final ViewerSorter     sorter   = new JJOutlineSorter();
    /** The structured viewer to use */
    private final StructuredViewer fViewer;
    /** The previous check status for the action */
    private boolean                oldState = false;

    /**
     * Constructor for LexicalSortingAction.
     * 
     * @param aViewer the structured viewer to use
     */
    public AlphabeticSortingAction(final StructuredViewer aViewer) {
      super(Activator.getString("JJOutlinePage.Sort_Action"), AS_RADIO_BUTTON); //$NON-NLS-1$
      fViewer = aViewer;
      final ImageDescriptor desc = Activator.getImageDescriptor("jj_alphab_sort.gif"); //$NON-NLS-1$
      setImageDescriptor(desc);
      setToolTipText(Activator.getString("JJOutlinePage.Sort_Tooltip")); //$NON-NLS-1$
    }

    /**
     * Runs the action.
     */
    @Override
    public void run() {
      if (oldState == true) {
        fViewer.setSorter(null);
        setChecked(false);
      }
      else {
        fViewer.setSorter(sorter);
      }
      oldState = isChecked();
    }
  }

  /**
   * Outline Sorter to perform the real sorting.
   */
  public static class JJOutlineSorter extends ViewerSorter {

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

    /**
     * Returns the element category.
     */
    @Override
    public int category(final Object aElement) {
      int category = 0;
      final JJNode node = (JJNode) aElement;
      if (node.getId() == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS) {
        category = OPTIONS;
      }
      else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN) {
        category = PARSER;
      }
      else if (node.getId() == JavaCCParserTreeConstants.JJTJAVACODE_PRODUCTION) {
        category = JAVACODE;
      }
      else if (node.getId() == JavaCCParserTreeConstants.JJTTOKEN_MANAGER_DECLS) {
        category = TOKEN_MGR_DECLS;
      }
      else if (node.getId() == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION) {
        category = REGEXPR_KIND;
      }
      else {
        category = RULES;
      }
      return category;
    }

    /**
     * Compares two objects through their categories.
     */
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

  /**
   * Creates a content outline page using the given editor (and a newly created content provider).
   * 
   * @param aEditor the given editor
   */
  public JJOutlinePage(final ITextEditor aEditor) {
    super();
    fEditor = (JJEditor) aEditor;
    fContentProvider = new JJOutlinePageContentProvider();
  }

  /**
   * Method declared on ContentOutlinePage.
   */
  @Override
  public void createControl(final Composite aParent) {
    super.createControl(aParent);
    final TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(fContentProvider);
    fLabelProvider = new JJLabelProvider();
    viewer.setLabelProvider(fLabelProvider);
    viewer.addSelectionChangedListener(this);

    // Adds button to viewer's toolbar
    final IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
    mgr.add(new CollapseAllAction(viewer));
    mgr.add(new AlphabeticSortingAction(viewer));

    // This updates the TreeViewer the first time
    setInput(fDocument);
  }

  /**
   * Method declared on ContentOutlinePage.
   */
  @Override
  public void selectionChanged(final SelectionChangedEvent aEvent) {
    super.selectionChanged(aEvent);
    final ISelection selection = aEvent.getSelection();
    if (selection.isEmpty()) {
      fEditor.resetHighlightRange();
    }
    else {
      final JJNode node = (JJNode) ((IStructuredSelection) selection).getFirstElement();
      selectionChanged(node);
    }
  }

  /**
   * Sets the given node as the new selection.
   * 
   * @param aNode the selected node
   */
  public void selectionChanged(final JJNode aNode) {
    fEditor.setSelection(aNode);
  }

  /**
   * Sets the input of the outline page.<br>
   * The TreeViewer calls {@link JJOutlinePageContentProvider#inputChanged(Viewer, Object, Object)} which
   * parses the given document to get the AST.
   * 
   * @param aDoc the given document
   */
  public void setInput(final IDocument aDoc) {
    fDocument = aDoc;
    if (aDoc == null) {
      return;
    }
    final TreeViewer viewer = getTreeViewer();
    if (viewer != null) {
      viewer.setInput(aDoc);
    }
    update();
  }

  /**
   * Updates the outline page.
   */
  public void update() {
    final TreeViewer viewer = getTreeViewer();
    if (viewer != null) {
      final Control control = viewer.getControl();
      if (control != null && !control.isDisposed()) {
        control.setRedraw(false);
        if (this.fDocument != null) {
          viewer.refresh(this.fDocument, true);
        }
        control.setRedraw(true);
      }
    }
  }

  /**
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose() {
    if (fLabelProvider != null) {
      fLabelProvider.dispose();
      fLabelProvider = null;
    }
    super.dispose();
  }

  /**
   * @return IContentProvider
   */
  public IContentProvider getContentProvider() {
    return fContentProvider;
  }
}
