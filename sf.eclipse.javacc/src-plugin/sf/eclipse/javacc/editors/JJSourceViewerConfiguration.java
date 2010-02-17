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
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

import sf.eclipse.javacc.Activator;

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
  //             added overview ruler annotation hover
  // MMa 02/2010 : formatting and javadoc revision

  /** The current presentation reconciler */
  private PresentationReconciler fReconciler;
  /** The current editor */
  private final JJEditor         fEditor;
  /** The current JavaCC code scanner */
  private JJCodeScanner          fJJCodeScanner;

  //  /** The current Java multi-line comment scanner */
  //  private AbstractJavaScanner    fMultilineCommentScanner;
  //  /** The current Java single-line comment scanner */
  //  private AbstractJavaScanner    fSinglelineCommentScanner;
  //  /** The current Java string scanner */
  //  private AbstractJavaScanner    fStringScanner;
  //  /** The current Javadoc scanner */
  //  private AbstractJavaScanner    fJavaDocScanner;
  //  /** The current color manager */
  //  private final IColorManager    fColorManager;

  /**
   * Standard constructor. Configures click, indentation, content assist, format.
   * 
   * @param aEditor the current editor
   */
  public JJSourceViewerConfiguration(final JJEditor aEditor) {
    fEditor = aEditor;
    fPreferenceStore = Activator.getDefault().getPreferenceStore();
    //    final Preferences coreStore = JavaCore.getPlugin().getPluginPreferences();
    //    fColorManager = new JavaTextTools(fPreferenceStore, coreStore).getColorManager();
    initializeScanners();
  }

  /**
   * Initializes the scanners.
   */
  private void initializeScanners() {
    fJJCodeScanner = new JJCodeScanner();
    //    fMultilineCommentScanner = new JavaCommentScanner(getColorManager(), fPreferenceStore,
    //                                                      IJavaColorConstants.JAVA_MULTI_LINE_COMMENT);
    //    fSinglelineCommentScanner = new JavaCommentScanner(getColorManager(), fPreferenceStore,
    //                                                       IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT);
    //    fStringScanner = new SingleTokenJavaScanner(getColorManager(), fPreferenceStore,
    //                                                IJavaColorConstants.JAVA_STRING);
    //    fJavaDocScanner = new JavaDocScanner(getColorManager(), fPreferenceStore);
  }

  /**
   * Disposes colors created by CodeScanner.
   */
  public void dispose() {
    if (fJJCodeScanner != null) {
      fJJCodeScanner.dispose();
      fJJCodeScanner = null;
    }
  }

  //  /**
  //   * Returns the editor in which the configured viewer(s) will reside.
  //   * 
  //   * @return the enclosing editor
  //   */
  //  protected JJEditor getEditor() {
  //    return fEditor;
  //  }
  //
  //  /**
  //   * Returns the JavaCC & Java source code scanner for this configuration.
  //   * 
  //   * @return the JavaCC & Java source code scanner
  //   */
  //  protected RuleBasedScanner getCodeScanner() {
  //    return fJJCodeScanner;
  //  }
  //
  //  /**
  //   * Returns the Java multi-line comment scanner for this configuration.
  //   * 
  //   * @return the Java multi-line comment scanner
  //   */
  //  protected RuleBasedScanner getMultilineCommentScanner() {
  //    return fMultilineCommentScanner;
  //  }
  //
  //  /**
  //   * Returns the Java single-line comment scanner for this configuration.
  //   * 
  //   * @return the Java single-line comment scanner
  //   */
  //  protected RuleBasedScanner getSinglelineCommentScanner() {
  //    return fSinglelineCommentScanner;
  //  }
  //
  //  /**
  //   * Returns the Java string scanner for this configuration.
  //   * 
  //   * @return the Java string scanner
  //   */
  //  protected RuleBasedScanner getStringScanner() {
  //    return fStringScanner;
  //  }
  //
  //  /**
  //   * Returns the JavaDoc scanner for this configuration.
  //   * 
  //   * @return the JavaDoc scanner
  //   */
  //  protected RuleBasedScanner getJavaDocScanner() {
  //    return fJavaDocScanner;
  //  }
  //
  //  /**
  //   * Returns the color manager for this configuration.
  //   * 
  //   * @return the color manager
  //   */
  //  protected IColorManager getColorManager() {
  //    return fColorManager;
  //  }

  /**
   * Returns a newly created auto indentation strategy.
   */
  @Override
  public IAutoEditStrategy[] getAutoEditStrategies(
                                                   @SuppressWarnings("unused") final ISourceViewer aSourceViewer,
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
  public String getConfiguredDocumentPartitioning(
                                                  @SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return JJDocumentProvider.JJ_PARTITIONING;
    //    return IJavaPartitions.JAVA_PARTITIONING;
  }

  /**
   * Returns the JJ content types.
   * 
   * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
   */
  @Override
  public String[] getConfiguredContentTypes(@SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return JJDocumentProvider.JJ_CONTENT_TYPES;
    //    return getConfiguredContentTypes();
  }

  //  /**
  //   * @return the array of configured content types
  //   */
  //  public static String[] getConfiguredContentTypes() {
  //    return new String[] {
  //        IDocument.DEFAULT_CONTENT_TYPE, IJavaPartitions.JAVA_DOC, IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
  //        IJavaPartitions.JAVA_SINGLE_LINE_COMMENT, IJavaPartitions.JAVA_STRING, IJavaPartitions.JAVA_CHARACTER };
  //  }

  /**
   * Creates and returns a IContentAssistant set with a JJCompletionProcessor for each JJ partition.
   * 
   * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
   */
  @Override
  public IContentAssistant getContentAssistant(final ISourceViewer aSourceViewer) {
    final ContentAssistant assistant = new ContentAssistant();
    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
    assistant.setContentAssistProcessor(new JJCompletionProcessor(), JJDocumentProvider.JJ_CODE);

    assistant.setContentAssistProcessor(new HippieProposalProcessor(), JJDocumentProvider.JJ_COMMENT);
    //        assistant.setContentAssistProcessor(new JavaCompletionProcessor(fEditor, assistant,
    //                                                                        JJDocumentProvider.JJ_COMMENT),
    //                                            JJDocumentProvider.JJ_COMMENT);

    //     Should we place this in Preferences ?
    //     assistant.enableAutoActivation(store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION));
    //     assistant.setAutoActivationDelay(500); 

    //    assistant.install(sourceViewer);
    //    return assistant;
    ContentAssistPreference.configure(assistant, fPreferenceStore);

    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    assistant.setInformationControlCreator(new IInformationControlCreator() {

      public IInformationControl createInformationControl(final Shell parent) {
        return new DefaultInformationControl(parent, JavaPlugin.getAdditionalInfoAffordanceString());
      }
    });

    return assistant;

    //    final ContentAssistant assistant = new ContentAssistant();
    //    assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
    //
    //    assistant.setRestoreCompletionProposalSize(getSettings("completion_proposal_size")); 
    //
    //    final IContentAssistProcessor jjProcessor = new JJCompletionProcessor();
    //    assistant.setContentAssistProcessor(jjProcessor, IDocument.DEFAULT_CONTENT_TYPE);
    //
    //    final ContentAssistProcessor singleLineProcessor = new JavaCompletionProcessor(
    //                                                                                   getEditor(),
    //                                                                                   assistant,
    //                                                                                   IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
    //    assistant.setContentAssistProcessor(singleLineProcessor, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
    //
    //    final ContentAssistProcessor stringProcessor = new JavaCompletionProcessor(getEditor(), assistant,
    //                                                                               IJavaPartitions.JAVA_STRING);
    //    assistant.setContentAssistProcessor(stringProcessor, IJavaPartitions.JAVA_STRING);
    //
    //    final ContentAssistProcessor multiLineProcessor = new JavaCompletionProcessor(
    //                                                                                  getEditor(),
    //                                                                                  assistant,
    //                                                                                  IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
    //    assistant.setContentAssistProcessor(multiLineProcessor, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
    //
    //    final ContentAssistProcessor javadocProcessor = new JavadocCompletionProcessor(getEditor(), assistant);
    //    assistant.setContentAssistProcessor(javadocProcessor, IJavaPartitions.JAVA_DOC);
    //
    //    ContentAssistPreference.configure(assistant, fPreferenceStore);
    //
    //    assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    //    assistant.setInformationControlCreator(new IInformationControlCreator() {
    //
    //      public IInformationControl createInformationControl(final Shell parent) {
    //        return new DefaultInformationControl(parent, JavaPlugin.getAdditionalInfoAffordanceString());
    //      }
    //    });
    //
    //    return assistant;
  }

  /**
   * Creates and returns a JJDoubleClickStrategy.
   * 
   * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
   */
  @Override
  public ITextDoubleClickStrategy getDoubleClickStrategy(
                                                         @SuppressWarnings("unused") final ISourceViewer aSourceViewer,
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
    if (fReconciler == null) {
      fReconciler = new PresentationReconciler();
      fReconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(aSourceViewer));
      //       JJCodeScanner is used for all partitions
      fJJCodeScanner = new JJCodeScanner();
      final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(fJJCodeScanner);
      fReconciler.setDamager(dr, JJDocumentProvider.JJ_CODE);
      fReconciler.setRepairer(dr, JJDocumentProvider.JJ_CODE);
      fReconciler.setDamager(dr, JJDocumentProvider.JJ_COMMENT);
      fReconciler.setRepairer(dr, JJDocumentProvider.JJ_COMMENT);

      //      final PresentationReconciler reconciler = new JavaPresentationReconciler();
      //      reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
      //
      //      DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
      //      reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
      //      reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
      //
      //      dr = new DefaultDamagerRepairer(getJavaDocScanner());
      //      reconciler.setDamager(dr, IJavaPartitions.JAVA_DOC);
      //      reconciler.setRepairer(dr, IJavaPartitions.JAVA_DOC);
      //
      //      dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
      //      reconciler.setDamager(dr, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
      //      reconciler.setRepairer(dr, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
      //
      //      dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
      //      reconciler.setDamager(dr, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
      //      reconciler.setRepairer(dr, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
      //
      //      dr = new DefaultDamagerRepairer(getStringScanner());
      //      reconciler.setDamager(dr, IJavaPartitions.JAVA_STRING);
      //      reconciler.setRepairer(dr, IJavaPartitions.JAVA_STRING);
      //
      //      dr = new DefaultDamagerRepairer(getStringScanner());
      //      reconciler.setDamager(dr, IJavaPartitions.JAVA_CHARACTER);
      //      reconciler.setRepairer(dr, IJavaPartitions.JAVA_CHARACTER);

    }
    return fReconciler;
  }

  /**
   * Creates and returns a MonoReconciler set with a new JJReconcilingStrategy.
   * 
   * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
   */
  @Override
  public IReconciler getReconciler(final ISourceViewer aSourceViewer) {
    final IReconcilingStrategy strategy = new JJReconcilingStrategy(aSourceViewer, fEditor);
    return new MonoReconciler(strategy, false);

    //    final JJEditor editor = getEditor();
    //    if (editor != null && editor.isEditable()) {
    //
    //      final JJCompositeReconcilingStrategy strategy = new JJCompositeReconcilingStrategy(
    //                                                                                         sourceViewer,
    //                                                                                         editor,
    //                                                                                         getConfiguredDocumentPartitioning(sourceViewer));
    //      final JavaReconciler reconciler = new JavaReconciler(editor, strategy, false);
    //      reconciler.setIsAllowedToModifyDocument(false);
    //      reconciler.setDelay(500);
    //
    //      return reconciler;
    //    }
    //    return null;
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
  public IAnnotationHover getOverviewRulerAnnotationHover(
                                                          @SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJAnnotationHover();
  }

  /**
   * Creates and returns a new JJTextHover.
   * 
   * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
   */
  @Override
  public ITextHover getTextHover(final ISourceViewer aSourceViewer, final String aContentType) {
    return new JJTextHover(aSourceViewer, aContentType, fEditor);
  }

  /**
   * Creates and returns the JJHyperlinkDetector used to detect hyperlinks.
   * 
   * @see SourceViewerConfiguration#getHyperlinkDetectors(ISourceViewer)
   */
  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(
                                                    @SuppressWarnings("unused") final ISourceViewer aSourceViewer) {
    return new JJHyperlinkDetector[] {
      new JJHyperlinkDetector(fEditor) };
  }

  //  /**
  //   * Returns the settings for the given section.
  //   * 
  //   * @param sectionName the section name
  //   * @return the settings
  //   */
  //  private IDialogSettings getSettings(final String sectionName) {
  //    IDialogSettings settings = JavaPlugin.getDefault().getDialogSettings().getSection(sectionName);
  //    if (settings == null) {
  //      settings = JavaPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
  //    }
  //
  //    return settings;
  //  }
  //
  //  /**
  //   * Determines whether the preference change encoded by the given event changes the behavior of one of its
  //   * contained components.
  //   * 
  //   * @param event the event to be investigated
  //   * @return <code>true</code> if event causes a behavioral change
  //   * @since 3.0
  //   */
  //  public boolean affectsTextPresentation(final PropertyChangeEvent event) {
  //    return /*fJJCodeScanner.affectsBehavior(event) ||*/
  //    fMultilineCommentScanner.affectsBehavior(event) || fSinglelineCommentScanner.affectsBehavior(event)
  //        || fStringScanner.affectsBehavior(event) || fJavaDocScanner.affectsBehavior(event);
  //  }
  //
  //  /**
  //   * Adapts the behavior of the contained components to the change encoded in the given event.
  //   * <p>
  //   * Clients are not allowed to call this method if the old setup with text tools is in use.
  //   * </p>
  //   * 
  //   * @param event the event to which to adapt
  //   * @see JavaSourceViewerConfiguration#JavaSourceViewerConfiguration(IColorManager, IPreferenceStore,
  //   *      ITextEditor, String)
  //   * @since 3.0
  //   */
  //  public void handlePropertyChangeEvent(final PropertyChangeEvent event) {
  //    //    if (fJJCodeScanner.affectsBehavior(event)) {
  //    //      fJJCodeScanner.adaptToPreferenceChange(event);
  //    //    }
  //    if (fMultilineCommentScanner.affectsBehavior(event)) {
  //      fMultilineCommentScanner.adaptToPreferenceChange(event);
  //    }
  //    if (fSinglelineCommentScanner.affectsBehavior(event)) {
  //      fSinglelineCommentScanner.adaptToPreferenceChange(event);
  //    }
  //    if (fStringScanner.affectsBehavior(event)) {
  //      fStringScanner.adaptToPreferenceChange(event);
  //    }
  //    if (fJavaDocScanner.affectsBehavior(event)) {
  //      fJavaDocScanner.adaptToPreferenceChange(event);
  //      //    if (fJavaDoubleClickSelector != null && JavaCore.COMPILER_SOURCE.equals(event.getProperty()))
  //      //      if (event.getNewValue() instanceof String)
  //      //        fJavaDoubleClickSelector.setSourceVersion((String) event.getNewValue());
  //    }
  //  }

}
