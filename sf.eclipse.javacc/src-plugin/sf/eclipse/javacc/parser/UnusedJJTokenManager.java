package sf.eclipse.javacc.parser;

/**
 * Custom TokenManager, aimed at computing and giving access to the token offset.<br>
 * Note that it extends {@link JavaCCParserTokenManager}, which must be rebuilt whenever the grammar tokens
 * change (recompile the grammar with USER_TOKEN_MANAGER = false;).
 * 
 * @author Marc Mazas 2012-2013-2014
 */
public class UnusedJJTokenManager extends JavaCCParserTokenManager implements TokenManager {

  /**
   * Constructor.
   * 
   * @param stream - the input stream
   */
  public UnusedJJTokenManager(final JavaCharStream stream) {
    super(stream);
  }

  //  /**
  //   * @return a new token, filled with the standard information and the offset
  //   */
  //  @Override
  //  protected Token jjFillToken() {
  //    final Token t = super.jjFillToken();
  //    t.offset = input_stream.bufpos;
  //    return t;
  //  }

  /** Reinitialise parser. */
  @Override
  public void ReInit(final JavaCharStream stream) {
    super.ReInit(stream);
  }

  /** Reinitialise parser. */
  @Override
  public void ReInit(final JavaCharStream stream, final int lexState) {
    super.ReInit(stream, lexState);
  }

}
