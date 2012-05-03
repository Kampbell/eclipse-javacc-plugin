package sf.eclipse.javacc.editors;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.options.PreferencesInitializer;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.Node;

/**
 * Reconciler strategy which updates the outline view, the folding structure and checks spelling on a document
 * change.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public class JJReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension,
                                  JavaCCParserTreeConstants {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls regions
  // MMa 12/2009 : added spell checking ; some renaming
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed computePositions on parsing failure
  // MMa 05/2012 : added Bill Fenlason's hack proposal in performUpdates()

  /** The current editor */
  JJEditor                 jJJEditor;
  /** The array of folding positions */
  ArrayList<Position>      jPositions       = new ArrayList<Position>();
  /** The current source viewer */
  ISourceViewer            jSourceViewer    = null;
  /** The current spelling context */
  private SpellingContext  jSpellingContext = null;
  /** The current document */
  private IDocument        jDocument;
  /** The current progress monitor */
  private IProgressMonitor jProgressMonitor;
  /** The (previous / current) spelling annotations */
  Annotation[]             jSpellingAnnotations;

  /**
   * Standard constructor.
   * 
   * @param aSourceViewer the current source viewer
   * @param aJJEditor the current editor
   */
  public JJReconcilingStrategy(final ISourceViewer aSourceViewer, final JJEditor aJJEditor) {
    jSourceViewer = aSourceViewer;
    jJJEditor = aJJEditor;
    jSpellingContext = new SpellingContext();
  }

  /**
   * @param aSourceViewer the source viewer to set
   */
  public void setSourceViewer(final ISourceViewer aSourceViewer) {
    jSourceViewer = aSourceViewer;
  }

  /**
   * Calls the update() method.
   * 
   * @see IReconcilingStrategy#setDocument(IDocument)
   */
  @Override
  public void setDocument(final IDocument aDoc) {
    jDocument = aDoc;
    performUpdates();
  }

  /**
   * Reconciles the whole document.
   * 
   * @see IReconcilingStrategyExtension#initialReconcile()
   */
  @Override
  public void initialReconcile() {
    performUpdates();
  }

  /**
   * Updates the whole outline view and checks the whole document spelling.
   * 
   * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
   */
  @Override
  public void reconcile(@SuppressWarnings("unused") final DirtyRegion aDirtyRegion,
                        @SuppressWarnings("unused") final IRegion aSubRegion) {
    performUpdates();
  }

  /**
   * Updates the whole outline view and checks the whole document spelling.
   * 
   * @see IReconcilingStrategy#reconcile(IRegion)
   */
  @Override
  public void reconcile(@SuppressWarnings("unused") final IRegion aPartition) {
    performUpdates();
  }

  /**
   * Performs the different updates : the outline view, the folding structure, the check spelling
   */
  public void performUpdates() {
    computePositions();

    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        jJJEditor.updateOutlinePage();
        jJJEditor.updateFoldingStructure(jPositions);
        checkSpelling();
        jSourceViewer.invalidateTextPresentation();
      }
    });
  }

  /**
   * Computes and adds folding positions.
   */
  private void computePositions() {
    // parse document to get the AST
    final StringReader in = new StringReader(jJJEditor.getDocument().get());
    final JJNode root = JavaCCParser.parse(in);
    in.close();

    // if parsing failed (we get a void id, instead of an option or parser_begin id), do not change anything
    if (root.getId() == JJTVOID) {
      return;
    }

    // clean old positions
    jPositions.clear();
    // process the tree to add folding positions
    walksDown(root);
  }

  /**
   * Processes recursively a given node and adds the folding positions.
   * 
   * @param aJJNode the node to process
   */
  public void walksDown(final JJNode aJJNode) {
    // add a region if the node is one of the appropriate types
    final int id = aJJNode.getId();
    if (id == JJTBNF_PRODUCTION || id == JJTREGULAR_EXPR_PRODUCTION || id == JJTREGEXPR_SPEC
        || id == JJTCLASSORINTERFACEDECLARATION || id == JJTMETHODDECLARATION || id == JJTPARSER_BEGIN
        || id == JJTJAVACC_OPTIONS || id == JJTTOKEN_MANAGER_DECLS || id == JJTJAVACODE_PRODUCTION) {
      try {
        final IDocument doc = jJJEditor.getDocument();
        // lines in JavaCC begin at 1, in Eclipse begin at 0
        final int start = doc.getLineOffset(aJJNode.getBeginLine() - 1);
        final int end = doc.getLineOffset(aJJNode.getEndLine());
        jPositions.add(new Position(start, end - start));
      } catch (final BadLocationException e) {
        // Ignore
        //        e.printStackTrace();
      }
    }
    // process children
    final Node[] children = aJJNode.getChildren();
    if (children != null) {
      for (final Node child : children) {
        walksDown((JJNode) child);
      }
    }
    return;
  }

  /**
   * Sets the progress monitor.
   * 
   * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
   */
  @Override
  public void setProgressMonitor(final IProgressMonitor aProgressMonitor) {
    jProgressMonitor = aProgressMonitor;
  }

  /**
   * Performs the spelling checking (if enabled).
   */
  void checkSpelling() {
    if (!Activator.getDefault().getPreferenceStore().getBoolean(PreferencesInitializer.P_NO_SPELL_CHECKING)) {
      final List<SpellingProblem> problems = collectSpellingProblems();
      if (problems != null) {
        final Map<Annotation, Position> annotations = createAnnotations(problems);
        final IAnnotationModelExtension model = (IAnnotationModelExtension) jSourceViewer.getAnnotationModel();
        model.replaceAnnotations(jSpellingAnnotations, annotations);
        jSpellingAnnotations = annotations.keySet().toArray(new Annotation[annotations.size()]);
      }
    }
  }

  /**
   * Asks the spelling service to check the document comments regions and returns the spelling problems.
   * 
   * @return the list of SpellingProblems
   */
  private List<SpellingProblem> collectSpellingProblems() {
    //        final SpellingService service = EditorsUI.getSpellingService();
    final IPreferenceStore fPreferences = EditorsPlugin.getDefault().getPreferenceStore();
    final JJSpellingService service = new JJSpellingService(fPreferences);
    final SpellingProblemCollector collector = new SpellingProblemCollector();
    final ITypedRegion[] nonCodeRegions = getNonCodeRegions();
    if (nonCodeRegions == null) {
      return null;
    }
    service.check(jDocument, nonCodeRegions, jSpellingContext, collector, jProgressMonitor);
    final List<SpellingProblem> problems = collector.getProblems();
    return problems;
  }

  /**
   * @return the array of the document non code regions
   */
  private ITypedRegion[] getNonCodeRegions() {
    final int docLength = jDocument.getLength();
    if (!(jDocument instanceof IDocumentExtension3)) {
      // should not occur
      return null;
    }
    final IDocumentExtension3 ext = (IDocumentExtension3) jDocument;
    final IDocumentPartitioner partitioner = ext.getDocumentPartitioner(JJDocumentProvider.JJ_PARTITIONING_ID);
    if (partitioner == null) {
      // should not occur
      return null;
    }
    final ITypedRegion[] partioningRegions = partitioner.computePartitioning(0, docLength);
    int nonCodeRegionsNumber = 0;
    for (final ITypedRegion region : partioningRegions) {
      if (region.getType() == JJDocumentProvider.JJ_COMMENT_CONTENT_TYPE) {
        nonCodeRegionsNumber++;
      }
    }
    if (nonCodeRegionsNumber == partioningRegions.length) {
      return partioningRegions;
    }
    final ITypedRegion[] nonCodeRegions = new ITypedRegion[nonCodeRegionsNumber];
    int i = 0;
    for (final ITypedRegion region : partioningRegions) {
      if (region.getType() == JJDocumentProvider.JJ_COMMENT_CONTENT_TYPE) {
        nonCodeRegions[i++] = region;
      }
    }
    return nonCodeRegions;
  }

  /**
   * Builds and returns a map of annotations corresponding to the given problems, and their positions.
   * 
   * @param aProblems the spelling problems to annotate
   * @return the map of annotations and their positions
   */
  private Map<Annotation, Position> createAnnotations(final List<SpellingProblem> aProblems) {
    final Map<Annotation, Position> annotations = new HashMap<Annotation, Position>();
    for (final Iterator<SpellingProblem> it = aProblems.iterator(); it.hasNext();) {
      final SpellingProblem problem = it.next();
      final Annotation annotation = new SpellingAnnotation(problem);
      final Position position = new Position(problem.getOffset(), problem.getLength() + 1);
      annotations.put(annotation, position);
    }
    return annotations;
  }

  /**
   * Inner class as a bridge with the infrastructure when collecting the spelling problems.
   * 
   * @see ISpellingProblemCollector
   */
  class SpellingProblemCollector implements ISpellingProblemCollector {

    /** The current problems list */
    List<SpellingProblem> jProblems;

    /**
     * Creates a new problems list.
     * 
     * @see ISpellingProblemCollector#beginCollecting()
     */
    @Override
    public void beginCollecting() {
      jProblems = new LinkedList<SpellingProblem>();
    }

    /**
     * Adds a new problem to the problems list.
     * 
     * @see ISpellingProblemCollector#accept(SpellingProblem)
     */
    @Override
    public void accept(final SpellingProblem aProblem) {
      jProblems.add(aProblem);
    }

    /**
     * Does nothing (at the end of problems collection)
     * 
     * @see ISpellingProblemCollector#endCollecting()
     */
    @Override
    public void endCollecting() {
      // nothing done here
    }

    /**
     * @return the problems list
     */
    public List<SpellingProblem> getProblems() {
      return jProblems;
    }
  }
}
