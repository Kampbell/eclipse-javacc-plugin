package sf.eclipse.javacc.head;

import static sf.eclipse.javacc.base.IConstants.GEN_FILE_QN;
import static sf.eclipse.javacc.base.IConstants.JJ_MARKER;
import static sf.eclipse.javacc.base.IConstants.LS;
import static sf.eclipse.javacc.preferences.IPrefConstants.P_CONSOLE_COMMAND;
import static sf.eclipse.javacc.preferences.IPrefConstants.P_CONSOLE_COMMAND_ATR;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.IConsole;

/**
 * Console for JavaCC output for normal usage (ie non headless builds).<br>
 * JavaCC / JTB output is parsed to add Problems for errors and warnings, so we don't really need a console...<br>
 * But sometimes it's reassuring to read "Parser generated successfully".<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.views"><br>
 * Since 1.3 this console is used for reporting errors, so must be.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class ConsoleView extends ViewPart implements IConsole {

  // MMa 02/2010 : formatting and javadoc revision ; fixed issue for JTB problems reporting
  // MMa 03/2010 : change on QN_GENERATED_FILE for bug 2965665 fix ;
  //               change on problems finding, markers & hyperlinks reporting
  // BF  06/2012 : added missing NON-NLS tag, suppress unused warning for ConsoleHyperlink in markErrors
  // BF  06/2012 : added use of preference color and attribute for console commands
  // MMa 10/2012 : added column numbers to JTB messages, managed multiple messages on the same line ;
  //               added (JavaCC) parsing problems in JTB hyperlink reporting ;
  //               fixed multiple markers hashtable for multiple files problem ; renamed
  // MMa 10/2014 : fixed markers deletion
  // MMa 11/2014 : enhanced printing methods ; some renamings ; modified some modifiers
  // MMa 01/2015 : added method for displaying output ; added dispose method

  // TODO check JJDoc problems handling

  /** The preference store */
  private final IPreferenceStore             jStore          = AbstractActivator.getDefault()
                                                                                .getPreferenceStore();

  /** The viewer control */
  StyledText                                 jStyledText;

  /** The console command color */
  Color                                      jCommandColor   = null;

  /** The print stream */
  final ByteArrayOutputStream                jBaos;

  /** The last parsed position, from where to start when retrieving text from viewer */
  private int                                jLastOffset;

  /** Pattern to extract lines in JavaCC / JJTree compilation messages */
  private final static Pattern               sLinePattern    = Pattern.compile("^.*", Pattern.MULTILINE);                                                         //$NON-NLS-1$

  /** Pattern to find the Error or Warning or ParseException message in JavaCC / JJTree compilation messages */
  private final static Pattern               sJjPbPattern    = Pattern.compile("(^Error:|^Warning:|^Error parsing input|Lexical error|Encountered[: ])(.+)$");    //$NON-NLS-1$

  /** Pattern to find the line and column numbers in a JavaCC / JJTree compilation messages line */
  private final static Pattern               sLineColPattern = Pattern.compile("[lL]ine (\\d+), [cC]olumn (\\d+)");                                               //$NON-NLS-1$

  /** Pattern to find the Info or Warning or Error message in a JTB compilation messages line */
  private final static Pattern               sJtbPbPattern   = Pattern.compile("\\((\\d+),(\\d+)\\):  (warning|info|soft error|unexpected program error):  (.*)"); //$NON-NLS-1$

  /**
   * Table of tables to manage multiple messages on the same line for the different files : key = line, val =
   * marker
   */
  private Map<String, Map<Integer, IMarker>> jMarkersHT;

  /** The console PrintStream (one for each thread) */
  private final ThreadLocal<PrintStream>     jPrintStream;

  /**
   * Standard constructor. Allocates the print stream.
   */
  public ConsoleView() {
    jBaos = new ByteArrayOutputStream(1024);
    jPrintStream = new ThreadLocal<PrintStream>() {

      @Override
      public PrintStream initialValue() {
        return new PrintStream(jBaos);
      }
    };

    // 2 files (.jjt + .jj or .jtb + .jj)
    jMarkersHT = new HashMap<String, Map<Integer, IMarker>>(2, 1);
  }

  /**
   * Creates the SWT controls for this workbench part. Called by Workbench to initialize the
   * org.eclipse.ui.views extension TextConsoleViewer(). Does most of the job.
   * <p>
   * * {@inheritDoc}
   */
  @Override
  public void createPartControl(final Composite aParent) {
    jStyledText = new StyledText(aParent, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
    jStyledText.setEditable(false);
    jStyledText.setDoubleClickEnabled(true);
    ConsoleHyperlink.setViewer(jStyledText);
    jStyledText.addDisposeListener(new DisposeListener() {

      /** {@inheritDoc} */
      @Override
      public void widgetDisposed(@SuppressWarnings("unused") final DisposeEvent e) {
        if (jCommandColor != null) {
          jCommandColor.dispose();
          jCommandColor = null;
        }

      }
    });

    // add a "Clear console" button on the toolbar
    final Action clear = new Action(AbstractActivator.getMsg("Console.Clear")) { //$NON-NLS-1$

      /** {@inheritDoc} */
      @Override
      public void run() {
        clear();
      }
    };
    final ImageDescriptor desc = AbstractActivator.getImageDescriptor("jj_clear_co.gif"); //$NON-NLS-1$
    clear.setImageDescriptor(desc);
    clear.setToolTipText(AbstractActivator.getMsg("Console.Clear_console")); //$NON-NLS-1$

    // "Copy" action on menu
    final Action copy = new Action(AbstractActivator.getMsg("Console.Copy")) { //$NON-NLS-1$

      /** {@inheritDoc} */
      @Override
      public void run() {
        jStyledText.copy();
      }
    };

    // add on toolbar
    final IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(clear);

    // add context menu
    final MenuManager menuMgr = new MenuManager();
    final Menu popup = menuMgr.createContextMenu(jStyledText);
    menuMgr.add(clear);
    menuMgr.add(copy);
    jStyledText.setMenu(popup);
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    getPrintStream().close();
  }

  /** {@inheritDoc} */
  @Override
  public void setFocus() {
    jStyledText.setFocus();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    if (Thread.currentThread() != jStyledText.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {

        /** {@inheritDoc} */
        @Override
        public void run() {
          clear();
        }
      });
    }
    else {
      jStyledText.setText(""); //$NON-NLS-1$
      if (jCommandColor != null) {
        jCommandColor.dispose();
        jCommandColor = null;
      }
      ConsoleHyperlink.clear();
      jLastOffset = 0;
    }
  }

  /** {@inheritDoc} */
  @Override
  public PrintStream getPrintStream() {
    return jPrintStream.get();
  }

  /** {@inheritDoc} */
  @Override
  public void print(final String aStr, final boolean aCmdFlag) {
    addText(aStr, aCmdFlag);
  }

  /** {@inheritDoc} */
  @Override
  public void println(final String aStr, final boolean aCmdFlag) {
    addText(aStr + LS, aCmdFlag);
  }

  /** {@inheritDoc} */
  @Override
  public void println() {
    addText(LS, false);
  }

  /** {@inheritDoc} */
  @Override
  public String fmtTS() {
    final Calendar calobj = Calendar.getInstance();
    return DF.format(calobj.getTime());
  }

  /** A date format */
  static final DateFormat DF = new SimpleDateFormat("(@ dd/MM/yyyy HH:mm:ss)"); //$NON-NLS-1$

  /** {@inheritDoc} */
  @Override
  public void displayOutput() {
    addText(jBaos.toString(), false);
    jBaos.reset();
    println();
  }

  /** {@inheritDoc} */
  @Override
  public void processReport(final IFile aFile, final boolean aIsJtb) {
    displayOutput();
    // add markers and hyperlinks
    processConsoleOutput(aFile, aIsJtb);
  }

  /**
   * Adds this text to the Console.
   * 
   * @param aTxt - the text to add
   * @param aIsConsoleCommand - if the text is to be displayed using the console command color preference
   */
  void addText(final String aTxt, final boolean aIsConsoleCommand) {
    // test before updating the viewer
    if (Thread.currentThread() != jStyledText.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {

        /** {@inheritDoc} */
        @Override
        public void run() {
          addText(aTxt, aIsConsoleCommand);
        }
      });
    }
    // update the viewer
    else {
      final int offset = jStyledText.getCharCount();
      jStyledText.append(aTxt);
      if (aIsConsoleCommand) {
        if (jCommandColor == null) {
          jCommandColor = new Color(Display.getCurrent(), PreferenceConverter.getColor(jStore,
                                                                                       P_CONSOLE_COMMAND));
        }
        final StyleRange style = new StyleRange(offset, aTxt.length() - 1, jCommandColor, null,
                                                jStore.getInt(P_CONSOLE_COMMAND_ATR));
        jStyledText.setStyleRange(style);
      }
      // scroll to end
      jStyledText.setCaretOffset(jStyledText.getCharCount());
      jStyledText.showSelection();
    }
  }

  /**
   * Decodes console output, catches lines reporting problems (infos / warnings / errors), adds hyperlinks and
   * markers for them.
   * 
   * @param aFile - the file to report on
   * @param aIsJtb - true if file is a JTB one, false otherwise
   */
  void processConsoleOutput(final IFile aFile, final boolean aIsJtb) {
    // test before retrieving text from the viewer
    if (Thread.currentThread() != jStyledText.getDisplay().getThread()) {
      Display.getDefault().asyncExec(new Runnable() {

        /** {@inheritDoc} */
        @Override
        public void run() {
          processConsoleOutput(aFile, aIsJtb);
        }
      });
    }
    else {
      final String fn = aFile.getName();
      Map<Integer, IMarker> fmht = jMarkersHT.get(fn);
      if (fmht != null) {
        // delete current markers
        final Collection<IMarker> currMarkers = fmht.values();
        for (final IMarker mark : currMarkers) {
          try {
            mark.delete();
          } catch (final CoreException e) {
            // nothing to do
            AbstractActivator.logBug(e);
          }
        }
        fmht.clear();
      }
      else {
        fmht = new HashMap<Integer, IMarker>(4, 1);
        jMarkersHT.put(fn, fmht);
      }
      // get the text from the console
      final StyledTextContent c = jStyledText.getContent();
      final int count = jStyledText.getCharCount();
      final String txt = c.getTextRange(jLastOffset, count - jLastOffset);

      String report; // the message for the marker
      int line, col; // location of the faulty text in editor
      IMarker topWarningMarker = null; // the warning marker at the top of the file
      IMarker topErrorMarker = null; // the error marker at the top of the file
      int offset, length; // location of the link in console
      int start = 0; // line number of the message

      /*
       *  .jj or .jjt file or .jtb file for the parser phase : we get outputs like :
       */
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
      final Matcher lineMatcher = sLinePattern.matcher(txt);
      while (lineMatcher.find()) {
        report = lineMatcher.group();
        start = lineMatcher.start();

        // look for problems
        final Matcher jjPbMatcher = sJjPbPattern.matcher(report);
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
        Matcher lineColumnMatcher = sLineColPattern.matcher(report);

        boolean hasNext = false;
        if (!lineColumnMatcher.find()) {
          // if there are no line & column numbers
          if (report.startsWith("Warning: Choice conflict involving")) { //$NON-NLS-1$
            hasNext = true;
            // if a lookahead conflict message involving two expansions, take the line & column numbers in the next line
            lineMatcher.find();
            start = lineMatcher.start();
            final String nextLine = lineMatcher.group();
            lineColumnMatcher = sLineColPattern.matcher(nextLine);
            if (!lineColumnMatcher.find()) {
              continue;
            }
            // add the next line to the report
            report = report.concat(LS).concat(nextLine);
          }
          else if ((report.contains("File is obsolete") && aFile.isDerived())) { //$NON-NLS-1$
            // do not take these warnings in generated .jj files, as they should not be there in that case (JavaCC bug ?)
            // and as it produces squiggly lines throughout the file when the file is already opened in a JJEditor
            // (it seems there is something to be done in the Editor / viewer to update the view)
            continue;
          }
          else if ((report.contains(":  Encountered error(s) during parsing."))) { //$NON-NLS-1$
            // do not take this last message
            continue;
          }
          else {
            // create an hyperlink pointing to the beginning of the file
            @SuppressWarnings("unused")
            final ConsoleHyperlink chl = new ConsoleHyperlink(jLastOffset + offset, length, aFile, 0, 0);
            IMarker topMarker = (severity == IMarker.SEVERITY_WARNING ? topWarningMarker : topErrorMarker);
            // mark the problem at the beginning of the editor
            if (topMarker == null) {
              // create the marker
              topMarker = addMarker(aFile, report, severity, 1);
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
              final String from = aFile.getPersistentProperty(GEN_FILE_QN);
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
            AbstractActivator.logBug(e);
          }

          // for lookahead conflict message involving two expansions, add also the 2 next lines to the report
          if (hasNext) {
            hasNext = false;
            lineMatcher.find();
            report = report.concat(LS).concat(lineMatcher.group());
            lineMatcher.find();
            report = report.concat(LS).concat(lineMatcher.group());
          }

          // add the problem to the editor problems
          addMarker(newFile, report, severity, line);

          // add an hyperlink in the console
          // the first line is 1 for JavaCC and 0 for Eclipse editors
          @SuppressWarnings("unused")
          final ConsoleHyperlink chl = new ConsoleHyperlink(jLastOffset + start + offset, length, newFile,
                                                            line - 1, col - 1);
        } // do this for all occurrences on the line
        while (lineColumnMatcher.find());
      }

      if (aIsJtb) {
        /*
         *  .jtb file for the pure JTB phase : we get in the console outputs like :
         */
        // new.jtb (406,3):  warning:  Non initialized user variable 'isTypedef'. May lead to compiler error(s) (specially for 'Token' variables). Check in generated parser.
        // new.jtb (461,2):  info:  Non "void" BNFProduction. Result type 'boolean' will be changed into 'type_modifiers', and a parser class variable 'jtbrt_type_modifiers' of type 'boolean' will be added to hold the return values.
        // new.jtb (340,3):  soft error:  Empty BNF expansion in "<production>()", 345
        // new.jtb (234,8):  unexpected program error:  <exception / throwable message>

        final Matcher jtbPbMatcher = sJtbPbPattern.matcher(txt);
        while (jtbPbMatcher.find()) {
          report = jtbPbMatcher.group();
          final String sevStr = jtbPbMatcher.group(3);
          final int severity = "info".equals(sevStr) ? IMarker.SEVERITY_INFO //$NON-NLS-1$
                                                    : "warning".equals(sevStr) ? IMarker.SEVERITY_WARNING //$NON-NLS-1$
                                                                              : IMarker.SEVERITY_ERROR;

          line = Integer.parseInt(jtbPbMatcher.group(1));
          col = Integer.parseInt(jtbPbMatcher.group(2));
          offset = jtbPbMatcher.start(0);
          // show the hyperlink only up to "info" or "warning" or "error" to increase Console readability
          length = jtbPbMatcher.end(3) - offset;
          // add the problem in the editor problems
          addMarker(aFile, report, severity, line);
          // the first line or column is 1 for JTB and 0 for Eclipse editors
          @SuppressWarnings("unused")
          final ConsoleHyperlink chl = new ConsoleHyperlink(jLastOffset + offset, length, aFile, line - 1,
                                                            col - 1);
        }
      }

      // next time the text shall be parsed from lastOffset
      jLastOffset = count;
    }
  }

  /**
   * Add a marker to signal a problem. Hover tips are managed by SourceViewerConfiguration.
   * 
   * @param aFile - the file to report on
   * @param aMsg - the marker message
   * @param aSeverity - the marker severity
   * @param aLine - the marker line number
   * @return the created marker
   */
  private IMarker addMarker(final IFile aFile, final String aMsg, final int aSeverity, final int aLine) {
    try {
      final Integer line = Integer.valueOf(aLine);
      final Map<Integer, IMarker> fmht = jMarkersHT.get(aFile.getName());
      final IMarker oldMarker = fmht.get(line);
      if (oldMarker == null) {
        // no marker already on this line
        final IMarker newMarker = aFile.createMarker(JJ_MARKER);
        final Map<String, Comparable<?>> attributes = new HashMap<String, Comparable<?>>(4, 1);
        attributes.put(IMarker.MESSAGE, aMsg);
        attributes.put(IMarker.SEVERITY, new Integer(aSeverity));
        attributes.put(IMarker.LINE_NUMBER, new Integer(aLine));
        newMarker.setAttributes(attributes);
        fmht.put(line, newMarker);
        return newMarker;
      }
      // marker already on this line ; assumes that the new severity is not higher than the old one
      final String msg = (((String) oldMarker.getAttribute(IMarker.MESSAGE))).concat(LS).concat(aMsg);
      oldMarker.setAttribute(IMarker.MESSAGE, msg);
      return oldMarker;
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
      return null;
    }
  }

  /**
   * Updates a current marker by adding a new line with a given new message.
   * 
   * @param aMarker - the marker to update
   * @param aMsg - the message to add
   */
  private static void addProblem(final IMarker aMarker, final String aMsg) {
    try {
      final String msg = (((String) aMarker.getAttribute(IMarker.MESSAGE))).concat(LS).concat(aMsg);
      aMarker.setAttribute(IMarker.MESSAGE, msg);
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
    }
  }
}
