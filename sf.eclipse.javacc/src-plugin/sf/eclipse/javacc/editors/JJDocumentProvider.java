package sf.eclipse.javacc.editors;

import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * Manages the document partitioning.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJDocumentProvider extends FileDocumentProvider implements IJavaPartitions {

  // MMa 12/2009 : renamed (?) ; attempts to be java like, then back (commented)
  // MMa 12/2009 : modified for spell checking
  // MMa 02/2010 : formatting and javadoc revision

  /** The JJ partitioning */
  public static final String   JJ_PARTITIONING  = "sf.eclipse.javacc.editors.JJEditor.partitioning"; //$NON-NLS-1$ 
  /** The identifier of the JJ code partition type */
  public static final String   JJ_CODE          = IDocument.DEFAULT_CONTENT_TYPE;
  /** The identifier of the JJ comment partition type */
  //  public static final String   JJ_COMMENT       = IJavaPartitions.JAVA_DOC;
  public static final String   JJ_COMMENT       = "JJ_COMMENT";                                     //$NON-NLS-1$ 

  /** The array of JJ content types */
  public static final String[] JJ_CONTENT_TYPES = {
      JJ_CODE, JJ_COMMENT                      };

  //  public static final String[] JJ_CONTENT_TYPES = {
  //      JJ_CODE, IJavaPartitions.JAVA_DOC, IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
  //      IJavaPartitions.JAVA_SINGLE_LINE_COMMENT
  //                                                /*, IJavaPartitions.JAVA_STRING
  //                                                  , IJavaPartitions.JAVA_CHARACTER */
  //                                                };

  /**
   * Creates a JJ partitioner and connects it to the document.
   * 
   * @see StorageDocumentProvider#setupDocument(Object, IDocument)
   */
  @Override
  protected void setupDocument(@SuppressWarnings("unused") final Object element, final IDocument document) {
    if (document instanceof IDocumentExtension3) {
      final IDocumentExtension3 ext = (IDocumentExtension3) document;
      final IDocumentPartitioner partitioner = createJJPartitioner();
      ext.setDocumentPartitioner(JJ_PARTITIONING, partitioner);
      //      ext.setDocumentPartitioner(IDocument.DEFAULT_CONTENT_TYPE, partitioner);
      partitioner.connect(document);
    }
  }

  /**
   * @return a FastPartitioner set with a RuleBasedPartitionScanner set with java and javadoc comment lines
   *         rules.
   */
  private IDocumentPartitioner createJJPartitioner() {
    final SingleLineRule java_singleline_comment = new SingleLineRule("//", null, new Token(JJ_COMMENT), //$NON-NLS-1$
                                                                      (char) 0, true, false);
    final MultiLineRule javadoc_comment = new MultiLineRule("/**", "*/", new Token(JJ_COMMENT), (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
                                                            true);
    final MultiLineRule java_multiline_comment = new MultiLineRule("/*", "*/", new Token(JJ_COMMENT), //$NON-NLS-1$ //$NON-NLS-2$
                                                                   (char) 0, true);
    final IPredicateRule[] rules = {
        java_singleline_comment, javadoc_comment, java_multiline_comment };

    final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    scanner.setPredicateRules(rules);
    return new FastPartitioner(scanner, JJ_CONTENT_TYPES);

    //    final Token jdt = new Token(IJavaPartitions.JAVA_DOC);
    //    final MultiLineRule javadoc_comment = new MultiLineRule("/**", "*/", jdt, (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
    //                                                            true);
    //    final Token jmlct = new Token(IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
    //    final MultiLineRule java_multiline_comment = new MultiLineRule("/*", "*/", jmlct, //$NON-NLS-1$ //$NON-NLS-2$
    //                                                                   (char) 0, true);
    //    final Token jslct = new Token(IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
    //    final SingleLineRule java_singleline_comment = new SingleLineRule("//", null, jslct, //$NON-NLS-1$
    //                                                                      (char) 0, true, false);
    //    final IPredicateRule[] rules = {
    //        javadoc_comment, java_multiline_comment, java_singleline_comment };
    //
    //    final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    //    scanner.setPredicateRules(rules);
    //    return new FastPartitioner(scanner, JJSourceViewerConfiguration.getConfiguredContentTypes());
  }

}
