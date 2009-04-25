package sf.eclipse.javacc.editors;

import java.io.StringReader;
import java.util.ArrayList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import sf.eclipse.javacc.parser.*;

/**
 * Reconciler strategy which updates the Outline View
 * on a document change.
 * 
 * @author Remi Koutcherawy 2003-2006 - CeCILL Licence http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJReconcilingStrategy implements IReconcilingStrategy, 
  IReconcilingStrategyExtension, JavaCCParserTreeConstants {
  
  JJEditor editor;
  ArrayList<Position> fPositions = new ArrayList<Position>();
  
  /**
   * Reconciling strategy updates the Outline View
   * @param anEditor the current editor
   */
  public JJReconcilingStrategy(JJEditor anEditor) {
    editor = anEditor;
  }

  /** 
   * Calls the update() method
   * @see IReconcilingStrategy#setDocument(IDocument)
   */
  public void setDocument(IDocument aDoc) {
    update();
  }

  /**
   * Calls the update() method
   * @see IReconcilingStrategyExtension#initialReconcile()
   */
  public void initialReconcile() {
    update();
  }

  /** 
   * Calls the update() method
   * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
   */
  public void reconcile(DirtyRegion aDirtyRegion, IRegion aRegion) {
    update();
  }

  /** 
   * Calls the update() method
   * @see IReconcilingStrategy#reconcile(IRegion)
   */
  public void reconcile(IRegion region) {
    update();
  }

  /**
   * Update the Outline View
   */
  public void update() {
    calculatePositions();
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        editor.updateOutlinePage();
        editor.updateFoldingStructure(fPositions);
      }
    });
  }

  /**
   * Does nothing
   * @see IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void setProgressMonitor(IProgressMonitor monitor) {
      // Needed by implementing IReconcilingStrategyExtension
      // Do nothing
  }

  /**
   * Calculate and add folding positions
   */
  private void calculatePositions() {
    // Clean old positions
    fPositions.clear();
    
    // Parse document to get the AST
    StringReader in = new StringReader(editor.getDocument().get());
    JJNode node = JavaCCParser.parse(in);
    in.close();

    // Search recursively and add folding positions for selected nodes
    search( node);
  }

  /**
   * Search a node's children
   * 
   * @param node the node
   */
  public void search(JJNode node) {
    // Add region if the node is one of these types
    int id = node.getId();
    if ( id == JJTJAVACC_OPTIONS
        || id == JJTPARSER_BEGIN
        || id == JJTJAVACODE_PRODUCTION
        || id == JJTBNF_PRODUCTION
        || id == JJTREGULAR_EXPR_PRODUCTION
        || id == JJTCLASSORINTERFACEDECLARATION
        || id == JJTMETHODDECLARATION ) {
      try {
        // Get Document from Editor
        IDocument doc = editor.getDocument();
        // Add Folding region
        int start = doc.getLineOffset(node.getBeginLine() - 1); // JavaCC begins à 1 Eclipse begins à 0
        int end = doc.getLineOffset(node.getEndLine());
        fPositions.add(new Position(start, end - start));
      }
      catch (BadLocationException e) {
//        e.printStackTrace(); // Ignore
      }
    }

    // Search children of this node
    Node[] children = node.getChildren();
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        JJNode child = (JJNode) children[i];
        search(child);
      }
    }
    return;
  }
}
