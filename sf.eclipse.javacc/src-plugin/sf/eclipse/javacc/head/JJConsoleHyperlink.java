package sf.eclipse.javacc.head;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import sf.eclipse.javacc.base.IJJConstants;

/**
 * Hyperlinks for JJConsole, a simplified version of org.eclipse.ui.forms.widgets.Hyperlink which are
 * underlined but... too complex, for me at least.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJConsoleHyperlink implements IJJConstants {

  // MMa 04/2009 : formatting and javadoc revision ; managed JJEditor / JTBEditor
  // MMa 02/2010 : formatting and javadoc revision

  /** The offset of text to mark */
  private final int                       jOffset;
  /** The length of text to mark */
  private final int                       jLength;
  /** The target of Hyperlink */
  private final IFile                     jFile;
  /** The line number in the target */
  private final int                       jFileLine;
  /** The column number in the target */
  private final int                       jFileCol;
  /** The "hand" cursor reference */
  static Cursor                           sHandCursor;
  /** The "busy" cursor reference */
  static Cursor                           sBusyCursor;
  /** True if mouse has been clicked down */
  static boolean                          sMouseDown;
  /** True if a drag event happened */
  static boolean                          sDragEvent;
  /** The list of hyperlinks */
  static private List<JJConsoleHyperlink> sLinksList = new ArrayList<JJConsoleHyperlink>();
  /** The (single) StyledText console */
  static private StyledText               sStyledText;

  /**
   * Constructs a hyperlink in the StyledText to the specified file.
   * 
   * @param aOffset the offset of text to mark
   * @param aLength the length of text to mark
   * @param aFile the target of Hyperlink
   * @param aLine the line number in the target
   * @param aCol the column number in the target
   */
  public JJConsoleHyperlink(final int aOffset, final int aLength, final IFile aFile, final int aLine,
                            final int aCol) {
    jOffset = aOffset;
    jLength = aLength;
    jFile = aFile;
    jFileLine = aLine;
    jFileCol = aCol;
    addStyleToStyledText();
  }

  /**
   * Notifies when this link is activated.
   */
  public void linkActivated() {
    if (Activator.getDefault() == null) {
      System.out.println("Link activated: " + jFile + " " + jFileLine + " at " + jOffset); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return;
    }
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null && jFile != null) {
        try {
          final String edid = "jtb".equals(jFile.getFileExtension()) ? JTBEDITOR_ID : JJEDITOR_ID; //$NON-NLS-1$
          final IEditorPart editorPart = page.openEditor(new FileEditorInput(jFile), edid, true);
          final ITextEditor textEditor = (ITextEditor) editorPart;
          final IEditorInput input = editorPart.getEditorInput();
          final IDocumentProvider provider = textEditor.getDocumentProvider();
          provider.connect(input);
          final IDocument doc = provider.getDocument(input);
          final int offset = doc.getLineOffset(jFileLine) + jFileCol;
          textEditor.selectAndReveal(offset, 0);
          provider.disconnect(input);
        } catch (final PartInitException e) {
          e.printStackTrace();
        } catch (final BadLocationException e) {
          e.printStackTrace();
        } catch (final CoreException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Sets the styledtext's link (blue) range.
   */
  private void addStyleToStyledText() {
    //  Color color = fStyledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
    final Color color = JFaceColors.getHyperlinkText(sStyledText.getDisplay());
    final StyleRange style = new StyleRange(jOffset, jLength, color, null);
    style.fontStyle = SWT.BOLD;
    style.underline = true; // Only for Eclipse 3.1
    sStyledText.setStyleRange(style);
    // keep a reference to self, in a List used when activated.
    sLinksList.add(this);
  }

  /**
   * @param aOffset the offset in the text
   * @return true is this link is at the given character location, false otherwise
   */
  boolean isLinkAt(final int aOffset) {
    // check if there is a link at the offset
    if (aOffset >= jOffset && aOffset < jOffset + jLength) {
      return true;
    }
    return false;
  }

  /**
   * @param aOffset the offset in the text
   * @return the link if any at the given character location
   */
  static JJConsoleHyperlink getLinkAt(final int aOffset) {
    JJConsoleHyperlink link;
    final Iterator<JJConsoleHyperlink> iter = sLinksList.iterator();
    while (iter.hasNext()) {
      link = iter.next();
      if (link.isLinkAt(aOffset)) {
        return link;
      }
    }
    return null;
  }

  /**
   * Clears all links for the StyledText widget.
   */
  static public void clear() {
    sLinksList.clear();
    sStyledText.setStyleRanges(new StyleRange[0]);
  }

  /**
   * Adds listeners on the StyledText.
   * 
   * @param aStyledText the StyledText to listen to
   */
  static public void setViewer(final StyledText aStyledText) {
    // keep a static reference to used StyledText
    sStyledText = aStyledText;

    // initialize cursors if not already done
    if (sHandCursor == null) {
      sHandCursor = aStyledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
      sBusyCursor = aStyledText.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
    }
    // add Listeners to the Viewer
    // activate link on a mouse click
    aStyledText.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseDown(final MouseEvent e) {
        if (e.button != 1) {
          return;
        }
        sMouseDown = true;
      }

      @Override
      public void mouseUp(final MouseEvent event) {
        sMouseDown = false;
        final StyledText stTxt = (StyledText) event.widget;
        final int offset = stTxt.getCaretOffset();
        final JJConsoleHyperlink link = getLinkAt(offset);
        if (link == null) {
          return;
        }
        if (sDragEvent) {
          sDragEvent = false;
          if (link.isLinkAt(offset)) {
            stTxt.setCursor(sHandCursor);
          }
        }
        else if (link.isLinkAt(offset)) {
          stTxt.setCursor(sBusyCursor);
          if (event.button == 1) {
            link.linkActivated();
            stTxt.setCursor(null);
          }
        }
      }
    });
    // change to hand cursor on a link
    aStyledText.addMouseMoveListener(new MouseMoveListener() {

      @Override
      public void mouseMove(final MouseEvent aEvent) {
        final StyledText stTxt = (StyledText) aEvent.widget;
        // do not change cursor on drag events
        if (sMouseDown) {
          if (!sDragEvent) {
            stTxt.setCursor(null);
          }
          sDragEvent = true;
          return;
        }
        int offset = -1;
        try {
          offset = stTxt.getOffsetAtLocation(new Point(aEvent.x, aEvent.y));
        } catch (final IllegalArgumentException ex) {
          // location is not over a character, leave as -1
        }
        if (offset == -1) {
          stTxt.setCursor(null);
        }
        else if (getLinkAt(offset) != null) {
          stTxt.setCursor(sHandCursor);
        }
        else {
          stTxt.setCursor(null);
        }
      }
    });
  }
}
