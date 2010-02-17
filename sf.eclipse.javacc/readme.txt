Installation 

Use the update site
1) Menu Help / Software Updates / Find and Install... / Search for new features to install
2) Next > / New remote site...
 Name : SF Eclipse JavaCC 
 URL : http://eclipse-javacc.sourceforge.net/
3) Check "SF Eclipse JavaCC" and click Finish

Or use the zip which is the archive of the update site
1) Menu Help / Install new software... / Add... / Archive...
   and select the zip file

Features

1) Editor for .jj, .jjt and .jtb files.
2) Outline.
   Menu Window->ShowView->Outline
3) JavaCC Options setting for project.
   Right click on project->Properties->JavaCC options.
   There you can set the javacc.jar you wish to use.
   You can enable builder to compile .jj, .jjt, .jtb on file save.
4) Once the javacc.jar is defined, you can compile 
   with a right click on a .jj or .jjt file and choose 
   "Compile with JavaCC"
5) Console for JavaCC outputs.
   Menu Window->ShowView->Others...->JavaCC Console
   Provides more complete information on errors reported by JavaCC.
4) Generated files are identified with a small 'G' on top right.
   Right click on a generated file->Properties->Info, 
   uncheck Derived to remove this decorator.
   Open the file, edit, save to see removal of this decorator.
5) JJDoc compilation is available when a .jj or .jjt file is opened.
6) JTB compilation is available on a .jtb file
7) Help navigate into rules definitions
   click on a rule, and right click to "goto definition"
   use Workbench "Back" to go back.

History
17/02/10 - version 1.5.18
- Fixed /**/ syntax coloring issue (bug 2946447), missing french JTB preferences issue, JTB preferences not stored issue
- Fixed compile annotations and hyperlink issues in JTB Editor / Console
- Splitted templates for static = true and static = false (bug 2951454)
- Added spell checking (but not spell correction...)
- Updated with JTB 1.4.1 (bug 2945965) (Marc Mazas)

13/11/09 - version 1.5.17
- Fixed formatting / syntax coloring / outline / autoindent / call hierarchy / context menus / messages issues
- Updated with JTB 1.4.0 and new options (Marc Mazas)

26/09/09 - version 1.5.16
- Support Variables in Builder, and Options
- Click in call hierarchy view reopen Editor if it was closed
- Keep jtb jar in preferences

07/09/09 - version 1.5.15
- Bug 2848368 support Galileo 
- Bug 2848673 support Mac OS X Leopard (compiled with java 1.5 instead of 1.6)
- Change the zip to be a zipped update site.
- Added JavaCC 5.0 new options

23/05/09 - version 1.5.14
- RfE 2589910 added call hierarchy

25/04/09 - modifications on version 1.5.12 (Marc Mazas)
- Updated / cleaned list of JavaCC / JJTree options in JJCCOptions & JJTreeOptions
  according to JavaCC v4.2
- Fix in ParentMatcher to take in account '\"' characters in strings
- Added new preferences and a listener in JJPreferences, JJPreferencesPage, JJCodeScanner,
  messages.properties and messages_fr.properties
- Rewrote and enhanced formatting and indentation in JJFormat, JJTokenRule and JJAutoIndentStrategy
- Modified Option, OptionSet, JJAbstractTab and JTBOption to improve JTB preferences tab readability

22/06/08 - version 1.5.12
- Folding rfe 1786801 Collapse/Expand
- Hovering rfe 1888744 Quick Outline
- AutoCompletion rfe 1769382 Auto-Completion for both the defined tokens and nodes ignoring case
- bug 1986438 JavaCC options don't save state in the project directory
  now saved in \.settings\sf.eclipse.javacc.prefs
- bug 1986443  Project->Clean doesn't clean the generated files
  the generated files are now deleted, except if they are marked as not derived in their properties
- bug 1990463  jjtree option TRACK_TOKENS is not functional 
  keyword added
- bug 1891111  Alt + left arrow should jump back to correct position
- bug 1816600  Outline lists return type instead of method name
- bug 1889637  Incorrect pair matching with Strings
- correcting navigation which took the last opened grammar
- correcting javacode which allows node descriptor (#)
- correcting return type from rules which broke outline
- keep last good parsing for folding, hover and Completion proposals

08/07/07 - version 1.5.11
- bug 1682259 JJTREE_OUTPUT_DIRECTORY
- bug 1745835 NullPointerException thrown for files with no extension

08/07/07 - version 1.5.10
- bug 1682259 JJTREE_OUTPUT_DIRECTORY
- bug 1745835 NullPointerException thrown for files with no extension

10/06/2007 - version 1.5.9 
Bug correction
- bug 1726419 Unicode Problem uppercase were not accepted
- bug 1720584 Not enough install space: 2.8G required
- bug 1702922 JTBEditor popup menu
- bug 1702893 xxx.jj.jtb Compile with JTB in popup menu.
- bug 1702776 error with JTB generated code JTB generates < < javacc corrected to allow << 

18/03/07 - version 1.5.8
- correct JJDoc bug (was not called on selected editor) 
- correct Console report (inverted JJTree and JavaCC)
- correct JJTREE_OUTPUT_DIRECTORY highlight and option setting
- correct new file wizard to check if options are set before setting defaults
- replace javacc.jar with last CVS version 2007/03/18 
- add new JJDoc options in javacc.jar (modified sources in javacc.jar)

10/03/07 - version 1.5.7
- correct double suppress warning 
- preserve unicode escape in characters and strings when formatting
- correct JJTree grammar to handle >> in outline

26/11/06 - version 1.5.6
- restore compatibility with Eclipse 3.1
- handle case with no src/bin dir defined

05/11/06 - version 1.5.5
- add feature 1584400 Better recognition of error/warning messages in console 
  hyperlinks (Warning / Error on two lines where not parsed)
- correct bug 1585822 Plug-in not working with Eclipse 3.2.1
- correct bug 1588477 Option 'suppress warnings' works only for public classes
- added a (basic) Ctrl+space content assistant for options keywords
- suppress warnings in sf.eclipse.javacc.wizards @SuppressWarnings("restriction") Java1.5
- switch the source to Java 1.5

01/10/06 - version 1.5.4
- suppress warnings in generated files thanks to Peter Murray and Ben Wake
- added global preferences for colors

24/05/06 - version 1.5.3
- corrected plugin key conflict F3 is now defined only for JavaCCPlugin
- corrected bug in automatic format

27/03/06 - version 1.5.2
- removed "exclude from build" and adopted Eclipse way, ie Build Path.
- added a French translation in a plugin fragment / feature patch
- corrected bug in outline alphabetic sort
- corrected bug in JTB wizard when default or no package is selected

24/02/06 - version 1.5.1
- added property to exclude a jj file from build
- packaged plug-in as Eclipse feature with update site
- packaged plug-in as a Directory (no more as a single jar) to use self contained javacc.jar 
- added automatic format

13/11/05 - version 1.5.0
- java 1.4 .class files compatibility
- bug correction (double compilation for .jtb in some case)
- icons for jtb files
- context help F1

31/10/05 - version 1.3
- support JTB
- Ctrl+ clic to open definition
- corrected bug in outline now showing up the first time
- removed self-history (now use the Workbench history)

28/08/05 - version 1.2 
- support Eclipse 3.1
- support Java 1.5
- matching bracket highlight
- keyboard shortcuts, all actions are in plugin.xml
- comment lines Ctrl+/ 
- shortcut “F3” to open declaration and "F2" to go back
- launch JavaCC and JJTree from where are the .jj or .jjt file
- http://www.cecill.info/ instead of http://www.gnu.org/licenses/gpl.txt
- removed TabFolderLayout (was copied from
  org.eclipse.jdt.internal.ui.util.TabFolderLayout
  Odd to see that it was defined in JDT not SWT !)
- removed button on the toolbar (replaced with popup menu)
- added Class and Method icons in outline

26/10/04 - version 1.1 
- support Eclipse 3.0
- added syntax highlight for private productions <# token>
- added use of java instead of javaw on Window's systems to call javacc
- added marker hover 
- correct outline, was showing '.' when rule returns a class or Object[]

28/07/05 - version 1.2 
- hyperlinks in Console
- bug correction in Builder, Console

25/07/03 - 1.0 First release for Eclipse 2.1

Caveats

1) Warning: Token.java: File is obsolete. 
Mark Token.java as derived (a small G appears top right of the icon)
Project / Clean... will delete and regenerate all files
Of course if you trigger the compilation manually by a right click Compile with JavaCC
afterward the warning will reappear.

2) *.jj files are copied into bin directory
It is a feature of Eclipse which copies all files which are not *.java.
You have the choice to make Eclipse copy or not copy these files.
You can disable copy of .jj and .jjt files with :
Menu "Window" -> "Preferences"
Choose "Java" -> "Compiler" then "Build path" tab and add in
Filtered Resources : *.jj,*.jjt

3) Any other bug ?
Send a mail to remi.koutcherawy@wanadoo.fr
