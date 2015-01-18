package sf.eclipse.javacc.head;

import static sf.eclipse.javacc.base.IConstants.CONSOLE_ID;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.IConsole;

/**
 * The main plugin for normal usage (ie non headless builds).<br>
 * Referenced by plugin.xml<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class Activator extends AbstractActivator {

  /**
   * Shows or creates the output console view. Specific to head builds.
   * 
   * @return the console
   */
  @Override
  public IConsole getConsole() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    // access only in the event thread, so create the event thread if not within it
    if (Thread.currentThread() != workbench.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {

        /** {@inheritDoc} */
        @Override
        public void run() {
          sConsole = getConsole();
        }
      });
    }
    else {
      // here we are in the event thread 
      final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      if (windows.length == 0) {
        return null;
      }
      final IWorkbenchPage page = windows[0].getActivePage();
      sConsole = (ConsoleView) page.findView(CONSOLE_ID);
      if (sConsole == null) {
        // if Console is not up, show it ! (console is doing error reporting, and must be up)
        try {
          page.showView(CONSOLE_ID);
        } catch (final PartInitException e) {
          logBug(e);
        }
        sConsole = (ConsoleView) page.findView(CONSOLE_ID);
      }
    }
    return sConsole;
  }

}
