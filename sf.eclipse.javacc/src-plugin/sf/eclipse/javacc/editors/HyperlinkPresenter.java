package sf.eclipse.javacc.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * Class HyperlinkPresenter underlines the link and colors the line and the text with the given color.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2012-2013-2014
 */
class HyperlinkPresenter extends DefaultHyperlinkPresenter {

  // MMa 10/2012 : renamed
  // MMa 11/2014 : modified some modifiers

  /** The text viewer */
  private ITextViewer jTextViewer;

  /** The links visible status */
  private boolean     jAreLinksVisible;

  /**
   * Instantiates a new hyperlink presenter.
   * 
   * @param iPreferenceStore - the underline color
   */
  public HyperlinkPresenter(final IPreferenceStore iPreferenceStore) {
    super(iPreferenceStore);
  }

  /** {@inheritDoc} */
  @Override
  public void install(final ITextViewer textViewer) {
    jTextViewer = textViewer;
    super.install(textViewer);
  }

  /** {@inheritDoc} */
  @Override
  public void hideHyperlinks() {
    super.hideHyperlinks();

    // Refresh the whole document to insure proper syntax coloring,
    // but only when necessary.  This is called by mouseMove() 
    if (jAreLinksVisible) {
      jTextViewer.invalidateTextPresentation();
      jAreLinksVisible = false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void showHyperlinks(final IHyperlink[] hyperlinks) {
    super.showHyperlinks(hyperlinks);
    jAreLinksVisible = true;
  }

}
