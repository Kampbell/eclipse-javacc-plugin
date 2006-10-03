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
 */
public class JJPreferences extends AbstractPreferenceInitializer {
  public static final String P_JJKEYWORD = "JavaCCKeyWordColorPref"; 
  public static final String P_JAVAKEYWORD = "JavaKeyWordColorPref"; 
  public static final String P_BACKGROUND = "BackgroundColorPref"; 
  public static final String P_STRING = "StringColorPref"; 
  public static final String P_COMMENT = "CommentColorPref"; 
  public static final String P_JDOC_COMMENT = "JavaDocCommentColorPref"; 
  public static final String P_TOKEN = "TokenColorPref"; 
  public static final String P_PTOKEN = "PrivateTokenColorPref"; 
  public static final String P_DEFAULT = "DefaultTextColorPref"; 
  public static final String P_MATCHING_CHAR = "MatchingCharColorPref"; 
  public static final String P_CONSOLE_COMMAND = "ConsoleCommandColorPref"; 
  
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
    Display display = Display.getCurrent();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    
    Color color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_JJKEYWORD, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_JAVAKEYWORD, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_WHITE);
    PreferenceConverter.setDefault(store, P_BACKGROUND,  color.getRGB());
    color = display.getSystemColor(SWT.COLOR_BLUE);
    PreferenceConverter.setDefault(store, P_STRING, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_COMMENT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_BLUE);
    PreferenceConverter.setDefault(store, P_JDOC_COMMENT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    PreferenceConverter.setDefault(store, P_TOKEN, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    PreferenceConverter.setDefault(store, P_PTOKEN, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_BLACK);
    PreferenceConverter.setDefault(store, P_DEFAULT, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    PreferenceConverter.setDefault(store, P_MATCHING_CHAR, color.getRGB());
    color = display.getSystemColor(SWT.COLOR_DARK_RED);
    PreferenceConverter.setDefault(store, P_CONSOLE_COMMAND, color.getRGB());
	}

}