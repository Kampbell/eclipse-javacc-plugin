package sf.eclipse.javacc;
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * CompileAction used in a Popup Menu for .jj or .jjt files
 * in PackageExplorer, referenced in plugin.xml 
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class CompileAction implements IObjectActionDelegate {
  private IWorkbenchPart part;
  private IFile file;

  /**
   * Called every time the action appears in a popup menu.
   */
  public void setActivePart(IAction action, IWorkbenchPart part) {
    this.part = part;
  }

  /**
   * Called when the selection in the workbench has changed.
   * If it's an IFile we keep a reference on it.
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      Object obj = ((IStructuredSelection) selection).getFirstElement();
      if (obj != null && obj instanceof IFile) {
        this.file = (IFile) obj;
      }
    }
  }
  
  /**
   * Called when the action has been triggered.
   * compile the .jj or .jjt file
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction anAction) {
    if (file == null)
      return;
    try {
      // Compile
      JJBuilder.CompileJJResource(file);
      // Refresh the whole project
      file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
}
