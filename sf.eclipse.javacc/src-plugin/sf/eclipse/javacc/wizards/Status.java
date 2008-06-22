package sf.eclipse.javacc.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.ui.JavaUI;

/**
 * A settable IStatus. Can be an error, warning, info or ok.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class Status implements IStatus {
  private String fStatusMessage;
  private int fSeverity;

  /**
   * Creates a status set to OK (no message)
   */
  public Status() {
    this(OK, null);
  }
  
  /**
   * Creates a status .
   * @param severity The status severity: ERROR, WARNING, INFO and OK.
   * @param message The message of the status for ERROR,WARNING and INFO.
   */
  public Status(int severity, String message) {
    fStatusMessage = message;
    fSeverity = severity;
  }
  
  /**
   * Returns if the status' severity is OK.
   */
  public boolean isOK() {
    return fSeverity == IStatus.OK;
  }
  
  /**
   * Returns if the status' severity is WARNING.
   */
  public boolean isWarning() {
    return fSeverity == IStatus.WARNING;
  }
  
  /**
   * Returns if the status' severity is INFO.
   */
  public boolean isInfo() {
    return fSeverity == IStatus.INFO;
  }
  
  /**
   * Returns if the status' severity is ERROR.
   */
  public boolean isError() {
    return fSeverity == IStatus.ERROR;
  }
  
  /**
   * @see IStatus#getMessage
   */
  public String getMessage() {
    return fStatusMessage;
  }
  
  /**
   * Sets the status to ERROR.
   * @param errorMessage The error message (can be empty, but not null)
   */
  public void setError(String errorMessage) {
    fStatusMessage = errorMessage;
    fSeverity = IStatus.ERROR;
  }
  
  /**
   * Sets the status to WARNING.
   * @param warningMessage The warning message (can be empty, but not null)
   */
  public void setWarning(String warningMessage) {
    fStatusMessage = warningMessage;
    fSeverity = IStatus.WARNING;
  }
  
  /**
   * Sets the status to INFO.
   * @param infoMessage The info message (can be empty, but not null)
   */
  public void setInfo(String infoMessage) {
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
  
  /*
   * @see IStatus#matches(int)
   */
  public boolean matches(int severityMask) {
    return (fSeverity & severityMask) != 0;
  }
  
  /**
   * Returns always <code>false</code>.
   * 
   * @see IStatus#isMultiStatus()
   */
  public boolean isMultiStatus() {
    return false;
  }
  
  /*
   * @see IStatus#getSeverity()
   */
  public int getSeverity() {
    return fSeverity;
  }
  
  /*
   * @see IStatus#getPlugin()
   */
  public String getPlugin() {
    return JavaUI.ID_PLUGIN;
  }
  
  /**
   * Returns always <code>null</code>.
   * @see IStatus#getException()
   */
  public Throwable getException() {
    return null;
  }
  
  /* 
   * @see IStatus#getCode()
   */
  public int getCode() {
    return fSeverity;
  }
  
  /*
   * @see IStatus#getChildren()
   */
  public IStatus[] getChildren() {
    return new IStatus[0];
  }
  
}