package sf.eclipse.javacc;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Defines all the properties names and Qualified Names
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public interface IConstants {
  public static final boolean DEBUG = false;
  
  // Prefix for QualifiedNames
  public static final String ID = "sf.eclipse.javacc";
  
  // Qualified names
  public static final String JJTREE_OPTIONS = "JJTREE_OPTIONS";
  public static final String JAVACC_OPTIONS = "JAVACC_OPTIONS";
  public static final String JJDOC_OPTIONS  = "JJDOC_OPTIONS";
  public static final String RUNTIME_JAR = "RUNTIME_JAR";
  public static final String PROJECT_OVERRIDE = "PROJECT_OVERRIDE";
  public static final String GENERATED_FILE = "GENERATED_FILE";
  public static final String SHOW_CONSOLE = "SHOW_CONSOLE";
  public static final String CLEAR_CONSOLE = "CLEAR_CONSOLE";
  public static final String JJ_NATURE = "JJ_NATURE";
  
  // JJBuilder and JJNature ID (see plugin.xml)
  public static final String JJ_NATURE_ID = "sf.eclipse.javacc.javaccnature";
  public static final String JJ_BUILDER_ID = "sf.eclipse.javacc.javaccbuilder";
  public static final String JJ_NATURE_NAME = "JavaCC Nature";
  public static final String JJ_BUILDER_NAME = "JavaCC JJBuilder";
  
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
  
}
