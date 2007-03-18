package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
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
import org.eclipse.swt.widgets.Label;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * The basic Tab for JavaCC, JJTree, and JJDoc Options 
 * This class is extended by :
 * @see JJCCOptions, @see JJTreeOptions, @see JJDocOptions
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public abstract class JJAbstractTab extends Composite 
  implements IPropertyChangeListener, IJJConstants {

  // The optionSet used as a model
  protected OptionSet optionSet;
      
  // Controls
  protected StringFieldEditor optionsField;
  protected BooleanFieldEditor checkField[];
  protected IntegerFieldEditor intField[];
  protected StringFieldEditor[] stringField;
  protected DirectoryFieldEditor[] pathField;
  protected FileFieldEditor[] fileField;
  
  // Option name, defined in subclasses
  protected QualifiedName qualifiedName = null;
  
  // To prevent loops
  protected boolean isUpdating;

  // The IResource to work on
  protected IResource resource;

  /**
   * Constructor for JJAbstractTab.
   * @param parent
   * @param style
   */
  public JJAbstractTab(Composite parent, IResource res) {
    super(parent, SWT.NONE);
    this.resource = res;
    GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;
  }

  /**
   * Fill in the control
   */  
  public void createContents() {
    isUpdating = true;
    // Get global line options String from Resource
    IResource res = resource;
    String options = null;
    if (res != null) {
      try {
        options = res.getPersistentProperty(qualifiedName);
        optionSet.configuresFrom(options);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }         

    // Add required sections
    addLineOptionsSection();
    if (optionSet.getOptionsSize(Option.INT) != 0)
      addIntOptionsSection();
    if (optionSet.getOptionsSize(Option.BOOLEAN) != 0
        || optionSet.getOptionsSize(Option.VOID) != 0)
      addBooleanOptionsSection();
    if (optionSet.getOptionsSize(Option.STRING) != 0)
      addStringOptionsSection();
    if (optionSet.getOptionsSize(Option.PATH) != 0)
      addPathOptionsSection();
    if (optionSet.getOptionsSize(Option.FILE) != 0)
      // Not shown when IResource is a project (irrevelant ... except for CSS for JJDoc)
      if (resource.getType() != IResource.PROJECT) 
        addFileOptionsSection();
    isUpdating = false;
  }

  /**
   * Shows the line command
   */
  protected void addLineOptionsSection() {
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    optionsField =
      new StringFieldEditor(
    qualifiedName.getLocalName(),// name
    qualifiedName.getLocalName() + " :", // label //$NON-NLS-1$
        StringFieldEditor.UNLIMITED, //width
        StringFieldEditor.VALIDATE_ON_FOCUS_LOST,//strategy
        composite);

    // Copy to OptionsField
    optionsField.setStringValue(optionSet.toString());
    
    // Typing text automatically performs update of Controls
    optionsField.setPropertyChangeListener(this);
  }  

  /**
   * Shows integer options with EditFields
   */
  protected void addIntOptionsSection() {
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    // Adds EditFields for integer values
    int nb = optionSet.getOptionsSize(Option.INT);
    intField = new IntegerFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.INT)
        continue;
      String label = optionSet.getName(i) + " ("+ Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      intField[nb] = new IntegerFieldEditor(
          optionSet.getName(i), label, composite);
      intField[nb].setPropertyChangeListener(this);
      intField[nb].setEmptyStringAllowed(true);

      // Sets IntegerFieldEditor value
      intField[nb].setStringValue(optionSet.getValue(i));
      nb++;
    }
  }
  
  /**
   * Shows boolean options with Checkboxes
   */
  protected void addBooleanOptionsSection() {
    // Aligns in 2 columns
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout(2, true));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    // Adds Checkboxes for boolean values
    int nb = optionSet.getOptionsSize(Option.BOOLEAN)
           + optionSet.getOptionsSize(Option.VOID);
    checkField = new BooleanFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.BOOLEAN)
        continue;
      String label = optionSet.getName(i) + " ("+ Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      checkField[nb] =
        new BooleanFieldEditor(
        		optionSet.getName(i), label, new Composite(composite, SWT.NONE));
        checkField[nb].setPropertyChangeListener(this);
      
      // Sets CheckBox state (BooleanFieldEditor subclassed for)
      checkField[nb].setBooleanValue(optionSet.getValue(i).equals("true") ? true :false); //$NON-NLS-1$
      nb++;
    }
    
    // Adds Checkboxes for void values
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.VOID)
        continue;
      String label = optionSet.getName(i) + " ("+ Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      checkField[nb] =
        new BooleanFieldEditor(
                optionSet.getName(i), label, new Composite(composite, SWT.NONE));
        checkField[nb].setPropertyChangeListener(this);
      
      // Sets CheckBox state (BooleanFieldEditor subclassed for)
      checkField[nb].setBooleanValue(optionSet.getValue(i).equals("true") ? true :false); //$NON-NLS-1$
      nb++;
    }
  }

  /**
   * Shows String options with EditFields
   */
  protected void addStringOptionsSection() {
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    // Adds StringFields for text values
    int nb = optionSet.getOptionsSize(Option.STRING);
    stringField = new StringFieldEditor[nb];
    nb = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.STRING)
        continue;
      String label = optionSet.getName(i) + " ("+ Activator.getString("JJAbstractTab.default") + optionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      stringField[nb] = new StringFieldEditor(
          optionSet.getName(i), label, composite);
      stringField[nb].setPropertyChangeListener(this);
      stringField[nb].setEmptyStringAllowed(true);

      // Sets StringFieldEditor value
      stringField[nb].setStringValue(optionSet.getValue(i));
      nb++;
    }
  }
    
  /**
   * Shows path options
   */
  protected void addPathOptionsSection() {
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Select_directory")); //$NON-NLS-1$
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Path_can_be_pathSection")); //$NON-NLS-1$
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$

    // Adds FileBrowser for path option
    int nb = optionSet.getOptionsSize(Option.PATH);
    pathField = new DirectoryFieldEditor[nb];
    nb = 0;    
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.PATH)
        continue;
      pathField[nb] =
        new DirectoryFieldEditor(
          optionSet.getName(i),
          optionSet.getName(i),
          Activator.getString("JJAbstractTab.Choose_a_directory"), //$NON-NLS-1$
          resource.getProject().getLocation().toOSString(),
          composite);
      pathField[nb].setPropertyChangeListener(this);
      
      // Sets DirectoryFieldEditor value
      pathField[nb].setStringValue(optionSet.getValueNoQuotes(i));
      nb++;
    }
  }
  
  /**
   * Shows file options
   */
  protected void addFileOptionsSection() {
    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Select_file")); //$NON-NLS-1$
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJAbstractTab.Path_can_be_FileSection")); //$NON-NLS-1$
    new Label(composite,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$

    // Adds FileBrowser for file option
    int nb = optionSet.getOptionsSize(Option.FILE);
    fileField = new FileFieldEditor[nb];
    nb = 0;    
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      if (optionSet.getType(i) != Option.FILE)
        continue;
      fileField[nb] =
        new FileFieldEditor(optionSet.getName(i),
         optionSet.getName(i), composite);
      fileField[nb].setPropertyChangeListener(this);
      
      // Sets FileFieldEditor value
      fileField[nb].setStringValue(optionSet.getValueNoQuotes(i));
      nb++;
    }
  }

  /**
   * Resets all fields
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
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
   * Updates all fields according to optionSet
   */
  protected void updateFieldsValues() {
    // Set Fields values according to optionSet
    int nBoolean = 0;
    int nInteger = 0;
    int nString = 0;
    int nPath = 0;
    int nFile = 0;
    for (int i = 0; i < optionSet.getOptionsSize(); i++) {
      String txt = optionSet.getValueNoQuotes(i);
      if (optionSet.getType(i) == Option.BOOLEAN
      		|| optionSet.getType(i) == Option.VOID ) {
        boolean state = txt.equals("true") ? true : false; //$NON-NLS-1$
        checkField[nBoolean].setBooleanValue(state);
        nBoolean++;
      } else if (optionSet.getType(i) == Option.INT) {
        intField[nInteger].setStringValue(txt);
        nInteger++;
      } else if (optionSet.getType(i) == Option.STRING) {
        stringField[nString].setStringValue(txt);
        nString++;
      } else if (optionSet.getType(i) == Option.PATH) {
        pathField[nPath].setStringValue(txt);
        nPath++;
      } else if (optionSet.getType(i) == Option.FILE) {
        if(fileField != null)
          fileField[nFile].setStringValue(txt);
        nFile++;
      }
    }
  }
  
  /** 
   * Saves options
   * The value of options Field is set in the Element Property
   */
  public boolean performOk() {
    String options = optionsField.getStringValue();
    IResource res = this.resource;
    if (res != null)
      try {
        res.setPersistentProperty(qualifiedName, options);
      } catch (CoreException e) {
          // Nothing do do, we don't need to bother the user
      }
    return true;
  }

  /**
   * Listens to changes from booleanField, intFields, stringFields, pathField
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (!e.getProperty().equals("field_editor_value")) //$NON-NLS-1$
      return;      
    if (isUpdating)
      return;      
    
    // Handles special case where the command line field is modified.
    if (e.getSource() == optionsField ) {
      optionSet.configuresFrom(optionsField.getStringValue());
      isUpdating = true;
      updateFieldsValues();
      isUpdating = false;
    }
    else {
      FieldEditor field = (FieldEditor) e.getSource();
      
      // Which option ?
      int iOption;
      for (iOption = 0; iOption < optionSet.getOptionsSize(); iOption++) {
        String name = optionSet.getName(iOption);
        String fieldPrefName = field.getPreferenceName();
        if (fieldPrefName.equals(name))
          break;
      }

      // Handles a change in an BooleanFieldEditor.
      if (e.getSource() instanceof BooleanFieldEditor) {
        BooleanFieldEditor bfield = (BooleanFieldEditor) e.getSource();
        // Fix option according to the new Boolean value.
        optionSet.getOption(iOption).setValue(
          bfield.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // Handles a change in an IntegerFieldEditor.
      else if (e.getSource() instanceof IntegerFieldEditor) {
        IntegerFieldEditor ifield = (IntegerFieldEditor) e.getSource();
        // Fix option according to the new Integer value.
        optionSet.getOption(iOption).setValue(ifield.getStringValue());
      }
      // Handles a change in DirectoryFieldEditor.
      else if (e.getSource() instanceof DirectoryFieldEditor) {
        DirectoryFieldEditor sfield = (DirectoryFieldEditor) e.getSource();
        // Sets option according to the new String value.
        String val = sfield.getStringValue();
        // Adds quotes to take care of path with spaces
        if (val.indexOf(' ') != -1)
          optionSet.getOption(iOption).setValue("\""+val+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        else
          optionSet.getOption(iOption).setValue(val);
      }
      // Handles a change in FileFieldEditor.
      else if (e.getSource() instanceof FileFieldEditor) {
        FileFieldEditor sfield = (FileFieldEditor) e.getSource();
        // Sets option according to the new String value.
        String val = sfield.getStringValue();
        // Adds quotes to take care of path with spaces
        if (val.indexOf(' ') != -1)
          optionSet.getOption(iOption).setValue("\""+val+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        else
          optionSet.getOption(iOption).setValue(val);
      }
      // Handles a change in StringFieldEditor.
      else if (e.getSource() instanceof StringFieldEditor) {
        StringFieldEditor sfield = (StringFieldEditor) e.getSource();
        // Sets option according to the new String value.
        optionSet.getOption(iOption).setValue(sfield.getStringValue());
      }
      // Updates optionsField
      optionsField.setStringValue(optionSet.toString());  
    }
  }
}