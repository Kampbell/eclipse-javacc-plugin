package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.*;
import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.Node;
import sf.eclipse.javacc.parser.Token;
import sf.eclipse.javacc.preferences.IPrefConstants;

/**
 * Editor designed for JavaCC files.<br>
 * Has subclass for JTB files.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.editors">
 * 
 * @see "http://www.realsolve.co.uk/site/tech/jface-text.php"
 * @see "https://www.eclipse.org/articles/Article-Folding-in-Eclipse-Text-Editors/folding.html"
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class JJEditor extends TextEditor implements IPrefConstants {

  // MMa 11/2009 : formatting and javadoc revision ; added constructor for subclass
  // MMa 12/2009 : added spell checking ; some renaming
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : refactoring / renamings
  // MMa 08/2011 : impacts of Call Hierarchy View enhancements
  // BF  05/2012 : refresh the presentation and check spelling when a color or spelling preference changes
  // BF  06/2012 : replaced see tags with inheritDoc tags
  // MMa 10/2012 : revised interactions with ReconcilingStrategy, OutlinePage and OutlineContentProvider ;
  //               use JJNode instead of IDocument ; adapted to non static parser
  // MMa 10/2014 : updated folding structure algorithm for summarizable annotations ; some renamings ;
  //               added PARSER_BEGIN & removed JAVACODE_PROD to the foldable nodes
  // MMa 11/2014 : added foldable comments ; added listeners to synchronize with Outline Page through
  //                on selection change (single-click) and no more through double-click ; fixed selection's
  //                display when called from the OutlinePage or the CallHierarchyView ;
  //               modified some modifiers ; some renamings ; added reset of OutlinePage revealing flag

  //  /** The source viewer configuration */
  //  private SourceViewerConfiguration                 jSourceViewerConf;

  /** The reconciling strategy */
  private ReconcilingStrategy                       jReconStrategy;

  /** The projection support */
  private ProjectionSupport                         jProjectionSupport;

  /** The saved additions to the projection annotations */
  private final Map<ProjectionAnnotation, Position> jSavedAdditions    = new HashMap<ProjectionAnnotation, Position>();

  //  /** The projection annotation model */
  //  private ProjectionAnnotationModel                 jAnnotationModel;

  /** The list of foldable positions */
  private final List<Position>                      jFoldablePositions = new ArrayList<Position>(100);

  /** The editor's character pairs matcher */
  private final CharacterPairMatcher                jCharPairMatcher   = new CharacterPairMatcher();

  /** The character pairs matching color */
  private Color                                     jColorMatchingChar;

  /** The editor's character pairs painter */
  private MatchingCharacterPainter                  jMatchingCharPainter;

  /** The Outline Page */
  private OutlinePage                               jOutlinePage;

  /** The parser */
  private JavaCCParser                              jParser;

  /** The AST root node built from the text */
  private JJNode                                    jAstRoot;

  /** The elements built from the AST root node */
  private Elements                                  jElements;

  //  /** A flag to tell whether we have just passed the initialization or not, in order not to reparse */
  //  private boolean                                   jJustInitialized   = false;

  /** The editor selection changed listener */
  private EditorSelectionChangedListener            jEditorSelectionChangedListener;

  //  /**
  //   * Standard constructor.
  //   */
  //  public JJEditor() {
  //    super();
  //  }

  /**
   * Initializes this editor.<br>
   * Called by {@link AbstractDecoratedTextEditor#AbstractDecoratedTextEditor()}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void initializeEditor() {
    super.initializeEditor();
    // DocumentProvider now set through plugin.xml
    //    setDocumentProvider(new UnusedDocumentProvider());
    // CodeScanner, Formatter, IndentStrategy, ContentAssist, ...
    //    jSourceViewerConf = new SourceViewerConfiguration(this);
    //    setSourceViewerConfiguration(jSourceViewerConf);
    setSourceViewerConfiguration(new JSourceViewerConfiguration(this));
    // used to synchronize Outline Page, folding structure and check spelling
    jReconStrategy = new ReconcilingStrategy(null, this);
    // used to retrieve Elements updated at each document parsing
    jElements = new Elements();
  }

  /**
   * Overridden to see when it is called and with what.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void doSetInput(final IEditorInput input) throws CoreException {
    super.doSetInput(input);
  }

  /**
   * Overridden in order to add matching character support and to install ProjectionSupport.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void createPartControl(final Composite aParent) {
    //    jJustInitialized = false;
    super.createPartControl(aParent);
    //    jJustInitialized = true;
    // Matching characters
    showMatchingCharacters();
    // Projection Support
    final ProjectionViewer pv = (ProjectionViewer) getSourceViewer();
    jProjectionSupport = new ProjectionSupport(pv, getAnnotationAccess(), getSharedColors());
    jProjectionSupport.addSummarizableAnnotationType(MARKER_ERROR);
    jProjectionSupport.addSummarizableAnnotationType(MARKER_WARNING);
    jProjectionSupport.addSummarizableAnnotationType(MARKER_INFO);
    jProjectionSupport.install();
    // turn projection mode on
    pv.doOperation(ProjectionViewer.TOGGLE);
    //    jAnnotationModel = pv.getProjectionAnnotationModel();
    // install selection listener
    jEditorSelectionChangedListener = new EditorSelectionChangedListener();
    jEditorSelectionChangedListener.install(getSelectionProvider());
  }

  /**
   * Overridden to return a ProjectionViewer instead of a SourceViewer.<br>
   * Called by {@link AbstractTextEditor#createPartControl(Composite)}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected ISourceViewer createSourceViewer(final Composite aParent, final IVerticalRuler aRuler,
                                             final int aStyles) {
    fAnnotationAccess = getAnnotationAccess();
    fOverviewRuler = createOverviewRuler(getSharedColors());
    final ISourceViewer pv = new ProjectionViewer(aParent, aRuler, getOverviewRuler(),
                                                  isOverviewRulerVisible(), aStyles);
    // ensure decoration support has been created and configured
    getSourceViewerDecorationSupport(pv);
    return pv;
  }

  /**
   * Overridden to add edit location in History. Alt + left arrow should jump back to correct position.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void updateContentDependentActions() {
    super.updateContentDependentActions();
    markInNavigationHistory();
  }

  /** {@inheritDoc} */
  @Override
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] {
      JJEDITOR_SCOPE_ID });
  }

  /**
   * Disposes the colors and the code scanner.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    // dispose Color
    if (jColorMatchingChar != null) {
      jColorMatchingChar.dispose();
      jColorMatchingChar = null;
    }
    // dispose SourceViewerConfiguration
    JSourceViewerConfiguration jsvc = (JSourceViewerConfiguration) getSourceViewerConfiguration();
    if (jsvc != null) {
      jsvc.dispose();
      jsvc = null;
    }
    // uninstall EditorSelectionChangedListener
    if (jEditorSelectionChangedListener != null) {
      jEditorSelectionChangedListener.uninstall(getSelectionProvider());
      jEditorSelectionChangedListener = null;
    }
    // remove reference in the Call Hierarchy View
    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    Assert.isTrue(windows.length > 0);
    final IWorkbenchPage page = windows[0].getActivePage();
    if (page != null) {
      final CallHierarchyView chv = (CallHierarchyView) page.findView(CALL_HIERARCHY_ID);
      if (chv == null) {
        return;
      }
      chv.jEditor = null;
    }
    super.dispose();
  }

  /**
   * Returns the OutlinePage declared on IAdaptable for an {@link IContentOutlinePage} class, and for other
   * classes calls the super method.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Object getAdapter(@SuppressWarnings("rawtypes") final Class aRequiredClass) {
    if (IContentOutlinePage.class.equals(aRequiredClass)) {
      if (jOutlinePage == null) {
        jOutlinePage = new OutlinePage(this);
        updateOutlinePage();
      }
      return jOutlinePage;
    }
    if (jProjectionSupport != null) {
      final Object adapter = jProjectionSupport.getAdapter(getSourceViewer(), aRequiredClass);
      if (adapter != null) {
        return adapter;
      }
    }
    return super.getAdapter(aRequiredClass);
  }

  /**
   * Relay as {@link AbstractTextEditor#getSourceViewer()} is protected.
   * 
   * @return the source viewer
   */
  public final ISourceViewer getSourceViewerPlease() {
    return getSourceViewer();
  }

  /**
   * Performs the different updates (Outline Page, Call Hierarchy View, Folding Structure) after parsing the
   * document.
   */
  public void performUpdates() {
    parse();
    updateOutlinePage();
    updateCallHierarchyView();
    updateFoldingStructure();
  }

  /**
   * Parses the document and builds the elements if parsing did not fail.
   */
  private void parse() {
    //    if (!jJustInitialized) {
    final StringReader in = new StringReader(getDocument().get());
    if (jParser == null) {
      jParser = new JavaCCParser(in);
    }
    else {
      jParser.ReInit(in);
    }
    jAstRoot = jParser.parse(in);
    in.close();
    // if parsing failed astRoot is an ASTroot with a single Token whose image is the error text
    if (jAstRoot != null && jAstRoot.getFirstToken().next != null) {
      jElements.clear();
      jAstRoot.buildElements(jElements);
    }
    else {
      jOutlinePage.jRevealing = false;
    }
    //    }
    //    jJustInitialized = false;
  }

  /**
   * Updates the Outline Page and reveals the current location.<br>
   * Calls {@link OutlinePage#setNodes(JJNode, JJNode)} with the current AST root node, which leads to
   * {@link TreeViewer#setInput(Object)} which will update the Outline Page.
   */
  public void updateOutlinePage() {
    if (jOutlinePage == null) {
      jOutlinePage = new OutlinePage(this);
    }
    // if synchronized, update it
    if (jOutlinePage.isSyncWithEditor()) {
      jOutlinePage.setNodes(jAstRoot, getOpchvNodeFromSelection());
    }
  }

  /**
   * Updates the Call Hierarchy View and reveals the current location if the view is synchronized with the
   * editor.
   */
  public void updateCallHierarchyView() {
    // find the Call Hierarchy View
    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    Assert.isTrue(windows.length > 0);
    final IWorkbenchPage page = windows[0].getActivePage();
    if (page != null) {
      final CallHierarchyView chv = (CallHierarchyView) page.findView(CALL_HIERARCHY_ID);
      if (chv == null) {
        return;
      }
      // case project file with synchronized CHV or case dragged and dropped file, update the CHV
      final IEditorInput editorInput = getEditorInput();
      if ((editorInput instanceof IFileEditorInput && chv.isSyncWithEditor(((IFileEditorInput) editorInput).getFile()))
          || (editorInput instanceof FileStoreEditorInput)) {
        final JJNode jjNode = getOpchvNodeFromSelection();
        chv.setSelection(jjNode, this);
      }
    }
  }

  /**
   * @return the top node from the OP / CHV list of nodes
   */
  private JJNode getOpchvNodeFromSelection() {
    int line = 0;
    final ISelection selection = getSelectionProvider().getSelection();
    if (selection instanceof ITextSelection) {
      line = ((ITextSelection) selection).getStartLine();
    }
    return jElements.getOpchvTopNodeFromLine(line);
  }

  /**
   * Tells the editor which regions are expandable or collapsable.
   */
  private void updateFoldingStructure() {
    final ProjectionViewer pv = (ProjectionViewer) getSourceViewer();
    final ProjectionAnnotationModel model = pv.getProjectionAnnotationModel();
    if (model == null) {
      // saw null model in case of drag and drop in the editor area
      AbstractActivator.logErr("null AnnotationModel, unable to updateFoldingStructure() in JJEditor ;" //$NON-NLS-1$
                               + " please report this message with the actions which led to it"); //$NON-NLS-1$
      return;
    }
    computeFoldablePositions();
    final Map<ProjectionAnnotation, Position> additions;
    additions = new HashMap<ProjectionAnnotation, Position>(jFoldablePositions.size());
    for (final Position pos : jFoldablePositions) {
      boolean collapsed = false;
      // search through saved annotations, to keep state (collapsed or not)
      final Iterator<Entry<ProjectionAnnotation, Position>> e = jSavedAdditions.entrySet().iterator();
      while (e.hasNext()) {
        final Entry<ProjectionAnnotation, Position> mapEntry = e.next();
        final Position value = mapEntry.getValue();
        if (value.equals(pos)) {
          collapsed = mapEntry.getKey().isCollapsed();
          break;
        }
      }
      // add it to the additions
      additions.put(new ProjectionAnnotation(collapsed), pos);
    }
    // deletions are the saved additions
    final Annotation[] deletions = (jSavedAdditions.keySet().toArray(new Annotation[jSavedAdditions.size()]));
    model.modifyAnnotations(deletions, additions, null);
    // save the additions
    jSavedAdditions.clear();
    jSavedAdditions.putAll(additions);
  }

  /**
   * Updates spelling and colors presentation when preference changes.
   */
  public void updateSpellingAndColors() {
    getReconcilingStrategy().checkSpelling();
    jMatchingCharPainter = null;
    showMatchingCharacters();
    getSourceViewer().invalidateTextPresentation();
  }

  /**
   * Adds a Painter to show matching characters.
   */
  private void showMatchingCharacters() {
    if (jMatchingCharPainter == null) {
      if (getSourceViewer() instanceof ISourceViewerExtension2) {
        jMatchingCharPainter = new MatchingCharacterPainter(getSourceViewer(), jCharPairMatcher);
        final Display display = Display.getCurrent();
        final IPreferenceStore store = AbstractActivator.getDefault().getPreferenceStore();
        jColorMatchingChar = new Color(display, PreferenceConverter.getColor(store, P_MATCHING_CHAR));
        jMatchingCharPainter.setColor(jColorMatchingChar);
        final ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(jMatchingCharPainter);
      }
    }
  }

  /**
   * Selects, reveals and highlights in the JJEditor the selection corresponding to a given node of the AST.
   * 
   * @param aNode - the JJNode to set the selection on
   */
  public void selectNode(final JJNode aNode) {
    if (aNode != null && aNode != JJNode.getOohsjjnode()) {
      try {
        final IDocument doc = getDocument();
        if (doc != null) {
          final int start = doc.getLineOffset(aNode.getBeginLine() - 1) + aNode.getBeginColumn() - 1;
          final int end = doc.getLineOffset(aNode.getEndLine() - 1) + aNode.getEndColumn() - 1;
          final int len = end - start + 1;
          selectAndReveal(start, len);
          resetHighlightRange();
          setHighlightRange(start, len, true);
          final Token nameToken = aNode.getNameToken();
          final int nameStart = doc.getLineOffset(nameToken.beginLine - 1) + nameToken.beginColumn - 1;
          final int nameEnd = doc.getLineOffset(nameToken.endLine - 1) + nameToken.endColumn - 1;
          selectAndReveal(nameStart, nameEnd - nameStart + 1);
          markInNavigationHistory();
        }
      } catch (final IllegalArgumentException e) {
        AbstractActivator.logBug(e);
        resetHighlightRange();
      } catch (final BadLocationException e) {
        AbstractActivator.logBug(e);
        resetHighlightRange();
      }
    }
  }

  /**
   * Computes and adds the folding positions.
   */
  private void computeFoldablePositions() {
    if (jAstRoot == null) {
      return;
    }
    // clean old positions map
    jFoldablePositions.clear();
    // process the tree to add foldable nodes
    addNodeFoldablePositions(jAstRoot);
    // add the foldable comments
    addFoldableComments();
  }

  /**
   * Processes recursively a given node and adds its folding positions if applicable.
   * 
   * @param aJJNode - the node to process
   */
  private void addNodeFoldablePositions(final JJNode aJJNode) {
    // add a region if the node is one of the appropriate types
    // note that JJTJAVACODE_PROD is not foldable as the just following method declaration is foldable
    final int id = aJJNode.getId();
    if (id == JJTBNF_PROD || id == JJTREGULAR_EXPR_PROD || id == JJTREGEXPR_SPEC || id == JJTCLAORINTDECL
        || id == JJTENUMDECL || id == JJTANNOTTYPEDECL || id == JJTMETHODDECL || id == JJTCONSTRDECL
        || id == JJTJAVACC_OPTIONS || id == JJTTOKEN_MANAGER_DECLS || id == JJTPARSER_BEGIN) {
      try {
        final IDocument doc = getDocument();
        // lines in JavaCC begin at 1, in Eclipse begin at 0 ; take nodes only on 2 or more lines
        final int startLine = aJJNode.getBeginLine() - 1;
        final int endLine = aJJNode.getEndLine() - 1;
        if (startLine < endLine) {
          final int start = doc.getLineOffset(startLine);
          int end;
          if (doc.getNumberOfLines() > endLine + 1) {
            end = doc.getLineOffset(endLine + 1);
          }
          else {
            end = doc.getLineOffset(endLine) + doc.getLineLength(endLine);
          }
          jFoldablePositions.add(new Position(start, end - start));
        }
      } catch (final BadLocationException e) {
        AbstractActivator.logBug(e, aJJNode.getBeginLine(), aJJNode.getEndLine());
        return;
      }
    }
    // process children
    final Node[] children = aJJNode.getChildren();
    if (children != null) {
      for (final Node child : children) {
        addNodeFoldablePositions((JJNode) child);
      }
    }
    return;
  }

  /**
   * Adds the multi line comments and the blocs of single line comments.
   */
  private void addFoldableComments() {
    final ITypedRegion[] nonCodeRegions = jReconStrategy.getNonCodeRegions();
    if (nonCodeRegions == null) {
      return;
    }
    int offset = 0;
    int firstSlcOffset = 0;
    int len = 0;
    int multipleSlcLen = 0;
    int firstSlcLine = -1;
    int lastSlcLine = -1;
    final IDocument doc = getDocument();
    try {
      for (final ITypedRegion region : nonCodeRegions) {
        offset = region.getOffset();
        len = region.getLength();
        final int startLine = doc.getLineOfOffset(offset);
        final int endLine = doc.getLineOfOffset(offset + len);
        if (region.getType() == LINE_CMT_CONTENT_TYPE) {
          // case contiguous single line comments
          if (firstSlcLine == -1) {
            // first new one : memorize
            lastSlcLine = firstSlcLine = startLine;
            firstSlcOffset = offset;
            multipleSlcLen = len;
          }
          else {
            // next new one
            if (startLine == lastSlcLine + 1) {
              // contiguous : see if alone on the line or not
              final int slcLastOffset = firstSlcOffset + multipleSlcLen;
              int pos = offset - 1;
              boolean alone = true;
              while (pos > slcLastOffset) {
                if (!Character.isWhitespace(doc.getChar(pos))) {
                  alone = false;
                  break;
                }
                pos--;
              }
              if (alone) {
                // alone : memorize
                lastSlcLine = startLine;
                // cannot just increment with len because of end of lines
                multipleSlcLen = offset + len - firstSlcOffset;
              }
              else {
                // not alone : add old set then memorize
                jFoldablePositions.add(new Position(firstSlcOffset, multipleSlcLen));
                lastSlcLine = firstSlcLine = startLine;
                firstSlcOffset = offset;
                multipleSlcLen = len;
              }
            }
            else {
              // not contiguous : add old set then memorize
              if (firstSlcLine < lastSlcLine) {
                jFoldablePositions.add(new Position(firstSlcOffset, multipleSlcLen));
              }
              lastSlcLine = firstSlcLine = startLine;
              firstSlcOffset = offset;
              multipleSlcLen = len;
            }
          }
        }
        else {
          // case multi line comments
          // first add memorized set of single line comments
          if (firstSlcLine < lastSlcLine) {
            jFoldablePositions.add(new Position(firstSlcOffset, multipleSlcLen));
            firstSlcLine = lastSlcLine = -1;
          }
          // then add multi line comments
          if (startLine != endLine) {
            final int startLineOffset = doc.getLineOffset(startLine);
            int endLineOffset;
            if (doc.getNumberOfLines() > endLine + 1) {
              endLineOffset = doc.getLineOffset(endLine + 1);
            }
            else {
              endLineOffset = doc.getLineOffset(endLine) + doc.getLineLength(endLine);
            }
            jFoldablePositions.add(new Position(startLineOffset, endLineOffset - startLineOffset));
          }
        }
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, offset, len);
    }
  }

  /**
   * Extends the selection to a whole word (including the '#' for private label identifiers and JJTree node
   * descriptors).<br>
   * Note : quite similar than {@link #selectWord(IDocument, IRegion)}, but cast is not possible.
   * 
   * @param aSelection - the selection
   * @return the extended selection
   */
  public ITextSelection selectWord(final ITextSelection aSelection) {
    final int caretPos = aSelection.getOffset();
    final IDocument doc = getDocument();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c) && c != '#') {
          break;
        }
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      final int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos++;
      }
      endPos = pos;
      return new TextSelection(doc, startPos, endPos - startPos);
    } catch (final BadLocationException e) {
      // Do nothing, except returning
      AbstractActivator.logBug(e);
    }
    return aSelection;
  }

  /**
   * Extends Selection to a whole Word (not including the '#' for private label identifiers and JJTree node
   * descriptors).<br>
   * Note : quite similar than {@link #selectWord(ITextSelection)}, but cast is not possible.
   * 
   * @param aDoc - the document
   * @param aSelection - the selected text
   * @return the extended selection (up to a whole word)
   */
  public static ITextSelection selectWord(final IDocument aDoc, final IRegion aSelection) {
    final int caretPos = aSelection.getOffset();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = aDoc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      final int length = aDoc.getLength();
      while (pos < length) {
        c = aDoc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos++;
      }
      endPos = pos;
      return new TextSelection(aDoc, startPos, endPos - startPos);
    } catch (final BadLocationException e) {
      // so nothing, except returning
      AbstractActivator.logBug(e);
    }
    return null;
  }

  /**
   * @return the astRoot
   */
  public final JJNode getAstRoot() {
    return jAstRoot;
  }

  /**
   * @return document
   */
  public final IDocument getDocument() {
    return getDocumentProvider().getDocument(getEditorInput());
  }

  /**
   * @return jElements
   */
  public final Elements getElements() {
    return jElements;
  }

  /**
   * @return the reconciling strategy
   */
  public final ReconcilingStrategy getReconcilingStrategy() {
    return jReconStrategy;
  }

  /**
   * Updates the Outline Page on a selection change.
   */
  public void selectionChanged() {
    if (getSelectionProvider() == null) {
      return;
    }
    updateOutlinePage();
  }

  /**
   * Updates the Java outline page selection and this editor's range indicator.<br>
   * Copied from JavaEditor.
   */
  private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {

    /** Standard constructor */
    EditorSelectionChangedListener() {
    }

    /** {@inheritDoc } */
    @Override
    public void selectionChanged(@SuppressWarnings("unused") final SelectionChangedEvent aEvent) {
      JJEditor.this.selectionChanged();
    }

  }

  /**
   * Internal implementation class for a change listener.<br>
   * Copied from JavaEditor.
   */
  protected abstract class AbstractSelectionChangedListener implements ISelectionChangedListener {

    /**
     * Installs this selection changed listener with the given selection provider. If the selection provider
     * is a post selection provider, post selection changed events are the preferred choice, otherwise normal
     * selection changed events are requested.
     * 
     * @param aSelectionProvider the selection provider
     */
    void install(final ISelectionProvider aSelectionProvider) {
      if (aSelectionProvider == null) {
        return;
      }
      if (aSelectionProvider instanceof IPostSelectionProvider) {
        final IPostSelectionProvider provider = (IPostSelectionProvider) aSelectionProvider;
        provider.addPostSelectionChangedListener(this);
      }
      else {
        aSelectionProvider.addSelectionChangedListener(this);
      }
    }

    /**
     * Removes this selection changed listener from the given selection provider.
     * 
     * @param aSelectionProvider the selection provider
     */
    void uninstall(final ISelectionProvider aSelectionProvider) {
      if (aSelectionProvider == null) {
        return;
      }
      if (aSelectionProvider instanceof IPostSelectionProvider) {
        final IPostSelectionProvider provider = (IPostSelectionProvider) aSelectionProvider;
        provider.removePostSelectionChangedListener(this);
      }
      else {
        aSelectionProvider.removeSelectionChangedListener(this);
      }
    }
  }

}
