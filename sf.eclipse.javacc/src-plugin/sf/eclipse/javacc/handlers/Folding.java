package sf.eclipse.javacc.handlers;

import static sf.eclipse.javacc.base.IConstants.COLLAPSE_ALL_ID;
import static sf.eclipse.javacc.base.IConstants.COLLAPSE_ID;
import static sf.eclipse.javacc.base.IConstants.EXPAND_ALL_ID;
import static sf.eclipse.javacc.base.IConstants.EXPAND_ID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Folding handler. Manages collapse, expand, collapse_all and expand_all commands.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2014
 */
public class Folding extends AbstractHandler {

  // MMa 11/2012 & 11/2014 : created from the corresponding now deprecated action, & merged in a single class

  /** {@inheritDoc} */
  @Override
  public Object execute(final ExecutionEvent event) {
    // in which part were we called
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (!(part instanceof IEditorPart)) {
      // on a viewer, do nothing
      return null;
    }
    // on an editor
    final IEditorPart editor = (IEditorPart) part;
    if (!(editor instanceof JJEditor)) {
      // not our editor (no reason why, however), do nothing
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.NotOur_problem (" + editor.getClass().getName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
    // our editor
    final JJEditor jEditor = (JJEditor) editor;
    final ISourceViewer sourceViewer = jEditor.getSourceViewerPlease();
    if (sourceViewer instanceof ProjectionViewer) {
      final ProjectionViewer pv = (ProjectionViewer) sourceViewer;
      if (pv.isProjectionMode()) {
        final Command cmd = event.getCommand();
        final String cmdId = cmd.getId();
        if (COLLAPSE_ID.equals(cmdId)) {
          if (pv.canDoOperation(ProjectionViewer.COLLAPSE)) {
            pv.doOperation(ProjectionViewer.COLLAPSE);
          }
        }
        else if (COLLAPSE_ALL_ID.equals(cmdId)) {
          if (pv.canDoOperation(ProjectionViewer.COLLAPSE_ALL)) {
            pv.doOperation(ProjectionViewer.COLLAPSE_ALL);
          }
        }
        else if (EXPAND_ID.equals(cmdId)) {
          if (pv.canDoOperation(ProjectionViewer.EXPAND)) {
            pv.doOperation(ProjectionViewer.EXPAND);
          }
        }
        else if (EXPAND_ALL_ID.equals(cmdId)) {
          if (pv.canDoOperation(ProjectionViewer.EXPAND_ALL)) {
            pv.doOperation(ProjectionViewer.EXPAND_ALL);
          }
        }
      }
    }
    return null;
  }

}
