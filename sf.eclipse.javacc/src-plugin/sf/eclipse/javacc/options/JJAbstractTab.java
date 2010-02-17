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
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * The basic Tab for JavaCC, JJTree, JJDoc and JTB Options.<br>
 * This class is extended by :
 * 
 * @see JJCCOptions
 * @see JJTreeOptions
 * @see JJDocOptions
 * @see JTBOptions
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public abstract class JJAbstractTab extends Composite implements IPropertyChangeListener, IJJConstants {

  // MMa 04/2009 : added descriptions
  // MMa 11/2009 : javadoc and formatting revision ; changed line option section
  // MMa 02/2010 : formatting and javadoc revision

  /** The optionSet used as a model */
  protected OptionSet              fOptionSet;
  // Controls
  /** String options control */
  protected StringFieldEditor      fOptionsField;
  /** Boolean options control */
  protected BooleanFieldEditor     fCheckField[];
  /** Boolean options control */
  protected IntegerFieldEditor     fIntField[];
  /** String options controls */
  protected StringFieldEditor[]    fStringField;
  /** Directory options controls */
  protected DirectoryFieldEditor[] fPathField;
  /** File options controls */
  protected FileFieldEditor[]      fFileField;
  /** The option name, defined in subclasses */
  protected String                 fPreferenceName = null;
  /** Flag to prevent loops */
  protected boolean                fIsUpdating;
  /** The IResource we are working on */
  protected IResource              fResource;

  /**
   * Standard constructor.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JJAbstractTab(final Composite aParent, final IResource aRes) {
    super(aParent, SWT.NONE);
    this.fResource = aRes;
    final GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;
  }

  /**
   * Fills in the control.
   */
  public void createContents() {
    fIsUpdating = true;
    // get global line options String from Resource
    final IResource res = fResource;
    String options = null;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      options = prefs.get(fPreferenceName, ""); //$NON-NLS-1$
      fOptionSet.configuresFrom(options);
    }
    // add required sections
    addLineOptionsSection();
    if (fOptionSet.getOptionsSize(Option.INT) != 0) {
      addIntOptionsSection();
    }
    if (fOptionSet.getOptionsSize(Option.BOOLEAN) != 0 || fOptionSet.getOptionsSize(Option.VOID) != 0) {
      addBooleanOptionsSection();
    }
    if (fOptionSet.getOptionsSize(Option.STRING) != 0) {
      addStringOptionsSection();
    }
    if (fOptionSet.getOptionsSize(Option.PATH) != 0) {
      addPathOptionsSection();
    }
    if (fOptionSet.getOptionsSize(Option.FILE) != 0) {
      // not shown when IResource is a project (relevant ... except for CSS for JJDoc)
      if (fResource.getType() != IResource.PROJECT) {
        addFileOptionsSection();
      }
    }
    fIsUpdating = false;
  }

  /**
   * Shows the resulting command line arguments.
   */
  protected void addLineOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fOptionsField = new StringFieldEditor(
                                          fPreferenceName, // name
                                          "(" + Activator.getString("JJAbstractTab.resulting") + ") " + fPreferenceName + " :", // label //$NON-NLS-1$ //$NON-NLS-2$
                                          StringFieldEditor.UNLIMITED, // width
                                          StringFieldEditor.VALIDATE_ON_FOCUS_LOST, // strategy
                                          composite);
    // copy to OptionsField
    fOptionsField.setStringValue(fOptionSet.toString());
    // typing text automatically performs update of Controls
    fOptionsField.setPropertyChangeListener(this);
  }

  /**
   * Shows integer options with EditFields.
   */
  protected void addIntOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // add EditFields for integer values
    int nb = fOptionSet.getOptionsSize(Option.INT);
    fIntField = new IntegerFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.INT) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      fIntField[nb] = new IntegerFieldEditor(fOptionSet.getName(i), label, composite);
      fIntField[nb].setPropertyChangeListener(this);
      fIntField[nb].setEmptyStringAllowed(true);
      // set IntegerFieldEditor value
      fIntField[nb].setStringValue(fOptionSet.getValue(i));
      nb++;
    }
  }

  /**
   * Shows boolean options with checkboxes.
   */
  protected void addBooleanOptionsSection() {
    // aligns in 2 columns
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout(2, true));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // add checkboxes for boolean values
    int nb = fOptionSet.getOptionsSize(Option.BOOLEAN) + fOptionSet.getOptionsSize(Option.VOID);
    fCheckField = new BooleanFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.BOOLEAN) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      fCheckField[nb] = new BooleanFieldEditor(fOptionSet.getName(i), label, new Composite(composite,
                                                                                           SWT.NONE));
      fCheckField[nb].setPropertyChangeListener(this);
      // set checkbox state (BooleanFieldEditor subclassed for)
      fCheckField[nb].setBooleanValue(fOptionSet.getValue(i).equals("true") ? true : false); //$NON-NLS-1$
      nb++;
    }
    // add checkboxes for void values
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.VOID) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      fCheckField[nb] = new BooleanFieldEditor(fOptionSet.getName(i), label, new Composite(composite,
                                                                                           SWT.NONE));
      fCheckField[nb].setPropertyChangeListener(this);
      // set checkbox state (BooleanFieldEditor subclassed for)
      fCheckField[nb].setBooleanValue(fOptionSet.getValue(i).equals("true") ? true : false); //$NON-NLS-1$
      nb++;
    }
  }

  /**
   * Shows String options with EditFields.
   */
  protected void addStringOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // add StringFields for text values
    int nb = fOptionSet.getOptionsSize(Option.STRING);
    fStringField = new StringFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.STRING) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      fStringField[nb] = new StringFieldEditor(fOptionSet.getName(i), label, composite);
      fStringField[nb].setPropertyChangeListener(this);
      fStringField[nb].setEmptyStringAllowed(true);
      // set StringFieldEditor value
      fStringField[nb].setStringValue(fOptionSet.getValue(i));
      nb++;
    }
  }

  /**
   * Shows path options.
   */
  protected void addPathOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Select_directory")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Path_can_be_pathSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    // add FileBrowser for path option
    int nb = fOptionSet.getOptionsSize(Option.PATH);
    fPathField = new DirectoryFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.PATH) {
        continue;
      }
      fPathField[nb] = new DirectoryFieldEditor(fOptionSet.getName(i), fOptionSet.getNameAndDescription(i),
                                                Activator.getString("JJAbstractTab.Choose_a_directory"), //$NON-NLS-1$
                                                fResource.getProject().getLocation().toOSString(), composite);
      fPathField[nb].setPropertyChangeListener(this);
      // set DirectoryFieldEditor value
      fPathField[nb].setStringValue(fOptionSet.getValueNoQuotes(i));
      nb++;
    }
  }

  /**
   * Shows file options.
   */
  protected void addFileOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Select_file")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL)
                                                   .setText(Activator
                                                                     .getString("JJAbstractTab.Path_can_be_FileSection")); //$NON-NLS-1$
    new Label(composite, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    // add FileBrowser for file option
    int nb = fOptionSet.getOptionsSize(Option.FILE);
    fFileField = new FileFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      if (fOptionSet.getType(i) != Option.FILE) {
        continue;
      }
      fFileField[nb] = new FileFieldEditor(fOptionSet.getName(i), fOptionSet.getNameAndDescription(i),
                                           composite);
      fFileField[nb].setPropertyChangeListener(this);
      // set FileFieldEditor value
      fFileField[nb].setStringValue(fOptionSet.getValueNoQuotes(i));
      nb++;
    }
  }

  /**
   * Resets all fields.
   * 
   * @see PreferencePage
   */
  public void performDefaults() {
    // reset the optionSet
    fOptionSet.resetToDefaultValues();
    // reset global line options field
    fOptionsField.setStringValue(null);
    // set Fields according to Options
    updateFieldsValues();
  }

  /**
   * Updates all fields according to optionSet.
   */
  protected void updateFieldsValues() {
    // set Fields values according to optionSet
    int nBoolean = 0;
    int nInteger = 0;
    int nString = 0;
    int nPath = 0;
    int nFile = 0;
    for (int i = 0; i < fOptionSet.getOptionsSize(); i++) {
      final String txt = fOptionSet.getValueNoQuotes(i);
      final int type = fOptionSet.getType(i);
      if (type == Option.BOOLEAN || type == Option.VOID) {
        final boolean state = txt.equals("true") ? true : false; //$NON-NLS-1$
        fCheckField[nBoolean].setBooleanValue(state);
        nBoolean++;
      }
      else if (type == Option.INT) {
        fIntField[nInteger].setStringValue(txt);
        nInteger++;
      }
      else if (type == Option.STRING) {
        fStringField[nString].setStringValue(txt);
        nString++;
      }
      else if (type == Option.PATH) {
        fPathField[nPath].setStringValue(txt);
        nPath++;
      }
      else if (type == Option.FILE) {
        if (fFileField != null) {
          fFileField[nFile].setStringValue(txt);
        }
        nFile++;
      }
    }
  }

  /**
   * Saves options. The value of options Field is set in the Element Property.
   * 
   * @return true if OK, false otherwise
   */
  public boolean performOk() {
    final String options = fOptionsField.getStringValue();
    final IResource res = this.fResource;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      prefs.put(fPreferenceName, options);
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
   * Listens to changes from booleanField, intFields, stringFields, pathField
   * 
   * @param aEvent the property change event object describing which property changed and how
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent aEvent) {
    if (!aEvent.getProperty().equals("field_editor_value")) { //$NON-NLS-1$
      return;
    }
    if (fIsUpdating) {
      return;
    }
    // handle special case where the command line field is modified
    if (aEvent.getSource() == fOptionsField) {
      fOptionSet.configuresFrom(fOptionsField.getStringValue());
      fIsUpdating = true;
      updateFieldsValues();
      fIsUpdating = false;
    }
    else {
      final FieldEditor field = (FieldEditor) aEvent.getSource();
      // which option ?
      int iOption;
      for (iOption = 0; iOption < fOptionSet.getOptionsSize(); iOption++) {
        final String name = fOptionSet.getName(iOption);
        final String fieldPrefName = field.getPreferenceName();
        if (fieldPrefName.equals(name)) {
          break;
        }
      }
      // handle a change in an BooleanFieldEditor
      if (aEvent.getSource() instanceof BooleanFieldEditor) {
        final BooleanFieldEditor bfield = (BooleanFieldEditor) aEvent.getSource();
        // Fix option according to the new Boolean value
        fOptionSet.getOption(iOption).setValue(bfield.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // handle a change in an IntegerFieldEditor
      else if (aEvent.getSource() instanceof IntegerFieldEditor) {
        final IntegerFieldEditor ifield = (IntegerFieldEditor) aEvent.getSource();
        // Fix option according to the new Integer value
        fOptionSet.getOption(iOption).setValue(ifield.getStringValue());
      }
      // handle a change in DirectoryFieldEditor.
      else if (aEvent.getSource() instanceof DirectoryFieldEditor) {
        final DirectoryFieldEditor sfield = (DirectoryFieldEditor) aEvent.getSource();
        // set option according to the new String value
        final String val = sfield.getStringValue();
        // add quotes to take care of path with spaces
        if (val.indexOf(' ') != -1) {
          fOptionSet.getOption(iOption).setValue("\"" + val + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          fOptionSet.getOption(iOption).setValue(val);
        }
      }
      // handle a change in FileFieldEditor
      else if (aEvent.getSource() instanceof FileFieldEditor) {
        final FileFieldEditor sfield = (FileFieldEditor) aEvent.getSource();
        // set option according to the new String value
        final String val = sfield.getStringValue();
        // add quotes to take care of path with spaces
        if (val.indexOf(' ') != -1) {
          fOptionSet.getOption(iOption).setValue("\"" + val + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          fOptionSet.getOption(iOption).setValue(val);
        }
      }
      // handle a change in StringFieldEditor
      else if (aEvent.getSource() instanceof StringFieldEditor) {
        final StringFieldEditor sfield = (StringFieldEditor) aEvent.getSource();
        // set option according to the new String value
        fOptionSet.getOption(iOption).setValue(sfield.getStringValue());
      }
      // update optionsField
      fOptionsField.setStringValue(fOptionSet.toString());
    }
  }
}