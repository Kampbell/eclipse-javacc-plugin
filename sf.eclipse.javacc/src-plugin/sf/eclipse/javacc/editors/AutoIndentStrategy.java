package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.preferences.IPrefConstants.P_NO_ADV_AUTO_INDENT;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.handlers.Format;
import sf.eclipse.javacc.scanners.CodeColorScanner;

/**
 * Auto indent strategy sensitive to newlines, braces, parenthesis, vertical bar, angle brackets and colons.
 * 
 * @see org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 * @author Bill Fenlason 2012
 */
class AutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

  // MMa 11/2009 : javadoc and formatting revision ; removed newlines around '(' and ')'
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed NPE
  // BF  05/2012 : removed reference to TokenRule
  // BF  06/2012 : removed unnecessary code to avoid warning message
  // MMa 07/2012 : fixed < < and > > to << and >>
  // MMa 10/2012 : renamed
  // MMa 02/2016 : fixed a bad location exception ; changed handling of '{', '}', '<'

/**
   * Customizes indentation after a newline, '{', '}', '(', ')', '|', '<', '>', ':' according to indentation used in {@link Format}
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void customizeDocumentCommand(final IDocument aDoc, final DocumentCommand aCmd) {
    final boolean noAdvancedAutoInd = AbstractActivator.getDefault().getPreferenceStore()
                                                       .getBoolean(P_NO_ADV_AUTO_INDENT);
    if (noAdvancedAutoInd) {
      if (aCmd.length == 0 && aCmd.text != null && endsWithDelimiter(aDoc, aCmd.text)) {
        basicIndentAfterNewLine(aDoc, aCmd);
      }
    }
    else {
      if (aCmd.length == 0 && aCmd.text != null && endsWithDelimiter(aDoc, aCmd.text)) {
        smartIndentAfterNewLine(aDoc, aCmd);
      }
      else if ("{".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftBrace(aDoc, aCmd);
      }
      else if ("}".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightBrace(aDoc, aCmd);
      }
      else if ("(".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftPar(aDoc, aCmd);
      }
      else if (")".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightPar(aDoc, aCmd);
      }
      else if ("|".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterVertBar(aDoc, aCmd);
      }
      else if ("<".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftAngleBracket(aDoc, aCmd);
      }
      else if (">".equals(aCmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightAngleBracket(aDoc, aCmd);
      }
    }
  }

  /**
   * Sets the basic indentation of a new line based on the command provided in the document.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void basicIndentAfterNewLine(final IDocument aDoc, final DocumentCommand aCmd) {
    final int docLength = aDoc.getLength();
    if (aCmd.offset == -1 || docLength == 0) {
      return;
    }
    try {
      // line is the line number of the newline character
      final int line = aDoc.getLineOfOffset(aCmd.offset);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, aCmd.offset);
      // currIndent is the current line indentation string
      final String currIndent = aDoc.get(startPos, firstNonWS - startPos);
      // keep current indentation and add it to the command
      // set the replacement document command text
      aCmd.text += currIndent;
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Sets the indentation of a new line based on the command provided in the document.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartIndentAfterNewLine(final IDocument aDoc, final DocumentCommand aCmd) {
    final int docLength = aDoc.getLength();
    if (aCmd.offset == -1 || docLength == 0) {
      return;
    }
    int p = 0;
    try {
      // p is the position of the newline character in the modified document
      p = aCmd.offset;
      // line is the line number of the newline character
      final int line = aDoc.getLineOfOffset(aCmd.offset);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, aCmd.offset);
      // currIndent is the current line indentation string
      final String currIndent = aDoc.get(startPos, firstNonWS - startPos);
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the newline
        // find last non space not tab character
        char c;
        while (true) {
          c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            break;
          }
        }
        if (c == '{' || c == '(') {
          // last character was a left brace or parenthesis, so we must increment indentation and
          // add it to the command
          sb.append(aCmd.text).append(currIndent).append(CodeColorScanner.getIndentString());
        }
        else {
          // otherwise keep current indentation and add it to the command
          sb.append(aCmd.text).append(currIndent);
        }
      }
      else {
        // case newline at the beginning of a line (after whitespaces)
        // we assume current indentation is OK
        // just add the current indentation to the command
        sb.append(aCmd.text).append(currIndent);
      }
      // remove trailing whitespaces
      p = aCmd.offset;
      while (p > 0) {
        final char c = aDoc.getChar(--p);
        if (c != ' ' && c != '\t') {
          ++p;
          break;
        }
      }
      // modify the document command length and offset
      aCmd.length += (aCmd.offset - p);
      aCmd.offset = p;
      // set the replacement document command text
      aCmd.text = sb.toString();
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, aCmd.offset, p);
    }
  }

  /**
   * Inserts a left brace according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterLeftBrace(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '{' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '{' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      // currIndent is the current line indentation string
      final String currIndent = aDoc.get(startPos, firstNonWS - startPos);
      // eol is the newline string for the current line
      //      final String eol = aDoc.getLegalLineDelimiters()[0];
      final String eol = aDoc.getLineDelimiter(line);
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '{'
        //        // add a new line, the previous indentation, the left brace, a new line and an incremented indentation
        //        sb.append(eol).append(currIndent).append('{');
        //        sb.append(eol).append(currIndent).append(CodeColorScanner.getIndentString());
        // add a space if necessary, the left brace and a space
        sb.append(p == firstNonWS ? "" : " ").append("{ "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // "remove" trailing whitespaces
        while (true) {
          final char c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        // modify the document command length and offset
        aCmd.length += (aCmd.offset - p);
        aCmd.offset = p;
      }
      else {
        // case '{' at the beginning of a line (after whitespaces)
        // we assume current indentation is ok
        // indentString is the indentation string derived from the preferences
        final String indentString = CodeColorScanner.getIndentString();
        final int indLen = indentString.length();
        if (p <= startPos + indLen) {
          // add the left brace, a new line and an incremented indentation
          sb.append('{');
          sb.append(eol).append(currIndent).append(indentString);
        }
        else {
          // add the left brace and a space
          sb.append("{ "); //$NON-NLS-1$
        }
      }
      // set the replacement document command text
      aCmd.text = sb.toString();
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a right brace according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterRightBrace(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '}' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '}' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      // currIndent is the current line indentation string
      //      final String currIndent = aDoc.get(startPos, firstNonWS - startPos);
      // eol is the newline string
      //      final String eol = aDoc.getLegalLineDelimiters()[0];
      // indentString is the indentation string derived from the preferences
      final String indentString = CodeColorScanner.getIndentString();
      final int indLen = indentString.length();
      // nextIndent is the current and decremented indentation
      //      final int len = currIndent.length() - indLen;
      //      final String nextIndent = (len > 0 ? currIndent.substring(0, len) : ""); //$NON-NLS-1$
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '}'
        //        // add a new line, the current and decremented indentation, the right brace, a new line and
        //        // the current and decremented indentation
        //        sb.append(eol).append(nextIndent).append('}');
        //        sb.append(eol).append(nextIndent);
        //         add a space, the right brace
        sb.append(" }"); //$NON-NLS-1$
        // "remove" trailing whitespaces
        while (true) {
          final char c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        // modify the document command length and offset
        aCmd.length += (aCmd.offset - p);
        aCmd.offset = p;
        // set the replacement document command text
        aCmd.text = sb.toString();
      }
      else {
        // case '}' at the beginning of a line (after whitespaces)
        // we must decrement the current indentation and keep the right brace
        int k = 0;
        while (k < indLen) {
          final char c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            p++;
            break;
          }
          k++;
        }
        // modify the document command length and offset
        aCmd.length += (aCmd.offset - p);
        aCmd.offset = p;
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a left parenthesis according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterLeftPar(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '(' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '(' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      // currIndent is the current line indentation string
      final String currIndent = aDoc.get(startPos, firstNonWS - startPos);
      // eol is the newline string
      final String eol = aDoc.getLegalLineDelimiters()[0];
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '('
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        // case '(' not at the beginning of a line (after whitespaces), do nothing
        //        if (Character.isJavaIdentifierPart(c)) {
        //          // '(' is probably at the beginning of a parameter list, so do nothing
        //        } else {
        //          // otherwise '(' is probably at the beginning of a choice
        //          // add a new line, the previous indentation, the left parenthesis, a new line and
        //          // an incremented indentation
        //          sb.append(eol).append(currIndent).append('(');
        //          sb.append(eol).append(currIndent).append(CodeScanner.getIndentString());
        //          // modify the document command length and offset
        //          cmd.length += (cmd.offset - p);
        //          cmd.offset = p;
        //          // set the replacement document command text
        //          cmd.text = sb.toString();
        //        }
      }
      else {
        // case '(' at the beginning of a line (after whitespaces)
        // assume current indentation is OK
        // add the left parenthesis, a new line and an incremented indentation
        sb.append('(');
        sb.append(eol).append(currIndent).append(CodeColorScanner.getIndentString());
        // set the replacement document command text
        aCmd.text = sb.toString();
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a right parenthesis according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterRightPar(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the ')' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the ')' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      // indentString is the indentation string derived from the preferences
      final String indentString = CodeColorScanner.getIndentString();
      // nextIndent is the current and decremented indentation
      final int indLen = indentString.length();
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the ')'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        // case ')' not at the beginning of a line (after whitespaces), do nothing
        //        if (c == '(' || Character.isJavaIdentifierPart(c)) {
        //          // ')' is probably at the end of a parameter list, so do nothing
        //        } else {
        //          // add a new line, the current and decremented indentation, the right parenthesis
        //          sb.append(eol).append(nextIndent).append(')');
        //          // modify the document command length and offset
        //          cmd.length += (cmd.offset - p);
        //          cmd.offset = p;
        //          // set the replacement document command text
        //          cmd.text = sb.toString();
        //        }
      }
      else {
        // case ')' at the beginning of a line (after whitespaces)
        // we must decrement the current indentation and keep the right parenthesis
        int k = 0;
        while (k < indLen) {
          final char c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            p++;
            break;
          }
          k++;
        }
        // modify the document command length and offset
        aCmd.length += (aCmd.offset - p);
        aCmd.offset = p;
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a vertical bar according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterVertBar(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '|' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '|' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      // indentString is the indentation string derived from the preferences
      final String indentString = CodeColorScanner.getIndentString();
      // nextIndent is the current and decremented indentation
      final int indLen = indentString.length();
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '|', do nothing
      }
      else {
        // case '|' at the beginning of a line (after whitespaces)
        // look at previous line
        final int sp = aDoc.getLineOffset(line - 1);
        final int ws = findEndOfWhiteSpace(aDoc, sp, p);
        if (aDoc.getChar(ws) == '|') {
          // case '|' is probably aligned with the '|' of the previous line,
          // so keep the vertical bar and add the special indentation
          // set the replacement document command text
          aCmd.text += CodeColorScanner.getSpecIndentString();
        }
        else {
          // case '|' is not after a previous line with a '|', so must decrement the current indentation,
          // keep the vertical bar and add the special indentation
          int k = 0;
          while (k < indLen) {
            final char c = aDoc.getChar(--p);
            if (c != ' ' && c != '\t') {
              p++;
              break;
            }
            k++;
          }
          // modify the document command length and offset
          aCmd.length += (aCmd.offset - p);
          aCmd.offset = p;
          // set the replacement document command text
          aCmd.text += CodeColorScanner.getSpecIndentString();
        }
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a left angle bracket according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterLeftAngleBracket(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '<' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '<' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character
      // after the last character of the line leading whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '<'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        if (c == '{' || c == '(' || c == ')' || c == '*' || c == '+' || c == '?' || c == '|' || c == '"'
            || c == '=' || c == ':') {
          // case after some JavaCC punctuation, so add a space
          // set the replacement document command text
          aCmd.text = "< "; //$NON-NLS-1$
        }
        else if (c == '<') {
          // case < < or <<, so remove possible space
          aCmd.offset = p;
        }
        else {
          // case probably in a Java expression, so do nothing
        }
      }
      else {
        // case '<' at the beginning of a line (after whitespaces), so add a space
        // set the replacement document command text
        aCmd.text = "< "; //$NON-NLS-1$
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Inserts a right angle bracket according to line and indentation formatting rules.
   * 
   * @param aDoc - the document being parsed
   * @param aCmd - the command being performed
   */
  void smartInsertAfterRightAngleBracket(final IDocument aDoc, final DocumentCommand aCmd) {
    if (aCmd.offset == -1 || aDoc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '>' character in the modified document
      int p = aCmd.offset;
      // line is the line number of the '>' character
      final int line = aDoc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = aDoc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(aDoc, startPos, p);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '>'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = aDoc.getChar(--p);
          if (c != ' ' && c != '\t') {
            ++p;
            break;
          }
        }
        if (c == '>') {
          // case > > or >>, so remove possible space
          aCmd.offset = p;
        }
        else if (c == '(') {
          // case in a Greater-than node, so do nothing
        }
        else {
          // all other cases, so remove previous whitespaces and prepend a space
          // modify the document command length and offset
          aCmd.length += (aCmd.offset - p);
          aCmd.offset = p;
          // set the replacement document command text
          aCmd.text = " >"; //$NON-NLS-1$
        }
      }
      else {
        // case '>' at the beginning of a line (after whitespaces), so do nothing
      }
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
  }

  /**
   * Returns whether or not the text ends with one of the document end of line delimiters.
   * 
   * @param aDoc - the document
   * @param aTxt - the text
   * @return true if the text ends with one of the document end of line delimiters, false otherwise
   */
  static boolean endsWithDelimiter(final IDocument aDoc, final String aTxt) {
    final String[] delimiters = aDoc.getLegalLineDelimiters();
    for (int i = 0; i < delimiters.length; i++) {
      if (aTxt.endsWith(delimiters[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the line number of the corresponding left brace.
   * 
   * @param aDoc - the document being parsed
   * @param aStartLine - the line to start searching back from (modified)
   * @param aEndPos - the end position to search back from (modified)
   * @param aRightBracesCount - the number of braces to skip
   * @return the line number of the next matching brace after end
   * @throws BadLocationException - if not in the right place
   */
  static int findMatchingLeftBrace(final IDocument aDoc, final int aStartLine, final int aEndPos,
                                   final int aRightBracesCount) throws BadLocationException {
    int ln = aStartLine;
    int startPos = aDoc.getLineOffset(ln);
    int brackcount = getBracesCount(aDoc, startPos, aEndPos, false) - aRightBracesCount;
    // Sums up the braces counts of each line (right braces count negative,
    // left positive) until we find a line the brings the count to zero
    while (brackcount < 0) {
      ln--;
      if (ln < 0) {
        return -1;
      }
      startPos = aDoc.getLineOffset(ln);
      final int ep = startPos + aDoc.getLineLength(ln) - 1;
      brackcount += getBracesCount(aDoc, startPos, ep, false);
    }
    return ln;
  }

  /**
   * Returns the brace value of a section of text. Right braces have a value of -1 and left braces have a
   * value of 1.
   * 
   * @param aDoc - the document being parsed
   * @param aStartPos - the start position for the search
   * @param aEndPos - the end position for the search
   * @param aIgnoreRightBraces - whether or not to ignore right braces in the count
   * @return the line number of the next matching brace after end
   * @throws BadLocationException - if not in the right place
   */
  static int getBracesCount(final IDocument aDoc, final int aStartPos, final int aEndPos,
                            final boolean aIgnoreRightBraces) throws BadLocationException {
    int p = aStartPos;
    int bracesCount = 0;
    boolean ignore = aIgnoreRightBraces;
    while (p < aEndPos) {
      final char c = aDoc.getChar(p);
      p++;
      switch (c) {
        case '/':
          if (p < aEndPos) {
            final char n = aDoc.getChar(p);
            if (n == '*') {
              // a comment starts, advance to the comment end
              p = getCommentEnd(aDoc, p + 1, aEndPos);
            }
            else if (n == '/') {
              // '//'-comment: nothing to do anymore on this line
              p = aEndPos;
            }
          }
          break;
        case '*':
          if (p < aEndPos) {
            final char n = aDoc.getChar(p);
            if (n == '/') {
              // we have been in a comment: forget what we read before
              bracesCount = 0;
              p++;
            }
          }
          break;
        case '{':
          bracesCount++;
          ignore = false;
          break;
        case '}':
          if (!ignore) {
            bracesCount--;
          }
          break;
        case '"':
        case '\'':
          p = getStringEnd(aDoc, p, aEndPos, c);
          break;
        default:
      }
    }
    return bracesCount;
  }

  /**
   * Returns the given line indentation string (the first ' ' and '\t' of the line).
   * 
   * @param aDoc - the document being parsed
   * @param aLine - the line being searched
   * @return the line indentation string
   * @throws BadLocationException - if not in the right place
   */
  String getLineIndent(final IDocument aDoc, final int aLine) throws BadLocationException {
    if (aLine < 0) {
      return ""; //$NON-NLS-1$
    }
    final int start = aDoc.getLineOffset(aLine);
    final int end = start + aDoc.getLineLength(aLine) - 1;
    final int wsEnd = findEndOfWhiteSpace(aDoc, start, end);
    return aDoc.get(start, wsEnd - start);
  }

  /**
   * Returns the end position of a comment starting at a given position.
   * 
   * @param aDoc - the document being parsed
   * @param aStartPos - the start position for the search
   * @param aEndPos - the end position for the search
   * @return the end position of a comment starting at the start position
   * @throws BadLocationException - if not in the right place
   */
  static int getCommentEnd(final IDocument aDoc, final int aStartPos, final int aEndPos)
                                                                                        throws BadLocationException {
    int p = aStartPos;
    while (p < aEndPos) {
      final char c = aDoc.getChar(p);
      p++;
      if (c == '*') {
        if (p < aEndPos && aDoc.getChar(p) == '/') {
          return p + 1;
        }
      }
    }
    return aEndPos;
  }

  /**
   * Returns the first position of a given non escaped character in a given document range.
   * 
   * @param aDoc - the document being parsed
   * @param aStartPos - the position to start searching from
   * @param aEndPos - the position to end searching to
   * @param aCh - the character to try to find
   * @return the first location of the character searched
   * @throws BadLocationException - if not in the right place
   */
  static int getStringEnd(final IDocument aDoc, final int aStartPos, final int aEndPos, final char aCh)
                                                                                                       throws BadLocationException {
    int p = aStartPos;
    while (p < aEndPos) {
      final char c = aDoc.getChar(p);
      p++;
      if (c == '\\') {
        // ignore escaped characters
        p++;
      }
      else if (c == aCh) {
        return p;
      }
    }
    return aEndPos;
  }
}
