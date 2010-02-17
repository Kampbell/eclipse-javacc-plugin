package sf.eclipse.javacc;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;

/**
 * Used to decorate generated files.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.decorators">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJDecorator extends LabelProvider implements ILabelDecorator, IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision

  /** the Image to add on the original if generated */
  private final Image fImgGeneratedStamp;
  /** the Image to add on the original if excluded */
  private final Image fImgExcludedJJ;

  /**
   * JJDecorator provides a small G in top right and <fromFile.jj>, but only if the resource is marked as
   * Derived.
   */
  public JJDecorator() {
    super();
    ImageDescriptor desc = Activator.getImageDescriptor("jj_generated.gif"); //$NON-NLS-1$
    fImgGeneratedStamp = desc.createImage(Display.getDefault());
    desc = Activator.getImageDescriptor("jj_file_exclude.gif"); //$NON-NLS-1$
    fImgExcludedJJ = desc.createImage(Display.getDefault());
  }

  /**
   * @see ILabelDecorator#dispose
   */
  @Override
  public void dispose() {
    super.dispose();
    fImgGeneratedStamp.dispose();
    fImgExcludedJJ.dispose();
  }

  /**
   * Decorates a given image of a given element with a small image (showing "generated" or "excluded") on the
   * top right
   * 
   * @param aImage the given image
   * @param aElement the given element
   * @return the decorated image
   * @see ILabelDecorator#decorateImage
   */
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
        gc.drawImage(fImgGeneratedStamp, 10, 0);
      }
      if (flagExcluded) {
        gc.drawImage(fImgExcludedJJ, 0, 0);
      }
      gc.dispose();
    }
    return newImage;
  }

  /**
   * Decorates a given string of a given element with a reference <...> to a .jj file if generated.
   * 
   * @param aStr the given Text
   * @param aElement the given element
   * @return the decorated string
   * @see ILabelDecorator#decorateText
   */
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
   * @param aElement the given element
   * @return the .jj file name
   */
  protected String getGeneratedProperty(final Object aElement) {
    String gen = null;
    if (aElement instanceof IResource) {
      final IResource res = (IResource) aElement;
      try {
        if (res.isDerived()) {
          gen = res.getPersistentProperty(QN_GENERATED_FILE);
        }
      } catch (final CoreException e) {
        // e.printStackTrace();
      }
    }
    return gen;
  }

  /**
   * Checks if a given element is a .jj, .jjt, .jtb file and is on the classpath.
   * 
   * @param aElement the given element
   * @return true if a given element is a .jj, .jjt, .jtb file and is on the classpath, false otherwise
   */
  protected boolean isOnClasspath(final Object aElement) {
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

  /**
   * Gets the static instance of JJDecorator.
   * 
   * @return JJDecorator object
   */
  public static JJDecorator getDefault() {
    JJDecorator result = null;
    final IDecoratorManager decoratorManager = Activator.getDefault().getWorkbench().getDecoratorManager();

    if (decoratorManager.getEnabled("sf.eclipse.javacc.jjdecorator")) {
      result = (JJDecorator) decoratorManager.getBaseLabelProvider("sf.eclipse.javacc.jjdecorator"); //$NON-NLS-1$
    }
    return result;
  }

  /**
   * Fires a Label Change Event to refresh all decorators.
   */
  public static void refresh() {
    final JJDecorator dec = JJDecorator.getDefault();
    if (dec != null) {
      dec.fireLabelProviderChanged(new LabelProviderChangedEvent(dec, null));
    }
  }
}
