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
(for project admins) - Marc Mazas - Nov 22th, 2009 / Feb 17th, 2010 / Feb 25th, 2010


- Build JTB Project, copy jtb-x.y.z.jar and remove old one, copy jtb_doc.html
- Build project "sf.eclipse.javacc" and review "plugin.xml"
  * do not use the export wizard
- Update project "sf.eclipse.javacc-feature" through "feature.xml" (mainly version)
  * use the export wizard to create the "sf.eclipse.javacc-z.y.zz-plugin.zip" file (under project "sf.eclipse.javacc-web")
- Update project "sf.eclipse.javacc-web" through "site.xml" (mainly feature, keep it under the category)
  * move to folder "old_versions" previous jars from folders "plugins" and "features"
  * update "index.html", "jtb_doc.html", and add last version for jtb jar
  * delete "artifacts.xml" & "content.xml"
  * use "Build all" in "Site Map"
  * check there are no old versions entries in "artifacts.xml" & "content.xml"
  * update "javacc-ver" property in "build_zip.xml"
  * create "sf.eclipse.javacc-z.y.xx-updatesite.zip" through "build_zip.xml"
  * upload files to the SF site as below



How to update the JavaCC Eclipse Plugin SourceForge site
--------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009 / Feb 18th, 2010

- First update the "to be downloaded by users" zip file on the project file manager page through the web interface
  (https://sourceforge.net/project/admin/explorer.php?group_id=56876) (Develop -> Project Admin / File Manager)

- Second update the update site (http://eclipse-javacc.sourceforge.net/, to be used by the Eclipse Update Manager)
  through WinSCP or Putty PSFTP

+ WinSCP :
  * create a new site : host web.sourceforge.net port 22
  * link it to the DSA key file on the workstation
  * cd /home/pfs/project/e/ec/eclipse-javacc to go to the release directory, where to put the sf.eclipse.javacc-z.y.xx-updatesite.zip
  * cd /home/groups/e/ec/eclipse-javacc/htdocs to go to the project web directory, where to put the index.html and all files
  current problem : open works with SFTP, not SCP (remote shell restricted to copy operations ?) ;
  
+ Putty PSFTP :
  * configure putty with host web.sourceforge.net port 22, sf account,project (e.g. mmazas,eclipse-javacc) on Connection / Data
  * run psftp, open web.sourceforge.net, it takes sf account,project, give pass phrase
  * sftp commands like ftp : ls, lcd, cd, put


