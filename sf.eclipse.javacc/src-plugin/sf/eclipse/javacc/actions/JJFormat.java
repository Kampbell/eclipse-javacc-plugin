package sf.eclipse.javacc.actions;

import java.io.StringReader;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.editors.JJCodeScanner;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.JavaCCParserConstants;
import sf.eclipse.javacc.parser.Token;

/**
 * Format action referenced by plugin.xml
 * For popup menu on Editor
 *  <extension point="org.eclipse.ui.popupMenus">
 * For key binding
 *  <extension point="org.eclipse.ui.editorActions">
 *
 * @author Remi Koutcherawy 2003-2006 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
/*
 * ModMMa : performance improvements and enhanced reformatting
 */
public class JJFormat implements IEditorActionDelegate, JavaCCParserConstants, IJJConstants {
  /** The editor */
  static JJEditor  editor;
  /** The document */
  static IDocument doc;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
    doc = editor.getDocument();
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(final IAction action, final ISelection selection) {
    // not used
  }

  /**
   * Perform Formatting.
   *
   * @see IActionDelegate#run(IAction)
   */
  public void run(final IAction action) {
    if (editor == null)
      return;
    final ISelection selection = editor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection))
      return;
    ITextSelection ts = (ITextSelection) selection;
    int tssl = ts.getStartLine();
    int tsel = ts.getEndLine();
    int tslen = ts.getLength();
    if (tssl < 0 || tsel < 0)
      return;
    try {
      // if No selection, treat full text
      if (tslen == 0) {
        ts = new TextSelection(doc, 0, doc.getLength());
        tssl = ts.getStartLine();
        tsel = ts.getEndLine();
        tslen = ts.getLength();
      }
      // If partial lines are selected, extend selection
      final IRegion endLine = doc.getLineInformation(tsel);
      final IRegion startLine = doc.getLineInformation(tssl);
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset() + endLine.getLength() - startLine.getOffset());
      tssl = ts.getStartLine();
      tsel = ts.getEndLine();
      tslen = ts.getLength();
      // Format the selection full text
      // The tricky part is to replace only part of the full text
      // we need to process the editor full text using the JavaCC grammar
      // and we have to replace only part of it.
      final String endLineDelim = doc.getLegalLineDelimiters()[0];
      final StringBuffer strbuf = new StringBuffer(2 * tslen);
      final String docText = doc.get();
      if (formatSelection(docText, endLineDelim, tssl + 1, tsel + 1, strbuf) == true) {
        // Replace the text with the modified version
        doc.replace(startLine.getOffset(), tslen, strbuf.toString());
      }
      // Reselect text... not exactly as JavaEditor... whole text here
      // editor.selectAndReveal(startLine.getOffset(), strbuf.length());
    } catch (final Exception e) {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final Shell shell = workbench.getDisplay().getActiveShell();
      final MessageDialog dialog = new MessageDialog(shell,
                                                     Activator.getString("JJFormat.Bug"), //$NON-NLS-1$
                                                     null,
                                                     fmtStackTrace(e, doc.getLegalLineDelimiters()[0]),
                                                     MessageDialog.QUESTION,
                                                     new String[] {
                                                       IDialogConstants.OK_LABEL },
                                                     0);
      dialog.open();
    }
    return;
  }

  /**
   * Formats the stacktrace in lines.
   *
   * @param ex the exception
   * @param eol the end of line string
   * @return the formatted stacktrace
   */
  static String fmtStackTrace(final Exception ex, final String eol) {
    final StringBuffer sb = new StringBuffer(2048);
    final StackTraceElement[] st = ex.getStackTrace();
    final int len = st.length;
    for (int i = 0; i < len; i++) {
      sb.append(st[i].toString()).append(eol);
    }
    return sb.toString();
  }

  /** empty string */
  public static final String EMPTY = "";  //$NON-NLS-1$
  /** space string */
  public static final String SPACE = " "; //$NON-NLS-1$
  /** tab string */
  public static final String TAB   = "\t"; //$NON-NLS-1$
  /** carriage return string */
  public static final String CR    = "\r"; //$NON-NLS-1$
  /** line feed string */
  public static final String LF    = "\n"; //$NON-NLS-1$
  /** form feed string */
  public static final String FF    = "\f"; //$NON-NLS-1$
  /** number sign string */
  public static final String NB    = "#"; //$NON-NLS-1$

  /**
   * Formats the selected text.
   * <p>
   * It reformats the indentation and spacing : it uses spaces to distinct constructs, and it tries to keep
   * comments and newlines (except around braces and parenthesis) as they are
   *
   * @param txt the text to format
   * @param endLineDelim the end of line delimiter string
   * @param firstLine the line number of the first character of the selected text
   * @param lastLine the line number of the last character of the selected text
   * @param sb the StringBuffer to receive the formatted text
   * @return
   */
  protected boolean formatSelection(final String txt, final String endLineDelim, final int firstLine,
                                    final int lastLine, final StringBuffer sb) {
    // Parse the full text, retain only the chain of Tokens
    final StringReader in = new StringReader(txt);
    final JJNode node = JavaCCParser.parse(in);
    in.close();
    if (node.getFirstToken().next == null) {
      // Warn Nothing shall be done if parsing failed.
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final Shell shell = workbench.getDisplay().getActiveShell();
      final MessageDialog dialog = new MessageDialog(shell, Activator.getString("JJFormat.0"), //$NON-NLS-1$
                                                     null, Activator.getString("JJFormat.1"), //$NON-NLS-1$
                                                     MessageDialog.QUESTION, new String[] {
                                                       IDialogConstants.OK_LABEL }, 0);
      dialog.open();
      return false;
    }
    /** the current token */
    Token currToken = node.getFirstToken();
    /** the next token */
    Token nextToken = (currToken == null ? null : currToken.next);
    /** a special token */
    Token specToken = null;
    /** the last token kind */
    int lastKind = -1;
    /** the current token kind */
    int currKind = (currToken == null ? -1 : currToken.kind);
    /** the next token kind */
    int nextKind = (nextToken == null ? -1 : nextToken.kind);
    /** the special token kind */
    int specKind;
    /** the current token image */
    String currImage;
    /** the next token image */
    String nextImage = EMPTY;
    /** the special token image */
    String specImage;
    /** number of (next token's) special tokens up to the end of line already processed */
    int nbSpecialToken = 0;
    /** the current line indentation */
    final StringBuffer currLineIndent = new StringBuffer(64);
    /** the next line indentation */
    final StringBuffer nextLineIndent = new StringBuffer(64);
    /** debug newlines */
    final boolean debugNL = false;
    /** debug indentation */
    final boolean debugInd = false;
    /** flag telling if at least one newline is needed after the current token */
    boolean needOneNewline = false;
    /** flag telling if two newlines are needed after the current token */
    boolean needTwoNewlines = false;
    /** flag telling if one newline will be needed at the end of the current line */
    boolean willNeedOneNewline = false;
    /** flag telling if two newlines will be needed at the end of the current line */
    boolean willNeedTwoNewlines = false;
    /** flag telling that a newline has just been written after the last token */
    boolean newlineJustWritten = false;
    /** flag telling that the need for a newline is postponed after encountering some token */
    boolean newLinePostponed = false;
    /** flag memorizing the need for a newline in case it is postponed */
    boolean prevNeedOneNewline = false;
    /**
     * flag memorizing the need to not output the first newlines & special tokens at the beginning of the
     * selection if the selection does not start at the first line of the document
     */
    boolean careFirstLine = (firstLine > 1);
    /** flag telling to not output anything because of outside the selected text */
    boolean skipOutput;
    /** buffer length at the last line */
    int lastSbLength = 0;
    /** flag telling if a space is needed after the current token */
    boolean needSpace = false;
    /** flag telling if no space is needed for some special cases */
    boolean needNoSpace = false;
    /**
     * false if in javacc/jjtree "sections" before parser_end (options, parser_begin), true otherwise (token,
     * special_token, skip, more, productions)
     */
    boolean isAfterParserEnd = false;
    /**
     * parenthesis level in lookahead constraints : -1 : outside ; 0 : at lookahead token ; >= 1 : inside, '('
     * and ')' included
     */
    int parLevelInLAC = -1;
    /** last parenthesis is LPAREN, not RPAREN */
    boolean lastParLPnotRP = false;
    /** last parenthesis is LPAREN, not RPAREN nor BIT_OR */
    boolean lastParLPnotRPnorBO = false;
    /** last straight bracket is LBRACKET, not RBRACKET nor BIT_OR not LOOKAHEAD nor 2 LPAREN */
    boolean lastBraLBnotOthers = false;
    /** current braces indentation level (changed at each '{' and '}') */
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
        // parenthesis level
        if (currKind == LPAREN) {
          lastParLPnotRP = true;
        } else if (currKind == RPAREN) {
          lastParLPnotRP = false;
        }
        // parenthesis level in lookahead constraints
        if (currKind == _LOOKAHEAD) {
          parLevelInLAC = 0;
        } else if (currKind == LPAREN && parLevelInLAC >= 0) {
          parLevelInLAC++;
        } else if (currKind == RPAREN && parLevelInLAC >= 0) {
          parLevelInLAC--;
        }
        // braces level (not in lookahead constraints)
        if (currKind == LBRACE && parLevelInLAC < 0) {
          bracesIndentLevel++;
        } else if (currKind == RBRACE && parLevelInLAC < 0) {
          bracesIndentLevel--;
        }
      }
      /*
       * compute the need for newline(s) and indentation
       */
      // for some keywords, need for later one or two newlines and no space
      if (currKind == _PARSER_BEGIN || currKind == _LOOKAHEAD) {
        willNeedOneNewline = true;
        needNoSpace = true;
      } else if (currKind == _PARSER_END) {
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
          nextLineIndent.append(JJCodeScanner.getIndentString());
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
          && ((currKind == SEMICOLON && !lastParLPnotRP) || (nextKind == LBRACE && currKind != RBRACE) || (nextKind == RBRACE
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
        if (currKind == COLON && nextKind != IDENTIFIER && nextKind != LBRACE) {
          Token nt = nextToken;
          Token pt = currToken;
          int gtltLevel = 1;
          int parLevel = 0;
          while (nt != null) {
            if (nt.kind == GT && pt.kind != LPAREN) {
              if (--gtltLevel == 0) {
                break;
              }
            } else if (nt.kind == LT) {
              gtltLevel++;
            } else if (nt.kind == RPAREN) {
              parLevel--;
            } else if (nt.kind == LPAREN && ++parLevel > 1) {
              needOneNewline = true;
              break;
            } else if (nt.kind == BIT_OR) {
              needOneNewline = true;
              break;
            }
            pt = nt;
            nt = nt.next;
          }
        }
        // after a '(' and not in lookahead constraints and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline and increment indentation
        if (currKind == LPAREN && parLevelInLAC < 0 && bracesIndentLevel <= 1) {
          lastParLPnotRPnorBO = true;
          Token nt = nextToken;
          while (nt != null) {
            if (nt.kind == RPAREN) {
              break;
            } else if (nt.kind == LPAREN || nt.kind == BIT_OR) {
              needOneNewline = true;
              lastParLPnotRPnorBO = false;
              nextLineIndent.append(JJCodeScanner.getIndentString());
              // currLineIndent will be set at the end of the loop
              break;
            }
            nt = nt.next;
          }
        }
        // after a ')' and before a TRY (in expansion_unit), need for a newline,
        // after a ')' and not in lookahead constraints and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline and decrement indentation,
        // and if followed by a '>', a '*', a '?' or a '+', postpone the need for a newline
        if (currKind == RPAREN && parLevelInLAC < 0 && bracesIndentLevel <= 1) {
          if (nextKind == TRY) {
            needOneNewline = true;
          } else if (!lastParLPnotRPnorBO) {
            needOneNewline = true;
            decrementIndent(nextLineIndent);
            // currLineIndent will be set at the end of the loop
          }
          lastParLPnotRPnorBO = false;
          if (NB.equals(nextImage)) {
            needOneNewline = false;
          } else if (nextKind == GT || nextKind == STAR || nextKind == HOOK || nextKind == PLUS) {
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
        // before a '(' and not in lookahead constraints and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline
        if (nextKind == LPAREN && parLevelInLAC < 0 && bracesIndentLevel <= 1) {
          boolean nextParenLeftNorRight = false;
          Token nt = nextToken;
          if (nt != null) {
            nt = nt.next;
            while (nt != null) {
              if (nt.kind == RPAREN) {
                nextParenLeftNorRight = false;
                break;
              } else if (nt.kind == LPAREN || nt.kind == BIT_OR) {
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
        // before a ')' and not in lookahead constraints and at most at the first braces level,
        // if not at the last enclosing level, or if at the last enclosing level with an inner '|',
        // need for a newline
        if (nextKind == RPAREN && parLevelInLAC < 0 && bracesIndentLevel <= 1) {
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
            } else if (nt.kind == RPAREN) {
              nbParen--;
            } else if (nt.kind == RBRACKET) {
              break;
            }
            if (nt.kind == LBRACE || nt.kind == _LOOKAHEAD || nt.kind == LBRACKET || nt.kind == BIT_OR
                || (nt.kind == LPAREN && nbParen > 1)) {
              needOneNewline = true;
              lastBraLBnotOthers = false;
              nextLineIndent.append(JJCodeScanner.getIndentString());
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
              } else if (nt.kind == RPAREN) {
                nbParen--;
              } else if (nt.kind == RBRACKET) {
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
        // increment indentation for a '<', and decrement indentation for a '>'
        if (currKind == LT) {
          nextLineIndent.append(JJCodeScanner.getIndentString());
          // currLineIndent will be set at the end of the loop
        } else if (currKind == GT && lastKind != LPAREN) {
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
      // if before a newline
      // for a '{}' pair
      // after some operators or punctuation '(', '[' (unless in last sections), '.', '!', '~',
      // '*' (unless in last sections), '/'
      // before some operators or punctuation ')', ']' (unless in last sections), ';', ',', '.',
      // '++', '--', '*', '/'
      // before an argument list
      // after some keywords
      // after a ':' if followed by a '{'
      // after some javacc operators or punctuation '#', '^'
      // before some javacc operators or punctuation '?', '*', '+'
      // before and after a javacc '-'
      // after the javacc construct '(>'
      // TODO for generics syntax, boolean expressions with '<' or '>'
      if (needOneNewline
          || (currKind == LBRACE && nextKind == RBRACE)
          || currKind == LPAREN
          || (currKind == LBRACKET && !isAfterParserEnd)
          || currKind == DOT
          || currKind == BANG
          || currKind == TILDE
          || (currKind == STAR && !isAfterParserEnd)
          || currKind == SLASH
          || nextKind == RPAREN
          || (nextKind == RBRACKET && !isAfterParserEnd)
          || nextKind == SEMICOLON
          || nextKind == COMMA
          || nextKind == DOT
          || nextKind == INCR
          || nextKind == DECR
          || nextKind == STAR
          || nextKind == SLASH
          || (nextKind == LPAREN && currKind == IDENTIFIER)
          || currKind == _PARSER_BEGIN
          || currKind == _PARSER_END
          || currKind == _LOOKAHEAD
          || (currKind == COLON && nextKind == LBRACE)
          || (isAfterParserEnd && (NB.equals(currImage) || nextKind == XOR || nextKind == HOOK
                                   || nextKind == STAR || nextKind == PLUS
                                   || ((currKind == MINUS || nextKind == MINUS) && bracesIndentLevel <= 1) || (lastKind == LPAREN && currKind == GT)))) {
        needSpace = false;
      }
      /*
       * now output everything, but only for tokens and special tokens in the selected text, and take care of
       * first newlines of the selection (but need to compute flags in all cases)
       */
      // set output flag
      if (currToken.beginLine >= firstLine && currToken.endLine <= lastLine) {
        if (careFirstLine) {
          skipOutput = true;
        } else {
          skipOutput = false;
        }
      } else {
        skipOutput = true;
      }
      // for a previous ')' not in lookahead constraints, if memorized,
      // output the one or two newlines
      if (lastKind == RPAREN) {
        if (willNeedOneNewline && parLevelInLAC < 0) {
          if (debugNL && !skipOutput) {
            sb.append("\t\t/* rp, W n 1 n l A */"); // $NON-NLS-1$
          }
          if (!skipOutput) {
            sb.append(endLineDelim);
          }
          willNeedOneNewline = false;
          newlineJustWritten = true;
        }
        if (willNeedTwoNewlines && parLevelInLAC < 0) {
          if (debugNL && !skipOutput) {
            sb.append("\t\t/* rp,  W n 2 n l A */"); // $NON-NLS-1$
          }
          if (!skipOutput) {
            sb.append(endLineDelim);
          }
          willNeedTwoNewlines = false;
          newlineJustWritten = true;
        }
      }
      // process the (previous) special token(s) (only those not already processed)
      specToken = currToken.specialToken;
      if (specToken != null) {
        // rewind to the first
        while (specToken.specialToken != null)
          specToken = specToken.specialToken;
        boolean afterNewline = newlineJustWritten;
        // examine each
        while (specToken != null) {
          if (nbSpecialToken > 0) {
            // skip those already processed
            nbSpecialToken--;
          } else {
            // output others only if comments in the selection
            specKind = specToken.kind;
            specImage = specToken.image;
            if ((specKind == SINGLE_LINE_COMMENT || specKind == MULTI_LINE_COMMENT || specKind == FORMAL_COMMENT)
                && (specToken.beginLine >= firstLine)) {
              if (!afterNewline) {
                // output one a space before each comment
                if (!skipOutput) {
                  sb.append(SPACE);
                }
              } else {
                // output indentation
                if (debugInd && !skipOutput) {
                  final int len = sb.length() - endLineDelim.length();
                  if (len >= 0 && sb.substring(len).equals(endLineDelim)) {
                    sb.setLength(len);
                  }
                  sb.append("\t\t/* stcu */").append(endLineDelim); // $NON-NLS-1$
                }
                if (!skipOutput) {
                  sb.append(currLineIndent);
                }
              }
              // output the comment
              if (!skipOutput) {
                sb.append(specToken.toString());
              }
              // manage newlines
              if (specKind == SINGLE_LINE_COMMENT) {
                afterNewline = true;
                newlineJustWritten = true;
              } else {
                afterNewline = false;
              }
            } else if ((CR.equals(specImage) || LF.equals(specImage) || FF.equals(specImage))
                       && !afterNewline) {
              if (debugNL && !skipOutput) {
                sb.append("\t\t/* p s t */"); // $NON-NLS-1$
              }
              if (!skipOutput) {
                sb.append(endLineDelim);
              }
              afterNewline = true;
              newlineJustWritten = true;
            }
          }
          specToken = specToken.next;
        }
      } // end if (specToken != null)
      // reset flags after the special processing of the first line of the selected text
      if (currToken.beginLine >= firstLine && currToken.endLine <= lastLine && careFirstLine) {
        careFirstLine = false;
        skipOutput = false;
      }
      // for a previous ';', if memorized, output the newline
      if (lastKind == SEMICOLON && willNeedOneNewline) {
        if (debugNL && !skipOutput) {
          sb.append("\t\t/* sc, W n 1 n l A */"); // $NON-NLS-1$
        }
        if (!skipOutput) {
          sb.append(endLineDelim);
        }
        willNeedOneNewline = false;
        newlineJustWritten = true;
      }
      // update (if necessary) and output indentation if a newline has been written
      if (newlineJustWritten) {
        if (debugInd && !skipOutput) {
          final int len = sb.length() - endLineDelim.length();
          if (len >= 0 && sb.substring(len).equals(endLineDelim)) {
            sb.setLength(len);
          }
        }
        if (currKind == LBRACE || currKind == LPAREN || currKind == LBRACKET || currKind == LT
            || currKind == GT) {
          if (debugInd && !skipOutput) {
            sb.append("\t\t/* cunl, ").append(currLineIndent.length()); // $NON-NLS-1$
            sb.append(", ").append(nextLineIndent.length()); // $NON-NLS-1$
            sb.append(" */").append(endLineDelim); // $NON-NLS-1$
          }
          if (!skipOutput) {
            sb.append(currLineIndent);
          }
        } else if (currKind == BIT_OR) {
          if (debugInd && !skipOutput) {
            sb.append("\t\t/* boun, ").append(currLineIndent.length()); // $NON-NLS-1$
            sb.append(", ").append(nextLineIndent.length()); // $NON-NLS-1$
            sb.append(" */").append(endLineDelim); // $NON-NLS-1$
          }
          if (!skipOutput) {
            final int len = currLineIndent.length() - JJCodeScanner.getIndentString().length();
            if (len > 0) {
              sb.append(currLineIndent.substring(0, len));
            }
          }
        } else {
          if (debugInd && !skipOutput) {
            sb.append("\t\t/* nenl, ").append(currLineIndent.length()); // $NON-NLS-1$
            sb.append(", ").append(nextLineIndent.length()); // $NON-NLS-1$
            sb.append(" */").append(endLineDelim); // $NON-NLS-1$
          }
          if (!skipOutput) {
            sb.append(nextLineIndent);
          }
        }
      }
      // output the token
      if (!skipOutput) {
        sb.append(currToken.toString());
      }
      // output the space
      if (needSpace) {
        if (needNoSpace && currKind == RPAREN && parLevelInLAC <= 0) {
          needNoSpace = false;
        } else if (newlineJustWritten && currKind == BIT_OR) {
          // case of an unindented '|' : reindent
          if (!skipOutput) {
            sb.append(JJCodeScanner.getSpecIndentString());
          }
        } else {
          // other normal cases
          if (!skipOutput) {
            sb.append(SPACE);
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
          while (specToken.specialToken != null)
            specToken = specToken.specialToken;
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
                  sb.append(SPACE);
                }
              }
              // output the special token
              if (!skipOutput) {
                sb.append(specToken.toString());
              }
            }
            if (specKind == SINGLE_LINE_COMMENT) {
              // skip one newline need for a single line comment which already includes one
              if (willNeedTwoNewlines) {
                willNeedTwoNewlines = false;
              } else if (willNeedOneNewline) {
                willNeedOneNewline = false;
              } else if (needTwoNewlines) {
                needTwoNewlines = false;
              } else if (needOneNewline) {
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
        lastSbLength = sb.length();
      }
      // output the one or two newlines after
      if (needTwoNewlines) {
        if (debugNL && !skipOutput) {
          sb.append("\t\t/* n 2 n l A */"); // $NON-NLS-1$
        }
        if (!skipOutput) {
          sb.append(endLineDelim);
        }
        needTwoNewlines = false;
        newlineJustWritten = true;
      }
      if (needOneNewline) {
        if (debugNL && !skipOutput) {
          sb.append("\t\t/* n 1 n l A */"); // $NON-NLS-1$
        }
        if (!skipOutput) {
          sb.append(endLineDelim);
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
        currLineIndent.append(JJCodeScanner.getIndentString());
      } else if (nlen < clen) {
        currLineIndent.setLength(nlen);
      }
    } // end while (currToken != null && currToken.kind != EOF)
    // remove trailing newlines
    sb.setLength(lastSbLength);
    return true;
  }

  /**
   * Decrements indentation by shortening the given StringBuffer with the given indentation String.
   * 
   * @param lineIndent the indentation StringBuffer
   */
  void decrementIndent(final StringBuffer lineIndent) {
    final int len = lineIndent.length() - JJCodeScanner.getIndentString().length();
    if (len >= 0)
      lineIndent.setLength(len);
  }
}