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
 * Content outline page for the JJ editor.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJOutlinePage extends ContentOutlinePage {
  protected IDocument doc;
  protected JJEditor ed;
  private   JJLabelProvider labelProvider;
  private   JJContentProvider contentProvider;
  
  /**
   * Inner class to compact the tree
   */
  public class CollapseAllAction extends Action {
    private TreeViewer fViewer;

    public CollapseAllAction(TreeViewer aViewer) {
      super(Activator.getString("JJOutlinePage.Collapse_all_Action")); //$NON-NLS-1$
      fViewer = aViewer;
      Activator jpg = Activator.getDefault();
      ImageDescriptor desc = jpg.getImageDescriptor("jj_collapse.gif"); //$NON-NLS-1$
      setImageDescriptor(desc);
      setToolTipText(Activator.getString("JJOutlinePage.Collapse_all_Tooltip")); //$NON-NLS-1$
    }
    public void run() {
      fViewer.collapseAll();
    }
  }
  /**
   * An Action to sort the tree alphabeticaly
   */
  public static class AlphabeticSortingAction extends org.eclipse.jface.action.Action {
    private static final ViewerSorter SORTER = new JJOutlineSorter();
    private StructuredViewer fViewer;

      /**
       * Constructor for LexicalSortingAction.
       */
      public AlphabeticSortingAction(StructuredViewer aViewer) {
        super(Activator.getString("JJOutlinePage.Sort_Action"), AS_RADIO_BUTTON); //$NON-NLS-1$
        fViewer = aViewer;
        Activator jpg = Activator.getDefault();
        ImageDescriptor desc = jpg.getImageDescriptor("jj_alphab_sort.gif"); //$NON-NLS-1$
        setImageDescriptor(desc);
        setToolTipText(Activator.getString("JJOutlinePage.Sort_Tooltip")); //$NON-NLS-1$
      }
      public void run() {
          valueChanged(isChecked(), true);
      }
      private void valueChanged(boolean aValue, boolean aDoStore) {
          setChecked(aValue);
          fViewer.setSorter(aValue ? SORTER : null);
      }
  }
  /**
   * Outline Sorter to perform the real sorting
   */
  public static class JJOutlineSorter extends ViewerSorter {
    public static final int OPTIONS = 0;
    public static final int PARSER = 1;
    public static final int TOKENS = 2;
    public static final int RULES = 3;

    public int category(Object anElement) {
      int category = 0;
      JJNode node = (JJNode)anElement;
      if (node.getId() == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS)
        category = OPTIONS;
      else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN)
        category = PARSER;
      else if (node.getId() == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION)
        category = TOKENS;
      else
        category = RULES;
      return category;
    }

    public int compare(Viewer aViewer, Object anObject1, Object anObject2) {
      int result;
      int cat1 = category(anObject1);
      int cat2 = category(anObject2);
      if (cat1 < cat2)
        result = -1;
      else if (cat1 > cat2)
        result = 1;
      else {
        ILabelProvider lprov = (ILabelProvider) ((ContentViewer) aViewer).getLabelProvider();
        result = lprov.getText(anObject1).compareTo(lprov.getText(anObject2));
      }
      return result;
    }
  }


  /**
   * Creates a content outline page using the given provider and the given editor.
   */
  public JJOutlinePage(ITextEditor ed) {
    super();
    this.ed = (JJEditor) ed;
    contentProvider = new JJContentProvider();    
  }

  /* (non-Javadoc)
   * Method declared on ContentOutlinePage
   */
  public void createControl(Composite parent) {
    super.createControl(parent);
    TreeViewer viewer = getTreeViewer();
    viewer.setContentProvider(contentProvider);
    labelProvider = new JJLabelProvider();
    viewer.setLabelProvider(labelProvider);
    viewer.addSelectionChangedListener(this);
    
    // Adds button to viewer's toolbar
    IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
    mgr.add(new CollapseAllAction(viewer));
    mgr.add(new AlphabeticSortingAction(viewer));
    
    // This updates the TreeViewer the first time
    setInput(doc);
  }

  /* (non-Javadoc)
   * Method declared on ContentOutlinePage
   */
  public void selectionChanged(SelectionChangedEvent event) {
    super.selectionChanged(event);
    ISelection selection = event.getSelection();
    if (selection.isEmpty())
      this.ed.resetHighlightRange();
    else {
      JJNode node =
        (JJNode) ((IStructuredSelection) selection).getFirstElement();
      selectionChanged(node);
    }
  }
  /**
   * @param node
   */
  public void selectionChanged(JJNode node) {
    this.ed.setSelection(node);
  }
  
  /**
   * Sets the input of the outline page
   * The TreeViewer calls JJContentProvider.inputChanged(doc)
   * which parse the Document to get the AST
   */
  public void setInput(IDocument doc) {
    this.doc = doc;
    if (doc == null)
      return;
    TreeViewer viewer = getTreeViewer();
    if (viewer != null)
      viewer.setInput(doc);
    update();
  }

  /**
   * Updates the outline page.
   */
  public void update() {
    TreeViewer viewer = getTreeViewer();
    if (viewer != null) {
      Control control = viewer.getControl();
      if (control != null && !control.isDisposed()) {
        control.setRedraw(false);
        if (this.doc != null) {
          viewer.refresh(this.doc, true);
        }
        control.setRedraw(true);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.IPage#dispose()
   */
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
