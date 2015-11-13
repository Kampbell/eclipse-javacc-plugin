package sf.eclipse.javacc.editors;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.JavaCompositeReconcilingStrategy;
import org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension;
import org.eclipse.jdt.internal.ui.text.spelling.JavaSpellingReconcileStrategy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * The JJ Composite Reconciling strategy.
 * 
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
@SuppressWarnings("restriction")
public class UnusedCompositeReconcilingStrategy extends JavaCompositeReconcilingStrategy {

  // MMa 10/2012 : renamed

  /** The current JJ editor */
  protected final JJEditor            jEditor;
  /** The current Reconciling Strategy */
  protected final ReconcilingStrategy jReconStrategy;

  /**
   * Creates a new Java reconciling strategy.
   * 
   * @param aSourceViewer - the source viewer
   * @param aJJEditor - the editor of the strategy's reconciler
   * @param aDocumentPartitioning - the document partitioning this strategy uses for configuration
   */
  public UnusedCompositeReconcilingStrategy(final ISourceViewer aSourceViewer, final JJEditor aJJEditor,
                                            final String aDocumentPartitioning) {
    super(aSourceViewer, aJJEditor, aDocumentPartitioning);
    jEditor = aJJEditor;
    jReconStrategy = new ReconcilingStrategy(aSourceViewer, aJJEditor);
    setReconcilingStrategies(new IReconcilingStrategy[] {
        jReconStrategy, new JavaSpellingReconcileStrategy(aSourceViewer, aJJEditor) });
  }

  /**
   * @return the problem requester for the editor's input element
   */
  private IProblemRequestorExtension getProblemRequestorExtension() {
    IDocumentProvider p = jEditor.getDocumentProvider();
    if (p == null) {
      // work around for https://bugs.eclipse.org/bugs/show_bug.cgi?id=51522
      p = JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
    }
    final IAnnotationModel m = p.getAnnotationModel(jEditor.getEditorInput());
    if (m instanceof IProblemRequestorExtension) {
      return (IProblemRequestorExtension) m;
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void reconcile(final DirtyRegion aDirtyRegion, final IRegion aSubRegion) {
    try {
      final IProblemRequestorExtension e = getProblemRequestorExtension();
      if (e != null) {
        try {
          e.beginReportingSequence();
          super.reconcile(aDirtyRegion, aSubRegion);
        } finally {
          e.endReportingSequence();
        }
      }
      else {
        super.reconcile(aDirtyRegion, aSubRegion);
      }
    } finally {
      reconciled();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void reconcile(final IRegion aPartition) {
    try {
      final IProblemRequestorExtension e = getProblemRequestorExtension();
      if (e != null) {
        try {
          e.beginReportingSequence();
          super.reconcile(aPartition);
        } finally {
          e.endReportingSequence();
        }
      }
      else {
        super.reconcile(aPartition);
      }
    } finally {
      reconciled();
    }
  }

  /**
   * Tells this strategy whether to inform its listeners.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void notifyListeners(@SuppressWarnings("unused") final boolean aNotify) {
    //    jStrategy.notifyListeners(notify);
  }

  /** {@inheritDoc} */
  @Override
  public void initialReconcile() {
    try {
      final IProblemRequestorExtension e = getProblemRequestorExtension();
      if (e != null) {
        try {
          e.beginReportingSequence();
          super.initialReconcile();
        } finally {
          e.endReportingSequence();
        }
      }
      else {
        super.initialReconcile();
      }
    } finally {
      reconciled();
    }
  }

  /**
   * Called before reconciling is started.
   * <p>
   * {@inheritDoc}
   * 
   * @since 3.0
   */
  @Override
  public void aboutToBeReconciled() {
    //    jStrategy.aboutToBeReconciled();

  }

  /**
   * Called when reconcile has finished.
   * 
   * @since 3.4
   */
  private void reconciled() {
    //    jStrategy.reconciled();
  }
}
