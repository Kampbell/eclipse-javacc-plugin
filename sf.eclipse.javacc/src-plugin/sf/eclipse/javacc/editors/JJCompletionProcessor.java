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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * The JJ content assist processor for completions in java or javacc code (not in java & javadoc comments).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJCompletionProcessor implements IContentAssistProcessor, JavaCCParserTreeConstants {

  // MMa 02/2010 : formatting and javadoc revision

  /**
   * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
   */
  public ICompletionProposal[] computeCompletionProposals(final ITextViewer aViewer, final int aDocOffset) {
    // list of Completion proposals to be added
    final List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

    // compute the completion proposal place (start, length)
    final String text = aViewer.getDocument().get();
    int start = aDocOffset - 1;
    while (!Character.isWhitespace(text.charAt(start))) {
      start--;
    }
    start++;
    final int end = aDocOffset;
    final String prefix = text.substring(start, end);
    final int length = end - start;

    // build the suggestions list
    final List<String> suggestions = new ArrayList<String>();
    int cursorPosition = 0;
    // TODO choose the keyword from the context
    // TODO alphabetic sort
    // add Keywords
    for (final String s : JJCodeScanner.fgJJkeywords) {
      if (s.toUpperCase().startsWith(prefix.toUpperCase())) {
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
        else {
          suggestions.add(s);
        }
      }
    }
    // get the editor showing the active document
    JJEditor jjeditor = null;
    final IDocument currentDocument = aViewer.getDocument();
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final IEditorReference editorReferences[] = window.getActivePage().getEditorReferences();
    for (int i = 0; i < editorReferences.length; i++) {
      final IEditorPart editor = editorReferences[i].getEditor(false); // don't create!
      if (editor instanceof JJEditor) {
        jjeditor = (JJEditor) editor;
        final IEditorInput input = jjeditor.getEditorInput();
        final IDocument doc = jjeditor.getDocumentProvider().getDocument(input);
        if (currentDocument.equals(doc)) {
          // we got the current JJEditor for the current Document
          break;
        }
      }
    }
    if (jjeditor == null) {
      // should not occur
      return null;
    }
    // add Elements (method, token)
    final JJElements jjElements = jjeditor.getJJElements();
    for (final String s : jjElements.getNonIdentifiersMap().keySet()) {
      final int id = jjElements.getNonIdentifierNode(s).getId();
      // nodes and methods
      if (s.toUpperCase().startsWith(prefix.toUpperCase())) {
        if (id == JJTBNF_PRODUCTION || id == JJTMETHODDECLARATION) {
          suggestions.add(s + "()"); //$NON-NLS-1$ 
        }
      }
      // tokens
      if (prefix.startsWith("<")) { //$NON-NLS-1$
        final String token = prefix.substring(1);
        if (s.toUpperCase().startsWith(token.toUpperCase())) {
          if (id == JJTREGEXPR_SPEC) {
            suggestions.add("<" + s + ">"); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    }
    // add all suggestions to proposals
    for (final Iterator<String> it = suggestions.iterator(); it.hasNext();) {
      final String txt = it.next();
      if (txt.length() > 0) {
        if (cursorPosition != 0) {
          proposals.add(new CompletionProposal(txt, start, length, cursorPosition));
        }
        else {
          proposals.add(new CompletionProposal(txt, start, length, txt.length()));
        }
      }
    }
    return proposals.toArray(new ICompletionProposal[proposals.size()]);
  }

  /**
   * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(
                                                         @SuppressWarnings("unused") final ITextViewer aViewer,
                                                         @SuppressWarnings("unused") final int aOffset) {
    return null;
  }

  /**
   * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] {
      ' ' };
  }

  /**
   * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters() {
    return new char[] {
      '£' };
  }

  /**
   * @see IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }

  /**
   * @see IContentAssistProcessor#getErrorMessage()
   */
  public String getErrorMessage() {
    return null;
  }
}
