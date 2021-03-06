If you want to hack the source get the source from CVS on sourceforge :
basic setting for Javacc-plugin : http://sourceforge.net/scm/?type=cvs&group_id=56876
complete tutorial : https://eclipse-tutorial.dev.java.net/eclipse-tutorial/part2.html 

You can get 5 projects (take the HEAD)
1/ sf.eclipse.javacc         to hack the plugin
2/ sf.eclipse.javacc.feature to make a release
3/ sf.eclipse.javacc-web     to generate an update site
4/ sf.eclipse.javacc.help    to correct the help (you'll are welcome) 
5/ sf.eclipse.javacc.help.feature to make a release of the help

Suppose you only get 1/ sf.eclipse.javacc
You can make modifications and test them eg. 
- open sf.eclipse.javacc.action.Format.java
- add 2 lines after  public void run(IAction action) {
    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    MessageDialog.openInformation(w.getShell()," My Hack "," Format action"); 
- open plugin.xml and on the "Overview" tab click "Launch an Eclipse application"
- now test on a .jj file Ctrl+shift+F you should see a Message window " My Hack "

Now that you have a plug-in to hack 
here is more insight on how the plug-in is designed...

This plug-in uses 14 extensions:

1) Nature
   point="org.eclipse.core.resources.natures"
   class="sf.eclipse.javacc.base.Nature"
   
   This class sets and removes JavaCC Nature to projects.
   The nature is used by the Workbench to identify the builder.
   Nature.configure() adds a new Command with Builder.
   
2) Builder
   point="org.eclipse.core.resources.builders"
   class="sf.eclipse.javacc.base.Builder"
   
   Builder extends IncrementalProjectBuilder
   and is called by the Workbench to compile javaCC files.
   
   Note that it is also used to compile files via static methods.
   (On Actions triggered from contextual menu on a Resource)
    
   Builder uses also in this package :
   -Dirlist to retrieve JavaCC generated files.
   -JarLauncher to launch JavaCC via Runtime.exec()
   
3) Console View for JavaCC output
   point="org.eclipse.ui.views"
   class="sf.eclipse.javacc.base.Console"
   
   Console extends ViewPart
   and is used to show JavaCC outputs.
   
   Note that to access Console, you should get an instance
   via JavaccPlugin.getConsole().
   
4) Properties for Grammar files or Project
   point="org.eclipse.ui.propertyPages"
   class="sf.eclipse.javacc.options.JJPropertyPage" for "*.jj" files
   class="sf.eclipse.javacc.options.JJPropertyPage" for "*.jjt" files
   class="sf.eclipse.javacc.options.JJPropertyPage" for "*.jtb" files
   class="sf.eclipse.javacc.options.JJPropertyPage" for Project Properties
   
   JJPropertyPage extends org.eclipse.ui.dialogs.PropertyPage
   and provides a way to set JavaCC command line arguments.
   -JavaCCOptions   for javacc arguments
   -JJTreeOptions for jjtree arguments
   -JJDocOptions  for jjdoc arguments
   -JTBOptions  for jjdoc arguments
   -JJRuntimeOptions for Eclipse use of JavaCC
    (Console, jarfile setting, Builder)
    
   JJPropertyPage sets up a TabFolder to show 4 tabs
   for these 5 classes.
   4 of them (JavaCCOptions, JJTreeOptions, JJDocOptions, JTBOptions)
   extends JJAbstractTab to share a lot of methods.
   JJRuntimeOptions extends directly Composite.
   
   These classes use OptionSet which manages a set of Options.
   OptionSet provide a way to generate a CommandLine, which
   gather all the options in a single PersistentProperty 
   which is set on the Resource.
   JJRuntimeOptions sets PersistentProperty on Project.
   (These PersistentProperty are retrieved by Builder from
    Resource or Project)
    
   BooleanFieldEditor and DirectoryFieldEditor
   are modified versions of :
     org.eclipse.jface.preference.BooleanFieldEditor
     org.eclipse.jface.preference.DirectoryFieldEditor
   to bypass oddities (not really bugs).
   
5) PopupMenu extension on Package Explorer
   point="org.eclipse.ui.popupMenus"
   class="sf.eclipse.javacc.JJCompile" for .jj, .jjt or jtb files

   CompileAction provides a direct way to compile .jj, .jjt or jtb files
   in the contextual PopupMenu associated with a File.
   CompileAction simply calls a static method of Builder.
   
6) Decorator to annotate generated files
   point="org.eclipse.ui.decorators">
   class="sf.eclipse.javacc.base.JJDecorator"

   JJDecorator provides a decoration for generated files.
   A text is added <file.jj> if the file is derived from file.jj.
   An icon 'G' is added on top right of the Icon.
   Note that isDerived() is tested before decoration.
   To remove decoration, if you choose to manually modify
   the generated file, uncheck the "Derived" property in Info.
   
7) Editor Extension
   point="org.eclipse.ui.editors"
   class="sf.eclipse.javacc.editors.JJEditor"
   
   JJEditor extends TextEditor
   
   The outline is made with JJTree and JavaCC.jjt provided with JavaCC.
   OutlinePage uses JJContentProvider as a tree provider.
   JJContentProvider implements IContentProvider, ITreeContentProvider and
   uses SimpleNode (generated by JavaCC and modified to fit ITreeContentProvider)
   which is used as a leaf for the tree.
   
8) Popup menu Extension 
   point="org.eclipse.ui.popupMenus"
   class="sf.eclipse.javacc.actions.xxx"
   
   As you might guess theses classes are for the popup menu of the editor.
   
9) Editor Actions
   point="org.eclipse.ui.editorActions"
   class="sf.eclipse.javacc.actions.xxx"
   
   Theses classes are for the actions of the editor.

10)Editors Commands
   point="org.eclipse.ui.commands"
    
   This extension is necessary for key bindings.
    
11) Editor key bindings
   point="org.eclipse.ui.bindings"

   This extension bind the actions of the editor to shortcut keys.
   There is no class in this extension, it's just a binding of keys
   to actions defined in 9) which are the sames are actions defined in 8).

12) New Wizard
   point="org.eclipse.ui.newWizards"
   class=""sf.eclipse.javacc.wizards.WizPage"
   
   Provides a basic .jj or jjt file to help begin a new JavaCC project.
   The file "new_file.jj" is in the templates directory of the plug-in.
   
   This extension highly relies on "internal" of JDT.
   This generate number of Warnings :
   Warning Discouraged access: The type xxx is not accessible due
    to restriction on required library ... org.eclipse.jdt.ui.jar

13) Help 
   point="org.eclipse.help.toc"
   file="JJToc.xml" and JJPlgToc.xml"
   
   Well, the help is in need of a serious update...
  
14) Preferences Extension (Global)
   point="org.eclipse.ui.preferencePages">
   class="sf.eclipse.javacc.options.JJPreferencesPage"
   class="sf.eclipse.javacc.options.JJPreferences"

   Provides a basic customisation of colors and indentation.
   They are visible on the next action (inserting, reformatting).
   
