package sf.eclipse.javacc.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * The Class JJHyperlinkPresenter.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJHyperlinkPresenter extends DefaultHyperlinkPresenter {

  /** The text viewer */
  private ITextViewer fTextViewer;

  /** The links visible status */
  private boolean     linksVisible;

  /**
   * Instantiates a new hyperlink presenter.
   * 
   * @param iPreferenceStore - the underline color
   */
  public JJHyperlinkPresenter(final IPreferenceStore iPreferenceStore) {
    super(iPreferenceStore);
  }

  /** {@inheritDoc} */
  @Override
  public void install(final ITextViewer textViewer) {
    fTextViewer = textViewer;
    super.install(textViewer);
  }

  /** {@inheritDoc} */
  @Override
  public void hideHyperlinks() {
    super.hideHyperlinks();

    // Refresh the whole document to insure proper syntax coloring,
    // but only when necessary.  This is called by mouseMove() 
    if (linksVisible) {
      fTextViewer.invalidateTextPresentation();
      linksVisible = false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void showHyperlinks(final IHyperlink[] hyperlinks) {
    super.showHyperlinks(hyperlinks);
    linksVisible = true;
  }

}
