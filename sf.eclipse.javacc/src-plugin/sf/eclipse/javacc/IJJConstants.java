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
  // MMa 02/2010 : changed JTB revision
  // MMa 03/2010 : added IDs; changed JTB version

  /** Debug mode flag (not used) */
  public static final boolean       DEBUG             = false;

  /** Default JavaCC jar name */
  public static final String        JAVACC_JAR_NAME   = "javacc.jar";                           //$NON-NLS-1$
  /** Default JTB jar name */
  public static final String        JTB_JAR_NAME      = "jtb-1.4.3.jar";                        //$NON-NLS-1$

  /** Prefix for qualified names */
  public static final String        ID                = "sf.eclipse.javacc";                    //$NON-NLS-1$

  /** JavaCC options qualified name suffix */
  public static final String        JAVACC_OPTIONS    = "JAVACC_OPTIONS";                       //$NON-NLS-1$
  /** JJTree options qualified name suffix */
  public static final String        JJTREE_OPTIONS    = "JJTREE_OPTIONS";                       //$NON-NLS-1$
  /** JJDoc options qualified name suffix */
  public static final String        JJDOC_OPTIONS     = "JJDOC_OPTIONS";                        //$NON-NLS-1$
  /** JTB options qualified name suffix */
  public static final String        JTB_OPTIONS       = "JTB_OPTIONS";                          //$NON-NLS-1$

  /** Suppress warnings run-time option qualified name suffix */
  public static final String        SUPPRESS_WARNINGS = "SUPPRESS_WARNINGS";                    //$NON-NLS-1$  
  /** Show console run-time option qualified name suffix */
  public static final String        SHOW_CONSOLE      = "SHOW_CONSOLE";                         //$NON-NLS-1$
  /** Clear console run-time option qualified name suffix */
  public static final String        CLEAR_CONSOLE     = "CLEAR_CONSOLE";                        //$NON-NLS-1$
  /** JJ nature run-time option qualified name suffix */
  public static final String        JJ_NATURE         = "JJ_NATURE";                            //$NON-NLS-1$
  /** Runtime JavaCC jar run-time option qualified name suffix */
  public static final String        RUNTIME_JJJAR     = "RUNTIME_JJJAR";                        //$NON-NLS-1$
  /** Runtime jtb jar run-time option qualified name suffix */
  public static final String        RUNTIME_JTBJAR    = "RUNTIME_JTBJAR";                       //$NON-NLS-1$

  /** Generated file qualified name */
  public static final QualifiedName QN_GENERATED_FILE = new QualifiedName(ID, "GENERATED_FILE"); //$NON-NLS-1$

  // JJBuilder and JavaCCNature ID (see plugin.xml)
  /** JJNature qualified name id */
  public static final String        JJ_NATURE_ID      = "sf.eclipse.javacc.javaccnature";       //$NON-NLS-1$
  /** JJNature qualified name label */
  public static final String        JJ_NATURE_NAME    = "JavaCC Nature";                        //$NON-NLS-1$
  /** JJBuilder qualified name id */
  public static final String        JJ_BUILDER_ID     = "sf.eclipse.javacc.javaccbuilder";      //$NON-NLS-1$
  /** JJBbuilder qualified name label */
  public static final String        JJ_BUILDER_NAME   = "JavaCC JJBuilder";                     //$NON-NLS-1$

  // JJDecorator ID (see plugin.xml)
  /** JJDecorator qualified name id */
  public static final String        JJ_DECORATOR_ID   = "sf.eclipse.javacc.jjdecorator";        //$NON-NLS-1$

  // JJConsole and JJCallHierarchy (see plugin.xml)
  /** JJConsole qualified name id */
  public static final String        CONSOLE_ID        = "sf.eclipse.javacc.Console";            //$NON-NLS-1$
  /** JJCallHierarchy qualified name id */
  public static final String        CALLHIERARCHY_ID  = "sf.eclipse.javacc.CallHierarchy";      //$NON-NLS-1$

  // JJEditor & JTBEditor & JJEditorScope (see plugin.xml)
  /** JJEditor qualified name id */
  public static final String        JJEDITOR_ID       = "sf.eclipse.javacc.editors.JJEditor";   //$NON-NLS-1$
  /** JTBEditor qualified name id */
  public static final String        JTBEDITOR_ID      = "sf.eclipse.javacc.editors.JTBEditor";  //$NON-NLS-1$
  /** JJEditorScope qualified name id */
  public static final String        JJEDITOR_SCOPE_ID = "sf.eclipse.javacc.JJEditorScope";      //$NON-NLS-1$
}
