How to set up the admin & security requirements for the JavaCC Eclipse Plugin update site
-----------------------------------------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009


- User must be granted admin rights through the project admin site
  (Project Admin / Membership)
- User must generate on his workstation a pair of DSA or RSA keys 
 e.g.: through Eclipse / Window / Preferences / General / Network Connections
                / SSH2 / Key Management / Generate DSA Keys
  user must save the keys on his workstation and choose and record a pass phrase
- User must upload his public key to the SF site
 (through Account / Services / Edit SSH Keys for Shell/CVS and paste ;
  the update takes some delay)




How to create the JavaCC Eclipse Plugin update site zip file
------------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009 / Feb 17th, 2010


- Build project sf.eclipse.javacc and review plugin.xml
  * do not use the export wizard
- Update project sf.eclipse.javacc-feature through feature.xml (version)
  * use the export wizard to create the sf.eclipse.javacc-z.y.zz-plugin.zip
- Update project sf.eclipse.javacc-web through site.xml (feature)
  * update index.html, jtb_doc.html, last version for jtb jar
  * delete artifacts.xml & content.xml
  * use "Build all" in "Site Map"
  * update version in build_zip.xml
  * create manually sf.eclipse.javacc-z.y.xx-updatesite.zip through build_zip.xml
  * upload files to the SF site as below




How to update the JavaCC Eclipse Plugin SourceForge site
--------------------------------------------------------
(for project admins) - Marc Mazas - Nov 22th, 2009

- Update the update site zip file either through the web interface
 (Develop -> Project Admin / File Manager) or through WinSCP or Putty PSFTP

- WinSCP :
  * create a new site : host web.sourceforge.net port 22
  * link it to the DSA key file on the workstation
  * cd /home/pfs/project/e/ec/eclipse-javacc to go to the release directory, where to put the sf.eclipse.javacc-z.y.xx-updatesite.zip
  * cd /home/groups/e/ec/eclipse-javacc/htdocs to go to the project web directory, where to put the index.html and al files
  current problem : open works with SFTP, not SCP (remote shell restricted to copy operations ?) ;
  
- Putty PSFTP :
  * configure Putty with host web.sourceforge.net port 22, sf account,project (e.g. mmazas,eclipse-javacc)
  * run psftp, open web.sourceforge.net, it takes sf account,project, give pass phrase
  * sftp commands like ftp : ls, lcd, cd, put


