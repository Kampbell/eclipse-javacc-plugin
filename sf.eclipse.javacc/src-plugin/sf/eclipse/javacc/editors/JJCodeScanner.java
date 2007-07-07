package sf.eclipse.javacc.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.options.JJPreferences;

/**
 * A rudimentary JavaCC code scanner
 * coloring words and comments.
 * @look org.eclipse.jdt.internal.ui.text.java.JavaCodeScanner
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJCodeScanner extends BufferedRuleBasedScanner {
  Color cJJKEYWORD;
  Color cJAVAKEYWORD;
  Color cBACKGROUND;
  Color cSTRING;
  Color cCOMMENT;
  Color cJDOC_COMMENT;
  Color cTOKEN;
  Color cPTOKEN;
  Color cDEFAULT;
  
  public static final String[] fgJJkeywords = {
      "options", //$NON-NLS-1$
      "LOOKAHEAD", //$NON-NLS-1$
      "IGNORE_CASE", //$NON-NLS-1$
      "PARSER_BEGIN", //$NON-NLS-1$
      "PARSER_END", //$NON-NLS-1$
      "JAVACODE", //$NON-NLS-1$
      "TOKEN", //$NON-NLS-1$
      "SPECIAL_TOKEN", //$NON-NLS-1$
      "MORE", //$NON-NLS-1$
      "MULTI", //$NON-NLS-1$
      "SKIP", //$NON-NLS-1$
      "TOKEN_MGR_DECLS", //$NON-NLS-1$
      "EOF", //$NON-NLS-1$
	  "EOL", //$NON-NLS-1$
	  "CHOICE_AMBIGUITY_CHECK", //$NON-NLS-1$
	  "OTHER_AMBIGUITY_CHECK", //$NON-NLS-1$
	  "STATIC", //$NON-NLS-1$
	  "DEBUG_PARSER", //$NON-NLS-1$
	  "DEBUG_LOOKAHEAD", //$NON-NLS-1$
	  "DEBUG_TOKEN_MANAGER", //$NON-NLS-1$
	  "OPTIMIZE_TOKEN_MANAGER", //$NON-NLS-1$
	  "ERROR_REPORTING", //$NON-NLS-1$
	  "JAVA_UNICODE_ESCAPE", //$NON-NLS-1$
	  "UNICODE_INPUT", //$NON-NLS-1$
	  "IGNORE_CASE", //$NON-NLS-1$
	  "COMMON_TOKEN_ACTION", //$NON-NLS-1$
	  "USER_TOKEN_MANAGER", //$NON-NLS-1$
	  "USER_CHAR_STREAM", //$NON-NLS-1$
	  "BUILD_PARSER", //$NON-NLS-1$
	  "BUILD_TOKEN_MANAGER", //$NON-NLS-1$
	  "SANITY_CHECK", //$NON-NLS-1$
	  "FORCE_LA_CHECK", //$NON-NLS-1$
	  "CACHE_TOKENS", //$NON-NLS-1$
	  "KEEP_LINE_COLUMN", //$NON-NLS-1$
	  "OUTPUT_DIRECTORY", //$NON-NLS-1$
	  "NODE_DEFAULT_VOID", //$NON-NLS-1$
	  "NODE_PREFIX", //$NON-NLS-1$
	  "NODE_PACKAGE", //$NON-NLS-1$
	  "NODE_SCOPE_HOOK", //$NON-NLS-1$
	  "NODE_FACTORY", //$NON-NLS-1$
	  "NODE_USES_PARSER", //$NON-NLS-1$
	  "BUILD_NODE_FILES", //$NON-NLS-1$
	  "VISITOR", //$NON-NLS-1$
	  "VISITOR_EXCEPTION", //$NON-NLS-1$
	  "OUTPUT_FILE", //$NON-NLS-1$
      "JDK_VERSION", //$NON-NLS-1$
      "NODE_EXTENDS", //$NON-NLS-1$
      "TOKEN_MANAGER_USES_PARSER", //$NON-NLS-1$
      "JJTREE_OUTPUT_DIRECTORY",
      "ONE_TABLE", //$NON-NLS-1$
      "TEXT", //$NON-NLS-1$
      "JJDOC_OUTPUT_FILE", //$NON-NLS-1$ 
      "JJDOC_OUTPUT_DIRECTORY"//$NON-NLS-1$
	  };
  
  public static final String[] fgJavaKeywords = {
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
      "true" }; //$NON-NLS-1$

  /**
   * Creates a JavaCC code scanner
   */
  public JJCodeScanner() {
    super();
    fRules = createRules();
  }
  
  public void dispose() {
    if (cJJKEYWORD != null) {
      cJJKEYWORD.dispose();
      cJJKEYWORD= null;
      cJAVAKEYWORD.dispose();
      cSTRING.dispose();
      cCOMMENT.dispose();
      cJDOC_COMMENT.dispose();
      cTOKEN.dispose();
      cPTOKEN.dispose();
      cDEFAULT.dispose();
    }
  }
  
  /**
   * Create Rules
   */
  public IRule[] createRules() {
    Display display = Display.getCurrent();
    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    
    cJJKEYWORD = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_JJKEYWORD));
    cJAVAKEYWORD = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_JAVAKEYWORD));
    cSTRING = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_STRING));
    cCOMMENT = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_COMMENT));
    cJDOC_COMMENT = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_JDOC_COMMENT));
    cTOKEN = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_TOKEN));
    cPTOKEN = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_PTOKEN));
    cDEFAULT = new Color(display, 
        PreferenceConverter.getColor(store, JJPreferences.P_DEFAULT));

    IToken jjkeyword = new Token(new TextAttribute(cJJKEYWORD, null, SWT.BOLD));
    IToken keyword = new Token(new TextAttribute(cJAVAKEYWORD, null, SWT.BOLD));
    IToken string = new Token(new TextAttribute(cSTRING, null, 0));
    IToken comment = new Token(new TextAttribute(cCOMMENT, null, 0));
    IToken jdocComment = new Token(new TextAttribute(cJDOC_COMMENT, null, 0));
    IToken token = new Token(new TextAttribute(cTOKEN, null, 0));
    IToken ptoken = new Token(new TextAttribute(cPTOKEN, null, 0));
    IToken other = new Token(new TextAttribute(cDEFAULT, null, 0));

    List<IRule> rules = new ArrayList<IRule>();

    // Add rules for comments.
    rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$
    rules.add(new MultiLineRule("/**", "*/", jdocComment)); //$NON-NLS-1$ //$NON-NLS-2$
    rules.add(new MultiLineRule("/*", "*/", comment)); //$NON-NLS-1$ //$NON-NLS-2$

    // Add rule for strings and character constants.
    rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rules.add(new SingleLineRule("< #", ">", ptoken, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rules.add(new SingleLineRule("<#", ">", ptoken, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
    rules.add(new SingleLineRule("<", ">", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$

    // Add word rule for JJKeywords and JavaKeywords.
    WordRule wordRule = new WordRule(new JJWordDetector(), other);
    // JJ keyword in dark green
    for (int i = 0; i < fgJJkeywords.length; i++)
      wordRule.addWord(fgJJkeywords[i], jjkeyword);
    // Java keywords in dark red
    for (int i = 0; i < fgJavaKeywords.length; i++)
      wordRule.addWord(fgJavaKeywords[i], keyword);
    rules.add(wordRule);

    setDefaultReturnToken(other);

    IRule[] result = new IRule[rules.size()];
    rules.toArray(result);
    return result;
  }

  /**
  * A JavaCC word detector.
  */
  class JJWordDetector implements IWordDetector {

    /**
     * @see IWordDetector#isWordStart
     */
    public boolean isWordStart(char c) {
      return Character.isJavaIdentifierStart(c);
    }

    /**
     * @see IWordDetector#isWordPart
     */
    public boolean isWordPart(char c) {
      return Character.isJavaIdentifierPart(c);
    }
  }
}
