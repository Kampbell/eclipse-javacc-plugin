package sf.eclipse.javacc.scanners;

import static sf.eclipse.javacc.base.IConstants.BLOCK_CMT_CONTENT_TYPE;
import static sf.eclipse.javacc.base.IConstants.JAVADOC_CONTENT_TYPE;
import static sf.eclipse.javacc.base.IConstants.LINE_CMT_CONTENT_TYPE;
import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.DocumentSetupParticipant;

/**
 * A scanner for the comment and javadoc partitions, returning the text attribute (color).<br>
 * Must be coherent with {@link DocumentSetupParticipant}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 */
public class CommentColorScanner extends RuleBasedScanner {

  // BF  05/2012 : code copied and moved from CodeScanner
  // BF  05/2012 : major rewrite
  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings
  // MMa 02/2016 : some renamings ; removed useless field jReturnToken ; renamed from CommentScanner

  /** The preference store */
  protected final IPreferenceStore        jStore        = AbstractActivator.getDefault().getPreferenceStore();

  /** The preference change listener and its associated method */
  protected final IPropertyChangeListener jPrefListener = new IPropertyChangeListener() {

                                                          /** {@inheritDoc} */
                                                          @Override
                                                          public void propertyChange(final PropertyChangeEvent aEvent) {
                                                            final String p = aEvent.getProperty();
                                                            if (!aEvent.getOldValue()
                                                                       .equals(aEvent.getNewValue())) {

                                                              if ((p.equals(P_COMMENT_LINE) && jContentType == LINE_CMT_CONTENT_TYPE)
                                                                  || (p.equals(P_COMMENT_BLOCK) && jContentType == BLOCK_CMT_CONTENT_TYPE)
                                                                  || (p.equals(P_COMMENT_JAVADOC) && jContentType == JAVADOC_CONTENT_TYPE)) {
                                                                synchronized (jOldColors) {
                                                                  jOldColors.add(jColorComment);
                                                                  jColorComment = null;
                                                                }
                                                                init();
                                                              }
                                                              else if (p.equals(P_COMMENT_BACKGROUND)) {
                                                                synchronized (jOldColors) {
                                                                  jOldColors.add(jColorCommentBackground);
                                                                  jColorCommentBackground = null;
                                                                }
                                                                init();
                                                              }
                                                              else {
                                                                if ((p.equals(P_COMMENT_LINE_ATR) && jContentType == LINE_CMT_CONTENT_TYPE)
                                                                    || (p.equals(P_COMMENT_BLOCK_ATR) && jContentType == BLOCK_CMT_CONTENT_TYPE)
                                                                    || (p.equals(P_COMMENT_JAVADOC_ATR) && jContentType == JAVADOC_CONTENT_TYPE)) {
                                                                  init();
                                                                }
                                                              }
                                                            }
                                                          }
                                                        };

  /** The Comment color */
  protected Color                         jColorComment;

  /** The comment background color */
  protected Color                         jColorCommentBackground;

  /** The unused (not yet disposed) color list */
  protected final List<Color>             jOldColors    = new ArrayList<Color>();

  /** The partition content type */
  protected final String                  jContentType;

  /** The default return token */
  protected static IToken                 sDefaultToken = new Token(new TextAttribute(null, null, 0));

  /**
   * Constructor with specified content type to set default return token
   * 
   * @param aContentType - the content type
   */
  public CommentColorScanner(final String aContentType) {
    jContentType = (aContentType == null) ? "" : aContentType; //$NON-NLS-1$

    jStore.addPropertyChangeListener(jPrefListener);

    init();
  }

  /**
   * Loads preferences from the store, adds a listener and initializes the rules.
   */
  void init() {
    disposeUnusedColors();
    IToken returnToken;
    if (jColorCommentBackground == null) {
      jColorCommentBackground = new Color(Display.getCurrent(),
                                          PreferenceConverter.getColor(jStore, P_COMMENT_BACKGROUND));
    }
    if (jContentType == LINE_CMT_CONTENT_TYPE) {
      if (jColorComment == null) {
        jColorComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(jStore, P_COMMENT_LINE));
      }
      returnToken = new Token(new TextAttribute(jColorComment, jColorCommentBackground,
                                                jStore.getInt(P_COMMENT_LINE_ATR)));
      final IRule[] rules = {
        new EndOfLineRule("//", returnToken) }; //$NON-NLS-1$    
      setRules(rules);
    }
    else if (jContentType == BLOCK_CMT_CONTENT_TYPE) {
      if (jColorComment == null) {
        jColorComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(jStore, P_COMMENT_BLOCK));
      }
      returnToken = new Token(new TextAttribute(jColorComment, jColorCommentBackground,
                                                jStore.getInt(P_COMMENT_BLOCK_ATR)));
      final IRule[] rules = {
        new MultiLineRule("/*", "*/", returnToken, (char) 0, true) }; //$NON-NLS-1$ //$NON-NLS-2$
      setRules(rules);
    }
    else if (jContentType == JAVADOC_CONTENT_TYPE) {
      if (jColorComment == null) {
        jColorComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(jStore,
                                                                                     P_COMMENT_JAVADOC));
      }
      returnToken = new Token(new TextAttribute(jColorComment, jColorCommentBackground,
                                                jStore.getInt(P_COMMENT_JAVADOC_ATR)));
      final IRule[] rules = {
        new MultiLineRule("/**", "*/", returnToken, (char) 0, true) }; //$NON-NLS-1$ //$NON-NLS-2$
      setRules(rules);
    }
    else {
      returnToken = sDefaultToken;
      setRules(null);
    }
    setDefaultReturnToken(returnToken);
  }

  /** {@inheritDoc} */
  @Override
  public void setRange(final IDocument aDoc, final int aOffset, final int aLength) {
    super.setRange(aDoc, aOffset, aLength);
  }

  /**
   * Disposes the old (currently unused) colors
   */
  private void disposeUnusedColors() {
    synchronized (jOldColors) {
      while (!jOldColors.isEmpty()) {
        final Color color = jOldColors.remove(jOldColors.size() - 1);
        if (color != null) {
          color.dispose();
        }
      }
    }
  }

  /**
   * Calls all dispose() methods and unregisters the listener.
   */
  public void dispose() {
    synchronized (jOldColors) {
      jOldColors.add(jColorComment);
      jColorComment = null;
      jOldColors.add(jColorCommentBackground);
      jColorCommentBackground = null;
    }
    disposeUnusedColors();
    jStore.removePropertyChangeListener(jPrefListener);
  }

}
