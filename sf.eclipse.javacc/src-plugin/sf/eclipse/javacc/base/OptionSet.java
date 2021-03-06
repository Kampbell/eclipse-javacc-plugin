package sf.eclipse.javacc.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A set of Options ; can parse options on a command line and generate one new set.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class OptionSet {

  // MMa 04/2009 : added Option description related method
  // MMa 02/2010 : formatting and javadoc revision ; fixed not stored Option.VOID properties issue
  // ... ....... : fixed display true cases for void options

  /** The list of options */
  protected List<Option> list;
  /** The "equals" flag : true if an '=' is needed, false otherwise */
  protected boolean      needsEqual;
  /** The command line target (after the options) */
  protected String       target;

  /**
   * Standard constructor.
   * 
   * @param aNeedsEqual - true if an '=' is needed, false otherwise
   */
  public OptionSet(final boolean aNeedsEqual) {
    list = new ArrayList<Option>();
    needsEqual = aNeedsEqual;
  }

  /**
   * @return the set of options as in a command line string
   */
  public String buildCmdLine() {
    final StringBuffer sb = new StringBuffer(32);
    final int len = list.size();
    for (int i = 0; i < len; i++) {
      final String val = getValue(i);
      final String defVal = getDefaultValue(i);
      final int type = getType(i);
      if (type == Option.VOID) {
        // special case for void options
        if (val.equals("true")) { //$NON-NLS-1$
          sb.append(sb.length() == 0 ? "-" : " -").append(getName(i)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        continue;
      }
      if (val.equals(defVal)) {
        // don't show anything for default values for non void options
        continue;
      }
      sb.append(sb.length() == 0 ? "-" : " -").append(getName(i)).append(needsEqual ? "=" : " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      if (val.indexOf(' ') != -1) {
        // add enclosing quotes if val contains one or more spaces
        sb.append("\"").append(val).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
      }
      else {
        sb.append(val);
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
   * @param aCmdLineArgs - the command line arguments string
   * @return an array of strings
   */
  public static String[] tokenize(final String aCmdLineArgs) {
    int count = 0;
    String[] arguments = new String[10];
    final StringTokenizer tokenizer = new StringTokenizer(aCmdLineArgs, " \"", true); //$NON-NLS-1$
    String token;
    boolean insideQuotes = false;
    boolean startNewToken = true;
    // takes care of quotes on the command line
    // '-xx aa -bb -cc'   ---> {"-xx aa", "-bb", "-cc"}
    // '-xx "aa -bb" -cc' ---> {"-xx aa -bb", "-cc"}
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
          // don't keep quote
          arguments[count++] = ""; //$NON-NLS-1$
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
   * @param aStr - the command line arguments
   */
  public void configuresFrom(final String aStr) {

    final int nb = list.size();

    // clears all void options
    for (int j = 0; j < nb; j++) {
      final Option opt = list.get(j);
      if (opt.getType() == Option.VOID) {
        opt.setValue("false"); //$NON-NLS-1$
      }
    }

    if (aStr == null) {
      // empty command line string
      return;
    }

    // parse the command line string
    final String[] tok = tokenize(aStr);
    Option lastOpt = null;
    for (int i = 0; i < tok.length; i++) {
      final String toki = tok[i];
      final boolean startsWithDash = toki.startsWith("-"); //$NON-NLS-1$
      final int indexOfEqual = toki.indexOf("="); //$NON-NLS-1$ 
      if (needsEqual) {
        // JavaCC / JJTree / JJDoc option : '-xx=yy' or '-xx = yy' or '-xx="yy zz"' or '-xx = "yy zz"'
        if (startsWithDash) {
          // '-xx'
          if (indexOfEqual != -1) {
            // first '-xx' of '-xx=yy' or '-xx="yy zz"', or first '-xx=' of '-xx= yy' or '-xx= "yy zz"'
            final String name = toki.substring(1, indexOfEqual);
            String value = toki.substring(indexOfEqual + 1);
            if (value == null || value.length() == 0) {
              continue;
            }
            value = stripEnclosingQuotes(value);
            for (int j = 0; j < nb; j++) {
              final Option opt = (list.get(j));
              if (opt.getName().equals(name)) {
                opt.setValue(value);
                lastOpt = null;
                break;
              }
            }
          }
          else {
            // first '-xx' of '-xx = yy' or '-xx = "yy zz"' or '-xx =yy' or '-xx ="yy zz"'
            final String name = toki.substring(1);
            for (int j = 0; j < nb; j++) {
              final Option opt = (list.get(j));
              if (opt.getName().equals(name)) {
                lastOpt = opt;
                break;
              }
            }
          }
        }
        // last 'yy' of '-xx= yy' or '-xx = yy', or first 'yy' of last "yy zz" of '-xx= "yy zz"' or "yy zz" of '-xx = "yy zz"'
        else if (indexOfEqual != -1) {
          final String value = stripEnclosingQuotes(toki.substring(1));
          if (lastOpt != null) {
            lastOpt.setValue(lastOpt.getValue() + value);
          }
        }
        else {
          final String value = stripEnclosingQuotes(toki);
          if (lastOpt != null) {
            lastOpt.setValue(lastOpt.getValue() + value);
          }
        }
      }
      else {
        // JTB option : '-xx' or '-xx yy' or '-xx "yy zz"'
        if (startsWithDash) {
          final String name = toki.substring(1);
          for (int j = 0; j < nb; j++) {
            final Option opt = list.get(j);
            if (opt.getName().equals(name)) {
              if (opt.getType() == Option.VOID) {
                opt.setValue("true"); //$NON-NLS-1$
                lastOpt = null;
              }
              else {
                lastOpt = opt;
              }
              break;
            }
          }
        }
        else if (lastOpt != null) {
          String value = toki;
          value = stripEnclosingQuotes(value);
          lastOpt.setValue(value);
        }
        else {
          target = toki;
        }
      }
    }
  }

  /**
   * Strips enclosing quotes.
   * 
   * @param aValue - the string
   * @return the string with the enclosing quotes stripped, or the string itself
   */
  private static String stripEnclosingQuotes(final String aValue) {
    String value = aValue;
    final int len = value.length() - 1;
    if ((len > 0) && (value.charAt(0) == '"') && (value.charAt(len) == '"')) {
      value = value.substring(1, len - 1);
    }
    return value;
  }

  /**
   * Resets all options to their default values.
   */
  public void resetToDefaultValues() {
    final int len = list.size();
    for (int i = 0; i < len; i++) {
      final Option opt = getOption(i);
      opt.setValue(opt.getDefaultValue());
    }
    target = null;
  }

  /**
   * Adds an Option.
   * 
   * @param aOption - the option
   */
  public final void add(final Option aOption) {
    list.add(aOption);
  }

  /**
   * @param aIndex - the option index
   * @return the option
   */
  public final Option getOption(final int aIndex) {
    return list.get(aIndex);
  }

  /**
   * @return the total number of options
   */
  public final int getOptionsSize() {
    return list.size();
  }

  /**
   * @param aType - the given type
   * @return the number of options of the given type
   */
  public int getOptionsSize(final int aType) {
    int n = 0;
    final Iterator<Option> it = list.iterator();
    while (it.hasNext()) {
      if (it.next().getType() == aType) {
        n++;
      }
    }
    return n;
  }

  /**
   * @param aIndex - the option index
   * @return the option type
   */
  public final int getType(final int aIndex) {
    return getOption(aIndex).getType();
  }

  /**
   * @param aIndex - the option index
   * @return the option name
   */
  public final String getName(final int aIndex) {
    return getOption(aIndex).getName();
  }

  /**
   * @param aIndex - the option index
   * @return the option name and description
   */
  public final String getNameAndDescription(final int aIndex) {
    return getOption(aIndex).getNameAndDescription();
  }

  /**
   * @param aIndex - the option index
   * @return the option value (enclosed in extra quotes if contains one or more spaces)
   */
  public String getValueInQuotes(final int aIndex) {
    String val = getOption(aIndex).getValue();
    if (val.indexOf(' ') != -1 /*&& !val.startsWith("\"")*/) {
      val = "\"" + val + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    }
    return val;
  }

  /**
   * @param aIndex - the option index
   * @return the option value (no quotes added)
   */
  public final String getValue(final int aIndex) {
    return getOption(aIndex).getValue();
  }

  /**
   * @param aIndex - the option index
   * @return the option default value
   */
  public final String getDefaultValue(final int aIndex) {
    return getOption(aIndex).getDefaultValue();
  }
}