package sf.eclipse.javacc.options;

/**
 * An option is a name + type + value + default value.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class Option {

  // MMa : added description related field and method (for JTB cryptic options)
  // MMa 02/2010 : formatting and javadoc revision

  /** Integer option type */
  public static final int INT     = 1;
  /** Boolean option type */
  public static final int BOOLEAN = 2;
  /** String option type */
  public static final int STRING  = 3;
  /** File option type */
  public static final int FILE    = 4;
  /** Path option type */
  public static final int PATH    = 5;
  /** Target option type (unused) */
  public static final int TARGET  = 6;
  /** Void option type */
  public static final int VOID    = 7;
  /** The option type (as above) */
  protected int           type;
  /** The option name (no leading '-') */
  protected String        name;
  /** The option description */
  protected String        description;
  /** The option value (no extra added enclosing quotes) */
  protected String        value;
  /** The option defaultValue */
  protected String        defaultValue;

  /**
   * Constructor with a name, default value and type
   * 
   * @param aName the option name
   * @param aDefaultValue the option default value
   * @param aType the option type
   */
  Option(final String aName, final String aDefaultValue, final int aType) {
    name = aName;
    description = ""; //$NON-NLS-1$
    defaultValue = aDefaultValue;
    type = aType;
  }

  /**
   * Constructor with a name, description, default value and type
   * 
   * @param aName the option name
   * @param aDescription the option description
   * @param aDefaultValue the option default value
   * @param aType the option type
   */
  Option(final String aName, final String aDescription, final String aDefaultValue, final int aType) {
    name = aName;
    description = aDescription;
    defaultValue = aDefaultValue;
    type = aType;
  }

  /**
   * @return the type
   */
  int getType() {
    return type;
  }

  /**
   * @return the name.
   */
  String getName() {
    return name;
  }

  /**
   * @return the name and the description.
   */
  String getNameAndDescription() {
    if (description.length() == 0) {
      return name;
    }
    return name + " - " + description; //$NON-NLS-1$
  }

  /**
   * @return the value.
   */
  String getValue() {
    return value;
  }

  /**
   * Sets the value.
   * 
   * @param aValue the value to be set
   */
  void setValue(final String aValue) {
    if (aValue == null) {
      value = defaultValue;
    }
    else {
      value = aValue;
    }
  }

  /**
   * @return the default value.
   */
  String getDefaultValue() {
    return defaultValue;
  }
}
