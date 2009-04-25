package sf.eclipse.javacc.options;

/**
 * An option is a name + type + value + default value
 * 
 * @author Remi Koutcherawy 2003-2006 - CeCILL Licence http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
// ModMMa : added description related field and method (for JTB cryptic options)
public class Option {
  /** Integer option type */
  public static final int INT = 1;
  /** Bolean option type */
  public static final int BOOLEAN = 2;
  /** String option type */
  public static final int STRING = 3;
  /** File option type */
  public static final int FILE = 4;
  /** Path option type */
  public static final int PATH = 5;
  /** Target option type */
  public static final int TARGET = 6;
  /** Void option type */
  public static final int VOID = 7;

  protected int type;
  protected String name;
  protected String description;
  protected String value;
  protected String defaultValue;

  /**
   * Constructor with a name, default value and type
   */
  Option(String aName, String aDefaultValue, int aType) {
    name = aName;
    description = "";
    defaultValue = aDefaultValue;
    type = aType;
  }

  /**
   * Constructor with a name, description, default value and type
   */
  Option(String aName, String aDescription, String aDefaultValue, int aType) {
    name = aName;
    description = aDescription;
    defaultValue = aDefaultValue;
    type = aType;
  }

 /**
  * Returns the type.
  */
  int getType() {
    return type;
  }

  /**
   * Returns the name.
   */
   String getName() {
     return name;
   }

   /**
    * Returns the name and the description.
    */
    String getNameAndDescription() {
      if (description.length() == 0) {
        return name;
      }
      return name + " - " + description;
    }

 /**
  * Returns the value.
  */
  String getValue() {
    return value;
  }

 /**
  * Sets the value.
  */
  void setValue(String aValue) {
    if (aValue == null) // || value.equals("")
      value = defaultValue;
    else
      value = aValue;
  }

 /**
  * Returns the default value.
  */
  String getDefaultValue() {
    return defaultValue;
  }
}
