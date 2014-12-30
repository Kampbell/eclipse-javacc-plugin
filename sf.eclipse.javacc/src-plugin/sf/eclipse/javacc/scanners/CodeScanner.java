package sf.eclipse.javacc.scanners;

import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.UnusedDocumentProvider;
import sf.eclipse.javacc.handlers.Format;

/**
 * A (not anymore so rudimentary) JavaCC code scanner for coloring tokens.<br>
 * Must be coherent with {@link UnusedDocumentProvider}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 * @author Bill Fenlason 2012
 */
public class CodeScanner extends RuleBasedScanner {

  // MMa 04/2009 : added different coloring objects, renamed some, added some JavaCC / JJTree keywords,
  // ........... : removed some, and added indentations and color preferences changes management
  // MMa 11/2009 : formatting & javadoc revision ; added JTB keywords
  // MMa 02/2010 : formatting and javadoc revision ; fixed /**/ syntax coloring
  // MMa 03/2010 : aligned with partitions in DocumentProvider ; renamed some variables
  // BF  05/2012 : moved comment related code to CommentScanner
  // BF  05/2012 : moved preference constants to IPrefConstants
  // BF  05/2012 : added preferences for font attributes (BOLD etc) and other new preference colors
  // BF  05/2012 : java keywords moved to JavaCodeRule, javaCC keywords moved to JavaCCCodeRule
  // BF  06/2012 : rewrite to use HashMap, delay color disposal, many color preferences
  // MMa 10/2012 : suppressed the hiding field fDocument ; renamed
  // MMa 11/2014 : some renamings

  /** The color preference names */
  protected static final String[]       sColorPrefNames  = {
      // Order significant, matching pairs
      P_JAVA_BLOCK_BRACE, // 
      P_JAVA_KEYWORD, // 
      P_JAVA_IDENTIFIER, // 
      P_JAVA_STRING, //
      P_JAVA_NUMERIC, // 
      P_JAVA_PUNCTUATION, // 
      P_JAVA_DEFAULT_TEXT, // 
      P_JAVA_BACKGROUND, //
      P_JAVACC_KEYWORD, // 
      P_JAVACC_EXPANSION_BRACE, //
      P_JAVACC_CHOICE_PUNCT, // 
      P_JAVACC_OPTION, //
      P_JAVACC_OPTION_BRACE, //
      P_JAVACC_PARSER_NAME, //
      P_JAVACC_PARSER_NAME_PAREN, //
      P_JAVACC_OTHER_PUNCT, //
      P_JAVACC_DEFAULT_TEXT, //
      P_JAVACC_BACKGROUND, //      
      P_JAVACC_NUMERIC, //
      P_TOKEN_LABEL, //
      P_TOKEN_LABEL_PUNCT, //
      P_TOKEN_LABEL_DEF, //
      P_TOKEN_LABEL_PRIVATE_DEF, //
      P_TOKEN_LABEL_PRIVATE_DEF_PUNCT, //
      P_LEXICAL_STATE, //
      P_LEXICAL_STATE_PUNCT, //
      P_LEXICAL_STATE_NEXT, //
      P_REG_EX_BRACE, //
      P_JAVACC_STRING, // 
      P_REG_EX_BRACKET, //
      P_REG_EX_TOKEN_PUNCT, // 
      P_REG_EX_CHOICE_PUNCT, //
      P_REG_EX_OTHER_PUNCT, //
      P_JJTREE_NODE_NAME_PUNCT, //
      P_JJTREE_NODE_NAME, //
      P_JJTREE_NODE_EXPR_PAREN, //
                                                         };

  /** The font attribute preference names */
  protected static final String[]       sAtrPrefNames    = {
      // Order significant, matching pairs
      P_JAVA_BLOCK_BRACE_ATR, // 
      P_JAVA_KEYWORD_ATR, // 
      P_JAVA_IDENTIFIER_ATR, // 
      P_JAVA_STRING_ATR, // 
      P_JAVA_NUMERIC_ATR, // 
      P_JAVA_PUNCTUATION_ATR, //
      P_JAVA_DEFAULT_TEXT_ATR, // 
      P_JAVACC_KEYWORD_ATR, // 
      P_JAVACC_EXPANSION_BRACE_ATR, //
      P_JAVACC_CHOICE_PUNCT_ATR, //
      P_JAVACC_OPTION_ATR, //
      P_JAVACC_OPTION_BRACE_ATR, //
      P_JAVACC_PARSER_NAME_ATR, //
      P_JAVACC_PARSER_NAME_PAREN_ATR, //
      P_JAVACC_OTHER_PUNCT_ATR, // 
      P_JAVACC_DEFAULT_TEXT_ATR, //
      P_JAVACC_NUMERIC_ATR, //
      P_TOKEN_LABEL_ATR, //
      P_TOKEN_LABEL_PUNCT_ATR, //
      P_TOKEN_LABEL_DEF_ATR, //
      P_TOKEN_LABEL_PRIVATE_DEF_ATR, // 
      P_TOKEN_LABEL_PRIVATE_DEF_PUNCT_ATR, //
      P_LEXICAL_STATE_ATR, // 
      P_LEXICAL_STATE_PUNCT_ATR, //
      P_LEXICAL_STATE_NEXT_ATR, //
      P_REG_EX_BRACE_ATR, //
      P_JAVACC_STRING_ATR, //
      P_REG_EX_BRACKET_ATR, //
      P_REG_EX_TOKEN_PUNCT_ATR, //
      P_REG_EX_CHOICE_PUNCT_ATR, //
      P_REG_EX_OTHER_PUNCT_ATR, //      
      P_JJTREE_NODE_NAME_PUNCT_ATR, //
      P_JJTREE_NODE_NAME_ATR, //
      P_JJTREE_NODE_EXPR_PAREN_ATR, //
                                                         };

  /** The indentation string */
  private static String                 sIndentString;

  /** The special indentation string */
  private static String                 sSpecIndentString;

  /** The color map */
  final Map<String, Color>              jColorMap        = new HashMap<String, Color>();

  /** The attribute map */
  final Map<String, Integer>            jAtrMap          = new HashMap<String, Integer>();

  /** The unused (not yet disposed) color list */
  final List<Color>                     jOldColors       = new ArrayList<Color>();

  /** The display */
  final Display                         jDisplay         = Display.getCurrent();

  /** The preference store */
  static final IPreferenceStore         sStore           = AbstractActivator.getDefault().getPreferenceStore();

  /** The preference change listener and its associated method */
  private final IPropertyChangeListener jPrefListener    = new IPropertyChangeListener() {

                                                           /** {@inheritDoc} */
                                                           @Override
                                                           public void propertyChange(final PropertyChangeEvent event) {
                                                             final String p = event.getProperty();

                                                             if (!event.getOldValue()
                                                                       .equals(event.getNewValue())) {

                                                               if (jColorMap.containsKey(p)) {
                                                                 jOldColors.add(jColorMap.get(p));
                                                                 jColorMap.put(p,
                                                                               new Color(
                                                                                         jDisplay,
                                                                                         PreferenceConverter.getColor(sStore,
                                                                                                                      p)));
                                                                 updateRules(p);
                                                               }

                                                               else if (jAtrMap.containsKey(p)) {
                                                                 jAtrMap.put(p, new Integer(sStore.getInt(p)));
                                                                 updateRules(p);
                                                               }

                                                               else if (p == P_JAVA_BLOCK_BRACE_ALT_BG) {
                                                                 updateRules(P_JAVA_BLOCK_BRACE); // Not p !
                                                               }

                                                               else if (p == P_INDENT_CHAR
                                                                        || p == P_INDENT_CHAR_NB) {
                                                                 setIndentString();
                                                                 setSpecIndentString();
                                                               }
                                                             }
                                                           }
                                                         };

  /** The JavaCC code rule */
  private final JavaCCCodeRule          jJavaCCCodeRule;

  /** The last document range start */
  private int                           jLastRangeOffset = -1;

  /**
   * Instantiates a new JavaCC code scanner.
   */
  public CodeScanner() {
    super();
    sStore.addPropertyChangeListener(jPrefListener);

    for (final String p : sColorPrefNames) {
      jColorMap.put(p, new Color(jDisplay, PreferenceConverter.getColor(sStore, p)));
    }
    for (final String p : sAtrPrefNames) {
      jAtrMap.put(p, new Integer(sStore.getInt(p)));
    }

    jJavaCCCodeRule = new JavaCCCodeRule(jColorMap, jAtrMap);
    setRules(new IRule[] {
      jJavaCCCodeRule });

    setIndentString();
    setSpecIndentString();
  }

  /**
   * Update the rules after a preference change.
   * 
   * @param preferenceName - the preference name
   */
  void updateRules(final String preferenceName) {
    jJavaCCCodeRule.updateRules(preferenceName);
  }

  /** {@inheritDoc} */
  @Override
  public void setRange(final IDocument document, final int offset, final int length) {
    super.setRange(document, offset, length);
    fDocument = document;

    if (jLastRangeOffset < 0 || jLastRangeOffset >= offset) {
      jJavaCCCodeRule.initialize();
    }
    jLastRangeOffset = offset;
  }

  /**
   * Returns the next token using the specified rule.
   * 
   * @param rule - the rule
   * @return the next token
   */
  public IToken nextToken(final IRule rule) {
    fTokenOffset = fOffset;
    fColumn = UNDEFINED;

    final IToken token = (rule.evaluate(this));
    if (!token.isUndefined()) {
      return token;
    }
    if (read() == EOF) {
      return Token.EOF;
    }
    return fDefaultReturnToken;
  }

  /**
   * Returns the next token using the specified rules array.
   * 
   * @param rules - the rules array
   * @return the next token
   */
  public IToken nextToken(final IRule[] rules) {
    fTokenOffset = fOffset;
    fColumn = UNDEFINED;

    if (rules != null) {
      for (int i = 0; i < rules.length; i++) {
        final IToken token = (rules[i].evaluate(this));
        if (!token.isUndefined()) {
          return token;
        }
      }
    }
    if (read() == EOF) {
      return Token.EOF;
    }
    return fDefaultReturnToken;
  }

  /**
   * Gets the document.
   * 
   * @return the Document
   */
  public IDocument getDocument() {
    return fDocument;
  }

  /**
   * Disposes the old (currently unused by StyledTextWidget) colors.
   */
  private void disposeUnusedColors() {
    synchronized (jOldColors) {
      while (!jOldColors.isEmpty()) {
        final Color oldColor = jOldColors.remove(jOldColors.size() - 1);
        if (oldColor != null) {
          oldColor.dispose();
        }
      }
    }
  }

  /**
   * Calls all dispose() methods and unregisters the listener.
   */
  public void dispose() {
    synchronized (jOldColors) {
      for (final String p : sColorPrefNames) {
        jOldColors.add(jColorMap.remove(p));
      }
      disposeUnusedColors();
      sStore.removePropertyChangeListener(jPrefListener);
    }
  }

  /**
   * Computes the special indentation string after a '|' from the store preferences : if the indentation
   * character is space, return a string with the number of indentation characters minus one, and if the
   * indentation character is tab, return a string with the number of indentation characters.
   */
  public static void setSpecIndentString() {
    final String idstr = (sStore.getBoolean(P_INDENT_CHAR) ? Format.TAB : Format.SPACE);
    int nbch = sStore.getInt(P_INDENT_CHAR_NB);
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
   * Gets the special indentation string (for after a '|').
   * 
   * @return the special indentation string
   */
  public static String getSpecIndentString() {
    if (sSpecIndentString == null) {
      CodeScanner.setSpecIndentString();
    }
    return sSpecIndentString;
  }

  /**
   * Computes the indentation string from the store preferences.
   */
  public static void setIndentString() {
    final int nbch = sStore.getInt(P_INDENT_CHAR_NB);
    final String idstr = (sStore.getBoolean(P_INDENT_CHAR) ? Format.TAB : Format.SPACE);
    final StringBuilder sb = new StringBuilder(nbch);
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
      CodeScanner.setIndentString();
    }
    return sIndentString;
  }

}
