package sf.eclipse.javacc.editors.old;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.spelling.SpellingEngineRegistry;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingEngineDescriptor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * JJ spelling service copied from the System wide spelling service {@link SpellingService}.
 * 
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
@SuppressWarnings("restriction")
class UnusedSpellingServiceJJ {

  // MMa 12/2009 : added to project for spell checking
  // BF  06/2012 : added unused tag for unused exception

  /**
   * A named preference that controls if spelling is enabled or disabled.
   * <p>
   * Value is of type <code>Boolean</code>
   * </p>
   */
  public static final String       PREF_SPELLING_ENABLED = "spellingEnabled"; //$NON-NLS-1$

  /**
   * A named preference that controls which spelling engine is used. The value is the spelling engine's
   * extension id.
   * <p>
   * Value is of type <code>String</code>
   * </p>
   */
  public static final String       PREF_SPELLING_ENGINE  = "spellingEngine"; //$NON-NLS-1$

  /** Preferences */
  protected final IPreferenceStore jPrefStore;

  /**
   * Initializes the spelling service with the given preferences.
   * 
   * @param aPrefStore - the preferences
   * @see SpellingService#PREFERENCE_SPELLING_ENABLED
   * @see SpellingService#PREFERENCE_SPELLING_ENGINE
   */
  public UnusedSpellingServiceJJ(final IPreferenceStore aPrefStore) {
    jPrefStore = aPrefStore;
  }

  /**
   * Checks the given document. Reports all found spelling problems to the collector. The spelling engine is
   * chosen based on the settings from the given preferences.
   * 
   * @param aDoc - the document to check
   * @param aCtx - the context
   * @param aCollector - the problem collector
   * @param aMonitor - the progress monitor, can be <code>null</code>
   */
  public void check(final IDocument aDoc, final SpellingContext aCtx,
                    final ISpellingProblemCollector aCollector, final IProgressMonitor aMonitor) {
    check(aDoc, new IRegion[] {
      new Region(0, aDoc.getLength()) }, aCtx, aCollector, aMonitor);
  }

  /**
   * Checks the given regions in the given document. Reports all found spelling problems to the collector. The
   * spelling engine is chosen based on the settings from the given preferences.
   * 
   * @param aDoc - the document to check
   * @param aRegions - the regions to check
   * @param aCtx - the context
   * @param aCollector - the problem collector
   * @param aMonitor - the progress monitor, can be <code>null</code>
   */
  public void check(final IDocument aDoc, final IRegion[] aRegions, final SpellingContext aCtx,
                    final ISpellingProblemCollector aCollector, final IProgressMonitor aMonitor) {
    try {
      aCollector.beginCollecting();
      jPrefStore.setDefault(PREF_SPELLING_ENABLED, true);
      if (jPrefStore.getBoolean(PREF_SPELLING_ENABLED)) {
        try {
          final ISpellingEngine engine = createEngine(jPrefStore);
          if (engine != null) {
            final ISafeRunnable runnable = new ISafeRunnable() {

              /** {@inheritDoc} */
              @Override
              public void run() throws Exception {
                engine.check(aDoc, aRegions, aCtx, aCollector, aMonitor);
              }

              /** {@inheritDoc} */
              @Override
              public void handleException(@SuppressWarnings("unused") final Throwable x) {
                // swallowed
              }
            };
            SafeRunner.run(runnable);
          }
        } catch (final CoreException e) {
          AbstractActivator.logBug(e);
        }
      }
    } finally {
      aCollector.endCollecting();
    }
  }

  /**
   * Returns all spelling engine descriptors from extensions to the spelling engine extension point.
   * 
   * @return all spelling engine descriptors
   */
  public static SpellingEngineDescriptor[] getSpellingEngineDescriptors() {
    final SpellingEngineRegistry registry = getSpellingEngineRegistry();
    if (registry == null) {
      return new SpellingEngineDescriptor[0];
    }
    return registry.getDescriptors();
  }

  /**
   * Returns the default spelling engine descriptor from extensions to the spelling engine extension point.
   * 
   * @return the default spelling engine descriptor or <code>null</code> if none could be found
   */
  public static SpellingEngineDescriptor getDefaultSpellingEngineDescriptor() {
    final SpellingEngineRegistry registry = getSpellingEngineRegistry();
    if (registry == null) {
      return null;
    }
    return registry.getDefaultDescriptor();
  }

  /**
   * Returns the descriptor of the active spelling engine based on the value of the
   * <code>PREFERENCE_SPELLING_ENGINE</code> preference in the given preferences.
   * 
   * @param aPrefStore - the preferences
   * @return the descriptor of the active spelling engine or <code>null</code> if none could be found
   * @see SpellingService#PREFERENCE_SPELLING_ENGINE
   */
  public static SpellingEngineDescriptor getActiveSpellingEngineDescriptor(final IPreferenceStore aPrefStore) {
    final SpellingEngineRegistry registry = getSpellingEngineRegistry();
    if (registry == null) {
      return null;
    }

    SpellingEngineDescriptor descriptor = null;
    if (aPrefStore.contains(PREF_SPELLING_ENGINE)) {
      descriptor = registry.getDescriptor(aPrefStore.getString(PREF_SPELLING_ENGINE));
    }
    if (descriptor == null) {
      descriptor = registry.getDefaultDescriptor();
    }
    return descriptor;
  }

  /**
   * Creates a spelling engine based on the value of the <code>PREFERENCE_SPELLING_ENGINE</code> preference in
   * the given preferences.
   * 
   * @param aPrefStore - the preferences
   * @return the created spelling engine or <code>null</code> if none could be created
   * @throws CoreException - if the creation failed
   * @see SpellingService#PREFERENCE_SPELLING_ENGINE
   */
  private static ISpellingEngine createEngine(final IPreferenceStore aPrefStore) throws CoreException {
    final SpellingEngineDescriptor descriptor = getActiveSpellingEngineDescriptor(aPrefStore);
    if (descriptor != null) {
      return descriptor.createEngine();
      //      return new SpellingEngineJJ();
    }
    return null;
  }

  /**
   * Returns the spelling engine registry.
   * 
   * @return the spelling engine registry or <code>null</code> if the plug-in has been shutdown
   */
  private final static SpellingEngineRegistry getSpellingEngineRegistry() {
    return TextEditorPlugin.getDefault().getSpellingEngineRegistry();
  }
}
