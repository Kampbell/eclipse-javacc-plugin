package sf.eclipse.javacc.handlers;

import static sf.eclipse.javacc.base.IConstants.DEF_KEEP_DEL_FILES_IN_HIST;
import static sf.eclipse.javacc.base.IConstants.GEN_FILE_QN;
import static sf.eclipse.javacc.base.IConstants.KEEP_DEL_FILES_IN_HIST;
import static sf.eclipse.javacc.base.IConstants.PLUGIN_QN;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.IConsole;

/**
 * Delete derived files handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28
 * @author Marc Mazas 2012-2013-2014
 */
public class DeleteDerivedFiles extends AbstractHandler {

  // MMa 11/2012 : created
  // MMa 11/2014 : tested and linked to the views through menus and tool bars
  //               modified some modifiers

  /** The project's JavaCC preferences */
  private IEclipsePreferences jPrefs = null;

  /** {@inheritDoc} */
  @Override
  public Object execute(final ExecutionEvent event) {
    // in which part were we called
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (!(part instanceof IViewPart)) {
      // not on a viewer, do nothing
      return null;
    }
    final ISelection sel = HandlerUtil.getCurrentSelection(event);
    IFolder parentFolder = null;
    if ((sel instanceof IResource)) {
      // case in the JJEditor
      // not implemented
      return null;
    }
    else if ((sel instanceof TreeSelection)) {
      // case for example in the PackageExplorerPar or ProjectExplorer parts with a file selected
      final TreePath[] tps = ((TreeSelection) sel).getPaths();
      if (tps.length == 0) {
        return null;
      }
      final TreePath tp = tps[0];
      // take the selected element (in plugin.xml we select only directories)
      final Object pf = tp.getSegment(tp.getSegmentCount() - 1);
      if (!(pf instanceof IPackageFragment)) {
        return null;
      }
      final IResource pfRes = ((IPackageFragment) pf).getResource();
      if (!(pfRes instanceof IFolder)) {
        return null;
      }
      parentFolder = (IFolder) pfRes;
    }
    if (parentFolder == null) {
      return null;
    }
    final int updFlag = getKeepDelFilesInHistory(parentFolder) ? IResource.KEEP_HISTORY : 0;
    final IConsole console = AbstractActivator.getDefault().getConsole();
    // have seen the case of null, but don't know why null can occur
    Assert.isNotNull(console);
    console.print("Deleting generated (and not modified by user) files under ", true); //$NON-NLS-1$
    console.print(parentFolder.getFullPath().toOSString(), false);
    console.print(" : ", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    IResource[] members;
    try {
      members = parentFolder.members();
      delete(members, updFlag, console, new NullProgressMonitor());
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
    }
    console.println();
    return null;
  }

  /**
   * Deletes recursively generated AND derived files. A modified generated file, marked as not derived, shall
   * not be deleted.
   * 
   * @param aMembers - the IResource[] to delete
   * @param aUpdFlag - flag passed to {@link IResource#delete(int, IProgressMonitor)}
   * @param aConsole - the console to print the deleted files names
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void delete(final IResource[] aMembers, final int aUpdFlag, final IConsole aConsole,
                      final IProgressMonitor aMonitor) throws CoreException {
    for (final IResource res : aMembers) {
      if (res.getType() == IResource.FOLDER) {
        delete(((IFolder) res).members(), aUpdFlag, aConsole, aMonitor);
      }
      else if (res.isDerived() && res.getPersistentProperty(GEN_FILE_QN) != null) {
        res.delete(aUpdFlag, aMonitor);
        aConsole.println(res.getFullPath().toOSString(), false);
      }
      else {
        // normally a .jj/.jjt/.jtb file
      }
    }
  }

  /**
   * Retrieves the "keep deleted files from history" flag (from the preferences).
   * 
   * @param aFile - the IResource to get the jar file for
   * @return the flag
   */
  private boolean getKeepDelFilesInHistory(final IResource aFile) {
    String flag = ""; //$NON-NLS-1$
    try {
      if (jPrefs == null) {
        jPrefs = new ProjectScope(aFile.getProject()).getNode(PLUGIN_QN);
      }
      flag = jPrefs.get(KEEP_DEL_FILES_IN_HIST, DEF_KEEP_DEL_FILES_IN_HIST);
    } catch (final Exception e) {
      AbstractActivator.logBug(e);
    }
    return "true".equals(flag); //$NON-NLS-1$
  }

}
