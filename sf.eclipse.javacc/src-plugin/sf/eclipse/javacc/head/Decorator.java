package sf.eclipse.javacc.head;

import static sf.eclipse.javacc.base.IConstants.GEN_FILE_QN;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Used to decorate generated files.<br>
 * Decorator provides a small G in top right and <fromFile.jj>, but only if the resource is marked as Derived.
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.decorators">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class Decorator extends LabelProvider implements ILabelDecorator {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : change on QN_GENERATED_FILE for bug 2965665 fix
  // MMa 12/2010 : fix a NPE in getGeneratedProperty
  // MMa 10/2012 : renamed
  // MMa 11/2014 : modified some modifiers ; renamed fields ; commented unused code

  /** The Image to add on the original if generated */
  private final Image jImgGenerated;
  /** The Image to add on the original if excluded */
  private final Image jImgExcluded;

  /**
   * Constructor.
   */
  public Decorator() {
    super();
    ImageDescriptor desc = AbstractActivator.getImageDescriptor("jj_generated.gif"); //$NON-NLS-1$
    jImgGenerated = desc.createImage(Display.getDefault());
    desc = AbstractActivator.getImageDescriptor("jj_excluded.gif"); //$NON-NLS-1$
    jImgExcluded = desc.createImage(Display.getDefault());
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    super.dispose();
    jImgGenerated.dispose();
    jImgExcluded.dispose();
  }

  /** {@inheritDoc} */
  @Override
  public Image decorateImage(final Image aImage, final Object aElement) {
    // the image to decorate is the background we paint on
    Image newImage = aImage;
    // Look if decoration is needed
    final boolean flagGenerated = (getGeneratedProperty(aElement) != null);
    final boolean flagExcluded = !isOnClasspath(aElement);
    if (flagGenerated == true || flagExcluded == true) {
      // create
      final Rectangle bounds = aImage.getBounds();
      final Display display = Display.getDefault();
      newImage = new Image(display, bounds.height, bounds.width);
      // decorate
      final GC gc = new GC(newImage);
      gc.drawImage(aImage, 0, 0);
      if (flagGenerated == true) {
        gc.drawImage(jImgGenerated, 10, 0);
      }
      if (flagExcluded) {
        gc.drawImage(jImgExcluded, 0, 0);
      }
      gc.dispose();
    }
    return newImage;
  }

  /** {@inheritDoc} */
  @Override
  public String decorateText(final String aStr, final Object aElement) {
    final String generated = getGeneratedProperty(aElement);
    if (generated == null) {
      return aStr;
    }
    final StringBuffer buf = new StringBuffer(aStr);
    buf.append(" <"); //$NON-NLS-1$
    buf.append(generated);
    buf.append(">"); //$NON-NLS-1$
    return buf.toString();
  }

  /**
   * Retrieves the .jj file name if the file is derived.
   * 
   * @param aElement - the given element
   * @return the .jj file name
   */
  private String getGeneratedProperty(final Object aElement) {
    if (aElement == null) {
      return null;
    }
    String gen = null;
    if (aElement instanceof IResource) {
      final IResource res = (IResource) aElement;
      try {
        if (res.isDerived()) {
          gen = res.getPersistentProperty(GEN_FILE_QN);
          if (gen == null) {
            return null;
          }
          gen = gen.substring(gen.lastIndexOf('/') + 1);
        }
      } catch (final CoreException e) {
        AbstractActivator.logBug(e);
        return null;
      }
    }
    return gen;
  }

  /**
   * Checks if a given element is a .jj, .jjt, .jtb file and is on the classpath.
   * 
   * @param aElement - the given element
   * @return true if a given element is a .jj, .jjt, .jtb file and is on the classpath, false otherwise
   */
  private boolean isOnClasspath(final Object aElement) {
    boolean gen = true;
    if (aElement instanceof IResource) {
      final IResource res = (IResource) aElement;
      // look only for jj, jjt and jtb files
      final String ext = res.getFullPath().getFileExtension();
      if ("jj".equals(ext) || "jjt".equals(ext) || "jtb".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final IProject project = res.getProject();
        final IJavaProject javaProject = JavaCore.create(project);
        if (javaProject != null) {
          gen = javaProject.isOnClasspath(res);
        }
      }
    }
    return gen;
  }

  //  /**
  //   * Gets the static instance of Decorator.
  //   * 
  //   * @return the Decorator instance
  //   */
  //  private static Decorator getDefault() {
  //    Decorator result = null;
  //    final IDecoratorManager decoratorManager = Activator.getDefault().getWorkbench().getDecoratorManager();
  //
  //    if (decoratorManager.getEnabled(DECORATOR_ID)) {
  //      result = (Decorator) decoratorManager.getBaseLabelProvider(DECORATOR_ID);
  //    }
  //    return result;
  //  }

  //  /**
  //   * Fires a Label Change Event to refresh all decorators.
  //   */
  //  private static void refresh() {
  //    final Decorator dec = Decorator.getDefault();
  //    if (dec != null) {
  //      dec.fireLabelProviderChanged(new LabelProviderChangedEvent(dec, null));
  //    }
  //  }
}
