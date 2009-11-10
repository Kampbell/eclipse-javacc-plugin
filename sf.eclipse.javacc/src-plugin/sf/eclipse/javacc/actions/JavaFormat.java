package sf.eclipse.javacc.actions;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class JavaFormat {
  public static void main(String[] args) throws Exception {
    // source : a String containing your source code
    String source = "CodeFormatter \nformatter=ToolFactory.createCodeFormatter\n"+
    "(DefaultCodeFormatterConstants.getJavaConventionsSettings());IDocument\n"+
    "document             = new Document  (  source);"; 
    CodeFormatter formatter = ToolFactory
                   .createCodeFormatter(DefaultCodeFormatterConstants
                           .getJavaConventionsSettings());
    IDocument document = new Document(source);
    TextEdit textEdit = formatter.format(CodeFormatter.K_UNKNOWN, source, 0, source.length(), 0, null);
    textEdit.apply(document);
    String formattedSource = document.get();
    
    System.out.println("Avant : \n"+source);
    System.out.println("Après : \n"+formattedSource);
  }
}
