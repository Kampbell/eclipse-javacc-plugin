package sf.eclipse.javacc.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

/**
 * Reconciler strategy which update the Outline View
 * on a document change.
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension{
  JJEditor editor;
  
  /**
   * Reconciling strategy updates the Outline View
   */
  public JJReconcilingStrategy(JJEditor anEditor) {
    editor = anEditor;
  }

  public void setDocument(IDocument aDoc) {
	update();
  }

  /**
   * @see IReconcilingStrategyExtension#initialReconcile()
   */
  public void initialReconcile() {
	update();
  }
  
  public void reconcile(DirtyRegion aDirtyRegion, IRegion aRegion) {
    update();
  }

  public void reconcile(IRegion aRegion) {
    update();
  }

  /**
   * Update the Outline View
   */
  public void update() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        editor.updateOutlinePage();
      }
    });
  }

  /**
   * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void setProgressMonitor(IProgressMonitor monitor) {
      // Needed by implementing IReconcilingStrategyExtension
      // Do nothing
  }
}
