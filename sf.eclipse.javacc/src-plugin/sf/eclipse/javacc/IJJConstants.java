package sf.eclipse.javacc;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Defines all the Properties names and Qualified Names
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public interface IJJConstants {
  public static final boolean DEBUG = false;

  // Prefix for QualifiedNames
  public static final String ID = "sf.eclipse.javacc"; //$NON-NLS-1$

  // Qualified names
  public static final String JJTREE_OPTIONS = "JJTREE_OPTIONS"; //$NON-NLS-1$
  public static final String JAVACC_OPTIONS = "JAVACC_OPTIONS"; //$NON-NLS-1$
  public static final String JJDOC_OPTIONS = "JJDOC_OPTIONS"; //$NON-NLS-1$
  public static final String RUNTIME_JAR = "RUNTIME_JAR"; //$NON-NLS-1$
  public static final String PROJECT_OVERRIDE = "PROJECT_OVERRIDE"; //$NON-NLS-1$
  public static final String GENERATED_FILE = "GENERATED_FILE"; //$NON-NLS-1$
  public static final String SHOW_CONSOLE = "SHOW_CONSOLE"; //$NON-NLS-1$
  public static final String CLEAR_CONSOLE = "CLEAR_CONSOLE"; //$NON-NLS-1$
  public static final String JJ_NATURE = "JJ_NATURE"; //$NON-NLS-1$

  public static final String JTB_OPTIONS = "JTB_OPTIONS"; //$NON-NLS-1$
  public static final String RUNTIME_JTBJAR = "RUNTIME_JTBJAR"; //$NON-NLS-1$

  // JJBuilder and JJNature ID (see plugin.xml)
  public static final String JJ_NATURE_ID = "sf.eclipse.javacc.javaccnature"; //$NON-NLS-1$
  public static final String JJ_BUILDER_ID = "sf.eclipse.javacc.javaccbuilder"; //$NON-NLS-1$
  public static final String JJ_NATURE_NAME = "JavaCC Nature"; //$NON-NLS-1$
  public static final String JJ_BUILDER_NAME = "JavaCC JJBuilder"; //$NON-NLS-1$

  // Qualified Names
  public static final QualifiedName QN_JJTREE_OPTIONS = new QualifiedName(ID, JJTREE_OPTIONS);
  public static final QualifiedName QN_JAVACC_OPTIONS = new QualifiedName(ID, JAVACC_OPTIONS);
  public static final QualifiedName QN_JJDOC_OPTIONS = new QualifiedName(ID, JJDOC_OPTIONS);
  public static final QualifiedName QN_RUNTIME_JAR = new QualifiedName(ID, RUNTIME_JAR);
  public static final QualifiedName QN_CLEAR_CONSOLE = new QualifiedName(ID, CLEAR_CONSOLE);
  public static final QualifiedName QN_PROJECT_OVERRIDE = new QualifiedName(ID, PROJECT_OVERRIDE);
  public static final QualifiedName QN_SHOW_CONSOLE = new QualifiedName(ID, SHOW_CONSOLE);
  public static final QualifiedName QN_GENERATED_FILE = new QualifiedName(ID, GENERATED_FILE);
  public static final QualifiedName QN_JJ_NATURE = new QualifiedName(ID, JJ_NATURE);

  public static final QualifiedName QN_RUNTIME_JTBJAR = new QualifiedName(ID, RUNTIME_JTBJAR);
  public static final QualifiedName QN_JTB_OPTIONS = new QualifiedName(ID, JTB_OPTIONS);

  // Constant definitions for plug-in Preferences
  public static final String P_PATH = "pathPreference"; //$NON-NLS-1$
  public static final String P_BOOLEAN = "booleanPreference"; //$NON-NLS-1$
  public static final String P_CHOICE = "choicePreference"; //$NON-NLS-1$
  public static final String P_STRING = "stringPreference"; //$NON-NLS-1$

  // JJConsole
  public static final String CONSOLE_ID = "sf.eclipse.javacc.Console"; //$NON-NLS-1$
  
  // JJEditor
  public static final String EDITOR_ID = "sf.eclipse.javacc.editors.JJEditor"; //$NON-NLS-1$
}
