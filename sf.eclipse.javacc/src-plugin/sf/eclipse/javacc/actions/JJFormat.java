package sf.eclipse.javacc.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.JavaCCParserConstants;
import sf.eclipse.javacc.parser.Token;

/**
 * Format action
 * Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.popupMenus">
 *  for popup menu on Editor
 * <extension point="org.eclipse.ui.editorActions"> 
 *  for key binding
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class JJFormat implements IEditorActionDelegate, JavaCCParserConstants {
  static JJEditor editor;
  static IDocument doc;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
    doc = editor.getDocument();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // not used
  }
  
  /**
   * Perform Format
   */
  public void run(IAction action) {
    if (editor == null)
      return;

    ISelection selection = editor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection))
      return;

    ITextSelection ts = (ITextSelection) selection;
    if (ts.getStartLine() < 0 || ts.getEndLine() < 0)
      return;

    try {      
      // Buffer for replacement text
      StringBuffer strbuf = new StringBuffer();

      // if No selection... treat full text
      if (ts.getLength() == 0)
	ts = new TextSelection(doc, 0, doc.getLength());

      // If partial lines are selected, extend selection
      IRegion endLine = doc.getLineInformation(ts.getEndLine());
      IRegion startLine = doc.getLineInformation(ts.getStartLine());
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset()
          + endLine.getLength() - startLine.getOffset());

      // Format Selection given full text
      // The tricky part is to replace only part of the full text 
      // we need to treat full editor text using JavaCC grammar
      // and we have to replace only part of it.
      String endLineDelim = doc.getLegalLineDelimiters()[0];
      String identString = "  "; // TODO grep Legal ident string //$NON-NLS-1$
      if (formatSelection(doc.get(), endLineDelim, identString,
          ts.getStartLine() + 1, ts.getEndLine() + 1, strbuf) == true) {
      
      // Replace the text with the modified version
      doc.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());
      }
      // Reselect text... not exactly as JavaEditor... whole text here
//    editor.selectAndReveal(startLine.getOffset(), strbuf.length());
    } catch (Exception e) {
      // Should not append
    }
    return;
  }
  
  protected boolean formatSelection(String txt, String endLineDelim,
      String identString, int begin, int end, StringBuffer sb) {
    // Parse the full text, retain only the chain of Tokens
    StringReader in = new StringReader(txt);
    JJNode node = JavaCCParser.parse(in);
    in.close();
    if (node.getFirstToken().kind == 0) {
      return false;
    }
    Token f = node.getFirstToken();
    StringBuffer ident = new StringBuffer();

    // states
    boolean needNewLine = false;
    int lastkind = -1;

    while (f.kind != EOF) {
      // Special rule, for these keyword add a new line after ')'
      if (f.kind == _PARSER_BEGIN || f.kind == _PARSER_END)
        needNewLine = true;
      if (f.beginLine < begin || f.endLine > end) {
        // next token
        lastkind = f.kind;
        f = f.next;
        continue;
      }

      // update identation for opening brace
      if (lastkind == LBRACE && f.kind != RBRACE) {
        ident.append(identString);
        sb.append(endLineDelim).append(ident);
      }

      // prepend newline and ident after JAVACC keyword
      if (lastkind == RPAREN && needNewLine) {
        sb.append(endLineDelim).append(ident);
        needNewLine = false;
      }
      if (f.kind == BIT_OR && lastkind != RBRACE) {
        sb.append(endLineDelim).append(ident);
      }
      if (f.kind == RBRACE && lastkind != SEMICOLON && lastkind != LBRACE
          && lastkind != RBRACE && lastkind != BIT_OR) {
        sb.append(endLineDelim).append(ident);
      }
      // Closing brace delete ident
      if (f.kind == RBRACE && lastkind != LBRACE) {
        if (ident.length() != 0) {
          sb.delete(sb.length() - identString.length(), sb.length());
        }
      }

      // prepend space
      if (f.kind == ASSIGN || (f.kind == IDENTIFIER && lastkind == IDENTIFIER)
          || f.kind == EQ || f.kind == LE || f.kind == GE || f.kind == NE
          || f.kind == SC_OR || f.kind == SC_AND || f.kind == BIT_AND
          || f.kind == INSTANCEOF || f.kind == EXTENDS)
        sb.append(" "); //$NON-NLS-1$

      // the special token(s)
      Token st = f.specialToken;
      if (st != null) {
        // Rewind to the first
        while (st.specialToken != null)
          st = st.specialToken;
        // Examine each
        while (st != null) {
          if (st.kind == SINGLE_LINE_COMMENT || st.kind == FORMAL_COMMENT
              || st.kind == MULTI_LINE_COMMENT) {
            if (st.beginLine >= begin) {
              sb.append(st.toString());
            }
          }
          if (st.kind == FORMAL_COMMENT || st.kind == MULTI_LINE_COMMENT)
            sb.append(endLineDelim);
          st = st.next;
        }
      }

      // THE token
      //sb.append("["+f.kind+"]");
      sb.append(f.toString());

      // append newline and ident
      if (f.kind == SEMICOLON) {
        sb.append(endLineDelim).append(ident);
      }
      if (f.kind == RBRACE && lastkind != LBRACE) {
        if (ident.length() != 0) {
          ident.delete(ident.length() - identString.length(), ident.length());
        }
        sb.append(endLineDelim).append(ident);
      }

      // append space
      if ((f.kind >= ABSTRACT && f.kind <= WHILE) && f.kind != NULL
          && f.kind != CONTINUE && f.kind != FALSE && f.kind != TRUE)
        sb.append(" "); //$NON-NLS-1$
      else if (f.kind == ASSIGN || f.kind == COMMA || f.kind == EQ
          || f.kind == LE || f.kind == GE || f.kind == NE || f.kind == SC_OR
          || f.kind == SC_AND || f.kind == BIT_AND || f.kind == BIT_OR
          || f.kind == _JAVACODE || f.kind == INSTANCEOF)
        sb.append(" "); //$NON-NLS-1$

      // next token
      lastkind = f.kind;
      f = f.next;
    }
    return true;
  }
  
  /**
   * Unit test
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String testFile = new File(".").getCanonicalPath()+"/Divers/test.jjt"; //$NON-NLS-1$ //$NON-NLS-2$
    StringBuffer sb = new StringBuffer();
    
    // Read the test file to format
    File f = new File(testFile);
    FileInputStream reader = new FileInputStream(f);
    int c;
    int nLines = 0;
    while ((c = reader.read()) != -1) {
      sb.append((char) c);
      if ((char) c == '\n')
	nLines++;
    }
    
    // Here are the arguments
    String txt = sb.toString();
    sb = new StringBuffer();
    String endline = "\n"; //$NON-NLS-1$
    String identString = "~"; //$NON-NLS-1$

    // Do format
    JJFormat jjf = new JJFormat();
    jjf.formatSelection(txt, endline, identString, 0, nLines , sb); // nLines
    
    // See what we got
    System.out.println("after>"+sb.toString()); //$NON-NLS-1$
  }
}