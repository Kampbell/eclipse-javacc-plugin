package sf.eclipse.javacc.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * A rudimentary JavaCC code scanner
 * coloring words and comments.
 * @see org.eclipse.jdt.internal.ui.text.java.JavaCodeScanner
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJCodeScanner extends RuleBasedScanner {
  /** The color keys */
  public static final int JJKEYWORD = SWT.COLOR_DARK_GREEN;
  public static final int JAVAKEYWORD = SWT.COLOR_DARK_RED;
  public static final int BACKGROUND = SWT.COLOR_WIDGET_BACKGROUND;
  public static final int STRING = SWT.COLOR_BLUE;
  public static final int COMMENT = SWT.COLOR_DARK_GREEN;
  public static final int JDOC_COMMENT = SWT.COLOR_DARK_BLUE;
  public static final int DEFAULT = SWT.COLOR_BLACK;

  public static final String[] fgJJkeywords =
    {
      "options",
      "LOOKAHEAD",
      "IGNORE_CASE",
      "PARSER_BEGIN",
      "PARSER_END",
      "JAVACODE",
      "TOKEN",
      "SPECIAL_TOKEN",
      "MORE",
      "SKIP",
      "TOKEN_MGR_DECLS",
      "EOF",
	  "EOL",
	  "CHOICE_AMBIGUITY_CHECK",
	  "OTHER_AMBIGUITY_CHECK",
	  "STATIC",
	  "DEBUG_PARSER",
	  "DEBUG_LOOKAHEAD",
	  "DEBUG_TOKEN_MANAGER",
	  "OPTIMIZE_TOKEN_MANAGER",
	  "ERROR_REPORTING",
	  "JAVA_UNICODE_ESCAPE",
	  "UNICODE_INPUT",
	  "IGNORE_CASE",
	  "COMMON_TOKEN_ACTION",
	  "USER_TOKEN_MANAGER",
	  "USER_CHAR_STREAM",
	  "BUILD_PARSER",
	  "BUILD_TOKEN_MANAGER",
	  "SANITY_CHECK",
	  "FORCE_LA_CHECK",
	  "CACHE_TOKENS",
	  "KEEP_LINE_COLUMN",
	  "OUTPUT_DIRECTORY"
	  };
  
  public static final String[] fgJavaKeywords =
    {
      "abstract",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "const",
      "continue",
      "default",
      "do",
      "double",
      "else",
      "extends",
      "false",
      "final",
      "finally",
      "float",
      "for",
      "goto",
      "if",
      "implements",
      "import",
      "instanceof",
      "int",
      "interface",
      "long",
      "native",
      "new",
      "null",
      "package",
      "private",
      "protected",
      "public",
      "return",
      "short",
      "static",
      "super",
      "switch",
      "synchronized",
      "this",
      "throw",
      "throws",
      "transient",
      "true",
      "try",
      "void",
      "volatile",
      "while",
      "void",
      "boolean",
      "char",
      "byte",
      "short",
      "strictfp",
      "int",
      "long",
      "float",
      "double",
      "false",
      "null",
      "true" };

  /**
   * Creates a JavaCC code scanner
   */
  public JJCodeScanner() {
    super();
    fRules = createRules();
  }

  /**
   * Create Rules
   */
  public IRule[] createRules() {
    Display display = Display.getCurrent();
    Color cJJKEYWORD = display.getSystemColor(JJKEYWORD);
    Color cJAVAKEYWORD = display.getSystemColor(JAVAKEYWORD);
    Color cBACKGROUND = null; //display.getSystemColor(BACKGROUND);
    Color cSTRING = display.getSystemColor(STRING);
    Color cCOMMENT = display.getSystemColor(COMMENT);
    Color cJDOC_COMMENT = display.getSystemColor(JDOC_COMMENT);
    Color cDEFAULT = display.getSystemColor(DEFAULT);

    IToken jjkeyword =
      new Token(new TextAttribute(cJJKEYWORD, cBACKGROUND, SWT.BOLD));
    IToken keyword =
      new Token(new TextAttribute(cJAVAKEYWORD, cBACKGROUND, SWT.BOLD));
    IToken string = new Token(new TextAttribute(cSTRING));
    IToken comment = new Token(new TextAttribute(cCOMMENT));
    IToken jdocComment = new Token(new TextAttribute(cJDOC_COMMENT));
    IToken other = new Token(new TextAttribute(cDEFAULT));

    List rules = new ArrayList();

    // Add rules for comments.
    rules.add(new EndOfLineRule("//", comment));
    rules.add(new MultiLineRule("/**", "*/", jdocComment));
    rules.add(new MultiLineRule("/*", "*/", comment));

    // Add rule for strings and character constants.
    rules.add(new SingleLineRule("\"", "\"", string, '\\'));
    rules.add(new SingleLineRule("'", "'", string, '\\'));

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
