package sf.eclipse.javacc.filters;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import sf.eclipse.javacc.IJJConstants;

public class FilterGeneratedJavaCCFiles extends ViewerFilter implements IJJConstants {
  public boolean select(Viewer viewer, Object parentElement, Object obj) {
    if (obj instanceof IAdaptable) {
      IResource resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
      if (resource != null) {
        try {
          return !(resource.isDerived() && resource
              .getPersistentProperty(QN_GENERATED_FILE) != null);
        }
        catch (CoreException e) {}
      }
    }
    return true;
  }
}
