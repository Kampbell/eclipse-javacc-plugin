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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.options.JJPreferences;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Editor designed for JavaCC files.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJEditor extends TextEditor implements IJJConstants, INavigationLocationProvider {

  // MMa 11/2009 : formatting and javadoc revision ; added constructor for subclass
  // MMa 12/2009 : added spell checking ; some renaming
  // MMa 02/2010 : formatting and javadoc revision

  /** The JJ outline page */
  protected JJOutlinePage                               fOutlinePage;
  /** The JJ source viewer configuration */
  protected JJSourceViewerConfiguration                 fJJSourceViewerConfiguration;
  /** The projection support */
  private ProjectionSupport                             fProjectionSupport;
  /** The projection annotations */
  private final HashMap<ProjectionAnnotation, Position> fOldAnnotations = new HashMap<ProjectionAnnotation, Position>();
  /** The projection annotation model */
  private ProjectionAnnotationModel                     fAnnotationModel;

  /** The editor's pair Parent Matcher */
  private final JJCharacterPairMatcher                  fParentMatcher  = new JJCharacterPairMatcher();
  /** The pair matching char color */
  private Color                                         fColorMatchingChar;

  /** The editor's peer character painter */
  private MatchingCharacterPainter                      fMatchingCharacterPainter;
  /** The JJ elements */
  private JJElements                                    fJJElements;

  /**
   * Standard constructor.
   */
  public JJEditor() {
    super();
    // offer the possibility to add contributions to context menu via plugin.xml
    setEditorContextMenuId("sf.eclipse.javacc.editors.JJEditor"); //$NON-NLS-1$
  }

  /**
   * Customized constructor for subclass.
   * 
   * @param aCtx the context menu id (from plugin.xml)
   */
  public JJEditor(final String aCtx) {
    super();
    // for sub-classes like JTBEditor
    setEditorContextMenuId(aCtx);
  }

  /**
   * Initializes this editor.
   */
  @Override
  protected void initializeEditor() {
    super.initializeEditor();

    // JJ DocumentProvider
    setDocumentProvider(new JJDocumentProvider());
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    fJJSourceViewerConfiguration = new JJSourceViewerConfiguration(this);
    setSourceViewerConfiguration(fJJSourceViewerConfiguration);
    // used to retrieve JJElements (methods, tokens, class) updated at each document parsing
    fJJElements = new JJElements();

    // actions are declared in plugin.xml
  }

  /**
   * Disposes the colors and the code scanner.
   */
  @Override
  public void dispose() {
    if (fColorMatchingChar != null) {
      fColorMatchingChar.dispose();
      fColorMatchingChar = null;
    }
    if (fJJSourceViewerConfiguration != null) {
      fJJSourceViewerConfiguration.dispose();
      fJJSourceViewerConfiguration = null;
    }
    super.dispose();
  }

  /**
   * @see TextEditor#initializeKeyBindingScopes()
   */
  @Override
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] {
      "sf.eclipse.javacc.JJEditorScope" }); //$NON-NLS-1$
  }

  /**
   * Overridden in order to add MatchingCharacterPainter to install ProjectionSupport
   */
  @Override
  public void createPartControl(final Composite aParent) {
    super.createPartControl(aParent);
    // ParentMatcher
    showMatchingCharacters();
    // Projection Support
    final ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
    fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
    fProjectionSupport.install();
    // turn projection mode on
    viewer.doOperation(ProjectionViewer.TOGGLE);
    fAnnotationModel = viewer.getProjectionAnnotationModel();
  }

  /**
   * Overridden to return a ProjectionViewer instead of a SourceViewer.
   */
  @Override
  protected ISourceViewer createSourceViewer(final Composite aParent, final IVerticalRuler aRuler,
                                             final int aStyles) {
    fAnnotationAccess = createAnnotationAccess();
    fOverviewRuler = createOverviewRuler(getSharedColors());
    final ISourceViewer viewer = new ProjectionViewer(aParent, aRuler, fOverviewRuler,
                                                      isOverviewRulerVisible(), aStyles);
    // ensure decoration support has been created and configured
    getSourceViewerDecorationSupport(viewer);
    return viewer;
  }

  /**
   * Relay as AbstractTextEditor.getSourceViewer() is protected.
   * 
   * @return the source viewer
   */
  public ISourceViewer getSourceViewerPlease() {
    return getSourceViewer();
  }

  /**
   * Tells the editor which regions are collapsable.
   * 
   * @param aPositions the folding positions
   */
  public void updateFoldingStructure(final ArrayList<Position> aPositions) {
    final HashMap<ProjectionAnnotation, Position> additions = new HashMap<ProjectionAnnotation, Position>();
    for (int i = 0; i < aPositions.size(); i++) {
      ProjectionAnnotation annotation = null;
      final Position pos = aPositions.get(i);
      boolean collapsed = false;
      // search existing annotations, to keep state (collapsed or not)
      final Iterator<Entry<ProjectionAnnotation, Position>> e = fOldAnnotations.entrySet().iterator();
      while (e.hasNext()) {
        final Entry<ProjectionAnnotation, Position> mapEntry = e.next();
        final ProjectionAnnotation key = mapEntry.getKey();
        final Position value = mapEntry.getValue();
        if (value.equals(pos)) {
          collapsed = key.isCollapsed();
          break;
        }
      }
      // create new annotation eventually with old state
      annotation = new ProjectionAnnotation(collapsed);
      additions.put(annotation, pos);
    }
    final Annotation[] deletions = (fOldAnnotations.keySet().toArray(new Annotation[fOldAnnotations.size()]));
    fAnnotationModel.modifyAnnotations(deletions, additions, null);
    // now we can add additions to oldAnnotations
    fOldAnnotations.clear();
    fOldAnnotations.putAll(additions);
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
        fColorMatchingChar = new Color(display, PreferenceConverter.getColor(store,
                                                                             JJPreferences.P_MATCHING_CHAR));
        fMatchingCharacterPainter.setColor(fColorMatchingChar);
        final ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(fMatchingCharacterPainter);
      }
    }
  }

  /**
   * Returns the JJOutlinePage declared on IAdaptable.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(final Class aRequiredClass) {
    if (IContentOutlinePage.class.equals(aRequiredClass)) {
      if (fOutlinePage == null) {
        fOutlinePage = new JJOutlinePage(this);
        updateOutlinePage();
      }
      return fOutlinePage;
    }
    return super.getAdapter(aRequiredClass);
  }

  /**
   * Takes the current document and calls setInput() on JJOutlinePage which leads to
   * JJOutlinePageContentProvider.setInput() which parses the document.
   */
  protected void updateOutlinePage() {
    if (fOutlinePage == null) {
      fOutlinePage = (JJOutlinePage) getAdapter(IContentOutlinePage.class);
    }
    fOutlinePage.setInput(getDocument());
    // get root node to build JJElement HashMap
    final JJOutlinePageContentProvider contentProvider = (JJOutlinePageContentProvider) fOutlinePage
                                                                                                    .getContentProvider();
    // If the outline is not up, then use the ContentProvider directly
    if (fOutlinePage.getControl() == null) {
      contentProvider.inputChanged(null, null, getDocument());
    }
    final JJNode rootNode = contentProvider.getAST();
    // if parsing failed we don't touch JJElements used for navigation and completion 
    if (rootNode == null || rootNode.getFirstToken().next == null) {
      return;
    }

    // clear and Fill the JJElements HashMap for this Editor
    fJJElements.clear();
    rootNode.setJJElementsToUpdate(fJJElements);
    rootNode.buildHashMap();
  }

  /**
   * @return jjElements
   */
  public JJElements getJJElements() {
    return fJJElements;
  }

  /**
   * @return document
   */
  public IDocument getDocument() {
    final IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    return doc;
  }

  /**
   * Sets the Selection given a Node of the AST.
   * 
   * @param aNode the JJNode to set the selection on
   */
  public void setSelection(final JJNode aNode) {
    try {
      final IDocument doc = getDocument();
      if (doc != null) {
        final int start = doc.getLineOffset(aNode.getBeginLine() - 1);
        int end = doc.getLineOffset(aNode.getEndLine());
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
   * Overridden to add edit location in History. Alt + left arrow should jump back to correct position.
   */
  @Override
  protected void updateContentDependentActions() {
    super.updateContentDependentActions();
    markInNavigationHistory();
  }

  /**
   * Highlights a range and selects a text.
   * 
   * @param aRange the range to highlight
   * @param aSelection the text to select
   */
  public void setSelection(final IRegion aRange, final ITextSelection aSelection) {
    if (aRange != null) {
      setHighlightRange(aRange.getOffset(), aRange.getLength(), true);
    }
    else {
      resetHighlightRange();
    }
    if (aSelection != null) {
      selectAndReveal(aSelection.getOffset(), aSelection.getLength());
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
