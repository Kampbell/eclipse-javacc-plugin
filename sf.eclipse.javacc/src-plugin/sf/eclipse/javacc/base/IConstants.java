package sf.eclipse.javacc.base;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.IDocument;

/**
 * Defines near all the common static constants (except those for the preferences) (to be used as static
 * import).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 * @author Bill Fenlason 2012
 */
public interface IConstants {

  // MMa 04/2009 : formatting and javadoc revision ; added and removed some entries 
  // MMa 12/2009 : changed JTB version
  // MMa 02/2010 : changed JTB revision
  // MMa 03/2010 : added IDs; changed JTB version
  // MMa 08/2011 : added mark generated files as derived option RFE 3314103
  // MMa 08/2011 : changed JTB revision
  // BF  06/2012 : added missing //$NON-NLS-1$ tags
  // MMa 10/2012 : added JVM options option and keep deleted files in history option ; moved arrays ; renamed
  // MMa 11/2014 : changed JavaCC jar name & JTB revision ; fixed default JDK_VERSION
  // MMa 12/2014 : added jars directory

  //  /** Debug mode flag (not used) */
  //  static final boolean         DEBUG                      = false;

  /*
   *  JARs
   */
  /** Default JavaCC jar name */
  static final String          JAVACC_JAR_NAME            = "javacc-5.0.jar";                                 //$NON-NLS-1$
  /** Default JTB jar name */
  static final String          JTB_JAR_NAME               = "jtb-1.4.8.jar";                                  //$NON-NLS-1$
  /** Jars directory name */
  static final String          JARS_DIR                   = "/jars/";                                         //$NON-NLS-1$

  /*
   *  Project options
   */
  /** JavaCC options qualified name suffix */
  static final String          JAVACC_OPTIONS             = "JAVACC_OPTIONS";                                 //$NON-NLS-1$
  /** JJTree options qualified name suffix */
  static final String          JJTREE_OPTIONS             = "JJTREE_OPTIONS";                                 //$NON-NLS-1$
  /** JJDoc options qualified name suffix */
  static final String          JJDOC_OPTIONS              = "JJDOC_OPTIONS";                                  //$NON-NLS-1$
  /** JTB options qualified name suffix */
  static final String          JTB_OPTIONS                = "JTB_OPTIONS";                                    //$NON-NLS-1$
  /** JavaCC option default JDK version */
  static final String          DEF_JDK_VERSION            = "1.5";                                            //$NON-NLS-1$
  /** JavaCC // JJTree // JJDoc / JTB empty default option */
  static final String          DEF_EMPTY_OPTION           = "";                                               //$NON-NLS-1$
  /** Suppress warnings run-time option qualified name suffix */
  static final String          SUPPRESS_WARNINGS          = "SUPPRESS_WARNINGS";                              //$NON-NLS-1$  
  /** Default value for {@link #SUPPRESS_WARNINGS} */
  static final String          DEF_SUPPRESS_WARNINGS      = "false";                                          //$NON-NLS-1$
  /** Mark generated files as derived run-time option qualified name suffix */
  static final String          MARK_GEN_FILES_DERIVED     = "MARK_GEN_FILES_AS_DERIVED";                      //$NON-NLS-1$  
  /** Default value for {@link #MARK_GEN_FILES_DERIVED} */
  static final String          DEF_MARK_GEN_FILES_DERIVED = "true";                                           //$NON-NLS-1$
  /** Keep deleted files in history run-time option qualified name suffix */
  static final String          KEEP_DEL_FILES_IN_HIST     = "KEEP_DEL_FILES_IN_HISTORY";                      //$NON-NLS-1$  
  /** Default value for {@link #KEEP_DEL_FILES_IN_HIST} */
  static final String          DEF_KEEP_DEL_FILES_IN_HIST = "false";                                          //$NON-NLS-1$
  /** Show console run-time option qualified name suffix */
  static final String          SHOW_CONSOLE               = "SHOW_CONSOLE";                                   //$NON-NLS-1$
  /** Clear console run-time option qualified name suffix */
  static final String          CLEAR_CONSOLE              = "CLEAR_CONSOLE";                                  //$NON-NLS-1$
  /** Default value for {@link #CLEAR_CONSOLE} */
  static final String          DEF_CLEAR_CONSOLE          = "true";                                           //$NON-NLS-1$
  /** Nature run-time option qualified name suffix */
  static final String          NATURE                     = "JJ_NATURE";                                      //$NON-NLS-1$
  /** Default value for {@link #NATURE} */
  static final String          DEF_NATURE                 = "true";                                           //$NON-NLS-1$
  /** JavaCC jar run-time option qualified name suffix */
  static final String          RUNTIME_JJJAR              = "RUNTIME_JJJAR";                                  //$NON-NLS-1$
  /** JTB jar run-time option qualified name suffix */
  static final String          RUNTIME_JTBJAR             = "RUNTIME_JTBJAR";                                 //$NON-NLS-1$
  /** JVM options run-time option qualified name suffix */
  static final String          RUNTIME_JVMOPTIONS         = "RUNTIME_JVMOPTIONS";                             //$NON-NLS-1$

  /*
   *   plugin.xml
   */

  /** Plugin qualifier */
  static final String          PLUGIN_QN                  = "sf.eclipse.javacc";                              //$NON-NLS-1$
  /** Generated file qualified name */
  static final QualifiedName   GEN_FILE_QN                = new QualifiedName(PLUGIN_QN, "GENERATED_FILE");   //$NON-NLS-1$
  /** Plugin name */
  public static final String   PLUGIN_NAME                = "JavaCC";                                         //$NON-NLS-1$

  // Builder and Nature ID
  /** Nature qualified name id (note that in plugin.xml it is "javaccnature") */
  static final String          NATURE_ID                  = "sf.eclipse.javacc.javaccnature";                 //$NON-NLS-1$
  /** Nature qualified name label */
  static final String          NATURE_NAME                = "JavaCC Nature";                                  //$NON-NLS-1$
  /** Builder qualified name id (note that in plugin.xml it is "javaccbuilder") */
  static final String          BUILDER_ID                 = "sf.eclipse.javacc.javaccbuilder";                //$NON-NLS-1$
  /** Builder qualified name label */
  static final String          BUILDER_NAME               = "JavaCC Builder";                                 //$NON-NLS-1$

  //  Decorator ID 
  /** Decorator qualified name id */
  static final String          DECORATOR_ID               = "sf.eclipse.javacc.jjdecorator";                  //$NON-NLS-1$

  //  Console and CallHierarchy and Outline
  /** Console qualified name id */
  static final String          CONSOLE_ID                 = "sf.eclipse.javacc.Console";                      //$NON-NLS-1$
  /** CallHierarchy qualified name id */
  static final String          CALL_HIERARCHY_ID          = "sf.eclipse.javacc.CallHierarchy";                //$NON-NLS-1$
  /** CallHierarchyView synchronize from editor flag */
  static final String          CALL_HIERARCHY_SYNC        = "CALL_HIERARCHY_SYNC_FROM_EDITOR";                //$NON-NLS-1$
  /** JJOutilinePage synchronize from editor flag */
  static final String          OUTLINE_SYNC               = "OUTLINE_SYNC_FROM_EDITOR";                       //$NON-NLS-1$

  //  JJEditor & JJEditorScope
  /** JJEditor qualified name id */
  static final String          JJEDITOR_ID                = "sf.eclipse.javacc.editors.JJEditor";             //$NON-NLS-1$
  /** JJEditorScope qualified name id */
  static final String          JJEDITOR_SCOPE_ID          = "sf.eclipse.javacc.JJEditorScope";                //$NON-NLS-1$

  //  Folding commands
  /** Collapse command id */
  static final String          COLLAPSE_ID                = "sf.eclipse.javacc.foldingcollapse";              //$NON-NLS-1$
  /** Collapse_all command id */
  static final String          COLLAPSE_ALL_ID            = "sf.eclipse.javacc.foldingcollapseall";           //$NON-NLS-1$
  /** Expand command id */
  static final String          EXPAND_ID                  = "sf.eclipse.javacc.foldingexpand";                //$NON-NLS-1$
  /** Expand_all command id */
  static final String          EXPAND_ALL_ID              = "sf.eclipse.javacc.foldingexpandall";             //$NON-NLS-1$

  //  AnnotationTypes
  /** Error marker qualified name id */
  static final String          MARKER_ERROR               = "sf.eclipse.javacc.marker.error";                 //$NON-NLS-1$
  /** Warning marker qualified name id */
  static final String          MARKER_WARNING             = "sf.eclipse.javacc.marker.warning";               //$NON-NLS-1$
  /** Info marker qualified name id */
  static final String          MARKER_INFO                = "sf.eclipse.javacc.marker.info";                  //$NON-NLS-1$

  //  Marker
  /** Marker id ; note that in plugin.xml it is "jjmarker" */
  static final String          JJ_MARKER                  = "sf.eclipse.javacc.jjmarker";                     //$NON-NLS-1$

  //  JJDocumentProvider / partitioning
  /** The partitioning ID */
  public static final String   PARTITIONING_ID            = "sf.eclipse.javacc.editors.JJEditor.partitioning"; //$NON-NLS-1$ 
  /** The identifier for the code partition content type */
  public static final String   CODE_CONTENT_TYPE          = "JJ_CODE";                                        //$NON-NLS-1$ 
  /** The identifier for the single line comments partition content type */
  public static final String   LINE_CMT_CONTENT_TYPE      = "JJ_LINE_COMMENT";                                //$NON-NLS-1$ 
  /** The identifier for the multiline comments partition content type */
  public static final String   BLOCK_CMT_CONTENT_TYPE     = "JJ_BLOCK_COMMENT";                               //$NON-NLS-1$ 
  /** The identifier for the javadoc partition content type */
  public static final String   JAVADOC_CONTENT_TYPE       = "JJ_JAVADOC";                                     //$NON-NLS-1$ 
  /** The array of partition content types */
  public static final String[] CONTENT_TYPES              = {
      CODE_CONTENT_TYPE, //
      LINE_CMT_CONTENT_TYPE, //
      BLOCK_CMT_CONTENT_TYPE, //
      JAVADOC_CONTENT_TYPE, //
      IDocument.DEFAULT_CONTENT_TYPE                     };

  /*
   * Platform
   */
  /** The system line separator */
  public static final String   LS                         = System.getProperty("line.separator");             //$NON-NLS-1$

  /** The system line separator length */
  public static final int      LSLEN                      = LS.length();

  /*
   * Files and folders
   */
  /** Icons folder name */
  public static final String   ICONS_FOLDER               = "icons/";                                         //$NON-NLS-1$

  /** Templates folder name */
  public static final String   TEMPLATES_FOLDER           = "/templates/";                                    //$NON-NLS-1$

  /** Template file name prefix */
  public static final String   TEMPLATE_PREFIX            = "New_file";                                       //$NON-NLS-1$

  /** Non static template file name suffix */
  public static final String   TEMPLATE_NON_STATIC        = "_non_static";                                    //$NON-NLS-1$

  /** Static template file name suffix */
  public static final String   TEMPLATE_STATIC            = "_static";                                        //$NON-NLS-1$

}
