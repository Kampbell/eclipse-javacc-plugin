package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


/**
 * Configuration for JavaCC code.
 * 
 * NB. Oddity, The IAutoIndentStrategy is deprecated,
 * but the SourceViewerConfiguration class needs this interface. 
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJSourceViewerConfiguration extends SourceViewerConfiguration {
  protected PresentationReconciler reconciler = null;
  protected JJEditor editor;
  public static JJDoubleClickStrategy doubleClickStrategy;

  /**
   * Configuration ie clic, ident, content assist, format
   */
  public JJSourceViewerConfiguration(JJEditor editor) {
    this.editor = editor;
  }

  /**
   * Returns the auto indentation strategy
   * To be replaced by IAutoEditStrategy;
   * when SourceViewerConfiguration method signature is updated
   */
  public IAutoIndentStrategy getAutoIndentStrategy(
    ISourceViewer isv,
    String ctype) {
    return (new JJAutoIndentStrategy());
  }

  /**
   * Returns all configured content types
   */
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
  }

  /**
   * Method declared on SourceViewerConfiguration
   */
  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    ContentAssistant assistant = new ContentAssistant();
    //		assistant.setContentAssistProcessor(new JJCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
    //		assistant.enableAutoActivation(true);
    //		assistant.setAutoActivationDelay(500);
    //		assistant.setProposalPopupOrientation(assistant.PROPOSAL_OVERLAY);
    //		assistant.setContextInformationPopupOrientation(assistant.CONTEXT_INFO_ABOVE);
    //		assistant.setContextInformationPopupBackground(JJEditorEnvironment.getColorManager().getColor(new RGB(150, 150, 0)));
    //
    return assistant;
  }

  /**
   * Method declared on SourceViewerConfiguration
   */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
    ISourceViewer sourceViewer,
    String contentType) {
    doubleClickStrategy = new JJDoubleClickStrategy();
    return doubleClickStrategy;
  }

  /**
   * Returns the presentation reconciler.
   */
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    if (reconciler != null)
      return reconciler;
    reconciler = new PresentationReconciler();

    // JJCodeScanner is used for all sections
    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new JJCodeScanner());
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    return reconciler;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentFormatter(ISourceViewer)
   */
  public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
    ContentFormatter formatter = new ContentFormatter();
    //    IFormattingStrategy strategy= new JavaFormattingStrategy(sourceViewer);
    //    
    //    formatter.setFormattingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
    //    formatter.enablePartitionAwareFormatting(false);    
    //    formatter.setPartitionManagingPositionCategories(fJavaTextTools.getPartitionManagingPositionCategories());
    return formatter;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
   */
  public IReconciler getReconciler(ISourceViewer sourceViewer) {
    IReconciler iRreconciler;
    if (editor != null) {
    	iRreconciler =
        new MonoReconciler(editor.getReconcilingStrategy(), false);
    } else {
    	iRreconciler = null;
    }
    return iRreconciler;
  }
  /**
   * @return
   */
  public static JJDoubleClickStrategy getDoubleClickStrategy() {
    return doubleClickStrategy;
  }

}
