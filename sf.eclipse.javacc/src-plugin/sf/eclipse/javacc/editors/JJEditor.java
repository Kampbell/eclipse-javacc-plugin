package sf.eclipse.javacc.editors;

import java.util.*;
import java.util.Map.Entry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.options.JJPreferences;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Editor designed for JavaCC files Referenced by plugin.xml <extension
 * point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003-2006 CeCILL Licence
 *         http://www.cecill.info/index.en.html
 */
public class JJEditor extends TextEditor implements IJJConstants, INavigationLocationProvider {
  protected JJOutlinePage outlinePage;
  protected JJReconcilingStrategy reconcilingStrategy;
  protected JJSourceViewerConfiguration jjSourceViewerConfiguration;
  private ProjectionSupport projectionSupport;
  private HashMap<ProjectionAnnotation, Position> oldAnnotations = new HashMap<ProjectionAnnotation, Position>();;
  private ProjectionAnnotationModel annotationModel;
  
  /** The editor's peer Parent Matcher */
  private ParentMatcher fParentMatcher = new ParentMatcher();
  private Color colorMatchingChar;
  
  /** The editor's peer character painter */
  private MatchingCharacterPainter fMatchingCharacterPainter;

  /**
   * Constructor
   */
  public JJEditor() {
    super();
    // Offer the possibility to add contributions to context menu via
    // plugin.xml
    setEditorContextMenuId("sf.eclipse.javacc.editors.JJEditor"); //$NON-NLS-1$
  }
  
  /**
   * Initializes this editor. Method declared on AbstractTextEditor
   */
  protected void initializeEditor() {
    super.initializeEditor();
    
    // Generic Document provider
    setDocumentProvider(new FileDocumentProvider());
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    jjSourceViewerConfiguration = new JJSourceViewerConfiguration(this);
    setSourceViewerConfiguration(jjSourceViewerConfiguration);
    // Used to synchronize Outline and Editor
    reconcilingStrategy = new JJReconcilingStrategy(this);
    
    // Actions are declared in plugin.xml
  }
  
  /** 
   * Dispose of colors
   */
  public void dispose() {
    if (colorMatchingChar != null) {
      colorMatchingChar.dispose();
      colorMatchingChar= null;
    }    
    if (jjSourceViewerConfiguration != null) {
      jjSourceViewerConfiguration.dispose();
      jjSourceViewerConfiguration= null;
    }
    super.dispose();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
   */
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] { "sf.eclipse.javacc.JJEditorScope" }); //$NON-NLS-1$
  }
  
  /**
   * Subclassed in order 
   * to add MatchingCharacterPainter
   * to install ProjectionSupport
   */
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    // Parent matcher
    showMatchingCharacters();
    // Projection Support
    ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
    projectionSupport.install();
    // Turn projection mode on
    viewer.doOperation(ProjectionViewer.TOGGLE);
    annotationModel = viewer.getProjectionAnnotationModel();
  }
  /**
   * Subclassed to return a ProjectionViewer instead of a SourceViewer.
   */
  protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
    ISourceViewer viewer = new ProjectionViewer(parent, ruler,  getOverviewRuler(), isOverviewRulerVisible(), styles);
    // ensure decoration support has been created and configured.
    getSourceViewerDecorationSupport(viewer);
    fAnnotationAccess= getAnnotationAccess();
    fOverviewRuler= createOverviewRuler(getSharedColors());
    return viewer;
  }
  public ISourceViewer getSourceViewer2() {
    return getSourceViewer();
  }
  /**
   * Tell the editor which regions are collapsible
   * @param positions
   */
  public void updateFoldingStructure(ArrayList<Position> positions) {
    HashMap<ProjectionAnnotation, Position> additions = new HashMap<ProjectionAnnotation, Position>();
    for (int i = 0; i < positions.size(); i++) {
      ProjectionAnnotation annotation = null;
      Position pos = positions.get(i);
      boolean collapsed = false;
      // Search existing annotations, to keep state (collapsed or not)
      Iterator<Entry<ProjectionAnnotation, Position>> e = oldAnnotations.entrySet().iterator();
      while (e.hasNext()) {
        Entry<ProjectionAnnotation, Position> mapEntry = e.next();
        ProjectionAnnotation key = (ProjectionAnnotation) mapEntry.getKey();
        Position value = (Position) mapEntry.getValue();
        if (value.equals(pos)) {
          collapsed = key.isCollapsed();
          break;
        }
      }
      // Create new annotation eventually with old state
      annotation = new ProjectionAnnotation(collapsed);
      additions.put(annotation, pos);
    }
    // Clumsy only additions can be passed as a Map, we need to build an array
    Annotation[] deletions = (Annotation[]) (oldAnnotations.keySet().toArray(new Annotation[] {}));
    annotationModel.modifyAnnotations(deletions, additions, null);
    // Now we can add additions to oldAnnotations
    oldAnnotations.clear();
    oldAnnotations.putAll(additions);
  }
  
  /**
   * Add a Painter to show matching characters.
   */
  private final void showMatchingCharacters() {
    if (fMatchingCharacterPainter == null) {
      if (getSourceViewer() instanceof ISourceViewerExtension2) {
        fMatchingCharacterPainter = new MatchingCharacterPainter(
            getSourceViewer(), fParentMatcher);
        Display display = Display.getCurrent();
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        colorMatchingChar = new Color(display, PreferenceConverter.getColor(
            store, JJPreferences.P_MATCHING_CHAR));
        fMatchingCharacterPainter.setColor(colorMatchingChar);
        ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(fMatchingCharacterPainter);
      }
    }
  }

  /**
   * Returns ContentOutlinePage Method declared on IAdaptable
   */
  @SuppressWarnings("unchecked") //$NON-NLS-1$
  public Object getAdapter(Class key) {
    if (key.equals(IContentOutlinePage.class)) {
      if (outlinePage == null) {
        outlinePage = new JJOutlinePage(this);
        updateOutlinePage();
      }
      return outlinePage;
    }
    return super.getAdapter(key);
  }
  
  /**
   * Used by JJConfiguration.getReconciler()
   * 
   * @return JJReconcilingStrategy
   */
  public JJReconcilingStrategy getReconcilingStrategy() {
    return reconcilingStrategy;
  }
  
  /**
   * Called by JJReconcilingStrategy Takes the current Document and
   * setInput() on JJOutlinePage which leads to
   * JJContentProvider.setInput() which parse the document.
   */
  protected void updateOutlinePage() {
    if (outlinePage == null)
      outlinePage = (JJOutlinePage) getAdapter(IContentOutlinePage.class);
    outlinePage.setInput(getDocument());
    // get root node to build JJElement Hashmap
    JJContentProvider contentProvider = (JJContentProvider) outlinePage.getContentProvider();
    JJNode node = contentProvider.getAST();
    // If the outline is not up, then use the ContentProvider directly
    if (outlinePage.getControl() == null) {
      contentProvider.inputChanged(null, null, getDocument());
      node = contentProvider.getAST();
    }
    
    // Clear and Fill the JJElements HashMap
    JJElements.clear();
    node.buildHashMap();
  }
  
  /**
   * @return document
   */
  public IDocument getDocument() {
    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    return doc;
  }

  /**
   * Set the Selection given a Node of the AST
   * @param node
   */
  public void setSelection(JJNode node) {
    try {
      IDocument doc = getDocument();
      if (doc != null) {
        int start = doc.getLineOffset(node.getBeginLine() - 1);
        int end = doc.getLineOffset(node.getEndLine());
        if (start > end)
          end = start;
        int length = end - start;
        selectAndReveal(start, length);
        resetHighlightRange();
        setHighlightRange(start, length, true);
        markInNavigationHistory();
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      resetHighlightRange();
    } catch (BadLocationException e) {
      e.printStackTrace();
      resetHighlightRange();
    }
  }
  
  /**
   * Subclassed to add edit location in History
   * [ 1891111 ] Alt + left arrow should jump back to correct position 
   */
  protected void updateContentDependentActions() {
    super.updateContentDependentActions();
    markInNavigationHistory();
  }
  
  /**
   * Set the selection
   * 
   * @param IRegion
   *                (the highligth), ITextSelection (the selection)
   */
  public void setSelection(IRegion range, ITextSelection sel) {
    if (range != null)
      setHighlightRange(range.getOffset(), range.getLength(), true);
    else
      resetHighlightRange();
    if (sel != null)
      selectAndReveal(sel.getOffset(), sel.getLength());
    }

  public void updateColors() {
//    Display display = Display.getCurrent();
//    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//    Color color = new Color(display, PreferenceConverter.getColor(store, JJPreferences.P_JJKEYWORD));
//    getSourceViewer().getTextWidget().setBackground(color);
  }
}
