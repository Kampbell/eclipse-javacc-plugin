package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * The content assist processor for completions JavaCC and inner Java code (not in Java & Javadoc comments).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class CompletionProcessor implements IContentAssistProcessor {

  // MMa 02/2010 : formatting and javadoc revision
  // BF  05/2012 : rename and change location of JavaCC keyword table
  // MMa 10/2012 : fixed cursor position bug, added proposals sorting, revised list of proposals,
  //               used static import ; adapted to modifications in grammar nodes ; renamed
  // MMa 11/2014 : added some final modifiers, added OUTPUT_LANGUAGE option
  // MMa 12/2014 : improved top level proposals
  // MMa 11/2015 : added spaces to tokens on insertion through completion proposal

  // Note keywords could be centralized with options
  // see "http://www.realsolve.co.uk/site/tech/jface-text.php"

  /** Comparator */
  private final CompletionProposalComparator jCPC = new CompletionProposalComparator();

  /** {@inheritDoc} */
  @Override
  public ICompletionProposal[] computeCompletionProposals(final ITextViewer aTextViewer, final int aDocOffset) {

    // get the editor showing the active document
    JJEditor jjeditor = null;
    final IDocument currentDocument = aTextViewer.getDocument();
    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    final IEditorReference editorReferences[] = window.getActivePage().getEditorReferences();
    for (int i = 0; i < editorReferences.length; i++) {
      final IEditorPart editor = editorReferences[i].getEditor(false); // don't create!
      if (editor instanceof JJEditor) {
        jjeditor = (JJEditor) editor;
        final IEditorInput input = jjeditor.getEditorInput();
        final IDocument doc = jjeditor.getDocumentProvider().getDocument(input);
        if (currentDocument.equals(doc)) {
          // we got the current JJEditor for the current Document
          break;
        }
      }
    }
    if (jjeditor == null) {
      // should not occur
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.Null_problem")); //$NON-NLS-1$ 
      return null;
    }

    // get the document extension
    final IEditorInput input = jjeditor.getEditorInput();
    final IResource res = (IResource) input.getAdapter(IResource.class);
    if (res == null) {
      // should not occur
      return null;
    }
    final String ext = res.getFullPath().getFileExtension();
    if (!"jj".equals(ext) && !"jjt".equals(ext) && !"jtb".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // should not occur
      return null;
    }

    // compute the completion proposal place (start, length)
    final String text = aTextViewer.getDocument().get();
    int start = 0;
    int length = 0;
    final List<String> props;
    final List<Integer> curPos;
    int line = 0;
    int column = 0;
    String[] caProps = null;
    int[] caCurPos = null;

    if (text.length() == 0) {
      // empty text : only top level keywords
      caProps = sJjCATopKwProps;
      caCurPos = sJjCATopKwCurPos;
      props = new ArrayList<String>(10);
      curPos = new ArrayList<Integer>(10);
      for (int i = 0; i < caProps.length; i++) {
        final String str = caProps[i];
        props.add(str);
        curPos.add(new Integer(caCurPos[i]));
      }
    }
    else {
      // non empty text
      start = aDocOffset - 1;
      char ch = text.charAt(start);
      // move back until non whitespace or punctuation
      while (!Character.isWhitespace(ch) && ch != '}' && ch != '{' && ch != ')' && ch != '(' && ch != ']'
             && ch != '[' && ch != '|' && ch != '>' && ch != ';' && ch != '*' && ch != '+' && ch != '?'
             && ch != ':' && ch != '!' && ch != '%') {
        start--;
        if (start < 0) {
          break;
        }
        ch = text.charAt(start);
      }
      start++;
      final String prefix = text.substring(start, aDocOffset).toUpperCase();
      boolean isTokenPrefix = text.length() <= start ? false : text.charAt(start) == '<';
      final boolean isAnglePrefix = "<".equals(prefix); //$NON-NLS-1$
      // special case when typed '<' has been transformed automatically to '< '
      if (start >= 2 && text.charAt(start - 2) == '<' && text.charAt(start - 1) == ' ') {
        start = start - 2;
        isTokenPrefix = true;
      }
      length = aDocOffset - start;
      try {
        line = currentDocument.getLineOfOffset(aDocOffset);
        int len = 0;
        for (int l = 0; l < line; l++) {
          len += currentDocument.getLineLength(l);
        }
        column = aDocOffset - len;
      } catch (final BadLocationException e) {
        // should not occur
        AbstractActivator.logBug(e, aDocOffset);
      }
      final Elements jElements = jjeditor.getElements();
      final int topNodeId = jElements.getEnclosingNodeId(line, column);
      // choose the proposals depending on the file extension and the location in the document
      props = new ArrayList<String>(30);
      curPos = new ArrayList<Integer>(30);
      // first the options and language keywords
      switch (topNodeId) {
        case JJTVOID:
          caProps = computeTopKwProps(jElements);
          caCurPos = computeTopKwCurProps(jElements);
          break;
        case JJTJAVACC_OPTIONS:
          if ("jj".equals(ext)) { //$NON-NLS-1$
            caProps = sJjCAOptProps;
            caCurPos = sJjCAOptCurPos;
          }
          else if ("jjt".equals(ext)) { //$NON-NLS-1$
            caProps = sJjtCAOptProps;
            caCurPos = sJjtCAOptCurPos;
          }
          else {
            caProps = sJtbCAOptProps;
            caCurPos = sJtbCAOptCurPos;
          }
          break;
        case JJTJAVACODE_BLOCK:
        case JJTBNF_PROD_JAVA_BLOCK:
        case JJTTOKEN_MANAGER_DECLS:
          // although legal, not very useful, so commented out
          //      caOptKwProps = sJjCAJavaKwProps;
          //      caOptKwCurPos = sJjCAJavaKwCurPos;
          break;
        case JJTBNF_PROD_EXP_BLOCK:
          caProps = sJjCAExpKwProps;
          caCurPos = sJjCAExpKwCurPos;
          break;
        case JJTREG_EXPR_PROD_BLOCK:
          caProps = sJjCARegKwProps;
          caCurPos = sJjCARegKwCurPos;
          break;
        default:
          break;
      }
      // add the previous grammar elements (options and language keywords)
      if (!isTokenPrefix && caProps != null && caCurPos != null) {
        for (int i = 0; i < caProps.length; i++) {
          final String str = caProps[i];
          if (str.startsWith(prefix)) {
            props.add(str);
            curPos.add(new Integer(caCurPos[i]));
          }
        }
      }
      // now the user grammar elements
      switch (topNodeId) {
        case JJTJAVACC_OPTIONS:
          addTrueFalse(props, curPos, prefix);
          break;
        case JJTPARSER_BEGIN:
        case JJTJAVACODE_BLOCK:
        case JJTBNF_PROD_JAVA_BLOCK:
        case JJTTOKEN_MANAGER_DECLS:
        case JJTREGEXPR_SPEC_JAVA_BLOCK:
        case JJTEXP_UNIT_JAVA_BLOCK:
          addCommonJavaIdentifiers(props, curPos, prefix);
          break;
        default:
          break;
      }
      final Set<String> jElementsSet = jElements.getCompPropsIdentMap().keySet();
      switch (topNodeId) {
        case JJTPARSER_BEGIN:
        case JJTJAVACODE_BLOCK:
        case JJTBNF_PROD_JAVA_BLOCK:
        case JJTTOKEN_MANAGER_DECLS:
        case JJTREGEXPR_SPEC_JAVA_BLOCK:
        case JJTEXP_UNIT_JAVA_BLOCK:
          for (final String str : jElementsSet) {
            final int id = jElements.getCompProps(str).getId();
            addMethod(prefix, isTokenPrefix, props, curPos, str, id);
            // although legal, not that much useful, and adds a lot of noise in the proposals
            //        addTokenLabelAsKind(prefix, isTokenPrefix, isAnglePrefix, props, curPos, str, id);
          }
          break;
        case JJTBNF_PROD_EXP_BLOCK:
          for (final String str : jElementsSet) {
            final int id = jElements.getCompProps(str).getId();
            addBNFProductionOrMethod(prefix, isTokenPrefix, props, curPos, str, id);
            addTokenLabel(prefix, isTokenPrefix, isAnglePrefix, props, curPos, str, id);
          }
          break;
        case JJTREG_EXPR_PROD_BLOCK:
          for (final String str : jElementsSet) {
            final int id = jElements.getCompProps(str).getId();
            addTokenLabel(prefix, isTokenPrefix, isAnglePrefix, props, curPos, str, id);
          }
          break;
        default:
          break;
      }
    }
    // build the list of completion proposals
    final List<ICompletionProposal> compProps = new ArrayList<ICompletionProposal>(curPos.size());
    // add all suggestions to proposals
    final Iterator<Integer> curPosIter = curPos.iterator();
    for (final Iterator<String> propsIter = props.iterator(); propsIter.hasNext();) {
      final String txt = propsIter.next();
      final int pos = curPosIter.next().intValue();
      if (txt.length() > 0) {
        if (pos != 0) {
          compProps.add(new CompletionProposal(txt, start, length, pos));
        }
        else {
          compProps.add(new CompletionProposal(txt, start, length, txt.length()));
        }
      }
    }
    // sort proposals
    final ICompletionProposal[] arr = compProps.toArray(new ICompletionProposal[compProps.size()]);
    Arrays.sort(arr, jCPC);
    return arr;
  }

  /**
   * @param aJElements - the nodes
   * @return the images of the proposals
   */
  private static String[] computeTopKwProps(final Elements aJElements) {
    int len = JjTopKwCA.values().length;
    if (aJElements.isOptionsThere) {
      len--;
    }
    if (aJElements.isParserBeginThere) {
      //      len = len - 2;
      len--;
    }
    int k = 0;
    final String[] caProps = new String[len];
    for (final JjTopKwCA v : JjTopKwCA.values()) {
      if (v == JjTopKwCA.OPTIONS && aJElements.isOptionsThere) {
        continue;
      }
      if (v == JjTopKwCA.PARSER_BEGIN && aJElements.isParserBeginThere) {
        continue;
      }
      //      if (v == JjTopKwCA.PARSER_END && jElements.isParserBeginThere) {
      //        continue;
      //      }
      caProps[k++] = v.disp;
    }
    return caProps;
  }

  /**
   * @param aJElements - the nodes
   * @return the cursor positions of the proposals
   */
  private static int[] computeTopKwCurProps(final Elements aJElements) {
    int len = JjTopKwCA.values().length;
    if (aJElements.isOptionsThere) {
      len--;
    }
    if (aJElements.isParserBeginThere) {
      //      len = len - 2;
      len--;
    }
    int k = 0;
    final int[] caCurPos = new int[len];
    for (final JjTopKwCA v : JjTopKwCA.values()) {
      if (v == JjTopKwCA.OPTIONS && aJElements.isOptionsThere) {
        continue;
      }
      if (v == JjTopKwCA.PARSER_BEGIN && aJElements.isParserBeginThere) {
        continue;
      }
      //      if (v == JjTopKwCA.PARSER_END && jElements.isParserBeginThere) {
      //        continue;
      //      }
      caCurPos[k++] = v.curPos;
    }
    return caCurPos;
  }

  /**
   * Adds a token label to the proposals.
   * 
   * @param prefix - the prefix
   * @param isTokenPrefix - the flag is a token prefix
   * @param isAnglePrefix - the flag is a short token prefix
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param str - the element
   * @param id - the node id
   */
  private static void addTokenLabel(final String prefix, final boolean isTokenPrefix,
                                    final boolean isAnglePrefix, final List<String> props,
                                    final List<Integer> curPos, final String str, final int id) {
    // tokens
    if (id == JJTIDENT_REG_EXPR_LABEL || id == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      if (isAnglePrefix || (isTokenPrefix && str.toUpperCase().startsWith(prefix))) {
        props.add("< " + str + " > "); //$NON-NLS-1$ //$NON-NLS-2$
        curPos.add(new Integer(str.length() + 5));
      }
    }
  }

  /**
   * Adds a token label as the kind indicator to the proposals.
   * 
   * @param prefix - the prefix
   * @param isTokenPrefix - the flag is a token prefix
   * @param isAnglePrefix - the flag is a short token prefix
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param str - the element
   * @param id - the node id
   */
  @SuppressWarnings("unused")
  private static void addTokenLabelAsKind(final String prefix, final boolean isTokenPrefix,
                                          final boolean isAnglePrefix, final List<String> props,
                                          final List<Integer> curPos, final String str, final int id) {
    // tokens
    if (id == JJTIDENT_REG_EXPR_LABEL || id == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      if (isAnglePrefix || (isTokenPrefix && str.toUpperCase().startsWith(prefix))) {
        props.add(str + " "); //$NON-NLS-1$
        curPos.add(new Integer(str.length() + 1));
      }
    }
  }

  /**
   * Adds a method to the proposals.
   * 
   * @param prefix - the prefix
   * @param isTokenPrefix - the flag is a token prefix
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param str - the element
   * @param id - the node id
   */
  private static void addMethod(final String prefix, final boolean isTokenPrefix, final List<String> props,
                                final List<Integer> curPos, final String str, final int id) {
    if (!isTokenPrefix) {
      // nodes and methods
      if (id == JJTMETHODDECL) {
        if (str.toUpperCase().startsWith(prefix)) {
          props.add(str + "() "); //$NON-NLS-1$
          curPos.add(new Integer(str.length() + 3));
        }
      }
    }
  }

  /**
   * Adds a bnf production or method to the proposals.
   * 
   * @param prefix - the prefix
   * @param isTokenPrefix - the flag is a token prefix
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param str - the element
   * @param id - the node id
   */
  private static void addBNFProductionOrMethod(final String prefix, final boolean isTokenPrefix,
                                               final List<String> props, final List<Integer> curPos,
                                               final String str, final int id) {
    if (!isTokenPrefix) {
      // nodes and methods
      if (id == JJTBNF_PROD || id == JJTMETHODDECL) {
        if (str.toUpperCase().startsWith(prefix)) {
          props.add(str + "() "); //$NON-NLS-1$
          curPos.add(new Integer(str.length() + 3));
        }
      }
    }
  }

  /**
   * Adds common java identifiers (true, false, boolean, return, String, Token) to the proposals.
   * 
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param prefix - the prefix
   */
  private static void addCommonJavaIdentifiers(final List<String> props, final List<Integer> curPos,
                                               final String prefix) {
    addTrueFalse(props, curPos, prefix);
    /* keywords */
    if ("BOOLEAN".startsWith(prefix)) { //$NON-NLS-1$
      props.add("boolean "); //$NON-NLS-1$
      curPos.add(new Integer("boolean ".length())); //$NON-NLS-1$
    }
    if ("FINAL".startsWith(prefix)) { //$NON-NLS-1$
      props.add("final "); //$NON-NLS-1$
      curPos.add(new Integer("final ".length())); //$NON-NLS-1$
    }
    if ("PRIVATE".startsWith(prefix)) { //$NON-NLS-1$
      props.add("private "); //$NON-NLS-1$
      curPos.add(new Integer("private ".length())); //$NON-NLS-1$
    }
    if ("PROTECTED".startsWith(prefix)) { //$NON-NLS-1$
      props.add("protected "); //$NON-NLS-1$
      curPos.add(new Integer("protected ".length())); //$NON-NLS-1$
    }
    if ("PUBLIC".startsWith(prefix)) { //$NON-NLS-1$
      props.add("public "); //$NON-NLS-1$
      curPos.add(new Integer("public ".length())); //$NON-NLS-1$
    }
    if ("RETURN".startsWith(prefix)) { //$NON-NLS-1$
      props.add("return "); //$NON-NLS-1$
      curPos.add(new Integer("return ".length())); //$NON-NLS-1$
    }
    if ("STATIC".startsWith(prefix)) { //$NON-NLS-1$
      props.add("static "); //$NON-NLS-1$
      curPos.add(new Integer("static ".length())); //$NON-NLS-1$
    }
    if ("THIS".startsWith(prefix)) { //$NON-NLS-1$
      props.add("this"); //$NON-NLS-1$
      curPos.add(new Integer("this".length())); //$NON-NLS-1$
    }
    /* classes and fields */
    if ("ARRAYLIST".startsWith(prefix)) { //$NON-NLS-1$
      props.add("ArrayList<> "); //$NON-NLS-1$
      curPos.add(new Integer("ArrayList<> ".length() - 2)); //$NON-NLS-1$
    }
    if ("HASHMAP".startsWith(prefix)) { //$NON-NLS-1$
      props.add("HashMap<,> "); //$NON-NLS-1$
      curPos.add(new Integer("HashMap<,> ".length() - 3)); //$NON-NLS-1$
    }
    if ("HASHTABLE".startsWith(prefix)) { //$NON-NLS-1$
      props.add("Hashtable<,> "); //$NON-NLS-1$
      curPos.add(new Integer("Hashtable<,> ".length() - 3)); //$NON-NLS-1$
    }
    if ("IMAGE".startsWith(prefix)) { //$NON-NLS-1$
      props.add("image"); //$NON-NLS-1$
      curPos.add(new Integer("image".length())); //$NON-NLS-1$
    }
    if ("INTEGER".startsWith(prefix)) { //$NON-NLS-1$
      props.add("Integer "); //$NON-NLS-1$
      curPos.add(new Integer("Integer ".length())); //$NON-NLS-1$
    }
    if ("LIST".startsWith(prefix)) { //$NON-NLS-1$
      props.add("List<> "); //$NON-NLS-1$
      curPos.add(new Integer("List<> ".length() - 2)); //$NON-NLS-1$
    }
    if ("MAP".startsWith(prefix)) { //$NON-NLS-1$
      props.add("Map<,> "); //$NON-NLS-1$
      curPos.add(new Integer("Map<,> ".length() - 3)); //$NON-NLS-1$
    }
    if ("STRING".startsWith(prefix)) { //$NON-NLS-1$
      props.add("String "); //$NON-NLS-1$
      curPos.add(new Integer("String ".length())); //$NON-NLS-1$
    }
    if ("TOKEN".startsWith(prefix)) { //$NON-NLS-1$
      props.add("Token "); //$NON-NLS-1$
      curPos.add(new Integer("Token ".length())); //$NON-NLS-1$
    }
  }

  /**
   * Adds true and false and return to the proposals.
   * 
   * @param props - the proposals
   * @param curPos - the cursor position
   * @param prefix - the prefix
   */
  private static void addTrueFalse(final List<String> props, final List<Integer> curPos, final String prefix) {
    if ("TRUE".startsWith(prefix)) { //$NON-NLS-1$
      props.add("true"); //$NON-NLS-1$
      curPos.add(new Integer("true".length())); //$NON-NLS-1$
    }
    if ("FALSE".startsWith(prefix)) { //$NON-NLS-1$
      props.add("false"); //$NON-NLS-1$
      curPos.add(new Integer("false".length())); //$NON-NLS-1$
    }
  }

  /** {@inheritDoc} */
  @Override
  public final IContextInformation[] computeContextInformation(@SuppressWarnings("unused") final ITextViewer aTextViewer,
                                                               @SuppressWarnings("unused") final int aOffset) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public final char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] {
      ' ' };
  }

  /** {@inheritDoc} */
  @Override
  public final char[] getContextInformationAutoActivationCharacters() {
    return new char[] {
      '?' };
  }

  /** {@inheritDoc} */
  @Override
  public final IContextInformationValidator getContextInformationValidator() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public final String getErrorMessage() {
    return null;
  }

  /*
   * Comparator
   */

  /**
   * Comparator for ICompletionProposal.
   */
  class CompletionProposalComparator implements Comparator<ICompletionProposal> {

    /** {@inheritDoc} */
    @Override
    public int compare(final ICompletionProposal o1, final ICompletionProposal o2) {

      final String kw1 = o1.getDisplayString();
      final String kw2 = o2.getDisplayString();
      return kw1.compareTo(kw2);
    }
  }

  /*
   * Enumerations
   */

  /**
   * Enumeration of the JavaCC Options for completion proposals.
   */
  enum JjOptCA {
    /** BUILD_PARSER JavaCC option */
    BUILD_PARSER("BUILD_PARSER", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** BUILD_TOKEN_MANAGER JavaCC option */
    BUILD_TOKEN_MANAGER("BUILD_TOKEN_MANAGER", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** CACHE_TOKENS JavaCC option */
    CACHE_TOKENS("CACHE_TOKENS", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** CHOICE_AMBIGUITY_CHECK JavaCC option */
    CHOICE_AMBIGUITY_CHECK("CHOICE_AMBIGUITY_CHECK", false, "3", "(default 2)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** COMMON_TOKEN_ACTION JavaCC option */
    COMMON_TOKEN_ACTION("COMMON_TOKEN_ACTION", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** DEBUG_LOOKAHEAD JavaCC option */
    DEBUG_LOOKAHEAD("DEBUG_LOOKAHEAD", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** DEBUG_PARSER JavaCC option */
    DEBUG_PARSER("DEBUG_PARSER", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** DEBUG_TOKEN_MANAGER JavaCC option */
    DEBUG_TOKEN_MANAGER("DEBUG_TOKEN_MANAGER", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** ERROR_REPORTING JavaCC option */
    ERROR_REPORTING("ERROR_REPORTING", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** FORCE_LA_CHECK JavaCC option */
    FORCE_LA_CHECK("FORCE_LA_CHECK", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** GENERATE_ANNOTATIONS JavaCC option (undocumented) */
    GENERATE_ANNOTATIONS("GENERATE_ANNOTATIONS", true, "false", "(default true ; undocumented)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** GENERATE_CHAINED_EXCEPTION JavaCC option (undocumented) */
    GENERATE_CHAINED_EXCEPTION("GENERATE_CHAINED_EXCEPTION", true, "false", "(default true ; undocumented)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** GENERATE_GENERICS JavaCC option (undocumented) */
    GENERATE_GENERICS("GENERATE_GENERICS", true, "false", "(default true ; undocumented)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** GENERATE_STRING_BUILDER JavaCC option (undocumented) */
    GENERATE_STRING_BUILDER("GENERATE_STRING_BUILDER", true, "false", "(default true ; undocumented)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** GRAMMAR_ENCODING JavaCC option */
    GRAMMAR_ENCODING("GRAMMAR_ENCODING", false, "???", "(default file.encoding)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** IGNORE_CASE JavaCC option */
    IGNORE_CASE("IGNORE_CASE", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** JAVA_UNICODE_ESCAPE JavaCC option */
    JAVA_UNICODE_ESCAPE("JAVA_UNICODE_ESCAPE", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** JDK_VERSION JavaCC option */
    JDK_VERSION("JDK_VERSION", false, "1.4", "(default 1.5 - needs javacc.jar v4.x)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** KEEP_LINE_COLUMN JavaCC option (undocumented) */
    KEEP_LINE_COLUMN("KEEP_LINE_COLUMN", true, "false", "(default true ; undocumented)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** LOOKAHEAD JavaCC option */
    LOOKAHEAD("LOOKAHEAD", false, "2", "(default 1)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** OTHER_AMBIGUITY_CHECK JavaCC option */
    OTHER_AMBIGUITY_CHECK("OTHER_AMBIGUITY_CHECK", false, "2", "(default 1)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** OUTPUT_DIRECTORY JavaCC option */
    OUTPUT_DIRECTORY("OUTPUT_DIRECTORY", false, "???", "(default current directory)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** OUTPUT_LANGUAGE JavaCC option */
    OUTPUT_LANGUAGE("OUTPUT_LANGUAGE", false, "C++", "(default \"java\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** SANITY_CHECK JavaCC option */
    SANITY_CHECK("SANITY_CHECK", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** STATIC JavaCC option */
    STATIC("STATIC", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** SUPPORT_CLASS_VISIBILITY_PUBLIC JavaCC option */
    SUPPORT_CLASS_VISIBILITY_PUBLIC("SUPPORT_CLASS_VISIBILITY_PUBLIC", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TOKEN_EXTENDS JavaCC option */
    TOKEN_EXTENDS("TOKEN_EXTENDS", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TOKEN_FACTORY JavaCC option */
    TOKEN_FACTORY("TOKEN_FACTORY", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TOKEN_MANAGER_USES_PARSER JavaCC option */
    TOKEN_MANAGER_USES_PARSER("TOKEN_MANAGER_USES_PARSER", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** UNICODE_INPUT JavaCC option */
    UNICODE_INPUT("UNICODE_INPUT", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** USER_CHAR_STREAM JavaCC option */
    USER_CHAR_STREAM("USER_CHAR_STREAM", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** USER_TOKEN_MANAGER JavaCC option */
    USER_TOKEN_MANAGER("USER_TOKEN_MANAGER", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param isBoolean - true if a boolean option, false otherwise (a string option)
     * @param aVal - the value
     * @param aCmt - a comment
     */
    JjOptCA(final String aKw, final boolean isBoolean, final String aVal, final String aCmt) {
      kw = aKw;
      if (isBoolean) {
        final String tmp = aKw + " = "; //$NON-NLS-1$
        curPos = tmp.length();
        disp = tmp + aVal + "; // " + aCmt; //$NON-NLS-1$
      }
      else {
        final String tmp = aKw + " = \""; //$NON-NLS-1$
        curPos = tmp.length();
        disp = tmp + aVal + "\"; // " + aCmt; //$NON-NLS-1$
      }
    }

  } // end JjOptCA enum

  /**
   * Enumeration of the JJTree Options for completion proposals.
   */
  enum JjtOptCA {
    /** BUILD_NODE_FILES JJTree option */
    BUILD_NODE_FILES("BUILD_NODE_FILES", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** JJTREE_OUTPUT_DIRECTORY JJTree option */
    JJTREE_OUTPUT_DIRECTORY(
                            "JJTREE_OUTPUT_DIRECTORY", false, "???", "(default use value of OUTPUT_DIRECTORY)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** MULTI JJTree option */
    MULTI("MULTI", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_CLASS JJTree option */
    NODE_CLASS("NODE_CLASS", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_DEFAULT_VOID JJTree option */
    NODE_DEFAULT_VOID("NODE_DEFAULT_VOID", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_EXTENDS JJTree option */
    NODE_EXTENDS("NODE_EXTENDS", false, "???", "(default \"\") (Deprecated)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_FACTORY JJTree option */
    NODE_FACTORY("NODE_FACTORY", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_PACKAGE JJTree option */
    NODE_PACKAGE("NODE_PACKAGE", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_PREFIX JJTree option */
    NODE_PREFIX("NODE_PREFIX", false, "???", "(default \"AST\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_SCOPE_HOOK JJTree option */
    NODE_SCOPE_HOOK("NODE_SCOPE_HOOK", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** NODE_USES_PARSER JJTree option */
    NODE_USES_PARSER("NODE_USES_PARSER", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** OUTPUT_FILE JJTree option */
    OUTPUT_FILE("OUTPUT_FILE", false, "???", "(undocumented"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$   undocumented
    //    /** OUTPUT_LANGUAGE JJTree option */
    //    OUTPUT_LANGUAGE("OUTPUT_LANGUAGE", false, "C++", "(default \"java\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** STATIC JJTree option */
    STATIC("STATIC", true, "false", "(default true)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TRACK_TOKENS JJTree option */
    TRACK_TOKENS("TRACK_TOKENS", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** VISITOR JJTree option */
    VISITOR("VISITOR", true, "true", "(default false)"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** VISITOR_DATA_TYPE JJTree option */
    VISITOR_DATA_TYPE("VISITOR_DATA_TYPE", false, "???", "(default \"Object\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** VISITOR_EXCEPTION JJTree option */
    VISITOR_EXCEPTION("VISITOR_EXCEPTION", false, "???", "(default \"\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** VISITOR_RETURN_TYPE JJTree option */
    VISITOR_RETURN_TYPE("VISITOR_RETURN_TYPE", false, "???", "(default \"Object\")"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param isBoolean - true if a boolean option, false otherwise (a string option)
     * @param aVal - the value
     * @param aCmt - a comment
     */
    JjtOptCA(final String aKw, final boolean isBoolean, final String aVal, final String aCmt) {
      kw = aKw;
      if (isBoolean) {
        final String tmp = aKw + " = "; //$NON-NLS-1$
        curPos = tmp.length();
        disp = tmp + aVal + "; // " + aCmt; //$NON-NLS-1$
      }
      else {
        final String tmp = aKw + " = \""; //$NON-NLS-1$
        curPos = tmp.length();
        disp = tmp + aVal + "\"; // " + aCmt; //$NON-NLS-1$
      }
    }

  } // end JjtOptCA enum

  /**
   * Enumeration of the JTB Options for completion proposals.
   */
  enum JtbOptCA {
    /** JTB_CL JTB option */
    JTB_CL("JTB_CL", true, "classes list (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_D JTB option */
    JTB_D("JTB_D", false, "nodes and visitors directory"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_DL JTB option */
    JTB_DL("JTB_DL", true, "depth level (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_E JTB option */
    JTB_E("JTB_E", true, "suppress semantic check (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_F JTB option */
    JTB_F("JTB_F", true, "descriptive fields (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_IA JTB option */
    JTB_IA("JTB_IA", true, "inline accepts (default true)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_JD JTB option */
    JTB_JD("JTB_JD", true, "javadoc comments (default true)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_ND JTB option */
    JTB_ND("JTB_ND", false, "nodes directory (default syntaxtree)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_NP JTB option */
    JTB_NP("JTB_NP", false, "nodes package (default syntaxtree)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_NPFX JTB option */
    JTB_NPFX("JTB_NPFX", false, "nodes prefix"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_NSFX JTB option */
    JTB_NSFX("JTB_NSFX", false, "nodes suffix"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_NS JTB option */
    JTB_NS("JTB_NS", false, "nodes superclass"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_O JTB option */
    JTB_O("JTB_O", false, "out file (default jtb.out.jj)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_P JTB option */
    JTB_P("JTB_P", false, "nodes and visitors package"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_PP JTB option */
    JTB_PP("JTB_PP", true, "parent pointers (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_PRINTER JTB option */
    JTB_PRINTER(
                "JTB_PRINTER", true, "dumper & formatter (default false) (TreeFormatter.java not overwritten!)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_SCHEME JTB option */
    JTB_SCHEME("JTB_SCHEME", true, "scheme (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_TK JTB option */
    JTB_TK("JTB_TK", true, "special tokens (default true)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_VA JTB option */
    JTB_VA("JTB_VA", true, "variable arguments (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_VD JTB option */
    JTB_VD("JTB_VD", false, "visitors directory"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_VP JTB option */
    JTB_VP("JTB_VP", false, "visitors package"), //$NON-NLS-1$ //$NON-NLS-2$
    /** JTB_W JTB option */
    JTB_W("JTB_W", true, "no overwrite (default false)"), //$NON-NLS-1$ //$NON-NLS-2$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param isVoid - true if a void option (boolean), false otherwise (a string or int option)
     * @param aCmt - a comment
     */
    JtbOptCA(final String aKw, final boolean isVoid, final String aCmt) {
      kw = aKw;
      if (isVoid) {
        disp = aKw + " = ; // " + aCmt; //$NON-NLS-1$
        curPos = aKw.length() + " = ".length(); //$NON-NLS-1$
      }
      else {
        disp = aKw + " = \"\"; // " + aCmt; //$NON-NLS-1$
        curPos = aKw.length() + " = \"".length(); //$NON-NLS-1$
      }
    }

  } // end JtbOptCA enum

  /**
   * Enumeration of the JavaCC keywords + "options" allowed at the top level (for completion proposals).
   */
  enum JjTopKwCA {
    /** options JavaCC word */
    OPTIONS("options", "options {\n  \n}\n", "options {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** JAVACODE JavaCC keyword */
    JAVACODE("JAVACODE", "JAVACODE {\n  \n}\n", "JAVACODE ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** MORE JavaCC keyword */
    MORE("MORE", "MORE : {\n  \n}\n", "MORE : {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** PARSER_BEGIN JavaCC keyword */
    PARSER_BEGIN(
                 "PARSER_BEGIN", "PARSER_BEGIN()\n\nPARSER_END()\n\n", "PARSER_BEGIN()\n\nPARSER_END(".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //    /** PARSER_END JavaCC keyword */
    //    PARSER_END("PARSER_END", "PARSER_END()\n\n", "PARSER_END(".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** SKIP JavaCC keyword */
    SKIP("SKIP", "SKIP : {\n  \n}\n", "SKIP : {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** SPECIAL_TOKEN JavaCC keyword */
    SPECIAL_TOKEN("SPECIAL_TOKEN", "SPECIAL_TOKEN : {\n  \n}\n", "SPECIAL_TOKEN : {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TOKEN JavaCC keyword */
    TOKEN("TOKEN", "TOKEN : {\n  \n}\n", "TOKEN : {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** TOKEN_MGR_DECLS JavaCC keyword */
    TOKEN_MGR_DECLS("TOKEN_MGR_DECLS", "TOKEN_MGR_DECLS : {\n  \n}\n", "TOKEN_MGR_DECLS : {\n  ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** void Java keyword */
    VOID("void", "void ", "void ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param aDisp - the display string of the content assist proposal
     * @param aCurPos - cursor position for the content assist proposal
     */
    JjTopKwCA(final String aKw, final String aDisp, final int aCurPos) {
      kw = aKw;
      disp = aDisp;
      curPos = aCurPos;
    }

  } // end JjTopKwCA enum

  /**
   * Enumeration of the JavaCC keywords allowed in expansions only (for completion proposals).
   */
  enum JjExpKwCA {
    /** EOF JavaCC keyword, to be used as a token */
    EOF("EOF", "< EOF > ", "< EOF > ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    /** LOOKAHEAD JavaCC keyword */
    LOOKAHEAD("LOOKAHEAD", "LOOKAHEAD() ", "LOOKAHEAD(".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param aDisp - the display string of the content assist proposal
     * @param aCurPos - cursor position for the content assist proposal
     */
    JjExpKwCA(final String aKw, final String aDisp, final int aCurPos) {
      kw = aKw;
      disp = aDisp;
      curPos = aCurPos;
    }

  } // end JjExpKwCA enum

  /**
   * Enumeration of the JavaCC keywords allowed in regular expressions only (for completion proposals).
   */
  enum JjRegKwCA {
    /** EOF JavaCC keyword, to be used as a token */
    EOF("EOF", "< EOF > ", "< EOF > ".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param aDisp - the display string of the content assist proposal
     * @param aCurPos - cursor position for the content assist proposal
     */
    JjRegKwCA(final String aKw, final String aDisp, final int aCurPos) {
      kw = aKw;
      disp = aDisp;
      curPos = aCurPos;
    }

  } // end JjRegKwCA enum

  /**
   * Enumeration of the JavaCC keywords allowed in java code (for completion proposals).
   */
  enum JjJavaKwCA {
    /** EOF JavaCC keyword, to be used as a token.kind */
    EOF("EOF", "EOF", "EOF".length()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ;

    /** The keyword */
    protected String kw;
    /** The display string of the content assist proposal */
    protected String disp;
    /** The cursor position for the content assist proposal */
    protected int    curPos;

    /**
     * Constructor.
     * 
     * @param aKw - the keyword
     * @param aDisp - the display string of the content assist proposal
     * @param aCurPos - cursor position for the content assist proposal
     */
    JjJavaKwCA(final String aKw, final String aDisp, final int aCurPos) {
      kw = aKw;
      disp = aDisp;
      curPos = aCurPos;
    }

  } // end JjJavaKwCA enum

  /*
   * Static arrays
   */

  /** The options proposals for a .jj file */
  protected static String[] sJjCAOptProps;
  /** The options cursor positions for a .jj file */
  protected static int[]    sJjCAOptCurPos;
  // initialize both above
  static {
    final int jjlen = JjOptCA.values().length;
    int k = 0;
    sJjCAOptProps = new String[jjlen];
    sJjCAOptCurPos = new int[jjlen];
    for (final JjOptCA v : JjOptCA.values()) {
      sJjCAOptProps[k] = v.disp;
      sJjCAOptCurPos[k++] = v.curPos;
    }
  }

  /** The options proposals for a .jjt file */
  protected static String[] sJjtCAOptProps;
  /** The options cursor positions for a .jjt file */
  protected static int[]    sJjtCAOptCurPos;
  // initialize both above
  static {
    final int jjlen = JjOptCA.values().length;
    final int jjtlen = JjtOptCA.values().length;
    int k = 0;
    sJjtCAOptProps = new String[jjlen + jjtlen];
    sJjtCAOptCurPos = new int[jjlen + jjtlen];
    for (final JjOptCA v : JjOptCA.values()) {
      sJjtCAOptProps[k] = v.disp;
      sJjtCAOptCurPos[k++] = v.curPos;
    }
    for (final JjtOptCA v : JjtOptCA.values()) {
      sJjtCAOptProps[k] = v.disp;
      sJjtCAOptCurPos[k++] = v.curPos;
    }
  }

  /** The options proposals for a .jtb file */
  protected static String[] sJtbCAOptProps;
  /** The options cursor positions options for a .jtb file */
  protected static int[]    sJtbCAOptCurPos;
  // initialize both above
  static {
    final int jjlen = JjOptCA.values().length;
    final int jtblen = JtbOptCA.values().length;
    int k = 0;
    sJtbCAOptProps = new String[jjlen + jtblen];
    sJtbCAOptCurPos = new int[jjlen + jtblen];
    for (final JjOptCA v : JjOptCA.values()) {
      sJtbCAOptProps[k] = v.disp;
      sJtbCAOptCurPos[k++] = v.curPos;
    }
    for (final JtbOptCA v : JtbOptCA.values()) {
      sJtbCAOptProps[k] = v.disp;
      sJtbCAOptCurPos[k++] = v.curPos;
    }
  }

  /** The top keywords proposals file */
  protected static String[] sJjCATopKwProps;
  /** The options cursor positions options for a .jj file */
  protected static int[]    sJjCATopKwCurPos;
  // initialize both above
  static {
    final int len = JjTopKwCA.values().length;
    int k = 0;
    sJjCATopKwProps = new String[len];
    sJjCATopKwCurPos = new int[len];
    for (final JjTopKwCA v : JjTopKwCA.values()) {
      sJjCATopKwProps[k] = v.disp;
      sJjCATopKwCurPos[k++] = v.curPos;
    }
  }

  /** The expansions only keywords proposals file */
  protected static String[] sJjCAExpKwProps;
  /** The expansions only cursor positions options for a .jj file */
  protected static int[]    sJjCAExpKwCurPos;
  // initialize both above
  static {
    final int len = JjExpKwCA.values().length;
    int k = 0;
    sJjCAExpKwProps = new String[len];
    sJjCAExpKwCurPos = new int[len];
    for (final JjExpKwCA v : JjExpKwCA.values()) {
      sJjCAExpKwProps[k] = v.disp;
      sJjCAExpKwCurPos[k++] = v.curPos;
    }
  }

  /** The regular expressions only keywords proposals file */
  protected static String[] sJjCARegKwProps;
  /** The regular expressions cursor positions options for a .jj file */
  protected static int[]    sJjCARegKwCurPos;
  // initialize both above
  static {
    final int len = JjRegKwCA.values().length;
    int k = 0;
    sJjCARegKwProps = new String[len];
    sJjCARegKwCurPos = new int[len];
    for (final JjRegKwCA v : JjRegKwCA.values()) {
      sJjCARegKwProps[k] = v.disp;
      sJjCARegKwCurPos[k++] = v.curPos;
    }
  }

  /** The java code keywords proposals file */
  protected static String[] sJjCAJavaKwProps;
  /** The java code only cursor positions options for a .jj file */
  protected static int[]    sJjCAJavaKwCurPos;
  // initialize both above
  static {
    final int len = JjJavaKwCA.values().length;
    int k = 0;
    sJjCAJavaKwProps = new String[len];
    sJjCAJavaKwCurPos = new int[len];
    for (final JjJavaKwCA v : JjJavaKwCA.values()) {
      sJjCAJavaKwProps[k] = v.disp;
      sJjCAJavaKwCurPos[k++] = v.curPos;
    }
  }

}
