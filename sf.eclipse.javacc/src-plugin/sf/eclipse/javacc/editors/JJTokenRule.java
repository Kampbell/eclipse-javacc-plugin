package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.rules.*;

/**
 * A special IRule for JavaCC tokens both plain and private
 * @author Remi Koutcherawy 2003-2008 CeCILL Licence
 * http://www.cecill.info/index.en.html
 */
public class JJTokenRule implements IRule {
  private IToken token;
  private IToken ptoken;
  // token colors normal tokens, ptoken colors private tokens
  public JJTokenRule(IToken token, IToken ptoken){
    this.token = token;
    this.ptoken = ptoken;
  }

  public IToken evaluate(ICharacterScanner scanner) {
    boolean match = false;
    boolean privateToken = false;
    int c;
    while ((c = scanner.read()) != ICharacterScanner.EOF) {
      // Begin rule
      if (c == '<')
        match = true;
      // Follow rule
      else if (match) {
        // Private token identified
        if (c == '#')
          privateToken = true;
        // End token
        else if (c == '>' || c == ':')
          return privateToken ? ptoken : token;
        // Avoid coloring Strings
        else if (c == '\"'){
          scanner.unread();
          return privateToken ? ptoken : token;
        }
        // Try to avoid coloring java expressions like : if(a < b...)
        else if (c == '=' || c == '&' || c == '|' || c == ')')
          return Token.UNDEFINED;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }
    return Token.UNDEFINED;
  }
}
