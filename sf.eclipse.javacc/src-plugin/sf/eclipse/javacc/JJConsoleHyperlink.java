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
//import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Hyperlinks for JJConsole, a simplified version of
 * org.eclipse.ui.forms.widgets.Hyperlink 
 * which are underlined but... too complex, for me at least.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJConsoleHyperlink implements IJJConstants { 
  private int fOffset;
  private int fLength;
  private IFile fFile;
  private int fFileLine;
  private int fFileCol;
 
  /**
   * Constructs a hyperlink in the StyledText to the specified file.
   * @param styleText where this link will be
   * @param offset of text to mark
   * @param length of text to mark
   * @param file the target of Hyperlink
   * @param line number in the target
   * @param col number in the target 
   */
  public JJConsoleHyperlink(int offset, int length, IFile file, int line, int col) {
    fOffset = offset;
    fLength = length;
    fFile = file;
    fFileLine = line;
    fFileCol = col;
	addStyleToStyledText();
  }
  
  /**
   * Notification when this link is activated
   */
  public void linkActivated() {
    if (Activator.getDefault() == null){
      System.out.println("Link activated: "+fFile+" "+fFileLine+" at "+fOffset); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return;
    }
    IWorkbenchWindow window = Activator.getDefault().getWorkbench()
        .getActiveWorkbenchWindow();
    if (window != null) {
      IWorkbenchPage page = window.getActivePage();
      if (page != null && fFile != null) 
        try {
          IEditorPart editorPart = page.openEditor(new FileEditorInput(fFile), EDITOR_ID, true);
          ITextEditor textEditor = (ITextEditor) editorPart;
          IEditorInput input = editorPart.getEditorInput();
          IDocumentProvider provider = textEditor.getDocumentProvider();
          provider.connect(input);
          
          IDocument doc = provider.getDocument(input);
          int offset = doc.getLineOffset(fFileLine) +fFileCol;
          textEditor.selectAndReveal(offset, 0);        
          provider.disconnect(input);
        } catch (PartInitException e) {
          e.printStackTrace();
        } catch (BadLocationException e) {
          e.printStackTrace();
        } catch (CoreException e) {
          e.printStackTrace();
        }
    }
  }

  /**
   * Sets the styledtext's link (blue) ranges
   * @param styledText
   */
  private void addStyleToStyledText() {
//  Color fg = fStyledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
    Color fg = JFaceColors.getHyperlinkText(fStyledText.getDisplay());
    StyleRange style = new StyleRange(fOffset, fLength, fg, null);
    style.fontStyle = SWT.BOLD;
    style.underline = true; // Only for Eclipse 3.1
    fStyledText.setStyleRange(style);
    
    // keep a reference to self, in a List used when activated.
    linksList.add(this);
  }
  
  /**
   * Returns true is this link is at the given character location
   * @param offset in the text
   */
  boolean isLinkAt(int offset) {
    // Check if there is a link at the offset
    if (offset >= fOffset && offset < fOffset + fLength) {
      return true;
    }
    return false;
  }

  /**
   * Static references
   * This class manages all Links
   */
  static Cursor handCursor;
  static Cursor busyCursor;
  static boolean mouseDown;
  static boolean dragEvent;
  static private List linksList = new ArrayList();
  // Only One Static StyledText Console
  static private StyledText fStyledText; 

  /**
   * Returns the link if any at the given character location
   * @param offset in the text
   */
  static JJConsoleHyperlink getLinkAt(int offset) {
    JJConsoleHyperlink link;
    Iterator iter = linksList.iterator();
    while(iter.hasNext()){
      link = (JJConsoleHyperlink) iter.next();
      if (link.isLinkAt(offset))
        return link;
    }
    return null;
  }

  /**
   * Clear all links for the StyledText widget
   * @param offset in the text
   */
  static public void clear() {
    linksList.clear();
    fStyledText.setStyleRanges(new StyleRange[0]);
  }
  
  /**
   * Static help method to add listeners on the StyledText  
   * @param styledText
   */
  static public void setViewer(StyledText styledText) {
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
      public void mouseDown(MouseEvent e) {
        if (e.button != 1) 
          return;
        mouseDown = true;
      }
      public void mouseUp(MouseEvent e) {
        mouseDown = false;
        StyledText styledText = (StyledText)e.widget;
        int offset = styledText.getCaretOffset();
        JJConsoleHyperlink link = getLinkAt(offset);
        if (link == null) return;
        if (dragEvent) {
          dragEvent = false;
          if (link.isLinkAt(offset)) {
            styledText.setCursor(handCursor);
          }
        } else if (link.isLinkAt(offset)) {		
          styledText.setCursor(busyCursor);
          if (e.button == 1) {
            link.linkActivated();
            styledText.setCursor(null);
          }
        }
      }
    });
    // Change to hand cursor on a link
    styledText.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        StyledText styledText = (StyledText)e.widget;
        // Do not change cursor on drag events
        if (mouseDown) {
          if (!dragEvent) {
            styledText.setCursor(null);
          }
          dragEvent = true;
          return;
        }
        int offset = -1;
        try {
          offset = styledText.getOffsetAtLocation(new Point(e.x, e.y));
        } catch (IllegalArgumentException ex) {
          // location is not over a character, leave as -1
        }
        if (offset == -1)
          styledText.setCursor(null);
        else if (getLinkAt(offset) != null) 
          styledText.setCursor(handCursor);
        else 
          styledText.setCursor(null);
      }
    });
  }
}

