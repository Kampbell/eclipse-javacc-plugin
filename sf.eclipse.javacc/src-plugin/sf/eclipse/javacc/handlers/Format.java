package sf.eclipse.javacc.handlers;

import static sf.eclipse.javacc.base.IConstants.LS;
import static sf.eclipse.javacc.parser.JavaCCParserConstants.*;

import java.io.StringReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.Token;
import sf.eclipse.javacc.scanners.CodeColorScanner;

/**
 * Format handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014-2015-2016
 */
public class Format extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action
  // MMa 01-02/2016 : fixed problems :
  //   loss of some comments or part of comments (special tokens),
  //   escaped non ASCII characters in tokens printed as non escaped,
  //   loss or addition of newlines, platform line delimiters,

  // TODO see if it would not be better to use JTB's JavaCCPrinter

  /** The parser */
  private JavaCCParser jParser;

  /** {@inheritDoc} */
  @Override
  public Object execute(final ExecutionEvent event) {
    // in which part were we called
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (!(part instanceof IEditorPart)) {
      // on a viewer, do nothing
      return null;
    }
    // on an editor
    final IEditorPart editor = (IEditorPart) part;
    if (!(editor instanceof JJEditor)) {
      // not our editor (no reason why, however), do nothing
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.NotOur_problem (" + editor.getClass().getName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
    // our editor
    final JJEditor jEditor = (JJEditor) editor;
    final IDocument doc = jEditor.getDocument();
    final ISelection selection = jEditor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection)) {
      return null;
    }
    ITextSelection ts = (ITextSelection) selection;
    final int tsoffset = ts.getOffset();
    int tssl = ts.getStartLine();
    int tsel = ts.getEndLine();
    int tslen = ts.getLength();
    if (tssl < 0 || tsel < 0) {
      return null;
    }
    int sloffset = 0;
    boolean isFullText = false;
    try {
      if (tslen == 0) {
        // if no selection, treat full text
        isFullText = true;
        tssl = 0;
        tsel = doc.getNumberOfLines() - 1;
        tslen = doc.getLength();
      }
      else {
        // compute the new selection
        // take full lines
        final IRegion startLine = doc.getLineInformation(tssl);
        sloffset = startLine.getOffset();
        final IRegion endLine = doc.getLineInformation(tsel);
        final String eldelim = doc.getLineDelimiter(ts.getEndLine());
        final int eldelimlen = eldelim == null ? 0 : eldelim.length();
        final int eloffset = endLine.getOffset();
        final int ellen = endLine.getLength();
        int extlen = eloffset + ellen + eldelimlen - sloffset;
        if (extlen > doc.getLength()) {
          extlen = doc.getLength();
        }
        // create the new selection
        ts = new TextSelection(doc, sloffset, extlen);
        tssl = ts.getStartLine();
        tsel = ts.getEndLine();
        tslen = ts.getLength();
      }
      //  process the editor full text using the JavaCC grammar and replace only part of it
      final StringBuilder buf = new StringBuilder(2 * tslen);
      final String docText = doc.get();
      if (formatSelection(docText, tssl + 1, tsel + 1, buf) == true) {
        // replace the text with the modified version
        doc.replace(sloffset, tslen, buf.toString());
        // reposition
        jEditor.selectAndReveal(isFullText ? tsoffset : sloffset, 0);
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, tsoffset, tslen, sloffset);
    }
    return null;
  }

  //  /**
  //   * Formats the stacktrace in lines.
  //   * 
  //   * @param aEx - the exception
  //   * @param aEol - the end of line string
  //   * @return the formatted stacktrace
  //   */
  //  static String fmtStackTrace(final Exception aEx, final String aEol) {
  //    final StringBuffer sb = new StringBuffer(2048);
  //    final StackTraceElement[] st = aEx.getStackTrace();
  //    final int len = st.length;
  //    for (int i = 0; i < len; i++) {
  //      sb.append(st[i].toString()).append(aEol);
  //    }
  //    return sb.toString();
  //  }

  /** Empty string */
  public static final String EMPTY = "";    //$NON-NLS-1$
  /** Space string */
  public static final String SPACE = " ";   //$NON-NLS-1$
  /** Tab string */
  public static final String TAB   = "\t";  //$NON-NLS-1$
  /** Carriage return + line feed string */
  public static final String CRLF  = "\r\n"; //$NON-NLS-1$
  /** Carriage return string */
  public static final String CR    = "\r";  //$NON-NLS-1$
  /** Line feed string */
  public static final String LF    = "\n";  //$NON-NLS-1$
  /** Form feed string */
  public static final String FF    = "\f";  //$NON-NLS-1$
  /** Number sign string */
  public static final String NB    = "#";   //$NON-NLS-1$

  /**
   * Formats the selected text.
   * <p>
   * It reformats the indentation and spacing : it uses spaces to distinct constructs, and it tries to keep
   * comments and newlines (except around braces and parenthesis) where they are.
   * <p>
   * OK, this method is a bit long and complex ... want for a better algorithm :)
   * 
   * @param aTxt - the text to format
   * @param aFirstLine - the line number of the first character of the selected text
   * @param aLastLine - the line number of the last character of the selected text
   * @param aSb - the StringBuilder to receive the formatted text
   * @return true if successful, false otherwise
   */
  private boolean formatSelection(final String aTxt, final int aFirstLine, final int aLastLine,
                                  final StringBuilder aSb) {
    // Parse the full text, retain only the chain of Tokens (with their special tokens)
    final StringReader in = new StringReader(aTxt);
    if (jParser == null) {
      jParser = new JavaCCParser(in);
    }
    else {
      jParser.ReInit(in);
    }
    final JJNode node = jParser.parse(in);
    in.close();
    if (node.getFirstToken().next == null) {
      // warn nothing shall be done if parsing failed
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final Shell shell = workbench.getDisplay().getActiveShell();
      final MessageDialog dialog = new MessageDialog(shell, AbstractActivator.getMsg("Format.Title"), //$NON-NLS-1$
                                                     null, AbstractActivator.getMsg("Format.Message"), //$NON-NLS-1$
                                                     MessageDialog.QUESTION, new String[] {
                                                       IDialogConstants.OK_LABEL }, 0);
      dialog.open();
      return false;
    }
    /*
     * we are or we are not within java code in the following cases : (TODO to be completed)
     * within options : no
     * between parser_begin and parser_end : yes
     * within javacode : yes
     * within token_mgr_decls : yes
     * within regular_expressions or bnf_productions :
     * - within the first {} : yes
     * - within the second {} :
     *   * within an expansion unit try :
     *   * $  within the try : yes if bracesIndentLevel >= 3
     *   * $  within the catch and the finally : yes if bracesIndentLevel >= 2
     *   * within a conditional node : yes
     *   * within everything else : yes if bracesIndentLevel >= 2
     */
    /** The current token */
    Token currToken = node.getFirstToken();
    /** The next token */
    Token nextToken = (currToken == null ? null : currToken.next);
    /** A special token */
    Token specToken = null;
    /** The last token kind */
    int lastKind = -1;
    /** The current token kind */
    int currKind = (currToken == null ? -1 : currToken.kind);
    /** The next token kind */
    int nextKind = (nextToken == null ? -1 : nextToken.kind);
    /** The special token kind */
    int specKind;
    /** The current token image */
    String currImage;
    /** The next token image */
    String nextImage = EMPTY;
    /** The special token image */
    String specImage;
    /** Number of (next token's) special tokens up to the end of line already output */
    int nbSpecTokAlrOut = 0;
    /** The current line indentation */
    final StringBuffer currLineIndent = new StringBuffer(64);
    /** The next line indentation */
    final StringBuffer nextLineIndent = new StringBuffer(64);
    /** Debug newlines */
    final boolean debugNL = false;
    //    /** Debug indentation */
    //    final boolean debugInd = false;
    /** Flag telling if at least one newline is needed after the current token */
    boolean needOneNewline = false;
    /** Flag telling if two newlines are needed after the current token */
    boolean needTwoNewlines = false;
    /** Flag telling if one newline will be needed at the end of the current line */
    boolean willNeedOneNewline = false;
    /** Flag telling if two newlines will be needed at the end of the current line */
    boolean willNeedTwoNewlines = false;
    /** Flag telling that a newline has just been written after the last token */
    boolean newlineJustWritten = true;
    /** Flag telling that the need for a newline is postponed after encountering some token */
    boolean newLinePostponed = false;
    /** Flag memorizing the need for a newline in case it is postponed */
    boolean prevNeedOneNewline = false;
    //    /** Debug output token and special tokens */
    //    final boolean debugOutput = false;
    /**
     * Flag telling to output the current token and its special tokens because the token is inside of the
     * selected text
     */
    boolean outputToken = true;
    /**
     * Flag telling to output the special tokens of the next token which are inside the selected text
     */
    boolean outputNextTkSpecTk = true;
    /**
     * Flag limiting when to output the special tokens of the next token which are inside the selected text
     */
    boolean firstTimeAfterSelection = true;
    /** Flag telling if a space is needed after the current token */
    boolean needSpace = false;
    /** Flag telling if no space is needed for some special cases */
    boolean needNoSpace = false;
    /**
     * False if in JavaCC / JJTree "sections" before parser_end (options, parser_begin), true otherwise
     * (token, special_token, skip, more, productions)
     */
    boolean isAfterParserEnd = false;
    /**
     * Parenthesis level in lookahead constraints : -1 : outside ; 0 : at lookahead token ; >= 1 : inside, '('
     * and ')' included
     */
    int parLevelInLAC = -1;
    /** Parenthesis level in for loops : 0 : outside ; 1, 2, ... : inside */
    int parLevelInFL = 0;
    /** Parenthesis level in JJTree nodes : -1 : outside ; 0 : found a node ; 1, 2, ... : inside */
    int parLevelInJN = 0;
    /** True from the 'for' keyword to the enclosing parenthesis, false otherwise */
    boolean inForLoop = false;
    /** Last parenthesis is LPAREN, not RPAREN nor BIT_OR */
    boolean lastParLPnotRPnorBO = false;
    /** Last straight bracket is LBRACKET, not RBRACKET nor BIT_OR not LOOKAHEAD nor 2 LPAREN */
    boolean lastBraLBnotOthers = false;
    /** Current braces indentation level (changed at each '{' and '}') */
    int bracesIndentLevel = 0;

    String[] ss = null;

    // main loop on all tokens
    while (currToken != null && currToken.kind != EOF) {
      currImage = currToken.image;

      /* -------------------- *
       * A - see where we are *
       * -------------------- */
      // flag for last sections
      if (!isAfterParserEnd && currKind == _PARSER_END) {
        isAfterParserEnd = true;
      }
      if (isAfterParserEnd) {
        // parenthesis level in lookahead constraints
        if (currKind == _LOOKAHEAD) {
          parLevelInLAC = 0;
        }
        else if (currKind == LPAREN && parLevelInLAC >= 0) {
          parLevelInLAC++;
        }
        else if (currKind == RPAREN && parLevelInLAC >= 0) {
          parLevelInLAC--;
        }
        // braces level (not in lookahead constraints)
        if (currKind == LBRACE && parLevelInLAC < 0) {
          bracesIndentLevel++;
        }
        else if (currKind == RBRACE && parLevelInLAC < 0) {
          bracesIndentLevel--;
        }
      }

      // parenthesis level for the for loops
      if (!inForLoop) {
        if ("for".equals(currImage)) { //$NON-NLS-1$
          inForLoop = true;
        }
      }
      if (inForLoop) {
        if (currKind == LPAREN) {
          parLevelInFL++;
        }
        else if (currKind == RPAREN) {
          parLevelInFL--;
          if (parLevelInFL == 0) {
            inForLoop = false;
          }
        }
      }

      // parenthesis level for JJTree nodes
      if (NB.equals(currImage) && parLevelInJN < 0) {
        parLevelInJN++;
      }
      if (parLevelInJN >= 0) {
        if (currKind == LPAREN) {
          parLevelInJN++;
        }
        else if (currKind == RPAREN) {
          parLevelInJN--;
          if (parLevelInJN == 0) {
            parLevelInJN--;
          }
        }
        else if (currKind == COLON) {
          parLevelInJN = -1;
        }
      }

      /* *************************************************** *
       * B - compute the need for newline(s) and indentation *
       * *************************************************** */
      /* --------------------- *
       * B1 - the general case *
       * --------------------- */
      // for some keywords, need for later one or two newlines and no space
      if (currKind == _PARSER_BEGIN || currKind == _LOOKAHEAD) {
        willNeedOneNewline = true;
        needNoSpace = true;
      }
      else if (currKind == _PARSER_END) {
        willNeedOneNewline = true;
        willNeedTwoNewlines = true;
        needNoSpace = true;
      }
      // usually there is no need for newlines and change indentation
      needOneNewline = false;
      needTwoNewlines = false;
      /* ---------------------------------------------------------------------- *
       * B2 - compute the exceptions for which there is a need for two newlines *
       * ---------------------------------------------------------------------- */
      // before a class or method or member declaration after a ';' or a '}'
      if ((currKind == SEMICOLON || currKind == RBRACE)
          && (nextKind == CLASS || nextKind == PUBLIC || nextKind == PRIVATE || nextKind == PROTECTED
              || nextKind == FINAL || nextKind == STATIC || nextKind == VOID || nextKind == SYNCHRONIZED || nextKind == ABSTRACT)) {
        needOneNewline = true;
        needTwoNewlines = true;
      }
      /* --------------------------------------------------------------------------------------------- *
       * B3 - compute some exceptions for which there is a need for one newline and change indentation *
       * --------------------------------------------------------------------------------------------- */
      // after a '{' and not in lookahead constraints and not followed by a '}',
      // or after a '{' before the PARSER_END line,
      // need for a newline and increment indentation
      if (currKind == LBRACE && parLevelInLAC < 0) {
        if (nextKind != RBRACE || !isAfterParserEnd) {
          needOneNewline = true;
          nextLineIndent.append(CodeColorScanner.getIndentString());
          // currLineIndent will be set at the end of the loop
        }
      }
      // after a '}' and not in lookahead constraints, output a newline,
      // and if not following by a '{' or  before the PARSER_END line, decrement indentation,
      // and if not followed by a EOF or a'{' and with no indentation, need for an extra newline
      if (currKind == RBRACE && parLevelInLAC < 0) {
        needOneNewline = true;
        if (lastKind != LBRACE || !isAfterParserEnd) {
          decrementIndent(nextLineIndent);
          // currLineIndent will be set at the end of the loop
        }
        if (nextKind != EOF && nextKind != LBRACE && nextLineIndent.length() == 0) {
          needTwoNewlines = true;
        }
      }
      /* ---------------------------------------------------------------------- *
       * B4 - compute some exceptions for which there is a need for one newline *
       * ---------------------------------------------------------------------- */
      // if not in lookahead constraints and
      // after a ';' not in a for loop, or
      // before a '{', only if not after a '}' or
      // before a '}' only if not after a ';' or a '{'
      if (parLevelInLAC < 0
          && ((currKind == SEMICOLON && !inForLoop) || (nextKind == LBRACE && currKind != RBRACE) || (nextKind == RBRACE
                                                                                                      && currKind != SEMICOLON && currKind != LBRACE))) {
        needOneNewline = true;
      }
      /* ----------------------------------------------------------- *
       * B5 - compute other exceptions specific to the last sections *
       * ----------------------------------------------------------- */
      if (isAfterParserEnd) {
        // after a '>' ending a lexical state list
        if (currKind == GT && nextKind >= _TOKEN && nextKind <= _SKIP) {
          needOneNewline = true;
        }
        // after a ':' and not before an identifier nor a '{',
        // if, within the regular_expr_production, followed by one or more '|',
        // or a parenthesis level of two or more, need for a newline
        if (currKind == COLON && bracesIndentLevel <= 1 && nextKind != IDENTIFIER && nextKind != LBRACE) {
          Token nt = nextToken;
          Token pt = currToken;
          int gtltLevel = 1;
          int euParLevel = 0;
          while (nt != null) {
            if (nt.kind == GT && pt.kind != LPAREN) {
              if (--gtltLevel == 0) {
                break;
              }
            }
            else if (nt.kind == LT) {
              gtltLevel++;
            }
            else if (nt.kind == RPAREN) {
              euParLevel--;
            }
            else if (nt.kind == LPAREN && ++euParLevel > 1) {
              needOneNewline = true;
              break;
            }
            else if (nt.kind == BIT_OR) {
              needOneNewline = true;
              break;
            }
            pt = nt;
            nt = nt.next;
          }
        }
        // after a '(' and not in lookahead constraints nor in a conditional node
        // and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline and increment indentation
        if (currKind == LPAREN && parLevelInLAC < 0 && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          lastParLPnotRPnorBO = true;
          Token nt = nextToken;
          while (nt != null) {
            if (nt.kind == RPAREN) {
              break;
            }
            else if (nt.kind == LPAREN || nt.kind == BIT_OR) {
              needOneNewline = true;
              lastParLPnotRPnorBO = false;
              nextLineIndent.append(CodeColorScanner.getIndentString());
              // currLineIndent will be set at the end of the loop
              break;
            }
            nt = nt.next;
          }
        }
        // after a ')' and before a TRY (in expansion_unit), need for a newline,
        // after a ')' and not in lookahead constraints  nor in a conditional node
        // and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline and decrement indentation,
        // and if followed by a '>', a '*', a '?' or a '+', postpone the need for a newline
        if (currKind == RPAREN && parLevelInLAC < 0 && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          if (nextKind == TRY) {
            needOneNewline = true;
          }
          else if (!lastParLPnotRPnorBO) {
            needOneNewline = true;
            decrementIndent(nextLineIndent);
            // currLineIndent will be set at the end of the loop
          }
          lastParLPnotRPnorBO = false;
          if (NB.equals(nextImage)) {
            needOneNewline = false;
          }
          else if (nextKind == GT || nextKind == STAR || nextKind == HOOK || nextKind == PLUS) {
            newLinePostponed = true;
            prevNeedOneNewline = needOneNewline;
            needOneNewline = false;
          }
        }
        // after a '>', a '*', a '?' or a '+' not followed by a '>',
        // if a newline is postponed, recover the need if no newline already computed
        if (newLinePostponed && nextKind != GT
            && (currKind == GT || currKind == STAR || currKind == HOOK || currKind == PLUS)) {
          needOneNewline |= prevNeedOneNewline;
          newLinePostponed = false;
        }
        // before a '(' and not in lookahead constraints  nor in a conditional node
        // and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline
        if (nextKind == LPAREN && parLevelInLAC < 0 && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          boolean nextParenLeftNorRight = false;
          Token nt = nextToken;
          if (nt != null) {
            nt = nt.next;
            while (nt != null) {
              if (nt.kind == RPAREN) {
                nextParenLeftNorRight = false;
                break;
              }
              else if (nt.kind == LPAREN || nt.kind == BIT_OR) {
                nextParenLeftNorRight = true;
                break;
              }
              nt = nt.next;
            }
          }
          if (nextParenLeftNorRight) {
            needOneNewline = true;
          }
        }
        // before a ')' and not in lookahead constraints  nor in a conditional node
        // and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline
        if (nextKind == RPAREN && parLevelInLAC < 0 && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          if (!lastParLPnotRPnorBO) {
            needOneNewline = true;
          }
        }
        // after a '[' and before a '{', a lookahead expression, or not at the last enclosing level,
        // or at the last enclosing level with an inner '|' or more than one '(',
        // need for a newline and increment indentation
        if (currKind == LBRACKET) {
          lastBraLBnotOthers = true;
          Token nt = nextToken;
          int nbParen = 0;
          while (nt != null) {
            if (nt.kind == LPAREN) {
              nbParen++;
            }
            else if (nt.kind == RPAREN) {
              nbParen--;
            }
            else if (nt.kind == RBRACKET) {
              break;
            }
            if (nt.kind == LBRACE || nt.kind == _LOOKAHEAD || nt.kind == LBRACKET || nt.kind == BIT_OR
                || (nt.kind == LPAREN && nbParen > 1)) {
              needOneNewline = true;
              lastBraLBnotOthers = false;
              nextLineIndent.append(CodeColorScanner.getIndentString());
              // currLineIndent will be set at the end of the loop
              break;
            }
            nt = nt.next;
          }
        }
        // after a ']' and after a lookahead expression, or not at the last enclosing level,
        // or at the last enclosing level with an inner '|', or more than one '(',
        // need for a newline and decrement indentation
        if (currKind == RBRACKET) {
          if (!lastBraLBnotOthers) {
            needOneNewline = true;
            decrementIndent(nextLineIndent);
            // currLineIndent will be set at the end of the loop
          }
          lastBraLBnotOthers = false;
        }
        // before a '[' and before a lookahead expression, or not at the last enclosing level,
        // or at the last enclosing level with an inner '|' or more than one '(',
        // need for a newline
        if (nextKind == LBRACKET) {
          Token nt = nextToken;
          if (nt != null) {
            nt = nt.next;
            int nbParen = 0;
            while (nt != null) {
              if (nt.kind == LPAREN) {
                nbParen++;
              }
              else if (nt.kind == RPAREN) {
                nbParen--;
              }
              else if (nt.kind == RBRACKET) {
                break;
              }
              if (nt.kind == LBRACE || nt.kind == _LOOKAHEAD || nt.kind == LBRACKET || nt.kind == BIT_OR
                  || (nt.kind == LPAREN && nbParen > 1)) {
                needOneNewline = true;
                break;
              }
              nt = nt.next;
            }
          }
        }
        // before a ']' and after a lookahead expression, or not at the last enclosing level,
        // or at the last enclosing level with an inner '|', or more than one '(',
        // need for a newline
        if (nextKind == RBRACKET) {
          if (!lastBraLBnotOthers) {
            needOneNewline = true;
          }
        }
        // before a '|', need for a newline
        if (nextKind == BIT_OR) {
          needOneNewline = true;
        }
        // increment indentation for a '<', and decrement indentation for a '>' in a regular expression
        if (currKind == LT && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          nextLineIndent.append(CodeColorScanner.getIndentString());
          // currLineIndent will be set at the end of the loop
        }
        else if (currKind == GT && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          decrementIndent(nextLineIndent);
          // currLineIndent will be set at the end of the loop
        }
      } // end if (isAfterParserEnd)

      /* ************************************************************* *
       * C - compute the (following) space : usually there must be one *
       * ************************************************************* */
      /* --------------------- *
       * C1 - the general case *
       * --------------------- */
      needSpace = true;
      /* ------------------------------------------------------------------------------------- *
       * C2 - compute the exceptions to the (following) space due to the current or next token *
       * ------------------------------------------------------------------------------------- */
      // before a newline
      // in a '{}' pair
      // after some operators or punctuation '(', '[' (unless in last sections), '.', '!', '~',
      // before some operators or punctuation ')', ']' (unless in last sections), ';', ',', '.',
      // before and after '++' & '--' (unless before or after a java identifier),
      // before an argument list
      // after some JavaCC keywords
      // after a ':' if followed by a '{'
      // after some JavaCC operators or punctuation or construct '#', '^','(>' in last sections
      // before some JavaCC operators '?', '*', '+' in expansion units 
      // before and after the JavaCC operator '-' in regular expression
      // TODO for the JavaCC operators '?', '*', '+' in expansion_unit try syntax : try { (production)+ } catch : no space after the ')'
      if (needOneNewline
          || (currKind == LBRACE && nextKind == RBRACE)
          || currKind == LPAREN
          || (currKind == LBRACKET && !isAfterParserEnd)
          || currKind == DOT
          || currKind == BANG
          || currKind == TILDE
          || nextKind == RPAREN
          || (nextKind == RBRACKET && !isAfterParserEnd)
          || nextKind == SEMICOLON
          || nextKind == COMMA
          || nextKind == DOT
          || ((nextKind == INCR || nextKind == DECR) && Character.isJavaIdentifierStart(currImage.charAt(0)))
          || ((currKind == INCR || currKind == DECR) && Character.isJavaIdentifierStart(nextImage.charAt(0)))
          || (nextKind == LPAREN && currKind == IDENTIFIER)
          || currKind == _PARSER_BEGIN
          || currKind == _PARSER_END
          || currKind == _LOOKAHEAD
          || (currKind == GT && nextKind == GT)
          || (currKind == LT && nextKind == LT)
          || (currKind == COLON && nextKind == LBRACE)
          || ((NB.equals(currImage) || nextKind == XOR || (lastKind == LPAREN && currKind == GT)) && isAfterParserEnd)
          || ((nextKind == HOOK || nextKind == STAR || nextKind == PLUS || currKind == MINUS || nextKind == MINUS)
              && bracesIndentLevel <= 1 && parLevelInJN < 0 && isAfterParserEnd)

      ) {
        needSpace = false;
      }

      /* ************************************************************************************** *
       * D - now output everything, but only for tokens and special tokens in the selected text *
       * ************************************************************************************** */
      // set output flags
      if (currToken.beginLine >= aFirstLine && currToken.endLine <= aLastLine) {
        // inside
        outputToken = true;
        outputNextTkSpecTk = true;
      }
      // see if special tokens should be included in the selected text, whereas the token should not
      else if (currToken.endLine > aLastLine && firstTimeAfterSelection) {
        firstTimeAfterSelection = false;
        outputToken = false;
        specToken = currToken.specialToken;
        int stbl = -1;
        if (specToken != null) {
          // rewind to the first
          while (specToken.specialToken != null) {
            specToken = specToken.specialToken;
          }
          stbl = specToken.beginLine;
        }
        outputNextTkSpecTk = stbl <= aLastLine;
      }
      else {
        // outside
        outputToken = false;
        outputNextTkSpecTk = false;
      }
      //      if (debugOutput) {
      //        boolean affiche = false;
      //        affiche = (currToken.beginLine >= (aFirstLine - 1)) && (currToken.endLine <= (aLastLine + 2));
      //        if (affiche) {
      //          aSb.append("\t/* out : cti = " + currImage + ", afl = " + aFirstLine + ", all = " + aLastLine); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      //          aSb.append(", ctbl = " + currToken.beginLine + ", ctel = " + currToken.endLine); //$NON-NLS-1$ //$NON-NLS-2$
      //          specToken = currToken.specialToken;
      //          int nbst = 0;
      //          int stbl = -1;
      //          int stel = -1;
      //          if (specToken != null) {
      //            nbst++;
      //            stel = specToken.endLine;
      //            // rewind to the first
      //            while (specToken.specialToken != null) {
      //              specToken = specToken.specialToken;
      //              nbst++;
      //            }
      //            stbl = specToken.beginLine;
      //          }
      //          aSb.append(", nbst = " + nbst + ", stbl = " + stbl + ", stel = " + stel); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      //          aSb.append(", nbSTAO = " + nbSpecTokAlrOut); //$NON-NLS-1$ 
      //          aSb.append(", ot = " + outputToken + ", ost = " + outputNextTkSpecTk + " */" + LS); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
      //        }
      //      }

      // for a previous ')' not in lookahead constraints, if memorized, output the one or two newlines
      if (lastKind == RPAREN) {
        if (willNeedOneNewline && parLevelInLAC < 0) {
          if (outputToken && currToken.beginLine > aFirstLine) {
            if (debugNL) {
              aSb.append("\t\t/* rp, W n 1 n l, A */"); //$NON-NLS-1$
            }
            aSb.append(LS);
          }
          willNeedOneNewline = false;
          newlineJustWritten = true;
        }
        if (willNeedTwoNewlines && parLevelInLAC < 0) {
          if (outputToken || outputNextTkSpecTk) {
            if (debugNL) {
              aSb.append("\t\t/* rp, W n 2 n l, B */"); //$NON-NLS-1$
            }
            aSb.append(LS);
          }
          willNeedTwoNewlines = false;
          newlineJustWritten = true;
        }
      }

      // process the special token(s) (only those not already processed with the previous token)
      specToken = currToken.specialToken;
      if ((outputToken || outputNextTkSpecTk) && specToken != null) {
        // rewind to the first
        while (specToken.specialToken != null) {
          specToken = specToken.specialToken;
        }
        // process them
        boolean skipNL = true;
        while (specToken != null) {
          if (outputNextTkSpecTk && nbSpecTokAlrOut > 0) {
            // skip those already processed
            nbSpecTokAlrOut--;
            specToken = specToken.next;
            continue;
          }
          // output others only if in the selected text 
          if (specToken.endLine < aFirstLine) {
            specToken = specToken.next;
            continue;
          }
          if (specToken.beginLine > aLastLine) {
            break;
          }
          specKind = specToken.kind;
          specImage = specToken.image;
          if (specKind == SINGLE_LINE_COMMENT) {
            skipNL = false;
            if (newlineJustWritten) {
              // output indentation after a new line
              aSb.append(currLineIndent);
            }
            else {
              // otherwise output one space
              aSb.append(SPACE);
            }
            // output the comment
            aSb.append(specImage);
            // manage newlines
            newlineJustWritten = true;
            skipNL = true;
          }
          else if (specKind == MULTI_LINE_COMMENT || specKind == FORMAL_COMMENT) {
            skipNL = false;
            if (newlineJustWritten) {
              // output indentation after a new line
              aSb.append(currLineIndent);
            }
            else {
              // otherwise output one space
              aSb.append(SPACE);
            }
            if (specToken.beginLine >= aFirstLine && specToken.endLine <= aLastLine) {
              // inside
              aSb.append(specImage);
            }
            else if (specToken.beginLine < aFirstLine && specToken.endLine >= aFirstLine) {
              // around first line
              ss = specImage.split("\\r\\n|\\r|\\n"); //$NON-NLS-1$
              int i;
              for (i = aFirstLine - specToken.beginLine; i < specToken.endLine - specToken.beginLine; i++) {
                if (debugNL) {
                  aSb.append("\t\t/* a f l Tk, " + i + ", C */"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                aSb.append(ss[i]).append(LS);
              }
              aSb.append(ss[i]);
            }
            else if (specToken.beginLine <= aLastLine && specToken.endLine > aLastLine) {
              // around last line
              ss = specImage.split("\\r\\n|\\r|\\n"); //$NON-NLS-1$
              int i;
              for (i = 0; i <= aLastLine - specToken.beginLine; i++) {
                if (debugNL) {
                  aSb.append("\t\t/* a l l Tk, " + i + ", D */"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                aSb.append(ss[i]).append(LS);
              }
            }
            newlineJustWritten = false;
          }
          else if (CRLF.equals(specImage) || CR.equals(specImage) || LF.equals(specImage)) {
            // newline but not for the first bunch
            if (!skipNL) {
              if (debugNL) {
                aSb.append("\t\t/* crlf Tk, " + skipNL + ", E */"); //$NON-NLS-1$ //$NON-NLS-2$
              }
              aSb.append(LS);
              newlineJustWritten = true;
            }
          }
          else if (FF.equals(specImage)) {
            aSb.append(FF);
          }
          specToken = specToken.next;
        } // end  while (specToken != null)
      } // end if ((outputToken || outputNextTkSpecTk) && specToken != null)

      // for a previous ';', if memorized, output the newline
      if (lastKind == SEMICOLON && willNeedOneNewline) {
        if (outputToken) {
          if (debugNL) {
            aSb.append("\t\t/* sc Tk, W n 1 n l, F */"); //$NON-NLS-1$
          }
          aSb.append(LS);
        }
        willNeedOneNewline = false;
        newlineJustWritten = true;
      }

      // update (if necessary) and output indentation if a newline has been written
      if (newlineJustWritten) {
        //        if (debugInd) {
        //          final int len = sb.length() - endLineDelim.length();
        //          if (len >= 0 && sb.substring(len).equals(endLineDelim)) {
        //            sb.setLength(len);
        //          }
        //        }
        if (currKind == LBRACE || currKind == LPAREN || currKind == LBRACKET || currKind == LT
            || currKind == GT) {
          if (outputToken) {
            aSb.append(currLineIndent);
          }
        }
        else if (currKind == BIT_OR) {
          if (outputToken) {
            final int len = currLineIndent.length() - CodeColorScanner.getIndentString().length();
            if (len > 0) {
              aSb.append(currLineIndent.substring(0, len));
            }
          }
        }
        else {
          //          if (debugInd) {
          //            // TODO voir que le délimiteur de ligne peut être CR ou LF uniquemeent
          //            final int len = aSb.length() - LS.length();
          //            if (len >= 0 && aSb.substring(len).equals(LS)) {
          //              aSb.setLength(len);
          //            }
          //            aSb.append("\t\t/* nenl, ").append(currLineIndent.length()); //$NON-NLS-1$
          //            aSb.append(", ").append(nextLineIndent.length()); //$NON-NLS-1$
          //            aSb.append(" */").append(LS); //$NON-NLS-1$
          //          }
          if (outputToken) {
            aSb.append(nextLineIndent);
          }
        }
      } // end  if (newlineJustWritten)

      // output the token
      if (outputToken) {
        aSb.append(addEscapes(currImage));
        newlineJustWritten = false;
      }

      // output the space
      if (needSpace) {
        if (needNoSpace && currKind == RPAREN && parLevelInLAC <= 0) {
          needNoSpace = false;
        }
        else if (newlineJustWritten && currKind == BIT_OR) {
          // case of an unindented '|' : re-indent
          if (outputToken) {
            aSb.append(CodeColorScanner.getSpecIndentString());
          }
        }
        else {
          // other normal cases
          if (outputToken) {
            aSb.append(SPACE);
          }
        }
      } // end if (needSpace)

      // reset flag
      newlineJustWritten = false;

      // process the special tokens from the next token but in the selected text
      if (outputNextTkSpecTk /*&& outputToken*/&& nextToken != null) {
        nbSpecTokAlrOut = 0;
        specToken = nextToken.specialToken;
        if (specToken != null) {
          // rewind to the first
          while (specToken.specialToken != null) {
            specToken = specToken.specialToken;
          }
          // process them
          while (specToken != null) {
            // process only those on the same line as the current token
            if (specToken.beginLine != currToken.endLine) {
              break;
            }
            nbSpecTokAlrOut++;
            if (specToken.beginLine < aFirstLine) {
              specToken = specToken.next;
              continue;
            }
            specImage = specToken.image;
            specKind = specToken.kind;
            if (specKind == SINGLE_LINE_COMMENT) {
              // prepend a space if none has just been output
              if (!needSpace || currKind == RPAREN) {
                aSb.append(SPACE);
              }
              // output the comment
              aSb.append(specImage);
              // skip one newline need for a single line comment because it is terminated by one
              if (willNeedTwoNewlines) {
                willNeedTwoNewlines = false;
              }
              else if (willNeedOneNewline) {
                willNeedOneNewline = false;
              }
              else if (needTwoNewlines) {
                needTwoNewlines = false;
              }
              else if (needOneNewline) {
                needOneNewline = false;
              }
              newlineJustWritten = true;
            }
            else if (specKind == MULTI_LINE_COMMENT || specKind == FORMAL_COMMENT) {
              // prepend a space if none has just been output
              if (!needSpace || currKind == RPAREN) {
                aSb.append(SPACE);
              }
              // output the comment, taking care of the selection's boundaries
              if (specToken.beginLine >= aFirstLine && specToken.endLine <= aLastLine) {
                // inside
                aSb.append(specImage);
              }
              else if (specToken.beginLine < aFirstLine && specToken.endLine >= aFirstLine) {
                // around first line
                ss = specImage.split("\\r\\n|\\r|\\n"); //$NON-NLS-1$
                int i;
                for (i = aFirstLine - specToken.beginLine; i < specToken.endLine - specToken.beginLine - 1; i++) {
                  if (debugNL) {
                    aSb.append("\t\t/* a f l spTk, " + i + ", G */"); //$NON-NLS-1$ //$NON-NLS-2$
                  }
                  aSb.append(ss[i]).append(LS);
                }
                aSb.append(ss[i]);
              }
              else if (specToken.beginLine <= aLastLine && specToken.endLine > aLastLine) {
                // around last line
                ss = specImage.split("\\r\\n|\\r|\\n"); //$NON-NLS-1$
                int i;
                for (i = 0; i <= aLastLine - specToken.beginLine; i++) {
                  if (debugNL) {
                    aSb.append("\t\t/* a l l spTk, " + i + ", H */"); //$NON-NLS-1$ //$NON-NLS-2$
                  }
                  aSb.append(ss[i]).append(LS);
                }
              }
              newlineJustWritten = false;
            }
            else if (CRLF.equals(specImage) || CR.equals(specImage) || LF.equals(specImage)) {
              if (!needOneNewline && !willNeedOneNewline) {
                if (debugNL) {
                  aSb.append("\t\t/* a crlf spTk, I */"); //$NON-NLS-1$ 
                }
                aSb.append(LS);
              }
            }
            else if (TAB.equals(specImage) || FF.equals(specImage)) {
              // tabs or form feeds : take them as they are, but not the spaces
              aSb.append(specImage);
            }
            specToken = specToken.next;
          } // end while (specToken != null)
        } // end if (specToken != null)
      } // end if (outputNextTkSpecTk && nextToken != null)

      // output the one or two newlines 
      if (needTwoNewlines) {
        if (outputToken && !(outputNextTkSpecTk && currToken.endLine == aLastLine)) {
          if (debugNL) {
            aSb.append("\t\t/* n 2 n l, J, " + nbSpecTokAlrOut + " */"); //$NON-NLS-1$ //$NON-NLS-2$
          }
          aSb.append(LS);
        }
        needTwoNewlines = false;
        newlineJustWritten = true;
      }
      if (needOneNewline) {
        if (outputToken) {
          if (debugNL) {
            aSb.append("\t\t/* n 1 n l, K */"); //$NON-NLS-1$
          }
          aSb.append(LS);
        }
        needOneNewline = false;
        newlineJustWritten = true;
      }

      // update some flags and variables
      if (currKind == RPAREN && parLevelInLAC == 0) {
        parLevelInLAC = -1;
      }
      needSpace = false;
      lastKind = currKind;
      currToken = nextToken;
      currKind = nextKind;
      nextToken = (currToken == null ? null : currToken.next);
      nextKind = (nextToken == null ? -1 : nextToken.kind);
      nextImage = (nextToken == null ? EMPTY : nextToken.image);
      final int clen = currLineIndent.length();
      final int nlen = nextLineIndent.length();
      if (clen < nlen) {
        currLineIndent.append(CodeColorScanner.getIndentString());
      }
      else if (nlen < clen) {
        currLineIndent.setLength(nlen);
      }

    } // end while (currToken != null && currToken.kind != EOF)
    // remove the last new lines
    return true;
  }

  /**
   * Decrements indentation by shortening the given StringBuffer with the given indentation String.
   * 
   * @param aSb - the indentation StringBuffer
   */
  private static void decrementIndent(final StringBuffer aSb) {
    final int len = aSb.length() - CodeColorScanner.getIndentString().length();
    if (len >= 0) {
      aSb.setLength(len);
    }
  }

  /**
   * Replaces unprintable characters by their escaped (or unicode escaped) equivalents in the given string
   * 
   * @param str - the input string
   * @return the escaped string
   */
  protected static final String addEscapes(final String str) {
    final StringBuffer retval = new StringBuffer();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i)) {
        case 0:
          continue;
          //        case '\b':
          //          retval.append("\\b"); //$NON-NLS-1$
          //          continue;
          //        case '\t':
          //          retval.append("\\t"); //$NON-NLS-1$
          //          continue;
        case '\n':
          retval.append("\n"); //$NON-NLS-1$
          continue;
          //        case '\f':
          //          retval.append("\\f"); //$NON-NLS-1$
          //          continue;
        case '\r':
          retval.append("\r"); //$NON-NLS-1$
          continue;
          //        case '\"':
          //          retval.append("\\\""); //$NON-NLS-1$
          //          continue;
          //        case '\'':
          //          retval.append("\\\'"); //$NON-NLS-1$
          //          continue;
          //        case '\\':
          //          retval.append("\\\\"); //$NON-NLS-1$
          //          continue;
        default:
          if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
            final String s = "0000" + Integer.toString(ch, 16); //$NON-NLS-1$
            retval.append("\\u" + s.substring(s.length() - 4, s.length())); //$NON-NLS-1$
          }
          else {
            retval.append(ch);
          }
          continue;
      }
    }
    return retval.toString();
  }

}
