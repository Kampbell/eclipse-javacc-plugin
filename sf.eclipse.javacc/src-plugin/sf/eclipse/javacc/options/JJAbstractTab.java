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
 * @author Remi Koutcherawy 2003-2009 - CeCILL License http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public abstract class JJAbstractTab extends Composite implements IPropertyChangeListener, IJJConstants {

  //MMa 04/09 : added descriptions
  //MMa 11/09 : javadoc and formatting revision ; changed line option section

  /** the optionSet used as a model */
  protected OptionSet              optionSet;
  // Controls
  /** string options control */
  protected StringFieldEditor      optionsField;
  /** boolean options control */
  protected BooleanFieldEditor     checkField[];
  /** boolean options control */
  protected IntegerFieldEditor     intField[];
  /** string options controls */
  protected StringFieldEditor[]    stringField;
  /** directory options controls */
  protected DirectoryFieldEditor[] pathField;
  /** file options controls */
  protected FileFieldEditor[]      fileField;
  /** the option name, defined in subclasses */
  protected String                 preferenceName = null;
  /** flag to prevent loops */
  protected boolean                isUpdating;
  /** The IResource we are working on */
  protected IResource              resource;

  /**
   * Standard onstructor.
   * 
   * @param parent the parent
   * @param res the resource
   */
  public JJAbstractTab(final Composite parent, final IResource res) {
    super(parent, SWT.NONE);
    this.resource = res;
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
    isUpdating = true;
    // Get global line options String from Resource
    final IResource res = resource;
    String options = null;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      options = prefs.get(preferenceName, ""); //$NON-NLS-1$
      optionSet.configuresFrom(options);
    }
    // Add required sections
    addLineOptionsSection();
    if (optionSet.getOptionsSize(Option.INT) != 0) {
      addIntOptionsSection();
    }
    if (optionSet.getOptionsSize(Option.BOOLEAN) != 0 || optionSet.getOptionsSize(Option.VOID) != 0) {
      addBooleanOptionsSection();
    }
    if (optionSet.getOptionsSize(Option.STRING) != 0) {
      addStringOptionsSection();
    }
    if (optionSet.getOptionsSize(Option.PATH) != 0) {
      addPathOptionsSection();
    }
    if (optionSet.getOptionsSize(Option.FILE) != 0) {
      // Not shown when IResource is a project (irrevelant ... except for CSS for JJDoc)
      if (resource.getType() != IResource.PROJECT) {
        addFileOptionsSection();
      }
    }
    isUpdating = false;
  }

  /**
   * Shows the resulting command line arguments.
   */
  protected void addLineOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    optionsField = new StringFieldEditor(preferenceName,// name
                                         "(Resulting) " + preferenceName + " :", // label //$NON-NLS-1$ //$NON-NLS-2$
                                         StringFieldEditor.UNLIMITED, // width
                                         StringFieldEditor.VALIDATE_ON_FOCUS_LOST,// strategy
                                         composite);
    // Copy to OptionsField
    optionsField.setStringValue(optionSet.toString());
    // Typing text automatically performs update of Controls
    optionsField.setPropertyChangeListener(this);
  }

  /**
   * Shows integer options with EditFields.
   */
  protected void addIntOptionsSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // Adds EditFields for integer values
    int nb = optionSet.getOptionsSize(Option.INT);
    intField = new IntegerFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.INT) {
        continue;
      }
      final String label = optionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      intField[nb] = new IntegerFieldEditor(optionSet.getName(i), label, composite);
      intField[nb].setPropertyChangeListener(this);
      intField[nb].setEmptyStringAllowed(true);
      // Sets IntegerFieldEditor value
      intField[nb].setStringValue(optionSet.getValue(i));
      nb++;
    }
  }

  /**
   * Shows boolean options with Checkboxes.
   */
  protected void addBooleanOptionsSection() {
    // Aligns in 2 columns
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout(2, true));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // Adds Checkboxes for boolean values
    int nb = optionSet.getOptionsSize(Option.BOOLEAN) + optionSet.getOptionsSize(Option.VOID);
    checkField = new BooleanFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.BOOLEAN) {
        continue;
      }
      final String label = optionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      checkField[nb] = new BooleanFieldEditor(optionSet.getName(i), label, new Composite(composite, SWT.NONE));
      checkField[nb].setPropertyChangeListener(this);
      // Sets CheckBox state (BooleanFieldEditor subclassed for)
      checkField[nb].setBooleanValue(optionSet.getValue(i).equals("true") ? true : false); //$NON-NLS-1$
      nb++;
    }
    // Adds Checkboxes for void values
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.VOID) {
        continue;
      }
      final String label = optionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      checkField[nb] = new BooleanFieldEditor(optionSet.getName(i), label, new Composite(composite, SWT.NONE));
      checkField[nb].setPropertyChangeListener(this);
      // Sets CheckBox state (BooleanFieldEditor subclassed for)
      checkField[nb].setBooleanValue(optionSet.getValue(i).equals("true") ? true : false); //$NON-NLS-1$
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
    // Adds StringFields for text values
    int nb = optionSet.getOptionsSize(Option.STRING);
    stringField = new StringFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.STRING) {
        continue;
      }
      final String label = optionSet.getNameAndDescription(i)
                           + " (" + Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      stringField[nb] = new StringFieldEditor(optionSet.getName(i), label, composite);
      stringField[nb].setPropertyChangeListener(this);
      stringField[nb].setEmptyStringAllowed(true);
      // Sets StringFieldEditor value
      stringField[nb].setStringValue(optionSet.getValue(i));
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
    // Adds FileBrowser for path option
    int nb = optionSet.getOptionsSize(Option.PATH);
    pathField = new DirectoryFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.PATH) {
        continue;
      }
      pathField[nb] = new DirectoryFieldEditor(optionSet.getName(i), optionSet.getNameAndDescription(i),
                                               Activator.getString("JJAbstractTab.Choose_a_directory"), //$NON-NLS-1$
                                               resource.getProject().getLocation().toOSString(), composite);
      pathField[nb].setPropertyChangeListener(this);
      // Sets DirectoryFieldEditor value
      pathField[nb].setStringValue(optionSet.getValueNoQuotes(i));
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
    // Adds FileBrowser for file option
    int nb = optionSet.getOptionsSize(Option.FILE);
    fileField = new FileFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.FILE) {
        continue;
      }
      fileField[nb] = new FileFieldEditor(optionSet.getName(i), optionSet.getNameAndDescription(i), composite);
      fileField[nb].setPropertyChangeListener(this);
      // Sets FileFieldEditor value
      fileField[nb].setStringValue(optionSet.getValueNoQuotes(i));
      nb++;
    }
  }

  /**
   * Resets all fields.
   * 
   * @see PreferencePage
   */
  public void performDefaults() {
    // Reset the optionSet
    optionSet.resetToDefaultValues();
    // Reset global line options field
    optionsField.setStringValue(null);
    // Set Fields according to Options
    updateFieldsValues();
  }

  /**
   * Updates all fields according to optionSet.
   */
  protected void updateFieldsValues() {
    // Set Fields values according to optionSet
    int nBoolean = 0;
    int nInteger = 0;
    int nString = 0;
    int nPath = 0;
    int nFile = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      final String txt = optionSet.getValueNoQuotes(i);
      if (optionSet.getType(i) == Option.BOOLEAN || optionSet.getType(i) == Option.VOID) {
        final boolean state = txt.equals("true") ? true : false; //$NON-NLS-1$
        checkField[nBoolean].setBooleanValue(state);
        nBoolean++;
      }
      else if (optionSet.getType(i) == Option.INT) {
        intField[nInteger].setStringValue(txt);
        nInteger++;
      }
      else if (optionSet.getType(i) == Option.STRING) {
        stringField[nString].setStringValue(txt);
        nString++;
      }
      else if (optionSet.getType(i) == Option.PATH) {
        pathField[nPath].setStringValue(txt);
        nPath++;
      }
      else if (optionSet.getType(i) == Option.FILE) {
        if (fileField != null) {
          fileField[nFile].setStringValue(txt);
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
    final String options = optionsField.getStringValue();
    final IResource res = this.resource;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      prefs.put(preferenceName, options);
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
   * @param e the property change event object describing which property changed and how
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent e) {
    if (!e.getProperty().equals("field_editor_value")) { //$NON-NLS-1$
      return;
    }
    if (isUpdating) {
      return;
    }
    // Handles special case where the command line field is modified.
    if (e.getSource() == optionsField) {
      optionSet.configuresFrom(optionsField.getStringValue());
      isUpdating = true;
      updateFieldsValues();
      isUpdating = false;
    }
    else {
      final FieldEditor field = (FieldEditor) e.getSource();
      // Which option ?
      int iOption;
      for (iOption = 0; iOption < optionSet.getOptionsSize(); iOption++) {
        final String name = optionSet.getName(iOption);
        final String fieldPrefName = field.getPreferenceName();
        if (fieldPrefName.equals(name)) {
          break;
        }
      }
      // Handles a change in an BooleanFieldEditor.
      if (e.getSource() instanceof BooleanFieldEditor) {
        final BooleanFieldEditor bfield = (BooleanFieldEditor) e.getSource();
        // Fix option according to the new Boolean value.
        optionSet.getOption(iOption).setValue(bfield.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // Handles a change in an IntegerFieldEditor.
      else if (e.getSource() instanceof IntegerFieldEditor) {
        final IntegerFieldEditor ifield = (IntegerFieldEditor) e.getSource();
        // Fix option according to the new Integer value.
        optionSet.getOption(iOption).setValue(ifield.getStringValue());
      }
      // Handles a change in DirectoryFieldEditor.
      else if (e.getSource() instanceof DirectoryFieldEditor) {
        final DirectoryFieldEditor sfield = (DirectoryFieldEditor) e.getSource();
        // Sets option according to the new String value.
        final String val = sfield.getStringValue();
        // Adds quotes to take care of path with spaces
        if (val.indexOf(' ') != -1) {
          optionSet.getOption(iOption).setValue("\"" + val + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          optionSet.getOption(iOption).setValue(val);
        }
      }
      // Handles a change in FileFieldEditor.
      else if (e.getSource() instanceof FileFieldEditor) {
        final FileFieldEditor sfield = (FileFieldEditor) e.getSource();
        // Sets option according to the new String value.
        final String val = sfield.getStringValue();
        // Adds quotes to take care of path with spaces
        if (val.indexOf(' ') != -1) {
          optionSet.getOption(iOption).setValue("\"" + val + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          optionSet.getOption(iOption).setValue(val);
        }
      }
      // Handles a change in StringFieldEditor.
      else if (e.getSource() instanceof StringFieldEditor) {
        final StringFieldEditor sfield = (StringFieldEditor) e.getSource();
        // Sets option according to the new String value.
        optionSet.getOption(iOption).setValue(sfield.getStringValue());
      }
      // Updates optionsField
      optionsField.setStringValue(optionSet.toString());
    }
  }
}