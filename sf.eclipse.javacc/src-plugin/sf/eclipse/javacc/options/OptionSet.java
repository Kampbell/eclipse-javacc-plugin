package sf.eclipse.javacc.options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A set of Options ; can parse options on a command line and generate one new set.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class OptionSet {

  // MMa 04/2009 : added Option description related method
  // MMa 02/2010 : formatting and javadoc revision ; fixed not stored Option.VOID properties issue

  /** The list of options */
  protected ArrayList<Option> fList;
  /** The command line target (after the options) */
  protected String            fTarget;

  /**
   * Standard constructor.
   */
  public OptionSet() {
    fList = new ArrayList<Option>();
  }

  /**
   * @return the set of options as in a command line string.
   */
  public String buildCmdLine() {
    final StringBuffer sb = new StringBuffer(32);
    final int len = fList.size();
    for (int i = 0; i < len; i++) {
      final String val = getValue(i);
      final String defVal = getDefaultValue(i);
      final int type = getType(i);
      if (val.equals(defVal)) {
        continue;
      }
      if (sb.length() != 0) {
        sb.append(" "); //$NON-NLS-1$
      }
      sb.append("-").append(getName(i)); //$NON-NLS-1$
      if (type != Option.VOID) {
        if (val.indexOf(' ') != -1) {
          // add enclosing quotes if val contains one or more spaces
          sb.append("=\"").append(val).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
          sb.append("=").append(val); //$NON-NLS-1$
        }
      }
    }
    // no need in preference pages
    //    if (fTarget != null) {
    //      sb.append(" ").append(fTarget); //$NON-NLS-1$
    //    }
    return sb.toString();
  }

  /**
   * Splits the command line arguments string into an array of strings.
   * 
   * @param cmdLineArgs the command line arguments string
   * @return an array of strings
   */
  public static String[] tokenize(final String cmdLineArgs) {
    int count = 0;
    String[] arguments = new String[10];
    final StringTokenizer tokenizer = new StringTokenizer(cmdLineArgs, " \"", true); //$NON-NLS-1$
    String token;
    boolean insideQuotes = false;
    boolean startNewToken = true;
    // takes care of quotes on the command line
    // '-xxx aaa -bbb -ccc'   ---> {"-xxx aaa", "-bbb", "-ccc"}
    // '-xxx "aaa -bbb" -ccc' ---> {"-xxx aaa -bbb", "-ccc"}
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (token.equals(" ")) { //$NON-NLS-1$
        if (insideQuotes) {
          arguments[count - 1] += token;
          startNewToken = false;
        }
        else {
          startNewToken = true;
        }
      }
      else if (token.equals("\"")) { //$NON-NLS-1$
        if (!insideQuotes && startNewToken) {
          if (count == arguments.length) {
            System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
          }
          // keep quotes
          arguments[count++] = "\""; //$NON-NLS-1$
        }
        insideQuotes = !insideQuotes;
        startNewToken = false;
      }
      else {
        if (insideQuotes) {
          arguments[count - 1] += token;
        }
        else {
          if (token.length() > 0 && !startNewToken) {
            arguments[count - 1] += token;
          }
          else {
            if (count == arguments.length) {
              System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
            }
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
   * Configures from a command line.
   * 
   * @param str the command line arguments
   */
  public void configuresFrom(final String str) {
    resetToDefaultValues();
    if (str == null) {
      return;
    }
    final String[] tok = tokenize(str);
    for (int i = 0; i < tok.length; i++) {
      final String toki = tok[i];
      final boolean startsWithDash = toki.startsWith("-"); //$NON-NLS-1$
      final int indexOfEqual = toki.indexOf("="); //$NON-NLS-1$ 
      // Standard option : -xxx=yyy or -xxx="yyy zzz" (tokenize does not remove enclosing quotes)
      if (startsWithDash && indexOfEqual != -1) {
        final String name = toki.substring(1, indexOfEqual);
        String value = toki.substring(indexOfEqual + 1);
        if (value == null || value.length() == 0) {
          continue;
        }
        // strip enclosing quotes
        final int len = value.length() - 1;
        if ((len > 0) && (value.charAt(0) == '"') && (value.charAt(len) == '"')) {
          value = value.substring(1, len - 1);
        }
        for (int j = 0; j < fList.size(); j++) {
          final Option opt = (fList.get(j));
          if (opt.getName().equals(name)) {
            opt.setValue(value);
            break;
          }
        }
      }
      // Void option : -xxx
      else if (startsWithDash) {
        final String name = toki.substring(1);
        for (int j = 0; j < fList.size(); j++) {
          final Option opt = fList.get(j);
          if (opt.getName().equals(name)) {
            final String defVal = opt.getDefaultValue();
            if ("true".equals(defVal)) { //$NON-NLS-1$
              opt.setValue("false"); //$NON-NLS-1$
            }
            else {
              opt.setValue("true"); //$NON-NLS-1$
            }
            break;
          }
        }
      }
      else {
        fTarget = toki;
      }
    }
  }

  /**
   * Resets all options to their default values.
   */
  public void resetToDefaultValues() {
    final int len = fList.size();
    for (int i = 0; i < len; i++) {
      final Option opt = getOption(i);
      opt.setValue(opt.getDefaultValue());
    }
    fTarget = null;
  }

  /**
   * Adds an Option.
   * 
   * @param option the option
   */
  public void add(final Option option) {
    fList.add(option);
  }

  /**
   * @param i the option index
   * @return the option
   */
  public Option getOption(final int i) {
    return fList.get(i);
  }

  /**
   * @return the total number of options.
   */
  public int getOptionsSize() {
    return fList.size();
  }

  /**
   * @param type the given type
   * @return the number of options of the given type
   */
  public int getOptionsSize(final int type) {
    int n = 0;
    final Iterator<Option> it = fList.iterator();
    while (it.hasNext()) {
      if (it.next().getType() == type) {
        n++;
      }
    }
    return n;
  }

  /**
   * @param i the option index
   * @return the option type
   */
  public int getType(final int i) {
    return getOption(i).getType();
  }

  /**
   * @param i the option index
   * @return the option name
   */
  public String getName(final int i) {
    return getOption(i).getName();
  }

  /**
   * @param i the option index
   * @return the option name and description
   */
  public String getNameAndDescription(final int i) {
    return getOption(i).getNameAndDescription();
  }

  /**
   * @param i the option index
   * @return the option value (enclosed in extra quotes if contains one or more spaces)
   */
  public String getValueInQuotes(final int i) {
    String val = getOption(i).getValue();
    if (val.indexOf(' ') != -1 /*&& !val.startsWith("\"")*/) {
      val = "\"" + val + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    }
    return val;
  }

  /**
   * @param i the option index
   * @return the option value (no quotes added)
   */
  public String getValue(final int i) {
    return getOption(i).getValue();
  }

  /**
   * @param i the option index
   * @return the option default value
   */
  public String getDefaultValue(final int i) {
    return getOption(i).getDefaultValue();
  }
}