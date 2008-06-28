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
  
  public JJTokenRule(IToken token, IToken ptoken){
    this.token = token;
    this.ptoken = ptoken;
  }

  public IToken evaluate(ICharacterScanner scanner) {
    boolean match = false;
    boolean privateToken = false;
    int c;
    while ((c = scanner.read()) != ICharacterScanner.EOF) {
      if (c == '<')
        match = true;
      else if (match) {
        if (c == '#')
          privateToken = true;
        else if (c == '>' || c == ':')
          return privateToken ? ptoken : token;
      }
      else {
        scanner.unread();
        return Token.UNDEFINED;
      }
    }
    return Token.UNDEFINED;
  }
}
