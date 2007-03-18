Installation 

1) Unzip JavaCC_Feature_1.5.7.zip into Eclipse directory. 
2) Launch Eclipse.

or

1) Menu Help / Software Updates / Find and Install... / Search for new features to install
2) Next > / New remote site...
 Name : SF Eclipse JavaCC 
 URL : http://eclipse-javacc.sourceforge.net/
3) Check "SF Eclipse JavaCC" and click Finish

Features

1) Editor for .jj, .jjt and .jtb files.
2) Outline.
   Menu Window->ShowView->Outline
3) JavaCC Options setting for project.
   Right click on project->Properties->JavaCC options.
   There you can set the javacc.jar you whish to use.
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
   Open the file, edit, save to see removal of this Decorator.
5) JJDoc compilation is available when a .jj or .jjt file is opened.
6) JTB compilation is available on a .jtb file
7) Help navigate into rules definitions
   click on a rule, and right click to "goto definition"
   use Workbench "Back" to go back.

History
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

Next
- group options (no more file and project options) under Preferences...
- correct the coloring : < LT : "<" > => '>' is in wrong color
  if ( i < 10 && i > 3 ) or < #BRACKET: [">","<"] > ill colored
- traduction en français
- refactoring (pas grand chose, juste renommer une méthode)

Bugs and caveats.

1) The option OUTPUT_DIR doesn't work for JJDoc.
This is a bug in JJDOC.
Try directly from the console :
>jjdoc -OUTPUT_DIRECTORY:doc new_file.jj
Java Compiler Compiler Version 3.2 (Documentation Generator Version 0.1.4)
(type "jjdoc" with no arguments for help)
Reading from file new_file.jj . . .
Grammar documentation generated successfully in new_file.html

The "new_file.html" has not been generated in "doc" directory !
We should have read : "generated successfully in doc/new_file.html"

Workaround : you can use the -OUTPUT_FILE option :
>jjdoc -OUTPUT_FILE:doc/doc.html new_file.jj
Java Compiler Compiler Version 3.2 (Documentation Generator Version 0.1.4)
(type "jjdoc" with no arguments for help)
Reading from file new_file.jj . . .
Grammar documentation generated successfully in doc/doc.html

Unfortunately then you have to specify this option for each file
you compile with JJDoc.

The plugin is ready for the correction; it passes both options
when invoking JJDoc.

2) *.jj files are copied in bin directory
It is a feature of Eclipse which copies all files which are not *.java.
You have the choice to make Eclipse copy or not copy these files.
You can disable copy of .jj and .jjt files with :
Menu "Window" -> "Preferences"
Choose "Java" -> "Compiler" then "Build path" tab and add in
Filtered Resources : *.jj,*.jjt

3) Any other bug ?
Send a mail to remi.koutcherawy@wanadoo.fr
