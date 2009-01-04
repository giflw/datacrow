 <-----/@@\----->                              
<-< <  \\//  > >->                             
  <-<-\ __ /->->                               
  Data /  \ Crow                               
      ^    ^ 

Version 3.4.7
Created on xx/01/2009
Created by Robert Jan van der Waals

Contact me at info@datacrow.net
Find me at http://www.datacrow.net
           http://sourceforge.net/projects/datacrow


----------------------------------------------------------------------------------------
Table of Content

 1.0 ------- Introduction
 2.0 ------- Requirements
 3.0 ------- About the database (HSQL)
 4.0 ------- Upgrading
 5.0 ------- Building the project
 6.0 ------- Starting Data Crow 
 7.0 ------- Credits
 8.0 ------- Licenses and 3rd party software


----------------------------------------------------------------------------------------
1.0 Introduction

Data Crow is a program to register Software, Images, Music Files, Audio CDs, 
Books and Movies. The registration is automated as far as possible. To achieve 
this, an internet connection is needed as Data Crow uses web services 
(like amazon.com, imdb.com and freedb.org) to retrieve information about a piece 
of software, an audio CD or a movie. Further more, files are parsed to retrieve 
useful information. 

Features:

* Skinnable UI.
* Platform independent.
* Internal help system (F1).
* Nice looking and easy to use UI.
* Loan administration.
* Optional web module (multi user / remote interface).
* Create your own modules or modify existing modules.
* Advanced user configuration (access rights + field access).
* Rename your files based on the information of Data Crow.
* Highly customizable; add your own fields, alter existing modules or create a new
  one, rename any text within the application, set the fonts, ..
* Reporting (PDF / HTML / Text / XML).
* Registration of Software, Audio CDs and Music Files, Books, Movies and Images.
* Search for items using online services such as Amazon.com (http://www.amazon.com), 
  Imdb (http://www.imdb.com), Musicbrainz (http://musicbrainz.org) and many others.
* Extract information from files on your CD, DVD or Harddisk:
  image files (JPG, GIF, PNG), music files (technical info and tag content of 
  ASF, OGG, APE, FLAC and MP3 files) and movie file (DivX, Xvid, ASF, MKV, OGM, 
  RIFF, MOV, IFO, VOB and Mpeg video).
* Internal HSQL database + SQL query tool for expert users.


----------------------------------------------------------------------------------------
2.0 Requirements

Data Crow was tested on:
Windows XP, Windows 98 and Ubuntu 7.10.
Systems used: P4 2.4 Ghz, Core 2 Duo 2.2ghz

Minimum requirement: 
An 800 Mhz system is perfectly capable of running Data Crow.
Data Crow needs, when using larger databases, at least 256 MB.
(a database of 10,000 records, with pictures, is considered large).

Data Crow needs Java:
  * JRE 1.5 or higher from Sun (http://java.sun.com/)


----------------------------------------------------------------------------------------
3.0 About the database (HSQL)

Data Crow uses the HSQL database engine. It's powerful, fast and can run on
any system. Look at http://hsqldb.sourceforge.net/web/hsqlDocsFrame.html 
for more information

By default a database with the name "dc" is used. 
To use another database, add the database name as a parameter:
java -jar datacrow.jar -db:<database name> or
datacrow.exe -db:<database name>


----------------------------------------------------------------------------------------
4.0 Upgrading

Upgrading is as simple as installing the latest version on top of an older version.
Always make a backup of your data first by using the "Backup & Restore" functionality
situated in the tools menu.

Upgrade paths:
  
  Upgrade paths are necessary to remove deprecated coding in newer versions. 
  I will try to keep the upgrade paths as short as possible.

  [Version 2.8.4 and Older]
  From versions below the 2.8.6 to the latest: Install the 2.8.6 first. 
  Start Data Crow and start the conversion (answer "yes" on the upgrade question)
  After a successful upgrade install & run the 2.9.5 version. After this the latest
  version of Data Crow can be installed.
  
  [Versions between 2.8.6 and 2.9.4]  
  To upgrade 2.8.6 and later versions, but below the 2.9.5 version:
  first install and run the 2.9.5 version prior to upgrading to the latest version.
  
  [Versions between 2.9.5 and 3.0]  
  First install and run the 3.0 version prior to upgrading to the latest version. 

  [Version 3.0 and higher]  
  Install and run the latest version.


----------------------------------------------------------------------------------------
5.0 Building the project

In the main directory (\Project) a batch file can be found; build.bat. This
batch file calls Ant (see http://jakarta.apache.org/) and uses the build.xml
file. Packages which have been build can be found in the "lib" Directory.

Of course you can also just call ant within the main directory.


----------------------------------------------------------------------------------------
6.0 Starting Data Crow

Data Crow can be started by typing "java -Xmx256m -jar datacrow.jar". 
On Windows platforms the datacrow.exe file can be used to start.

Data Crow needs to be pointed to its installation directory. This can be done by setting
the DATACROW_HOME system environment variable. In case you do not know how to do this for 
your operating system or when you are using multiple installations of Data Crow you can
also choose to use the -dir: parameter (explained below).  

Additional parameters:
    
    -db:<database name> 
     Forces Data Crow to use another database.
    
    -dir:<installation directory> 
     Use this parameter when Data Crow starts incorrectly and complains 
     about missing directories (non Windows platform only). 
    
    -nocache
     Forces Data Crow to ignore the cached items and load everything fresh from the 
     database.
     
    -webserver
     Starts the web server without starting the actual Data Crow application.


----------------------------------------------------------------------------------------
7.0 Credits

This piece of software would not have succeeded (or even existed) with help
from other development teams and information providers:

* ImageJ: A very powerful image editor.
  http://rsb.info.nih.gov/ij/
* HSQL: This product includes Hypersonic SQL.
  Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
  I want to thank Thomas Mueller for providing this application with an easy
  to use, powerful but small and platform independent database.
  http://hsqldb.sourceforge.net/
* Med's Movie Manager allows parsing of various movie file formats
  http://sourceforge.net/projects/xmm
* http://www.freedb.org for allowing me to retrieve data from their web servers
* http://musicbrainz.org for allowing me to retrieve Music Albums
* Amazon.com for allowing me to retrieve data from their web servers
  http://www.amazon.com
* Imdb.com for allowing me to extract data from their database 
  http://www.imdb.com
* Entagged. I have been switching back and forth between several music tag 
  readers. Now there is Entagged and I like it so far..
  http://entagged.sourceforge.net/
* izPack
  for providing a smooth way to install Data Crow on multiple platforms
  http://www.izforge.com/izpack/


----------------------------------------------------------------------------------------
8.0 License

    Data Crow is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses.
    