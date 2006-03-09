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
  static String prefix = "//"; //$NON-NLS-1$

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

      // If partial lines are selected, extend selection
      IRegion endLine = doc.getLineInformation(ts.getEndLine());
      IRegion startLine = doc.getLineInformation(ts.getStartLine());
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset()
          + endLine.getLength() - startLine.getOffset());
      
      // Format Selection given full text
      String endLineDelim = doc.getLegalLineDelimiters()[0];
      String identString = "  ";
   System.out.println("getStartLine() "+ts.getStartLine()+1+" getEndLine() "+ ts.getEndLine()+1);
      formatSelection(doc.get(), endLineDelim, identString,
	  ts.getStartLine()+1, ts.getEndLine()+1, strbuf);
      System.out.println("formated :["+strbuf.toString()+"]");
      
      // The tricky part is to replace only part of the full text 
      // we need to treat full editor text using JavaCC grammar
      // and we have to replace only part of it.
      
//      for (int i = ts.getStartLine(); i <= ts.getEndLine(); i++) {
//	IRegion reg = doc.getLineInformation(i);
//	String line = doc.get(reg.getOffset(), reg.getLength());
//	System.out.println("sel :"+line);
//	strbuf.append(line);
//      }
      

//      int i;
//      String endLineDelim = doc.getLegalLineDelimiters()[0];
//      String line;
//      
//      // For each line, format
//      for (i = ts.getStartLine(); i < ts.getEndLine(); i++) {
//        IRegion reg = doc.getLineInformation(i);
//        line = doc.get(reg.getOffset(), reg.getLength());
//          strbuf.append(prefix);
//          strbuf.append(line);
//          strbuf.append(endLineDelim);
//      }
//      // Last line doesn't need line delimiter
//      IRegion reg = doc.getLineInformation(i);
//      line = doc.get(reg.getOffset(), reg.getLength());
//        strbuf.append("//"); //$NON-NLS-1$
//        strbuf.append(line);
//      
      // Replace the text with the modified version
      doc.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());
      
      // Reselect text... not exactly as JavaEditor... whole text here
      editor.selectAndReveal(startLine.getOffset(), strbuf.length());
    } catch (Exception e) {
      // Should not append
    }
    return;
  }
  
  protected void formatSelection(String txt, String endLineDelim, 
      String identString, int begin, int end, StringBuffer sb) {
    StringReader in = new StringReader(txt);
    JJNode node = JavaCCParser.parse(in);
    in.close();
   
    Token f = node.getFirstToken();
    StringBuffer ident = new StringBuffer();
    
    // states 
    boolean needNewLine = false;
    boolean is = false;
    int lastkind = -1;

    while(f.kind != EOF) {
      // states update
      if (f.kind == _PARSER_BEGIN || f.kind == _PARSER_END)
	needNewLine = true;
      if (f.beginLine >= begin && is == false)
	is = true;
      if (f.endLine > end && is == true)
	is = false;
      if (is)
      System.out.println("line "+f.beginLine+","+f.endLine+" "+f.image);
     
      // update identation for opening brace
      if (lastkind == LBRACE && f.kind != RBRACE ){
	ident.append(identString);
	if(is) sb.append(endLineDelim).append(ident);
      }
      
      // prepend newline and ident after JAVACC keyword
      if ( lastkind == RPAREN && needNewLine) {
 	if(is) sb.append(endLineDelim).append(ident);
 	needNewLine = false;
      }
      if ( f.kind == BIT_OR) {
 	if(is) sb.append(endLineDelim).append(ident);
      }
      if (f.kind == RBRACE && lastkind != SEMICOLON 
	  && lastkind != LBRACE && lastkind != RBRACE
	  && lastkind != BIT_OR) {
	if(is) sb.append(endLineDelim).append(ident);
      }
      // Closing brace delete ident
      if (f.kind == RBRACE && lastkind != LBRACE ) {
	if (ident.length() != 0) {
	  if(is) sb.delete(sb.length()-identString.length(), sb.length());
	}
      }
      
      // prepend space  
      if ( f.kind == ASSIGN 
	  || (f.kind == IDENTIFIER && lastkind == IDENTIFIER)
	  || f.kind == EQ || f.kind == LE || f.kind == GE || f.kind == NE
	  || f.kind == SC_OR|| f.kind == SC_AND || f.kind == BIT_AND 
	  )
	if(is) sb.append(" ");
      
      // the special token(s)
      Token specialtoken = f.specialToken;
      if (specialtoken != null){
	while (specialtoken.specialToken != null)
	  specialtoken = specialtoken.specialToken;
	while (specialtoken != null) {
	  if (specialtoken.kind == SINGLE_LINE_COMMENT 
	      ||specialtoken.kind == FORMAL_COMMENT
	      ||specialtoken.kind == MULTI_LINE_COMMENT) { 
	    if(is) sb.append(specialtoken.toString());
	    if(is) sb.append(endLineDelim);
	  }
	  specialtoken = specialtoken.next;
	}
      }
      
      // THE token
//      strbuf.append("["+f.kind+"]");
      if(is) sb.append(f.toString());
      
      // append newline and ident
      if (f.kind == SEMICOLON ) {
	if(is) sb.append(endLineDelim).append(ident);
      }
      if (f.kind == RBRACE && lastkind != LBRACE ) {
	if (ident.length() != 0) {
	  ident.delete(ident.length()-identString.length(), ident.length());
	}
	if(is) sb.append(endLineDelim).append(ident);
      }

      // append space 
      if (  (f.kind >= ABSTRACT && f.kind <= WHILE )
	  && f.kind != NULL && f.kind != CONTINUE
	  && f.kind != FALSE && f.kind != TRUE
	  )
	if(is) sb.append(" ");
      else if ( f.kind == ASSIGN || f.kind == COMMA 
	  || f.kind == EQ || f.kind == LE || f.kind == GE || f.kind == NE
	  || f.kind == SC_OR || f.kind == SC_AND || f.kind == BIT_AND || f.kind == BIT_OR
	  )
	if(is) sb.append(" ");

      // next token
      lastkind = f.kind;
      f = f.next;
    }   
  }
  
  /**
   * Unit test
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String testFile = new File(".").getCanonicalPath()+"/Divers/test.jjt";
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
//    System.out.println("before>"+sb.toString());
    
    // Here are the arguments
    String txt = sb.toString();
    sb = new StringBuffer();
    String endline = "\n";
    String identString = "\t";

    // Do format
    JJFormat jjf = new JJFormat();
    jjf.formatSelection(txt, endline, identString,
	0, 12 , sb); // nLines
    
    // See what we got
    System.out.println("after>"+sb.toString());
  }
}