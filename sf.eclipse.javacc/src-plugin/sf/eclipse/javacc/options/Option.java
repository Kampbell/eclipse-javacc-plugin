package sf.eclipse.javacc.options;

/**
 * An option is a name + type + value + default value
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class Option {
  public static final int INT = 1;
  public static final int BOOLEAN = 2;
  public static final int STRING = 3;
  public static final int FILE = 4;
  public static final int PATH = 5;
  public static final int TARGET = 6;
  public static final int VOID = 7;

  protected int type;
  protected String name;
  protected String value;
  protected String defaultValue;

  /**
   * A String Option 
   * @param name
   * @param default
   * @param type Option.STRING, BOOLEAN, FILE, PATH, TARGET
   */
  Option(String name, String defaultValue, int type) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.type = type;
  }

 /**
  * Returns type Option.STRING, INT, BOOLEAN, FILE, PATH, TARGET
  */
  int getType() {
    return type;
  }

 /**
  * Returns name
  */
  String getName() {
    return name;
  }

 /**
  * Returns value
  */
  String getValue() {
    return value;
  }

 /**
  * Sets value
  */
  void setValue(String value) {
    if (value == null) // || value.equals("")
      this.value = defaultValue;
    else
      this.value = value;
  }

 /**
  * Returns default value
  */
  String getDefaultValue() {
    return defaultValue;
  }
}
