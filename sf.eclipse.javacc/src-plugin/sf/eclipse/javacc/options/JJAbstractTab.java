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
  // MMa 02/2010 : formatting and javadoc revision ; fixed not stored Option.VOID properties issue ; fixed output file not showing issue

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
  /** The options preference property, defined in subclasses */
  protected String                 fPreferenceName = null;
  /** The flag to prevent loops from user input and change listeners */
  protected boolean                fIsUpdating;
  /** The IResource we are working on */
  protected IResource              fResource;
  /** The "default" label */
  String                           fDefaultLabel   = Activator.getString("JJAbstractTab.default");

  /**
   * Standard constructor.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JJAbstractTab(final Composite aParent, final IResource aRes) {
    super(aParent, SWT.NONE);
    fResource = aRes;
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
    // get global line options string from resource
    final IResource res = fResource;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      final String options = prefs.get(fPreferenceName, ""); //$NON-NLS-1$
      fOptionSet.configuresFrom(options);
    }
    // add required sections
    addCmdLnOptSection();
    if (fOptionSet.getOptionsSize(Option.INT) != 0) {
      addIntegerOptSection();
    }
    if (fOptionSet.getOptionsSize(Option.BOOLEAN) != 0 || fOptionSet.getOptionsSize(Option.VOID) != 0) {
      addBooleanOptSection();
    }
    if (fOptionSet.getOptionsSize(Option.STRING) != 0) {
      addStringOptSection();
    }
    if (fOptionSet.getOptionsSize(Option.PATH) != 0) {
      addPathOptSection();
    }
    if (fOptionSet.getOptionsSize(Option.FILE) != 0) {
      //      // not shown when IResource is a project (relevant ... except for CSS for JJDoc)
      //      if (fResource.getType() != IResource.PROJECT) {
      addFileOptSection();
      //      }
    }
    fIsUpdating = false;
  }

  /**
   * Shows the resulting command line arguments.
   */
  protected void addCmdLnOptSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fCmdLnOptField = new StringFieldEditor(
                                           fPreferenceName, // name
                                           "(" + Activator.getString("JJAbstractTab.resulting") + ") " + fPreferenceName + " :", // label //$NON-NLS-1$ //$NON-NLS-2$
                                           StringFieldEditor.UNLIMITED, // width
                                           StringFieldEditor.VALIDATE_ON_FOCUS_LOST, // strategy
                                           composite);
    fCmdLnOptField.setStringValue(fOptionSet.buildCmdLine());
    fCmdLnOptField.setPropertyChangeListener(this);
  }

  /**
   * Shows integer options with EditFields.
   */
  protected void addIntegerOptSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
   */
  protected void addBooleanOptSection() {
    // aligns in 2 columns
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout(2, true));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    int k = fOptionSet.getOptionsSize(Option.BOOLEAN) + fOptionSet.getOptionsSize(Option.VOID);
    fCheckboxField = new BooleanFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) == Option.BOOLEAN || fOptionSet.getType(i) == Option.VOID) {
        final String label = fOptionSet.getNameAndDescription(i)
                             + " (" + fDefaultLabel + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
        fCheckboxField[k] = new BooleanFieldEditor(fOptionSet.getName(i), label, new Composite(composite,
                                                                                               SWT.NONE));
        fCheckboxField[k].setBooleanValue("true".equals(fOptionSet.getValue(i)) ? true : false); //$NON-NLS-1$
        fCheckboxField[k].setPropertyChangeListener(this);
        k++;
      }
    }
  }

  /**
   * Shows String options with EditFields.
   */
  protected void addStringOptSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    int k = fOptionSet.getOptionsSize(Option.STRING);
    fStringField = new StringFieldEditor[k];
    k = 0;
    final int nb = fOptionSet.getOptionsSize();
    for (int i = 0; i < nb; i++) {
      if (fOptionSet.getType(i) != Option.STRING) {
        continue;
      }
      final String label = fOptionSet.getNameAndDescription(i)
                           + " (" + fDefaultLabel + fOptionSet.getDefaultValue(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
      fStringField[k] = new StringFieldEditor(fOptionSet.getName(i), label, composite);
      fStringField[k].setEmptyStringAllowed(true);
      fStringField[k].setStringValue(fOptionSet.getValueInQuotes(i));
      fStringField[k].setPropertyChangeListener(this);
      k++;
    }
  }

  /**
   * Shows path options.
   */
  protected void addPathOptSection() {
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
   * Shows file options.
   */
  protected void addFileOptSection() {
    final Composite composite = new Composite(this, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
    fOptionSet.resetToDefaultValues();
    fCmdLnOptField.setStringValue(null);
    updateFieldsValues();
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
    final IResource res = fResource;
    if (res != null) {
      final IProject project = res.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      prefs.put(fPreferenceName, fCmdLnOptField.getStringValue());
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
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent aEvent) {
    // don't see what use it is
    //    if (!aEvent.getProperty().equals("field_editor_value")) { //$NON-NLS-1$
    //      return;
    //    }
    if (fIsUpdating) {
      return;
    }
    if (aEvent.getSource() == fCmdLnOptField) {
      // handle special case where the command line field is modified
      fOptionSet.configuresFrom(fCmdLnOptField.getStringValue());
      fIsUpdating = true;
      updateFieldsValues();
      fIsUpdating = false;
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
  }

  //  /**
  //   * Sets the value of the option, adding extra enclosing quotes if the value contains one or more spaces.
  //   * 
  //   * @param aOpt the option to set
  //   * @param aVal the value to set
  //   */
  //  private void specialSetValue(final Option aOpt, final String aVal) {
  //    final String str = (aVal == null ? "" : aVal);
  //    // add quotes to take care of path / files with spaces
  //    if (str.indexOf(' ') != -1) {
  //      aOpt.setValue("\"" + str + "\""); //$NON-NLS-1$ //$NON-NLS-2$
  //    }
  //    else {
  //      aOpt.setValue(str);
  //    }
  //  }
}