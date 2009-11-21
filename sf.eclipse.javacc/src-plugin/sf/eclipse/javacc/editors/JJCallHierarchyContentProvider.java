package sf.eclipse.javacc.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * Content provider for the Call hierarchy view provides callers or callees depending of fMode
 * 
 * @author Remi Koutcherawy 2003-2009 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJCallHierarchyContentProvider implements ITreeContentProvider {

  /*
   * MMa 11/09 : javadoc and formatting revision
   */
  /** true for callers mode, false for callees mode */
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
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
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
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(final Object obj) {
    return obj == null ? null : ((JJNode) obj).jjtGetParent();
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
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
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    // nothing done here
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(@SuppressWarnings("unused") final Viewer viewer,
                           @SuppressWarnings("unused") final Object oldInput,
                           @SuppressWarnings("unused") final Object newInput) {
    // nothing done here
  }
}
