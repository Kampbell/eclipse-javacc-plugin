package sf.eclipse.javacc.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
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
 * Editor designed for JavaCC files. Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJEditor extends TextEditor implements IJJConstants, INavigationLocationProvider {

  // MMa 11/09 : formatting and javadoc revision ; added constructor for subclass

  /** the JJ outline page */
  protected JJOutlinePage                               outlinePage;
  /** the JJ reconciling strategy */
  protected JJReconcilingStrategy                       reconcilingStrategy;
  /** the JJ source viewer configuration */
  protected JJSourceViewerConfiguration                 jjSourceViewerConfiguration;
  /** the projection support */
  private ProjectionSupport                             projectionSupport;
  /** the annotations */
  private final HashMap<ProjectionAnnotation, Position> oldAnnotations = new HashMap<ProjectionAnnotation, Position>();
  /** the annotation model */
  private ProjectionAnnotationModel                     annotationModel;

  /** the editor's pair Parent Matcher */
  private final ParentMatcher                           fParentMatcher = new ParentMatcher();
  /** the pair matching char color */
  private Color                                         colorMatchingChar;

  /** the editor's peer character painter */
  private MatchingCharacterPainter                      fMatchingCharacterPainter;
  /** the JJ elements */
  private JJElements                                    jjElements;

  /**
   * Standard constructor.
   */
  public JJEditor() {
    super();
    // Offer the possibility to add contributions to context menu via plugin.xml
    setEditorContextMenuId("sf.eclipse.javacc.editors.JJEditor"); //$NON-NLS-1$
  }

  /**
   * Customized constructor for subclass.
   * 
   * @param ctx the context menu id (from plugin.xml)
   */
  public JJEditor(final String ctx) {
    super();
    // Offer the possibility to add contributions to context menu via plugin.xml
    setEditorContextMenuId(ctx);
  }

  /**
   * Initializes this editor. Method declared on AbstractTextEditor
   */
  @Override
  protected void initializeEditor() {
    super.initializeEditor();

    // Generic Document provider
    setDocumentProvider(new FileDocumentProvider());
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    jjSourceViewerConfiguration = new JJSourceViewerConfiguration(this);
    setSourceViewerConfiguration(jjSourceViewerConfiguration);
    // Used to synchronize Outline and Editor
    reconcilingStrategy = new JJReconcilingStrategy(this);
    // Used to retrieve JJElements (methods, tokens, class) updated at each document parsing
    jjElements = new JJElements();

    // Actions are declared in plugin.xml
  }

  /**
   * Disposes the colors.
   */
  @Override
  public void dispose() {
    if (colorMatchingChar != null) {
      colorMatchingChar.dispose();
      colorMatchingChar = null;
    }
    if (jjSourceViewerConfiguration != null) {
      jjSourceViewerConfiguration.dispose();
      jjSourceViewerConfiguration = null;
    }
    super.dispose();
  }

  /**
   * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
   */
  @Override
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] {
      "sf.eclipse.javacc.JJEditorScope" }); //$NON-NLS-1$
  }

  /**
   * Subclassed in order to add MatchingCharacterPainter to install ProjectionSupport
   */
  @Override
  public void createPartControl(final Composite parent) {
    super.createPartControl(parent);
    // Parent matcher
    showMatchingCharacters();
    // Projection Support
    final ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
    projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
    projectionSupport.install();
    // Turn projection mode on
    viewer.doOperation(ProjectionViewer.TOGGLE);
    annotationModel = viewer.getProjectionAnnotationModel();
  }

  /**
   * Subclassed to return a ProjectionViewer instead of a SourceViewer.
   */
  @Override
  protected ISourceViewer createSourceViewer(final Composite parent, final IVerticalRuler ruler,
                                             final int styles) {
    final ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(),
                                                      isOverviewRulerVisible(), styles);
    // ensure decoration support has been created and configured.
    getSourceViewerDecorationSupport(viewer);
    fAnnotationAccess = getAnnotationAccess();
    fOverviewRuler = createOverviewRuler(getSharedColors());
    return viewer;
  }

  /**
   * @return the source viewer
   */
  public ISourceViewer getSourceViewer2() {
    return getSourceViewer();
  }

  /**
   * Tells the editor which regions are collapsable.
   * 
   * @param positions the positions
   */
  public void updateFoldingStructure(final ArrayList<Position> positions) {
    final HashMap<ProjectionAnnotation, Position> additions = new HashMap<ProjectionAnnotation, Position>();
    for (int i = 0; i < positions.size(); i++) {
      ProjectionAnnotation annotation = null;
      final Position pos = positions.get(i);
      boolean collapsed = false;
      // Search existing annotations, to keep state (collapsed or not)
      final Iterator<Entry<ProjectionAnnotation, Position>> e = oldAnnotations.entrySet().iterator();
      while (e.hasNext()) {
        final Entry<ProjectionAnnotation, Position> mapEntry = e.next();
        final ProjectionAnnotation key = mapEntry.getKey();
        final Position value = mapEntry.getValue();
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
    final Annotation[] deletions = (oldAnnotations.keySet().toArray(new Annotation[] {}));
    annotationModel.modifyAnnotations(deletions, additions, null);
    // Now we can add additions to oldAnnotations
    oldAnnotations.clear();
    oldAnnotations.putAll(additions);
  }

  /**
   * Adds a Painter to show matching characters.
   */
  private final void showMatchingCharacters() {
    if (fMatchingCharacterPainter == null) {
      if (getSourceViewer() instanceof ISourceViewerExtension2) {
        fMatchingCharacterPainter = new MatchingCharacterPainter(getSourceViewer(), fParentMatcher);
        final Display display = Display.getCurrent();
        final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        colorMatchingChar = new Color(display, PreferenceConverter.getColor(store,
                                                                            JJPreferences.P_MATCHING_CHAR));
        fMatchingCharacterPainter.setColor(colorMatchingChar);
        final ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(fMatchingCharacterPainter);
      }
    }
  }

  /**
   * Returns ContentOutlinePage Method declared on IAdaptable.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(final Class key) {
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
   * @return the reconciling strategy
   */
  public JJReconcilingStrategy getReconcilingStrategy() {
    return reconcilingStrategy;
  }

  /**
   * Takes the current socument and calls setInput() on JJOutlinePage which leads to
   * JJOutlinePageContentProvider.setInput() which parses the document.
   */
  protected void updateOutlinePage() {
    if (outlinePage == null) {
      outlinePage = (JJOutlinePage) getAdapter(IContentOutlinePage.class);
    }
    outlinePage.setInput(getDocument());
    // Get root node to build JJElement HashMap
    final JJOutlinePageContentProvider contentProvider = (JJOutlinePageContentProvider) outlinePage
                                                                                                   .getContentProvider();
    JJNode rootNode = contentProvider.getAST();
    // If the outline is not up, then use the ContentProvider directly
    if (outlinePage.getControl() == null) {
      contentProvider.inputChanged(null, null, getDocument());
      rootNode = contentProvider.getAST();
    }
    // If parsing failed we don't touch JJElements used for navigation and completion 
    if (rootNode.getFirstToken().next == null) {
      return;
    }

    // Clear and Fill the JJElements HashMap for this Editor
    jjElements.clear();
    rootNode.setJJElementsToUpdate(jjElements);
    rootNode.buildHashMap();
  }

  /**
   * @return jjElements
   */
  public JJElements getJJElements() {
    return jjElements;
  }

  /**
   * @return document
   */
  public IDocument getDocument() {
    final IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    return doc;
  }

  /**
   * Set the Selection given a Node of the AST
   * 
   * @param node the JJNode to set the selection on
   */
  public void setSelection(final JJNode node) {
    try {
      final IDocument doc = getDocument();
      if (doc != null) {
        final int start = doc.getLineOffset(node.getBeginLine() - 1);
        int end = doc.getLineOffset(node.getEndLine());
        if (start > end) {
          end = start;
        }
        final int length = end - start;
        selectAndReveal(start, length);
        resetHighlightRange();
        setHighlightRange(start, length, true);
        markInNavigationHistory();
      }
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
      resetHighlightRange();
    } catch (final BadLocationException e) {
      e.printStackTrace();
      resetHighlightRange();
    }
  }

  /**
   * Subclassed to add edit location in History [ 1891111 ] Alt + left arrow should jump back to correct
   * position
   */
  @Override
  protected void updateContentDependentActions() {
    super.updateContentDependentActions();
    markInNavigationHistory();
  }

  /**
   * Highlights a range and selects a text.
   * 
   * @param range the range to highlight
   * @param sel the text to select
   */
  public void setSelection(final IRegion range, final ITextSelection sel) {
    if (range != null) {
      setHighlightRange(range.getOffset(), range.getLength(), true);
    }
    else {
      resetHighlightRange();
    }
    if (sel != null) {
      selectAndReveal(sel.getOffset(), sel.getLength());
    }
  }

  /**
   * Updates colors. Does nothing.
   */
  public void updateColors() {
    //    Display display = Display.getCurrent();
    //    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    //    Color color = new Color(display, PreferenceConverter.getColor(store, JJPreferences.P_JJKEYWORD));
    //    getSourceViewer().getTextWidget().setBackground(color);
  }
}
