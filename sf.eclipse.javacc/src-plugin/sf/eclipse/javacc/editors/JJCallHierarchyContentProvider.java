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
 * @author Marc Mazas 2009-2010-2011
 */
public class JJCallHierarchyContentProvider implements ITreeContentProvider {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision

  /** True for callers mode, false for callees mode */
  private int mode;

  /**
   * Set the mode, can be {@link JJCallHierarchyView#CALLERS} or {@link JJCallHierarchyView#CALLEES}.
   * 
   * @param aMode the mode to set
   */
  public void setCallMode(final int aMode) {
    mode = aMode;
  }

  /**
   * @see ITreeContentProvider#getChildren(Object)
   */
  @Override
  public Object[] getChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == JJCallHierarchyView.CALLERS) {
      return node.getCallers();
    }
    else {
      return node.getCallees();
    }
  }

  /**
   * @see IStructuredContentProvider#getElements(Object)
   */
  @Override
  public Object[] getElements(final Object aInputElement) {
    return getChildren(aInputElement);
  }

  /**
   * @see ITreeContentProvider#getParent(Object)
   */
  @Override
  public Object getParent(final Object aObj) {
    return aObj == null ? null : ((JJNode) aObj).jjtGetParent();
  }

  /**
   * @see ITreeContentProvider#hasChildren(Object)
   */
  @Override
  public boolean hasChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == JJCallHierarchyView.CALLERS) {
      return node.getCallers().length != 0;
    }
    else {
      return node.getCallees().length != 0;
    }
  }

  /**
   * @see IContentProvider#dispose()
   */
  @Override
  public void dispose() {
    // nothing done here
  }

  /**
   * @see IContentProvider#inputChanged(Viewer, Object, Object)
   */
  @Override
  public void inputChanged(@SuppressWarnings("unused") final Viewer aViewer,
                           @SuppressWarnings("unused") final Object aOldInput,
                           @SuppressWarnings("unused") final Object aNewInput) {
    // nothing done here
  }
}
