package sf.eclipse.javacc.options;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.Activator;

/**
 * Class used to initialize default preference values.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJPreferences extends AbstractPreferenceInitializer {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa : added / renamed colors and indentation preferences

  /** JavaCC non automatic indentation preference */
  public static final String P_NO_ADV_AUTO_INDENT = "JavaCCNoAutoIndentPref"; //$NON-NLS-1$
  /** JavaCC indentation character preference */
  public static final String P_INDENT_CHAR        = "JavaCCIndentCharPref";   //$NON-NLS-1$
  /** JavaCC number of indentation character preference */
  public static final String P_INDENT_CHAR_NB     = "JavaCCIndentCharNbPref"; //$NON-NLS-1$
  /** JavaCC keyword color preference */
  public static final String P_JJKEYWORD          = "JavaCCKeyWordColorPref"; //$NON-NLS-1$
  /** Java keyword color preference */
  public static final String P_JAVAKEYWORD        = "JavaKeyWordColorPref";   //$NON-NLS-1$
  /** Background color preference */
  public static final String P_BACKGROUND         = "BackgroundColorPref";    //$NON-NLS-1$
  /** String color preference */
  public static final String P_STRING             = "StringColorPref";        //$NON-NLS-1$
  /** Comment color preference */
  public static final String P_COMMENT            = "CommentColorPref";       //$NON-NLS-1$
  /** Javadoc comment color preference */
  public static final String P_JDOC_COMMENT       = "JavaDocCommentColorPref"; //$NON-NLS-1$
  /** Normal label identifier color preference */
  public static final String P_NORMALLABEL        = "TokenColorPref";         //$NON-NLS-1$
  /** Private label identifier color preference */
  public static final String P_PRIVATELABEL       = "PrivateTokenColorPref";  //$NON-NLS-1$
  /** Lexical state list or lexical state identifier color preference */
  public static final String P_LEXICALSTATE       = "LexicalStateColorPref";  //$NON-NLS-1$
  /** Regular_expression punctuation color preference */
  public static final String P_REGEXPUNCT         = "RegExPunctColorPref";    //$NON-NLS-1$
  /** Choices enclosing punctuation color preference */
  public static final String P_CHOICESPUNCT       = "ChoicesPunctColorPref";  //$NON-NLS-1$
  /** Default color preference */
  public static final String P_DEFAULT            = "DefaultTextColorPref";   //$NON-NLS-1$
  /** Matching opening/closing character color preference */
  public static final String P_MATCHING_CHAR      = "MatchingCharColorPref";  //$NON-NLS-1$
  /** Console command color preference */
  public static final String P_CONSOLE_COMMAND    = "ConsoleCommandColorPref"; //$NON-NLS-1$

  /**
   * @see AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    final Display display = Display.getCurrent();
    final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    /*
     * Indentation
     */
    store.setDefault(JJPreferences.P_NO_ADV_AUTO_INDENT, false);
    store.setDefault(JJPreferences.P_INDENT_CHAR, false);
    store.setDefault(JJPreferences.P_INDENT_CHAR_NB, 2);
    /*
     * Colors
     */
    Color color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_JJKEYWORD, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_JAVAKEYWORD, color.getRGB());
    // color = display.getSystemColor(SWT.COLOR_WHITE);
    // PreferenceConverter.setDefault(store, P_BACKGROUND, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_BLUE);
    PreferenceConverter.setDefault(store, P_STRING, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_COMMENT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_BLUE);
    PreferenceConverter.setDefault(store, P_JDOC_COMMENT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    PreferenceConverter.setDefault(store, P_NORMALLABEL, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_PRIVATELABEL, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_LEXICALSTATE, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_MAGENTA);
    PreferenceConverter.setDefault(store, P_REGEXPUNCT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_RED);
    PreferenceConverter.setDefault(store, P_CHOICESPUNCT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_BLACK);
    PreferenceConverter.setDefault(store, P_DEFAULT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_MATCHING_CHAR, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_CONSOLE_COMMAND, color.getRGB());
  }
}
