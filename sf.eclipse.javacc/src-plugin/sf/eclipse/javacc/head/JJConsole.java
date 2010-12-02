package sf.eclipse.javacc.head;

import java.io.ByteArrayOutputStream;
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

import sf.eclipse.javacc.base.IJJConsole;
import sf.eclipse.javacc.base.IJJConstants;

/**
 * Console for JavaCC output for normal usage (ie non headless builds).<br>
 * JavaCC output is parsed to add Problems for errors and warnings, so we don't really need a console...<br>
 * But sometimes it's reassuring to read "Parser generated successfully".<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.views"><br>
 * Since 1.3 this console is used for reporting errors, so must be.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJConsole extends ViewPart implements IJJConstants, IJJConsole {

  // MMa 02/2010 : formatting and javadoc revision ; fixed issue for JTB problems reporting
  // MMa 03/2010 : change on QN_GENERATED_FILE for bug 2965665 fix ; change on problems finding, markers & hyperlinks reporting
  // TODO check JJDoc problems handling

  /** The viewer control */
  static StyledText                   fViewer;
  /** The print stream */
  private final ByteArrayOutputStream fBaos;
  /** The last parsed position, from where to start when retrieving text from viewer */
  private int                         fLastOffset;
  /** Pattern to extract lines in JavaCC / JJTree compilation messages */
  final static Pattern                jjLinePattern    = Pattern.compile("^.*", Pattern.MULTILINE);                                                     //$NON-NLS-1$
  /** Pattern to find the Error or Warning or ParseException message in JavaCC / JJTree compilation messages */
  //  final static Pattern                jjPbPattern      = Pattern
  //                                                                .compile("([eE]rror[: ]|[wW]arning[^s]|ParseException:|Encountered\\s\")(.+)");         //$NON-NLS-1$
  final static Pattern                jjPbPattern      = Pattern
                                                                .compile("(^Error:|^Warning:|^Error parsing input|Lexical error|Encountered[: ])(.+)$"); //$NON-NLS-1$
  /** Pattern to find the line and column numbers in a JavaCC / JJTree compilation messages line */
  final static Pattern                jjLineColPattern = Pattern.compile("[lL]ine (\\d+), [cC]olumn (\\d+)");                                           //$NON-NLS-1$
  /** Pattern to find the Info or Warning message in a JTB compilation messages line */
  final static Pattern                jtbPbPattern     = Pattern
                                                                .compile("\\((\\d+)\\)(:  (warning|info|soft error|unexpected program error):  (.*))?"); //$NON-NLS-1$
  /** The platform line separator (should be taken from Eclipse ???) */
  String                              SEP              = System.getProperty("line.separator");

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
   * Clears the console.
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
    // add markers and hyperlinks
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
   * Decodes console output, catches lines reporting problems (warnings / errors), adds hyperlinks and problem
   * markers for them.
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
      IMarker topWarningMarker = null; // the warning marker at the top of the file
      IMarker topErrorMarker = null; // the error marker at the top of the file
      int offset, length; // location of the link in console
      int start = 0; // line number of the message

      if (!aIsJtb) {
        // .jj or .jjt file : we get outputs like :
        // -- messages with line and column numbers
        //  (from JavaCCErrors parse_error, semantic_error & warning methods)
        // Warning: Line 83, Column 13: Encountered LOOKAHEAD(...) at a non-choice location.  This will be ignored.
        // Error: Line 61, Column 5: Undefined lexical token name "HEX_LITERA".
        //  (from JavaCCErrors methods and direct println() calls in LookaheadCalc)
        // Warning: Choice conflict in (...)* construct at line 99, column 3.
        // Warning: Choice conflict involving two expansions at
        //          line 99, column 3 and line 109, column 22 respectively.
        //  (from direct println(ex.toString()) calls in Main after a ParseException or Exception from JavaCCParser)
        // Exception in thread "main" org.javacc.jjtree.TokenMgrError: Lexical error at line 40, column 6.  Encountered: "\r" (13), after : "\"\\t"
        //  (from direct io.getMsg().println(("Error parsing input: " + ex.toString())) calls in JJTree after a ParseException or Exception from JJTreeParser)
        // Error parsing input: org.javacc.jjtree.ParseException: Encountered " "static" "static "" at line 8, column 3.
        // -- messages without line and column numbers (not always at the end of the report)
        // Warning: Lookahead adequacy checking not being performed since option LOOKAHEAD is more than 1.  Set option FORCE_LA_CHECK to true to force checking.
        // Warning: ParseException.java: File is obsolete.  Please rename or delete this file so that a new one can be generated for you.

        // parse line by line to find problems
        // then get the numbers following 'line ' and 'column ' on the line or the next line
        // hyperlinks are added with offset, length in the text and file, line, column references
        // creating multiple markers on the same line does not seem to work (only the first one seems displayed), however
        // - very few cases seem possible for multiple errors on the same line (choice conflict involving two expansions, incoherences with the parser class name),
        //   so showing only one is not a big deal (the solution would be to maintain a map of the lines / markers)
        // - only one case seems to occur for multiple warnings on the same line (warnings not related to a specific line and shown on top of the file)
        //   for which we have set a special processing for updating the (single) marker

        // match line by line
        final Matcher lineMatcher = jjLinePattern.matcher(txt);
        while (lineMatcher.find()) {
          report = lineMatcher.group();
          start = lineMatcher.start();

          // look for problems
          final Matcher jjPbMatcher = jjPbPattern.matcher(report);
          if (!jjPbMatcher.find()) {
            continue;
          }

          // set severity accordingly
          final int severity = (jjPbMatcher.group().indexOf("arning") != -1) ? IMarker.SEVERITY_WARNING //$NON-NLS-1$
                                                                            : IMarker.SEVERITY_ERROR;
          // note the position of the problem in the text
          offset = start + jjPbMatcher.start();
          length = jjPbMatcher.end(1) - jjPbMatcher.start();

          // look for line and column numbers
          Matcher lineColumnMatcher = jjLineColPattern.matcher(report);

          boolean hasNext = false;
          if (!lineColumnMatcher.find()) {
            // if there are no line & column numbers
            if (report.startsWith("Warning: Choice conflict involving")) { //$NON-NLS-1$
              hasNext = true;
              // if a lookahead conflict message involving two expansions, take the line & column numbers in the next line
              lineMatcher.find();
              start = lineMatcher.start();
              final String nextLine = lineMatcher.group();
              lineColumnMatcher = jjLineColPattern.matcher(nextLine);
              if (!lineColumnMatcher.find()) {
                continue;
              }
              // add the next line to the report
              report = report.concat(SEP).concat(nextLine);
            }
            else if ((report.contains("File is obsolete") && aFile.isDerived())) { //$NON-NLS-1$
              // do not take these warnings in generated .jj files, as they should not be there in that case (JavaCC bug ?)
              // and as it produces squiggly lines throughout the file when the file is already opened in a JJEditor
              // (it seems there is something to be done in the Editor / viewer to update the view)
              continue;
            }
            else {
              // create an hyperlink pointing to the beginning of the file
              new JJConsoleHyperlink(fLastOffset + offset, length, aFile, 0, 0);
              IMarker topMarker = severity == IMarker.SEVERITY_WARNING ? topWarningMarker : topErrorMarker;
              // mark the problem at the beginning of the editor
              if (topMarker == null) {
                // create the marker
                topMarker = markProblem(aFile, report, severity, 1);
                if (severity == IMarker.SEVERITY_WARNING) {
                  topWarningMarker = topMarker;
                }
                else {
                  topErrorMarker = topMarker;
                }
              }
              else {
                // update the existing marker
                addProblem(topMarker, report);
              }
              continue;
            }
          }

          // for each line and column group in the message, add a marker and an hyperlink
          do {
            line = Integer.parseInt(lineColumnMatcher.group(1));
            col = Integer.parseInt(lineColumnMatcher.group(2));
            offset = lineColumnMatcher.start();
            length = lineColumnMatcher.end() - offset;

            // replace the .jj file by the .jjt file if it derives from (but not by the .jtb file)
            IFile newFile = aFile;
            try {
              if (aFile.isDerived()) {
                final String from = aFile.getPersistentProperty(QN_GENERATED_FILE);
                final IProject project = aFile.getProject();
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

            // for lookahead conflict message involving two expansions, add also the 2 next lines to the report
            if (hasNext) {
              hasNext = false;
              lineMatcher.find();
              report = report.concat(SEP).concat(lineMatcher.group());
              lineMatcher.find();
              report = report.concat(SEP).concat(lineMatcher.group());
            }

            // add the problem to the editor problems
            markProblem(newFile, report, severity, line);

            // add an hyperlink in the console
            // the first line is 1 for JavaCC and 0 for Eclipse editors
            new JJConsoleHyperlink(fLastOffset + start + offset, length, newFile, line - 1, col - 1);
          } // do this for all occurrences on the line.
          while (lineColumnMatcher.find());
        }
      }
      else {
        // .jtb file : we get outputs like : 
        // new.jtb (406):  warning:  Non initialized user variable 'isTypedef'. May lead to compiler error(s) (specially for 'Token' variables). Check in generated parser.
        // new.jtb (461):  info:  Non "void" BNFProduction. Result type 'boolean' will be changed into 'type_modifiers', and a parser class variable 'jtbrt_type_modifiers' of type 'boolean' will be added to hold the return values.
        // new.jtb (340):  soft error:  Empty BNF expansion in "<production>()", 345
        // new.jtb (234):  unexpected program error:  <exception / throwable message>

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
          // add the problem in the editor problems 
          markProblem(aFile, report, severity, line);
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
   * @return the created marker
   */
  private IMarker markProblem(final IFile aFile, final String aMsg, final int aSeverity, final int aLine) {
    try {
      final IMarker marker = aFile.createMarker(IMarker.PROBLEM);
      final HashMap<String, Comparable<?>> attributes = new HashMap<String, Comparable<?>>(4);
      attributes.put(IMarker.MESSAGE, aMsg);
      attributes.put(IMarker.SEVERITY, new Integer(aSeverity));
      attributes.put(IMarker.LINE_NUMBER, new Integer(aLine));
      marker.setAttributes(attributes);
      return marker;
    } catch (final CoreException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Updates a current marker by adding a new line with a given new message.
   * 
   * @param aMarker the marker to update
   * @param aMsg the message to add
   */
  private void addProblem(final IMarker aMarker, final String aMsg) {
    try {
      aMarker.setAttribute(IMarker.MESSAGE, (((String) aMarker.getAttribute(IMarker.MESSAGE))).concat(SEP)
                                                                                              .concat(aMsg));
    } catch (final CoreException ex) {
      ex.printStackTrace();
    }
  }
}
