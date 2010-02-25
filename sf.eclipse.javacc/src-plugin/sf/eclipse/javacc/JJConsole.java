package sf.eclipse.javacc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
 * Console for JavaCC output.<br>
 * JavaCC output is parsed to add Tasks for errors and warnings, so we don't really need a console...<br>
 * But sometimes it's reassuring to read "Parser generated successfully".<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.views"><br>
 * Since 1.3 this console is used for reporting errors, so must be.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJConsole extends ViewPart implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision ; fixed issue for JTB problems reporting
  // TODO voir pour problems JJDoc

  /** The viewer control */
  static StyledText                   fViewer;
  /** The print stream */
  private final ByteArrayOutputStream fBaos;
  /** The last parsed position, from where to start when retrieving text from viewer */
  private int                         fLastOffset;
  /** Pattern to find the line number in JavaCC / JJTree compilation messages */
  final static Pattern                jjLinePattern    = Pattern.compile("^.*", Pattern.MULTILINE);                                                     //$NON-NLS-1$
  /** Pattern to find the Error or Warning or ParseException message in JavaCC / JJTree compilation messages */
  final static Pattern                jjPbPattern      = Pattern
                                                                .compile("([eE]rror[: ]|[wW]arning[^s]|ParseException:|Encountered\\s\")(.+)");         //$NON-NLS-1$
  /** Pattern to find the line and column numbers in JavaCC / JJTree compilation messages */
  final static Pattern                jjLineColPattern = Pattern.compile("[lL]ine (\\d+), [cC]olumn (\\d+)");                                           //$NON-NLS-1$
  /** Pattern to find the Info or Warning message in JTB compilation messages */
  final static Pattern                jtbPbPattern     = Pattern
                                                                .compile("\\((\\d+)\\)(:  (warning|info|soft error|unexpected program error):  (.*))?"); //$NON-NLS-1$

  /**
   * Standard constructor. Allocates the print stream.
   */
  public JJConsole() {
    fBaos = new ByteArrayOutputStream(1024);
  }

  /**
   * Creates the SWT controls for this workbench part. Called by Workbench to initialize the
   * org.eclipse.ui.views extension TextConsoleViewer(). Does most of the job.
   * 
   * @see ViewPart#createPartControl
   */
  @Override
  public void createPartControl(final Composite aParent) {
    fViewer = new StyledText(aParent, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
    fViewer.setEditable(false);
    fViewer.setDoubleClickEnabled(true);
    JJConsoleHyperlink.setViewer(fViewer);

    // add a "Clear console" button on the toolbar
    final Action clear = new Action(Activator.getString("JJConsole.Clear")) { //$NON-NLS-1$

      @Override
      public void run() {
        clear();
      }
    };
    final ImageDescriptor desc = Activator.getImageDescriptor("jj_clear_co.gif"); //$NON-NLS-1$
    clear.setImageDescriptor(desc);
    clear.setToolTipText(Activator.getString("JJConsole.Clear_JavaCC_console")); //$NON-NLS-1$

    // "Copy" action on menu
    final Action copy = new Action(Activator.getString("JJConsole.Copy")) { //$NON-NLS-1$

      @Override
      public void run() {
        fViewer.copy();
      }
    };

    // add on toolbar
    final IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(clear);

    // add context menu
    final MenuManager menuMgr = new MenuManager();
    final Menu popup = menuMgr.createContextMenu(fViewer);
    menuMgr.add(clear);
    menuMgr.add(copy);
    fViewer.setMenu(popup);
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    fViewer.setFocus();
  }

  /**
   * Clears console.
   */
  public void clear() {
    if (Thread.currentThread() != fViewer.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {

        public void run() {
          clear();
        }
      });
    }
    else {
      fViewer.setText(""); //$NON-NLS-1$
      JJConsoleHyperlink.clear();
      fLastOffset = 0;
    }
  }

  /**
   * @return the PrintStream to write to Console
   */
  public PrintStream getPrintStream() {
    return new PrintStream(fBaos);
  }

  /**
   * Prints to Console in Bold Red.
   * 
   * @param aStr the text to print
   */
  public void print(final String aStr) {
    addText(aStr, true);
  }

  /**
   * Ends reporting. Called when JJBuilder has finished.
   * 
   * @param aFile the file to report on
   * @param aIsJtb true if file is a JTB one, false otherwise.
   */
  public void endReport(final IFile aFile, final boolean aIsJtb) {
    addText(fBaos.toString(), false);
    fBaos.reset();
    // add Markers and Hyperlinks
    markErrors(aFile, aIsJtb);
  }

  /**
   * Adds this text to the Console.
   * 
   * @param aTxt the text to add
   * @param aIsRed if the text is to be displayed in Dark Red
   */
  void addText(final String aTxt, final boolean aIsRed) {
    // test before updating the viewer
    if (Thread.currentThread() != fViewer.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          addText(aTxt, aIsRed);
        }
      });
    }
    // update the viewer
    else {
      final int offset = fViewer.getCharCount();
      fViewer.append(aTxt);
      if (aIsRed) {
        final Color fg = fViewer.getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
        final StyleRange style = new StyleRange(offset, aTxt.length() - 1, fg, null);
        style.fontStyle = SWT.BOLD;
        fViewer.setStyleRange(style);
      }
      // scroll to end
      fViewer.setCaretOffset(fViewer.getCharCount());
      fViewer.showSelection();
    }
  }

  /**
   * Decodes output to catch lines reporting errors.
   * 
   * @param aFile the file to report on
   * @param aIsJtb true if file is a JTB one, false otherwise.
   */
  void markErrors(final IFile aFile, final boolean aIsJtb) {
    // test before retrieving text from the viewer
    if (Thread.currentThread() != fViewer.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          markErrors(aFile, aIsJtb);
        }
      });
    }
    else {
      // get the text from the console
      final StyledTextContent c = fViewer.getContent();
      final int count = fViewer.getCharCount();
      final String txt = c.getTextRange(fLastOffset, count - fLastOffset);

      String report; // the message for the marker
      int line, col; // location of the faulty text in editor
      int offset, length; // location of the link in console

      if (!aIsJtb) {
        // .jj or .jjt file : we get outputs like :
        // "Error at line 14, column 15"
        // "Warning: Lookahead adequacy checking not being performed since option LOOKAHEAD is more than 1.  Set option FORCE_LA_CHECK to true to force checking."
        // "Encountered "<" at line 77, column 15."

        // parse line by line to find problems
        // then get the numbers following 'line ' and 'column ' on the line or the next line
        // hyperlinks are added with offset, length in the text and file, line, column references 

        // match line by line
        final Matcher lineMatcher = jjLinePattern.matcher(txt);
        while (lineMatcher.find()) {
          report = lineMatcher.group();

          // look for 'Error' or 'Warning' or 'ParseException' or 'Encountered'
          final Matcher jjPbMatcher = jjPbPattern.matcher(report);
          if (jjPbMatcher.find() == false) {
            continue;
          }

          // set severity accordingly
          final int severity = (jjPbMatcher.group().indexOf("arning") != -1) ? //$NON-NLS-1$
                                                                            IMarker.SEVERITY_WARNING
                                                                            : IMarker.SEVERITY_ERROR;
          // note the position of the problem in the text
          offset = lineMatcher.start() + jjPbMatcher.start();
          length = jjPbMatcher.end(1) - jjPbMatcher.start();

          // look for 'line l, column c' 
          Matcher lineColumnMatcher = jjLineColPattern.matcher(report);

          // if there is no line column, guess they are on the next line
          if (lineColumnMatcher.find() == false) {
            lineMatcher.find();
            // add next line to the report
            report += "\n" + lineMatcher.group(); //$NON-NLS-1$
            lineColumnMatcher = jjLineColPattern.matcher(lineMatcher.group());
            // if the report doesn't give any line
            if (lineColumnMatcher.find() == false) {
              // mark at the beginning of the editor
              markError(aFile, report, severity, 1, 0);
              // mark the Warning with an hyperlink pointing to the beginning of the file
              new JJConsoleHyperlink(fLastOffset + offset, length, aFile, 1, 0);
              continue;
            }
          }
          // for each line, column, add a report and an hyperlink
          do {
            line = Integer.parseInt(lineColumnMatcher.group(1));
            col = Integer.parseInt(lineColumnMatcher.group(2));
            offset = lineColumnMatcher.start();
            length = lineColumnMatcher.end() - offset;

            // replace .jj by .jjt eventually, but not by .jtb
            IFile newFile = aFile;
            try {
              if (aFile.isDerived()) {
                String from = aFile.getPersistentProperty(QN_GENERATED_FILE);
                String dir = aFile.getParent().getLocation().toOSString();
                from = dir + File.separatorChar + from;
                final IProject project = aFile.getProject();
                dir = project.getLocation().toOSString();
                from = from.substring(dir.length() + 1);
                final IResource resFrom = project.findMember(from);
                if (resFrom != null) {
                  newFile = (IFile) resFrom;
                  if (newFile.getFileExtension().equals("jtb")) { //$NON-NLS-1$
                    newFile = aFile;
                  }
                }
              }
            } catch (final CoreException e) {
              // ignore
            }

            // add the problem to the editor problems
            markError(newFile, report, severity, line, col);

            // add Hyperlink in the console
            // the first line is 1 for JavaCC and 0 for Eclipse editors
            new JJConsoleHyperlink(fLastOffset + lineMatcher.start() + offset, length, newFile, line - 1,
                                   col - 1);
          } // do this for all occurrences on the line.
          while (lineColumnMatcher.find());
        }
      }
      else {
        // .jtb file : we get outputs like : 
        // "new.jtb (406):  warning:  Non initialized user variable 'isTypedef'. May lead to compiler error(s) (specially for 'Token' variables). Check in generated parser."
        // "new.jtb (461):  info:  Non "void" BNFProduction. Result type 'boolean' will be changed into 'type_modifiers', and a parser class variable 'jtbrt_type_modifiers' of type 'boolean' will be added to hold the return values."
        // "new.jtb (340):  soft error:  Empty BNF expansion in "<production>()", 345"
        // "new.jtb (234):  unexpected program error:  <exception / throwable message>"

        final Matcher jtbPbMatcher = jtbPbPattern.matcher(txt);
        while (jtbPbMatcher.find()) {
          report = jtbPbMatcher.group();
          final String sevStr = jtbPbMatcher.group(3);
          final int severity = "info".equals(sevStr) ? IMarker.SEVERITY_INFO //$NON-NLS-1$
                                                    : "warning".equals(sevStr) ? IMarker.SEVERITY_WARNING //$NON-NLS-1$
                                                                              : IMarker.SEVERITY_ERROR;

          line = Integer.parseInt(jtbPbMatcher.group(1));
          col = 1;
          offset = jtbPbMatcher.start(0);
          // show the hyperlink only up to "info" or "warning" or "error" to increase Console readability
          length = jtbPbMatcher.end(3) - offset;
          // add the Warning in the editor problems 
          markError(aFile, report, severity, line, col);
          // the first line is 1 for JTB and 0 for Eclipse editors
          new JJConsoleHyperlink(fLastOffset + offset, length, aFile, line - 1, col - 1);
        }
      }

      // next time the text shall be parsed from lastOffset 
      fLastOffset = count;
    }
  }

  /**
   * Add a marker to signal a problem. Hover tips are managed by JJSourceViewerConfiguration.
   * 
   * @param aFile the file to report on
   * @param aMsg the marker message
   * @param aSeverity the marker severity
   * @param aLine the marker line
   * @param aCol the marker column
   */
  private static void markError(final IFile aFile, final String aMsg, final int aSeverity, final int aLine,
                                @SuppressWarnings("unused") final int aCol) {
    try {
      final IMarker marker = aFile.createMarker(IMarker.PROBLEM);
      final HashMap<String, Comparable<?>> attributes = new HashMap<String, Comparable<?>>(4);
      attributes.put(IMarker.MESSAGE, aMsg);
      attributes.put(IMarker.SEVERITY, new Integer(aSeverity));
      attributes.put(IMarker.LINE_NUMBER, new Integer(aLine));
      // would be nice except it is the char count from the start of the file
      // LINE_NUMBER is not taken into account if CHAR_START is given
      //      attributes.put(IMarker.CHAR_START, new Integer(col));
      //      attributes.put(IMarker.CHAR_END, new Integer(col+1));
      marker.setAttributes(attributes);
    } catch (final CoreException ex) {
      ex.printStackTrace();
    }
  }
}
