package sf.eclipse.javacc.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class JJCompletionProcessor implements IContentAssistProcessor {
  /*
   * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
   */
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    List<CompletionProposal> proposals= new ArrayList<CompletionProposal>();

    String text= viewer.getDocument().get();
    int start = documentOffset-1;
    while (!Character.isWhitespace(text.charAt(start)))
      start--;
    start++;
    
    int end = documentOffset;
    String prefix = text.substring(start, end);
    int length = end - start;
    
    List<String> suggestions = new ArrayList<String>();
    // TODO choose the keyword from the context
    // TODO alphabetic sort
    for (String s : JJCodeScanner.fgJJkeywords){
      if (s.startsWith(prefix))
        suggestions.add(s);
    }
    for (Iterator<String> it= suggestions.iterator(); it.hasNext();) {
      String txt = it.next();
      if (txt.length() > 0)
        proposals.add(new CompletionProposal(txt, start, length, txt.length()));
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
    return new char[] {'�'};
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