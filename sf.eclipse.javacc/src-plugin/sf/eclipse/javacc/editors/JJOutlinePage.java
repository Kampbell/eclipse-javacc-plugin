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
 * @author Remi Koutcherawy 2003-2006 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJOutlinePage extends ContentOutlinePage {

  /*
   * MMa 11/09 : javadoc and formatting revision ; changed sorting categories ;
   * added javacode and token_mgr_decls entries ; moved lexical states at the kind's right
   */
  /** the document to outline */
  protected IDocument                        doc;
  /** the editor to outline */
  protected JJEditor                         ed;
  /** the label provider to use */
  private JJLabelProvider                    labelProvider;
  /** the content provider to use */
  private final JJOutlinePageContentProvider contentProvider;

  /**
   * Inner class to compact the tree
   */
  public class CollapseAllAction extends Action {

    /** the tree viewer to use */
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
   * An Action to sort the tree alphabeticaly
   */
  public class AlphabeticSortingAction extends org.eclipse.jface.action.Action {

    /** the viewer sorter to use */
    private final ViewerSorter     sorter   = new JJOutlineSorter();
    /** the structured viewer to use */
    private final StructuredViewer fViewer;
    /** previous check status for the action */
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
   * Outline Sorter to perform the real sorting
   */
  public static class JJOutlineSorter extends ViewerSorter {

    // sorting is most of the time useful for bnf productions, not token definitions
    /** the rules outline category */
    public static final int RULES           = -1;
    /** the options outline category */
    public static final int OPTIONS         = 0;
    /** the parser outline category */
    public static final int PARSER          = 1;
    /** the javacode outline category */
    public static final int JAVACODE        = 2;
    /** the token_mgr_decls outline category */
    public static final int TOKEN_MGR_DECLS = 3;
    /** the regexpr_kind outline category */
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
   * @param aEd the given editor
   */
  public JJOutlinePage(final ITextEditor aEd) {
    super();
    ed = (JJEditor) aEd;
    contentProvider = new JJOutlinePageContentProvider();
  }

  /**
   * Method declared on ContentOutlinePage
   */
  @Override
  public void createControl(final Composite aParent) {
    super.createControl(aParent);
    final TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(contentProvider);
    labelProvider = new JJLabelProvider();
    viewer.setLabelProvider(labelProvider);
    viewer.addSelectionChangedListener(this);

    // Adds button to viewer's toolbar
    final IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
    mgr.add(new CollapseAllAction(viewer));
    mgr.add(new AlphabeticSortingAction(viewer));

    // This updates the TreeViewer the first time
    setInput(doc);
  }

  /**
   * Method declared on ContentOutlinePage
   */
  @Override
  public void selectionChanged(final SelectionChangedEvent aEvent) {
    super.selectionChanged(aEvent);
    final ISelection selection = aEvent.getSelection();
    if (selection.isEmpty()) {
      ed.resetHighlightRange();
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
    ed.setSelection(aNode);
  }

  /**
   * Sets the input of the outline page.<br>
   * The TreeViewer calls {@link JJOutlinePageContentProvider#inputChanged(Viewer, Object, Object)} which
   * parses the given document to get the AST.
   * 
   * @param aDoc the given document
   */
  public void setInput(final IDocument aDoc) {
    doc = aDoc;
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
        if (this.doc != null) {
          viewer.refresh(this.doc, true);
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
    if (labelProvider != null) {
      labelProvider.dispose();
      labelProvider = null;
    }
    super.dispose();
  }

  /**
   * @return IContentProvider
   */
  public IContentProvider getContentProvider() {
    return contentProvider;
  }
}
