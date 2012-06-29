package sf.eclipse.javacc.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * Content provider for the Call hierarchy view provides callers or callees depending of fMode
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
public class JJCallHierarchyContentProvider implements ITreeContentProvider {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision
  // BF  06/2012 : removed else clauses to avoid warning message

  /** True for callers mode, false for callees mode */
  private int mode;

  /**
   * Set the mode, can be {@link JJCallHierarchyView#CALLERS} or {@link JJCallHierarchyView#CALLEES}.
   * 
   * @param aMode - the mode to set
   */
  public void setCallMode(final int aMode) {
    mode = aMode;
  }

  /** {@inheritDoc} */
  @Override
  public Object[] getChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == JJCallHierarchyView.CALLERS) {
      return node.getCallers();
    }
    return node.getCallees();
  }

  /** {@inheritDoc} */
  @Override
  public Object[] getElements(final Object aInputElement) {
    return getChildren(aInputElement);
  }

  /** {@inheritDoc} */
  @Override
  public Object getParent(final Object aObj) {
    return aObj == null ? null : ((JJNode) aObj).jjtGetParent();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == JJCallHierarchyView.CALLERS) {
      return node.getCallers().length != 0;
    }
    return node.getCallees().length != 0;
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    // nothing done here
  }

  /** {@inheritDoc} */
  @Override
  public void inputChanged(@SuppressWarnings("unused") final Viewer aViewer,
                           @SuppressWarnings("unused") final Object aOldInput,
                           @SuppressWarnings("unused") final Object aNewInput) {
    // nothing done here
  }
}
