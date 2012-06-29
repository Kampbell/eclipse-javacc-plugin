package sf.eclipse.javacc.filters;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sf.eclipse.javacc.base.IJJConstants;

/**
 * Filters the generated JavaCC files.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class FilterGeneratedJavaCCFiles extends ViewerFilter implements IJJConstants {

  /** {@inheritDoc} */
  @Override
  public boolean select(@SuppressWarnings("unused") final Viewer aViewer,
                        @SuppressWarnings("unused") final Object aParentElement, final Object aObj) {
    if (aObj instanceof IAdaptable) {
      final IResource resource = (IResource) ((IAdaptable) aObj).getAdapter(IResource.class);
      if (resource != null) {
        try {
          return !(resource.isDerived() && resource.getPersistentProperty(QN_GENERATED_FILE) != null);
        } catch (final CoreException e) {
          // swallowed
        }
      }
    }
    return true;
  }
}
