package sf.eclipse.javacc;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

/**
 * Console for JavaCC output
 * JavaCC output is parsed to add Tasks for errors and warnings,
 * so we don't really need a console...
 * But sometimes it's reassuring to read "Parser generated successfully"
 * @see ViewPart
 * @see org.eclipse.debug.internal.ui.views.console.ConsoleView
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJConsole extends ViewPart {
  public static final String CONSOLE_ID = "sf.eclipse.javacc.Console"; //$NON-NLS-1$
  private StringBuffer buf = new StringBuffer();
  private ArrayList list = new ArrayList();
  StyledText viewer = null;
  private Color red;

  /**
   * Creates the SWT controls for this workbench part.
   * Called by the Workbench to initialize the org.eclipse.ui.views extension
   * @see ViewPart#createPartControl
   */
  public void createPartControl(Composite parent) {
    viewer = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
    viewer.setEditable(false);
    viewer.setDoubleClickEnabled(true);
    red = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

    // Add a "Clear console" button on the toolbar
    Action clear = new Action(JavaccPlugin.getResourceString("JJConsole.Clear")) { //$NON-NLS-1$
      public void run() {
        clear();
      }
    };
    ImageDescriptor desc = JavaccPlugin.getDefault().getResourceImageDescriptor("jj_clear_co.gif"); //$NON-NLS-1$
    clear.setImageDescriptor(desc);
    clear.setToolTipText(JavaccPlugin.getResourceString("JJConsole.Clear_JavaCC_console")); //$NON-NLS-1$

    // "Copy" action on menu
    Action copy = new Action(JavaccPlugin.getResourceString("JJConsole.Copy")) { //$NON-NLS-1$
      public void run() {
        viewer.copy();
      }
    };
    
    // Add on toolbar
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(clear);

    // Add context menu
    MenuManager menuMgr = new MenuManager();
    Menu popup = menuMgr.createContextMenu(this.viewer);
    menuMgr.add(clear);
    menuMgr.add(copy);
    viewer.setMenu(popup);
    
    // In order for the clipboard actions to be accessible via their shortcuts
    // (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler.
    // viewer.setKeyBinding('c' | SWT.CTRL, ST.COPY); does not work
    IActionBars actionBars = getViewSite().getActionBars();
    actionBars.setGlobalActionHandler("copy", copy); //$NON-NLS-1$
  } 
  
  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    viewer.setFocus();
  }
    
  /**
   * Add the string to the console
   */
  public void addText(String txt) {
    buf.append(txt);
  }
  
  /**
   * Adds the string using red and bold style
   */
  public void addRedText(String txt) {
    StyleRange style = new StyleRange();
    style.start = buf.length();
    style.length = txt.length();
    style.foreground = red;
    style.fontStyle = SWT.BOLD;
    list.add(style);
    buf.append(txt);
  } 
  
  /**
   * Clears view
   */
  public void clear() {
    buf.delete(0, buf.length());
    list.clear();
    show();
  }
  
  /**
   * Displays text
   */
  public void show() {
    if (!isValidThread()) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          show();
        }
      });
    }
    else {
      viewer.setText(buf.toString());
      Iterator it = list.iterator();
      while (it.hasNext())
        viewer.setStyleRange((StyleRange) (it.next()));

      // Scrolls to end
      int lines = viewer.getClientArea().height / viewer.getLineHeight();
      int index = viewer.getLineCount() - lines - 1;
      viewer.setTopIndex(index);
    }
  }
  
  /**
   * Tests before performing update on the viewer
   */
  protected boolean isValidThread() {
    return Thread.currentThread() == viewer.getDisplay().getThread();
  }
}