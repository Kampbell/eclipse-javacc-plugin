package sf.eclipse.javacc.parser;

/**
 * Class to be extended by the JavaCC Token class through the TOKEN_EXTENDS option, aimed at computing and
 * giving access to the token offset.
 * 
 * @author Marc Mazas 2012-2013-2014
 */
public class UnusedJJToken implements java.io.Serializable {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  /** The offset from the begin of the stream (1-Relative) */
  public int                offset;

}
