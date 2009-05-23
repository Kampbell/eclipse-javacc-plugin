package sf.eclipse.javacc.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * Content provider for the Call hierarchy view
 * provides callers or callees depending of fMode
 * 
 * @author Remi Koutcherawy 2008-2009
 * CeCILL License http://www.cecill.info/index.en.html
 */
public class JJCallHierarchyContentProvider implements ITreeContentProvider {
  private int fMode;
 
  /**
   * Set the mode, can be JJCallHierarchy.CALLERS or JJCallHierarchy.CALLEES
   * getChildren() then uses node.getCallers() or node.getCallees()
   */
  public void setCallMode(int mode) {
    fMode = mode;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object obj) {
    JJNode node = (JJNode) obj;
    if (fMode == JJCallHierarchy.CALLERS)
      return node.getCallers();
    else 
      return node.getCallees();
  }
  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object obj) {
    return  obj == null ? null : ((JJNode)obj).jjtGetParent();
  }
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object obj) {
    JJNode node = (JJNode) obj;
    if (fMode == JJCallHierarchy.CALLERS)
      return node.getCallers().length != 0;
    else 
      return node.getCallees().length != 0;
  }
  // Not used but required by implementing ITreeContentProvider
  public void dispose() {}
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
}
