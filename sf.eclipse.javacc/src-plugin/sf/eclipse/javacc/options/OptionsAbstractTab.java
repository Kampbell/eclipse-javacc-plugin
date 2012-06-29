package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.base.Option;
import sf.eclipse.javacc.base.OptionSet;
import sf.eclipse.javacc.head.Activator;

/**
 * The basic Tab for JavaCC, JJTree, JJDoc and JTB project options.<br>
 * This class is extended by :
 * 
 * @see JavaCCOptions
 * @see JJTreeOptions
 * @see JJDocOptions
 * @see JTBOptions
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public abstract class OptionsAbstractTab extends Composite implements IPropertyChangeListener, IJJConstants {

  // MMa 04/2009 : added descriptions
  // MMa 11/2009 : javadoc and formatting revision ; changed line option section
  // MMa 02/2010 : formatting and javadoc revision ; fixed not stored Option.VOID properties issue
  // ... ....... : fixed output file not showing issue ; fixed display true cases for void options
  // MMa 03/2010 : enhanced layout (groups / tool tips) ; renamed preference keys
  // MMa 08/2011 : fixed empty default value issue

  /** The optionSet used as a model */
  protected OptionSet              jOptionSet;
  // Controls
  /** The command line options control */
  protected StringFieldEditor      jCmdLnOptField;
  /** The Boolean options control */
  protected BooleanFieldEditor     jCheckboxField[];
  /** The Integer options control */
  protected IntegerFieldEditor     jIntegerField[];
  /** The String options controls */
  protected StringFieldEditor[]    jStringField;
  /** The Directory options controls */
  protected DirectoryFieldEditor[] jPathField;
  /** The File options controls */
  protected FileFieldEditor[]      jFileField;
  /** The number of columns to use for boolean options */
  protected int                    jNbColBooleans     = 1;
  /** The options preference property, defined in subclasses */
  protected String                 jPreferenceName    = null;
  /** The flag to prevent loops from user input and change listeners */
  protected boolean                jIsUpdating;
  /** The IResource we are working on */
  protected IResource              jResource;
  /** The "default" label */
  String                           jDefaultLabel      = Activator.getString("OptAbsTab.default");      //$NON-NLS-1$
  /** The "empty default" label */
  String                           jEmptyDefaultLabel = Activator.getString("OptAbsTab.empty_default"); //$NON-NLS-1$

  /**
   * Standard constructor.
   * 
   * @param aParent - the parent
   * @param aRes - the resource
   */
  public OptionsAbstractTab(final Composite aParent, final IResource aRes) {
    super(aParent, SWT.NONE);
    jResource = aRes;
  }

  /**
   * Fills in the control.
   */
  public void createContents() {
    jIsUpdating = true;
    // get the global line options string from the resource
    String options = null;
    if (jResource != null) {
      final IProject proj = jResource.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      options = prefs.get(jPreferenceName, ""); //$NON-NLS-1$
      jOptionSet.configuresFrom(options);
    }
    // add layout
    final GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // add group
    final Group resGrp = new Group(this, SWT.NONE);
    resGrp.setText(Activator.getString("OptAbsTab.Resulting_group")); //$NON-NLS-1$
    resGrp.setLayout(layout);
    resGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add command line options
    addCmdLnOptSection(options, resGrp);

    // add group
    final Group optGrp = new Group(this, SWT.NONE);
    optGrp.setText(Activator.getString("OptAbsTab.Options_group")); //$NON-NLS-1$
    optGrp.setLayout(layout);
    optGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    if (jOptionSet.getOptionsSize(Option.INT) != 0) {
      addIntegerOptSection(optGrp);
    }
    if (jOptionSet.getOptionsSize(Option.BOOLEAN) != 0 || jOptionSet.getOptionsSize(Option.VOID) != 0) {
      addBooleanOptSection(optGrp);
    }
    if (jOptionSet.getOptionsSize(Option.STRING) != 0) {
      addStringOptSection(optGrp);
    }
    if (jOptionSet.getOptionsSize(Option.PATH) != 0) {
      addPathOptSection(optGrp);
    }
    if (jOptionSet.getOptionsSize(Option.FILE) != 0) {
      //      // not shown when IResource is a project (relevant ... except for CSS for JJDoc)
      //      if (fResource.getType() != IResource.PROJECT) {
      addFileOptSection(optGrp);
      //      }
    }
    jIsUpdating = false;
  }

  /**
   * Shows the resulting command line arguments field.
   * 
   * @param aStr - the command line arguments
   * @param aGrp - the group inside which to add the field
   */
  protected void addCmdLnOptSection(final String aStr, final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    jCmdLnOptField = new StringFieldEditor(
                                           jPreferenceName, // name
                                           "(" + Activator.getString("OptAbsTab.Resulting") + ") " + jPreferenceName + " :", // label //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                           StringFieldEditor.UNLIMITED, // width
                                           StringFieldEditor.VALIDATE_ON_FOCUS_LOST, // strategy
                                           composite);
    jCmdLnOptField.setStringValue(aStr);
    jCmdLnOptField.setPropertyChangeListener(this);
  }

  /**
   * Shows integer options with edit fields.
   * 
   * @param aGrp - the group inside which to add the option
   */
  protected void addIntegerOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    final GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(parent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(250);
    // Eclipse 3.4
    gd.widthHint = 250;
    composite.setLayoutData(gd);
    int k = jOptionSet.getOptionsSize(Option.INT);
    jIntegerField = new IntegerFieldEditor[k];
    k = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (jOptionSet.getType(i) != Option.INT) {
        continue;
      }
      final String label = jOptionSet.getNameAndDescription(i)
                           + " (" + jDefaultLabel + jOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
      jIntegerField[k] = new IntegerFieldEditor(jOptionSet.getName(i), label, composite);
      jIntegerField[k].setEmptyStringAllowed(true);
      jIntegerField[k].setStringValue(jOptionSet.getValue(i));
      jIntegerField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows boolean options with checkboxes.
   * 
   * @param aGrp - the group inside which to add the option
   */
  protected void addBooleanOptSection(final Composite aGrp) {
    // aligns in 2 columns
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout(jNbColBooleans, false));
    composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    int k = jOptionSet.getOptionsSize(Option.BOOLEAN) + jOptionSet.getOptionsSize(Option.VOID);
    jCheckboxField = new BooleanFieldEditor[k];
    k = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (jOptionSet.getType(i) == Option.BOOLEAN || jOptionSet.getType(i) == Option.VOID) {
        final String label = jOptionSet.getNameAndDescription(i)
                             + " (" + jDefaultLabel + jOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
        // need to create a new Composite to have 2 columns !
        jCheckboxField[k] = new BooleanFieldEditor(jOptionSet.getName(i), label, new Composite(composite,
                                                                                               SWT.NONE));
        jCheckboxField[k].setBooleanValue("true".equals(jOptionSet.getValue(i)) ? true : false); //$NON-NLS-1$
        jCheckboxField[k].setPropertyChangeListener(this);
        k++;
      }
    }
  }

  /**
   * Shows string options with edit fields.
   * 
   * @param aGrp - the group inside which to add the option
   */
  protected void addStringOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    final GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(parent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(400);
    // Eclipse 3.4
    gd.widthHint = 400;
    composite.setLayoutData(gd);
    int k = jOptionSet.getOptionsSize(Option.STRING);
    jStringField = new StringFieldEditor[k];
    k = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (jOptionSet.getType(i) != Option.STRING) {
        continue;
      }
      final String defVal = jOptionSet.getDefaultValue(i);
      String defLbl = jEmptyDefaultLabel;
      if (defVal != null && defVal.length() != 0) {
        defLbl = jDefaultLabel + defVal;
      }
      final String label = jOptionSet.getNameAndDescription(i).concat(" (").concat(defLbl).concat(")"); //$NON-NLS-1$ //$NON-NLS-2$ 
      jStringField[k] = new StringFieldEditor(jOptionSet.getName(i), label, composite);
      jStringField[k].setEmptyStringAllowed(true);
      jStringField[k].setStringValue(jOptionSet.getValueInQuotes(i));
      jStringField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows path options with edit fields.
   * 
   * @param aGrp - the group inside which to add the option
   */
  protected void addPathOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("OptAbsTab.Select_directory")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("OptAbsTab.Path_can_be_pathSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    int k = jOptionSet.getOptionsSize(Option.PATH);
    jPathField = new DirectoryFieldEditor[k];
    k = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (jOptionSet.getType(i) != Option.PATH) {
        continue;
      }
      jPathField[k] = new DirectoryFieldEditor(jOptionSet.getName(i), jOptionSet.getNameAndDescription(i),
                                               Activator.getString("OptAbsTab.Choose_a_directory"), //$NON-NLS-1$
                                               jResource.getProject().getLocation().toOSString(), composite);
      jPathField[k].setStringValue(jOptionSet.getValueInQuotes(i));
      jPathField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows file options with edit fields.
   * 
   * @param aGrp - the group inside which to add the option
   */
  protected void addFileOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("OptAbsTab.Select_file")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("OptAbsTab.Path_can_be_FileSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    int k = jOptionSet.getOptionsSize(Option.FILE);
    jFileField = new FileFieldEditor[k];
    k = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (jOptionSet.getType(i) != Option.FILE) {
        continue;
      }
      jFileField[k] = new FileFieldEditor(jOptionSet.getName(i), jOptionSet.getNameAndDescription(i),
                                          composite);
      jFileField[k].setStringValue(jOptionSet.getValueInQuotes(i));
      jFileField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Resets all fields.
   * <p>
   * {@inheritDoc}
   */
  public void performDefaults() {
    jIsUpdating = true;
    jOptionSet.resetToDefaultValues();
    updateFieldsValues();
    jCmdLnOptField.setStringValue(jOptionSet.buildCmdLine());
    jIsUpdating = false;
  }

  /**
   * Updates all fields according to optionSet.
   */
  protected void updateFieldsValues() {
    int nBoolean = 0;
    int nInteger = 0;
    int nString = 0;
    int nPath = 0;
    int nFile = 0;
    final int nb = jOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      String txt = jOptionSet.getValue(i);
      final int type = jOptionSet.getType(i);
      if (type == Option.BOOLEAN || type == Option.VOID) {
        final boolean state = "true".equals(txt) ? true : false; //$NON-NLS-1$
        jCheckboxField[nBoolean].setBooleanValue(state);
        nBoolean++;
      }
      else if (type == Option.INT) {
        jIntegerField[nInteger].setStringValue(txt);
        nInteger++;
      }
      else if (type == Option.STRING) {
        jStringField[nString].setStringValue(txt);
        nString++;
      }
      else if (type == Option.PATH) {
        // strip enclosing quotes
        final int len = txt.length() - 1;
        if ((len > 0) && (txt.charAt(0) == '"') && (txt.charAt(len) == '"')) {
          txt = txt.substring(1, len - 1);
        }
        jPathField[nPath].setStringValue(txt);
        nPath++;
      }
      else if (type == Option.FILE) {
        // strip enclosing quotes
        final int len = txt.length() - 1;
        if ((len > 0) && (txt.charAt(0) == '"') && (txt.charAt(len) == '"')) {
          txt = txt.substring(1, len - 1);
        }
        jFileField[nFile].setStringValue(txt);
        nFile++;
      }
    }
  }

  /**
   * Saves the options. The value of the command line options field is set in the Element Property.
   * 
   * @return true if OK, false otherwise
   */
  public boolean performOk() {
    if (jResource != null) {
      final IProject project = jResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      //      prefs.put(fPreferenceName, fCmdLnOptField.getStringValue());
      prefs.put(jPreferenceName, jOptionSet.buildCmdLine());
      try {
        prefs.flush();
      } catch (final BackingStoreException e) {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }

  /**
   * Listens to changes from booleanFields, intFields, stringFields, pathField.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void propertyChange(final PropertyChangeEvent aEvent) {
    // don't see what use it is
    //    if (!aEvent.getProperty().equals("field_editor_value")) { //$NON-NLS-1$
    //      return;
    //    }
    if (jIsUpdating) {
      return;
    }
    jIsUpdating = true;
    if (aEvent.getSource() == jCmdLnOptField) {
      // handle special case where the command line field is modified
      jOptionSet.configuresFrom(jCmdLnOptField.getStringValue());
      updateFieldsValues();
    }
    else {
      final FieldEditor field = (FieldEditor) aEvent.getSource();
      // find the option
      int ixOpt;
      final int nb = jOptionSet.getOptionsSize();
      for (ixOpt = 0; ixOpt < nb; ixOpt++) {
        final String name = jOptionSet.getName(ixOpt);
        final String fieldPrefName = field.getPreferenceName();
        if (fieldPrefName.equals(name)) {
          break;
        }
      }
      final Option opt = jOptionSet.getOption(ixOpt);
      // handle a change in an BooleanFieldEditor
      if (aEvent.getSource() instanceof BooleanFieldEditor) {
        final BooleanFieldEditor bfield = (BooleanFieldEditor) aEvent.getSource();
        // set option according to the new Boolean value
        opt.setValue(bfield.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // handle a change in an IntegerFieldEditor
      else if (aEvent.getSource() instanceof IntegerFieldEditor) {
        final IntegerFieldEditor ifield = (IntegerFieldEditor) aEvent.getSource();
        // set option according to the new Integer value
        opt.setValue(ifield.getStringValue());
      }
      // handle a change in DirectoryFieldEditor
      else if (aEvent.getSource() instanceof DirectoryFieldEditor) {
        final DirectoryFieldEditor sfield = (DirectoryFieldEditor) aEvent.getSource();
        // set option according to the new String value
        //        final String val = sfield.getStringValue();
        //        specialSetValue(opt, val);
        opt.setValue(sfield.getStringValue());
      }
      // handle a change in FileFieldEditor
      else if (aEvent.getSource() instanceof FileFieldEditor) {
        final FileFieldEditor sfield = (FileFieldEditor) aEvent.getSource();
        // set option according to the new String value
        //        final String val = sfield.getStringValue();
        //        specialSetValue(opt, val);
        opt.setValue(sfield.getStringValue());
      }
      // handle a change in StringFieldEditor
      else if (aEvent.getSource() instanceof StringFieldEditor) {
        final StringFieldEditor sfield = (StringFieldEditor) aEvent.getSource();
        // set option according to the new String value
        opt.setValue(sfield.getStringValue());
      }
    }
    // update (even back) the command line field
    jCmdLnOptField.setStringValue(jOptionSet.buildCmdLine());
    jIsUpdating = false;
  }

}