package sf.eclipse.javacc.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * This tree content provider for the Call Hierarchy View provides callers or callees trees depending of the
 * mode selected in the view.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
class CallHierarchyContentProvider implements ITreeContentProvider {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision
  // BF  06/2012 : removed else clauses to avoid warning message
  // MMa 10/2012 : renamed
  // MMa 11/2014 : added some final modifiers 

  /** True for callers mode, false for callees mode */
  protected int mode;

  /**
   * Set the mode, can be {@link CallHierarchyView#CALLERS} or {@link CallHierarchyView#CALLEES}.
   * 
   * @param aMode - the mode to set
   */
  public final void setCallMode(final int aMode) {
    mode = aMode;
  }

  /** {@inheritDoc} */
  @Override
  public final Object[] getChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == CallHierarchyView.CALLERS) {
      return node.getCallers();
    }
    return node.getCallees();
  }

  /** {@inheritDoc} */
  @Override
  public final Object[] getElements(final Object aInputElement) {
    return getChildren(aInputElement);
  }

  /** {@inheritDoc} */
  @Override
  public final Object getParent(final Object aObj) {
    return aObj == null ? null : ((JJNode) aObj).jjtGetParent();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean hasChildren(final Object aObj) {
    final JJNode node = (JJNode) aObj;
    if (mode == CallHierarchyView.CALLERS) {
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
  public final void inputChanged(@SuppressWarnings("unused") final Viewer aViewer,
                                 @SuppressWarnings("unused") final Object aOldInput,
                                 @SuppressWarnings("unused") final Object aNewInput) {
    // nothing done here
  }
}
