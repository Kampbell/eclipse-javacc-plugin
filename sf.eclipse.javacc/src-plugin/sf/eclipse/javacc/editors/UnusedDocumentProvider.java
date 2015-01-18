package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.*;

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

import sf.eclipse.javacc.scanners.CodeScanner;
import sf.eclipse.javacc.scanners.PartitionScannerRule;
import sf.eclipse.javacc.scanners.SMLJCDetector;

/**
 * Manages the document partitioning.<br>
 * Must be coherent with {@link CodeScanner}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class UnusedDocumentProvider extends FileDocumentProvider {

  // MMa 12/2009 : renamed (?) ; attempts to be java like, then back (commented)
  // MMa 12/2009 : modified for spell checking
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added rules for JJ code / comments partitions
  // BF  05/2012 : revised to use optimized code partitioning rule, multiple content types
  // MMa 10/2012 : renamed
  // MMa 11/2014 : renamed and replaced by DocumentSetupParticipant

  /**
   * Constructs a document partitioner and connects it to the document.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void setupDocument(final Object aElement, final IDocument aDoc) {
    if (aDoc instanceof IDocumentExtension3) {
      final IDocumentExtension3 theDoc = (IDocumentExtension3) aDoc;
      final IDocumentPartitioner partitioner = createPartitioner();
      theDoc.setDocumentPartitioner(PARTITIONING_ID, partitioner);
      partitioner.connect(aDoc);
    }
  }

  /**
   * Creates the partitioner.
   * 
   * @return a FastPartitioner set with a RuleBasedPartitionScanner set with code and comment partitions
   */
  private static IDocumentPartitioner createPartitioner() {
    final IToken codeToken = new Token(CODE_CONTENT_TYPE);
    final IToken lineCommentToken = new Token(LINE_CMT_CONTENT_TYPE);
    final IToken blockCommentToken = new Token(BLOCK_CMT_CONTENT_TYPE);
    final IToken javadocToken = new Token(JAVADOC_CONTENT_TYPE);

    final PartitionScannerRule codeRule = new PartitionScannerRule(codeToken);
    final EndOfLineRule lineCommentRule = new EndOfLineRule("//", lineCommentToken); //$NON-NLS-1$
    final WordPatternRule emptyBlockCommentRule = new WordPatternRule(new SMLJCDetector(),
                                                                      "/**", "/", blockCommentToken); //$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule javadocCommentRule = new MultiLineRule("/**", "*/", javadocToken, (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
                                                               true);
    final MultiLineRule blockCommentRule = new MultiLineRule("/*", "*/", blockCommentToken, //$NON-NLS-1$ //$NON-NLS-2$
                                                             (char) 0, true);

    final IPredicateRule[] rules = {
        codeRule, lineCommentRule, emptyBlockCommentRule, javadocCommentRule, blockCommentRule };

    final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    scanner.setPredicateRules(rules);

    return new FastPartitioner(scanner, CONTENT_TYPES);
  }

}
