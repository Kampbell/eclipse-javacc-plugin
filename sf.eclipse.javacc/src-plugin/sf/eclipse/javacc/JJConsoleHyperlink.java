package sf.eclipse.javacc;

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

/**
 * Hyperlinks for JJConsole, a simplified version of org.eclipse.ui.forms.widgets.Hyperlink which are
 * underlined but... too complex, for me at least.
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJConsoleHyperlink implements IJJConstants {

  // MMa 04/09 : formatting and javadoc revision ; managed JJEditor / JTBEditor

  /** the offset of text to mark */
  private final int   fOffset;
  /** the length of text to mark */
  private final int   fLength;
  /** the target of Hyperlink */
  private final IFile fFile;
  /** the line number in the target */
  private final int   fFileLine;
  /** the column number in the target */
  private final int   fFileCol;

  /**
   * Constructs a hyperlink in the StyledText to the specified file.
   * 
   * @param offset the offset of text to mark
   * @param length the lengty of text to mark
   * @param file the target of Hyperlink
   * @param line the line number in the target
   * @param col the column number in the target
   */
  public JJConsoleHyperlink(final int offset, final int length, final IFile file, final int line,
                            final int col) {
    fOffset = offset;
    fLength = length;
    fFile = file;
    fFileLine = line;
    fFileCol = col;

    addStyleToStyledText();
  }

  /**
   * Notifies when this link is activated.
   */
  public void linkActivated() {
    if (Activator.getDefault() == null) {
      System.out.println("Link activated: " + fFile + " " + fFileLine + " at " + fOffset); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return;
    }
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null && fFile != null) {
        try {
          // TODO here for jtb we should show the generated file editor, not the jtb file editor !
          final String edid = "jtb".equals(fFile.getFileExtension()) ? JTBEDITOR_ID : JJEDITOR_ID; //$NON-NLS-1$
          final IEditorPart editorPart = page.openEditor(new FileEditorInput(fFile), edid, true);
          final ITextEditor textEditor = (ITextEditor) editorPart;
          final IEditorInput input = editorPart.getEditorInput();
          final IDocumentProvider provider = textEditor.getDocumentProvider();
          provider.connect(input);

          final IDocument doc = provider.getDocument(input);
          final int offset = doc.getLineOffset(fFileLine) + fFileCol;
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
    //  Color fg = fStyledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
    final Color fg = JFaceColors.getHyperlinkText(fStyledText.getDisplay());
    final StyleRange style = new StyleRange(fOffset, fLength, fg, null);
    style.fontStyle = SWT.BOLD;
    style.underline = true; // Only for Eclipse 3.1
    fStyledText.setStyleRange(style);

    // keep a reference to self, in a List used when activated.
    linksList.add(this);
  }

  /**
   * @param offset the offset in the text
   * @return true is this link is at the given character location, false otherwise
   */
  boolean isLinkAt(final int offset) {
    // Check if there is a link at the offset
    if (offset >= fOffset && offset < fOffset + fLength) {
      return true;
    }
    return false;
  }

  /*
   * Static references and methods. This class manages all Links.
   */
  /** the "hand" cursor reference */
  static Cursor                           handCursor;
  /** the "busy" cursor reference */
  static Cursor                           busyCursor;
  /** true if mouse has been clicked down */
  static boolean                          mouseDown;
  /** true if a drag event happened */
  static boolean                          dragEvent;
  /** the list of hyperlinks */
  static private List<JJConsoleHyperlink> linksList = new ArrayList<JJConsoleHyperlink>();
  /** the (single) StyledText console */
  static private StyledText               fStyledText;

  /**
   * @param offset the offset in the text
   * @return the link if any at the given character location
   */
  static JJConsoleHyperlink getLinkAt(final int offset) {
    JJConsoleHyperlink link;
    final Iterator<JJConsoleHyperlink> iter = linksList.iterator();
    while (iter.hasNext()) {
      link = iter.next();
      if (link.isLinkAt(offset)) {
        return link;
      }
    }
    return null;
  }

  /**
   * Clears all links for the StyledText widget.
   */
  static public void clear() {
    linksList.clear();
    fStyledText.setStyleRanges(new StyleRange[0]);
  }

  /**
   * Adds listeners on the StyledText.
   * 
   * @param styledText the StyledText to listen to
   */
  static public void setViewer(final StyledText styledText) {
    // Keep a static reference to used StyledText
    fStyledText = styledText;

    // Initialize cursors if not already done
    if (handCursor == null) {
      handCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
      busyCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
    }
    // Add Listeners to the Viewer
    // Activate link on a mouse clic
    styledText.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseDown(final MouseEvent e) {
        if (e.button != 1) {
          return;
        }
        mouseDown = true;
      }

      @Override
      public void mouseUp(final MouseEvent e) {
        mouseDown = false;
        final StyledText stTxt = (StyledText) e.widget;
        final int offset = stTxt.getCaretOffset();
        final JJConsoleHyperlink link = getLinkAt(offset);
        if (link == null) {
          return;
        }
        if (dragEvent) {
          dragEvent = false;
          if (link.isLinkAt(offset)) {
            stTxt.setCursor(handCursor);
          }
        }
        else if (link.isLinkAt(offset)) {
          stTxt.setCursor(busyCursor);
          if (e.button == 1) {
            link.linkActivated();
            stTxt.setCursor(null);
          }
        }
      }
    });
    // Change to hand cursor on a link
    styledText.addMouseMoveListener(new MouseMoveListener() {

      public void mouseMove(final MouseEvent e) {
        final StyledText stTxt = (StyledText) e.widget;
        // Do not change cursor on drag events
        if (mouseDown) {
          if (!dragEvent) {
            stTxt.setCursor(null);
          }
          dragEvent = true;
          return;
        }
        int offset = -1;
        try {
          offset = stTxt.getOffsetAtLocation(new Point(e.x, e.y));
        } catch (final IllegalArgumentException ex) {
          // location is not over a character, leave as -1
        }
        if (offset == -1) {
          stTxt.setCursor(null);
        }
        else if (getLinkAt(offset) != null) {
          stTxt.setCursor(handCursor);
        }
        else {
          stTxt.setCursor(null);
        }
      }
    });
  }
}
