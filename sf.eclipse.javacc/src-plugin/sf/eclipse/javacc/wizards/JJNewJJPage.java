package sf.eclipse.javacc.wizards;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * The "New" wizard page allows setting the source directory, the package for the new file, the extension, as
 * well as the file name. This page handles the file creation.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public class JJNewJJPage extends WizardPage implements IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision ; changed some modifiers for synthetic accesses
  // MMa 02/2010 : formatting and javadoc revision ; differentiate static / non static files

  /** The source directory */
  IPackageFragmentRoot         fSrcRootFragment;
  /** The package */
  private IPackageFragment     fPackageFragment;
  /** The workspace root directory returned by the plugin */
  private final IWorkspaceRoot fWorkspaceRoot;
  /** The source input */
  Text                         fSrcRootText;
  /** The package input */
  Text                         fPackageText;
  /** The file name input */
  private Text                 fFileNameText;
  /** The source change status */
  protected IStatus            fSrcRootStatus;
  /** The package change status */
  protected IStatus            fPackageStatus;
  /** The file name change status */
  protected IStatus            fFileStatus;
  /** The checked source */
  String                       fSrcRoot;
  /** The checked package */
  private String               fPackage;
  /** The checked file name */
  private String               fFileName;
  /** The checked file extension */
  String                       fExtension;
  /** The static / non static flag */
  boolean                      fStaticFlag;

  /**
   * Creates a new <code>NewJJWizardPage</code>.
   */
  public JJNewJJPage() {
    super("NewJJWizardPage"); //$NON-NLS-1$
    fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    setDescription(Activator.getString("JJNewJJPage.This_wizard_creates_a_new_file")); //$NON-NLS-1$
    setTitle(Activator.getString("JJNewJJPage.New_javacc_or_jtb_file")); //$NON-NLS-1$
  }

  /**
   * Initializes the fields.
   * 
   * @param aSelection used to initialize the fields
   */
  public void init(final IStructuredSelection aSelection) {
    final IJavaElement javaElem = getInitialJavaElement(aSelection);
    fPackage = ""; //$NON-NLS-1$
    fSrcRoot = ""; //$NON-NLS-1$
    if (javaElem != null) {
      // initialize package name
      fPackageFragment = (IPackageFragment) javaElem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      if (fPackageFragment != null && !fPackageFragment.isDefaultPackage()) {
        fPackage = fPackageFragment.getElementName();
      }
      // initialize fSrcRoot
      final IPackageFragmentRoot pfr = (IPackageFragmentRoot) javaElem
                                                                      .getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      if (pfr != null) {
        fSrcRoot = pfr.getPath().toString();
        if (fSrcRoot.startsWith("/")) {
          fSrcRoot = fSrcRoot.substring(1);
        }
      }
    }
    // initialize extension
    fExtension = ".jj"; //$NON-NLS-1$
    // initialize static flag
    fStaticFlag = true;
    // initialize filename
    fFileName = Activator.getString("JJNewJJPage.New_file"); //$NON-NLS-1$
  }

  /**
   * @see WizardPage#createControl
   * @param aParent the parent
   */
  public void createControl(final Composite aParent) {
    final Composite topLevel = new Composite(aParent, SWT.NONE);
    topLevel.setFont(aParent.getFont());
    final GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.verticalSpacing = 9;
    topLevel.setLayout(layout);

    // first: the source folder where to create the file
    Label label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Folder")); //$NON-NLS-1$
    // the input field
    fSrcRootText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    fSrcRootText.setLayoutData(gd);
    fSrcRootText.setText(fSrcRoot);
    fSrcRootText.addModifyListener(new ModifyListener() {

      public void modifyText(@SuppressWarnings("unused") final ModifyEvent event) {
        fSrcRootStatus = sourceContainerChanged();
        if (fSrcRootStatus.isOK()) {
          packageChanged(); // Revalidates package
        }
        updateStatus();
      }
    });
    // the browse button
    Button button = new Button(topLevel, SWT.PUSH);
    button.setText(Activator.getString("JJNewJJPage.Browse")); //$NON-NLS-1$
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final IPackageFragmentRoot root = chooseSourceContainer();
        if (root != null) {
          fSrcRootFragment = root;
          fSrcRoot = root.getPath().makeRelative().toString();
          fSrcRootText.setText(fSrcRoot);
          fSrcRootStatus = sourceContainerChanged();
          updateStatus();
        }
      }
    });

    // second: the package name
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Package_name")); //$NON-NLS-1$
    // the input field
    fPackageText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    fPackageText.setLayoutData(gd);
    fPackageText.setText(fPackage);
    fPackageText.addModifyListener(new ModifyListener() {

      public void modifyText(@SuppressWarnings("unused") final ModifyEvent e) {
        fPackageStatus = packageChanged();
        updateStatus();
      }
    });
    // the browse button
    button = new Button(topLevel, SWT.PUSH);
    button.setText(Activator.getString("JJNewJJPage.Browse")); //$NON-NLS-1$
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final IPackageFragment pack = choosePackage();
        if (pack != null) {
          final String str = pack.getElementName();
          fPackageText.setText(str);
          fPackageStatus = packageChanged();
          updateStatus();
        }
      }
    });
    // third: three check box for .jj, .jjt, .jtb
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Choose_type")); //$NON-NLS-1$

    final Composite group1 = new Composite(topLevel, SWT.NONE);
    group1.setLayoutData(new GridData());
    final GridLayout gl1 = new GridLayout();
    gl1.numColumns = 3;
    group1.setLayout(gl1);

    // the radio button listener
    final SelectionAdapter listener1 = new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent event) {
        fExtension = (String) event.widget.getData();
        extensionChanged();
        updateStatus();
      }
    };
    // .jj
    Button radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(Activator.getString("JJNewJJPage.JJ_file")); //$NON-NLS-1$
    radio1.setData(".jj"); //$NON-NLS-1$
    radio1.setSelection(true);
    radio1.addSelectionListener(listener1);
    // .jjt
    radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(Activator.getString("JJNewJJPage.JJT_file")); //$NON-NLS-1$
    radio1.setData(".jjt"); //$NON-NLS-1$
    radio1.setSelection(false);
    radio1.addSelectionListener(listener1);
    // .jtb
    radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(Activator.getString("JJNewJJPage.JTB_File")); //$NON-NLS-1$
    radio1.setData(".jtb"); //$NON-NLS-1$
    radio1.addSelectionListener(listener1);
    new Label(topLevel, SWT.NULL); // to fill the line

    // fourth: two check box for static / non static
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Choose_flag")); //$NON-NLS-1$

    final Composite group2 = new Composite(topLevel, SWT.NONE);
    group2.setLayoutData(new GridData());
    final GridLayout gl2 = new GridLayout();
    gl2.numColumns = 2;
    group2.setLayout(gl2);

    // the radio button listener
    final SelectionAdapter listener2 = new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent event) {
        fStaticFlag = "true".equals(event.widget.getData()); //$NON-NLS-1$
        updateStatus();
      }
    };
    // static
    Button radio2 = new Button(group2, SWT.RADIO);
    radio2.setText(Activator.getString("JJNewJJPage.Static_flag")); //$NON-NLS-1$
    radio2.setData("true"); //$NON-NLS-1$
    radio2.setSelection(true);
    radio2.addSelectionListener(listener2);
    // non static
    radio2 = new Button(group2, SWT.RADIO);
    radio2.setText(Activator.getString("JJNewJJPage.Non_static_flag")); //$NON-NLS-1$
    radio2.setData("false"); //$NON-NLS-1$
    radio2.setSelection(false);
    radio2.addSelectionListener(listener2);
    new Label(topLevel, SWT.NULL); // to fill the line

    // fifth: the file name
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.File_name")); //$NON-NLS-1$
    // The input field
    fFileNameText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    fFileNameText.setText(fFileName);
    gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    fFileNameText.setLayoutData(gd);
    fFileNameText.addModifyListener(new ModifyListener() {

      public void modifyText(@SuppressWarnings("unused") final ModifyEvent event) {
        fFileStatus = fileNameChanged();
        updateStatus();
      }
    });
    label = new Label(topLevel, SWT.NULL); // to fill the line

    // finish
    setControl(topLevel);

    // verify that all this is OK
    fSrcRootStatus = sourceContainerChanged();
    fPackageStatus = packageChanged();
    fFileStatus = fileNameChanged();
    if (fSrcRootStatus.getSeverity() == IStatus.ERROR || fPackageStatus.getSeverity() == IStatus.ERROR
        || fFileStatus.getSeverity() == IStatus.ERROR) {
      setPageComplete(false);
    }

    // help context
    PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel, "JJNewJJPage"); //$NON-NLS-1$
  }

  /**
   * Verifies the input for the Source container field.
   * 
   * @return the status
   */
  IStatus sourceContainerChanged() {
    final Status status = new Status();

    fSrcRootFragment = null;
    final String str = fSrcRootText.getText();
    if (str.length() == 0) {
      status.setError(Activator.getString("JJNewJJPage.Folder_name_is_empty")); //$NON-NLS-1$
      return status;
    }
    final IPath path = new Path(str);
    final IResource res = fWorkspaceRoot.findMember(path);
    if (res != null) {
      final int resType = res.getType();
      if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
        final IProject proj = res.getProject();
        if (!proj.isOpen()) {
          status.setError(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_error_ProjectClosed,
                                               new Object[] {
                                                 proj.getFullPath().toString() }));
          return status;
        }
        final IJavaProject jproject = JavaCore.create(proj);
        fSrcRootFragment = jproject.getPackageFragmentRoot(res);
        if (res.exists()) {
          try {
            if (!proj.hasNature(JavaCore.NATURE_ID)) {
              if (resType == IResource.PROJECT) {
                status.setError(NewWizardMessages.NewContainerWizardPage_warning_NotAJavaProject);
              }
              else {
                status.setWarning(NewWizardMessages.NewContainerWizardPage_warning_NotInAJavaProject);
              }
              return status;
            }
            if (fSrcRootFragment.isArchive()) {
              status
                    .setError(MessageFormat
                                           .format(
                                                   NewWizardMessages.NewContainerWizardPage_error_ContainerIsBinary,
                                                   new Object[] {
                                                     str }));
              return status;
            }
            if (fSrcRootFragment.getKind() == IPackageFragmentRoot.K_BINARY) {
              status
                    .setWarning(MessageFormat
                                             .format(
                                                     NewWizardMessages.NewContainerWizardPage_warning_inside_classfolder,
                                                     new Object[] {
                                                       str }));
            }
            else if (!jproject.isOnClasspath(fSrcRootFragment)) {
              status
                    .setWarning(MessageFormat
                                             .format(
                                                     NewWizardMessages.NewContainerWizardPage_warning_NotOnClassPath,
                                                     new Object[] {
                                                       str }));
            }
          } catch (final CoreException e) {
            status.setWarning(NewWizardMessages.NewContainerWizardPage_warning_NotAJavaProject);
          }
        }
        return status;
      }
      else {
        status.setError(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_error_NotAFolder,
                                             new Object[] {
                                               str }));
        return status;
      }
    }
    else {
      status
            .setError(MessageFormat
                                   .format(
                                           NewWizardMessages.NewContainerWizardPage_error_ContainerDoesNotExist,
                                           new Object[] {
                                             str }));
      return status;
    }
  }

  /**
   * Verifies the input for the package field.
   * 
   * @return the status
   */
  IStatus packageChanged() {
    final Status status = new Status();
    final String packName = getPackage();

    if (packName.length() > 0) {
      final IStatus val = JavaConventions.validatePackageName(packName, CompilerOptions.VERSION_1_5,
                                                              CompilerOptions.VERSION_1_5); // For Eclipse 3.4
      // IStatus val = JavaConventions.validatePackageName(packName,null, null); // Eclipse 3.3
      // IStatus val = JavaConventions.validatePackageName(packName,); // Keep for Eclipse 3.2
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(MessageFormat.format(NewWizardMessages.NewPackageWizardPage_error_InvalidPackageName,
                                             new Object[] {
                                               val.getMessage() }));
        return status;
      }
      else if (val.getSeverity() == IStatus.WARNING) {
        status
              .setWarning(MessageFormat
                                       .format(
                                               NewWizardMessages.NewPackageWizardPage_warning_DiscouragedPackageName,
                                               new Object[] {
                                                 val.getMessage() }));
      }
    }

    final IPackageFragmentRoot root = fSrcRootFragment;
    if (root != null && root.getJavaProject().exists()) {
      final IPackageFragment pack = root.getPackageFragment(packName);
      try {
        final IPath rootPath = root.getPath();
        final IPath outputPath = root.getJavaProject().getOutputLocation();
        if (rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath)) {
          // if the bin folder is inside of our root, don't allow to name a package like the bin folder
          final IPath packagePath = pack.getPath();
          if (outputPath.isPrefixOf(packagePath)) {
            status.setError(NewWizardMessages.NewPackageWizardPage_error_IsOutputFolder);
            return status;
          }
        }
        if (!pack.exists()) {
          status
                .setError(Activator.getString("JJNewJJPage.The_package") + " " + pack.getElementName() + " " + Activator.getString(Activator.getString("JJNewJJPage._does_not_exist"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
      } catch (final JavaModelException e) {
        Activator.logErr(e.getMessage());
      }
    }
    return status;
  }

  /**
   * Handles the extension change.
   */
  void extensionChanged() {
    String fileName = getFileName();
    int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc == -1) {
      dotLoc = fileName.length();
    }
    fileName = fileName.substring(0, dotLoc) + fExtension;
    fFileNameText.setText(fileName);
    return;
  }

  /**
   * Verifies the input for the file name field.
   * 
   * @return the status
   */
  IStatus fileNameChanged() {
    String fileName = getFileName();
    if (fileName.length() == 0) {
      // if no filename
      final Status status = new Status();
      status.setError(Activator.getString("JJNewJJPage.File_name_must_be_specified")); //$NON-NLS-1$
      return status;
    }
    final int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc != -1) {
      final String ext = fileName.substring(dotLoc + 1);
      if (ext.equalsIgnoreCase("jj") == false //$NON-NLS-1$
          && ext.equalsIgnoreCase("jjt") == false //$NON-NLS-1$
          && ext.equalsIgnoreCase("jtb") == false) { //$NON-NLS-1$
        // if filename with the wrong extension
        final Status status = new Status();
        status.setError(Activator.getString("JJNewJJPage.File_extension_must_be_jj")); //$NON-NLS-1$
        return status;
      }
      fileName = fileName.substring(0, dotLoc);
    }
    // in the end validate using JavaConventions
    return (JavaConventions.validateIdentifier(fileName, CompilerOptions.VERSION_1_5,
                                               CompilerOptions.VERSION_1_5));
  }

  /**
   * Updates the status line and the OK button according to the current statuses.
   */
  protected void updateStatus() {
    final IStatus status = StatusUtil.getMostSevere(new IStatus[] {
        fSrcRootStatus, fPackageStatus, fFileStatus });
    setPageComplete(!status.matches(IStatus.ERROR));
    StatusUtil.applyToStatusLine(this, status);
  }

  /**
   * @return the directory input field
   */
  public String getSrcDir() {
    return fSrcRoot;
  }

  /**
   * @return the content of the package input field
   */
  public String getPackage() {
    return fPackageText.getText();
  }

  /**
   * @return the static flag
   */
  public boolean getStaticFalg() {
    return fStaticFlag;
  }

  /**
   * @return the content of the file input field
   */
  public String getFileName() {
    return fFileNameText.getText();
  }

  /**
   * @return the filename without extension
   */
  public String getFileNameWithoutExtension() {
    String fileName = getFileName();
    final int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc != -1) {
      fileName = fileName.substring(0, dotLoc);
    }
    return fileName;
  }

  /**
   * @return the extension from the file
   */
  public String getExtension() {
    final String fileName = getFileName();
    final int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc != -1) {
      final String ext = fileName.substring(dotLoc);
      return ext;
    }
    return fExtension;
  }

  /**
   * Opens a dialog to let the user choose a source container.
   * 
   * @return the source container
   */
  IPackageFragmentRoot chooseSourceContainer() {
    Class<?>[] acceptedClasses = new Class[] {
        IPackageFragmentRoot.class, IJavaProject.class };
    final TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses,
                                                                                        false) {

      @Override
      public boolean isSelectedValid(final Object element) {
        try {
          if (element instanceof IJavaProject) {
            final IJavaProject jproject = (IJavaProject) element;
            final IPath path = jproject.getProject().getFullPath();
            return (jproject.findPackageFragmentRoot(path) != null);
          }
          else if (element instanceof IPackageFragmentRoot) {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          }
          return true;
        } catch (final JavaModelException e) {
          Activator.logErr(e.getStatus().getMessage());
        }
        return false;
      }
    };

    acceptedClasses = new Class[] {
        IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
    final ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {

      @Override
      public boolean select(final Viewer viewer, final Object parent, final Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (final JavaModelException e) {
            Activator.logErr(e.getStatus().getMessage());
            return false;
          }
        }
        return super.select(viewer, parent, element);
      }
    };

    final StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
    final ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider,
                                                                             provider);
    dialog.setValidator(validator);
    //    dialog.setSorter(new JavaElementSorter());
    dialog.setComparator(new JavaElementComparator());
    dialog.setTitle(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
    dialog.setMessage(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
    dialog.addFilter(filter);
    dialog.setInput(JavaCore.create(fWorkspaceRoot));
    dialog.setInitialSelection("dummy"); //$NON-NLS-1$

    if (dialog.open() == Window.OK) {
      final Object element = dialog.getFirstResult();
      if (element instanceof IJavaProject) {
        final IJavaProject jproject = (IJavaProject) element;
        return jproject.getPackageFragmentRoot(jproject.getProject());
      }
      else if (element instanceof IPackageFragmentRoot) {
        return (IPackageFragmentRoot) element;
      }
      return null;
    }
    return null;
  }

  /**
   * Opens a dialog to let the user choose a package.
   * 
   * @return the package
   */
  IPackageFragment choosePackage() {
    final IPackageFragmentRoot froot = fSrcRootFragment;
    IJavaElement[] packages = null;
    try {
      if (froot != null && froot.exists()) {
        packages = froot.getChildren();
      }
    } catch (final JavaModelException e) {
      Activator.logErr(e.getMessage());
    }
    if (packages == null) {
      packages = new IJavaElement[0];
    }

    final ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                                                                             getShell(),
                                                                             new JavaElementLabelProvider(
                                                                                                          JavaElementLabelProvider.SHOW_DEFAULT));
    dialog.setIgnoreCase(false);
    dialog.setTitle(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_title);
    dialog.setMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_description);
    dialog.setEmptyListMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_empty);
    dialog.setElements(packages);
    if (fPackageFragment != null) {
      dialog.setInitialSelections(new Object[] {
        fPackageFragment });
    }

    if (dialog.open() == Window.OK) {
      return (IPackageFragment) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Inspects a selection to find a Java element.
   * 
   * @param aSelection the selection to be inspected
   * @return a Java element to be used as the initial selection, or <code>null</code>, if no Java element
   *         exists in the given selection
   */
  protected IJavaElement getInitialJavaElement(final IStructuredSelection aSelection) {
    IJavaElement javaElem = null;
    if (aSelection != null && !aSelection.isEmpty()) {
      final Object selectedElement = aSelection.getFirstElement();
      if (selectedElement instanceof IAdaptable) {
        final IAdaptable adaptable = (IAdaptable) selectedElement;

        javaElem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
        if (javaElem == null) {
          IResource resource = (IResource) adaptable.getAdapter(IResource.class);
          if (resource != null && resource.getType() != IResource.ROOT) {
            while (javaElem == null && resource.getType() != IResource.PROJECT) {
              resource = resource.getParent();
              javaElem = (IJavaElement) resource.getAdapter(IJavaElement.class);
            }
            if (javaElem == null) {
              javaElem = JavaCore.create(resource); // java project
            }
          }
        }
      }
    }
    if (javaElem == null) {
      IWorkbenchPart part = org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage().getActivePart();
      if (part instanceof ContentOutline) {
        part = org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage().getActiveEditor();
      }

      if (part instanceof org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider) {
        final Object elem = ((org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider) part)
                                                                                                   .getViewPartInput();
        if (elem instanceof IJavaElement) {
          javaElem = (IJavaElement) elem;
        }
      }
    }

    if (javaElem == null || javaElem.getElementType() == IJavaElement.JAVA_MODEL) {
      try {
        final IJavaProject[] projects = JavaCore.create(fWorkspaceRoot).getJavaProjects();
        if (projects.length == 1) {
          javaElem = projects[0];
        }
      } catch (final JavaModelException e) {
        Activator.logErr(e.getMessage());
      }
    }
    return javaElem;
  }
}
