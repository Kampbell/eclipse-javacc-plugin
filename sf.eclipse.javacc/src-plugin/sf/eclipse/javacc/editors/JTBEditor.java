package sf.eclipse.javacc.editors;

/**
 * Editor designed for JTB files (almost the same as JJEditor except for the context id). Referenced by
 * plugin.xml<br>
 * <extension point="org.eclipse.ui.editors">
 * 
 * @author Marc Mazas 2009
 */
public class JTBEditor extends JJEditor {

  // MMa 11/09 : created from JJEditor to allow proper context menu

  /**
   * Standard constructor.
   */
  public JTBEditor() {
    super("sf.eclipse.javacc.editors.JTBEditor"); //$NON-NLS-1$
  }

}
