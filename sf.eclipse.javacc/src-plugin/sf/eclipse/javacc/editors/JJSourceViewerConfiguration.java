package sf.eclipse.javacc.editors;

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
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.scanners.JJCodeScanner;
import sf.eclipse.javacc.scanners.JJCommentScanner;

/**
 * ViewerConfiguration for JavaCC code.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
@SuppressWarnings("restriction")
public class JJSourceViewerConfiguration extends TextSourceViewerConfiguration {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 12/2009 : added spell checking ; some renaming and scope changes ; extracted JJAnnotationHover ; removed fDoubleClickStrategy field ;
  // ... ....... . added overview ruler annotation hover
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed bug 2958124 on lost tabs
  // BF  05/2012 : added comment scanner processing and Javadoc content type
  // BF  05/2012 : added default content type deletion
  // BF  06/2012 : added hyperlink presenter override

  /** The current presentation reconciler */
  private PresentationReconciler jReconciler;

  /** The current editor */
  private final JJEditor         jJJEditor;

  /** The current JavaCC code scanner */
  private JJCodeScanner          jJJCodeScanner;

  /** The current line comment scanner */
  private JJCommentScanner       jJJLineCommentScanner;

  /** The current block comment scanner */
  private JJCommentScanner       jJJBlockCommentScanner;

  /** The current Javadoc scanner */
  private JJCommentScanner       jJJavadocScanner;

  /**
   * Standard constructor. Configures click, indentation, content assist, format.
   * 
   * @param aJJEditor - the current editor
   */
  public JJSourceViewerConfiguration(final JJEditor aJJEditor) {
    jJJEditor = aJJEditor;
  }

  /**
   * Disposes colors created by CodeScanner and CommentScanners.
   */
  public void dispose() {
    if (jJJCodeScanner != null) {
      jJJCodeScanner.dispose();
      jJJCodeScanner = null;
      jJJLineCommentScanner.dispose();
      jJJLineCommentScanner = null;
      jJJBlockCommentScanner.dispose();
      jJJBlockCommentScanner = null;
      jJJavadocScanner.dispose();
      jJJavadocScanner = null;
    }
  }

  /**
   * Returns a newly created auto indentation strategy.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IAutoEditStrategy[] getAutoEditStrategies(@SuppressWarnings("unused") final ISourceViewer aSourceViewer,
                                                   @SuppressWarnings("unused") final String aContentType) {
    final IAutoEditStrategy[] ret = {
      new JJAutoIndentStrategy() };
    return ret;
  }

  /**
   * Returns the JJ partitioning.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getConfiguredDocumentPartitioning(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return JJDocumentProvider.JJ_PARTITIONING_ID;
  }

  /**
   * Returns the JJ content types.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String[] getConfiguredContentTypes(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return JJDocumentProvider.JJ_CONTENT_TYPES;
  }

  /**
   * Creates and returns a IContentAssistant set with a {@link JJCompletionProcessor} for code partitions and
   * a {@link HippieProposalProcessor} for comment and string partitions.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IContentAssistant getContentAssistant(final ISourceViewer aSourceViewer) {
    final ContentAssistant assistant = new ContentAssistant();
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
    assistant.setContentAssistProcessor(new JJCompletionProcessor(), JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
    assistant.setContentAssistProcessor(new HippieProposalProcessor(),
                                        JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE);
    assistant.setContentAssistProcessor(new HippieProposalProcessor(),
                                        JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE);
    assistant.setContentAssistProcessor(new HippieProposalProcessor(),
                                        JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE);
    ContentAssistPreference.configure(assistant, Activator.getDefault().getPreferenceStore());
    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    assistant.setInformationControlCreator(new IInformationControlCreator() {

      @Override
      public IInformationControl createInformationControl(final Shell parent) {
        return new DefaultInformationControl(parent, JavaPlugin.getAdditionalInfoAffordanceString());
      }
    });

    return assistant;
  }

  /**
   * Creates and returns a JJDoubleClickStrategy.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public ITextDoubleClickStrategy getDoubleClickStrategy(@SuppressWarnings("unused") final ISourceViewer aSourceViewer,
                                                         @SuppressWarnings("unused") final String aContentType) {
    return new JJDoubleClickStrategy();
  }

  /**
   * Creates if necessary and returns a PresentationReconciler set with the JJ partitioning and the
   * JJCodeScanner.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IPresentationReconciler getPresentationReconciler(final ISourceViewer aSourceViewer) {
    if (jReconciler == null) {
      jReconciler = new PresentationReconciler();
      jReconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));

      jJJCodeScanner = new JJCodeScanner();
      final DefaultDamagerRepairer dr1 = new DefaultDamagerRepairer(jJJCodeScanner);
      jReconciler.setDamager(dr1, JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
      jReconciler.setRepairer(dr1, JJDocumentProvider.JJ_CODE_CONTENT_TYPE);

      jJJLineCommentScanner = new JJCommentScanner(JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE);
      final DefaultDamagerRepairer dr2 = new DefaultDamagerRepairer(jJJLineCommentScanner);
      jReconciler.setDamager(dr2, JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE);
      jReconciler.setRepairer(dr2, JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE);

      jJJBlockCommentScanner = new JJCommentScanner(JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE);
      final DefaultDamagerRepairer dr3 = new DefaultDamagerRepairer(jJJBlockCommentScanner);
      jReconciler.setDamager(dr3, JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE);
      jReconciler.setRepairer(dr3, JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE);

      jJJavadocScanner = new JJCommentScanner(JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE);
      final DefaultDamagerRepairer dr4 = new DefaultDamagerRepairer(jJJavadocScanner);
      jReconciler.setDamager(dr4, JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE);
      jReconciler.setRepairer(dr4, JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE);

      // Must provide damage/repair for the default content type, or remove the content type if none
      jReconciler.setDamager(null, IDocument.DEFAULT_CONTENT_TYPE);
      jReconciler.setRepairer(null, IDocument.DEFAULT_CONTENT_TYPE);
    }
    return jReconciler;
  }

  /**
   * Creates and returns a MonoReconciler set with a new JJReconcilingStrategy.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IReconciler getReconciler(final ISourceViewer aSourceViewer) {
    if (jJJEditor == null) {
      return null;
    }
    final JJReconcilingStrategy strategy = jJJEditor.getReconcilingStrategy();
    strategy.setSourceViewer(aSourceViewer);
    return new MonoReconciler(strategy, false);

  }

  /**
   * Creates and returns a new JJAnnotationHover for the vertical ruler.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IAnnotationHover getAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJAnnotationHover();
  }

  /**
   * Creates and returns a new JJAnnotationHover for the overview ruler.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IAnnotationHover getOverviewRulerAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJAnnotationHover();
  }

  /**
   * Creates and returns a new JJTextHover.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public ITextHover getTextHover(final ISourceViewer aSourceViewer, final String aContentType) {
    return new JJTextHover(aSourceViewer, aContentType, jJJEditor);
  }

  /**
   * Creates and returns the JJHyperlinkDetector used to detect hyperlinks.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJHyperlinkDetector[] {
      new JJHyperlinkDetector(jJJEditor) };
  }

  /**
   * Creates and returns the JJHyperlinkPresenter. {@link DefaultHyperlinkPresenter}.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IHyperlinkPresenter getHyperlinkPresenter(@SuppressWarnings("unused") final ISourceViewer sourceViewer) {
    return new JJHyperlinkPresenter(Activator.getDefault().getPreferenceStore());
  }

}
