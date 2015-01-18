package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;

import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Compiler for .jj, .jjt and .jtb files for normal usage (ie non headless builds).<br>
 * It is used by the build process and by the compile commands.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.core.resources.builders">.<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class Builder extends IncrementalProjectBuilder implements IResourceDeltaVisitor, IResourceVisitor {

  // MMa 01/2015 : moved previous non IncrementalProjectBuilder methods to new Compiler class
  //               changed initialization location

  /** The associated compiler */
  private Compiler            compiler      = null;

  /** The java project */
  private IJavaProject        jJavaProject  = null;

  /** The project output folder */
  private IPath               jOutputFolder = null;

  /** The project's JavaCC preferences */
  private IEclipsePreferences jPrefs        = null;

  //  /** Standard constructor */
  //  public Compiler() {
  //  }

  /** @Inheritdoc */
  @Override
  public void startupOnInitialize() {
    super.startupOnInitialize();
    final IProject project = getProject();
    compiler = new Compiler(project);
    jJavaProject = JavaCore.create(project);
    jPrefs = new ProjectScope(project).getNode(PLUGIN_QN);
    try {
      jOutputFolder = jJavaProject.getOutputLocation().removeFirstSegments(1);
    } catch (final JavaModelException e) {
      AbstractActivator.logBug(e, project.getLocation().segments());
    }
  }

  /**
   * Invoked in response to a call to one of the <code>IProject.build</code>.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected IProject[] build(final int aKind, @SuppressWarnings({
    "rawtypes" }) final Map aArgs, final IProgressMonitor aMonitor) throws CoreException {
    if (aKind == IncrementalProjectBuilder.FULL_BUILD) {
      fullBuild(aMonitor);
    }
    else if (aKind == IncrementalProjectBuilder.INCREMENTAL_BUILD
             || aKind == IncrementalProjectBuilder.AUTO_BUILD) {
      incrementalBuild(aMonitor);
    }
    else if (aKind == IncrementalProjectBuilder.CLEAN_BUILD) {
      clean(aMonitor);
    }
    // refresh the whole project
    getProject().refreshLocal(IResource.DEPTH_INFINITE, aMonitor);
    return null;
  }

  /**
   * Performs a full build.
   * 
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void fullBuild(@SuppressWarnings("unused") final IProgressMonitor aMonitor) throws CoreException {
    getProject().accept(this);
  }

  /**
   * Performs an incremental build or a full build if no delta is available.
   * 
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void incrementalBuild(final IProgressMonitor aMonitor) throws CoreException {
    final IResourceDelta delta = getDelta(getProject());
    if (delta != null) {
      delta.accept(this);
    }
    else {
      fullBuild(aMonitor);
    }
  }

  /**
   * Cleans all generated files.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void clean(final IProgressMonitor aMonitor) throws CoreException {
    super.clean(aMonitor);
    final IResource[] members = getProject().members();
    clean(members, aMonitor);
  }

  /**
   * Deletes recursively generated and still derived files. A modified generated file, marked as not derived,
   * shall not be deleted.
   * 
   * @param aMembers - the resources to delete
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void clean(final IResource[] aMembers, final IProgressMonitor aMonitor) throws CoreException {
    final int updFlag = getKeepDelFilesInHistory() ? IResource.KEEP_HISTORY : 0;
    for (final IResource res : aMembers) {
      if (res.getType() == IResource.FOLDER) {
        clean(((IFolder) res).members(), aMonitor);
      }
      else if (res.isDerived() && res.getPersistentProperty(GEN_FILE_QN) != null) {
        res.delete(updFlag, aMonitor);
      }
      else {
        // normally a .jj/.jjt/.jtb file
        res.deleteMarkers(JJ_MARKER, false, IResource.DEPTH_ZERO);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean visit(final IResourceDelta aDelta) throws CoreException {
    return visit(aDelta.getResource());
  }

  /** {@inheritDoc} */
  @Override
  public boolean visit(final IResource aRes) throws CoreException {
    if (aRes == null) {
      return false;
    }
    final String ext = aRes.getFileExtension();
    // test not a java file first as it is the most frequent case
    final boolean okToCompile = !"java".equals(ext) //$NON-NLS-1$
                                && ("jj".equals(ext) || "jjt".equals(ext) || "jtb".equals(ext)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                && jJavaProject.isOnClasspath(aRes);
    if (okToCompile) {
      compiler.compileResource(aRes, false);
    }
    // this prevents traversing output directories
    if (jOutputFolder == null) {
      AbstractActivator.logErr("Java project output folder is null"); //$NON-NLS-1$
      return false;
    }
    final boolean isOut = aRes.getProjectRelativePath().equals(jOutputFolder)
                          & jOutputFolder.toString().length() != 0;
    return !isOut;
  }

  /**
   * Retrieves the "keep deleted files from history" flag (from the preferences).
   * 
   * @return the flag
   */
  boolean getKeepDelFilesInHistory() {
    String flag = ""; //$NON-NLS-1$
    try {
      flag = jPrefs.get(KEEP_DEL_FILES_IN_HIST, DEF_KEEP_DEL_FILES_IN_HIST);
    } catch (final Exception e) {
      AbstractActivator.logBug(e);
    }
    return "true".equals(flag); //$NON-NLS-1$
  }

}
