package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC element hyperlink.
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
class JJHyperlink implements IHyperlink {

  private final IRegion fRegion;
  private final JJEditor fEditor;
  private final JJNode fNode;

  /**
   * Creates a new Java element hyperlink.
   */
  public JJHyperlink(IRegion region, JJEditor editor, JJNode node) {
    fRegion = region;
    fEditor = editor;
    fNode = node;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkRegion()
   */
  public IRegion getHyperlinkRegion() {
    return fRegion;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#open()
   */
  public void open() {
    fEditor.setSelection(fNode);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getTypeLabel()
   */
  public String getTypeLabel() {
    return fNode.toString();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkText()
   */
  public String getHyperlinkText() {
    return fNode.toString();
  }
}