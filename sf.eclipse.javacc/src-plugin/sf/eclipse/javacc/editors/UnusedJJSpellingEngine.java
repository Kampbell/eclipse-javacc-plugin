package sf.eclipse.javacc.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.text.spelling.DefaultSpellingEngine;
import org.eclipse.jdt.internal.ui.text.spelling.JavaSpellingEngine;
import org.eclipse.jdt.internal.ui.text.spelling.SpellingEngine;
import org.eclipse.jdt.internal.ui.text.spelling.TextSpellingEngine;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;

/**
 * JJ spelling engine, copied from the default spelling engine {@link DefaultSpellingEngine} and modified to
 * use a {@link JavaSpellingEngine} to process {@link JJDocumentProvider#JJ_COMMENT_CONTENT_TYPE} partitions.
 * 
 * @author Marc Mazas 2009-2010
 */
@SuppressWarnings("restriction")
public class UnusedJJSpellingEngine extends DefaultSpellingEngine {

  // MMa 12/2009 : added to project for spell checking (but not used)

  /** Text content type */
  private static final IContentType               TEXT_CONTENT_TYPE = Platform
                                                                              .getContentTypeManager()
                                                                              .getContentType(
                                                                                              IContentTypeManager.CT_TEXT);

  /** Java source content type */
  private static final IContentType               JAVA_CONTENT_TYPE = Platform
                                                                              .getContentTypeManager()
                                                                              .getContentType(
                                                                                              JavaCore.JAVA_SOURCE_CONTENT_TYPE);

  //  /** Java properties content type */
  //  private static final IContentType PROPERTIES_CONTENT_TYPE = Platform
  //                                                                      .getContentTypeManager()
  //                                                                      .getContentType(
  //                                                                                      "org.eclipse.jdt.core.javaProperties"); //$NON-NLS-1$

  /** Available spelling engines by content type */
  private final Map<IContentType, SpellingEngine> jEngines          = new HashMap<IContentType, SpellingEngine>();

  /**
   * Initialize concrete engines.
   */
  public UnusedJJSpellingEngine() {
    if (JAVA_CONTENT_TYPE != null) {
      jEngines.put(JAVA_CONTENT_TYPE, new JavaSpellingEngine());
    }
    //    if (PROPERTIES_CONTENT_TYPE != null)
    //      fEngines.put(PROPERTIES_CONTENT_TYPE, new PropertiesFileSpellingEngine());
    if (TEXT_CONTENT_TYPE != null) {
      jEngines.put(TEXT_CONTENT_TYPE, new TextSpellingEngine());
    }
  }

  /**
   * Modified from
   * {@link DefaultSpellingEngine#check(IDocument, IRegion[], SpellingContext, ISpellingProblemCollector, IProgressMonitor)}
   * to return the {@link JavaSpellingEngine}.
   * 
   * @see ISpellingEngine#check(IDocument, IRegion[], SpellingContext, ISpellingProblemCollector,
   *      IProgressMonitor)
   */
  @Override
  public void check(final IDocument aDoc, final IRegion[] aRegions, final SpellingContext aCtx,
                    final ISpellingProblemCollector aCollector, final IProgressMonitor aMonitor) {
    //    ISpellingEngine engine= getEngine(context.getContentType());
    ISpellingEngine engine = getEngine(JAVA_CONTENT_TYPE);
    if (engine == null) {
      engine = getEngine(TEXT_CONTENT_TYPE);
    }
    if (engine != null) {
      engine.check(aDoc, aRegions, aCtx, aCollector, aMonitor);
    }
  }

  /**
   * Returns a spelling engine for the given content type or <code>null</code> if none could be found.
   * 
   * @param aContentType the content type
   * @return a spelling engine for the given content type or <code>null</code> if none could be found
   */
  private ISpellingEngine getEngine(final IContentType aContentType) {
    if (aContentType == null) {
      return null;
    }

    if (jEngines.containsKey(aContentType)) {
      return jEngines.get(aContentType);
    }

    return getEngine(aContentType.getBaseType());
  }
}
