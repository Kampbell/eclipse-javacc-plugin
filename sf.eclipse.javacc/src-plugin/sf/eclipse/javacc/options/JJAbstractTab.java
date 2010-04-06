package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
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

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * The basic Tab for JavaCC, JJTree, JJDoc and JTB project options.<br>
 * This class is extended by :
 * 
 * @see JavaCCOptions
 * @see JJTreeOptions
 * @see JJDocOptions
 * @see JTBOptions
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public abstract class JJAbstractTab extends Composite implements IPropertyChangeListener, IJJConstants {

  // MMa 04/2009 : added descriptions
  // MMa 11/2009 : javadoc and formatting revision ; changed line option section
  // MMa 02/2010 : formatting and javadoc revision ; fixed not stored Option.VOID properties issue
  // ... ....... : fixed output file not showing issue ; fixed display true cases for void options
  // MMa 03/2010 : enhanced layout (groups / tool tips) ; renamed preference keys

  /** The optionSet used as a model */
  protected OptionSet              fOptionSet;
  // Controls
  /** The command line options control */
  protected StringFieldEditor      fCmdLnOptField;
  /** The Boolean options control */
  protected BooleanFieldEditor     fCheckboxField[];
  /** The Integer options control */
  protected IntegerFieldEditor     fIntegerField[];
  /** The String options controls */
  protected StringFieldEditor[]    fStringField;
  /** The Directory options controls */
  protected DirectoryFieldEditor[] fPathField;
  /** The File options controls */
  protected FileFieldEditor[]      fFileField;
  /** The number of columns to use for boolean options */
  protected int                    fNbColBooleans     = 1;
  /** The options preference property, defined in subclasses */
  protected String                 fPreferenceName    = null;
  /** The flag to prevent loops from user input and change listeners */
  protected boolean                fIsUpdating;
  /** The IResource we are working on */
  protected IResource              fResource;
  /** The "default" label */
  String                           fDefaultLabel      = Activator.getString("JJAbstractTab.default");      //$NON-NLS-1$
  /** The "empty default" label */
  String                           fEmptyDefaultLabel = Activator.getString("JJAbstractTab.empty_default"); //$NON-NLS-1$

  /**
   * Standard constructor.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JJAbstractTab(final Composite aParent, final IResource aRes) {
    super(aParent, SWT.NONE);
    fResource = aRes;
  }

  /**
   * Fills in the control.
   */
  public void createContents() {
    fIsUpdating = true;
    // get the global line options string from the resource
    String options = null;
    if (fResource != null) {
      final IProject proj = fResource.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      options = prefs.get(fPreferenceName, ""); //$NON-NLS-1$
      fOptionSet.configuresFrom(options);
    }
    // add layout
    final GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // add group
    final Group resGrp = new Group(this, SWT.NONE);
    resGrp.setText(Activator.getString("JJAbstractTab.Resulting_group")); //$NON-NLS-1$
    resGrp.setLayout(layout);
    resGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add command line options
    addCmdLnOptSection(options, resGrp);

    // add group
    final Group optGrp = new Group(this, SWT.NONE);
    optGrp.setText(Activator.getString("JJAbstractTab.Options_group")); //$NON-NLS-1$
    optGrp.setLayout(layout);
    optGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    if (fOptionSet.getOptionsSize(Option.INT) != 0) {
      addIntegerOptSection(optGrp);
    }
    if (fOptionSet.getOptionsSize(Option.BOOLEAN) != 0 || fOptionSet.getOptionsSize(Option.VOID) != 0) {
      addBooleanOptSection(optGrp);
    }
    if (fOptionSet.getOptionsSize(Option.STRING) != 0) {
      addStringOptSection(optGrp);
    }
    if (fOptionSet.getOptionsSize(Option.PATH) != 0) {
      addPathOptSection(optGrp);
    }
    if (fOptionSet.getOptionsSize(Option.FILE) != 0) {
      //      // not shown when IResource is a project (relevant ... except for CSS for JJDoc)
      //      if (fResource.getType() != IResource.PROJECT) {
      addFileOptSection(optGrp);
      //      }
    }
    fIsUpdating = false;
  }

  /**
   * Shows the resulting command line arguments field.
   * 
   * @param aStr the command line arguments
   * @param aGrp the group inside which to add the field
   */
  protected void addCmdLnOptSection(final String aStr, final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    fCmdLnOptField = new StringFieldEditor(
                                           fPreferenceName, // name
                                           "(" + Activator.getString("JJAbstractTab.Resulting") + ") " + fPreferenceName + " :", // label //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                           StringFieldEditor.UNLIMITED, // width
                                           StringFieldEditor.VALIDATE_ON_FOCUS_LOST, // strategy
                                           composite);
    fCmdLnOptField.setStringValue(aStr);
    fCmdLnOptField.setPropertyChangeListener(this);
  }

  /**
   * Shows integer options with edit fields.
   * 
   * @param aGrp the group inside which to add the option
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
    int k = fOptionSet.getOptionsSize(Option.INT);
    fIntegerField = new IntegerFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) != Option.INT) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + fDefaultLabel + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
      fIntegerField[k] = new IntegerFieldEditor(fOptionSet.getName(i), label, composite);
      fIntegerField[k].setEmptyStringAllowed(true);
      fIntegerField[k].setStringValue(fOptionSet.getValue(i));
      fIntegerField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows boolean options with checkboxes.
   * 
   * @param aGrp the group inside which to add the option
   */
  protected void addBooleanOptSection(final Composite aGrp) {
    // aligns in 2 columns
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout(fNbColBooleans, false));
    composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    int k = fOptionSet.getOptionsSize(Option.BOOLEAN) + fOptionSet.getOptionsSize(Option.VOID);
    fCheckboxField = new BooleanFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) == Option.BOOLEAN || fOptionSet.getType(i) == Option.VOID) {
        final String label = fOptionSet.getNameAndDescription(i)
                             + " (" + fDefaultLabel + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
        // need to create a new Composite to have 2 columns !
        fCheckboxField[k] = new BooleanFieldEditor(fOptionSet.getName(i), label, new Composite(composite,
                                                                                               SWT.NONE));
        fCheckboxField[k].setBooleanValue("true".equals(fOptionSet.getValue(i)) ? true : false); //$NON-NLS-1$
        fCheckboxField[k].setPropertyChangeListener(this);
        k++;
      }
    }
  }

  /**
   * Shows string options with edit fields.
   * 
   * @param aGrp the group inside which to add the option
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
    int k = fOptionSet.getOptionsSize(Option.STRING);
    fStringField = new StringFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) != Option.STRING) {
        continue;
      }
      final String defVal = fOptionSet.getDefaultValue(i);
      String defLbl = fEmptyDefaultLabel;
      if (defVal != null && defVal.length() == 0) {
        defLbl = fDefaultLabel + defVal;
      }
      final String label = fOptionSet.getNameAndDescription(i).concat(" (").concat(defLbl).concat(")"); //$NON-NLS-1$ //$NON-NLS-2$ 
      fStringField[k] = new StringFieldEditor(fOptionSet.getName(i), label, composite);
      fStringField[k].setEmptyStringAllowed(true);
      fStringField[k].setStringValue(fOptionSet.getValueInQuotes(i));
      fStringField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows path options with edit fields.
   * 
   * @param aGrp the group inside which to add the option
   */
  protected void addPathOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Select_directory")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Path_can_be_pathSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    int k = fOptionSet.getOptionsSize(Option.PATH);
    fPathField = new DirectoryFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) != Option.PATH) {
        continue;
      }
      fPathField[k] = new DirectoryFieldEditor(fOptionSet.getName(i), fOptionSet.getNameAndDescription(i),
                                               Activator.getString("JJAbstractTab.Choose_a_directory"), //$NON-NLS-1$
                                               fResource.getProject().getLocation().toOSString(), composite);
      fPathField[k].setStringValue(fOptionSet.getValueInQuotes(i));
      fPathField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows file options with edit fields.
   * 
   * @param aGrp the group inside which to add the option
   */
  protected void addFileOptSection(final Composite aGrp) {
    final Composite composite = new Composite(aGrp, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Select_file")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Path_can_be_FileSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    int k = fOptionSet.getOptionsSize(Option.FILE);
    fFileField = new FileFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) != Option.FILE) {
        continue;
      }
      fFileField[k] = new FileFieldEditor(fOptionSet.getName(i), fOptionSet.getNameAndDescription(i),
                                          composite);
      fFileField[k].setStringValue(fOptionSet.getValueInQuotes(i));
      fFileField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Resets all fields.
   * 
   * @see PreferencePage
   */
  public void performDefaults() {
    fIsUpdating = true;
    fOptionSet.resetToDefaultValues();
    updateFieldsValues();
    fCmdLnOptField.setStringValue(fOptionSet.buildCmdLine());
    fIsUpdating = false;
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
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      String txt = fOptionSet.getValue(i);
      final int type = fOptionSet.getType(i);
      if (type == Option.BOOLEAN || type == Option.VOID) {
        final boolean state = "true".equals(txt) ? true : false; //$NON-NLS-1$
        fCheckboxField[nBoolean].setBooleanValue(state);
        nBoolean++;
      }
      else if (type == Option.INT) {
        fIntegerField[nInteger].setStringValue(txt);
        nInteger++;
      }
      else if (type == Option.STRING) {
        fStringField[nString].setStringValue(txt);
        nString++;
      }
      else if (type == Option.PATH) {
        // strip enclosing quotes
        final int len = txt.length() - 1;
        if ((len > 0) && (txt.charAt(0) == '"') && (txt.charAt(len) == '"')) {
          txt = txt.substring(1, len - 1);
        }
        fPathField[nPath].setStringValue(txt);
        nPath++;
      }
      else if (type == Option.FILE) {
        // strip enclosing quotes
        final int len = txt.length() - 1;
        if ((len > 0) && (txt.charAt(0) == '"') && (txt.charAt(len) == '"')) {
          txt = txt.substring(1, len - 1);
        }
        fFileField[nFile].setStringValue(txt);
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
    if (fResource != null) {
      final IProject project = fResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      //      prefs.put(fPreferenceName, fCmdLnOptField.getStringValue());
      prefs.put(fPreferenceName, fOptionSet.buildCmdLine());
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
   * 
   * @param aEvent the property change event object describing which property changed and how
   * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent aEvent) {
    // don't see what use it is
    //    if (!aEvent.getProperty().equals("field_editor_value")) { //$NON-NLS-1$
    //      return;
    //    }
    if (fIsUpdating) {
      return;
    }
    fIsUpdating = true;
    if (aEvent.getSource() == fCmdLnOptField) {
      // handle special case where the command line field is modified
      fOptionSet.configuresFrom(fCmdLnOptField.getStringValue());
      updateFieldsValues();
    }
    else {
      final FieldEditor field = (FieldEditor) aEvent.getSource();
      // find the option
      int ixOpt;
      final int nb = fOptionSet.getOptionsSize();
      for (ixOpt = 0; ixOpt < nb; ixOpt++) {
        final String name = fOptionSet.getName(ixOpt);
        final String fieldPrefName = field.getPreferenceName();
        if (fieldPrefName.equals(name)) {
          break;
        }
      }
      final Option opt = fOptionSet.getOption(ixOpt);
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
    fCmdLnOptField.setStringValue(fOptionSet.buildCmdLine());
    fIsUpdating = false;
  }

}