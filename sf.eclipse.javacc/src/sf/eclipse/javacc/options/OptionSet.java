package sf.eclipse.javacc.options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A set of Options can parse a command line and generate one. 
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt */
public class OptionSet {

  protected ArrayList list;
  protected String target;

  /**
   * Constructor OptionSet.
   */
  public OptionSet() {
    list = new ArrayList();
  }

  /**
   * Get as a command line
   */
  public String toString() {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < list.size(); i++) {
      String value = getValue(i);
      String def = getDefaultValue(i);
      if (value.equals(def))
        continue; 
      if (b.length() != 0)
        b.append(" ");
      b.append("-").append(getName(i));
      b.append("=").append(value);
    }
    if (target != null)
      b.append(" ").append(target);
    return b.toString();
  }

  /**
   * Splits arg into String[]
   */
  public static String[] tokenize(String commandLine) {

    int count = 0;
    String[] arguments = new String[10];
    StringTokenizer tokenizer = new StringTokenizer(commandLine, " \"", true); //$NON-NLS-1$
    String token;
    boolean insideQuotes = false;
    boolean startNewToken = true;

    // takes care of quotes on the command line
    // '-xxx "aaa -bbb" -ccc' --->  {"-xxx aaa -bbb","-ccc"}
    // '-xxx aaa -bbb -ccc'   --->  {"-xxx aaa","-bbb","-ccc"}
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();

      if (token.equals(" ")) {
        if (insideQuotes) {
          arguments[count - 1] += token;
          startNewToken = false;
        } else {
          startNewToken = true;
        }
      } else if (token.equals("\"")) {
        if (!insideQuotes && startNewToken) {
          if (count == arguments.length)
            System.arraycopy(arguments, 0,
              (arguments = new String[count * 2]),
              0, count);
          arguments[count++] = "";
        }
        insideQuotes = !insideQuotes;
        startNewToken = false;
      } else {
        if (insideQuotes) {
          arguments[count - 1] += token;
        } else {
          if (token.length() > 0 && !startNewToken) {
            arguments[count - 1] += token;
          } else {
            if (count == arguments.length)
              System.arraycopy(arguments, 0,
                (arguments = new String[count * 2]),
                0, count);
            arguments[count++] = token;
          }
        }
        startNewToken = false;
      }
    }
    System.arraycopy(arguments, 0, arguments = new String[count], 0, count);
    return arguments;
  }

  /**
   * Configures from a Command line
   * @param string
   */
  public void configuresFrom(String str) {
    if (str == null) {
      resetToDefaultValues();
      return;
    }
    
    String[] tok = tokenize(str);
    for (int i = 0; i < tok.length; i++) {
      // Standart option ie -xxx=yyy
      if (tok[i].startsWith("-") && tok[i].indexOf("=") != -1) {
        String name = tok[i].substring(1, tok[i].indexOf('='));
        String value = tok[i].substring(tok[i].indexOf('=') + 1);
        if (value.length() == 0)
          continue;
        for (int j = 0; j < list.size(); j++) {
          Option opt = (Option) (list.get(j));
          if (opt.getName().equals(name)) {
            opt.setValue(value);
            break;
          }
        }
      }
      // Maybe target argument, ie file.jj or file.jjt
      else
        target = tok[i];
    }
  }

  /**
   * Reset all options to default values.
   */
  public void resetToDefaultValues() {
    Iterator it = list.iterator();
    while (it.hasNext()) {
      Option opt = ((Option) it.next());
      opt.setValue(opt.getDefaultValue());
    }
  }

  /**
   * Adds an Option
   * @param option
   */
  public void add(Option option) {
    list.add(option);
  }

  /**
   * Gets i option of type.
   */
  public Option getOption(int i) {
    return (Option) list.get(i);
  }

  /**
   * Gets total number of options.
   */
  public int getOptionsSize() {
    return list.size();
  }
  /**
   * Gets number of options of given type.
   */
  public int getOptionsSize(int type) {
    int n = 0;
    Iterator it = list.iterator();
    while (it.hasNext())
      if (((Option) it.next()).getType() == type)
        n++;
    return n;
  }

  /**
   * Gets type of i option.
   * @param i
   */
  public int getType(int i) {
    return getOption(i).getType();
  }

  /**
   * Gets name of i option.
   * @param i
   */
  public String getName(int i) {
    return getOption(i).getName();
  }

  /**
   * Gets value of i option.
   * @param i
   */
  public String getValue(int i) {
    String val = getOption(i).getValue();
    if (val.indexOf(' ') != -1 && !val.startsWith("\""))
      val = "\""+val+"\"";
    return val;
  }
  /**
   * Gets value of i option.
   * @param i
   */
  public String getValueNoQuotes(int i) {
    return getOption(i).getValue();
  }

  /**
   * Gets default value of i option.
   * @param i
   */
  public String getDefaultValue(int i) {
    return getOption(i).getDefaultValue();
  }

  /**
   *  Unit Test
   */
  public static void main(String[] args) {
    OptionSet os = new OptionSet();
    os.add(new Option("ONE", "false", Option.BOOLEAN));
    os.add(new Option("TWO", "def", Option.STRING));
    os.configuresFrom("-ONE=true -TWO=\"toto titi\" file");
    System.out.println(os.toString());
  }
}