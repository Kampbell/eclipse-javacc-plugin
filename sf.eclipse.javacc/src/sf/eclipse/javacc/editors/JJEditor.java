package sf.eclipse.javacc.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import sf.eclipse.javacc.IConstants;
import sf.eclipse.javacc.parser.SimpleNode;

/**
 * Editor designed for JavaCC files
 * Referenced in plugin.xml <extension point="org.eclipse.ui.editors">
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJEditor extends TextEditor implements IConstants { 
  protected JJOutlinePage page;
  protected JJReconcilingStrategy reconcilingStrategy;

  /**
   * Constructor
   */
  public JJEditor() {
    super();
  }
  
  /**
   * Initializes this editor.
   * Method declared on AbstractTextEditor
   */
  protected void initializeEditor() { 
    super.initializeEditor();
       
    // Generic Document provider
    setDocumentProvider(new FileDocumentProvider());    
    // JJ CodeScanner, Formatter, IndentStrategy, ContentAssist,...
    setSourceViewerConfiguration(new JJSourceViewerConfiguration(this));
    // Used to synchronize Outline and Editor
    reconcilingStrategy = new JJReconcilingStrategy(this);

    // A contributorClass for Actions is declared in plugin.xml
    // the actions are defined in JJEditorActions;
    
  }
  
  /** 
   * Returns ContentOutlinePage
   * Method declared on IAdaptable
   */
  public Object getAdapter(Class key) {
    if (key.equals(IContentOutlinePage.class)) {
      IDocument doc = getDocument();
      page = new JJOutlinePage(this);
      //page.setInput(doc);
      return page;
    }
    return super.getAdapter(key);
  }
  
  /**
   * Used by JJConfiguration.getReconciler()
   * @return JJReconcilingStrategy
   */
  public JJReconcilingStrategy getReconcilingStrategy() {
    return reconcilingStrategy;
  }

  /**
   * Adds Actions to context menu
   * Actions are declared in JJEditorActions
   * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
   */
  protected void editorContextMenuAboutToShow(IMenuManager parentMenu) {
    IAction action = JJEditorActions.gotoRuleAction;
    parentMenu.add(action);
    
    action = JJEditorActions.goBackAction;
    markAsStateDependentAction(action.getText(), true); 
    parentMenu.add(action);

    // TODO add a format command
       
    super.editorContextMenuAboutToShow(parentMenu);
  }

  /**
   * Called by JJReconcilingStrategy
   * Takes the current Document and setInput() on JJOutlinePage
   * which leads to JJContentProvider.setInput() which parse the document.
   */
  protected void updateOutlinePage() {
    IDocument doc = getDocument();
    if (page != null) {
      page.setInput(doc);
    }
  }

  /**
   * @return document
   */
  public IDocument getDocument() {
    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    return doc;
  }

  /**
   * Search via the contentProvider of the OutlinePage
   * for the String text given as argument
   * @param text
   * @return SimpleNode
   */
  public SimpleNode search(String text) {
    JJContentProvider contentProvider = null;
    // Get a chance to parse if Outline has not parsed the document
    if (page == null) {
      contentProvider = new JJContentProvider();
      contentProvider.parse(getDocument().get());
    }
    else
      contentProvider = (JJContentProvider) page.getContentProvider();
    // Get and search the AST
    SimpleNode node = contentProvider.getAST();
    node = contentProvider.getAST();
    if (node != null)
      return node.search(text);
    return null;
  }

  /**
   * Set the Selection given a Node of the AST
   * @param node
   */
  public void setSelection(SimpleNode node) {
    try {
      IDocument doc = getDocument();
      if (doc != null) {
        int start = doc.getLineOffset(node.getBeginLine() - 1);
        int end = doc.getLineOffset(node.getEndLine());
        if (start > end) end = start;
        int length = end - start;
        resetHighlightRange();
        setHighlightRange(start, length, true);
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
   * @param IRegion (the highligth), ITextSelection (the selection)
   */
  public void setSelection(IRegion range, ITextSelection sel) {
    if (range != null)
      setHighlightRange(range.getOffset(), range.getLength(), true);
    else 
      resetHighlightRange();
    if (sel != null)
      selectAndReveal(sel.getOffset(), sel.getLength());
  }
}
