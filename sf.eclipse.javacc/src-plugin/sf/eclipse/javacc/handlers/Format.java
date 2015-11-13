package sf.eclipse.javacc.handlers;

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
import org.eclipse.ui.texteditor.AbstractTextEditor;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.Token;
import sf.eclipse.javacc.scanners.CodeScanner;

/**
 * Format handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014-2015
 */
public class Format extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action

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
    int tssl = ts.getStartLine();
    int tsel = ts.getEndLine();
    int tslen = ts.getLength();
    if (tssl < 0 || tsel < 0) {
      return null;
    }
    // save line position
    final int ln = tssl;
    // if No selection, treat full text
    if (tslen == 0) {
      //      ts = new TextSelection(doc, 0, doc.getLength());
      //      tssl = ts.getStartLine();
      //      tsel = ts.getEndLine();
      //      tslen = ts.getLength();
      tssl = 0;
      tsel = doc.getNumberOfLines() - 1;
      tslen = doc.getLength();
    }
    int sloffset = 0;
    try {
      // extend selection (in case partial lines have been selected)
      final IRegion endLine = doc.getLineInformation(tsel);
      final IRegion startLine = doc.getLineInformation(tssl);
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset() + endLine.getLength()
                                                         - startLine.getOffset());
      tssl = ts.getStartLine();
      tsel = ts.getEndLine();
      tslen = ts.getLength();

      // Format the selection full text
      // The tricky part is to replace only part of the full text
      // we need to process the editor full text using the JavaCC grammar
      // and we have to replace only part of it
      final String endLineDelim = doc.getLegalLineDelimiters()[0];
      final StringBuffer strbuf = new StringBuffer(2 * tslen);
      final String docText = doc.get();
      if (formatSelection(docText, endLineDelim, tssl + 1, tsel + 1, strbuf) == true) {
        // Replace the text with the modified version
        sloffset = startLine.getOffset();
        doc.replace(sloffset, tslen, strbuf.toString());
      }
      // Reselect text... not exactly as JavaEditor... whole text here
      // editor.selectAndReveal(startLine.getOffset(), strbuf.length());
      // reposition
      ((AbstractTextEditor) editor).selectAndReveal(doc.getLineInformation(ln).getOffset(), 0);
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, sloffset, tslen);
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
  public static final String EMPTY = "";  //$NON-NLS-1$
  /** Space string */
  public static final String SPACE = " "; //$NON-NLS-1$
  /** Tab string */
  public static final String TAB   = "\t"; //$NON-NLS-1$
  /** Carriage return string */
  public static final String CR    = "\r"; //$NON-NLS-1$
  /** Line feed string */
  public static final String LF    = "\n"; //$NON-NLS-1$
  /** Form feed string */
  public static final String FF    = "\f"; //$NON-NLS-1$
  /** Number sign string */
  public static final String NB    = "#"; //$NON-NLS-1$

  /**
   * Formats the selected text.
   * <p>
   * It reformats the indentation and spacing : it uses spaces to distinct constructs, and it tries to keep
   * comments and newlines (except around braces and parenthesis) as they are.
   * 
   * @param aTxt - the text to format
   * @param aEndLineDelim - the end of line delimiter string
   * @param aFirstLine - the line number of the first character of the selected text
   * @param aLastLine - the line number of the last character of the selected text
   * @param aSb - the StringBuffer to receive the formatted text
   * @return true if successful, false otherwise
   */
  private boolean formatSelection(final String aTxt, final String aEndLineDelim, final int aFirstLine,
                                  final int aLastLine, final StringBuffer aSb) {
    // Parse the full text, retain only the chain of Tokens
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
      // Warn Nothing shall be done if parsing failed
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
     * TODO : we are or we are not within java code in the following cases :
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
    /** Number of (next token's) special tokens up to the end of line already processed */
    int nbSpecialToken = 0;
    /** The current line indentation */
    final StringBuffer currLineIndent = new StringBuffer(64);
    /** The next line indentation */
    final StringBuffer nextLineIndent = new StringBuffer(64);
    /** Debug newlines */
    final boolean debugNL = false;
    /** Debug indentation */
    final boolean debugInd = false;
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
    /**
     * Flag memorizing the need to not output the first newlines & special tokens at the beginning of the
     * selection if the selection does not start at the first line of the document
     */
    boolean careFirstLine = (aFirstLine > 1);
    /** Flag telling to not output anything because of outside the selected text */
    boolean skipOutput;
    /** Buffer length at the last line */
    int lastSbLength = 0;
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
    // main loop on all tokens
    while (currToken != null && currToken.kind != EOF) {
      currImage = currToken.image;
      /*
       * see where we are
       */
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

      /*
       * compute the need for newline(s) and indentation
       */
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
      /*
       * compute the exceptions for which there is a need for two newlines
       */
      // before a class or method or member declaration after a ';' or a '}'
      if ((currKind == SEMICOLON || currKind == RBRACE)
          && (nextKind == CLASS || nextKind == PUBLIC || nextKind == PRIVATE || nextKind == PROTECTED
              || nextKind == FINAL || nextKind == STATIC || nextKind == VOID || nextKind == SYNCHRONIZED || nextKind == ABSTRACT)) {
        needOneNewline = true;
        needTwoNewlines = true;
      }
      /*
       * compute some exceptions for which there is a need for one newline and change indentation
       */
      // after a '{' and not in lookahead constraints and not followed by a '}',
      // need for a newline and increment indentation
      if (currKind == LBRACE && parLevelInLAC < 0) {
        if (nextKind != RBRACE) {
          needOneNewline = true;
          nextLineIndent.append(CodeScanner.getIndentString());
          // currLineIndent will be set at the end of the loop
        }
      }
      // after a '}' and not in lookahead constraints, output a newline,
      // and if not following by a '{', decrement indentation,
      // and if not followed by a '{' and with no indentation, need for an extra newline
      if (currKind == RBRACE && parLevelInLAC < 0) {
        needOneNewline = true;
        if (lastKind != LBRACE) {
          decrementIndent(nextLineIndent);
          // currLineIndent will be set at the end of the loop
        }
        if (nextKind != LBRACE && nextLineIndent.length() == 0) {
          needTwoNewlines = true;
        }
      }
      /*
       * compute some exceptions for which there is a need for one newline
       */
      // if not in lookahead constraints and
      // after a ';' not in a for loop, or
      // before a '{', only if not after a '}' or
      // before a '}' only if not after a ';' or a '{'
      if (parLevelInLAC < 0
          && ((currKind == SEMICOLON && !inForLoop) || (nextKind == LBRACE && currKind != RBRACE) || (nextKind == RBRACE
                                                                                                      && currKind != SEMICOLON && currKind != LBRACE))) {
        needOneNewline = true;
      }
      /*
       * compute other exceptions specific to the last sections
       */
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
              nextLineIndent.append(CodeScanner.getIndentString());
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
              nextLineIndent.append(CodeScanner.getIndentString());
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
          nextLineIndent.append(CodeScanner.getIndentString());
          // currLineIndent will be set at the end of the loop
        }
        else if (currKind == GT && parLevelInJN < 0 && bracesIndentLevel <= 1) {
          decrementIndent(nextLineIndent);
          // currLineIndent will be set at the end of the loop
        }
      } // end if (isAfterParserEnd)
      /*
       * compute the (following) space : usually there must be one
       */
      needSpace = true;
      /*
       * compute the exceptions to the (following) space due to the current or next token
       */
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
      /*
       * now output everything, but only for tokens and special tokens in the selected text, and take care of
       * first newlines of the selection (but need to compute flags in all cases)
       */
      // set output flag
      if (currToken.beginLine >= aFirstLine && currToken.endLine <= aLastLine) {
        if (careFirstLine) {
          skipOutput = true;
        }
        else {
          skipOutput = false;
        }
      }
      else {
        skipOutput = true;
      }
      // for a previous ')' not in lookahead constraints, if memorized,
      // output the one or two newlines
      if (lastKind == RPAREN) {
        if (willNeedOneNewline && parLevelInLAC < 0) {
          if (!skipOutput) {
            aSb.append(aEndLineDelim);
          }
          willNeedOneNewline = false;
          newlineJustWritten = true;
        }
        if (willNeedTwoNewlines && parLevelInLAC < 0) {
          if (debugNL) {
            aSb.append("\t\t/* rp,  W n 2 n l A */"); //$NON-NLS-1$
          }
          if (!skipOutput) {
            aSb.append(aEndLineDelim);
          }
          willNeedTwoNewlines = false;
          newlineJustWritten = true;
        }
      }
      // process the (previous) special token(s) (only those not already processed)
      specToken = currToken.specialToken;
      if (specToken != null) {
        // rewind to the first
        while (specToken.specialToken != null) {
          specToken = specToken.specialToken;
        }
        boolean afterNewline = newlineJustWritten;
        // examine each
        while (specToken != null) {
          if (nbSpecialToken > 0) {
            // skip those already processed
            nbSpecialToken--;
          }
          else {
            // output others only if comments in the selection
            specKind = specToken.kind;
            specImage = specToken.image;
            if ((specKind == SINGLE_LINE_COMMENT || specKind == MULTI_LINE_COMMENT || specKind == FORMAL_COMMENT)
                && (specToken.beginLine >= aFirstLine)) {
              if (!afterNewline) {
                // output one a space before each comment
                if (!skipOutput) {
                  aSb.append(SPACE);
                }
              }
              else {
                if (!skipOutput) {
                  aSb.append(currLineIndent);
                }
              }
              // output the comment
              if (!skipOutput) {
                aSb.append(specToken.toString());
              }
              // manage newlines
              if (specKind == SINGLE_LINE_COMMENT) {
                afterNewline = true;
                newlineJustWritten = true;
              }
              else {
                afterNewline = false;
              }
            }
            else if ((CR.equals(specImage) || LF.equals(specImage) || FF.equals(specImage)) && !afterNewline) {
              if (!skipOutput) {
                aSb.append(aEndLineDelim);
              }
              afterNewline = true;
              newlineJustWritten = true;
            }
          }
          specToken = specToken.next;
        }
      } // end if (specToken != null)
      // reset flags after the special processing of the first line of the selected text
      if (currToken.beginLine >= aFirstLine && currToken.endLine <= aLastLine && careFirstLine) {
        careFirstLine = false;
        skipOutput = false;
      }
      // for a previous ';', if memorized, output the newline
      if (lastKind == SEMICOLON && willNeedOneNewline) {
        if (debugNL) {
          aSb.append("\t\t/* sc, W n 1 n l A */"); //$NON-NLS-1$
        }
        if (!skipOutput) {
          aSb.append(aEndLineDelim);
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
          if (!skipOutput) {
            aSb.append(currLineIndent);
          }
        }
        else if (currKind == BIT_OR) {
          if (!skipOutput) {
            final int len = currLineIndent.length() - CodeScanner.getIndentString().length();
            if (len > 0) {
              aSb.append(currLineIndent.substring(0, len));
            }
          }
        }
        else {
          if (debugInd) {
            final int len = aSb.length() - aEndLineDelim.length();
            if (len >= 0 && aSb.substring(len).equals(aEndLineDelim)) {
              aSb.setLength(len);
            }
            aSb.append("\t\t/* nenl, ").append(currLineIndent.length()); //$NON-NLS-1$
            aSb.append(", ").append(nextLineIndent.length()); //$NON-NLS-1$
            aSb.append(" */").append(aEndLineDelim); //$NON-NLS-1$
          }
          if (!skipOutput) {
            aSb.append(nextLineIndent);
          }
        }
      }
      // output the token
      if (!skipOutput) {
        aSb.append(currToken.toString());
      }
      // output the space
      if (needSpace) {
        if (needNoSpace && currKind == RPAREN && parLevelInLAC <= 0) {
          needNoSpace = false;
        }
        else if (newlineJustWritten && currKind == BIT_OR) {
          // case of an unindented '|' : re-indent
          if (!skipOutput) {
            aSb.append(CodeScanner.getSpecIndentString());
          }
        }
        else {
          // other normal cases
          if (!skipOutput) {
            aSb.append(SPACE);
          }
        }
      }
      // reset flag
      newlineJustWritten = false;
      // process the special token(s) following on the same line
      if (nextToken != null) {
        nbSpecialToken = 0;
        specToken = nextToken.specialToken;
        if (specToken != null) {
          // rewind to the first
          while (specToken.specialToken != null) {
            specToken = specToken.specialToken;
          }
          // process them
          while (specToken != null) {
            nbSpecialToken++;
            specImage = specToken.image;
            // process only those on the same line
            if (CR.equals(specImage) || LF.equals(specImage) || FF.equals(specImage)) {
              break;
            }
            // output only comments
            specKind = specToken.kind;
            if (specKind == SINGLE_LINE_COMMENT || specKind == MULTI_LINE_COMMENT
                || specKind == FORMAL_COMMENT) {
              // prepend a space if none has just been output
              if (!needSpace || currKind == RPAREN) {
                if (!skipOutput) {
                  aSb.append(SPACE);
                }
              }
              // output the special token
              if (!skipOutput) {
                aSb.append(specToken.toString());
              }
            }
            if (specKind == SINGLE_LINE_COMMENT) {
              // skip one newline need for a single line comment which already includes one
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
              // end of line reached
              break;
            }
            specToken = specToken.next;
          }
        }
      }
      // memorize buffer length
      if (!skipOutput) {
        lastSbLength = aSb.length();
      }
      // output the one or two newlines after
      if (needTwoNewlines) {
        if (!skipOutput) {
          aSb.append(aEndLineDelim);
        }
        needTwoNewlines = false;
        newlineJustWritten = true;
      }
      if (needOneNewline) {
        if (!skipOutput) {
          aSb.append(aEndLineDelim);
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
        currLineIndent.append(CodeScanner.getIndentString());
      }
      else if (nlen < clen) {
        currLineIndent.setLength(nlen);
      }
    } // end while (currToken != null && currToken.kind != EOF)
    // remove trailing newlines
    aSb.setLength(lastSbLength);
    return true;
  }

  /**
   * Decrements indentation by shortening the given StringBuffer with the given indentation String.
   * 
   * @param aSb - the indentation StringBuffer
   */
  private static void decrementIndent(final StringBuffer aSb) {
    final int len = aSb.length() - CodeScanner.getIndentString().length();
    if (len >= 0) {
      aSb.setLength(len);
    }
  }

}
