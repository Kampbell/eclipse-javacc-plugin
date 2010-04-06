package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.actions.JJFormat;
import sf.eclipse.javacc.options.JJPreferences;

/**
 * Auto indent strategy sensitive to newlines, braces, parenthesis, vertical bar, angle brackets and colons.
 * 
 * @see org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy implements IAutoEditStrategy {

  // MMa 11/2009 : javadoc and formatting revision ; removed newlines around '(' and ')'
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : fixed NPE

/**
   * Customizes indentation after a newline, '{', '}', '(', ')', '|', '<', '>', ':' according to indentation used in {@link JJFormat}
   * 
   * @see IAutoEditStrategy#customizeDocumentCommand(IDocument, DocumentCommand)
   * @param doc the document
   * @param cmd the document command (the last character)
   */
  @Override
  public void customizeDocumentCommand(final IDocument doc, final DocumentCommand cmd) {
    final boolean noAdvancedAutoInd = Activator.getDefault().getPreferenceStore()
                                               .getBoolean(JJPreferences.P_NO_ADV_AUTO_INDENT);
    if (noAdvancedAutoInd) {
      if (cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text)) {
        basicIndentAfterNewLine(doc, cmd);
      }
    }
    else {
      if (cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text)) {
        smartIndentAfterNewLine(doc, cmd);
      }
      else if ("{".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftBrace(doc, cmd);
      }
      else if ("}".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightBrace(doc, cmd);
      }
      else if ("(".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftPar(doc, cmd);
      }
      else if (")".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightPar(doc, cmd);
      }
      else if ("|".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterVertBar(doc, cmd);
      }
      else if ("<".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterLeftAngleBracket(doc, cmd);
      }
      else if (">".equals(cmd.text)) { //$NON-NLS-1$
        smartInsertAfterRightAngleBracket(doc, cmd);
      }
    }
  }

  /**
   * Sets the basic indentation of a new line based on the command provided in the document.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void basicIndentAfterNewLine(final IDocument doc, final DocumentCommand cmd) {
    final int docLength = doc.getLength();
    if (cmd.offset == -1 || docLength == 0) {
      return;
    }
    try {
      // line is the line number of the newline character
      final int line = doc.getLineOfOffset(cmd.offset);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, cmd.offset);
      // currIndent is the current line indentation string
      final String currIndent = doc.get(startPos, firstNonWS - startPos);
      // keep current indentation and add it to the command
      // set the replacement document command text
      cmd.text += currIndent;
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the indentation of a new line based on the command provided in the document.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartIndentAfterNewLine(final IDocument doc, final DocumentCommand cmd) {
    final int docLength = doc.getLength();
    if (cmd.offset == -1 || docLength == 0) {
      return;
    }
    try {
      // p is the position of the newline character in the modified document
      int p = cmd.offset;
      // line is the line number of the newline character
      final int line = doc.getLineOfOffset(cmd.offset);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, cmd.offset);
      // currIndent is the current line indentation string
      final String currIndent = doc.get(startPos, firstNonWS - startPos);
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the newline
        // find last non space not tab character
        char c;
        while (true) {
          c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
            break;
          }
        }
        if (c == '{' || c == '(') {
          // last character was a left brace or parenthesis, so we must increment indentation and
          // add it to the command
          sb.append(cmd.text).append(currIndent).append(JJCodeScanner.getIndentString());
        }
        else {
          // otherwise keep current indentation and add it to the command
          sb.append(cmd.text).append(currIndent);
        }
      }
      else {
        // case newline at the beginning of a line (after whitespaces)
        // we assume current indentation is OK
        // just add the current indentation to the command
        sb.append(cmd.text).append(currIndent);
      }
      // remove trailing whitespaces
      p = cmd.offset;
      while (p >= 0) {
        final char c = doc.getChar(--p);
        if (c != ' ' && c != '\t') { // $NON-NLS-2$
          ++p;
          break;
        }
      }
      // modify the document command length and offset
      cmd.length += (cmd.offset - p);
      cmd.offset = p;
      // set the replacement document command text
      cmd.text = sb.toString();
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a left brace according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterLeftBrace(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '{' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '{' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      // currIndent is the current line indentation string
      final String currIndent = doc.get(startPos, firstNonWS - startPos);
      // eol is the newline string
      final String eol = doc.getLegalLineDelimiters()[0];
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '{'
        // add a new line, the previous indentation, the left brace, a new line and an incremented indentation
        sb.append(eol).append(currIndent).append('{');
        sb.append(eol).append(currIndent).append(JJCodeScanner.getIndentString());
        // "remove" trailing whitespaces
        while (true) {
          final char c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
            ++p;
            break;
          }
        }
        // modify the document command length and offset
        cmd.length += (cmd.offset - p);
        cmd.offset = p;
      }
      else {
        // case '{' at the beginning of a line (after whitespaces)
        // we assume current indentation is ok
        // add the left brace, a new line and an incremented indentation
        sb.append('{');
        sb.append(eol).append(currIndent).append(JJCodeScanner.getIndentString());
      }
      // set the replacement document command text
      cmd.text = sb.toString();
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a right brace according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterRightBrace(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '}' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '}' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      // currIndent is the current line indentation string
      final String currIndent = doc.get(startPos, firstNonWS - startPos);
      // eol is the newline string
      final String eol = doc.getLegalLineDelimiters()[0];
      // indentString is the indentation string derived from the preferences
      final String indentString = JJCodeScanner.getIndentString();
      // nextIndent is the current and decremented indentation
      final int indLen = indentString.length();
      final int len = currIndent.length() - indLen;
      final String nextIndent = (len > 0 ? currIndent.substring(0, len) : ""); //$NON-NLS-1$
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '}'
        // add a new line, the current and decremented indentation, the right brace, a new line and
        // the current and decremented indentation
        sb.append(eol).append(nextIndent).append('}');
        sb.append(eol).append(nextIndent);
        // "remove" trailing whitespaces
        while (true) {
          final char c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
            ++p;
            break;
          }
        }
        // modify the document command length and offset
        cmd.length += (cmd.offset - p);
        cmd.offset = p;
        // set the replacement document command text
        cmd.text = sb.toString();
      }
      else {
        // case '}' at the beginning of a line (after whitespaces)
        // we must decrement the current indentation and keep the right brace
        int k = 0;
        while (k < indLen) {
          final char c = doc.getChar(--p);
          if (c != ' ' && c != '\t') {
            p++;
            break;
          }
          k++;
        }
        // modify the document command length and offset
        cmd.length += (cmd.offset - p);
        cmd.offset = p;
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a left parenthesis according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterLeftPar(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '(' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '(' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      // currIndent is the current line indentation string
      final String currIndent = doc.get(startPos, firstNonWS - startPos);
      // eol is the newline string
      final String eol = doc.getLegalLineDelimiters()[0];
      // replacement buffer
      final StringBuffer sb = new StringBuffer(32);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '('
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
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
        //          sb.append(eol).append(currIndent).append(JJCodeScanner.getIndentString());
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
        sb.append(eol).append(currIndent).append(JJCodeScanner.getIndentString());
        // set the replacement document command text
        cmd.text = sb.toString();
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a right parenthesis according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterRightPar(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the ')' character in the modified document
      int p = cmd.offset;
      // line is the line number of the ')' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      // indentString is the indentation string derived from the preferences
      final String indentString = JJCodeScanner.getIndentString();
      // nextIndent is the current and decremented indentation
      final int indLen = indentString.length();
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the ')'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
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
          final char c = doc.getChar(--p);
          if (c != ' ' && c != '\t') {
            p++;
            break;
          }
          k++;
        }
        // modify the document command length and offset
        cmd.length += (cmd.offset - p);
        cmd.offset = p;
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a vertical bar according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterVertBar(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '|' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '|' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      // indentString is the indentation string derived from the preferences
      final String indentString = JJCodeScanner.getIndentString();
      // nextIndent is the current and decremented indentation
      final int indLen = indentString.length();
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '|', do nothing
      }
      else {
        // case '|' at the beginning of a line (after whitespaces)
        // look at previous line
        final int sp = doc.getLineOffset(line - 1);
        final int ws = findEndOfWhiteSpace(doc, sp, p);
        if (doc.getChar(ws) == '|') {
          // case '|' is probably aligned with the '|' of the previous line,
          // so keep the vertical bar and add the special indentation
          // set the replacement document command text
          cmd.text += JJCodeScanner.getSpecIndentString();
        }
        else {
          // case '|' is not after a previous line with a '|', so must decrement the current indentation,
          // keep the vertical bar and add the special indentation
          int k = 0;
          while (k < indLen) {
            final char c = doc.getChar(--p);
            if (c != ' ' && c != '\t') {
              p++;
              break;
            }
            k++;
          }
          // modify the document command length and offset
          cmd.length += (cmd.offset - p);
          cmd.offset = p;
          // set the replacement document command text
          cmd.text += JJCodeScanner.getSpecIndentString();
        }
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a left angle bracket according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterLeftAngleBracket(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '<' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '<' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '<'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
            ++p;
            break;
          }
        }
        if (JJTokenRule.isChoicesPunct(c) || c == '|' || c == '"' || c == '=' || c == ':') {
          // case after some JavaCC punctuation, so add a space
          // set the replacement document command text
          cmd.text = "< "; //$NON-NLS-1$
        }
        else {
          // case probably in a Java expression, so do nothing
        }
      }
      else {
        // case '<' at the beginning of a line (after whitespaces), so add a space
        // set the replacement document command text
        cmd.text = "< "; //$NON-NLS-1$
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Inserts a right angle bracket according to line and indentation formatting rules.
   * 
   * @param doc - the document being parsed
   * @param cmd - the command being performed
   */
  void smartInsertAfterRightAngleBracket(final IDocument doc, final DocumentCommand cmd) {
    if (cmd.offset == -1 || doc.getLength() == 0) {
      return;
    }
    try {
      // p is the position of the '>' character in the modified document
      int p = cmd.offset;
      // line is the line number of the '>' character
      final int line = doc.getLineOfOffset(p);
      // startPos is the offset of the first character of the line
      final int startPos = doc.getLineOffset(line);
      // firstNonWS is the offset of the next character after the last character of the line leading
      // whitespaces
      final int firstNonWS = findEndOfWhiteSpace(doc, startPos, p);
      if (firstNonWS < p) {
        // case line has characters others than spaces and tabs before the '>'
        // "remove" trailing whitespaces
        char c;
        while (true) {
          c = doc.getChar(--p);
          if (c != ' ' && c != '\t') { // $NON-NLS-2$
            ++p;
            break;
          }
        }
        if (c == '(') {
          // case in a Greater-than node, so do nothing
        }
        else {
          // all other cases, so remove previous whitespaces and prepend a space
          // modify the document command length and offset
          cmd.length += (cmd.offset - p);
          cmd.offset = p;
          // set the replacement document command text
          cmd.text = " >"; //$NON-NLS-1$
        }
      }
      else {
        // case '>' at the beginning of a line (after whitespaces), so do nothing
      }
    } catch (final BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns whether or not the text ends with one of the document end of line delimiters.
   * 
   * @param doc the document
   * @param txt the text
   * @return true if the text ends with one of the document end of line delimiters, false otherwise
   */
  boolean endsWithDelimiter(final IDocument doc, final String txt) {
    final String[] delimiters = doc.getLegalLineDelimiters();
    for (int i = 0; i < delimiters.length; i++) {
      if (txt.endsWith(delimiters[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the line number of the corresponding left brace.
   * 
   * @param doc - the document being parsed
   * @param startLine - the line to start searching back from (modified)
   * @param endPos - the end position to search back from (modified)
   * @param rightBracesCount - the number of braces to skip
   * @return the line number of the next matching brace after end
   * @throws BadLocationException if not in the right place
   */
  int findMatchingLeftBrace(final IDocument doc, final int startLine, final int endPos,
                            final int rightBracesCount) throws BadLocationException {
    int ln = startLine;
    int startPos = doc.getLineOffset(ln);
    int brackcount = getBracesCount(doc, startPos, endPos, false) - rightBracesCount;
    // Sums up the braces counts of each line (right braces count negative,
    // left positive) until we find a line the brings the count to zero
    while (brackcount < 0) {
      ln--;
      if (ln < 0) {
        return -1;
      }
      startPos = doc.getLineOffset(ln);
      final int ep = startPos + doc.getLineLength(ln) - 1;
      brackcount += getBracesCount(doc, startPos, ep, false);
    }
    return ln;
  }

  /**
   * Returns the brace value of a section of text. Right braces have a value of -1 and left braces have a
   * value of 1.
   * 
   * @param doc - the document being parsed
   * @param startPos - the start position for the search
   * @param endPos - the end position for the search
   * @param ignoreRightBraces - whether or not to ignore right braces in the count
   * @return the line number of the next matching brace after end
   * @throws BadLocationException if not in the right place
   */
  int getBracesCount(final IDocument doc, final int startPos, final int endPos,
                     final boolean ignoreRightBraces) throws BadLocationException {
    int p = startPos;
    int bracesCount = 0;
    boolean ignore = ignoreRightBraces;
    while (p < endPos) {
      final char c = doc.getChar(p);
      p++;
      switch (c) {
        case '/':
          if (p < endPos) {
            final char n = doc.getChar(p);
            if (n == '*') {
              // a comment starts, advance to the comment end
              p = getCommentEnd(doc, p + 1, endPos);
            }
            else if (n == '/') {
              // '//'-comment: nothing to do anymore on this line
              p = endPos;
            }
          }
          break;
        case '*':
          if (p < endPos) {
            final char n = doc.getChar(p);
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
          p = getStringEnd(doc, p, endPos, c);
          break;
        default:
      }
    }
    return bracesCount;
  }

  /**
   * Returns the given line indentation string (the first ' ' and '\t' of the line).
   * 
   * @param doc - the document being parsed
   * @param line - the line being searched
   * @return the line indentation string.
   * @throws BadLocationException if not in the right place
   */
  String getLineIndent(final IDocument doc, final int line) throws BadLocationException {
    if (line < 0) {
      return ""; //$NON-NLS-1$
    }
    final int start = doc.getLineOffset(line);
    final int end = start + doc.getLineLength(line) - 1;
    final int wsEnd = findEndOfWhiteSpace(doc, start, end);
    return doc.get(start, wsEnd - start);
  }

  /**
   * Returns the end position of a comment starting at a given position.
   * 
   * @param doc - the document being parsed
   * @param startPos - the start position for the search
   * @param endPos - the end position for the search
   * @return the end position of a comment starting at the start position
   * @throws BadLocationException if not in the right place
   */
  int getCommentEnd(final IDocument doc, final int startPos, final int endPos) throws BadLocationException {
    int p = startPos;
    while (p < endPos) {
      final char c = doc.getChar(p);
      p++;
      if (c == '*') {
        if (p < endPos && doc.getChar(p) == '/') {
          return p + 1;
        }
      }
    }
    return endPos;
  }

  /**
   * Returns the first position of a given non escaped character in a given document range.
   * 
   * @param doc - the document being parsed
   * @param startPos - the position to start searching from
   * @param endPos - the position to end searching to
   * @param ch - the character to try to find
   * @return the first location of the character searched
   * @throws BadLocationException if not in the right place
   */
  int getStringEnd(final IDocument doc, final int startPos, final int endPos, final char ch)
                                                                                            throws BadLocationException {
    int p = startPos;
    while (p < endPos) {
      final char c = doc.getChar(p);
      p++;
      if (c == '\\') {
        // ignore escaped characters
        p++;
      }
      else if (c == ch) {
        return p;
      }
    }
    return endPos;
  }
}
