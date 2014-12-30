package sf.eclipse.javacc.scanners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.jdt.internal.ui.text.CombinedWordRule;
import org.eclipse.jdt.internal.ui.text.ISourceVersionDependent;
import org.eclipse.jdt.internal.ui.text.JavaWhitespaceDetector;
import org.eclipse.jdt.internal.ui.text.JavaWordDetector;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A JavaCC & Java code scanner.
 * 
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
@SuppressWarnings("restriction")
public final class UnusedJavaCodeScanner extends AbstractJavaScanner {

  // MMa 12/2009 : added to project (but not used)
  // BF  05/2012 : removed else clauses to prevent warning
  // MMa 10/2012 : renamed

  /**
   * Rule to detect java operators.
   * 
   * @since 3.0
   */
  protected static final class OperatorRule implements IRule {

    /** Java operators */
    protected final char[] JAVA_OPERATORS = {
                                              ';', '.', '=', '/', '\\', '+', '-', '*', '<', '>', ':', '?',
                                              '!', ',', '|', '&', '^', '%', '~' };
    /** Token to return for this rule */
    protected final IToken jToken;

    /**
     * Creates a new operator rule.
     * 
     * @param aToken - Token to use for this rule
     */
    public OperatorRule(final IToken aToken) {
      jToken = aToken;
    }

    /**
     * Is this character an operator character?
     * 
     * @param aCh - Character to determine whether it is an operator character
     * @return <code>true</code> if the character is an operator, <code>false</code> otherwise.
     */
    public boolean isOperator(final char aCh) {
      for (int index = 0; index < JAVA_OPERATORS.length; index++) {
        if (JAVA_OPERATORS[index] == aCh) {
          return true;
        }
      }
      return false;
    }

    /** {@inheritDoc} */
    @Override
    public IToken evaluate(final ICharacterScanner aScanner) {

      int character = aScanner.read();
      if (isOperator((char) character)) {
        do {
          character = aScanner.read();
        } while (isOperator((char) character));
        aScanner.unread();
        return jToken;
      }
      aScanner.unread();
      return Token.UNDEFINED;
    }
  }

  /**
   * Rule to detect java brackets.
   * 
   * @since 3.3
   */
  protected static final class BracketRule implements IRule {

    /** Java brackets */
    protected final char[] JAVA_BRACKETS = {
                                             '(', ')', '{', '}', '[', ']' };
    /** Token to return for this rule */
    protected final IToken jToken;

    /**
     * Creates a new bracket rule.
     * 
     * @param aToken - Token to use for this rule
     */
    public BracketRule(final IToken aToken) {
      jToken = aToken;
    }

    /**
     * Is this character a bracket character?
     * 
     * @param aCh - Character to determine whether it is a bracket character
     * @return <code>true</code> if the character is a bracket, <code>false</code> otherwise
     */
    public boolean isBracket(final char aCh) {
      for (int index = 0; index < JAVA_BRACKETS.length; index++) {
        if (JAVA_BRACKETS[index] == aCh) {
          return true;
        }
      }
      return false;
    }

    /** {@inheritDoc} */
    @Override
    public IToken evaluate(final ICharacterScanner aScanner) {

      int character = aScanner.read();
      if (isBracket((char) character)) {
        do {
          character = aScanner.read();
        } while (isBracket((char) character));
        aScanner.unread();
        return jToken;
      }
      aScanner.unread();
      return Token.UNDEFINED;
    }
  }

  /**
   * Word matcher aware of JDK version.
   */
  protected static class VersionedWordMatcher extends CombinedWordRule.WordMatcher implements
                                                                                  ISourceVersionDependent {

    /** The default token for a word */
    protected final IToken jDefaultToken;
    /** The target Java version */
    protected final String jVersion;
    /** True if the source version matches the target version, false otherwise */
    protected boolean      jIsVersionMatch;

    /**
     * Standard constructor which initializes the fields.
     * 
     * @param aDefaultToken - the default token for a word
     * @param aVersion - the target JDK version
     * @param aCurrentVersion - the source current version
     */
    public VersionedWordMatcher(final IToken aDefaultToken, final String aVersion,
                                final String aCurrentVersion) {
      jDefaultToken = aDefaultToken;
      jVersion = aVersion;
      setSourceVersion(aCurrentVersion);
    }

    /** {@inheritDoc} */
    @Override
    public void setSourceVersion(final String aVersion) {
      jIsVersionMatch = jVersion.compareTo(aVersion) <= 0;
    }

    /** {@inheritDoc} */
    @Override
    public IToken evaluate(final ICharacterScanner aScanner, final CombinedWordRule.CharacterBuffer aWord) {
      final IToken token = super.evaluate(aScanner, aWord);

      if (jIsVersionMatch || token.isUndefined()) {
        return token;
      }

      return jDefaultToken;
    }
  }

  /**
   * An annotation rule matches the '@' symbol, any following whitespace and optionally a following
   * <code>interface</code> keyword. It does not match if there is a comment between the '@' symbol and the
   * identifier. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=82452
   * 
   * @since 3.1
   */
  protected static class AnnotationRule implements IRule, ISourceVersionDependent {

    /**
     * A resettable scanner supports marking a position in a scanner and unreading back to the marked
     * position.
     */
    protected static final class ResettableScanner implements ICharacterScanner {

      /** The character scanner */
      protected final ICharacterScanner jCharacterScanner;
      /** The number of characters read before marking them */
      protected int                     readCount;

      /**
       * Creates a new resettable scanner that will forward calls to <code>scanner</code>, but store a marked
       * position.
       * 
       * @param aScanner - the delegate scanner
       */
      public ResettableScanner(final ICharacterScanner aScanner) {
        Assert.isNotNull(aScanner);
        jCharacterScanner = aScanner;
        mark();
      }

      /** {@inheritDoc} */
      @Override
      public int getColumn() {
        return jCharacterScanner.getColumn();
      }

      /** {@inheritDoc} */
      @Override
      public char[][] getLegalLineDelimiters() {
        return jCharacterScanner.getLegalLineDelimiters();
      }

      /** {@inheritDoc} */
      @Override
      public int read() {
        final int ch = jCharacterScanner.read();
        if (ch != ICharacterScanner.EOF) {
          readCount++;
        }
        return ch;
      }

      /** {@inheritDoc} */
      @Override
      public void unread() {
        if (readCount > 0) {
          readCount--;
        }
        jCharacterScanner.unread();
      }

      /**
       * Marks an offset in the scanned content.
       */
      public void mark() {
        readCount = 0;
      }

      /**
       * Resets the scanner to the marked position.
       */
      public void reset() {
        while (readCount > 0) {
          unread();
        }

        while (readCount < 0) {
          read();
        }
      }
    }

    /** The current whitespace detector */
    protected final IWhitespaceDetector jWhitespaceDetector = new JavaWhitespaceDetector();
    /** The current Java word detector */
    protected final IWordDetector       jWordDetector       = new JavaWordDetector();
    /** The token for the '@interface' annotation */
    protected final IToken              jInterfaceToken;
    /** The token for other annotations than the '@interface' annotation */
    protected final IToken              jAtToken;
    /** The target Java version */
    protected final String              jVersion;
    /** True if the source version matches the target version, false otherwise */
    protected boolean                   jIsVersionMatch;

    /**
     * Creates a new rule.
     * 
     * @param aInterfaceToken - the token to return if <code>'@\s*interface'</code> is matched
     * @param aAtToken - the token to return if <code>'@'</code> is matched, but not
     *          <code>'@\s*interface'</code>
     * @param aVersion - the lowest <code>JavaCore.COMPILER_SOURCE</code> version that this rule is enabled
     * @param aCurrentVersion - the current <code>JavaCore.COMPILER_SOURCE</code> version
     */
    public AnnotationRule(final IToken aInterfaceToken, final Token aAtToken, final String aVersion,
                          final String aCurrentVersion) {
      jInterfaceToken = aInterfaceToken;
      jAtToken = aAtToken;
      jVersion = aVersion;
      setSourceVersion(aCurrentVersion);
    }

    /** {@inheritDoc} */
    @Override
    public IToken evaluate(final ICharacterScanner aScanner) {
      if (!jIsVersionMatch) {
        return Token.UNDEFINED;
      }

      final ResettableScanner resettable = new ResettableScanner(aScanner);
      if (resettable.read() == '@') {
        return readAnnotation(resettable);
      }

      resettable.reset();
      return Token.UNDEFINED;
    }

    /**
     * Reads an annotation and returns the matching token.
     * 
     * @param aScanner - the resettable character scanner
     * @return the token matching the annotation
     */
    private IToken readAnnotation(final ResettableScanner aScanner) {
      aScanner.mark();
      skipWhitespace(aScanner);
      if (readInterface(aScanner)) {
        return jInterfaceToken;
      }
      aScanner.reset();
      return jAtToken;
    }

    /**
     * Reads an '@interface' annotation and returns the matching token.
     * 
     * @param aScanner - the resettable character scanner
     * @return the token matching the annotation
     */
    private boolean readInterface(final ICharacterScanner aScanner) {
      int ch = aScanner.read();
      int i = 0;
      while (i < INTERFACE.length() && INTERFACE.charAt(i) == ch) {
        i++;
        ch = aScanner.read();
      }
      if (i < INTERFACE.length()) {
        return false;
      }

      if (jWordDetector.isWordPart((char) ch)) {
        return false;
      }

      if (ch != ICharacterScanner.EOF) {
        aScanner.unread();
      }

      return true;
    }

    /**
     * Skips all following whitespaces.
     * 
     * @param aScanner - the resettable character scanner
     * @return the token matching the annotation
     */
    private boolean skipWhitespace(final ICharacterScanner aScanner) {
      while (jWhitespaceDetector.isWhitespace((char) aScanner.read())) {
        // do nothing
      }

      aScanner.unread();
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setSourceVersion(final String aVersion) {
      jIsVersionMatch = jVersion.compareTo(aVersion) <= 0;
    }

  }

  /** The target Java version as set in the preferences */
  protected static final String                 SOURCE_VERSION         = JavaCore.COMPILER_SOURCE;

  /** The Java keywords */
  protected static String[]                     sKeywords              = {
      "abstract", //$NON-NLS-1$
      "break", //$NON-NLS-1$
      "case", "catch", "class", "const", "continue", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "default", "do", //$NON-NLS-2$ //$NON-NLS-1$
      "else", "extends", //$NON-NLS-2$ //$NON-NLS-1$
      "final", "finally", "for", //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "goto", //$NON-NLS-1$
      "if", "implements", "import", "instanceof", "interface", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "native", "new", //$NON-NLS-2$ //$NON-NLS-1$
      "package", "private", "protected", "public", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "static", "super", "switch", "synchronized", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "this", "throw", "throws", "transient", "try", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
      "volatile", //$NON-NLS-1$
      "while" //$NON-NLS-1$
                                                                       };

  /** The 'interface' annotation keyword */
  protected static final String                 INTERFACE              = "interface";                                                    //$NON-NLS-1$
  /** The 'return' keyword */
  protected static final String                 RETURN                 = "return";                                                       //$NON-NLS-1$
  /** The Java 1.4 additional keywords */
  protected static String[]                     sJava14Keywords        = {
                                                                         "assert" };                                                     //$NON-NLS-1$
  /** The Java 1.5 additional keywords */
  protected static String[]                     sJava15Keywords        = {
                                                                         "enum" };                                                       //$NON-NLS-1$

  /** The Java types */
  protected static String[]                     sTypes                 = {
      "void", "boolean", "char", "byte", "short", "strictfp", "int", "long", "float", "double" };                                        //$NON-NLS-1$ //$NON-NLS-5$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-8$ //$NON-NLS-9$  //$NON-NLS-10$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$

  /** The Java constants */
  protected static String[]                     sConstants             = {
      "false", "null", "true"                                         };                                                                //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

  /** The annotation base preferences key */
  protected static final String                 ANNOTATION_BASE_KEY    = PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
                                                                         + SemanticHighlightings.ANNOTATION;
  /** The annotation color preferences key */
  protected static final String                 ANNOTATION_COLOR_KEY   = ANNOTATION_BASE_KEY
                                                                         + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

  /** The array of token colors */
  protected static String[]                     sTokenProperties       = {
      IJavaColorConstants.JAVA_KEYWORD, IJavaColorConstants.JAVA_STRING, IJavaColorConstants.JAVA_DEFAULT,
      IJavaColorConstants.JAVA_KEYWORD_RETURN, IJavaColorConstants.JAVA_OPERATOR,
      IJavaColorConstants.JAVA_BRACKET, ANNOTATION_COLOR_KEY,         };

  /** The version dependent rules list */
  protected final List<ISourceVersionDependent> jVersionDependentRules = new ArrayList<ISourceVersionDependent>(
                                                                                                                3);

  /**
   * Creates a Java code scanner
   * 
   * @param aColorManager - the color manager
   * @param aPrefStore - the preference store
   */
  public UnusedJavaCodeScanner(final IColorManager aColorManager, final IPreferenceStore aPrefStore) {
    super(aColorManager, aPrefStore);
    initialize();
  }

  /** {@inheritDoc} */
  @Override
  protected String[] getTokenProperties() {
    return sTokenProperties;
  }

  /** {@inheritDoc} */
  @Override
  protected List<IRule> createRules() {

    final List<IRule> rules = new ArrayList<IRule>();

    // Add rule for character constants
    Token token = getToken(IJavaColorConstants.JAVA_STRING);
    rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

    // Add generic whitespace rule
    rules.add(new WhitespaceRule(new JavaWhitespaceDetector()));

    final String version = getPreferenceStore().getString(SOURCE_VERSION);

    // Add JLS3 rule for /@\s*interface/ and /@\s*\w+/
    token = getToken(ANNOTATION_COLOR_KEY);
    final AnnotationRule atInterfaceRule = new AnnotationRule(getToken(IJavaColorConstants.JAVA_KEYWORD),
                                                              token, JavaCore.VERSION_1_5, version);
    rules.add(atInterfaceRule);
    jVersionDependentRules.add(atInterfaceRule);

    // Add word rule for new keywords, 4077
    final JavaWordDetector wordDetector = new JavaWordDetector();
    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final CombinedWordRule combinedWordRule = new CombinedWordRule(wordDetector, token);

    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final VersionedWordMatcher j14Matcher = new VersionedWordMatcher(token, JavaCore.VERSION_1_4, version);

    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < sJava14Keywords.length; i++) {
      j14Matcher.addWord(sJava14Keywords[i], token);
    }

    combinedWordRule.addWordMatcher(j14Matcher);
    jVersionDependentRules.add(j14Matcher);

    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final VersionedWordMatcher j15Matcher = new VersionedWordMatcher(token, JavaCore.VERSION_1_5, version);
    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < sJava15Keywords.length; i++) {
      j15Matcher.addWord(sJava15Keywords[i], token);
    }

    combinedWordRule.addWordMatcher(j15Matcher);
    jVersionDependentRules.add(j15Matcher);

    // Add rule for operators
    token = getToken(IJavaColorConstants.JAVA_OPERATOR);
    rules.add(new OperatorRule(token));

    // Add rule for brackets
    token = getToken(IJavaColorConstants.JAVA_BRACKET);
    rules.add(new BracketRule(token));

    // Add word rule for keyword 'return'
    final CombinedWordRule.WordMatcher returnWordRule = new CombinedWordRule.WordMatcher();
    token = getToken(IJavaColorConstants.JAVA_KEYWORD_RETURN);
    returnWordRule.addWord(RETURN, token);
    combinedWordRule.addWordMatcher(returnWordRule);

    // Add word rule for keywords, types, and constants
    final CombinedWordRule.WordMatcher wordRule = new CombinedWordRule.WordMatcher();
    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < sKeywords.length; i++) {
      wordRule.addWord(sKeywords[i], token);
    }
    for (int i = 0; i < sTypes.length; i++) {
      wordRule.addWord(sTypes[i], token);
    }
    for (int i = 0; i < sConstants.length; i++) {
      wordRule.addWord(sConstants[i], token);
    }

    combinedWordRule.addWordMatcher(wordRule);

    rules.add(combinedWordRule);

    setDefaultReturnToken(getToken(IJavaColorConstants.JAVA_DEFAULT));
    return rules;
  }

  /** {@inheritDoc} */
  @Override
  protected String getBoldKey(final String aColorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(aColorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
    }
    return super.getBoldKey(aColorKey);
  }

  /** {@inheritDoc} */
  @Override
  protected String getItalicKey(final String aColorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(aColorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
    }
    return super.getItalicKey(aColorKey);
  }

  /** {@inheritDoc} */
  @Override
  protected String getStrikethroughKey(final String aColorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(aColorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
    }
    return super.getStrikethroughKey(aColorKey);
  }

  /** {@inheritDoc} */
  @Override
  protected String getUnderlineKey(final String aColorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(aColorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
    }
    return super.getUnderlineKey(aColorKey);
  }

  /** {@inheritDoc} */
  @Override
  public boolean affectsBehavior(final PropertyChangeEvent aEvent) {
    return aEvent.getProperty().equals(SOURCE_VERSION) || super.affectsBehavior(aEvent);
  }

  /** {@inheritDoc} */
  @Override
  public void adaptToPreferenceChange(final PropertyChangeEvent aEvent) {

    if (aEvent.getProperty().equals(SOURCE_VERSION)) {
      final Object value = aEvent.getNewValue();

      if (value instanceof String) {
        final String s = (String) value;

        for (final Iterator<ISourceVersionDependent> it = jVersionDependentRules.iterator(); it.hasNext();) {
          final ISourceVersionDependent dependent = it.next();
          dependent.setSourceVersion(s);
        }
      }

    }
    else if (super.affectsBehavior(aEvent)) {
      super.adaptToPreferenceChange(aEvent);
    }
  }
}
