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
 * Used to decorate generated files
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.ui.decorators">
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJDecorator extends LabelProvider
  implements ILabelDecorator, IJJConstants {
  
  // Images to add on the original if generated or exluded
  private Image imgGeneratedStamp;
  private Image imgExcludedJJ;
  
  /**
   * JJDecorator provides a small G in top right and <fromFile.jj>
   * But only if the resource is marked as Derived.
   */
  public JJDecorator() {
    super();
    ImageDescriptor desc = Activator.getImageDescriptor("jj_generated.gif"); //$NON-NLS-1$
    imgGeneratedStamp = desc.createImage(Display.getDefault());
    desc = Activator.getImageDescriptor("jj_file_exclude.gif"); //$NON-NLS-1$
    imgExcludedJJ = desc.createImage(Display.getDefault());
  }

  /**
   * @see ILabelDecorator#dispose
   */
  public void dispose() {
    super.dispose();
    imgGeneratedStamp.dispose();
    imgExcludedJJ.dispose();
  }

  /**
   * Decorate Image with jj_generated.gif on top right
   * @see ILabelDecorator#decorateImage
   */
  public Image decorateImage(Image image, Object element) {
    // The image to decorate is the background we paint on
    Image newimg = image;
    // Look if decoration is needed
    boolean flagGenerated = (getGeneratedProperty(element) != null);
    boolean flagExcluded = ! isOnClasspath(element);
    if (flagGenerated == true || flagExcluded == true) {
      // Create
      Rectangle bounds = image.getBounds();
      Display display = Display.getDefault();
      newimg = new Image(display, bounds.height, bounds.width);
      // Decorate
      GC gc = new GC(newimg);
      gc.drawImage(image, 0, 0);
      if (flagGenerated == true)
	gc.drawImage(imgGeneratedStamp, 10, 0);
      if (flagExcluded)
	gc.drawImage(imgExcludedJJ, 0, 0);
      gc.dispose();
    }
    return newimg;
  }

  /**
   * Decorate Text with a reference <...> to .jj file
   * @see ILabelDecorator#decorateText
   */
  public String decorateText(String aText, Object obj) {
    String generated = getGeneratedProperty(obj);
    if (generated != null) {
      StringBuffer buf = new StringBuffer(aText);
      buf.append(" <"); //$NON-NLS-1$
      buf.append(generated);
      buf.append(">"); //$NON-NLS-1$
      aText = buf.toString();
    }
    return aText;
  }

  /**
   * Retrieves the .jj file name if the file is derived.
   * @param res
   * @return Originator file
   */
  protected String getGeneratedProperty(Object obj) {
    String gen = null;
    if (obj instanceof IResource) {
      IResource res = (IResource)obj;
      try {
        if (res.isDerived())
          gen = res.getPersistentProperty(QN_GENERATED_FILE);
      } catch (CoreException e) {
        // e.printStackTrace();
      }
    }
    return gen;
  }
  
  /**
   * Check if obj is a .jj, .jjt, jtb file and is on classpath
   * @param Object obj
   */
  protected boolean isOnClasspath(Object obj) {
    boolean gen = true;
    if (obj instanceof IResource) {
      IResource res = (IResource)obj;
      // Look only for jj, jjt and jtb files
      String ext = res.getFullPath().getFileExtension();
      if ("jj".equals(ext) || "jjt".equals(ext) || "jtb".equals(ext)){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// Look only for 
	IProject project = res.getProject();
	IJavaProject javaProject = JavaCore.create(project);
	if (javaProject != null) 
	  gen = javaProject.isOnClasspath(res);
      }
    }
    return gen;
  }

  /**
   * Get the static instance of JJDecorator
   * @return JJDecorator object
   * (not used within the rk.javacc.eclipse.plugin)
   */
  public static JJDecorator getDefault() {
    JJDecorator result = null;
    IDecoratorManager decoratorManager =
      Activator.getDefault().getWorkbench().getDecoratorManager();

    if (decoratorManager.getEnabled("sf.eclipse.javacc.jjdecorator")) //$NON-NLS-1$
    result= (JJDecorator) decoratorManager.getBaseLabelProvider(
        "sf.eclipse.javacc.jjdecorator"); //$NON-NLS-1$
    return result;
  }

  /**
   * Redecorate Files
   * Fire a Label Change event to refresh all decorators
   * (not used within the rk.javacc.eclipse.plugin)
   */
  public static void refresh() {
    JJDecorator dec = JJDecorator.getDefault();
    if (dec != null)
      dec.fireLabelProviderChanged(new LabelProviderChangedEvent(dec, null));
  }
}
