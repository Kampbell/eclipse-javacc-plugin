package sf.eclipse.javacc.editors;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * Content provider for the Call hierarchy view provides callers or callees depending of fMode
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJCallHierarchyContentProvider implements ITreeContentProvider {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision

  /** True for callers mode, false for callees mode */
  private int fMode;

  /**
   * Set the mode, can be JJCallHierarchy.CALLERS or JJCallHierarchy.CALLEES getChildren() then uses
   * node.getCallers() or node.getCallees()
   * 
   * @param mode the mode to set
   */
  public void setCallMode(final int mode) {
    fMode = mode;
  }

  /**
   * @see ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(final Object obj) {
    final JJNode node = (JJNode) obj;
    if (fMode == JJCallHierarchy.CALLERS) {
      return node.getCallers();
    }
    else {
      return node.getCallees();
    }
  }

  /**
   * @see IStructuredContentProvider#getElements(Object)
   */
  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  /**
   * @see ITreeContentProvider#getParent(Object)
   */
  public Object getParent(final Object obj) {
    return obj == null ? null : ((JJNode) obj).jjtGetParent();
  }

  /**
   * @see ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(final Object obj) {
    final JJNode node = (JJNode) obj;
    if (fMode == JJCallHierarchy.CALLERS) {
      return node.getCallers().length != 0;
    }
    else {
      return node.getCallees().length != 0;
    }
  }

  /**
   * @see IContentProvider#dispose()
   */
  public void dispose() {
    // nothing done here
  }

  /**
   * @see IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(@SuppressWarnings("unused") final Viewer viewer,
                           @SuppressWarnings("unused") final Object oldInput,
                           @SuppressWarnings("unused") final Object newInput) {
    // nothing done here
  }
}
