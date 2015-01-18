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
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider;
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

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * The "New" wizard page allows setting the source directory, the package for the new file, the extension, as
 * well as the file name. This page handles the file creation.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
@SuppressWarnings("restriction")
class NewGrammarWizardPage extends WizardPage {

  // MMa 11/2009 : javadoc and formatting revision ; changed some modifiers for synthetic accesses
  // MMa 02/2010 : formatting and javadoc revision ; differentiate static / non static files
  // MMa 08/2011 : fixed property error
  // BF  06/2012 : added NON-NLS tag, added unused tag and removed else to prevent warnings 
  // MMa 10/2012 : renamed
  // MMA 10/2014 : renamed

  /** The source directory */
  protected IPackageFragmentRoot jSrcRootFragment;
  /** The package */
  protected IPackageFragment     jPackageFragment;
  /** The workspace root directory returned by the plugin */
  protected final IWorkspaceRoot jWorkspaceRoot;
  /** The source input */
  protected Text                 jSrcRootText;
  /** The package input */
  protected Text                 jPackageText;
  /** The file name input */
  protected Text                 jFileNameText;
  /** The source change status */
  protected IStatus              jSrcRootStatus;
  /** The package change status */
  protected IStatus              jPackageStatus;
  /** The file name change status */
  protected IStatus              jFileStatus;
  /** The checked source */
  protected String               jSrcRoot;
  /** The checked package */
  protected String               jPackage;
  /** The checked file name */
  protected String               jFileName;
  /** The checked file extension */
  protected String               jExtension;
  /** The static / non static flag */
  protected boolean              jStaticFlag;

  /**
   * Creates a new <code>NewJJWizPage</code>.
   */
  public NewGrammarWizardPage() {
    super("NewJJWizPage"); //$NON-NLS-1$
    jWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    setDescription(AbstractActivator.getMsg("WizNewElem.Wizard_creates_a_new_file")); //$NON-NLS-1$
    setTitle(AbstractActivator.getMsg("WizNewElem.New_javacc_or_jtb_file")); //$NON-NLS-1$
  }

  /**
   * Initializes the fields.
   * 
   * @param aSelection - used to initialize the fields
   */
  public void init(final IStructuredSelection aSelection) {
    final IJavaElement javaElem = getInitialJavaElement(aSelection);
    //    jPackage = ""; //$NON-NLS-1$
    //    jSrcRoot = ""; //$NON-NLS-1$
    jPackage = AbstractActivator.getMsg("WizNewElem.New_package"); //$NON-NLS-1$
    jSrcRoot = AbstractActivator.getMsg("WizNewElem.New_root_folder"); //$NON-NLS-1$
    if (javaElem != null) {
      // initialize package name
      jPackageFragment = (IPackageFragment) javaElem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      if (jPackageFragment != null && !jPackageFragment.isDefaultPackage()) {
        jPackage = jPackageFragment.getElementName();
      }
      // initialize fSrcRoot
      final IPackageFragmentRoot pfr = (IPackageFragmentRoot) javaElem.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      if (pfr != null) {
        jSrcRoot = pfr.getPath().toString();
        if (jSrcRoot.startsWith("/")) { //$NON-NLS-1$
          jSrcRoot = jSrcRoot.substring(1);
        }
      }
    }
    // initialize extension
    jExtension = ".jj"; //$NON-NLS-1$
    // initialize static flag
    jStaticFlag = true;
    // initialize filename
    jFileName = AbstractActivator.getMsg("WizNewElem.New_file"); //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Override
  public void createControl(final Composite aParent) {
    final Composite topLevel = new Composite(aParent, SWT.NONE);
    topLevel.setFont(aParent.getFont());
    final GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.verticalSpacing = 9;
    topLevel.setLayout(layout);

    // first: the source folder where to create the file
    Label label = new Label(topLevel, SWT.NULL);
    label.setText(AbstractActivator.getMsg("WizNewElem.Folder")); //$NON-NLS-1$
    // the input field
    jSrcRootText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    jSrcRootText.setLayoutData(gd);
    jSrcRootText.setText(jSrcRoot);
    jSrcRootText.addModifyListener(new ModifyListener() {

      /** {@inheritDoc} */
      @Override
      public void modifyText(final ModifyEvent event) {
        jSrcRootStatus = sourceContainerChanged();
        if (jSrcRootStatus.isOK()) {
          packageChanged(); // Revalidates package
        }
        updateStatus();
      }
    });
    // the browse button
    Button button = new Button(topLevel, SWT.PUSH);
    button.setText(AbstractActivator.getMsg("WizNewElem.Browse")); //$NON-NLS-1$
    button.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final IPackageFragmentRoot root = chooseSourceContainer();
        if (root != null) {
          jSrcRootFragment = root;
          jSrcRoot = root.getPath().makeRelative().toString();
          jSrcRootText.setText(jSrcRoot);
          jSrcRootStatus = sourceContainerChanged();
          updateStatus();
        }
      }
    });

    // second: the package name
    label = new Label(topLevel, SWT.NULL);
    label.setText(AbstractActivator.getMsg("WizNewElem.Package_name")); //$NON-NLS-1$
    // the input field
    jPackageText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    jPackageText.setLayoutData(gd);
    jPackageText.setText(jPackage);
    jPackageText.addModifyListener(new ModifyListener() {

      /** {@inheritDoc} */
      @Override
      public void modifyText(final ModifyEvent e) {
        jPackageStatus = packageChanged();
        updateStatus();
      }
    });
    // the browse button
    button = new Button(topLevel, SWT.PUSH);
    button.setText(AbstractActivator.getMsg("WizNewElem.Browse")); //$NON-NLS-1$
    button.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final IPackageFragment pack = choosePackage();
        if (pack != null) {
          final String str = pack.getElementName();
          jPackageText.setText(str);
          jPackageStatus = packageChanged();
          updateStatus();
        }
      }
    });
    // third: three check box for .jj, .jjt, .jtb
    label = new Label(topLevel, SWT.NULL);
    label.setText(AbstractActivator.getMsg("WizNewElem.Choose_type")); //$NON-NLS-1$

    final Composite group1 = new Composite(topLevel, SWT.NONE);
    group1.setLayoutData(new GridData());
    final GridLayout gl1 = new GridLayout();
    gl1.numColumns = 3;
    group1.setLayout(gl1);

    // the radio button listener
    final SelectionAdapter listener1 = new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        jExtension = (String) event.widget.getData();
        extensionChanged();
        updateStatus();
      }
    };
    // .jj
    Button radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(AbstractActivator.getMsg("WizNewElem.JJ_file")); //$NON-NLS-1$
    radio1.setData(".jj"); //$NON-NLS-1$
    radio1.setSelection(true);
    radio1.addSelectionListener(listener1);
    // .jjt
    radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(AbstractActivator.getMsg("WizNewElem.JJT_file")); //$NON-NLS-1$
    radio1.setData(".jjt"); //$NON-NLS-1$
    radio1.setSelection(false);
    radio1.addSelectionListener(listener1);
    // .jtb
    radio1 = new Button(group1, SWT.RADIO);
    radio1.setText(AbstractActivator.getMsg("WizNewElem.JTB_File")); //$NON-NLS-1$
    radio1.setData(".jtb"); //$NON-NLS-1$
    radio1.addSelectionListener(listener1);
    new Label(topLevel, SWT.NULL); // to fill the line

    // fourth: two check box for static / non static
    label = new Label(topLevel, SWT.NULL);
    label.setText(AbstractActivator.getMsg("WizNewElem.Choose_flag")); //$NON-NLS-1$

    final Composite group2 = new Composite(topLevel, SWT.NONE);
    group2.setLayoutData(new GridData());
    final GridLayout gl2 = new GridLayout();
    gl2.numColumns = 2;
    group2.setLayout(gl2);

    // the radio button listener
    final SelectionAdapter listener2 = new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        jStaticFlag = "true".equals(event.widget.getData()); //$NON-NLS-1$
        updateStatus();
      }
    };
    // static
    Button radio2 = new Button(group2, SWT.RADIO);
    radio2.setText(AbstractActivator.getMsg("WizNewElem.Static_flag")); //$NON-NLS-1$
    radio2.setData("true"); //$NON-NLS-1$
    radio2.setSelection(true);
    radio2.addSelectionListener(listener2);
    // non static
    radio2 = new Button(group2, SWT.RADIO);
    radio2.setText(AbstractActivator.getMsg("WizNewElem.Non_static_flag")); //$NON-NLS-1$
    radio2.setData("false"); //$NON-NLS-1$
    radio2.setSelection(false);
    radio2.addSelectionListener(listener2);
    new Label(topLevel, SWT.NULL); // to fill the line

    // fifth: the file name
    label = new Label(topLevel, SWT.NULL);
    label.setText(AbstractActivator.getMsg("WizNewElem.File_name")); //$NON-NLS-1$
    // The input field
    jFileNameText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
    jFileNameText.setText(jFileName);
    gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    jFileNameText.setLayoutData(gd);
    jFileNameText.addModifyListener(new ModifyListener() {

      /** {@inheritDoc} */
      @Override
      public void modifyText(final ModifyEvent event) {
        jFileStatus = fileNameChanged();
        updateStatus();
      }
    });
    label = new Label(topLevel, SWT.NULL); // to fill the line

    // finish
    setControl(topLevel);

    // verify that all this is OK
    jSrcRootStatus = sourceContainerChanged();
    jPackageStatus = packageChanged();
    jFileStatus = fileNameChanged();
    if (jSrcRootStatus.getSeverity() == IStatus.ERROR || jPackageStatus.getSeverity() == IStatus.ERROR
        || jFileStatus.getSeverity() == IStatus.ERROR) {
      setPageComplete(false);
    }

    // help context
    PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel, "WizNewElem"); //$NON-NLS-1$
  }

  /**
   * Verifies the input for the Source container field.
   * 
   * @return the status
   */
  IStatus sourceContainerChanged() {
    final Status status = new Status();

    jSrcRootFragment = null;
    final String str = jSrcRootText.getText();
    if (str.length() == 0) {
      status.setError(AbstractActivator.getMsg("WizNewElem.Folder_name_is_empty")); //$NON-NLS-1$
      return status;
    }
    final IPath path = new Path(str);
    final IResource res = jWorkspaceRoot.findMember(path);
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
        jSrcRootFragment = jproject.getPackageFragmentRoot(res);
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
            if (jSrcRootFragment.isArchive()) {
              status.setError(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_error_ContainerIsBinary,
                                                   new Object[] {
                                                     str }));
              return status;
            }
            if (jSrcRootFragment.getKind() == IPackageFragmentRoot.K_BINARY) {
              status.setWarning(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_warning_inside_classfolder,
                                                     new Object[] {
                                                       str }));
            }
            else if (!jproject.isOnClasspath(jSrcRootFragment)) {
              status.setWarning(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_warning_NotOnClassPath,
                                                     new Object[] {
                                                       str }));
            }
          } catch (final CoreException e) {
            status.setWarning(NewWizardMessages.NewContainerWizardPage_warning_NotAJavaProject);
          }
        }
        return status;
      }
      status.setError(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_error_NotAFolder,
                                           new Object[] {
                                             str }));
      return status;
    }
    status.setError(MessageFormat.format(NewWizardMessages.NewContainerWizardPage_error_ContainerDoesNotExist,
                                         new Object[] {
                                           str }));
    return status;
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
      // IStatus val = JavaConventions.validatePackageName(packName); // Keep for Eclipse 3.2
      if (val.getSeverity() == IStatus.ERROR) {
        status.setError(MessageFormat.format(NewWizardMessages.NewPackageWizardPage_error_InvalidPackageName,
                                             new Object[] {
                                               val.getMessage() }));
        return status;
      }
      else if (val.getSeverity() == IStatus.WARNING) {
        status.setWarning(MessageFormat.format(NewWizardMessages.NewPackageWizardPage_warning_DiscouragedPackageName,
                                               new Object[] {
                                                 val.getMessage() }));
      }
    }

    final IPackageFragmentRoot root = jSrcRootFragment;
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
          status.setError(AbstractActivator.getMsg("WizNewElem.The_package") + " " + //$NON-NLS-1$ //$NON-NLS-2$
                          pack.getElementName() + " " + //$NON-NLS-1$
                          AbstractActivator.getMsg(AbstractActivator.getMsg("WizNewElem.does_not_exist"))); //$NON-NLS-1$
        }
      } catch (final JavaModelException e) {
        AbstractActivator.logBug(e);
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
    fileName = fileName.substring(0, dotLoc) + jExtension;
    jFileNameText.setText(fileName);
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
      status.setError(AbstractActivator.getMsg("WizNewElem.File_name_must_be_specified")); //$NON-NLS-1$
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
        status.setError(AbstractActivator.getMsg("WizNewElem.File_extension_must_be_jj")); //$NON-NLS-1$
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
        jSrcRootStatus, jPackageStatus, jFileStatus });
    setPageComplete(!status.matches(IStatus.ERROR));
    StatusUtil.applyToStatusLine(this, status);
  }

  /**
   * @return the directory input field
   */
  public String getSrcDir() {
    return jSrcRoot;
  }

  /**
   * @return the content of the package input field
   */
  public String getPackage() {
    return jPackageText.getText();
  }

  /**
   * @return the static flag
   */
  public boolean getStaticFalg() {
    return jStaticFlag;
  }

  /**
   * @return the content of the file input field
   */
  public String getFileName() {
    return jFileNameText.getText();
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
    return jExtension;
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

      /** {@inheritDoc} */
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
          AbstractActivator.logBug(e);
        }
        return false;
      }
    };

    acceptedClasses = new Class[] {
        IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
    final ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {

      /** {@inheritDoc} */
      @Override
      public boolean select(final Viewer viewer, final Object parent, final Object element) {
        if (element instanceof IPackageFragmentRoot) {
          try {
            return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
          } catch (final JavaModelException e) {
            AbstractActivator.logBug(e);
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
    dialog.setInput(JavaCore.create(jWorkspaceRoot));
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
    final IPackageFragmentRoot froot = jSrcRootFragment;
    IJavaElement[] packages = null;
    try {
      if (froot != null && froot.exists()) {
        packages = froot.getChildren();
      }
    } catch (final JavaModelException e) {
      AbstractActivator.logBug(e);
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
    if (jPackageFragment != null) {
      dialog.setInitialSelections(new Object[] {
        jPackageFragment });
    }

    if (dialog.open() == Window.OK) {
      return (IPackageFragment) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Inspects a selection to find a Java element.
   * 
   * @param aSelection - the selection to be inspected
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
      IWorkbenchPart part = JavaPlugin.getActivePage().getActivePart();
      if (part instanceof ContentOutline) {
        part = JavaPlugin.getActivePage().getActiveEditor();
      }

      if (part instanceof IViewPartInputProvider) {
        final Object elem = ((IViewPartInputProvider) part).getViewPartInput();
        if (elem instanceof IJavaElement) {
          javaElem = (IJavaElement) elem;
        }
      }
    }

    if (javaElem == null || javaElem.getElementType() == IJavaElement.JAVA_MODEL) {
      try {
        final IJavaProject[] projects = JavaCore.create(jWorkspaceRoot).getJavaProjects();
        if (projects.length == 1) {
          javaElem = projects[0];
        }
      } catch (final JavaModelException e) {
        AbstractActivator.logBug(e);
      }
    }
    return javaElem;
  }
}
