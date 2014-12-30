package sf.eclipse.javacc.preferences;

import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * Class used to initialize default preference values.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.core.runtime.preferences">.<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 * @author Bill Fenlason 2012
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

  // MMa : added / renamed colors and indentation preferences
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added check spelling
  // MMa 03/2010 : enhanced layout (groups / tool tips) ; renamed preference keys
  // MMa 08/2011 : renamed
  // BF  05/2012 : added font attribute (bold, italic) preferences
  // BF  05/2012 : moved preference constants to IPrefConstants
  // BF  05/2012 : changed comments to block comments and line comments
  // BF  06/2012 : major revisions to simplify and for new tabbed preference page

  /** {@inheritDoc} */
  @Override
  public void initializeDefaultPreferences() {
    final IPreferenceStore store = AbstractActivator.getDefault().getPreferenceStore();

    store.setValue(P_HYPERLINK_COLOR_SYSTEM_DEFAULT, false);
    store.setDefault(P_HYPERLINK_COLOR, BLUE);

    store.setDefault(P_APPLY_ON_TAB_SWITCH, false);
    store.setDefault(P_NO_PREFERENCE_TOOL_TIPS, false);

    // indentation

    store.setDefault(P_NO_ADV_AUTO_INDENT, false);
    store.setDefault(P_INDENT_CHAR, false);
    store.setDefault(P_INDENT_CHAR_NB, 2);

    // spelling

    store.setDefault(P_NO_SPELL_CHECKING, false);

    // comments

    store.setDefault(P_COMMENT_LINE, DARK_GREEN);
    store.setDefault(P_COMMENT_LINE_ATR, NONE);

    store.setDefault(P_COMMENT_BLOCK, DARK_GREEN);
    store.setDefault(P_COMMENT_BLOCK_ATR, NONE);

    store.setDefault(P_COMMENT_JAVADOC, BLUE);
    store.setDefault(P_COMMENT_JAVADOC_ATR, NONE);

    store.setDefault(P_COMMENT_BACKGROUND, WHITE);

    // Java    

    store.setDefault(P_JAVA_BLOCK_BRACE, DARK_RED);
    store.setDefault(P_JAVA_BLOCK_BRACE_ATR, BOLD);

    store.setDefault(P_JAVA_BLOCK_BRACE_ALT_BG, false);

    store.setDefault(P_JAVA_KEYWORD, DARK_RED);
    store.setDefault(P_JAVA_KEYWORD_ATR, BOLD);

    store.setDefault(P_JAVA_IDENTIFIER, DARK_RED);
    store.setDefault(P_JAVA_IDENTIFIER_ATR, NONE);

    store.setDefault(P_JAVA_PUNCTUATION, BLACK);
    store.setDefault(P_JAVA_PUNCTUATION_ATR, NONE);

    store.setDefault(P_JAVA_STRING, BLUE);
    store.setDefault(P_JAVA_STRING_ATR, NONE);

    store.setDefault(P_JAVA_NUMERIC, BLUE);
    store.setDefault(P_JAVA_NUMERIC_ATR, NONE);

    store.setDefault(P_JAVA_DEFAULT_TEXT, BLACK);
    store.setDefault(P_JAVA_DEFAULT_TEXT_ATR, NONE);

    store.setDefault(P_JAVA_BACKGROUND, WHITE);

    // JavaCC

    store.setDefault(P_JAVACC_KEYWORD, DARK_GREEN);
    store.setDefault(P_JAVACC_KEYWORD_ATR, BOLD);

    store.setDefault(P_JAVACC_EXPANSION_BRACE, RED);
    store.setDefault(P_JAVACC_EXPANSION_BRACE_ATR, BOLD);

    store.setDefault(P_JAVACC_CHOICE_PUNCT, RED);
    store.setDefault(P_JAVACC_CHOICE_PUNCT_ATR, BOLD);

    store.setDefault(P_JAVACC_STRING, BLUE);
    store.setDefault(P_JAVACC_STRING_ATR, NONE);

    store.setDefault(P_JAVACC_NUMERIC, BLUE);
    store.setDefault(P_JAVACC_NUMERIC_ATR, NONE);

    store.setDefault(P_JAVACC_OTHER_PUNCT, BLACK);
    store.setDefault(P_JAVACC_OTHER_PUNCT_ATR, NONE);

    store.setDefault(P_JAVACC_DEFAULT_TEXT, BLACK);
    store.setDefault(P_JAVACC_DEFAULT_TEXT_ATR, NONE);

    store.setDefault(P_JAVACC_BACKGROUND, WHITE);

    // regular expressions

    store.setDefault(P_REG_EX_BRACE, MAGENTA);
    store.setDefault(P_REG_EX_BRACE_ATR, BOLD);

    store.setDefault(P_REG_EX_BRACKET, MAGENTA);
    store.setDefault(P_REG_EX_BRACKET_ATR, BOLD);

    store.setDefault(P_REG_EX_TOKEN_PUNCT, MAGENTA);
    store.setDefault(P_REG_EX_TOKEN_PUNCT_ATR, BOLD);

    store.setDefault(P_REG_EX_CHOICE_PUNCT, RED);
    store.setDefault(P_REG_EX_CHOICE_PUNCT_ATR, BOLD);

    store.setDefault(P_REG_EX_OTHER_PUNCT, BLACK);
    store.setDefault(P_REG_EX_OTHER_PUNCT_ATR, NONE);

    // states

    store.setDefault(P_LEXICAL_STATE, DARK_RED);
    store.setDefault(P_LEXICAL_STATE_ATR, ITALIC);

    store.setDefault(P_LEXICAL_STATE_PUNCT, DARK_RED);
    store.setDefault(P_LEXICAL_STATE_PUNCT_ATR, ITALIC);

    store.setDefault(P_LEXICAL_STATE_NEXT, DARK_RED);
    store.setDefault(P_LEXICAL_STATE_NEXT_ATR, ITALIC);

    // labels

    store.setDefault(P_TOKEN_LABEL, DARK_YELLOW);
    store.setDefault(P_TOKEN_LABEL_ATR, NONE);

    store.setDefault(P_TOKEN_LABEL_PUNCT, DARK_YELLOW);
    store.setDefault(P_TOKEN_LABEL_PUNCT_ATR, NONE);

    store.setDefault(P_TOKEN_LABEL_DEF, DARK_YELLOW);
    store.setDefault(P_TOKEN_LABEL_DEF_ATR, NONE);

    store.setDefault(P_TOKEN_LABEL_PRIVATE_DEF, DARK_RED);
    store.setDefault(P_TOKEN_LABEL_PRIVATE_DEF_ATR, NONE);

    store.setDefault(P_TOKEN_LABEL_PRIVATE_DEF_PUNCT, DARK_RED);
    store.setDefault(P_TOKEN_LABEL_PRIVATE_DEF_PUNCT_ATR, NONE);

    // jjtree

    store.setDefault(P_JJTREE_NODE_NAME_PUNCT, RED);
    store.setDefault(P_JJTREE_NODE_NAME_PUNCT_ATR, ITALIC);

    store.setDefault(P_JJTREE_NODE_NAME, RED);
    store.setDefault(P_JJTREE_NODE_NAME_ATR, ITALIC);

    store.setDefault(P_JJTREE_NODE_EXPR_PAREN, RED);
    store.setDefault(P_JJTREE_NODE_EXPR_PAREN_ATR, ITALIC);

    // other

    store.setDefault(P_JAVACC_OPTION, DARK_GREEN);
    store.setDefault(P_JAVACC_OPTION_ATR, BOLD);

    store.setDefault(P_JAVACC_OPTION_BRACE, DARK_GREEN);
    store.setDefault(P_JAVACC_OPTION_BRACE_ATR, BOLD);

    store.setDefault(P_JAVACC_PARSER_NAME, DARK_GREEN);
    store.setDefault(P_JAVACC_PARSER_NAME_ATR, BOLD);

    store.setDefault(P_JAVACC_PARSER_NAME_PAREN, DARK_GREEN);
    store.setDefault(P_JAVACC_PARSER_NAME_PAREN_ATR, BOLD);

    store.setDefault(P_MATCHING_CHAR, DARK_GREEN);

    store.setDefault(P_CONSOLE_COMMAND, DARK_RED);
    store.setDefault(P_CONSOLE_COMMAND_ATR, BOLD);
  }

  // Currently unused values may be used in the future

  /** The RGB String for the color WHITE */
  protected static String       WHITE        = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_WHITE)
                                                                               .getRGB());

  /** The RGB String for the color BLACK */
  protected static String       BLACK        = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_BLACK)
                                                                               .getRGB());

  /** The RGB String for the color RED */
  protected static String       RED          = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_RED)
                                                                               .getRGB());

  /** The RGB String for the color DARK RED */
  protected static String       DARK_RED     = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_RED)
                                                                               .getRGB());

  /** The RGB String for the color GREEN */
  protected static String       GREEN        = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_GREEN)
                                                                               .getRGB());

  /** The RGB String for the color DARK GREEN */
  protected static String       DARK_GREEN   = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_GREEN)
                                                                               .getRGB());

  /** The RGB String for the color YELLOW */
  protected static String       YELLOW       = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_YELLOW)
                                                                               .getRGB());

  /** The RGB String for the color DARK YELLOW */
  protected static String       DARK_YELLOW  = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_YELLOW)
                                                                               .getRGB());

  /** The RGB String for the color BLUE */
  protected static String       BLUE         = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_BLUE)
                                                                               .getRGB());

  /** The RGB String for the color DARK BLUE */
  protected static String       DARK_BLUE    = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_BLUE)
                                                                               .getRGB());

  /** The RGB String for the color MAGENTA */
  protected static String       MAGENTA      = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_MAGENTA)
                                                                               .getRGB());

  /** The RGB String for the color DARK MAGENTA */
  protected static String       DARK_MAGENTA = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_MAGENTA)
                                                                               .getRGB());

  /** The RGB String for the color CYAN */
  protected static String       CYAN         = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_CYAN)
                                                                               .getRGB());

  /** The RGB String for the color DARK CYAN */
  protected static String       DARK_CYAN    = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_CYAN)
                                                                               .getRGB());

  /** The RGB String for the color GRAY */
  protected static String       GRAY         = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_GRAY)
                                                                               .getRGB());

  /** The RGB String for the color DARK GRAY */
  protected static String       DARK_GRAY    = StringConverter.asString(Display.getCurrent()
                                                                               .getSystemColor(SWT.COLOR_DARK_GRAY)
                                                                               .getRGB());

  /** The font attribute String constant NONE */
  protected static final String NONE         = String.valueOf(SWT.NONE);

  /** The font attribute String constant BOLD */
  protected static final String BOLD         = String.valueOf(SWT.BOLD);

  /** The font attribute String constant ITALIC */
  protected static final String ITALIC       = String.valueOf(SWT.ITALIC);

  /** The font attribute String constant BOLD_ITALIC */
  protected static final String BOLD_ITALIC  = String.valueOf(SWT.BOLD + SWT.ITALIC);

}
