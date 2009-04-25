package sf.eclipse.javacc.options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A set of Options can parse a command line and generate one.
 * 
 * @author Marc Mazas 2009
 */
// ModMMa : added Option description related method
public class OptionSet {
  protected ArrayList<Option> list;
  protected String            target;

  /**
   * Constructor OptionSet.
   */
  public OptionSet() {
    list = new ArrayList<Option>();
  }

  /**
   * @return the set of options as in a command line
   */
  public String toString() {
    final StringBuffer sb = new StringBuffer(32);
    for (int i = 0; i < list.size(); i++) {
      final String val = getValue(i);
      final String defVal = getDefaultValue(i);
      final int type = getType(i);
      if (val.equals(defVal))
        continue;
      if (sb.length() != 0)
        sb.append(" "); //$NON-NLS-1$
      sb.append("-").append(getName(i)); //$NON-NLS-1$
      if (type != Option.VOID)
        sb.append("=").append(val); //$NON-NLS-1$
    }
    if (target != null)
      sb.append(" ").append(target); //$NON-NLS-1$
    return sb.toString();
  }

  /**
   * Splits the command line arguments string into an array of strings
   * 
   * @param cmdLineArgs the command line arguments string
   * @return an array of strings
   */
  public static String[] tokenize(String cmdLineArgs) {
    int count = 0;
    String[] arguments = new String[10];
    StringTokenizer tokenizer = new StringTokenizer(cmdLineArgs, " \"", true); //$NON-NLS-1$
    String token;
    boolean insideQuotes = false;
    boolean startNewToken = true;
    // takes care of quotes on the command line
    // '-xxx "aaa -bbb" -ccc' ---> {"-xxx aaa -bbb","-ccc"}
    // '-xxx aaa -bbb -ccc' ---> {"-xxx aaa","-bbb","-ccc"}
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (token.equals(" ")) { //$NON-NLS-1$
        if (insideQuotes) {
          arguments[count - 1] += token;
          startNewToken = false;
        } else {
          startNewToken = true;
        }
      } else if (token.equals("\"")) { //$NON-NLS-1$
        if (!insideQuotes && startNewToken) {
          if (count == arguments.length)
            System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
          arguments[count++] = ""; //$NON-NLS-1$
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
              System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
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
   * Configures from a command line
   * 
   * @param str the command line arguments
   */
  public void configuresFrom(String str) {
    if (str == null) {
      resetToDefaultValues();
      return;
    }
    String[] tok = tokenize(str);
    for (int i = 0; i < tok.length; i++) {
      // Standard option ie -xxx=yyy
      if (tok[i].startsWith("-") && tok[i].indexOf("=") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
        String name = tok[i].substring(1, tok[i].indexOf('='));
        String value = tok[i].substring(tok[i].indexOf('=') + 1);
        if (value.length() == 0)
          continue;
        for (int j = 0; j < list.size(); j++) {
          Option opt = (list.get(j));
          if (opt.getName().equals(name)) {
            opt.setValue(value);
            break;
          }
        }
      }
      // Void option ie -xxx
      else if (tok[i].startsWith("-")) { //$NON-NLS-1$
        String name = tok[i].substring(1);
        String value = "true"; //$NON-NLS-1$
        for (int j = 0; j < list.size(); j++) {
          Option opt = list.get(j);
          if (opt.getName().equals(name)) {
            opt.setValue(value);
            break;
          }
        }
      }
      // Maybe target argument, ie file.jj or file.jjt
      else target = tok[i];
    }
  }

  /**
   * Reset all options to default values.
   */
  public void resetToDefaultValues() {
    Iterator<Option> it = list.iterator();
    while (it.hasNext()) {
      Option opt = it.next();
      opt.setValue(opt.getDefaultValue());
    }
  }

  /**
   * Adds an Option.
   * 
   * @param option the option
   */
  public void add(Option option) {
    list.add(option);
  }

  /**
   * @param i the option index
   * @return the option
   */
  public Option getOption(int i) {
    return list.get(i);
  }

  /**
   * @return the total number of options.
   */
  public int getOptionsSize() {
    return list.size();
  }

  /**
   * @param type the given type
   * @return the number of options of the given type
   */
  public int getOptionsSize(int type) {
    int n = 0;
    Iterator<Option> it = list.iterator();
    while (it.hasNext())
      if (it.next().getType() == type)
        n++;
    return n;
  }

  /**
   * @param i the option index
   * @return the option type
   */
  public int getType(int i) {
    return getOption(i).getType();
  }

  /**
   * @param i the option index
   * @return the option name
   */
  public String getName(int i) {
    return getOption(i).getName();
  }

  /**
   * @param i the option index
   * @return the option name and description
   */
  public String getNameAndDescription(int i) {
    return getOption(i).getNameAndDescription();
  }

  /**
   * @param i the option index
   * @return the option value enclosed in quotes
   */
  public String getValue(int i) {
    String val = getOption(i).getValue();
    if (val.indexOf(' ') != -1 && !val.startsWith("\"")) //$NON-NLS-1$
      val = "\"" + val + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    return val;
  }

  /**
   * @param i the option index
   * @return the option value
   */
  public String getValueNoQuotes(int i) {
    return getOption(i).getValue();
  }

  /**
   * @param i the option index
   * @return the option default value
   */
  public String getDefaultValue(int i) {
    return getOption(i).getDefaultValue();
  }
}