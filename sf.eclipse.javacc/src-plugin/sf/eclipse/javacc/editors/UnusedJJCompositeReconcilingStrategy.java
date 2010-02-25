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
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public class UnusedJJCompositeReconcilingStrategy extends JavaCompositeReconcilingStrategy {

  /** The current JJ editor */
  private final JJEditor              fJJEditor;
  /** The current JJ Reconciling Strategy */
  private final JJReconcilingStrategy fJJStrategy;

  /**
   * Creates a new Java reconciling strategy.
   * 
   * @param aViewer the source viewer
   * @param aEditor the editor of the strategy's reconciler
   * @param aDocumentPartitioning the document partitioning this strategy uses for configuration
   */
  public UnusedJJCompositeReconcilingStrategy(final ISourceViewer aViewer, final JJEditor aEditor,
                                        final String aDocumentPartitioning) {
    super(aViewer, aEditor, aDocumentPartitioning);
    fJJEditor = aEditor;
    fJJStrategy = new JJReconcilingStrategy(aViewer, aEditor);
    setReconcilingStrategies(new IReconcilingStrategy[] {
        fJJStrategy, new JavaSpellingReconcileStrategy(aViewer, aEditor) });
  }

  /**
   * @return the problem requestor for the editor's input element
   */
  private IProblemRequestorExtension getProblemRequestorExtension() {
    IDocumentProvider p = fJJEditor.getDocumentProvider();
    if (p == null) {
      // work around for https://bugs.eclipse.org/bugs/show_bug.cgi?id=51522
      p = JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
    }
    final IAnnotationModel m = p.getAnnotationModel(fJJEditor.getEditorInput());
    if (m instanceof IProblemRequestorExtension) {
      return (IProblemRequestorExtension) m;
    }
    return null;
  }

  /**
   * @see JavaCompositeReconcilingStrategy#reconcile(DirtyRegion, IRegion)
   */
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

  /**
   * @see JavaCompositeReconcilingStrategy#reconcile(IRegion)
   */
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
   * 
   * @param aNotify <code>true</code> if listeners should be notified
   */
  @Override
  public void notifyListeners(@SuppressWarnings("unused") final boolean aNotify) {
    //    fJJStrategy.notifyListeners(notify);
  }

  /**
   * @see JavaCompositeReconcilingStrategy#initialReconcile()
   */
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
   * 
   * @since 3.0
   */
  @Override
  public void aboutToBeReconciled() {
    //    fJJStrategy.aboutToBeReconciled();

  }

  /**
   * Called when reconcile has finished.
   * 
   * @since 3.4
   */
  private void reconciled() {
    //    fJJStrategy.reconciled();
  }
}
