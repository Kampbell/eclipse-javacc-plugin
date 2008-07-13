package sf.eclipse.javacc.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
//import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * ViewerConfiguration for JavaCC code.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJSourceViewerConfiguration extends TextSourceViewerConfiguration {
  protected PresentationReconciler reconciler = null;
  protected JJEditor editor;
  protected JJCodeScanner codeScanner;
  public static JJDoubleClickStrategy doubleClickStrategy;

  /**
   * Configuration ie clic, ident, content assist, format
   */
  public JJSourceViewerConfiguration(JJEditor editor) {
    this.editor = editor;
  }
  
  /** 
   * Dispose of Colors created by CodeScanner
   */
  public void dispose(){
    if (codeScanner != null) {
      codeScanner.dispose();
      codeScanner = null;
    }
  }

  /**
   * Returns the auto indentation strategy
   */
  public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer isv, String ctype) {
    IAutoEditStrategy[] ret = {new JJAutoIndentStrategy()};
    return (ret);
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
    assistant.setContentAssistProcessor(new JJCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
// Should we place this in Preferences ?
// assistant.enableAutoActivation(store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION));
// assistant.setAutoActivationDelay(500); 
    assistant.install(sourceViewer);
    return assistant;
  }

  /**
   * Method declared on SourceViewerConfiguration
   */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
      ISourceViewer sourceViewer, String contentType) {
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
    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

    // JJCodeScanner is used for all sections
    codeScanner = new JJCodeScanner();
    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(codeScanner);
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    return reconciler;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
   */
  public IReconciler getReconciler(ISourceViewer sourceViewer) {
    IReconciler iRreconciler;
    if (editor != null) {
    	iRreconciler = new MonoReconciler(editor.getReconcilingStrategy(), false);
    } else {
    	iRreconciler = null;
    }
    return iRreconciler;
  }
  
  /**
   * Annotation hover for error and warning markers 
   */
  static class JJHover implements IAnnotationHover {
    /*
     * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
      String text = null;
      IAnnotationModel model = sourceViewer.getAnnotationModel();
      Iterator<?> iter = model.getAnnotationIterator();
      while (iter.hasNext()) {
        Object obj = iter.next();
        // test necessary not to cast DiffRegion objects
        if (obj instanceof MarkerAnnotation) {
          MarkerAnnotation annotation = (MarkerAnnotation) obj;
          IMarker marker = annotation.getMarker();
          try {
            Integer line  = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
            if (line.intValue() == lineNumber + 1) { // different offsets
              text = annotation.getText();
              break;
            }
          } catch (CoreException e) {
            e.printStackTrace();
          }
        }
      }
      return text;
    }
  }

  /*
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
   */
  public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
    return new JJHover();
  }

  /*
   * @see org.eclipse.jface.text.source.TextSourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer)
   */
  public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
    return new JJTextHover(editor); // editor.getSourceViewer()
  }
   
  /**
   * Returns the hyperlink detectors which be used to detect hyperlinks
   * actually only one detector is returned
   */
  public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
    return new JJHyperlinkDetector[] {new JJHyperlinkDetector(editor)};
  }
}
