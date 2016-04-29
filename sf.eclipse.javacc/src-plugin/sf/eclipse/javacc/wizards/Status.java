package sf.eclipse.javacc.wizards;

import static sf.eclipse.javacc.base.IConstants.PLUGIN_QN;

import org.eclipse.core.runtime.IStatus;

/**
 * An IStatus which can be set. Can be an error, warning, info or OK.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */

class Status implements IStatus {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : renamed

  /** The status message */
  protected String jStatusMessage;
  /** The severity */
  protected int    jSeverity;

  /**
   * Creates a status set to OK (no message).
   */
  public Status() {
    this(OK, null);
  }

  /**
   * Creates a status of a given severity and message.
   * 
   * @param aSeverity - The status severity: ERROR, WARNING, INFO and OK
   * @param aStatusMessage - The message of the status for ERROR, WARNING and INFO
   */
  public Status(final int aSeverity, final String aStatusMessage) {
    jStatusMessage = aStatusMessage;
    jSeverity = aSeverity;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isOK() {
    return jSeverity == IStatus.OK;
  }

  /**
   * @return if the status' severity is WARNING
   */
  public boolean isWarning() {
    return jSeverity == IStatus.WARNING;
  }

  /**
   * @return if the status' severity is INFO
   */
  public boolean isInfo() {
    return jSeverity == IStatus.INFO;
  }

  /**
   * @return if the status' severity is ERROR
   */
  public boolean isError() {
    return jSeverity == IStatus.ERROR;
  }

  /** {@inheritDoc} */
  @Override
  public String getMessage() {
    return jStatusMessage;
  }

  /**
   * Sets the status to ERROR.
   * 
   * @param aErrorMessage - The error message (can be empty, but not null)
   */
  public void setError(final String aErrorMessage) {
    jStatusMessage = aErrorMessage;
    jSeverity = IStatus.ERROR;
  }

  /**
   * Sets the status to WARNING.
   * 
   * @param aWarningMessage - The warning message (can be empty, but not null)
   */
  public void setWarning(final String aWarningMessage) {
    jStatusMessage = aWarningMessage;
    jSeverity = IStatus.WARNING;
  }

  /**
   * Sets the status to INFO.
   * 
   * @param aInfoMessage - The info message (can be empty, but not null)
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

  /** {@inheritDoc} */
  @Override
  public boolean matches(final int aSeverityMask) {
    return (jSeverity & aSeverityMask) != 0;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isMultiStatus() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int getSeverity() {
    return jSeverity;
  }

  /** {@inheritDoc} */
  @Override
  public String getPlugin() {
    return PLUGIN_QN;
  }

  /**
   * Return always <code>null</code>.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Throwable getException() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public int getCode() {
    return jSeverity;
  }

  /** {@inheritDoc} */
  @Override
  public IStatus[] getChildren() {
    return new IStatus[0];
  }

}