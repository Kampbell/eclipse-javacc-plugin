package sf.eclipse.javacc.preferences;

import static sf.eclipse.javacc.preferences.IPrefConstants.*;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * The Preferences page class for the JavaCC Plugin Preferences.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.preferencePages">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class PreferencePage extends TabbedPreferencePage implements IWorkbenchPreferencePage {

  // MMa : added some colors and indentation preferences
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added groups and check spelling option
  // MMa 08/2011 : renamed
  // BF  05/2012 : changed logic to update preferences for opened editors
  // BF  05/2012 : added toggle buttons for Bold and Italic text attributes 
  // BF  05/2012 : moved constants to IPrefConstants 
  // BF  06/2012 : rewritten to use tabbed preference page, many new preferences
  // MMa 10/2012 : renamed

  /** Font attribute button labels */
  protected static final String[] sAtrLabels = {
      "", NLS_FONT_ATR_B, NLS_FONT_ATR_BI, NLS_FONT_ATR_I }; //$NON-NLS-1$

  /** Font attribute button preference values */
  protected static final String[] sAtrValues = {
      String.valueOf(SWT.NONE), //
      String.valueOf(SWT.BOLD), //
      String.valueOf((SWT.BOLD + SWT.ITALIC)), //
      String.valueOf(SWT.ITALIC)            };

  /** The do not show tool tips flag */
  protected final boolean         doNotShowToolTips;

  /**
   * Instantiates a new JavaCC preference page.
   */
  public PreferencePage() {
    super(GRID);
    setPreferenceStore(AbstractActivator.getDefault().getPreferenceStore());
    doNotShowToolTips = getPreferenceStore().getBoolean(P_NO_PREFERENCE_TOOL_TIPS);
    setDescription(NLS_TITLE_PREF_PAGE);
  }

  /** {@inheritDoc} */
  @Override
  public void createFieldEditors() {
    addJavaCCTab();
    addProductionsTab();
    addJavaTab();
    addOthersTab();

    endOfTabPages(P_APPLY_ON_TAB_SWITCH, NLS_LABEL_APPLY_ON_TAB_SWITCH, tt(NLS_TT_APPLY_ON_TAB_SWITCH));
    setApplyAndDefaultToolTips(tt(NLS_TT_BUTTON_APPLY), tt(NLS_TT_BUTTON_DEFAULTS));
  }

  //------------------------------- Tabs ---------------------------------------

  /**
   * Adds the JavaCC tab.
   */
  protected void addJavaCCTab() {
    addTab(NLS_TAB_JAVACC, tt(NLS_TT_TAB_JAVACC), getTabPageGridLayout(3));

    addJavaCCGroup();
    addJavaCCHeaderGroup();
    addJJTreeGroup();
  }

  /**
   * Adds the productions tab.
   */
  protected void addProductionsTab() {
    addTab(NLS_TAB_PRODUCTIONS, tt(NLS_TT_TAB_PRODUCTIONS), getTabPageGridLayout(3));

    addRegExGroup();
    addTokenLabelsGroup();
    addLexicalStateGroup();

    addBNFChoicesGroup();
    addJavaBlockBraceGroup();
  }

  /**
   * Adds the Java tab.
   */
  protected void addJavaTab() {
    addTab(NLS_TAB_JAVA, tt(NLS_TT_TAB_JAVA), getTabPageGridLayout(3));

    addJavaGroup();
    addCommentColorsGroup();
    addSpellCheckingGroup();
  }

  /**
   * Adds the others tab.
   */
  protected void addOthersTab() {
    addTab(NLS_TAB_OTHER, tt(NLS_TT_TAB_OTHER), getTabPageGridLayout(2));

    addMatchingCharGroup();
    addIndentationGroup();
    addHyperlinkGroup();
    addToolTipGroup();
    addConsoleCommandGroup();
  }

  //------------------------------- Groups -------------------------------------

  /**
   * Adds the JavaCC group and fields.
   */
  protected void addJavaCCGroup() {
    final Group group = getGroup(NLS_GROUP_JAVACC, tt(NLS_TT_GROUP_JAVACC), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_JAVACC_KEYWORD, NLS_LABEL_JAVACC_KEYWORD, //
                                    tt(NLS_TT_JAVACC_KEYWORD), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_KEYWORD_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_STRING, NLS_LABEL_JAVACC_STRING, //
                                    tt(NLS_TT_JAVACC_STRING), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_STRING_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_NUMERIC, NLS_LABEL_JAVACC_NUMERIC, //
                                    tt(NLS_TT_JAVACC_NUMERIC), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_NUMERIC_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_OTHER_PUNCT, NLS_LABEL_JAVACC_OTHER_PUNCT,
                                    tt(NLS_TT_JAVACC_OTHER_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_OTHER_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_DEFAULT_TEXT, NLS_LABEL_JAVACC_DEFAULT_TEXT, //
                                    tt(NLS_TT_JAVACC_DEFAULT_TEXT), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_DEFAULT_TEXT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_BACKGROUND,
                                    NLS_LABEL_JAVACC_BACKGROUND, //
                                    tt(NLS_TT_JAVACC_BACKGROUND), tt(NLS_TT_JAVACC_BACKGROUND_BUTTON),
                                    composite_1));

    //addField(new CycleButtonFieldEditor(null, null, null, null, composite_2));
  }

  /**
   * Adds the options group and fields.
   */
  protected void addJavaCCHeaderGroup() {
    final Group group = getGroup(NLS_GROUP_OPTIONS, tt(NLS_TT_GROUP_OPTIONS), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_JAVACC_OPTION, NLS_LABEL_JAVACC_OPTION, //
                                    tt(NLS_TT_JAVACC_OPTION), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_OPTION_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_OPTION_BRACE, NLS_LABEL_JAVACC_OPTION_BRACE, //
                                    tt(NLS_TT_JAVACC_OPTION_BRACE), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_OPTION_BRACE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_PARSER_NAME, //
                                    NLS_LABEL_JAVACC_PARSER_NAME, //
                                    tt(NLS_TT_JAVACC_PARSER_NAME), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_PARSER_NAME_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_PARSER_NAME_PAREN, NLS_LABEL_JAVACC_PARSER_NAME_PAREN, //
                                    tt(NLS_TT_JAVACC_PARSER_NAME_PAREN), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_PARSER_NAME_PAREN_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));
  }

  /**
   * Adds the JJTree node group and fields.
   */
  protected void addJJTreeGroup() {
    final Group group = getGroup(NLS_GROUP_JJTREE, tt(NLS_TT_GROUP_JJTREE), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_JJTREE_NODE_NAME_PUNCT, NLS_LABEL_JJTREE_NODE_NAME_PUNCT, //
                                    tt(NLS_TT_JJTREE_NODE_NAME_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_JJTREE_NODE_NAME_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JJTREE_NODE_NAME, NLS_LABEL_JJTREE_NODE_NAME, //
                                    tt(NLS_TT_JJTREE_NODE_NAME), composite_1));

    addField(new CycleButtonFieldEditor(P_JJTREE_NODE_NAME_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JJTREE_NODE_EXPR_PAREN, NLS_LABEL_JJTREE_NODE_EXPR_PAREN, //
                                    tt(NLS_TT_JJTREE_NODE_EXPR_PAREN), composite_1));

    addField(new CycleButtonFieldEditor(P_JJTREE_NODE_EXPR_PAREN_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

  }

  /**
   * Adds the regular expression group and fields.
   */
  protected void addRegExGroup() {
    final Group group = getGroup(NLS_GROUP_REGEX, tt(NLS_TT_GROUP_REGEX), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_REG_EX_BRACE, NLS_LABEL_REG_EX_BRACE, //
                                    tt(NLS_TT_REG_EX_BRACE), composite_1));

    addField(new CycleButtonFieldEditor(P_REG_EX_BRACE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_REG_EX_BRACKET, NLS_LABEL_REG_EX_BRACKET, //
                                    tt(NLS_TT_REG_EX_BRACKET), composite_1));

    addField(new CycleButtonFieldEditor(P_REG_EX_BRACKET_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_REG_EX_TOKEN_PUNCT, NLS_LABEL_REG_EX_TOKEN_PUNCT,
                                    tt(NLS_TT_REG_EX_TOKEN_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_REG_EX_TOKEN_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_REG_EX_CHOICE_PUNCT, NLS_LABEL_REG_EX_CHOICE_PUNCT,
                                    tt(NLS_TT_REG_EX_CHOICE_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_REG_EX_CHOICE_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_REG_EX_OTHER_PUNCT, NLS_LABEL_REG_EX_OTHER_PUNCT,
                                    tt(NLS_TT_REG_EX_OTHER_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_REG_EX_OTHER_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));
  }

  /**
   * Adds the token labels group and fields.
   */
  protected void addTokenLabelsGroup() {
    final Group group = getGroup(NLS_GROUP_LABELS, tt(NLS_TT_GROUP_LABELS), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_TOKEN_LABEL, NLS_LABEL_TOKEN_LABEL, //
                                    tt(NLS_TT_TOKEN_LABEL), composite_1));

    addField(new CycleButtonFieldEditor(P_TOKEN_LABEL_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_TOKEN_LABEL_PUNCT, NLS_LABEL_TOKEN_LABEL_PUNCT, //
                                    tt(NLS_TT_TOKEN_LABEL_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_TOKEN_LABEL_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_TOKEN_LABEL_DEF, NLS_LABEL_TOKEN_LABEL_DEF, //
                                    tt(NLS_TT_TOKEN_LABEL_DEF), composite_1));

    addField(new CycleButtonFieldEditor(P_TOKEN_LABEL_DEF_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_TOKEN_LABEL_PRIVATE_DEF_PUNCT, NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF_PUNCT, //
                                    tt(NLS_TT_TOKEN_LABEL_PRIVATE_DEF_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_TOKEN_LABEL_PRIVATE_DEF_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));
    addField(new ColorFieldEditorJJ(P_TOKEN_LABEL_PRIVATE_DEF, NLS_LABEL_TOKEN_LABEL_PRIVATE_DEF, //
                                    tt(NLS_TT_TOKEN_LABEL_PRIVATE_DEF), composite_1));

    addField(new CycleButtonFieldEditor(P_TOKEN_LABEL_PRIVATE_DEF_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

  }

  /**
   * Adds the lexical state group and fields.
   */
  protected void addLexicalStateGroup() {
    final Group group = getGroup(NLS_GROUP_STATES, tt(NLS_TT_GROUP_STATES), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_LEXICAL_STATE, NLS_LABEL_LEXICAL_STATE, //
                                    tt(NLS_TT_LEXICAL_STATE), composite_1));

    addField(new CycleButtonFieldEditor(P_LEXICAL_STATE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_LEXICAL_STATE_PUNCT, NLS_LABEL_LEXICAL_STATE_PUNCT, //
                                    tt(NLS_TT_LEXICAL_STATE_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_LEXICAL_STATE_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_LEXICAL_STATE_NEXT, NLS_LABEL_LEXICAL_STATE_NEXT, //
                                    tt(NLS_TT_LEXICAL_STATE_NEXT), composite_1));

    addField(new CycleButtonFieldEditor(P_LEXICAL_STATE_NEXT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));
  }

  /**
   * Adds the BNF choices group and fields.
   */
  protected void addBNFChoicesGroup() {
    final Group group = getGroup(NLS_GROUP_BNF_PROD, tt(NLS_TT_GROUP_BNF_PROD), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_JAVA_BLOCK_BRACE, NLS_LABEL_JAVA_BLOCK, //
                                    tt(NLS_TT_JAVA_BLOCK), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_BLOCK_BRACE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_EXPANSION_BRACE, NLS_LABEL_JAVACC_EXPANSION_BRACE, //
                                    tt(NLS_TT_JAVACC_EXPANSION_BRACE), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_EXPANSION_BRACE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVACC_CHOICE_PUNCT, NLS_LABEL_JAVACC_CHOICE_PUNCT, // 
                                    tt(NLS_TT_JAVACC_CHOICE_PUNCT), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVACC_CHOICE_PUNCT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

  }

  /**
   * Adds the Java block group and fields.
   */
  protected void addJavaBlockBraceGroup() {
    final Group group = getGroup(NLS_GROUP_JAVA_BLOCK, tt(NLS_TT_GROUP_JAVA_BLOCK), 1);
    final Composite composite_1 = getComposite(group);

    addField(new BooleanFieldEditorJJ(P_JAVA_BLOCK_BRACE_ALT_BG, NLS_LABEL_JAVA_BLOCK_BRACE_ALT_BG, //
                                      tt(NLS_TT_JAVA_BLOCK_BRACE_ALT_BG), // BooleanFieldEditor.SEPARATE_LABEL,
                                      composite_1));
  }

  /**
   * Adds the Java group and fields.
   */
  protected void addJavaGroup() {
    final Group group = getGroup(NLS_GROUP_JAVA, tt(NLS_TT_GROUP_JAVA), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_JAVA_KEYWORD, NLS_LABEL_JAVA_KEYWORD, //
                                    tt(NLS_TT_JAVA_KEYWORD), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_KEYWORD_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_IDENTIFIER, NLS_LABEL_JAVA_IDENTIFIER, //
                                    tt(NLS_TT_JAVA_IDENTIFIER), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_IDENTIFIER_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_STRING, NLS_LABEL_JAVA_STRING, //
                                    tt(NLS_TT_JAVA_STRING), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_STRING_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_NUMERIC, NLS_LABEL_JAVA_NUMERIC, //
                                    tt(NLS_TT_JAVA_NUMERIC), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_NUMERIC_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_PUNCTUATION, NLS_LABEL_JAVA_PUNCTUATION, //
                                    tt(NLS_TT_JAVA_PUNCTUATION), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_PUNCTUATION_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_DEFAULT_TEXT, NLS_LABEL_JAVA_DEFAULT_TEXT, //
                                    tt(NLS_TT_JAVA_DEFAULT_TEXT), composite_1));

    addField(new CycleButtonFieldEditor(P_JAVA_DEFAULT_TEXT_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_JAVA_BACKGROUND,
                                    NLS_LABEL_JAVA_BACKGROUND, //
                                    tt(NLS_TT_JAVA_BACKGROUND), tt(NLS_TT_JAVA_BACKGROUND_BUTTON),
                                    composite_1));

    addField(new CycleButtonFieldEditor(null, null, null, null, composite_2)); // not visible
  }

  /**
   * Adds the comments group and fields.
   */
  protected void addCommentColorsGroup() {
    final Group group = getGroup(NLS_GROUP_COMMENTS, tt(NLS_TT_GROUP_COMMENTS), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_COMMENT_BLOCK, NLS_LABEL_COMMENT_BLOCK, //
                                    tt(NLS_TT_COMMENT_BLOCK), composite_1));

    addField(new CycleButtonFieldEditor(P_COMMENT_BLOCK_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_COMMENT_LINE, NLS_LABEL_COMMENT_LINE, //
                                    tt(NLS_TT_COMMENT_LINE), composite_1));

    addField(new CycleButtonFieldEditor(P_COMMENT_LINE_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_COMMENT_JAVADOC, NLS_LABEL_COMMENT_JAVADOC, //
                                    tt(NLS_TT_COMMENT_JAVADOC), composite_1));

    addField(new CycleButtonFieldEditor(P_COMMENT_JAVADOC_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

    addField(new ColorFieldEditorJJ(P_COMMENT_BACKGROUND,
                                    NLS_LABEL_COMMENT_BACKGROUND, //
                                    tt(NLS_TT_COMMENT_BACKGROUND), tt(NLS_TT_COMMENT_BACKGROUND_BUTTON),
                                    composite_1));

    addField(new CycleButtonFieldEditor(null, null, null, null, composite_2)); // not visible
  }

  /**
   * Adds the comment spell checking group and field.
   */
  protected void addSpellCheckingGroup() {
    final Group group = getGroup(NLS_GROUP_SPELLING, tt(NLS_TT_GROUP_SPELLING), 1);
    final Composite composite = getComposite(group);

    addField(new BooleanFieldEditorJJ(P_NO_SPELL_CHECKING, NLS_LABEL_NO_SPELL_CHECKING, //
                                      tt(NLS_TT_NO_SPELL_CHECKING), composite));
  }

  /**
   * Adds the matching character group and field.
   */
  protected void addMatchingCharGroup() {
    final Group group = getGroup(NLS_GROUP_MATCHING, tt(NLS_TT_GROUP_MATCHING), 1);
    final Composite composite_1 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_MATCHING_CHAR, NLS_LABEL_MATCHING_CHAR, //
                                    tt(NLS_TT_MATCHING_CHAR), composite_1));
  }

  /**
   * Adds the indentation group and fields.
   */
  protected void addIndentationGroup() {
    final Group group = getGroup(NLS_GROUP_INDENTATION, tt(NLS_TT_GROUP_INDENTATION), 1);
    final Composite composite = getComposite(group);

    addField(new BooleanFieldEditorJJ(P_NO_ADV_AUTO_INDENT, NLS_LABEL_NO_ADV_AUTO_INDENT, //
                                      tt(NLS_TT_NO_ADV_AUTO_INDENT), composite));

    addField(new BooleanFieldEditorJJ(P_INDENT_CHAR, NLS_LABEL_INDENT_CHAR, //
                                      tt(NLS_TT_INDENT_CHAR), composite));

    final IntegerFieldEditor ife = new IntegerFieldEditor(P_INDENT_CHAR_NB, NLS_LABEL_INDENT_CHAR_NB, //
                                                          composite, 1);
    ife.setValidRange(1, 8);
    ife.getLabelControl(composite).setToolTipText(tt(NLS_TT_INDENT_CHAR_NB));
    addField(ife);
  }

  /**
   * Adds the console command group and fields.
   */
  protected void addConsoleCommandGroup() {
    final Group group = getGroup(NLS_GROUP_CONSOLE, tt(NLS_TT_GROUP_CONSOLE), 2);
    final Composite composite_1 = getComposite(group);
    final Composite composite_2 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_CONSOLE_COMMAND, NLS_LABEL_CONSOLE_COMMAND, //
                                    tt(NLS_TT_CONSOLE_COMMAND), composite_1));

    addField(new CycleButtonFieldEditor(P_CONSOLE_COMMAND_ATR, sAtrLabels, sAtrValues, //
                                        tt(NLS_TT_FONT_ATR), composite_2));

  }

  /**
   * Adds the hyperlink group and fields.
   */
  protected void addHyperlinkGroup() {
    final Group group = getGroup(NLS_GROUP_HYPERLINK, tt(NLS_TT_GROUP_HYPERLINK), 1);
    final Composite composite_1 = getComposite(group);

    addField(new ColorFieldEditorJJ(P_HYPERLINK_COLOR, NLS_HYPERLINK_COLOR, //
                                    tt(NLS_TT_HYPERLINK_COLOR), composite_1));

  }

  /**
   * Adds the no tool tip group and field.
   */
  protected void addToolTipGroup() {
    final Group group = getGroup(NLS_GROUP_TOOL_TIPS, tt(NLS_TT_GROUP_TOOL_TIPS), 1);
    final Composite composite = getComposite(group);

    addField(new BooleanFieldEditorJJ(P_NO_PREFERENCE_TOOL_TIPS, NLS_LABEL_NO_TOOL_TIPS, //
                                      NLS_TT_NO_TOOL_TIPS, composite));
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the supplied tool tip string, or a null string if tool tips are disabled.
   * 
   * @param toolTip - the tool tip text
   * @return the tool tip text or a null string if they are disabled
   */
  private String tt(final String toolTip) {
    return doNotShowToolTips ? "" : toolTip; //$NON-NLS-1$
  }

  /**
   * Gets the tab page grid layout.
   * 
   * @param columns - the number of columns on the tab page
   * @return the tab page grid layout
   */
  private static GridLayout getTabPageGridLayout(final int columns) {
    final GridLayout gridLayout = new GridLayout(columns, false);

    // allow more margin space, default is only 5 pixels
    gridLayout.marginHeight = 15;
    gridLayout.marginWidth = 15;
    gridLayout.horizontalSpacing = 15;
    gridLayout.verticalSpacing = 15;

    return gridLayout;
  }

  /**
   * Gets a group.
   * 
   * @param labelText - the label text
   * @param toolTipText - the tool tip text
   * @param columns - the number of columns
   * @return the group
   */
  private Group getGroup(final String labelText, final String toolTipText, final int columns) {
    final Group group = new Group(getFieldEditorParent(), SWT.NONE);
    group.setText(labelText);
    group.setToolTipText(toolTipText);
    group.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    group.setLayout(new GridLayout(columns, false));
    return group;
  }

  /**
   * Gets a composite inside a specified group.
   * 
   * @param group - the group containing the composite
   * @param columns - the number of columns in the composite
   * @return the composite
   */
  private static Composite getComposite(final Group group, final int columns) {
    final Composite composite = new Composite(group, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    composite.setLayout(new GridLayout(columns, false));
    return composite;
  }

  /**
   * Gets a composite inside a specified group.
   * 
   * @param group - the group containing the composite
   * @return the composite
   */
  private static Composite getComposite(final Group group) {
    return getComposite(group, 1);
  }

  /**
   * Updates spelling and colors on Apply or OK action.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean performOk() {
    final boolean result = super.performOk();
    updateSpellingAndColors();
    return result;
  }

  /**
   * Updates spelling and colors for all active JavaCC editors.
   */
  protected static void updateSpellingAndColors() {
    final IWorkbenchWindow window = AbstractActivator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        final IEditorReference[] editorReference = page.getEditorReferences();
        for (int i = 0; i < editorReference.length; i++) {
          final IEditorPart editorPart = editorReference[i].getEditor(false);
          if (editorPart instanceof JJEditor) {
            final JJEditor editor = (JJEditor) editorPart;
            editor.updateSpellingAndColors();
          }
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void init(final IWorkbench aWorkbench) {
    // required override, no action
  }
}
