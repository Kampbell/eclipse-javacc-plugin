package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import sf.eclipse.javacc.scanners.JJCodeScanner;
import sf.eclipse.javacc.scanners.JJPartitionScannerRule;
import sf.eclipse.javacc.scanners.JJSMLJCDetector;

/**
 * Manages the document partitioning.<br>
 * Must be coherent with {@link JJCodeScanner}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJDocumentProvider extends FileDocumentProvider {

  // MMa 12/2009 : renamed (?) ; attempts to be java like, then back (commented)
  // MMa 12/2009 : modified for spell checking
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added rules for JJ code / comments partitions
  // BF  05/2012 : revised to use optimized code partitioning rule, multiple content types

  /** The partitioning IDs */
  public static final String   JJ_PARTITIONING_ID            = "sf.eclipse.javacc.editors.JJEditor.partitioning"; //$NON-NLS-1$ 

  /** The identifier for the code partition content type */
  public static final String   JJ_CODE_CONTENT_TYPE          = "JJ_CODE";                                        //$NON-NLS-1$ 

  /** The identifier for the single line comments partition content type */
  public static final String   JJ_LINE_COMMENT_CONTENT_TYPE  = "JJ_LINE_COMMENT";                                //$NON-NLS-1$ 

  /** The identifier for the multiline comments partition content type */
  public static final String   JJ_BLOCK_COMMENT_CONTENT_TYPE = "JJ_BLOCK_COMMENT";                               //$NON-NLS-1$ 

  /** The identifier for the javadoc partition content type */
  public static final String   JJ_JAVADOC_CONTENT_TYPE       = "JJ_JAVADOC";                                     //$NON-NLS-1$ 

  /** The array of partition content types */
  public static final String[] JJ_CONTENT_TYPES              = {
      JJ_CODE_CONTENT_TYPE, //
      JJ_LINE_COMMENT_CONTENT_TYPE, //
      JJ_BLOCK_COMMENT_CONTENT_TYPE, //
      JJ_JAVADOC_CONTENT_TYPE                               };

  /**
   * Constructs a document partitioner and connects it to the document.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void setupDocument(@SuppressWarnings("unused") final Object aElement, final IDocument aDoc) {
    if (aDoc instanceof IDocumentExtension3) {
      final IDocumentExtension3 ext = (IDocumentExtension3) aDoc;
      final IDocumentPartitioner partitioner = createJJPartitioner();
      ext.setDocumentPartitioner(JJ_PARTITIONING_ID, partitioner);
      partitioner.connect(aDoc);
    }
  }

  /**
   * Creates the partitioner.
   * 
   * @return a FastPartitioner set with a RuleBasedPartitionScanner set with code and comment partitions.
   */
  private IDocumentPartitioner createJJPartitioner() {
    final IToken codeToken = new Token(JJ_CODE_CONTENT_TYPE);
    final IToken lineCommentToken = new Token(JJ_LINE_COMMENT_CONTENT_TYPE);
    final IToken blockCommentToken = new Token(JJ_BLOCK_COMMENT_CONTENT_TYPE);
    final IToken javadocToken = new Token(JJ_JAVADOC_CONTENT_TYPE);

    final JJPartitionScannerRule codeRule = new JJPartitionScannerRule(codeToken);
    final EndOfLineRule lineCommentRule = new EndOfLineRule("//", lineCommentToken); //$NON-NLS-1$
    final WordPatternRule emptyBlockCommentRule = new WordPatternRule(new JJSMLJCDetector(),
                                                                      "/**", "/", blockCommentToken); //$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule javadocCommentRule = new MultiLineRule("/**", "*/", javadocToken, (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
                                                               true);
    final MultiLineRule blockCommentRule = new MultiLineRule("/*", "*/", blockCommentToken, //$NON-NLS-1$ //$NON-NLS-2$
                                                             (char) 0, true);

    final IPredicateRule[] rules = {
        codeRule, lineCommentRule, emptyBlockCommentRule, javadocCommentRule, blockCommentRule };

    final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    scanner.setPredicateRules(rules);

    return new FastPartitioner(scanner, JJ_CONTENT_TYPES);
  }

}
