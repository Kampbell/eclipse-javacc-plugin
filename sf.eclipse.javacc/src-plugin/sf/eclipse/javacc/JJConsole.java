package sf.eclipse.javacc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

/**
 * Console for JavaCC output JavaCC output is parsed to add Tasks for errors and
 * warnings, so we don't really need a console... But sometimes it's reassuring
 * to read "Parser generated successfully"
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.ui.views">
 *  
 * Since 1.3 this console is used for reporting errors, so must be.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJConsole extends ViewPart implements IJJConstants {
  // viewer control
  static StyledText viewer;
  // PrintStream
  private ByteArrayOutputStream baos;
  // The file to report errors to
  private IFile fFile;
  // When retrieving text from viewer, need to start at last parsed 
  private int lastOffset;

  public JJConsole() { 
    baos = new ByteArrayOutputStream(1024);
  }
  
  /**
   * Creates the SWT controls for this workbench part. 
   * Called by Workbench to initialize the org.eclipse.ui.views extension
   * TextConsoleViewer() do most of the job
   * 
   * @see ViewPart#createPartControl
   */
  public void createPartControl(Composite parent) {
    viewer = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
    viewer.setEditable(false);
    viewer.setDoubleClickEnabled(true);
    JJConsoleHyperlink.setViewer(viewer);
    
    // Add a "Clear console" button on the toolbar
    Action clear = new Action(Activator.getString("JJConsole.Clear")) { //$NON-NLS-1$
      public void run() {
        clear();
      }
    };
    ImageDescriptor desc = Activator.getImageDescriptor("jj_clear_co.gif"); //$NON-NLS-1$
    clear.setImageDescriptor(desc);
    clear.setToolTipText(Activator.getString("JJConsole.Clear_JavaCC_console")); //$NON-NLS-1$
    
    // "Copy" action on menu
    Action copy = new Action(Activator.getString("JJConsole.Copy")) { //$NON-NLS-1$
      public void run() {
        viewer.copy();
      }
    };
    
    // Add on toolbar
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(clear);
    
    // Add context menu
    MenuManager menuMgr = new MenuManager();
    Menu popup = menuMgr.createContextMenu(viewer);
    menuMgr.add(clear);
    menuMgr.add(copy);
    viewer.setMenu(popup);
  } 

  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    viewer.setFocus();
  }

  /**
   * Clears console
   */
  public void clear() {
    if (Thread.currentThread() != viewer.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          clear();
        }
      });
    }
    else {
      viewer.setText(""); //$NON-NLS-1$
      JJConsoleHyperlink.clear();
      lastOffset = 0;
    }
  } 
  
  /**
   * This PrintStream shall be dumped to a String by endReport()
   * @return PrintStream to write to Console
   */
  public PrintStream getPrintStream() {
    return new PrintStream(baos);
  }
  
  /**
   * Print to Console in Bold Red
   * @param string
   */
  public void print(String string) {
    addText(string, true);
  }
  
  /**
   * Called when JJBuilder has finished
   */
  public void endReport(IFile file) {
    fFile = file;
    addText(baos.toString(), false);
    baos.reset();
    // Adds Markers and Hyperlinks
    markErrors();
  }
  
  /**
   * Add this text to the Console
   * @param text to add
   * @param isRed if the text is to be displayed in Dark Red
   */
  void addText(final String txt, final boolean isRed) {
    // Tests before updating the viewer
    if (Thread.currentThread() != viewer.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          addText(txt, isRed);
        }
      });
    }
    // Updates the viewer
    else {
      int offset = viewer.getCharCount();
      viewer.append(txt);
      if (isRed) {
        Color fg = viewer.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
        StyleRange style = new StyleRange(offset, txt.length()-1, fg, null);
        style.fontStyle = SWT.BOLD;
        viewer.setStyleRange(style);
      }
      // Scrolls to end
      viewer.setCaretOffset(viewer.getCharCount());
      viewer.showSelection();
    }
  }

  /**
   * Decode output to catch lines reporting errors
   */
  void markErrors() {
    // Tests before retrieving text from the viewer
    if (Thread.currentThread() != viewer.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          markErrors();
        }
      });
    }
    else {
    // Get the text from the console
    StyledTextContent c = viewer.getContent();
    int count = viewer.getCharCount();
    String txt = c.getTextRange(lastOffset, count - lastOffset);
    
    String report;      // The message for the marker
    int line, col;      // location of the faulty text in editor
    int offset, length; // location of the link in console

    // We get outputs like : Error at line 14, column 15
    // Warning: Lookahead adequacy checking not being performed since option LOOKAHEAD is more than 1.  Set option FORCE_LA_CHECK to true to force checking.
    // Parse line by line to find Error and Warnings
    // Then get the numbers following 'line ' and 'column ' on the line or the next line
    // Hyperlinks are added with offset, length in the text and file, line, col references 
    Pattern linePattern = Pattern.compile("^.*", Pattern.MULTILINE); //$NON-NLS-1$
    Pattern errorWarningPattern = Pattern.compile("([eE]rror[: ]|[wW]arning[^s]|ParseException:|Encountered\\s\")(.+)");//$NON-NLS-1$
    Pattern lineColumnPattern = Pattern.compile("[lL]ine (\\d+), [cC]olumn (\\d+)");//$NON-NLS-1$

    // Match line by line
    Matcher lineMatcher = linePattern.matcher(txt);
    while (lineMatcher.find()) {
      report = lineMatcher.group();

      // Look 'Error' or 'Warning' or 'ParseException'
      Matcher errorWarningMatcher = errorWarningPattern.matcher(report);
      if (errorWarningMatcher.find() == false)
        continue;
      
      // Set severity accordingly
      int severity = (errorWarningMatcher.group().indexOf("arning") != -1) ? //$NON-NLS-1$
          IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
      // Note the position of the Warning in the text
      offset = lineMatcher.start() + errorWarningMatcher.start();
      length = errorWarningMatcher.end(1) - errorWarningMatcher.start();

      // Look for 'line l, column c' 
      Matcher lineColumnMatcher = lineColumnPattern.matcher(report);
      
      // If there is no line col, guess they are on the next line
      if (lineColumnMatcher.find() == false){
        lineMatcher.find();
        // Add next line to the report
        report += "\n"+ lineMatcher.group(); //$NON-NLS-1$
        lineColumnMatcher = lineColumnPattern.matcher(lineMatcher.group());
        // If the report doesn't give any line
        if (lineColumnMatcher.find() == false) {
          // Mark at the beginning of the editor
          markError(fFile, report, severity, 1, 0);
          // Mark the Warning with an hyperlink pointing to the beginning of the file
          new JJConsoleHyperlink(lastOffset+offset, length, fFile, 1, 0);
          continue;
        }
      }
      // For each line,col add a report and an hyperlink
      do {
        line = Integer.parseInt(lineColumnMatcher.group(1));
        col = Integer.parseInt(lineColumnMatcher.group(2));
        offset = lineColumnMatcher.start();
        length = lineColumnMatcher.end() - offset;

        // Add the Error or Warning in the editor Problems
        markError(fFile, report, severity, line, col);

        // Add Hyperlink in the console
        // The first line is 1 for JavaCC and 0 for Eclipse editors
        new JJConsoleHyperlink(lastOffset+lineMatcher.start()+offset, length, fFile, line-1, col-1);
      } // Do this for all occurrences on the line.
      while (lineColumnMatcher.find());
    }
    
    // From JTB we get outputs like 
    // "testJTB.jj (67):  warning:  Block of Java code in one_line()."
    // "new_file.jtb (67):  Undefined token "EOL".
    // "Encountered "<" at line 77, column 15."
    Pattern p = Pattern.compile("\\((\\d+)\\)(:  warning(.*))?"); //$NON-NLS-1$
    Matcher m = p.matcher(txt);
    while (m.find()) {
      report = m.group(0);
      int severity = (report.indexOf("warning") != -1) ?  //$NON-NLS-1$
          IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
      line = Integer.parseInt(m.group(1));
      col = 1;
      offset = m.start(0);
      length = m.end(0) - offset;
      // Add the Error or Warning in the editor Problems 
      markError(fFile, report, severity, line, col);
      // The first line is 1 for JTB and 0 for Eclipse editors
      new JJConsoleHyperlink(lastOffset+offset, length, fFile, line-1, col-1);
    }    
    
    // Next time the text shall be parsed from lastOffset 
    lastOffset = count;
    }
  }
 
  /**
   * Add a marker to signal an error or a warning
   * NB. Hover tips are managed by JJSourceViewerConfiguration
   */
  private static void markError(IFile file, String msg, int type, int line, int col) {
    try {
      IMarker marker = file.createMarker(IMarker.PROBLEM); 
      HashMap<String, Comparable> attributes= new HashMap<String, Comparable>(4);
      attributes.put(IMarker.MESSAGE, msg);
      attributes.put(IMarker.SEVERITY, new Integer(type));
      attributes.put(IMarker.LINE_NUMBER, new Integer(line));
      // Would be nice except it is the char count from the start of the file
      // LINE_NUMBER is not taken into account if CHAR_START is given
//      attributes.put(IMarker.CHAR_START, new Integer(col));
//      attributes.put(IMarker.CHAR_END, new Integer(col+1));
      marker.setAttributes(attributes);
    } catch (CoreException ex) {
      ex.printStackTrace();
    }
  }
}


