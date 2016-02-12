package sf.eclipse.javacc.editors.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

/**
 * Default spelling problem collector, that manages spelling annotations corresponding to spelling problems on
 * an {@link IAnnotationModel}.<br>
 * If more than one thread reports problems to this collector in parallel, only the thread which called
 * {@link #beginCollecting()} last will be adhered to.
 * 
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class UnusedSpellingProblemCollector implements ISpellingProblemCollector {

  // MMa 12/2009 : added to project for spell checking (but not used)
  // MMa 10/2012 : renamed

  /** Annotation model */
  protected final IAnnotationModel            jAnnotationModel;

  /** Annotations to add */
  protected Map<SpellingAnnotation, Position> jAddAnnotations;

  /** Last thread that called {@link #beginCollecting()} */
  protected Thread                            jThread;

  /**
   * Initializes this collector with the given annotation model.
   * 
   * @param aAnnotationModel - the annotation model
   */
  public UnusedSpellingProblemCollector(final IAnnotationModel aAnnotationModel) {
    jAnnotationModel = aAnnotationModel;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(final SpellingProblem aProblem) {
    synchronized (this) {
      if (jThread != Thread.currentThread()) {
        return;
      }
      jAddAnnotations.put(new SpellingAnnotation(aProblem),
                          new Position(aProblem.getOffset(), aProblem.getLength() + 1));
    }
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void beginCollecting() {
    jThread = Thread.currentThread();
    jAddAnnotations = new HashMap<SpellingAnnotation, Position>();
  }

  /** {@inheritDoc} */
  @Override
  public void endCollecting() {
    synchronized (this) {
      if (jThread != Thread.currentThread()) {
        return;
      }
    }

    final String SPELLING_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$

    final List<Annotation> removeAnnotations = new ArrayList<Annotation>();
    for (final Iterator<?> iter = jAnnotationModel.getAnnotationIterator(); iter.hasNext();) {
      final Annotation annotation = (Annotation) iter.next();
      if (SPELLING_ANNOTATION_TYPE.equals(annotation.getType())) {
        removeAnnotations.add(annotation);
      }
    }

    Map<SpellingAnnotation, Position> addAnnotations;
    synchronized (this) {
      if (jThread != Thread.currentThread()) {
        return;
      }
      addAnnotations = jAddAnnotations;
    }

    if (jAnnotationModel instanceof IAnnotationModelExtension) {
      ((IAnnotationModelExtension) jAnnotationModel).replaceAnnotations(removeAnnotations.toArray(new Annotation[removeAnnotations.size()]),
                                                                        addAnnotations);
    }
    else {
      for (final Iterator<Annotation> iter = removeAnnotations.iterator(); iter.hasNext();) {
        jAnnotationModel.removeAnnotation(iter.next());
      }
      for (final Iterator<SpellingAnnotation> iter = addAnnotations.keySet().iterator(); iter.hasNext();) {
        final Annotation annotation = iter.next();
        jAnnotationModel.addAnnotation(annotation, addAnnotations.get(annotation));
      }
    }

    synchronized (this) {
      if (jThread != Thread.currentThread()) {
        return;
      }
      jThread = null;
      jAddAnnotations = null;
    }
  }
}