package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.*;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.scanners.CodeScanner;
import sf.eclipse.javacc.scanners.CommentScanner;

/**
 * Viewer configuration for JavaCC code.<br>
 * All get methods are called by
 * {@link SourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
@SuppressWarnings("restriction")
class JSourceViewerConfiguration extends TextSourceViewerConfiguration {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 12/2009 : added spell checking ; some renaming and scope changes ; extracted JJAnnotationHover ; removed fDoubleClickStrategy field ;
  // ... ....... . added overview ruler annotation hover
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed bug 2958124 on lost tabs
  // BF  05/2012 : added comment scanner processing and Javadoc content type
  // BF  05/2012 : added default content type deletion
  // BF  06/2012 : added hyperlink presenter override
  // MMa 10/2012 : renamed ; removed PresentationReconciler member
  // MMa 11/2014 : modified some modifiers ; renamed
  // MMa 12/2014 : added completion processor for default content type

  /** The editor */
  private final JJEditor jEditor;

  /** The JavaCC code scanner */
  private CodeScanner    jCodeScanner;

  /** The line comment scanner */
  private CommentScanner jLineCmtScanner;

  /** The block comment scanner */
  private CommentScanner jBlockCmtScanner;

  /** The Javadoc scanner */
  private CommentScanner jJavadocCmtScanner;

  /**
   * Constructor.
   * 
   * @param aJJEditor - the current editor
   */
  public JSourceViewerConfiguration(final JJEditor aJJEditor) {
    jEditor = aJJEditor;
  }

  /**
   * Disposes colors created by CodeScanner and CommentScanners.
   */
  public void dispose() {
    if (jCodeScanner != null) {
      jCodeScanner.dispose();
      jCodeScanner = null;
      jLineCmtScanner.dispose();
      jLineCmtScanner = null;
      jBlockCmtScanner.dispose();
      jBlockCmtScanner = null;
      jJavadocCmtScanner.dispose();
      jJavadocCmtScanner = null;
    }
  }

  /**
   * Creates and returns a {@link AutoIndentStrategy}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final IAutoEditStrategy[] getAutoEditStrategies(@SuppressWarnings("unused") final ISourceViewer aSourceViewer,
                                                         @SuppressWarnings("unused") final String aContentType) {
    return new IAutoEditStrategy[] {
      new AutoIndentStrategy() };
  }

  /**
   * Returns the JJ partitioning id.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final String getConfiguredDocumentPartitioning(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return PARTITIONING_ID;
  }

  /**
   * Returns the JJ content types.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final String[] getConfiguredContentTypes(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return CONTENT_TYPES;
  }

  /**
   * Creates and returns a {@link ContentAssistant} set with a {@link CompletionProcessor} for code partitions
   * and a {@link HippieProposalProcessor} for comment and string partitions.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IContentAssistant getContentAssistant(final ISourceViewer aSourceViewer) {
    final ContentAssistant ca = new ContentAssistant();
    ca.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
    ca.setContentAssistProcessor(new CompletionProcessor(), CODE_CONTENT_TYPE);
    ca.setContentAssistProcessor(new HippieProposalProcessor(), LINE_CMT_CONTENT_TYPE);
    ca.setContentAssistProcessor(new HippieProposalProcessor(), BLOCK_CMT_CONTENT_TYPE);
    ca.setContentAssistProcessor(new HippieProposalProcessor(), JAVADOC_CONTENT_TYPE);
    ca.setContentAssistProcessor(new CompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
    ContentAssistPreference.configure(ca, AbstractActivator.getDefault().getPreferenceStore());
    ca.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    ca.setInformationControlCreator(new IInformationControlCreator() {

      /** {@inheritDoc} */
      @Override
      public IInformationControl createInformationControl(final Shell parent) {
        return new DefaultInformationControl(parent, JavaPlugin.getAdditionalInfoAffordanceString());
      }
    });

    return ca;
  }

  /**
   * Creates and returns a {@link DoubleClickStrategy}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final ITextDoubleClickStrategy getDoubleClickStrategy(@SuppressWarnings("unused") final ISourceViewer aSourceViewer,
                                                               @SuppressWarnings("unused") final String aContentType) {
    return new DoubleClickStrategy();
  }

  /**
   * Creates and returns a {@link PresentationReconciler} set with the JJ partitioning and different
   * {@link CodeScanner} for the different content types.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IPresentationReconciler getPresentationReconciler(final ISourceViewer aSourceViewer) {
    PresentationReconciler jReconciler;
    jReconciler = new PresentationReconciler();
    jReconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));

    jCodeScanner = new CodeScanner();
    final DefaultDamagerRepairer dr1 = new DefaultDamagerRepairer(jCodeScanner);
    jReconciler.setDamager(dr1, CODE_CONTENT_TYPE);
    jReconciler.setRepairer(dr1, CODE_CONTENT_TYPE);

    jLineCmtScanner = new CommentScanner(LINE_CMT_CONTENT_TYPE);
    final DefaultDamagerRepairer dr2 = new DefaultDamagerRepairer(jLineCmtScanner);
    jReconciler.setDamager(dr2, LINE_CMT_CONTENT_TYPE);
    jReconciler.setRepairer(dr2, LINE_CMT_CONTENT_TYPE);

    jBlockCmtScanner = new CommentScanner(BLOCK_CMT_CONTENT_TYPE);
    final DefaultDamagerRepairer dr3 = new DefaultDamagerRepairer(jBlockCmtScanner);
    jReconciler.setDamager(dr3, BLOCK_CMT_CONTENT_TYPE);
    jReconciler.setRepairer(dr3, BLOCK_CMT_CONTENT_TYPE);

    jJavadocCmtScanner = new CommentScanner(JAVADOC_CONTENT_TYPE);
    final DefaultDamagerRepairer dr4 = new DefaultDamagerRepairer(jJavadocCmtScanner);
    jReconciler.setDamager(dr4, JAVADOC_CONTENT_TYPE);
    jReconciler.setRepairer(dr4, JAVADOC_CONTENT_TYPE);

    // Must provide damage/repair for the default content type, or remove the content type if none
    jReconciler.setDamager(null, IDocument.DEFAULT_CONTENT_TYPE);
    jReconciler.setRepairer(null, IDocument.DEFAULT_CONTENT_TYPE);
    return jReconciler;
  }

  /**
   * Creates and returns a new {@link MonoReconciler} set with a new {@link ReconcilingStrategy}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IReconciler getReconciler(final ISourceViewer aSourceViewer) {
    Assert.isNotNull(jEditor);
    //    if (jEditor == null) {
    //      return null;
    //    }
    final ReconcilingStrategy strategy = jEditor.getReconcilingStrategy();
    strategy.setSourceViewer(aSourceViewer);
    return new MonoReconciler(strategy, false);

  }

  /**
   * Creates and returns a new {@link AnnotationHover} for the vertical ruler.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final IAnnotationHover getAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new AnnotationHover();
  }

  /**
   * Creates and returns a new {@link AnnotationHover} for the overview ruler.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final IAnnotationHover getOverviewRulerAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new AnnotationHover();
  }

  /**
   * Creates and returns a new {@link TextHover}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final ITextHover getTextHover(final ISourceViewer aSourceViewer, final String aContentType) {
    return new TextHover(aSourceViewer, aContentType, jEditor);
  }

  /**
   * Creates and returns a new {@link HyperlinkDetector} used to detect hyperlinks.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final IHyperlinkDetector[] getHyperlinkDetectors(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new HyperlinkDetector[] {
      new HyperlinkDetector(jEditor) };
  }

  /**
   * Creates and returns a new {@link HyperlinkPresenter}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public final IHyperlinkPresenter getHyperlinkPresenter(@SuppressWarnings("unused") final ISourceViewer sourceViewer) {
    return new HyperlinkPresenter(AbstractActivator.getDefault().getPreferenceStore());
  }

}
