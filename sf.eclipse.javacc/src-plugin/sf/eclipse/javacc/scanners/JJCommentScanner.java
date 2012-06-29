package sf.eclipse.javacc.scanners;

import java.util.ArrayList;

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

import sf.eclipse.javacc.editors.JJDocumentProvider;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.preferences.IPrefConstants;

/**
 * A scanner for the comment and javadoc partitions, returning the text attribute (color).<br>
 * Must be coherent with {@link JJDocumentProvider}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJCommentScanner extends RuleBasedScanner implements IPrefConstants {

  // BF  05/2012 : code copied and moved from JJCodeScanner
  // BF  05/2012 : major rewrite

  /** The preference store */
  private final IPreferenceStore        sStore        = Activator.getDefault().getPreferenceStore();

  /** The preference change listener and its associated method */
  private final IPropertyChangeListener jPrefListener = new IPropertyChangeListener() {

                                                        @Override
                                                        public void propertyChange(final PropertyChangeEvent event) {
                                                          final String p = event.getProperty();
                                                          if (!event.getOldValue()
                                                                    .equals(event.getNewValue())) {

                                                            if ((p.equals(P_COMMENT_LINE) && jjContentType == JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE)
                                                                || (p.equals(P_COMMENT_BLOCK) && jjContentType == JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE)
                                                                || (p.equals(P_COMMENT_JAVADOC) && jjContentType == JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE)) {
                                                              synchronized (oldColors) {
                                                                oldColors.add(cComment);
                                                                cComment = null;
                                                              }
                                                              init();
                                                            }
                                                            else if (p.equals(P_COMMENT_BACKGROUND)) {
                                                              synchronized (oldColors) {
                                                                oldColors.add(cCommentBackground);
                                                                cCommentBackground = null;
                                                              }
                                                              init();
                                                            }
                                                            else {
                                                              if ((p.equals(P_COMMENT_LINE_ATR) && jjContentType == JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE)
                                                                  || (p.equals(P_COMMENT_BLOCK_ATR) && jjContentType == JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE)
                                                                  || (p.equals(P_COMMENT_JAVADOC_ATR) && jjContentType == JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE)) {
                                                                init();
                                                              }
                                                            }
                                                          }
                                                        }
                                                      };

  /** The Comment color */
  Color                                 cComment;

  /** The comment background color */
  Color                                 cCommentBackground;

  /** The unused (not yet disposed) color list */
  final ArrayList<Color>                oldColors     = new ArrayList<Color>();

  /** The partition content type */
  final String                          jjContentType;

  /** The return token */
  private IToken                        fReturnToken;

  /** The default return token */
  private static IToken                 fDefaultToken = new Token(new TextAttribute(null, null, 0));

  /**
   * Constructor with specified content type to set default return token
   * 
   * @param contentType - the content type
   */
  public JJCommentScanner(final String contentType) {
    jjContentType = (contentType == null) ? "" : contentType; //$NON-NLS-1$

    sStore.addPropertyChangeListener(jPrefListener);

    init();
  }

  /**
   * Loads preferences from the store, adds a listener and initializes the rules.
   */
  void init() {
    disposeUnusedColors();

    if (cCommentBackground == null) {
      cCommentBackground = new Color(Display.getCurrent(), PreferenceConverter.getColor(sStore,
                                                                                        P_COMMENT_BACKGROUND));
    }
    if (jjContentType == JJDocumentProvider.JJ_LINE_COMMENT_CONTENT_TYPE) {
      if (cComment == null) {
        cComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(sStore, P_COMMENT_LINE));
      }
      fReturnToken = new Token(new TextAttribute(cComment, cCommentBackground,
                                                 sStore.getInt(P_COMMENT_LINE_ATR)));
      final IRule[] rules = {
        new EndOfLineRule("//", fReturnToken) }; //$NON-NLS-1$    
      setRules(rules);
    }
    else if (jjContentType == JJDocumentProvider.JJ_BLOCK_COMMENT_CONTENT_TYPE) {
      if (cComment == null) {
        cComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(sStore, P_COMMENT_BLOCK));
      }
      fReturnToken = new Token(new TextAttribute(cComment, cCommentBackground,
                                                 sStore.getInt(P_COMMENT_BLOCK_ATR)));
      final IRule[] rules = {
        new MultiLineRule("/*", "*/", fReturnToken, (char) 0, true) }; //$NON-NLS-1$ //$NON-NLS-2$
      setRules(rules);
    }
    else if (jjContentType == JJDocumentProvider.JJ_JAVADOC_CONTENT_TYPE) {
      if (cComment == null) {
        cComment = new Color(Display.getCurrent(), PreferenceConverter.getColor(sStore, P_COMMENT_JAVADOC));
      }
      fReturnToken = new Token(new TextAttribute(cComment, cCommentBackground,
                                                 sStore.getInt(P_COMMENT_JAVADOC_ATR)));
      final IRule[] rules = {
        new MultiLineRule("/**", "*/", fReturnToken, (char) 0, true) }; //$NON-NLS-1$ //$NON-NLS-2$
      setRules(rules);
    }
    else {
      fReturnToken = fDefaultToken;
      setRules(null);
    }
    setDefaultReturnToken(fReturnToken);
  }

  /** {@inheritDoc} */
  @Override
  public void setRange(final IDocument document, final int offset, final int length) {
    super.setRange(document, offset, length);
    //fDocument = document;
  }

  /**
   * Disposes the old (currently unused) colors
   */
  private void disposeUnusedColors() {
    synchronized (oldColors) {
      while (!oldColors.isEmpty()) {
        final Color color = oldColors.remove(oldColors.size() - 1);
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
    synchronized (oldColors) {
      oldColors.add(cComment);
      cComment = null;
      oldColors.add(cCommentBackground);
      cCommentBackground = null;
    }
    disposeUnusedColors();
    sStore.removePropertyChangeListener(jPrefListener);
  }

}
