package sf.eclipse.javacc;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Defines all the Properties names and Qualified Names.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public interface IJJConstants {

  // MMa 04/2009 : formatting and javadoc revision ; added and removed some entries 
  // MMa 12/2009 : changed JTB version
  // MMa 02/2010 : change JTB revision

  /** debug mode flag (not used) */
  public static final boolean       DEBUG             = false;

  /** default JavaCC jar name */
  public static final String        JAVACC_JAR_NAME   = "javacc.jar";                         //$NON-NLS-1$
  /** default JTB jar name */
  public static final String        JTB_JAR_NAME      = "jtb-1.4.2.jar";                      //$NON-NLS-1$

  /** prefix for qualified names */
  public static final String        ID                = "sf.eclipse.javacc";                  //$NON-NLS-1$

  /** JavaCC options qualified name suffix */
  public static final String        JAVACC_OPTIONS    = "JAVACC_OPTIONS";                     //$NON-NLS-1$
  /** JJTree options qualified name suffix */
  public static final String        JJTREE_OPTIONS    = "JJTREE_OPTIONS";                     //$NON-NLS-1$
  /** JJDoc options qualified name suffix */
  public static final String        JJDOC_OPTIONS     = "JJDOC_OPTIONS";                      //$NON-NLS-1$
  /** JTB options qualified name suffix */
  public static final String        JTB_OPTIONS       = "JTB_OPTIONS";                        //$NON-NLS-1$
  /** suppress warnings qualified name suffix */
  public static final String        SUPPRESS_WARNINGS = "SUPPRESS_WARNINGS";                  //$NON-NLS-1$  

  //  /** check spelling in .jj, .jjt & .jtb files */
  //  public static final String        CHECK_SPELLING    = "CHECK_SPELLING";                     //$NON-NLS-1$  

  /** generated file qualified name suffix */
  public static final String        GENERATED_FILE    = "GENERATED_FILE";                     //$NON-NLS-1$
  /** show console qualified name suffix */
  public static final String        SHOW_CONSOLE      = "SHOW_CONSOLE";                       //$NON-NLS-1$
  /** clear console qualified name suffix */
  public static final String        CLEAR_CONSOLE     = "CLEAR_CONSOLE";                      //$NON-NLS-1$
  /** jj nature qualified name suffix */
  public static final String        JJ_NATURE         = "JJ_NATURE";                          //$NON-NLS-1$
  /** runtime JavaCC jar qualified name suffix */
  public static final String        RUNTIME_JJJAR     = "RUNTIME_JJJAR";                      //$NON-NLS-1$
  /** runtime jtb jar qualified name suffix */
  public static final String        RUNTIME_JTBJAR    = "RUNTIME_JTBJAR";                     //$NON-NLS-1$

  /** generated file qualified name */
  public static final QualifiedName QN_GENERATED_FILE = new QualifiedName(ID, GENERATED_FILE);

  // JJBuilder and JavaCCNature ID (see plugin.xml)
  /** JJNature qualified name id */
  public static final String        JJ_NATURE_ID      = "sf.eclipse.javacc.javaccnature";     //$NON-NLS-1$
  /** JJBuilder qualified name id */
  public static final String        JJ_BUILDER_ID     = "sf.eclipse.javacc.javaccbuilder";    //$NON-NLS-1$
  /** JJNature qualified name label */
  public static final String        JJ_NATURE_NAME    = "JavaCC Nature";                      //$NON-NLS-1$
  /** JJBbuilder qualified name label */
  public static final String        JJ_BUILDER_NAME   = "JavaCC JJBuilder";                   //$NON-NLS-1$

  // JJConsole and JJCallHierarchy
  /** console qualified name id */
  public static final String        CONSOLE_ID        = "sf.eclipse.javacc.Console";          //$NON-NLS-1$
  /** call hierarchy qualified name id */
  public static final String        CALLHIERARCHY_ID  = "sf.eclipse.javacc.CallHierarchy";    //$NON-NLS-1$

  // JJEditor & JTBEditor
  /** JJEditor qualified name id */
  public static final String        JJEDITOR_ID       = "sf.eclipse.javacc.editors.JJEditor"; //$NON-NLS-1$
  /** JTBEditor qualified name id */
  public static final String        JTBEDITOR_ID      = "sf.eclipse.javacc.editors.JTBEditor"; //$NON-NLS-1$
}
