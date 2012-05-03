package sf.eclipse.javacc.editors;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

import sf.eclipse.javacc.head.Activator;

/**
 * ViewerConfiguration for JavaCC code.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public class JJSourceViewerConfiguration extends TextSourceViewerConfiguration {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 12/2009 : added spell checking ; some renaming and scope changes ; extracted JJAnnotationHover ; removed fDoubleClickStrategy field ;
  // ... ....... . added overview ruler annotation hover
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed bug 2958124 on lost tabs

  /** The current presentation reconciler */
  private PresentationReconciler jReconciler;
  /** The current editor */
  private final JJEditor         jJJEditor;
  /** The current JavaCC code scanner */
  private JJCodeScanner          jJJCodeScanner;

  /**
   * Standard constructor. Configures click, indentation, content assist, format.
   * 
   * @param aJJEditor the current editor
   */
  public JJSourceViewerConfiguration(final JJEditor aJJEditor) {
    jJJEditor = aJJEditor;
  }

  /**
   * Disposes colors created by CodeScanner.
   */
  public void dispose() {
    if (jJJCodeScanner != null) {
      jJJCodeScanner.dispose();
      jJJCodeScanner = null;
    }
  }

  /**
   * Returns a newly created auto indentation strategy.
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
   * 
   * @see SourceViewerConfiguration#getConfiguredDocumentPartitioning(ISourceViewer)
   */
  @Override
  public String getConfiguredDocumentPartitioning(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return JJDocumentProvider.JJ_PARTITIONING_ID;
  }

  /**
   * Returns the JJ content types.
   * 
   * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
   */
  @Override
  public String[] getConfiguredContentTypes(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    //    return new String[] {
    //      IDocument.DEFAULT_CONTENT_TYPE };
    return JJDocumentProvider.JJ_CONTENT_TYPES;
  }

  /**
   * Creates and returns a IContentAssistant set with a {@link JJCompletionProcessor} for code partitions and
   * a {@link HippieProposalProcessor} for comment and string partitions.
   * 
   * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
   */
  @Override
  public IContentAssistant getContentAssistant(final ISourceViewer aSourceViewer) {
    // old code before adding second completion processor
    //    final ContentAssistant assistant = new ContentAssistant();
    //    assistant.setContentAssistProcessor(new JJCompletionProcessor(), JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
    //    // Should we place this in Preferences ?
    //    // assistant.enableAutoActivation(store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION));
    //    // assistant.setAutoActivationDelay(500); 
    //    assistant.install(aSourceViewer);
    //    return assistant;

    final ContentAssistant assistant = new ContentAssistant();
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
    assistant.setContentAssistProcessor(new JJCompletionProcessor(), JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
    assistant.setContentAssistProcessor(new HippieProposalProcessor(),
                                        JJDocumentProvider.JJ_COMMENT_CONTENT_TYPE);
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
   * 
   * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
   */
  @Override
  public ITextDoubleClickStrategy getDoubleClickStrategy(@SuppressWarnings("unused") final ISourceViewer aSourceViewer,
                                                         @SuppressWarnings("unused") final String aContentType) {
    return new JJDoubleClickStrategy();
  }

  /**
   * Creates if necessary and returns a PresentationReconciler set with the JJ partitioning and the
   * JJCodeScanner.
   * 
   * @see SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
   */
  @Override
  public IPresentationReconciler getPresentationReconciler(final ISourceViewer aSourceViewer) {
    if (jReconciler == null) {
      jReconciler = new PresentationReconciler();
      jReconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
      // JJCodeScanner is used for all partitions
      jJJCodeScanner = new JJCodeScanner();
      final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(jJJCodeScanner);
      //      fReconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
      //      fReconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
      jReconciler.setDamager(dr, JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
      jReconciler.setRepairer(dr, JJDocumentProvider.JJ_CODE_CONTENT_TYPE);
      jReconciler.setDamager(dr, JJDocumentProvider.JJ_COMMENT_CONTENT_TYPE);
      jReconciler.setRepairer(dr, JJDocumentProvider.JJ_COMMENT_CONTENT_TYPE);

    }
    return jReconciler;
  }

  /**
   * Creates and returns a MonoReconciler set with a new JJReconcilingStrategy.
   * 
   * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
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
   * 
   * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
   */
  @Override
  public IAnnotationHover getAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJAnnotationHover();
  }

  /**
   * Creates and returns a new JJAnnotationHover for the overview ruler.
   * 
   * @see SourceViewerConfiguration#getOverviewRulerAnnotationHover(ISourceViewer)
   */
  @Override
  public IAnnotationHover getOverviewRulerAnnotationHover(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJAnnotationHover();
  }

  /**
   * Creates and returns a new JJTextHover.
   * 
   * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
   */
  @Override
  public ITextHover getTextHover(final ISourceViewer aSourceViewer, final String aContentType) {
    return new JJTextHover(aSourceViewer, aContentType, jJJEditor);
  }

  /**
   * Creates and returns the JJHyperlinkDetector used to detect hyperlinks.
   * 
   * @see SourceViewerConfiguration#getHyperlinkDetectors(ISourceViewer)
   */
  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJHyperlinkDetector[] {
      new JJHyperlinkDetector(jJJEditor) };
  }

}
