package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.base.IConstants.*;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
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

import sf.eclipse.javacc.scanners.CodeColorScanner;
import sf.eclipse.javacc.scanners.CodePartitionRule;
import sf.eclipse.javacc.scanners.SMLJCDetector;

/**
 * The document setup participant for JJEditor.<br>
 * Manages the document partitioning.<br>
 * Must be coherent with {@link CodeColorScanner}.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.core.filebuffers.documentSetup">
 * 
 * @author Marc Mazas 2014-2015-2016
 */
public class DocumentSetupParticipant implements IDocumentSetupParticipant {

  // MMa 11/2014 : created from old DocumentProvider
  // MMa 02/2016 : removed default content type partition

  /**
   * Constructs a document partitioner and connects it to the document.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void setup(final IDocument aDoc) {
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
    //    final IToken defaultToken = new Token(IDocument.DEFAULT_CONTENT_TYPE);

    final CodePartitionRule codeRule = new CodePartitionRule(codeToken);
    final EndOfLineRule lineCommentRule = new EndOfLineRule("//", lineCommentToken); //$NON-NLS-1$
    final WordPatternRule emptyBlockCommentRule = new WordPatternRule(new SMLJCDetector(),
                                                                      "/**", "/", blockCommentToken); //$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule javadocCommentRule = new MultiLineRule("/**", "*/", javadocToken, (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
                                                               true);
    final MultiLineRule blockCommentRule = new MultiLineRule("/*", "*/", blockCommentToken, //$NON-NLS-1$ //$NON-NLS-2$
                                                             (char) 0, true);
    //    final PartitionScannerRule defaultRule = new PartitionScannerRule(defaultToken);

    // order is important : /**/, then /** blabla */, then /* blabla */ TODO voir s'il faut garder le eBCR !!!
    final IPredicateRule[] rules = {
        codeRule, lineCommentRule, emptyBlockCommentRule, javadocCommentRule, blockCommentRule /*, defaultRule*/};

    final RuleBasedPartitionScanner javaccPartitionScanner = new RuleBasedPartitionScanner();
    javaccPartitionScanner.setPredicateRules(rules);

    return new FastPartitioner(javaccPartitionScanner, CONTENT_TYPES);
  }
}
