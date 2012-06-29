package sf.eclipse.javacc.scanners;

import java.util.HashMap;

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

import sf.eclipse.javacc.preferences.IPrefConstants;

/**
 * Scanner rule for Java source code.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJJavaCodeRule implements IRule, IPrefConstants {

  /** The java keywords */
  public static final String[]           sJavaKeywords          = {
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
  public static final String[]           sJavaSpecialCharacters = {
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
  private static final String[]          javaColorPrefNames     = {
      // Order significant, matching pairs
      P_JAVA_KEYWORD, // 
      P_JAVA_IDENTIFIER, // 
      P_JAVA_STRING, //
      P_JAVA_NUMERIC, // 
      P_JAVA_PUNCTUATION, // 
      P_JAVA_DEFAULT_TEXT, //                                               
                                                                };

  /** The Java font attribute preference names */
  private static final String[]          javaAtrPrefNames       = {
      // Order significant, matching pairs
      P_JAVA_KEYWORD_ATR, //
      P_JAVA_IDENTIFIER_ATR, // 
      P_JAVA_STRING_ATR, //
      P_JAVA_NUMERIC_ATR, // 
      P_JAVA_PUNCTUATION_ATR, // 
      P_JAVA_DEFAULT_TEXT_ATR, //                                               
                                                                };

  /** The color map */
  private final HashMap<String, Color>   fColorMap;

  /** The attribute map */
  private final HashMap<String, Integer> fAtrMap;

  /** The token map */
  private final HashMap<String, IToken>  fTokenMap;

  /** The background color preference name */
  private final String                   fBGColorPrefName;

  //

  /** The Java keyword rule */
  private WordRule                       javaKeywordRule;

  /** The punctuation rule */
  private WordRule                       punctuationRule;

  /** The string rule */
  private SingleLineRule                 stringRule;

  /** The character rule */
  private SingleLineRule                 characterRule;

  /** The number rule */
  private JJNumericLiteralRule           numberRule;

  /** The default rule */
  private JJSimpleRule                   defaultRule;

  /** The whitespace rule */
  private WhitespaceRule                 whitespaceRule;

  /** The rules array */
  private IRule[]                        javaRules;

  /**
   * Instantiates a new Java code rule.
   * 
   * @param colorMap - the color map
   * @param atrMap - the attribute map
   * @param backgroundColorPrefName - the background color preference name
   */
  public JJJavaCodeRule(final HashMap<String, Color> colorMap, final HashMap<String, Integer> atrMap,
                        final String backgroundColorPrefName) {

    fColorMap = colorMap;
    fAtrMap = atrMap;
    fTokenMap = new HashMap<String, IToken>();

    fBGColorPrefName = backgroundColorPrefName;

    updateRules(null);
  }

  /**
   * @param preferenceName - the preference name that was updated
   */
  public void updateRules(final String preferenceName) {
    final boolean all = (preferenceName == null || preferenceName == fBGColorPrefName);
    String prefName = all ? fBGColorPrefName : preferenceName;
    final Color background = fColorMap.get(fBGColorPrefName);

    if (all) {
      fTokenMap.put(fBGColorPrefName, new Token(new TextAttribute(null, background, SWT.NONE)));
    }

    for (int i = 0; i < javaColorPrefNames.length; i++) {
      if (all || preferenceName == javaColorPrefNames[i] || preferenceName == javaAtrPrefNames[i]) {
        prefName = javaColorPrefNames[i];
        fTokenMap.put(prefName,
                      new Token(new TextAttribute(fColorMap.get(prefName), background,
                                                  fAtrMap.get(javaAtrPrefNames[i]).intValue())));
        if (!all) {
          break;
        }
      }
    }

    if (all || prefName == P_JAVA_KEYWORD || prefName == P_JAVA_IDENTIFIER) {
      javaKeywordRule = new WordRule(new JJWordDetector(), fTokenMap.get(P_JAVA_IDENTIFIER));
      for (final String key : sJavaKeywords) {
        javaKeywordRule.addWord(key, fTokenMap.get(P_JAVA_KEYWORD));
      }
    }

    if (all || prefName == P_JAVA_STRING) {
      stringRule = new SingleLineRule("\"", "\"", fTokenMap.get(P_JAVA_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$
      characterRule = new SingleLineRule("'", "'", fTokenMap.get(P_JAVA_STRING), '\\'); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if (all || prefName == P_JAVA_NUMERIC) {
      numberRule = new JJNumericLiteralRule(fTokenMap.get(P_JAVA_NUMERIC));
    }

    if (all || prefName == P_JAVA_PUNCTUATION) {
      punctuationRule = new WordRule(new JJSingleCharDetector());
      for (final String sp : sJavaSpecialCharacters) {
        punctuationRule.addWord(sp, fTokenMap.get(P_JAVA_PUNCTUATION));
      }
    }

    if (all || prefName == P_JAVA_DEFAULT_TEXT) {
      defaultRule = new JJSimpleRule(null, fTokenMap.get(P_JAVA_DEFAULT_TEXT));
    }

    if (all) {
      whitespaceRule = new WhitespaceRule(new JJWhitespaceDetector(), fTokenMap.get(fBGColorPrefName));
    }

    javaRules = new IRule[] {
        whitespaceRule, //
        javaKeywordRule, //
        punctuationRule, //
        stringRule, //
        characterRule, //
        numberRule, //
        defaultRule, //
    };
  }

  /** {@inheritDoc} */
  @Override
  public IToken evaluate(final ICharacterScanner scanner) {
    final JJCodeScanner fScanner = (JJCodeScanner) scanner;

    return fScanner.nextToken(javaRules);
  }
}