package sf.eclipse.javacc.filters;

import static sf.eclipse.javacc.base.IConstants.GEN_FILE_QN;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Filters the generated JavaCC files.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.jdt.ui.javaElementFilters">.<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class FilterGeneratedJavaCCFiles extends ViewerFilter {

  /** {@inheritDoc} */
  @Override
  public boolean select(@SuppressWarnings("unused") final Viewer aViewer,
                        @SuppressWarnings("unused") final Object aParentElement, final Object aObj) {
    if (aObj instanceof IAdaptable) {
      final IResource resource = (IResource) ((IAdaptable) aObj).getAdapter(IResource.class);
      if (resource != null) {
        try {
          return !(resource.isDerived() && resource.getPersistentProperty(GEN_FILE_QN) != null);
        } catch (final CoreException e) {
          AbstractActivator.logBug(e);
        }
      }
    }
    return true;
  }
}
