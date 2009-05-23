package sf.eclipse.javacc.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.*;

import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

public class JJCompletionProcessor implements IContentAssistProcessor, JavaCCParserTreeConstants {
  /*
   * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
   */
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    // List of Completion proposals to be added
    List<CompletionProposal> proposals= new ArrayList<CompletionProposal>();
    
    // Compute place (start, length) of the completion proposal
    String text= viewer.getDocument().get();
    int start = documentOffset-1;
    while (!Character.isWhitespace(text.charAt(start)))
      start--;
    start++;
    int end = documentOffset;
    String prefix = text.substring(start, end);
    int length = end - start;
    
    // Build list of suggestions
    List<String> suggestions = new ArrayList<String>();
    int cursorPosition = 0;
    // TODO choose the keyword from the context
    // TODO alphabetic sort
    // Add Keywords
    for (String s : JJCodeScanner.fgJJkeywords){
      if (s.toUpperCase().startsWith(prefix.toUpperCase())){
        if (s.equals("options")) { //$NON-NLS-1$
          suggestions.add("options{\n  \n}"); //$NON-NLS-1$
          cursorPosition = 11;
        }
        else if (s.equals("TOKEN")) { //$NON-NLS-1$
          suggestions.add("TOKEN:{\n  \n}"); //$NON-NLS-1$
          cursorPosition = 10;
        }
        else if (s.equals("MORE")) { //$NON-NLS-1$
          suggestions.add("MORE:{\n  \"\"\n}"); //$NON-NLS-1$
          cursorPosition = 10;
        }
        else if (s.equals("SPECIAL_TOKEN")) { //$NON-NLS-1$
          suggestions.add("SPECIAL_TOKEN:{\n  \"\"\n}"); //$NON-NLS-1$
          cursorPosition = 19;
        }        
        else
          suggestions.add(s);
      }
    }
    // Get the JJEditor showing the active document
    JJEditor jjeditor = null;
    IDocument currentDocument= viewer.getDocument();
    IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IEditorReference editorReferences[]= window.getActivePage().getEditorReferences();
    for (int i= 0; i < editorReferences.length; i++) {
      IEditorPart editor= editorReferences[i].getEditor(false); // don't create!
      if (editor instanceof JJEditor) {
        jjeditor = (JJEditor) editor;
        IEditorInput input = jjeditor.getEditorInput();
        IDocument doc = jjeditor.getDocumentProvider().getDocument(input);
        if (currentDocument.equals(doc))
          break; // We got the current JJEditor for the current Document
      }
    }
    // Add Elements (method, token)
    JJElements jjElements = jjeditor.getJJElements();
    for (String s : jjElements.getMap().keySet()){
      int id = jjElements.getNode(s).getId();
      // nodes and methods
      if (s.toUpperCase().startsWith(prefix.toUpperCase())){
        if (id == JJTBNF_PRODUCTION || id == JJTMETHODDECLARATION)
          suggestions.add(s+"()"); //$NON-NLS-1$ 
      }
      // tokens
      if (prefix.startsWith("<")) { //$NON-NLS-1$
        String token = prefix.substring(1);
        if (s.toUpperCase().startsWith(token.toUpperCase())) {
          if (id == JJTREGEXPR_SPEC)
            suggestions.add("<"+s+">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    // Add all suggestions to proposals
    for (Iterator<String> it = suggestions.iterator(); it.hasNext();) {
      String txt = it.next();
      if (txt.length() > 0) {
        if (cursorPosition !=0 )
          proposals.add(new CompletionProposal(txt, start, length, cursorPosition));
        else
          proposals.add(new CompletionProposal(txt, start, length, txt.length()));
      }
    }
    return proposals.toArray(new ICompletionProposal[proposals.size()]);
  }

  /*
   * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
    return null;
  }
  /*
   * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] {' '};
  }
  /*
   * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters() {
    return new char[] {'£'};
  }
  /*
   * @see IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }
  /*
   * @see IContentAssistProcessor#getErrorMessage()
   */
  public String getErrorMessage() {
    return null;
  }
}
