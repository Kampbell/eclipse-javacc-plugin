package sf.eclipse.javacc.editors.old;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * Test class to call the Eclipse formatter.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class UnusedTestJavaFormat {

  // MMa 11/2014 : renamed

  /**
   * Standard main.
   * 
   * @param aArgs - command line arguments
   * @throws BadLocationException - thrown by {@link org.eclipse.text.edits.TextEdit#apply(IDocument)}
   * @throws MalformedTreeException - thrown by {@link org.eclipse.text.edits.TextEdit#apply(IDocument)}
   */
  public static void main(final String[] aArgs) throws MalformedTreeException, BadLocationException {
    // source : a String containing your source code
    final String source = "CodeFormatter \nformatter=ToolFactory.createCodeFormatter\n" //$NON-NLS-1$
                          + "(DefaultCodeFormatterConstants.getJavaConventionsSettings());IDocument\n" //$NON-NLS-1$
                          + "document             = new Document  (  source);"; //$NON-NLS-1$
    final CodeFormatter formatter = ToolFactory.createCodeFormatter(DefaultCodeFormatterConstants.getJavaConventionsSettings());
    final IDocument document = new Document(source);
    final TextEdit textEdit = formatter.format(CodeFormatter.K_UNKNOWN, source, 0, source.length(), 0, null);
    textEdit.apply(document);
    final String formattedSource = document.get();

    System.out.println("Avant : \n" + source); //$NON-NLS-1$
    System.out.println("Après : \n" + formattedSource); //$NON-NLS-1$
  }
}
