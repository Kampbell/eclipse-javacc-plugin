How to set up the admin & security requirements for the JavaCC Eclipse Plugin update site
-----------------------------------------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009


- User must be granted admin rights through the project admin site
  (Project Admin / Membership)
- User must generate on his workstation a pair of DSA or RSA keys 
 e.g.: through Eclipse / Window / Preferences / General / Network Connections
                / SSH2 / Key Management / Generate DSA Keys
  user must choose (and record) a pass phrase and save the keys on his workstation
  (e.g. save private key under xxx.txt and public key under xxx.txt.pub ; they will be in OpenSSH format)
- User must upload his public key to the SF site
 (through Account / Services / Edit SSH Keys for Shell/CVS and paste ;
  the update may take some delay)
- To use PSFTP or WinSCP the files must be in Putty keys format, so use puttygen to load the keys (from xxx.txt)
  and save the private key to xxx.ppk and the public key to xxx.ppk.pub



How to create the JavaCC Eclipse Plugin update site zip file
------------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th 2009 / Feb 17th 2010 / Feb 25th 2010 / Mar 31th 2010 /
 Jun 29th 2012, Sept 26th 2012, Dec 30th 2014


- Build JTB Project, check that "Release_Notes.txt" and "jtb_doc.html" are up to date

For the normal plug-in:

- Build project "JTB-svn_java.net"
  * see build.xml
  * update doc/jtb_doc.hmtl
  
- Build project "sf.eclipse.javacc":
  * copy "jtb-x.y.z.jar" under the jars folder
  * update JTB_JAR_NAME in "IConstants"
  * review "plugin.xml" (change / add JavaCC / JTB jar names in "build.properties" tab, update the plugin
    version in overview tab)
  * update "readme.txt"
  * update dependencies if dropping support of some Eclipse version
    (Note : take care in using the "Find unused dependencies) : the org.eclipse.debug.ui is needed for the
     import org.eclipse.debug.ui.StringVariableSelectionDialog in GlobalOptions.java, and seems to make the
     org.eclipse.core.resources unused, although it is used in NewGrammarWizardPage.java; also the
     org.eclipse.swt has been added although "unused" for the windows 32b or 64b fragments)
  * do not use the export wizard

- Update project "sf.eclipse.javacc.feature" through "feature.xml":
  * update the version
  * recompute the dependencies
  * use the export wizard to create the "sf.eclipse.javacc-x.y.zz-plugin.zip" file
    (under project "sf.eclipse.javacc-web") (change the version in the file name)

- Update project "sf.eclipse.javacc-web" through "site.xml":
  * copy "jtb_doc.html" from "<jtb project>/doc/jtb_doc.html"
  * move to folder "old_versions" previous zips under the root
  * move to folder "old_versions/plugins" "old_versions/features" previous jars from folders "plugins" and
    "features"
  * update "index.html"
  * delete "artifacts.jar" & "content.jar"
  * in "Site Map" tab add new version feature (under the site, keep it in the category), remove old version
    feature, and use "Build all" (the new plugin jar is created) (it seems it is no longer needed to check
     there are no old versions entries in "artifacts.jar" & "content.jar")
  * update "javacc-ver" property in "build_zip.xml"
  * create "sf.eclipse.javacc-z.y.xx-updatesite.zip" through "build_zip.xml" (Run As ... / Ant Build)
  * upload files to the SF site as below

For the Headless plug-in:

- Build project "sf.eclipse.javacc.headless":
  * check any changes in base packages of project "sf.eclipse.javacc" do not produce errors in headless package
    of project "sf.eclipse.javacc.headless"
  * update the version in "plugin.xml"
  * check dependencies (specially the version for the normal plugin)
  * do not use the export wizard

- Update project "sf.eclipse.javacc.headless.feature" through "feature.xml":
  * update the version
  * recompute the dependencies
  * use the export wizard to create the "sf.eclipse.javacc.headless-x.y.zz-plugin.zip" file
    (under project "sf.eclipse.javacc.headless-web") (change version)

- Update project "sf.eclipse.javacc.headless-web" through "site.xml":
  * move to folder "old_versions" previous jars from folders "plugins" and "features"
  * update "index.html"
  * delete "artifacts.jar" & "content.jar"
  * in "Site Map" tab add new version feature (under the site, keep it in the category), remove old version
    feature, and use "Build all" (the new plugin jar is created) (it seems it is no longer needed to check
     there are no old versions entries in "artifacts.jar" & "content.jar")
  * update "javacc-ver" property in "build_zip.xml"
  * create "sf.eclipse.javacc.headless-z.y.xx-updatesite.zip" through "build_zip.xml" (Run As ... / Ant Build)
  * upload files to the SF site as below

- Commit files for the 6 projects to CVS and tag as a version


How to update the JavaCC Eclipse Plugin SourceForge site
--------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009 / Feb 18th, 2010 / Oct 1st, 2012

- First update the "to be downloaded by users" zip file on the project file manager page through the web interface
  (https://sourceforge.net/project/admin/?group_id=56876) (Menu Files, delete old files, add new files,
   set default download for all OS for the plugin.zip file, add old files to eclipse-javacc sub folders)
- Second update the update site (http://eclipse-javacc.sourceforge.net/, to be used by the Eclipse Update Manager)
  through WinSCP or Putty PSFTP

+ WinSCP :
  * create a new site : host web.sourceforge.net port 22
  * link it to the DSA key file on the workstation
  * cd /home/pfs/project/e/ec/eclipse-javacc to go to the release directory, where to put the
     sf.eclipse.javacc-z.y.xx-updatesite.zip
  * cd /home/groups/e/ec/eclipse-javacc/htdocs to go to the project web directory, where to put the
     index.html and all files from sf.eclipse.javacc-web
  current problems :
  - open works with SFTP, not SCP (remote shell restricted to copy operations ?) ;
  - cannot overwrite not owned files (even if in right group) : seems to work now
  
  
+ Putty PSFTP :
  * configure putty with host web.sourceforge.net port 22 on Session,
     sf account,project (e.g. mmazas,eclipse-javacc) on Connection / Data
     add the private key file path on Connection / SSH / Auth, and save session
  * run psftp, open web.sourceforge.net, it takes sf account,project, give keyboard-interactive password
     (not passphrase)
    or run psftp <putty_session> (e.g. SourceForge), give pass phrase
  * sftp commands like ftp : ls, lcd, cd, put, !dir

+ Filezilla :
  * configure site with web.sourceforge.net port 22, account,project (e.g. mmazas,eclipse-javacc),
     ask for password (keyboard-interactive-password) (not passphrase), local and remote folders
  * open connection to the site

- Update the project information (Project Admin / Update MetaData) : version, full description

- Send an announcement (News / New Post)


How to update the Eclipse MarketPlace JavaCC Plug-in entry
----------------------------------------------------------

- Login into http://marketplace.eclipse.org/content/javacc-eclipse-plug
- Use the Edit menu to edit the entry (mainly version), and save
