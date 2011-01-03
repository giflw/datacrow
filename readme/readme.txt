 <-----/@@\----->                              
<-< <  \\//  > >->                             
  <-<-\ __ /->->                               
  Data /  \ Crow                               
      ^    ^ 

Version 3.9.1
Created on January 3, 2011
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

Data Crow is an application to register Software, Images, Music Files, Audio CDs, 
Books and Movies. The registration is automated as far as possible. To achieve 
this, an internet connection is needed as Data Crow uses web services 
(like amazon.com, imdb.com and freedb.org) to retrieve information about a piece 
of software, an audio CD or a movie. Further more, files are parsed to retrieve 
useful information. 

Features:

* Skinnable UI.
* Internal help system (F1).
* Nice looking and easy to use UI.
* You can customize almost anything; design your own item form, quick view, hide fields
  and module which you are not using and create your own reports (XSLT scripts).  
* Platform independent and completely portable (run from an USB stick).
* Loan administration. Keep track of your loans.
* Optional internal web server and web GUI.
* Create your own collection modules and/or modify existing collection modules.
* Advanced user configuration (access rights + module and field access).
* Rename your files based on the information of Data Crow.
* Highly customizable; add your own fields, alter existing modules or create a new
  one, rename any text within the application, set the fonts, ..
* Reporting (PDF, HTML, Text and XML).
* Registration of Software, Audio CDs and Music Files, Books, Movies and Images.
* Search for items using online services such as Amazon.com (http://www.amazon.com), 
  Imdb (http://www.imdb.com), Musicbrainz (http://musicbrainz.org) and many others.
* Extract information from files on your CD, DVD or Harddisk:
  image files (JPG, GIF, PNG, SVG), music files (technical info and tag content of 
  ASF, OGG, APE, FLAC and MP3 files) and movie file (DivX, Xvid, ASF, MKV, OGM, 
  RIFF, MOV, IFO, VOB and Mpeg video).
* Internal HSQL database + SQL query tool for expert users.

Data Crow dows not write information to the registry or any other platform specific 
folder or structure. All the information is kept withing the Data Crow installation 
folder. It does not obey platform specific rules and can run on any platform 
(Windows, Linux and others) having Java 1.6 (from Sun) or higher installed. 


----------------------------------------------------------------------------------------
2.0 Requirements

Data Crow was tested on:
Windows XP, Windows 98 and Ubuntu (latest version at the moment of testing).
Systems used: P4 2.4 Ghz, Core 2 Duo 2.2ghz

Minimum requirement: 
An 1000 Mhz system is perfectly capable of running Data Crow.
Data Crow needs, for large collections, at least 256 MB of free memory.
(a collection of 10,000 items or more is considered large).

Data Crow needs Java:
  * JRE 1.6 (or higher) from Sun (http://java.sun.com/)


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

1) Create a backup ("Backup & Restore" functionality situated in the "tools" menu) from 
   within the old version. 
2) Uninstall the old version (or simply delete the Data Crow installation folder).
3) Install the new version.
4) Restore the backup created in step 1 on top of this latest version.

Note that you first have to upgrade to 3.8.16 version before you can upgrade to version
3.9.0 of Data Crow.


----------------------------------------------------------------------------------------
5.0 Building the project

Use Apache Ant (see http://jakarta.apache.org/) to build this project. Ant will you use
the build.xml file to compile the full Data Crow project.


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
    
    -webserver
     Starts the web server without starting the actual Data Crow application.
     Can be used in combination with the credentials parameter to bypass all the GUI
     stuff (such as the splash screen and the login dialog)
     
    -credentials:username/password
     Specify the login credentials to start Data Crow without displaying the login dialog.
     
    -nosplash
     Hides the splashscreen on startup.
     
    -debug
     For additional logging information.     
     
    -clearsettings
     Loads the default Data Crow settings. Disgards all user settings.
     
    -datadir:<path>
     Specifies an alternative location for the data folder. Spaces need to be substituted 
     by %20.
     

----------------------------------------------------------------------------------------
7.0 Credits

This application would not have succeeded (or even existed) without the help of:

* Bas Uildriks who designed and created the web site (http://www.datacrow.net).
* HSQL: This product includes Hypersonic SQL.
  Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
  I want to thank Thomas Mueller for providing this application with an easy
  to use, powerful but small and platform independent database.
  http://hsqldb.sourceforge.net/
* http://www.freedb.org for allowing me to retrieve data from their web servers.
* http://musicbrainz.org for allowing me to retrieve Music Albums.
* Amazon.com for allowing me to retrieve data from their web servers.
  http://www.amazon.com
* Imdb.com for allowing me to extract data from their database.
  http://www.imdb.com
* JAudioTagger which I use to parse audio file information.
  http://www.jthink.net/jaudiotagger
* izPack for providing a smooth way to install Data Crow on multiple platforms.
  http://www.izforge.com/izpack/
* ISBNExtractor. Extracts ISBNs from PDF documents.
  http://isbnextractor.sourceforge.net.   


----------------------------------------------------------------------------------------
8.0 License

This program is free software: you can redistribute it and/or modify it under the terms 
of the GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details. 

You should have received a copy of the GNU General Public License along with this program. 
If not, see http://www.gnu.org/licenses.
