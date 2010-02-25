package sf.eclipse.javacc.editors;

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
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public final class UnusedJJJavaCodeScanner extends AbstractJavaScanner {

  // MMa 12/2009 : added to project (but not used)

  /**
   * Rule to detect java operators.
   * 
   * @since 3.0
   */
  private static final class OperatorRule implements IRule {

    /** Java operators */
    private final char[] JAVA_OPERATORS = {
                                            ';', '.', '=', '/', '\\', '+', '-', '*', '<', '>', ':', '?', '!',
                                            ',', '|', '&', '^', '%', '~' };
    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new operator rule.
     * 
     * @param token Token to use for this rule
     */
    public OperatorRule(final IToken token) {
      fToken = token;
    }

    /**
     * Is this character an operator character?
     * 
     * @param character Character to determine whether it is an operator character
     * @return <code>true</code> if the character is an operator, <code>false</code> otherwise.
     */
    public boolean isOperator(final char character) {
      for (int index = 0; index < JAVA_OPERATORS.length; index++) {
        if (JAVA_OPERATORS[index] == character) {
          return true;
        }
      }
      return false;
    }

    /**
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(final ICharacterScanner scanner) {

      int character = scanner.read();
      if (isOperator((char) character)) {
        do {
          character = scanner.read();
        } while (isOperator((char) character));
        scanner.unread();
        return fToken;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }
  }

  /**
   * Rule to detect java brackets.
   * 
   * @since 3.3
   */
  private static final class BracketRule implements IRule {

    /** Java brackets */
    private final char[] JAVA_BRACKETS = {
                                           '(', ')', '{', '}', '[', ']' };
    /** Token to return for this rule */
    private final IToken fToken;

    /**
     * Creates a new bracket rule.
     * 
     * @param token Token to use for this rule
     */
    public BracketRule(final IToken token) {
      fToken = token;
    }

    /**
     * Is this character a bracket character?
     * 
     * @param character Character to determine whether it is a bracket character
     * @return <code>true</code> if the character is a bracket, <code>false</code> otherwise.
     */
    public boolean isBracket(final char character) {
      for (int index = 0; index < JAVA_BRACKETS.length; index++) {
        if (JAVA_BRACKETS[index] == character) {
          return true;
        }
      }
      return false;
    }

    /**
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(final ICharacterScanner scanner) {

      int character = scanner.read();
      if (isBracket((char) character)) {
        do {
          character = scanner.read();
        } while (isBracket((char) character));
        scanner.unread();
        return fToken;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }
  }

  /**
   * Word matcher aware of JDK version.
   */
  private static class VersionedWordMatcher extends CombinedWordRule.WordMatcher implements
                                                                                ISourceVersionDependent {

    /** the default token for a word */
    private final IToken fDefaultToken;
    /** the target Java version */
    private final String fVersion;
    /** true if the source version matches the target version, false otherwise */
    private boolean      fIsVersionMatch;

    /**
     * Standard constructor which initializes the fields.
     * 
     * @param defaultToken the default token for a word
     * @param version the target JDK version
     * @param currentVersion the source current version
     */
    public VersionedWordMatcher(final IToken defaultToken, final String version, final String currentVersion) {
      fDefaultToken = defaultToken;
      fVersion = version;
      setSourceVersion(currentVersion);
    }

    /**
     * @see ISourceVersionDependent#setSourceVersion(java.lang.String)
     */
    public void setSourceVersion(final String version) {
      fIsVersionMatch = fVersion.compareTo(version) <= 0;
    }

    /**
     * @see CombinedWordRule.WordMatcher#evaluate(ICharacterScanner, CombinedWordRule.CharacterBuffer)
     */
    @Override
    public IToken evaluate(final ICharacterScanner scanner, final CombinedWordRule.CharacterBuffer word) {
      final IToken token = super.evaluate(scanner, word);

      if (fIsVersionMatch || token.isUndefined()) {
        return token;
      }

      return fDefaultToken;
    }
  }

  /**
   * An annotation rule matches the '@' symbol, any following whitespace and optionally a following
   * <code>interface</code> keyword. It does not match if there is a comment between the '@' symbol and the
   * identifier. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=82452
   * 
   * @since 3.1
   */
  private static class AnnotationRule implements IRule, ISourceVersionDependent {

    /**
     * A resettable scanner supports marking a position in a scanner and unreading back to the marked
     * position.
     */
    private static final class ResettableScanner implements ICharacterScanner {

      /** the character scanner */
      private final ICharacterScanner fDelegate;
      /** the number of characters read before marking them */
      private int                     fReadCount;

      /**
       * Creates a new resettable scanner that will forward calls to <code>scanner</code>, but store a marked
       * position.
       * 
       * @param scanner the delegate scanner
       */
      public ResettableScanner(final ICharacterScanner scanner) {
        Assert.isNotNull(scanner);
        fDelegate = scanner;
        mark();
      }

      /**
       * @see ICharacterScanner#getColumn()
       */
      public int getColumn() {
        return fDelegate.getColumn();
      }

      /**
       * @see ICharacterScanner#getLegalLineDelimiters()
       */
      public char[][] getLegalLineDelimiters() {
        return fDelegate.getLegalLineDelimiters();
      }

      /**
       * @see ICharacterScanner#read()
       */
      public int read() {
        final int ch = fDelegate.read();
        if (ch != ICharacterScanner.EOF) {
          fReadCount++;
        }
        return ch;
      }

      /**
       * @see ICharacterScanner#unread()
       */
      public void unread() {
        if (fReadCount > 0) {
          fReadCount--;
        }
        fDelegate.unread();
      }

      /**
       * Marks an offset in the scanned content.
       */
      public void mark() {
        fReadCount = 0;
      }

      /**
       * Resets the scanner to the marked position.
       */
      public void reset() {
        while (fReadCount > 0) {
          unread();
        }

        while (fReadCount < 0) {
          read();
        }
      }
    }

    /** the current whitespace detector */
    private final IWhitespaceDetector fWhitespaceDetector = new JavaWhitespaceDetector();
    /** the current Java word detector */
    private final IWordDetector       fWordDetector       = new JavaWordDetector();
    /** the token for the '@interface' annotation */
    private final IToken              fInterfaceToken;
    /** the token for other annotations than the '@interface' annotation */
    private final IToken              fAtToken;
    /** the target Java version */
    private final String              fVersion;
    /** true if the source version matches the target version, false otherwise */
    private boolean                   fIsVersionMatch;

    /**
     * Creates a new rule.
     * 
     * @param interfaceToken the token to return if <code>'@\s*interface'</code> is matched
     * @param atToken the token to return if <code>'@'</code> is matched, but not <code>'@\s*interface'</code>
     * @param version the lowest <code>JavaCore.COMPILER_SOURCE</code> version that this rule is enabled
     * @param currentVersion the current <code>JavaCore.COMPILER_SOURCE</code> version
     */
    public AnnotationRule(final IToken interfaceToken, final Token atToken, final String version,
                          final String currentVersion) {
      fInterfaceToken = interfaceToken;
      fAtToken = atToken;
      fVersion = version;
      setSourceVersion(currentVersion);
    }

    /**
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(final ICharacterScanner scanner) {
      if (!fIsVersionMatch) {
        return Token.UNDEFINED;
      }

      final ResettableScanner resettable = new ResettableScanner(scanner);
      if (resettable.read() == '@') {
        return readAnnotation(resettable);
      }

      resettable.reset();
      return Token.UNDEFINED;
    }

    /**
     * Reads an annotation and returns the matching token.
     * 
     * @param scanner the resettable character scanner
     * @return the token matching the annotation
     */
    private IToken readAnnotation(final ResettableScanner scanner) {
      scanner.mark();
      skipWhitespace(scanner);
      if (readInterface(scanner)) {
        return fInterfaceToken;
      }
      else {
        scanner.reset();
        return fAtToken;
      }
    }

    /**
     * Reads an '@interface' annotation and returns the matching token.
     * 
     * @param scanner the resettable character scanner
     * @return the token matching the annotation
     */
    private boolean readInterface(final ICharacterScanner scanner) {
      int ch = scanner.read();
      int i = 0;
      while (i < INTERFACE.length() && INTERFACE.charAt(i) == ch) {
        i++;
        ch = scanner.read();
      }
      if (i < INTERFACE.length()) {
        return false;
      }

      if (fWordDetector.isWordPart((char) ch)) {
        return false;
      }

      if (ch != ICharacterScanner.EOF) {
        scanner.unread();
      }

      return true;
    }

    /**
     * Skips all following whitespaces.
     * 
     * @param scanner the resettable character scanner
     * @return the token matching the annotation
     */
    private boolean skipWhitespace(final ICharacterScanner scanner) {
      while (fWhitespaceDetector.isWhitespace((char) scanner.read())) {
        // do nothing
      }

      scanner.unread();
      return true;
    }

    /**
     * @see ISourceVersionDependent#setSourceVersion(java.lang.String)
     */
    public void setSourceVersion(final String version) {
      fIsVersionMatch = fVersion.compareTo(version) <= 0;
    }

  }

  /** the target Java version as set in the preferences */
  private static final String                 SOURCE_VERSION         = JavaCore.COMPILER_SOURCE;

  /** the Java keywords */
  static String[]                             fgKeywords             = {
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

  /** the 'interface' annotation keyword */
  private static final String                 INTERFACE              = "interface";                                                    //$NON-NLS-1$
  /** the 'return' keyword */
  private static final String                 RETURN                 = "return";                                                       //$NON-NLS-1$
  /** the Java 1.4 additional keywords */
  private static String[]                     fgJava14Keywords       = {
                                                                       "assert" };                                                     //$NON-NLS-1$
  /** the Java 1.5 additional keywords */
  private static String[]                     fgJava15Keywords       = {
                                                                       "enum" };                                                       //$NON-NLS-1$

  /** the Java types */
  private static String[]                     fgTypes                = {
      "void", "boolean", "char", "byte", "short", "strictfp", "int", "long", "float", "double" };                                      //$NON-NLS-1$ //$NON-NLS-5$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-8$ //$NON-NLS-9$  //$NON-NLS-10$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$

  /** the Java constants */
  private static String[]                     fgConstants            = {
      "false", "null", "true"                                       };                                                                //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

  /** the annotation base preferences key */
  private static final String                 ANNOTATION_BASE_KEY    = PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
                                                                       + SemanticHighlightings.ANNOTATION;
  /** the annotation color preferences key */
  private static final String                 ANNOTATION_COLOR_KEY   = ANNOTATION_BASE_KEY
                                                                       + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

  /** the array of token colors */
  private static String[]                     fgTokenProperties      = {
      IJavaColorConstants.JAVA_KEYWORD, IJavaColorConstants.JAVA_STRING, IJavaColorConstants.JAVA_DEFAULT,
      IJavaColorConstants.JAVA_KEYWORD_RETURN, IJavaColorConstants.JAVA_OPERATOR,
      IJavaColorConstants.JAVA_BRACKET, ANNOTATION_COLOR_KEY,       };

  /** the version dependent rules list */
  private final List<ISourceVersionDependent> fVersionDependentRules = new ArrayList<ISourceVersionDependent>(
                                                                                                              3);

  /**
   * Creates a Java code scanner
   * 
   * @param manager the color manager
   * @param store the preference store
   */
  public UnusedJJJavaCodeScanner(final IColorManager manager, final IPreferenceStore store) {
    super(manager, store);
    initialize();
  }

  /**
   * @see AbstractJavaScanner#getTokenProperties()
   */
  @Override
  protected String[] getTokenProperties() {
    return fgTokenProperties;
  }

  /**
   * @see AbstractJavaScanner#createRules()
   */
  @Override
  protected List<IRule> createRules() {

    final List<IRule> rules = new ArrayList<IRule>();

    // Add rule for character constants.
    Token token = getToken(IJavaColorConstants.JAVA_STRING);
    rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

    // Add generic whitespace rule.
    rules.add(new WhitespaceRule(new JavaWhitespaceDetector()));

    final String version = getPreferenceStore().getString(SOURCE_VERSION);

    // Add JLS3 rule for /@\s*interface/ and /@\s*\w+/
    token = getToken(ANNOTATION_COLOR_KEY);
    final AnnotationRule atInterfaceRule = new AnnotationRule(getToken(IJavaColorConstants.JAVA_KEYWORD),
                                                              token, JavaCore.VERSION_1_5, version);
    rules.add(atInterfaceRule);
    fVersionDependentRules.add(atInterfaceRule);

    // Add word rule for new keywords, 4077
    final JavaWordDetector wordDetector = new JavaWordDetector();
    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final CombinedWordRule combinedWordRule = new CombinedWordRule(wordDetector, token);

    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final VersionedWordMatcher j14Matcher = new VersionedWordMatcher(token, JavaCore.VERSION_1_4, version);

    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < fgJava14Keywords.length; i++) {
      j14Matcher.addWord(fgJava14Keywords[i], token);
    }

    combinedWordRule.addWordMatcher(j14Matcher);
    fVersionDependentRules.add(j14Matcher);

    token = getToken(IJavaColorConstants.JAVA_DEFAULT);
    final VersionedWordMatcher j15Matcher = new VersionedWordMatcher(token, JavaCore.VERSION_1_5, version);
    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < fgJava15Keywords.length; i++) {
      j15Matcher.addWord(fgJava15Keywords[i], token);
    }

    combinedWordRule.addWordMatcher(j15Matcher);
    fVersionDependentRules.add(j15Matcher);

    // Add rule for operators
    token = getToken(IJavaColorConstants.JAVA_OPERATOR);
    rules.add(new OperatorRule(token));

    // Add rule for brackets
    token = getToken(IJavaColorConstants.JAVA_BRACKET);
    rules.add(new BracketRule(token));

    // Add word rule for keyword 'return'.
    final CombinedWordRule.WordMatcher returnWordRule = new CombinedWordRule.WordMatcher();
    token = getToken(IJavaColorConstants.JAVA_KEYWORD_RETURN);
    returnWordRule.addWord(RETURN, token);
    combinedWordRule.addWordMatcher(returnWordRule);

    // Add word rule for keywords, types, and constants.
    final CombinedWordRule.WordMatcher wordRule = new CombinedWordRule.WordMatcher();
    token = getToken(IJavaColorConstants.JAVA_KEYWORD);
    for (int i = 0; i < fgKeywords.length; i++) {
      wordRule.addWord(fgKeywords[i], token);
    }
    for (int i = 0; i < fgTypes.length; i++) {
      wordRule.addWord(fgTypes[i], token);
    }
    for (int i = 0; i < fgConstants.length; i++) {
      wordRule.addWord(fgConstants[i], token);
    }

    combinedWordRule.addWordMatcher(wordRule);

    rules.add(combinedWordRule);

    setDefaultReturnToken(getToken(IJavaColorConstants.JAVA_DEFAULT));
    return rules;
  }

  /**
   * @see AbstractJavaScanner#getBoldKey(java.lang.String)
   */
  @Override
  protected String getBoldKey(final String colorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(colorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
    }
    return super.getBoldKey(colorKey);
  }

  /**
   * @see AbstractJavaScanner#getItalicKey(java.lang.String)
   */
  @Override
  protected String getItalicKey(final String colorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(colorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
    }
    return super.getItalicKey(colorKey);
  }

  /**
   * @see AbstractJavaScanner#getStrikethroughKey(java.lang.String)
   */
  @Override
  protected String getStrikethroughKey(final String colorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(colorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
    }
    return super.getStrikethroughKey(colorKey);
  }

  /**
   * @see AbstractJavaScanner#getUnderlineKey(java.lang.String)
   */
  @Override
  protected String getUnderlineKey(final String colorKey) {
    if ((ANNOTATION_COLOR_KEY).equals(colorKey)) {
      return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
    }
    return super.getUnderlineKey(colorKey);
  }

  /**
   * @see AbstractJavaScanner#affectsBehavior(PropertyChangeEvent)
   */
  @Override
  public boolean affectsBehavior(final PropertyChangeEvent event) {
    return event.getProperty().equals(SOURCE_VERSION) || super.affectsBehavior(event);
  }

  /**
   * @see AbstractJavaScanner#adaptToPreferenceChange(PropertyChangeEvent)
   */
  @Override
  public void adaptToPreferenceChange(final PropertyChangeEvent event) {

    if (event.getProperty().equals(SOURCE_VERSION)) {
      final Object value = event.getNewValue();

      if (value instanceof String) {
        final String s = (String) value;

        for (final Iterator<ISourceVersionDependent> it = fVersionDependentRules.iterator(); it.hasNext();) {
          final ISourceVersionDependent dependent = it.next();
          dependent.setSourceVersion(s);
        }
      }

    }
    else if (super.affectsBehavior(event)) {
      super.adaptToPreferenceChange(event);
    }
  }
}
