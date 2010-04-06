package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * Manages the document partitioning.<br>
 * Must be coherent with {@link JJCodeScanner}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJDocumentProvider extends FileDocumentProvider {

  // MMa 12/2009 : renamed (?) ; attempts to be java like, then back (commented)
  // MMa 12/2009 : modified for spell checking
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added rules for JJ code / comments partitions

  /** The JJ partitioning id */
  public static final String   JJ_PARTITIONING_ID      = "sf.eclipse.javacc.editors.JJEditor.partitioning"; //$NON-NLS-1$ 
  /** The identifier of the JJ code partition content type */
  // la coloration syntaxique ne marche pas si le code content type n'est pas le d�faut !!!
  public static final String   JJ_CODE_CONTENT_TYPE    = IDocument.DEFAULT_CONTENT_TYPE;
  //  public static final String   JJ_CODE_CONTENT_TYPE    = "JJ_CODE";
  /** The identifier of the JJ comments partition content type */
  public static final String   JJ_COMMENT_CONTENT_TYPE = "JJ_COMMENT";                                     //$NON-NLS-1$ 

  /** The array of JJ content types */
  public static final String[] JJ_CONTENT_TYPES        = {
      JJ_CODE_CONTENT_TYPE, JJ_COMMENT_CONTENT_TYPE   };

  /**
   * Standard constructor.
   */
  public JJDocumentProvider() {
    // nothing special done here
  }

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
      ext.setDocumentPartitioner(JJ_PARTITIONING_ID, partitioner);
      partitioner.connect(document);
    }
  }

  /**
   * @return a FastPartitioner set with a RuleBasedPartitionScanner set with non code (java and javadoc
   *         comments, string and character constants) rules.
   */
  private IDocumentPartitioner createJJPartitioner() {
    // TODO r��crire un scanner � partir de FastJavaPartitionScanner

    // code partition
    final IToken constToken = new Token(JJ_CODE_CONTENT_TYPE);
    // la coloration syntaxique ne marche pas si l'on met ces r�gles pour le code !
    // code rules
    //    final DefaultRule code = new DefaultRule(codeToken);
    //    final EOFRule eof = new EOFRule(codeToken);
    // constants rules
    final SingleLineRule str_cst = new SingleLineRule("\"", "\"", constToken, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    final SingleLineRule char_cst = new SingleLineRule("'", "'", constToken, '\\'); //$NON-NLS-1$ //$NON-NLS-2$

    // comments partition
    final IToken commToken = new Token(JJ_COMMENT_CONTENT_TYPE);
    // comments rules
    final EndOfLineRule singln_com = new EndOfLineRule("//", commToken); //$NON-NLS-1$
    final WordPatternRule empty_com = new WordPatternRule(new JJSMLJCDetector(), "/**", "/", commToken); //$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule javadoc_com = new MultiLineRule("/**", "*/", commToken, (char) 0, //$NON-NLS-1$ //$NON-NLS-2$
                                                        true);
    final MultiLineRule multln_com = new MultiLineRule("/*", "*/", commToken, //$NON-NLS-1$ //$NON-NLS-2$
                                                       (char) 0, true);
    // set rules (same order as JJCodeScanner ?)
    final IPredicateRule[] rules = {
        singln_com, str_cst, char_cst, empty_com, javadoc_com, multln_com /*, code, eof*/};

    final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
    scanner.setPredicateRules(rules);

    return new FastPartitioner(scanner, JJ_CONTENT_TYPES);
  }

  /**
   * Default predicate rule for java code. Test.
   */
  class DefaultRule implements IPredicateRule {

    /** The token for java code */
    IToken fDefaultToken;

    /**
     * Standard constructor.
     * 
     * @param aDefaultToken The token for java code
     */
    public DefaultRule(final IToken aDefaultToken) {
      fDefaultToken = aDefaultToken;
    }

    /**
     * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
     */
    public IToken evaluate(final ICharacterScanner scanner, @SuppressWarnings("unused") final boolean resume) {

      return evaluate(scanner);
    }

    /**
     * @see IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken() {
      return fDefaultToken;
    }

    /**
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(final ICharacterScanner scanner) {
      final int ic = scanner.read();
      if (ic != ICharacterScanner.EOF) {
        return fDefaultToken;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }

  }

  /**
   * End of file predicate rule. Test.
   */
  class EOFRule implements IPredicateRule {

    /** The token for end of file */
    IToken fEofToken;

    /**
     * Standard constructor.
     * 
     * @param aEofToken The token for end of file
     */
    public EOFRule(final IToken aEofToken) {
      fEofToken = aEofToken;
    }

    /**
     * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
     */
    public IToken evaluate(final ICharacterScanner scanner, @SuppressWarnings("unused") final boolean resume) {

      return evaluate(scanner);
    }

    /**
     * @see IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken() {
      return fEofToken;
    }

    /**
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(final ICharacterScanner scanner) {
      final int ic = scanner.read();
      if (ic == ICharacterScanner.EOF) {
        return fEofToken;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }

  }
}
