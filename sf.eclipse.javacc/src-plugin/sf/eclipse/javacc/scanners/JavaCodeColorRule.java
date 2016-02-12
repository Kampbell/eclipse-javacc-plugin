package sf.eclipse.javacc.scanners;

import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Scanner rule for Java source code.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 * @author Marc Mazas 2014-2015-2016
 */
class JavaCodeColorRule implements IRule {

  // MMa 10/2012 : renamed
  // MMa 11/2014 : some renamings
  // MMa 02/2016 : some renamings ; renamed from JavaCodeRule

  /** The java keywords */
  public static final String[]         sJavaKeywords          = {
      // Keyword list from the Java Language Specification, Third Edition
      "abstract", //$NON-NLS-1$
      "assert", //$NON-NLS-1$
      "boolean", //$NON-NLS-1$
      "break", //$NON-NLS-1$
      "byte", //$NON-NLS-1$
      "case", //$NON-NLS-1$
      "catch", //$NON-NLS-1$
      "char", //$NON-NLS-1$
      "class", //$NON-NLS-1$
      "const", //$NON-NLS-1$
      "continue", //$NON-NLS-1$
      "default", //$NON-NLS-1$
      "do", //$NON-NLS-1$
      "double", //$NON-NLS-1$
      "else", //$NON-NLS-1$
      "enum", //$NON-NLS-1$
      "extends", //$NON-NLS-1$
      "final", //$NON-NLS-1$
      "finally", //$NON-NLS-1$
      "float", //$NON-NLS-1$
      "for", //$NON-NLS-1$
      "goto", //$NON-NLS-1$
      "if", //$NON-NLS-1$
      "implements", //$NON-NLS-1$
      "import", //$NON-NLS-1$
      "instanceof", //$NON-NLS-1$
      "int", //$NON-NLS-1$
      "interface", //$NON-NLS-1$
      "long", //$NON-NLS-1$
      "native", //$NON-NLS-1$
      "new", //$NON-NLS-1$
      "package", //$NON-NLS-1$
      "private", //$NON-NLS-1$
      "protected", //$NON-NLS-1$
      "public", //$NON-NLS-1$
      "return", //$NON-NLS-1$
      "short", //$NON-NLS-1$
      "static", //$NON-NLS-1$
      "strictfp", //$NON-NLS-1$      
      "super", //$NON-NLS-1$
      "switch", //$NON-NLS-1$
      "synchronized", //$NON-NLS-1$
      "this", //$NON-NLS-1$
      "throw", //$NON-NLS-1$
      "throws", //$NON-NLS-1$
      "transient", //$NON-NLS-1$
      "try", //$NON-NLS-1$
      "void", //$NON-NLS-1$
      "volatile", //$NON-NLS-1$
      "while", //$NON-NLS-1$   

      // primitive types and literals from the Java language specification
      "boolean", //$NON-NLS-1$
      "byte", //$NON-NLS-1$
      "char", //$NON-NLS-1$
      "double", //$NON-NLS-1$
      "false", //$NON-NLS-1$
      "float", //$NON-NLS-1$
      "int", //$NON-NLS-1$
      "long", //$NON-NLS-1$
      "null", //$NON-NLS-1$
      "short", //$NON-NLS-1$
      "true", //$NON-NLS-1$
      "void", //$NON-NLS-1$
                                                              };

  /** The java punctuation characters */
  public static final String[]         sJavaSpecialCharacters = {
      // Derived from the Java language specification
      "(", //$NON-NLS-1$
      ")", //$NON-NLS-1$
      "{", //$NON-NLS-1$
      "}", //$NON-NLS-1$
      "[", //$NON-NLS-1$
      "]", //$NON-NLS-1$
      ";", //$NON-NLS-1$
      ".", //$NON-NLS-1$
      "=", //$NON-NLS-1$
      "/", //$NON-NLS-1$
      "\\", //$NON-NLS-1$
      "+", //$NON-NLS-1$
      "-", //$NON-NLS-1$
      "*", //$NON-NLS-1$
      "<", //$NON-NLS-1$
      ">", //$NON-NLS-1$
      ":", //$NON-NLS-1$
      "?", //$NON-NLS-1$
      "!", //$NON-NLS-1$
      ",", //$NON-NLS-1$
      "|", //$NON-NLS-1$
      "&", //$NON-NLS-1$      
      "^", //$NON-NLS-1$
      "%", //$NON-NLS-1$
      "~", //$NON-NLS-1$   
                                                              };
  // Other ASCII uses in Java:  "$", "_" (letters), "'",  "\"" (strings) 
  // Other ASCII not used in Java:  "#", "@", "`" 

  /** The Java color preference names */
  protected static final String[]      sJavaColorPrefNames    = {
      // Order significant, matching pairs
      P_JAVA_KEYWORD, // 
      P_JAVA_IDENTIFIER, // 
      P_JAVA_STRING, //
      P_JAVA_NUMERIC, // 
      P_JAVA_PUNCTUATION, // 
      P_JAVA_DEFAULT_TEXT, //                                               
                                                              };

  /** The Java font attribute preference names */
  protected static final String[]      sJavaAtrPrefNames      = {
      // Order significant, matching pairs
      P_JAVA_KEYWORD_ATR, //
      P_JAVA_IDENTIFIER_ATR, // 
      P_JAVA_STRING_ATR, //
      P_JAVA_NUMERIC_ATR, // 
      P_JAVA_PUNCTUATION_ATR, // 
      P_JAVA_DEFAULT_TEXT_ATR, //                                               
                                                              };

  /** The color map */
  protected final Map<String, Color>   jColorMap;

  /** The attribute map */
  protected final Map<String, Integer> jAtrMap;

  /** The token map */
  protected final Map<String, IToken>  jTokenMap;

  /** The background color preference name */
  protected final String               jBGColorPrefName;

  //

  /** The Java keyword rule */
  protected WordRule                   jJavaKeywordRule;

  /** The punctuation rule */
  protected WordRule                   jPunctuationRule;

  /** The string rule */
  protected SingleLineRule             jStringRule;

  /** The character rule */
  protected SingleLineRule             jCharacterRule;

  /** The number rule */
  protected NumericLiteralRule         jNumberRule;

  /** The default rule */
  protected SimpleSequenceRule                 jDefaultRule;

  /** The whitespace rule */
  protected WhitespaceRule             jWhitespaceRule;

  /** The rules array */
  protected IRule[]                    jJavaRules;

  /**
   * Instantiates a new Java code rule.
   * 
   * @param aColorMap - the color map
   * @param aAtrMap - the attribute map
   * @param backgroundColorPrefName - the background color preference name
   */
  public JavaCodeColorRule(final Map<String, Color> aColorMap, final Map<String, Integer> aAtrMap,
                      final String backgroundColorPrefName) {

    jColorMap = aColorMap;
    jAtrMap = aAtrMap;
    jTokenMap = new HashMap<String, IToken>();

    jBGColorPrefName = backgroundColorPrefName;

    updateRules(null);
  }

  /**
   * @param aPreferenceName - the preference name that was updated
   */
  public void updateRules(final String aPreferenceName) {
    final boolean all = (aPreferenceName == null || aPreferenceName == jBGColorPrefName);
    String prefName = all ? jBGColorPrefName : aPreferenceName;
    final Color background = jColorMap.get(jBGColorPrefName);

    if (all) {
      jTokenMap.put(jBGColorPrefName, new Token(new TextAttribute(null, background, SWT.NONE)));
    }

    for (int i = 0; i < sJavaColorPrefNames.length; i++) {
      if (all || aPreferenceName == sJavaColorPrefNames[i] || aPreferenceName == sJavaAtrPrefNames[i]) {
        prefName = sJavaColorPrefNames[i];
        jTokenMap.put(prefName,
                      new Token(new TextAttribute(jColorMap.get(prefName), background,
                                                  jAtrMap.get(sJavaAtrPrefNames[i]).intValue())));
        if (!all) {
          break;
        }
      }
    }

    if (all || prefName == P_JAVA_KEYWORD || prefName == P_JAVA_IDENTIFIER) {
      jJavaKeywordRule = new WordRule(new WordDetector(), jTokenMap.get(P_JAVA_IDENTIFIER));
      for (final String key : sJavaKeywords) {
        jJavaKeywordRule.addWord(key, jTokenMap.get(P_JAVA_KEYWORD));
      }
    }

    if (all || prefName == P_JAVA_STRING) {
      jStringRule = new SingleLineRule("\"", "\"", jTokenMap.get(P_JAVA_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$
      jCharacterRule = new SingleLineRule("'", "'", jTokenMap.get(P_JAVA_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if (all || prefName == P_JAVA_NUMERIC) {
      jNumberRule = new NumericLiteralRule(jTokenMap.get(P_JAVA_NUMERIC));
    }

    if (all || prefName == P_JAVA_PUNCTUATION) {
      jPunctuationRule = new WordRule(new SingleCharDetector());
      for (final String sp : sJavaSpecialCharacters) {
        jPunctuationRule.addWord(sp, jTokenMap.get(P_JAVA_PUNCTUATION));
      }
    }

    if (all || prefName == P_JAVA_DEFAULT_TEXT) {
      jDefaultRule = new SimpleSequenceRule("", jTokenMap.get(P_JAVA_DEFAULT_TEXT)); //$NON-NLS-1$
    }

    if (all) {
      jWhitespaceRule = new WhitespaceRule(new WhitespaceDetector(), jTokenMap.get(jBGColorPrefName));
    }

    jJavaRules = new IRule[] {
        jWhitespaceRule, //
        jJavaKeywordRule, //
        jPunctuationRule, //
        jStringRule, //
        jCharacterRule, //
        jNumberRule, //
        jDefaultRule, //
    };
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner aScanner) {
    final CodeColorScanner fScanner = (CodeColorScanner) aScanner;

    return fScanner.nextToken(jJavaRules);
  }
}