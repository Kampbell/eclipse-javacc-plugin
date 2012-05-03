package sf.eclipse.javacc.wizards;

import org.eclipse.core.runtime.IStatus;

import sf.eclipse.javacc.base.IJJConstants;

/**
 * An IStatus which can be set. Can be an error, warning, info or OK.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */

public class JJStatus implements IStatus {

  // MMa 02/2010 : formatting and javadoc revision

  /** The status message */
  private String jStatusMessage;
  /** The severity */
  private int    jSeverity;

  /**
   * Creates a status set to OK (no message).
   */
  public JJStatus() {
    this(OK, null);
  }

  /**
   * Creates a status of a given severity and message.
   * 
   * @param aSeverity The status severity: ERROR, WARNING, INFO and OK.
   * @param aStatusMessage The message of the status for ERROR, WARNING and INFO.
   */
  public JJStatus(final int aSeverity, final String aStatusMessage) {
    jStatusMessage = aStatusMessage;
    jSeverity = aSeverity;
  }

  /**
   * @return if the status' severity is OK.
   */
  @Override
  public boolean isOK() {
    return jSeverity == IStatus.OK;
  }

  /**
   * @return if the status' severity is WARNING.
   */
  public boolean isWarning() {
    return jSeverity == IStatus.WARNING;
  }

  /**
   * @return if the status' severity is INFO.
   */
  public boolean isInfo() {
    return jSeverity == IStatus.INFO;
  }

  /**
   * @return if the status' severity is ERROR.
   */
  public boolean isError() {
    return jSeverity == IStatus.ERROR;
  }

  /**
   * @see IStatus#getMessage
   * @return the status message
   */
  @Override
  public String getMessage() {
    return jStatusMessage;
  }

  /**
   * Sets the status to ERROR.
   * 
   * @param aErrorMessage The error message (can be empty, but not null)
   */
  public void setError(final String aErrorMessage) {
    jStatusMessage = aErrorMessage;
    jSeverity = IStatus.ERROR;
  }

  /**
   * Sets the status to WARNING.
   * 
   * @param aWarningMessage The warning message (can be empty, but not null)
   */
  public void setWarning(final String aWarningMessage) {
    jStatusMessage = aWarningMessage;
    jSeverity = IStatus.WARNING;
  }

  /**
   * Sets the status to INFO.
   * 
   * @param aInfoMessage The info message (can be empty, but not null)
   */
  public void setInfo(final String aInfoMessage) {
    jStatusMessage = aInfoMessage;
    jSeverity = IStatus.INFO;
  }

  /**
   * Sets the status to OK.
   */
  public void setOK() {
    jStatusMessage = null;
    jSeverity = IStatus.OK;
  }

  /**
   * @see IStatus#matches(int)
   */
  @Override
  public boolean matches(final int aSeverityMask) {
    return (jSeverity & aSeverityMask) != 0;
  }

  /**
   * @see IStatus#isMultiStatus()
   * @return always <code>false</code>.
   */
  @Override
  public boolean isMultiStatus() {
    return false;
  }

  /**
   * @see IStatus#getSeverity()
   */
  @Override
  public int getSeverity() {
    return jSeverity;
  }

  /**
   * @see IStatus#getPlugin()
   */
  @Override
  public String getPlugin() {
    //    return JavaUI.ID_PLUGIN;
    return IJJConstants.ID;
  }

  /**
   * @see IStatus#getException()
   * @return always <code>null</code>.
   */
  @Override
  public Throwable getException() {
    return null;
  }

  /**
   * @see IStatus#getCode()
   */
  @Override
  public int getCode() {
    return jSeverity;
  }

  /**
   * @see IStatus#getChildren()
   */
  @Override
  public IStatus[] getChildren() {
    return new IStatus[0];
  }

}