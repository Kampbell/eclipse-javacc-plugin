package sf.eclipse.javacc.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.JavaUI;

/**
 * An IStatus which can be set. Can be an error, warning, info or OK.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */

public class Status implements IStatus {

  // MMa 02/2010 : formatting and javadoc revision

  /** The status message */
  private String fStatusMessage;
  /** The severity */
  private int    fSeverity;

  /**
   * Creates a status set to OK (no message).
   */
  public Status() {
    this(OK, null);
  }

  /**
   * Creates a status of a given severity and message.
   * 
   * @param severity The status severity: ERROR, WARNING, INFO and OK.
   * @param message The message of the status for ERROR, WARNING and INFO.
   */
  public Status(final int severity, final String message) {
    fStatusMessage = message;
    fSeverity = severity;
  }

  /**
   * @return if the status' severity is OK.
   */
  public boolean isOK() {
    return fSeverity == IStatus.OK;
  }

  /**
   * @return if the status' severity is WARNING.
   */
  public boolean isWarning() {
    return fSeverity == IStatus.WARNING;
  }

  /**
   * @return if the status' severity is INFO.
   */
  public boolean isInfo() {
    return fSeverity == IStatus.INFO;
  }

  /**
   * @return if the status' severity is ERROR.
   */
  public boolean isError() {
    return fSeverity == IStatus.ERROR;
  }

  /**
   * @see IStatus#getMessage
   * @return the status message
   */
  public String getMessage() {
    return fStatusMessage;
  }

  /**
   * Sets the status to ERROR.
   * 
   * @param errorMessage The error message (can be empty, but not null)
   */
  public void setError(final String errorMessage) {
    fStatusMessage = errorMessage;
    fSeverity = IStatus.ERROR;
  }

  /**
   * Sets the status to WARNING.
   * 
   * @param warningMessage The warning message (can be empty, but not null)
   */
  public void setWarning(final String warningMessage) {
    fStatusMessage = warningMessage;
    fSeverity = IStatus.WARNING;
  }

  /**
   * Sets the status to INFO.
   * 
   * @param infoMessage The info message (can be empty, but not null)
   */
  public void setInfo(final String infoMessage) {
    fStatusMessage = infoMessage;
    fSeverity = IStatus.INFO;
  }

  /**
   * Sets the status to OK.
   */
  public void setOK() {
    fStatusMessage = null;
    fSeverity = IStatus.OK;
  }

  /**
   * @see IStatus#matches(int)
   */
  public boolean matches(final int severityMask) {
    return (fSeverity & severityMask) != 0;
  }

  /**
   * @see IStatus#isMultiStatus()
   * @return always <code>false</code>.
   */
  public boolean isMultiStatus() {
    return false;
  }

  /**
   * @see IStatus#getSeverity()
   */
  public int getSeverity() {
    return fSeverity;
  }

  /**
   * @see IStatus#getPlugin()
   */
  public String getPlugin() {
    return JavaUI.ID_PLUGIN;
  }

  /**
   * @see IStatus#getException()
   * @return always <code>null</code>.
   */
  public Throwable getException() {
    return null;
  }

  /**
   * @see IStatus#getCode()
   */
  public int getCode() {
    return fSeverity;
  }

  /**
   * @see IStatus#getChildren()
   */
  public IStatus[] getChildren() {
    return new IStatus[0];
  }

}