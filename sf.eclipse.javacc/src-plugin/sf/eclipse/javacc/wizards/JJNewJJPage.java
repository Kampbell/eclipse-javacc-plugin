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
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
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

/**
 * The "New" wizard page allows setting the srcdir, the package for the new file, the extension, as well as
 * the file name. This page handle the creation of the file.
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJNewJJPage extends WizardPage {

  /*
   * MMa 11/09 : javadoc and formatting revision ; changed some modifiers for synthetic accesses
   */

  IPackageFragmentRoot         fSrcRootFragment;
  private IPackageFragment     fPackageFragment;
  private final IWorkspaceRoot fWorkspaceRoot;

  Text                         fSrcRootText;
  Text                         fPackageText;
  private Text                 fFileNameText;

  protected IStatus            fSrcRootStatus;
  protected IStatus            fPackageStatus;
  protected IStatus            fExtensionStatus;
  protected IStatus            fFileStatus;

  String                       fSrcRoot;
  private String               fPackage;
  private String               fFileName;
  String                       fExtension;

  /**
   * Creates a new <code>NewJJWizardPage</code>
   */
  public JJNewJJPage() {
    super("NewJJWizardPage"); //$NON-NLS-1$
    fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    setDescription(Activator.getString("JJNewJJPage.This_wizard_creates_a_new_file")); //$NON-NLS-1$
    setTitle(Activator.getString("JJNewJJPage.New_javacc_or_jtb_file")); //$NON-NLS-1$
  }

  /**
   * The selection is used to initialize the fields.
   * 
   * @param aSelection used to initialize the fields
   */
  public void init(final IStructuredSelection aSelection) {
    final IJavaElement jelem = getInitialJavaElement(aSelection);
    fPackage = ""; //$NON-NLS-1$
    fSrcRoot = ""; //$NON-NLS-1$
    if (jelem != null) {
      // init package name
      fPackageFragment = (IPackageFragment) jelem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      if (fPackageFragment != null && !fPackageFragment.isDefaultPackage()) {
        fPackage = fPackageFragment.getElementName();
      }
      // init SrcRoot
      final IPackageFragmentRoot pfr = (IPackageFragmentRoot) jelem
                                                                   .getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      if (pfr != null) {
        fSrcRoot = pfr.getPath().toString();
      }
    }
    // init extension
    fExtension = ".jj"; //$NON-NLS-1$
    // init filename
    fFileName = Activator.getString("JJNewJJPage.New_file"); //$NON-NLS-1$
  }

  /**
   * @param aParent the parent
   * @see WizardPage#createControl
   */
  public void createControl(final Composite aParent) {
    final Composite topLevel = new Composite(aParent, SWT.NONE);
    topLevel.setFont(aParent.getFont());
    final GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.verticalSpacing = 9;
    topLevel.setLayout(layout);

    // First: the source folder where to create the file
    Label label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Folder")); //$NON-NLS-1$
    // the input field
    fSrcRootText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    fSrcRootText.setLayoutData(gd);
    fSrcRootText.setText(fSrcRoot);
    fSrcRootText.addModifyListener(new ModifyListener() {

      public void modifyText(@SuppressWarnings("unused") final ModifyEvent e) {
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
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent e) {
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

    // Second: the package name
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Package_name")); //$NON-NLS-1$
    // The input field
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
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent e) {
        final IPackageFragment pack = choosePackage();
        if (pack != null) {
          final String str = pack.getElementName();
          fPackageText.setText(str);
          fPackageStatus = packageChanged();
          updateStatus();
        }
      }
    });
    // Third: three check box for .jj, .jjt, .jtb
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.Choose_type")); //$NON-NLS-1$

    final Composite group = new Composite(topLevel, SWT.NONE);
    group.setLayoutData(new GridData());
    final GridLayout gd1 = new GridLayout();
    gd1.numColumns = 3;
    group.setLayout(gd1);

    // The radio button listener
    final SelectionAdapter listener = new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent event) {
        fExtension = (String) event.widget.getData();
        fExtensionStatus = extensionChanged();
        updateStatus();
      }
    };
    // .jj
    Button radio = new Button(group, SWT.RADIO);
    radio.setText(Activator.getString("JJNewJJPage.JJ_file")); //$NON-NLS-1$
    radio.setData(".jj"); //$NON-NLS-1$
    radio.setSelection(true);
    radio.addSelectionListener(listener);
    // .jjt
    radio = new Button(group, SWT.RADIO);
    radio.setText(Activator.getString("JJNewJJPage.JJT_file")); //$NON-NLS-1$
    radio.setData(".jjt"); //$NON-NLS-1$
    radio.setSelection(false);
    radio.addSelectionListener(listener);
    // .jtb
    radio = new Button(group, SWT.RADIO);
    radio.setText(Activator.getString("JJNewJJPage.JTB_File")); //$NON-NLS-1$
    radio.setData(".jtb"); //$NON-NLS-1$
    radio.addSelectionListener(listener);
    new Label(topLevel, SWT.NULL); // to fill the line

    // Fourth: the file name
    label = new Label(topLevel, SWT.NULL);
    label.setText(Activator.getString("JJNewJJPage.File_name")); //$NON-NLS-1$
    // The input field
    fFileNameText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    fFileNameText.setText(fFileName);
    gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    fFileNameText.setLayoutData(gd);
    fFileNameText.addModifyListener(new ModifyListener() {

      public void modifyText(@SuppressWarnings("unused") final ModifyEvent e) {
        fFileStatus = fileNameChanged();
        updateStatus();
      }
    });
    label = new Label(topLevel, SWT.NULL); // to fill the line

    // Finish
    setControl(topLevel);

    // Verifies that all this is OK
    fSrcRootStatus = sourceContainerChanged();
    fPackageStatus = packageChanged();
    fFileStatus = fileNameChanged();
    fExtensionStatus = new Status();
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
  @SuppressWarnings("restriction")
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
          status
                .setError(MessageFormat
                                       .format(
                                               org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_error_ProjectClosed,
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
                status
                      .setError(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_warning_NotAJavaProject);
              }
              else {
                status
                      .setWarning(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_warning_NotInAJavaProject);
              }
              return status;
            }
            if (fSrcRootFragment.isArchive()) {
              status
                    .setError(MessageFormat
                                           .format(
                                                   org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_error_ContainerIsBinary,
                                                   new Object[] {
                                                     str }));
              return status;
            }
            if (fSrcRootFragment.getKind() == IPackageFragmentRoot.K_BINARY) {
              status
                    .setWarning(MessageFormat
                                             .format(
                                                     org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_warning_inside_classfolder,
                                                     new Object[] {
                                                       str }));
            }
            else if (!jproject.isOnClasspath(fSrcRootFragment)) {
              status
                    .setWarning(MessageFormat
                                             .format(
                                                     org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_warning_NotOnClassPath,
                                                     new Object[] {
                                                       str }));
            }
          } catch (final CoreException e) {
            status
                  .setWarning(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_warning_NotAJavaProject);
          }
        }
        return status;
      }
      else {
        status
              .setError(MessageFormat
                                     .format(
                                             org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_error_NotAFolder,
                                             new Object[] {
                                               str }));
        return status;
      }
    }
    else {
      status
            .setError(MessageFormat
                                   .format(
                                           org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_error_ContainerDoesNotExist,
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
  @SuppressWarnings( {
      "restriction", "deprecation" })
  IStatus packageChanged() {
    final Status status = new Status();
    final String packName = getPackage();

    if (packName.length() > 0) {
      // IStatus val = JavaConventions.validatePackageName(packName,null, null); // Eclipse 3.3
      final IStatus val = JavaConventions.validatePackageName(packName); // Keep for Eclipse 3.2
      if (val.getSeverity() == IStatus.ERROR) {
        status
              .setError(MessageFormat
                                     .format(
                                             org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewPackageWizardPage_error_InvalidPackageName,
                                             new Object[] {
                                               val.getMessage() }));
        return status;
      }
      else if (val.getSeverity() == IStatus.WARNING) {
        status
              .setWarning(MessageFormat
                                       .format(
                                               org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewPackageWizardPage_warning_DiscouragedPackageName,
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
          // if the bin folder is inside of our root, don't allow to name a
          // package like the bin folder
          final IPath packagePath = pack.getPath();
          if (outputPath.isPrefixOf(packagePath)) {
            status
                  .setError(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewPackageWizardPage_error_IsOutputFolder);
            return status;
          }
        }
        if (!pack.exists()) {
          status
                .setError(Activator.getString("JJNewJJPage.the_package") + " " + pack.getElementName() + " " + Activator.getString(Activator.getString("JJNewJJPage._does_not_exist"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
      } catch (final JavaModelException e) {
        Activator.log(e.toString());
      }
    }
    return status;
  }

  /**
   * Handles extension change.
   * 
   * @return the status
   */
  IStatus extensionChanged() {
    String fileName = getFileName();
    int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc == -1) {
      dotLoc = fileName.length();
    }
    fileName = fileName.substring(0, dotLoc) + fExtension;
    fFileNameText.setText(fileName);
    // Does not really matter
    final Status status = new Status();
    return status;
  }

  /**
   * Verifies the input for the filename field.
   * 
   * @return the status
   */
  @SuppressWarnings("deprecation")
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
    return (JavaConventions.validateIdentifier(fileName));
  }

  /**
   * Updates the status line and the OK button according to the given status.
   */
  @SuppressWarnings("restriction")
  protected void updateStatus() {
    final IStatus status = org.eclipse.jdt.internal.ui.dialogs.StatusUtil.getMostSevere(new IStatus[] {
        fSrcRootStatus, fPackageStatus, fExtensionStatus, fFileStatus });
    setPageComplete(!status.matches(IStatus.ERROR));
    org.eclipse.jdt.internal.ui.dialogs.StatusUtil.applyToStatusLine(this, status);
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
   * Opens a dialog to let user choose a source container.
   * 
   * @return the source container
   */
  @SuppressWarnings( {
      "restriction", "unchecked", "deprecation" })
  IPackageFragmentRoot chooseSourceContainer() {
    Class[] acceptedClasses = new Class[] {
        IPackageFragmentRoot.class, IJavaProject.class };
    final org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator validator = new org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator(
                                                                                                                                                                acceptedClasses,
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
          Activator.log(e.getStatus().getMessage());
        }
        return false;
      }
    };

    acceptedClasses = new Class[] {
        IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
    final ViewerFilter filter = new org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter(acceptedClasses) {

      @Override
      public boolean select(final Viewer viewer, final Object parent, final Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (final JavaModelException e) {
            Activator.log(e.getStatus().getMessage());
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
    dialog.setSorter(new JavaElementSorter());
    dialog
          .setTitle(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
    dialog
          .setMessage(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
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
   * Open a dialog to let user choose a package
   * 
   * @return the package
   */
  @SuppressWarnings("restriction")
  IPackageFragment choosePackage() {
    final IPackageFragmentRoot froot = fSrcRootFragment;
    IJavaElement[] packages = null;
    try {
      if (froot != null && froot.exists()) {
        packages = froot.getChildren();
      }
    } catch (final JavaModelException e) {
      Activator.log(e.getMessage());
    }
    if (packages == null) {
      packages = new IJavaElement[0];
    }

    final ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                                                                             getShell(),
                                                                             new JavaElementLabelProvider(
                                                                                                          JavaElementLabelProvider.SHOW_DEFAULT));
    dialog.setIgnoreCase(false);
    dialog
          .setTitle(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_title);
    dialog
          .setMessage(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_description);
    dialog
          .setEmptyListMessage(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_empty);
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
   * Utility method to inspect a selection to find a Java element.
   * 
   * @param selection the selection to be inspected
   * @return a Java element to be used as the initial selection, or <code>null</code>, if no Java element
   *         exists in the given selection
   */
  @SuppressWarnings("restriction")
  protected IJavaElement getInitialJavaElement(final IStructuredSelection selection) {
    IJavaElement jelem = null;
    if (selection != null && !selection.isEmpty()) {
      final Object selectedElement = selection.getFirstElement();
      if (selectedElement instanceof IAdaptable) {
        final IAdaptable adaptable = (IAdaptable) selectedElement;

        jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
        if (jelem == null) {
          IResource resource = (IResource) adaptable.getAdapter(IResource.class);
          if (resource != null && resource.getType() != IResource.ROOT) {
            while (jelem == null && resource.getType() != IResource.PROJECT) {
              resource = resource.getParent();
              jelem = (IJavaElement) resource.getAdapter(IJavaElement.class);
            }
            if (jelem == null) {
              jelem = JavaCore.create(resource); // java project
            }
          }
        }
      }
    }
    if (jelem == null) {
      IWorkbenchPart part = org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage().getActivePart();
      if (part instanceof ContentOutline) {
        part = org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage().getActiveEditor();
      }

      if (part instanceof org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider) {
        final Object elem = ((org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider) part)
                                                                                                   .getViewPartInput();
        if (elem instanceof IJavaElement) {
          jelem = (IJavaElement) elem;
        }
      }
    }

    if (jelem == null || jelem.getElementType() == IJavaElement.JAVA_MODEL) {
      try {
        final IJavaProject[] projects = JavaCore.create(fWorkspaceRoot).getJavaProjects();
        if (projects.length == 1) {
          jelem = projects[0];
        }
      } catch (final JavaModelException e) {
        Activator.log(e.getMessage());
      }
    }
    return jelem;
  }
}
