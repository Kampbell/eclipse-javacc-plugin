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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.options.PreferencesInitializer;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Editor designed for JavaCC files.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJEditor extends TextEditor implements IJJConstants, INavigationLocationProvider {

  // MMa 11/2009 : formatting and javadoc revision ; added constructor for subclass
  // MMa 12/2009 : added spell checking ; some renaming
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : refactoring / renamings
  // MMa 08/2011 : impacts of Call Hierarchy view enhancements

  /** The JJ outline page */
  protected JJOutlinePage                               jJJOutlinePage;
  /** The JJ reconciling strategy */
  protected JJReconcilingStrategy                       jJJReconcilingStrategy;

  /** The JJ source viewer configuration */
  protected JJSourceViewerConfiguration                 jJJSourceViewerConfiguration;
  /** The projection support */
  private ProjectionSupport                             jProjectionSupport;
  /** The (previous / current) projection annotations */
  private final HashMap<ProjectionAnnotation, Position> jProjectionAnnotations = new HashMap<ProjectionAnnotation, Position>();
  /** The projection annotation model */
  private ProjectionAnnotationModel                     jAnnotationModel;

  /** The editor's pair Parent Matcher */
  private final JJCharacterPairMatcher                  jJJParentMatcher       = new JJCharacterPairMatcher();
  /** The pair matching char color */
  private Color                                         jColorMatchingChar;

  /** The editor's peer character painter */
  private MatchingCharacterPainter                      jMatchingCharacterPainter;
  /** The JJ elements */
  private JJElements                                    jJJElements;

  /**
   * Standard constructor.
   */
  public JJEditor() {
    super();
    // offer the possibility to add contributions to context menu via plugin.xml
    setEditorContextMenuId(JJEDITOR_ID);
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
    //    setDocumentProvider(new FileDocumentProvider());
    setDocumentProvider(new JJDocumentProvider());
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    jJJSourceViewerConfiguration = new JJSourceViewerConfiguration(this);
    setSourceViewerConfiguration(jJJSourceViewerConfiguration);
    // used to synchronize Outline and Editor
    jJJReconcilingStrategy = new JJReconcilingStrategy(null, this);
    // used to retrieve JJElements (methods, tokens, class) updated at each document parsing
    jJJElements = new JJElements();

    // actions are declared in plugin.xml
  }

  /**
   * Disposes the colors and the code scanner.
   */
  @Override
  public void dispose() {
    if (jColorMatchingChar != null) {
      jColorMatchingChar.dispose();
      jColorMatchingChar = null;
    }
    if (jJJSourceViewerConfiguration != null) {
      jJJSourceViewerConfiguration.dispose();
      jJJSourceViewerConfiguration = null;
    }
    super.dispose();
  }

  /**
   * @see TextEditor#initializeKeyBindingScopes()
   */
  @Override
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] {
      JJEDITOR_SCOPE_ID });
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
    jProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
    jProjectionSupport.install();
    // turn projection mode on
    viewer.doOperation(ProjectionViewer.TOGGLE);
    jAnnotationModel = viewer.getProjectionAnnotationModel();
  }

  /**
   * Overridden to return a ProjectionViewer instead of a SourceViewer.
   */
  @Override
  protected ISourceViewer createSourceViewer(final Composite aParent, final IVerticalRuler aRuler,
                                             final int aStyles) {
    final ISourceViewer viewer = new ProjectionViewer(aParent, aRuler, getOverviewRuler(),
                                                      isOverviewRulerVisible(), aStyles);
    // ensure decoration support has been created and configured
    getSourceViewerDecorationSupport(viewer);
    fAnnotationAccess = getAnnotationAccess();
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
   * Tells the editor which regions are expanded or collapsed.
   * 
   * @param aPositions the folding positions
   */
  public void updateFoldingStructure(final ArrayList<Position> aPositions) {
    final HashMap<ProjectionAnnotation, Position> additions = new HashMap<ProjectionAnnotation, Position>(
                                                                                                          aPositions.size());
    for (final Position pos : aPositions) {
      boolean collapsed = false;
      // search existing annotations, to keep state (collapsed or not)
      final Iterator<Entry<ProjectionAnnotation, Position>> e = jProjectionAnnotations.entrySet().iterator();
      while (e.hasNext()) {
        final Entry<ProjectionAnnotation, Position> mapEntry = e.next();
        final Position value = mapEntry.getValue();
        if (value.equals(pos)) {
          collapsed = mapEntry.getKey().isCollapsed();
          break;
        }
      }
      additions.put(new ProjectionAnnotation(collapsed), pos);
    }
    final Annotation[] deletions = (jProjectionAnnotations.keySet().toArray(new Annotation[jProjectionAnnotations.size()]));
    jAnnotationModel.modifyAnnotations(deletions, additions, null);
    // now we can add additions to current annotations
    jProjectionAnnotations.clear();
    jProjectionAnnotations.putAll(additions);
  }

  /**
   * Adds a Painter to show matching characters.
   */
  private final void showMatchingCharacters() {
    if (jMatchingCharacterPainter == null) {
      if (getSourceViewer() instanceof ISourceViewerExtension2) {
        jMatchingCharacterPainter = new MatchingCharacterPainter(getSourceViewer(), jJJParentMatcher);
        final Display display = Display.getCurrent();
        final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        jColorMatchingChar = new Color(display,
                                       PreferenceConverter.getColor(store,
                                                                    PreferencesInitializer.P_MATCHING_CHAR));
        jMatchingCharacterPainter.setColor(jColorMatchingChar);
        final ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(jMatchingCharacterPainter);
      }
    }
  }

  /**
   * Returns the JJOutlinePage declared on IAdaptable.
   */
  @Override
  public Object getAdapter(@SuppressWarnings("rawtypes") final Class aRequiredClass) {
    if (aRequiredClass.equals(IContentOutlinePage.class)) {
      if (jJJOutlinePage == null) {
        jJJOutlinePage = new JJOutlinePage(this);
        updateOutlinePage();
      }
      return jJJOutlinePage;
    }
    return super.getAdapter(aRequiredClass);
  }

  /**
   * @return the reconciling strategy
   */
  public JJReconcilingStrategy getReconcilingStrategy() {
    return jJJReconcilingStrategy;
  }

  /**
   * Takes the current document and calls setInput() on JJOutlinePage which leads to
   * JJOutlinePageContentProvider.setInput() which parses the document.
   */
  protected void updateOutlinePage() {
    if (jJJOutlinePage == null) {
      jJJOutlinePage = (JJOutlinePage) getAdapter(IContentOutlinePage.class);
    }
    jJJOutlinePage.setInput(getDocument());
    // get root node to build JJElement HashMap
    final JJOutlinePageContentProvider contentProvider = (JJOutlinePageContentProvider) jJJOutlinePage.getContentProvider();
    // if the outline is not up, then use the ContentProvider directly
    if (jJJOutlinePage.getControl() == null) {
      contentProvider.inputChanged(null, null, getDocument());
    }
    final JJNode rootNode = contentProvider.getAST();

    // if parsing failed we don't touch JJElements used for navigation and completion 
    if (rootNode == null || rootNode.getFirstToken().next == null) {
      return;
    }

    // clear and fill the JJElements HashMap for this Editor
    jJJElements.clear();
    rootNode.setJJElementsToUpdate(jJJElements);
    rootNode.buildJJNodesMap();
  }

  /**
   * @return jjElements
   */
  public JJElements getJJElements() {
    return jJJElements;
  }

  /**
   * @return document
   */
  public IDocument getDocument() {
    return getDocumentProvider().getDocument(getEditorInput());
  }

  /**
   * Sets the Selection given a Node of the AST.
   * 
   * @param aNode the JJNode to set the selection on
   */
  public void setSelection(final JJNode aNode) {
    if (aNode != null && aNode != JJNode.getOohsjjnode()) {
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
   * Updates spelling and colors. Currently does nothing as the editor is not redrawn.
   */
  public void updateSpellingAndColors() {
    //    Display display = Display.getCurrent();
    //    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    //    Color color = new Color(display, PreferenceConverter.getColor(store, JJPreferences.P_JJKEYWORD));
    //    getSourceViewer().getTextWidget().setBackground(color);
    //    fJJReconcilingStrategy.performUpdates();
  }

  /**
   * Triggers viewer updates if the check spelling preference has changed. Currently this is ineffective as
   * there are no listeners.
   * 
   * @see AbstractDecoratedTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
   * @since 3.3
   */
  @Override
  protected void handlePreferenceStoreChanged(final PropertyChangeEvent aEvent) {
    //    if (event.getProperty().equals(JJPreferences.P_CHECK_SPELLING)) {
    //      fJJReconcilingStrategy.performUpdates(true);
    //      return;
    //    }
    super.handlePreferenceStoreChanged(aEvent);
  }

}
