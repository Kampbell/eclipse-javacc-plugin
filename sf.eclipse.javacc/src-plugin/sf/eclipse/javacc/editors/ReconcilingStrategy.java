package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.BLOCK_CMT_CONTENT_TYPE;
import static sf.eclipse.javacc.base.IConstants.JAVADOC_CONTENT_TYPE;
import static sf.eclipse.javacc.base.IConstants.LINE_CMT_CONTENT_TYPE;
import static sf.eclipse.javacc.base.IConstants.PARTITIONING_ID;
import static sf.eclipse.javacc.preferences.IPrefConstants.P_NO_SPELL_CHECKING;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.scanners.CodeColorScanner;

/**
 * Reconciler strategy which, on a document change, tells the JJEditor to update the Outline Page, the Call
 * Hierarchy View and the folding structure, and does itself the check spelling .
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 */
public class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls regions
  // MMa 12/2009 : added spell checking ; some renaming
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed computePositions on parsing failure
  // MMa 05/2012 : added Bill Fenlason's hack proposal in performUpdates()
  // BF  05/2012 : added Block comments, Line comments and Javadoc partitions
  // BF  05/2012 : changed check spelling to remove annotations when spell checking is off
  // MMa 10/2012 : used static import ; adapted to modifications in grammar nodes and new nodes ;
  //               revised (simplified) interactions with JJEditor ; renamed
  // MMa 11/2014 : changed collectSpellingProblems() to use directly the standard spelling service ;
  //               modified some modifiers
  // MMa 02/2016 : added getRegions() ; improved reconciling regions

  /** The editor */
  final JJEditor                jEditor;

  /** The source viewer */
  ISourceViewer                 jSourceViewer;

  /** The source viewer configuration (as a way to reach the {@link CodeColorScanner} */
  JSourceViewerConfiguration    jSourceViewerConf;

  /** The spelling context */
  private final SpellingContext jSpellingContext;

  /** The document */
  private IDocument             jDocument;

  /** The progress monitor */
  private IProgressMonitor      jProgressMonitor;

  /** The (previous / current) spelling annotations */
  private Annotation[]          jSpellingAnnotations;

  /**
   * Constructor.
   * 
   * @param aSourceViewer - the current source viewer
   * @param aJJEditor - the current editor
   * @param aSourceViewerConf - the calling source viewer configuration
   */
  public ReconcilingStrategy(final ISourceViewer aSourceViewer, final JJEditor aJJEditor,
                             final JSourceViewerConfiguration aSourceViewerConf) {
    jSourceViewer = aSourceViewer;
    jSourceViewerConf = aSourceViewerConf;
    jEditor = aJJEditor;
    jSpellingContext = new SpellingContext();
  }

  /**
   * @param aSourceViewer - the source viewer to set
   */
  public final void setSourceViewer(final ISourceViewer aSourceViewer) {
    //    Assert.isTrue(jSourceViewer == null || jSourceViewer == aSourceViewer);
    jSourceViewer = aSourceViewer;
  }

  /**
   * Calls the update methods.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final void setDocument(final IDocument aDoc) {
    jDocument = aDoc;
    //    performUpdates();
  }

  /**
   * Calls the updates method.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final void initialReconcile() {
    updateJEditor();
    updateSpelling();
  }

  /**
   * Calls the updates method.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final void reconcile(@SuppressWarnings("unused") final DirtyRegion aDirtyRegion,
                              final IRegion aSubRegion) {
    updateJEditor();
    updateSpelling(aSubRegion);
  }

  /**
   * Calls the updates method.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final void reconcile(final IRegion aPartition) {
    updateJEditor();
    updateSpelling(aPartition);
  }

  /** {@inheritDoc} */
  @Override
  public final void setProgressMonitor(final IProgressMonitor aProgressMonitor) {
    jProgressMonitor = aProgressMonitor;
  }

  /**
   * Tells and waits for the JJEditor to perform its different updates (Outline Page, folding structure).
   */
  private void updateJEditor() {
    Display.getDefault().syncExec(new Runnable() {

      /** {@inheritDoc} */
      @Override
      public void run() {
        jEditor.performUpdates();
      }
    });
  }

  /**
   * Performs asynchronously the check spelling.
   */
  private void updateSpelling() {
    Display.getDefault().asyncExec(new Runnable() {

      /** {@inheritDoc} */
      @Override
      public void run() {
        checkSpelling();
        // want for a better way ...
        jSourceViewerConf.jCodeScanner.jJavaCCCodeRule.initialize();
        jSourceViewer.invalidateTextPresentation();
      }
    });
  }

  /**
   * Performs asynchronously check spelling on a partition.
   * 
   * @param aPartition - the partition to check
   */
  private void updateSpelling(final IRegion aPartition) {
    Display.getDefault().asyncExec(new Runnable() {

      /** {@inheritDoc} */
      @Override
      public void run() {
        checkSpelling();
        // want for a better way ...
        jSourceViewerConf.jCodeScanner.jJavaCCCodeRule.initialize();
        ((ITextViewerExtension2) jSourceViewer).invalidateTextPresentation(aPartition.getOffset(),
                                                                           aPartition.getLength());
      }
    });
  }

  /**
   * Performs the spelling checking (if enabled).
   */
  public void checkSpelling() {
    final IAnnotationModelExtension model = (IAnnotationModelExtension) jSourceViewer.getAnnotationModel();
    if (model != null) {
      if (!AbstractActivator.getDefault().getPreferenceStore().getBoolean(P_NO_SPELL_CHECKING)) {
        final List<SpellingProblem> problems = collectSpellingProblems();
        if (problems != null) {
          final Map<Annotation, Position> annotations = createAnnotations(problems);
          model.replaceAnnotations(jSpellingAnnotations, annotations);
          jSpellingAnnotations = annotations.keySet().toArray(new Annotation[annotations.size()]);
        }
      }
      else {
        model.replaceAnnotations(jSpellingAnnotations, null);
        jSpellingAnnotations = null;
      }
    }
    else {
      // saw null model in case of drag and drop in the editor area
      AbstractActivator.logErr("Null AnnotationModel, unable to checkSpelling() in ReconcilingStrategy ;" //$NON-NLS-1$
                               + " please report this message with the actions which led to it"); //$NON-NLS-1$
    }
  }

  /**
   * Asks the spelling service to check the document comments regions and returns the spelling problems.
   * 
   * @return the list of SpellingProblems
   */
  private List<SpellingProblem> collectSpellingProblems() {
    final SpellingService service = EditorsUI.getSpellingService();
    // EditorsPlugin & Activator does not return the same store (qualified names are org.eclipse.ui.editors
    // & sf.eclipse.javacc) and the default value for a boolean preference are not the same (true & false)
    // preference is in .metadata\.plugins\org.eclipse.core.runtime\.settings\org.eclipse.ui.editors.prefs
    //    final IPreferenceStore storew = EditorsPlugin.getDefault().getPreferenceStore();
    //    final boolean defw = storew.getDefaultBoolean("spellingEnabled");
    //    final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    //    final boolean def = store.getDefaultBoolean("spellingEnabled");
    //    store.setDefault("spellingEnabled", true);
    //    final SpellingService service = new SpellingService(store);
    //    final SpellingServiceJJ service = new SpellingServiceJJ(store);
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
  protected ITypedRegion[] getNonCodeRegions() {
    final ITypedRegion[] partioningRegions = getRegions();
    if (partioningRegions == null) {
      return null;
    }

    int nonCodeRegionsNumber = 0;
    for (final ITypedRegion region : partioningRegions) {
      final String regionType = region.getType();
      if (LINE_CMT_CONTENT_TYPE.equals(regionType) || BLOCK_CMT_CONTENT_TYPE.equals(regionType)
          || JAVADOC_CONTENT_TYPE.equals(regionType)) {
        nonCodeRegionsNumber++;
      }
    }
    if (nonCodeRegionsNumber == partioningRegions.length) {
      return partioningRegions;
    }

    final ITypedRegion[] nonCodeRegions = new ITypedRegion[nonCodeRegionsNumber];
    int i = 0;
    for (final ITypedRegion region : partioningRegions) {
      final String regionType = region.getType();
      if (LINE_CMT_CONTENT_TYPE.equals(regionType) || BLOCK_CMT_CONTENT_TYPE.equals(regionType)
          || JAVADOC_CONTENT_TYPE.equals(regionType)) {
        nonCodeRegions[i++] = region;
      }
    }
    return nonCodeRegions;
  }

  /**
   * @return the array of the document regions
   */
  protected ITypedRegion[] getRegions() {
    final int docLength = jDocument.getLength();
    if (!(jDocument instanceof IDocumentExtension3)) {
      // should not occur
      return null;
    }
    final IDocumentExtension3 ext = (IDocumentExtension3) jDocument;
    final IDocumentPartitioner partitioner = ext.getDocumentPartitioner(PARTITIONING_ID);
    if (partitioner == null) {
      // should not occur
      return null;
    }
    return partitioner.computePartitioning(0, docLength);
  }

  /**
   * Builds and returns a map of annotations corresponding to the given problems, and their positions.
   * 
   * @param aProblems - the spelling problems to annotate (must not be null)
   * @return the map of annotations and their positions
   */
  private static Map<Annotation, Position> createAnnotations(final List<SpellingProblem> aProblems) {
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
   */
  class SpellingProblemCollector implements ISpellingProblemCollector {

    /** The current problems list */
    protected List<SpellingProblem> jProblems;

    /**
     * Creates a new problems list.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public final void beginCollecting() {
      jProblems = new LinkedList<SpellingProblem>();
    }

    /**
     * Adds a new problem to the problems list.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public final void accept(final SpellingProblem aProblem) {
      jProblems.add(aProblem);
    }

    /**
     * Does nothing (at the end of problems collection)
     * <p>
     * {@inheritDoc}
     */
    @Override
    public final void endCollecting() {
      // nothing done here
    }

    /**
     * @return the problems list
     */
    public final List<SpellingProblem> getProblems() {
      return jProblems;
    }
  }
}
