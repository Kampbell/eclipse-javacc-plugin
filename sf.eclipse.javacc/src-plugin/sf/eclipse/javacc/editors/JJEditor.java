package sf.eclipse.javacc.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.options.JJPreferences;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Editor designed for JavaCC files Referenced by plugin.xml <extension
 * point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003-2006 CeCILL Licence
 *         http://www.cecill.info/index.en.html
 */
public class JJEditor extends TextEditor implements IJJConstants, INavigationLocationProvider {
  protected JJOutlinePage outlinePage;
  protected JJReconcilingStrategy reconcilingStrategy;
  protected JJSourceViewerConfiguration jjSourceViewerConfiguration;
  
  /** The editor's peer Parent Matcher */
  ParentMatcher fParentMatcher = new ParentMatcher();
  Color colorMatchingChar;
  
  /** The editor's peer character painter */
  private MatchingCharacterPainter fMatchingCharacterPainter;
  
  /**
   * Constructor
   */
  public JJEditor() {
    super();
    // Offer the possibility to add contributions to context menu via
    // plugin.xml
    setEditorContextMenuId("sf.eclipse.javacc.editors.JJEditor"); //$NON-NLS-1$
  }
  
  /**
   * Initializes this editor. Method declared on AbstractTextEditor
   */
  protected void initializeEditor() {
    super.initializeEditor();
    
    // Generic Document provider
    setDocumentProvider(new FileDocumentProvider());
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    jjSourceViewerConfiguration = new JJSourceViewerConfiguration(this);
    setSourceViewerConfiguration(jjSourceViewerConfiguration);
    // Used to synchronize Outline and Editor
    reconcilingStrategy = new JJReconcilingStrategy(this);
    
    // Actions are declared in plugin.xml
  }
  
  /** 
   * Dispose of colors
   */
  public void dispose() {
    if (colorMatchingChar != null) {
      colorMatchingChar.dispose();
      colorMatchingChar= null;
    }    
    if (jjSourceViewerConfiguration != null) {
      jjSourceViewerConfiguration.dispose();
      jjSourceViewerConfiguration= null;
    }
    super.dispose();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
   */
  protected void initializeKeyBindingScopes() {
    setKeyBindingScopes(new String[] { "sf.eclipse.javacc.JJEditorScope" }); //$NON-NLS-1$
  }
  
  /**
   * Subclassed in order to add a Parent Painter to the SourceViewer. One
   * Need to first create PartControl to get a SourceViewer.
   */
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    showMatchingCharacters();
  }

  /**
   * Add a Painter to show matching characters.
   */
  private final void showMatchingCharacters() {
    if (fMatchingCharacterPainter == null) {
      if (getSourceViewer() instanceof ISourceViewerExtension2) {
        fMatchingCharacterPainter = new MatchingCharacterPainter(
            getSourceViewer(), fParentMatcher);
        Display display = Display.getCurrent();
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        colorMatchingChar = new Color(display, PreferenceConverter.getColor(
            store, JJPreferences.P_MATCHING_CHAR));
        fMatchingCharacterPainter.setColor(colorMatchingChar);
        ITextViewerExtension2 extension = (ITextViewerExtension2) getSourceViewer();
        extension.addPainter(fMatchingCharacterPainter);
      }
    }
  }

  /**
   * Returns ContentOutlinePage Method declared on IAdaptable
   */
  public Object getAdapter(Class key) {
    if (key.equals(IContentOutlinePage.class)) {
      if (outlinePage == null) {
        outlinePage = new JJOutlinePage(this);
        updateOutlinePage();
      }
      return outlinePage;
    }
    return super.getAdapter(key);
  }
  
  /**
   * Used by JJConfiguration.getReconciler()
   * 
   * @return JJReconcilingStrategy
   */
  public JJReconcilingStrategy getReconcilingStrategy() {
    return reconcilingStrategy;
  }
  
  /**
   * Called by JJReconcilingStrategy Takes the current Document and
   * setInput() on JJOutlinePage which leads to
   * JJContentProvider.setInput() which parse the document.
   */
  protected void updateOutlinePage() {
    if (outlinePage == null)
      outlinePage = (JJOutlinePage) getAdapter(IContentOutlinePage.class);
    outlinePage.setInput(getDocument());
    // get root node to build JJElement Hashmap
    JJContentProvider contentProvider = (JJContentProvider) outlinePage
    .getContentProvider();
    JJNode node = contentProvider.getAST();
    // If the outline is not up, then use the ContentProvider directly
    if (outlinePage.getControl() == null) {
      contentProvider.inputChanged(null, null, getDocument());
      node = contentProvider.getAST();
    }
    
    // Fill the JJElements HashMap
    node.buildHashMap();
  }
  
  /**
   * @return document
   */
  public IDocument getDocument() {
    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    return doc;
  }
  
  /**
   * Set the Selection given a Node of the AST
   * @param node
   */
  public void setSelection(JJNode node) {
    try {
      IDocument doc = getDocument();
      if (doc != null) {
        int start = doc.getLineOffset(node.getBeginLine() - 1);
        int end = doc.getLineOffset(node.getEndLine());
        if (start > end)
          end = start;
        int length = end - start;
        resetHighlightRange();
        setHighlightRange(start, length, true);
        markInNavigationHistory();
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      resetHighlightRange();
    } catch (BadLocationException e) {
      e.printStackTrace();
      resetHighlightRange();
    }
  }
  
  /**
   * Set the selection
   * 
   * @param IRegion
   *                (the highligth), ITextSelection (the selection)
   */
  public void setSelection(IRegion range, ITextSelection sel) {
    if (range != null)
      setHighlightRange(range.getOffset(), range.getLength(), true);
    else
      resetHighlightRange();
    if (sel != null)
      selectAndReveal(sel.getOffset(), sel.getLength());
  }

  public void updateColors() {
//    Display display = Display.getCurrent();
//    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//    Color color = new Color(display, PreferenceConverter.getColor(store, JJPreferences.P_JJKEYWORD));
//    getSourceViewer().getTextWidget().setBackground(color);
  }
}
