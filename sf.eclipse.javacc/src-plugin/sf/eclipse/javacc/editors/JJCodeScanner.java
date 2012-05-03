package sf.eclipse.javacc.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.actions.JJFormat;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.options.PreferencesInitializer;

/**
 * A (not anymore so rudimentary) JavaCC code scanner coloring words and comments.<br>
 * Must be coherent with {@link JJDocumentProvider}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJCodeScanner extends BufferedRuleBasedScanner {

  // MMa 04/2009 : added different coloring objects, renamed some, added some JavaCC / JJTree keywords,
  // ........... : removed some, and added indentations and color preferences changes management
  // MMa 11/2009 : formatting & javadoc revision ; added JTB keywords
  // MMa 02/2010 : formatting and javadoc revision ; fixed /**/ syntax coloring
  // MMa 03/2010 : aligned with partitions in JJDocumentProvider ; renamed some variables

  /** The preference store */
  static IPreferenceStore      sStore;
  /** The indentation string */
  public static String         sIndentString;
  /** The special indentation string */
  public static String         sSpecIndentString;
  /** The preference change listener and its associated method */
  IPropertyChangeListener      jPrefListener  = new IPropertyChangeListener() {

                                                @Override
                                                public void propertyChange(final PropertyChangeEvent event) {
                                                  final String p = event.getProperty();
                                                  final Object ov = event.getOldValue();
                                                  final Object nv = event.getNewValue();
                                                  if ((p.equals(PreferencesInitializer.P_INDENT_CHAR)
                                                       || p.equals(PreferencesInitializer.P_INDENT_CHAR_NB)
                                                       || p.equals(PreferencesInitializer.P_JJKEYWORD)
                                                       || p.equals(PreferencesInitializer.P_JAVAKEYWORD)
                                                       || p.equals(PreferencesInitializer.P_STRING)
                                                       || p.equals(PreferencesInitializer.P_COMMENT)
                                                       || p.equals(PreferencesInitializer.P_JDOC_COMMENT)
                                                       || p.equals(PreferencesInitializer.P_NORMALLABEL)
                                                       || p.equals(PreferencesInitializer.P_PRIVATELABEL)
                                                       || p.equals(PreferencesInitializer.P_LEXICALSTATE)
                                                       || p.equals(PreferencesInitializer.P_REGEXPUNCT)
                                                       || p.equals(PreferencesInitializer.P_CHOICESPUNCT) || p.equals(PreferencesInitializer.P_DEFAULT))
                                                      && ov != nv) {
                                                    dispose();
                                                    loadPrefsAndInitRules();
                                                  }
                                                }
                                              };
  /** The JavaCC keywords coloring object */
  static Color                 cJJKEYWORD;
  /** The Java keywords coloring object */
  Color                        cJAVAKEYWORD;
  /** The Strings coloring object */
  Color                        cSTRING;
  /** The Java Comments coloring object */
  Color                        cCOMMENT;
  /** The Javadoc Comments coloring object */
  Color                        cJDOC_COMMENT;
  /** The Normal labels coloring object */
  Color                        cNORMALLABEL;
  /** The Private labels coloring object */
  Color                        cPRIVATELABEL;
  /** The Lexical states coloring object */
  Color                        cLEXICALSTATE;
  /** The Regular_expression Punctuations coloring object */
  Color                        cREGEXPUNCT;
  /** The Choices enclosing Punctuation coloring object */
  Color                        cCHOICESPUNCT;
  /** The Default coloring object */
  Color                        cDEFAULT;
  /** The JJTokenRule rule */
  JJTokenRule                  fJJTokenRule;

  /**
   * The JavaCC and JTB keywords
   */
  public static final String[] sJJkeywords   = {
      "BNF", //$NON-NLS-1$
      "BUILD_NODE_FILES", //$NON-NLS-1$
      "BUILD_PARSER", //$NON-NLS-1$
      "BUILD_TOKEN_MANAGER", //$NON-NLS-1$
      "CACHE_TOKENS", //$NON-NLS-1$
      "CHOICE_AMBIGUITY_CHECK", //$NON-NLS-1$
      "COMMON_TOKEN_ACTION", //$NON-NLS-1$
      "CSS", //$NON-NLS-1$
      "DEBUG_LOOKAHEAD", //$NON-NLS-1$
      "DEBUG_PARSER", //$NON-NLS-1$
      "DEBUG_TOKEN_MANAGER", //$NON-NLS-1$
      "EOF", //$NON-NLS-1$
      "EOL", //$NON-NLS-1$
      "ERROR_REPORTING", //$NON-NLS-1$
      "FORCE_LA_CHECK", //$NON-NLS-1$
      "GENERATE_ANNOTATIONS", //$NON-NLS-1$
      "GENERATE_CHAINED_EXCEPTION", //$NON-NLS-1$
      "GENERATE_GENERICS", //$NON-NLS-1$
      "GENERATE_STRING_BUILDER", //$NON-NLS-1$
      "GRAMMAR_ENCODING", //$NON-NLS-1$
      "IGNORE_CASE", //$NON-NLS-1$
      "JAVACODE", //$NON-NLS-1$
      "JAVA_UNICODE_ESCAPE", //$NON-NLS-1$
      "JDK_VERSION", //$NON-NLS-1$
      "JJTREE_OUTPUT_DIRECTORY", //$NON-NLS-1$
      "JTB_CL", //$NON-NLS-1$
      "JTB_D", //$NON-NLS-1$
      "JTB_DL", //$NON-NLS-1$
      "JTB_E", //$NON-NLS-1$
      "JTB_F", //$NON-NLS-1$
      "JTB_IA", //$NON-NLS-1$
      "JTB_JD", //$NON-NLS-1$
      "JTB_ND", //$NON-NLS-1$
      "JTB_NP", //$NON-NLS-1$
      "JTB_NPFX", //$NON-NLS-1$
      "JTB_NSFX", //$NON-NLS-1$
      "JTB_NS", //$NON-NLS-1$
      "JTB_O", //$NON-NLS-1$
      "JTB_P", //$NON-NLS-1$
      "JTB_PP", //$NON-NLS-1$
      "JTB_PRINTER", //$NON-NLS-1$
      "JTB_SCHEME", //$NON-NLS-1$
      "JTB_TK", //$NON-NLS-1$
      "JTB_VA", //$NON-NLS-1$
      "JTB_VP", //$NON-NLS-1$
      "JTB_W", //$NON-NLS-1$
      "KEEP_LINE_COLUMN", //$NON-NLS-1$
      "LOOKAHEAD", //$NON-NLS-1$
      "MORE", //$NON-NLS-1$
      "MULTI", //$NON-NLS-1$
      "NODE_CLASS", //$NON-NLS-1$
      "NODE_DEFAULT_VOID", //$NON-NLS-1$
      "NODE_EXTENDS", //$NON-NLS-1$
      "NODE_FACTORY", //$NON-NLS-1$
      "NODE_PACKAGE", //$NON-NLS-1$
      "NODE_PREFIX", //$NON-NLS-1$
      "NODE_SCOPE_HOOK", //$NON-NLS-1$
      "NODE_USES_PARSER", //$NON-NLS-1$
      "ONE_TABLE", //$NON-NLS-1$
      "OTHER_AMBIGUITY_CHECK", //$NON-NLS-1$
      "OUTPUT_DIRECTORY", //$NON-NLS-1$
      "OUTPUT_FILE", //$NON-NLS-1$
      "PARSER_BEGIN", //$NON-NLS-1$
      "PARSER_END", //$NON-NLS-1$
      "SANITY_CHECK", //$NON-NLS-1$
      "SKIP", //$NON-NLS-1$
      "SPECIAL_TOKEN", //$NON-NLS-1$
      "STATIC", //$NON-NLS-1$
      "SUPPORT_CLASS_VISIBILITY_PUBLIC", //$NON-NLS-1$
      "TEXT", //$NON-NLS-1$
      "TOKEN", //$NON-NLS-1$
      "TOKEN_EXTENDS", //$NON-NLS-1$
      "TOKEN_FACTORY", //$NON-NLS-1$
      "TOKEN_MANAGER_USES_PARSER", //$NON-NLS-1$
      "TOKEN_MGR_DECLS", //$NON-NLS-1$
      "TRACK_TOKENS", //$NON-NLS-1$
      "UNICODE_INPUT", //$NON-NLS-1$
      "USER_CHAR_STREAM", //$NON-NLS-1$
      "USER_TOKEN_MANAGER", //$NON-NLS-1$
      "VISITOR", //$NON-NLS-1$
      "VISITOR_DATA_TYPE", //$NON-NLS-1$
      "VISITOR_EXCEPTION", //$NON-NLS-1$
      "VISITOR_RETURN_TYPE", //$NON-NLS-1$
      "options"                              }; //$NON-NLS-1$
  /**
   * The java keywords
   */
  public static final String[] sJavaKeywords = {
      "abstract", //$NON-NLS-1$
      "boolean", //$NON-NLS-1$
      "break", //$NON-NLS-1$
      "byte", //$NON-NLS-1$
      "case", //$NON-NLS-1$
      "catch", //$NON-NLS-1$
      "char", //$NON-NLS-1$
      "class", //$NON-NLS-1$
      "const", //$NON-NLS-1$
      "continue", //$NON-NLS-1$
      "default", //$NON-NLS-1$
      "do", //$NON-NLS-1$
      "double", //$NON-NLS-1$
      "else", //$NON-NLS-1$
      "extends", //$NON-NLS-1$
      "false", //$NON-NLS-1$
      "final", //$NON-NLS-1$
      "finally", //$NON-NLS-1$
      "float", //$NON-NLS-1$
      "for", //$NON-NLS-1$
      "goto", //$NON-NLS-1$
      "if", //$NON-NLS-1$
      "implements", //$NON-NLS-1$
      "import", //$NON-NLS-1$
      "instanceof", //$NON-NLS-1$
      "int", //$NON-NLS-1$
      "interface", //$NON-NLS-1$
      "long", //$NON-NLS-1$
      "native", //$NON-NLS-1$
      "new", //$NON-NLS-1$
      "null", //$NON-NLS-1$
      "package", //$NON-NLS-1$
      "private", //$NON-NLS-1$
      "protected", //$NON-NLS-1$
      "public", //$NON-NLS-1$
      "return", //$NON-NLS-1$
      "short", //$NON-NLS-1$
      "static", //$NON-NLS-1$
      "super", //$NON-NLS-1$
      "switch", //$NON-NLS-1$
      "synchronized", //$NON-NLS-1$
      "this", //$NON-NLS-1$
      "throw", //$NON-NLS-1$
      "throws", //$NON-NLS-1$
      "transient", //$NON-NLS-1$
      "true", //$NON-NLS-1$
      "try", //$NON-NLS-1$
      "void", //$NON-NLS-1$
      "volatile", //$NON-NLS-1$
      "while", //$NON-NLS-1$
      "void", //$NON-NLS-1$
      "boolean", //$NON-NLS-1$
      "char", //$NON-NLS-1$
      "byte", //$NON-NLS-1$
      "short", //$NON-NLS-1$
      "strictfp", //$NON-NLS-1$
      "int", //$NON-NLS-1$
      "long", //$NON-NLS-1$
      "float", //$NON-NLS-1$
      "double", //$NON-NLS-1$
      "false", //$NON-NLS-1$
      "null", //$NON-NLS-1$
      "true"                                 }; //$NON-NLS-1$

  /**
   * Standard constructor, which initializes variables deriving from preferences and registers a listener.
   */
  public JJCodeScanner() {
    super();
    sStore = Activator.getDefault().getPreferenceStore();
    loadPrefsAndInitRules();
  }

  /**
   * Loads preference from the store, adds a listener and initializes the rules.
   */
  void loadPrefsAndInitRules() {
    sStore.addPropertyChangeListener(jPrefListener);
    setIndentString();
    setSpecIndentString();
    fRules = createRules();
  }

  /**
   * Calls all dispose() methods and unregisters the listener.
   */
  public void dispose() {
    if (cJJKEYWORD != null) {
      cJJKEYWORD.dispose();
      cJAVAKEYWORD.dispose();
      cSTRING.dispose();
      cCOMMENT.dispose();
      cJDOC_COMMENT.dispose();
      cNORMALLABEL.dispose();
      cPRIVATELABEL.dispose();
      cLEXICALSTATE.dispose();
      cREGEXPUNCT.dispose();
      cCHOICESPUNCT.dispose();
      cDEFAULT.dispose();
      cJJKEYWORD = null;
      cJAVAKEYWORD = null;
      cSTRING = null;
      cCOMMENT = null;
      cJDOC_COMMENT = null;
      cNORMALLABEL = null;
      cPRIVATELABEL = null;
      cLEXICALSTATE = null;
      cREGEXPUNCT = null;
      cCHOICESPUNCT = null;
      cDEFAULT = null;
      if (sStore != null) {
        sStore.removePropertyChangeListener(jPrefListener);
      }
    }
  }

  /**
   * Creates the rules tokens in the rules array.
   * 
   * @return the rules array
   */
  public IRule[] createRules() {
    final Display display = Display.getCurrent();
    // get color preferences
    cJJKEYWORD = new Color(display,
                           PreferenceConverter.getColor(sStore, PreferencesInitializer.P_JJKEYWORD));
    cJAVAKEYWORD = new Color(display, PreferenceConverter.getColor(sStore,
                                                                   PreferencesInitializer.P_JAVAKEYWORD));
    cSTRING = new Color(display, PreferenceConverter.getColor(sStore, PreferencesInitializer.P_STRING));
    cCOMMENT = new Color(display, PreferenceConverter.getColor(sStore, PreferencesInitializer.P_COMMENT));
    cJDOC_COMMENT = new Color(display, PreferenceConverter.getColor(sStore,
                                                                    PreferencesInitializer.P_JDOC_COMMENT));
    cNORMALLABEL = new Color(display, PreferenceConverter.getColor(sStore,
                                                                   PreferencesInitializer.P_NORMALLABEL));
    cPRIVATELABEL = new Color(display, PreferenceConverter.getColor(sStore,
                                                                    PreferencesInitializer.P_PRIVATELABEL));
    cLEXICALSTATE = new Color(display, PreferenceConverter.getColor(sStore,
                                                                    PreferencesInitializer.P_LEXICALSTATE));
    cREGEXPUNCT = new Color(display, PreferenceConverter.getColor(sStore,
                                                                  PreferencesInitializer.P_REGEXPUNCT));
    cCHOICESPUNCT = new Color(display, PreferenceConverter.getColor(sStore,
                                                                    PreferencesInitializer.P_CHOICESPUNCT));
    cDEFAULT = new Color(display, PreferenceConverter.getColor(sStore, PreferencesInitializer.P_DEFAULT));
    // create rules tokens
    final IToken jjKwRTk = new Token(new TextAttribute(cJJKEYWORD, null, SWT.BOLD));
    final IToken javaKwRTk = new Token(new TextAttribute(cJAVAKEYWORD, null, SWT.BOLD));
    final IToken strRTk = new Token(new TextAttribute(cSTRING, null, 0));
    final IToken comRTk = new Token(new TextAttribute(cCOMMENT, null, 0));
    final IToken jdocComRTk = new Token(new TextAttribute(cJDOC_COMMENT, null, 0));
    final IToken normLblRTk = new Token(new TextAttribute(cNORMALLABEL, null, 0));
    final IToken privLblRTk = new Token(new TextAttribute(cPRIVATELABEL, null, 0));
    final IToken lexStRTk = new Token(new TextAttribute(cLEXICALSTATE, null, SWT.ITALIC));
    final IToken regexPunRTk = new Token(new TextAttribute(cREGEXPUNCT, null, SWT.BOLD));
    final IToken chPunRTk = new Token(new TextAttribute(cCHOICESPUNCT, null, SWT.BOLD));
    final IToken fOther = new Token(new TextAttribute(cDEFAULT, null, 0));
    // set the default return token
    setDefaultReturnToken(fOther);
    // create the rules and fill the rules array
    // rule for single line comment
    final EndOfLineRule singln_com = new EndOfLineRule("//", comRTk); //$NON-NLS-1$
    // rules for strings and character constants
    final SingleLineRule str_cst = new SingleLineRule("\"", "\"", strRTk, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    final SingleLineRule char_cst = new SingleLineRule("'", "'", strRTk, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    // rules for multi line comments
    final WordPatternRule empty_com = new WordPatternRule(new JJSMLJCDetector(), "/**", "/", comRTk); //$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule javadoc_com = new MultiLineRule("/**", "*/", jdocComRTk, (char) 0, true);//$NON-NLS-1$ //$NON-NLS-2$
    final MultiLineRule multln_com = new MultiLineRule("/*", "*/", comRTk, (char) 0, true);//$NON-NLS-1$ //$NON-NLS-2$
    // rule for JavaCC and Java keywords
    final WordRule keyword = new WordRule(new JJWordDetector(), fOther);
    // color JavaCC keywords
    for (final String kw : sJJkeywords) {
      keyword.addWord(kw, jjKwRTk);
    }
    // color Java keywords
    for (final String kw : sJavaKeywords) {
      keyword.addWord(kw, javaKwRTk);
    }
    // rule for JavaCC syntax
    fJJTokenRule = new JJTokenRule(normLblRTk, privLblRTk, lexStRTk, regexPunRTk, chPunRTk);
    // set rules (same order as JJDocumentProvider ?)
    final IRule[] rules = {
        singln_com, str_cst, char_cst, empty_com, javadoc_com, multln_com, keyword, fJJTokenRule };
    return rules;
  }

  /**
   * Reinitializes state before (re)processing the document.
   * 
   * @see BufferedRuleBasedScanner#setRange(IDocument, int, int)
   */
  @Override
  public void setRange(final IDocument aDoc, final int aOffset, final int aLength) {
    fJJTokenRule.reinit();
    super.setRange(aDoc, aOffset, aLength);
  }

  /**
   * Computes the special indentation string after a '|' from the store preferences : if the indentation
   * character is space, return a string with the number of indentation characters minus one, and if the
   * indentation character is tab, return a string with the number of indentation characters.
   */
  public static void setSpecIndentString() {
    final String idstr = (sStore.getBoolean(PreferencesInitializer.P_INDENT_CHAR) ? JJFormat.TAB
                                                                                   : JJFormat.SPACE);
    int nbch = sStore.getInt(PreferencesInitializer.P_INDENT_CHAR_NB);
    if (" ".equals(idstr)) { //$NON-NLS-1$
      nbch--;
    }
    final StringBuffer sb = new StringBuffer(nbch);
    for (int i = 0; i < nbch; i++) {
      sb.append(idstr);
    }
    sSpecIndentString = sb.toString();
  }

  /**
   * Gets the special indentation string (for after a '|')
   * 
   * @return the special indentation string
   */
  public static String getSpecIndentString() {
    if (sSpecIndentString == null) {
      JJCodeScanner.setSpecIndentString();
    }
    return sSpecIndentString;
  }

  /**
   * Computes the indentation string from the store preferences.
   */
  public static void setIndentString() {
    final int nbch = sStore.getInt(PreferencesInitializer.P_INDENT_CHAR_NB);
    final String idstr = (sStore.getBoolean(PreferencesInitializer.P_INDENT_CHAR) ? JJFormat.TAB
                                                                                   : JJFormat.SPACE);
    final StringBuffer sb = new StringBuffer(nbch);
    for (int i = 0; i < nbch; i++) {
      sb.append(idstr);
    }
    sIndentString = sb.toString();
  }

  /**
   * Gets the indentation string.
   * 
   * @return the indentation string
   */
  public static String getIndentString() {
    if (sIndentString == null) {
      JJCodeScanner.setIndentString();
    }
    return sIndentString;
  }

}
