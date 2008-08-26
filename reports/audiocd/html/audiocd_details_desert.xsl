<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">

<html>
    <head>
        <link type="text/css" rel="stylesheet" href="desert.css" />
    </head>
    
    <body>
    
        <h1>Audio CDs</h1>
        
        <xsl:for-each select="data-crow-objects/audio-cd">
        
            <h1><xsl:value-of select="title"/></h1>
        
            <table class="layout" cellspacing="0" cellpadding="0">
                <colgroup>
                    <col width="300" />
                    <col width="300" />
                </colgroup>
            
                <tr class="layout">
                    <td class="layout">
                        <table width="300" cellspacing="0" cellpadding="0">
    
                            <colgroup align="left" valign="top">
                                <col width="100" />
                                <col width="200" />
                            </colgroup>
                            
                            <tr><th>Artists</th>
                            <td><xsl:for-each select="artists/artist">
                                    <xsl:value-of select="name"/>
                                </xsl:for-each></td></tr>
        
                            <tr><th>Genre</th>
                                <td><xsl:for-each select="genres/genre">
                                        <xsl:value-of select="name"/>
                                    </xsl:for-each></td></tr>
        
                            <tr><th>Year</th>
                                <td><xsl:value-of select="year"/></td></tr>
        
                            <tr><th>State</th>
                                <td><xsl:value-of select="state"/></td></tr>
        
                            <tr><th>Location</th>
                                <td><xsl:value-of select="location"/></td></tr>
        
                            <tr><th>Rating</th>
                                <td><xsl:value-of select="rating"/></td></tr>
                        </table>
                    </td>
                    <td class="layout">             
                        <xsl:if test="picture-front != ''">
                        <center>
                            <img alt=""><xsl:attribute name="src"><xsl:value-of select="picture-front" /></xsl:attribute></img>
                        </center>
                        </xsl:if>
                    </td>
                </tr>
            </table>
            
            <xsl:if test="audio-tracks/*">
                
                <br />
            
                <table cellspacing="0" cellpadding="0">
                    <colgroup align="left" valign="top">
                        <col width="20" />
                        <col width="540" />
                        <col width="40" />
                    </colgroup>
                    
                    <tr><th>Nr</th>
                        <th>Title</th>
                        <th>Playlength</th></tr>
                    
                    <xsl:for-each select="audio-tracks/audio-track">
                        <tr><td><xsl:value-of select="track"/></td>
                            <td><xsl:value-of select="title"/></td>
                            <td><xsl:value-of select="playlength"/></td></tr>
                    </xsl:for-each>
                </table>
            </xsl:if>  
            
            <br />          
        </xsl:for-each>
    </body>
</html>
</xsl:template>
</xsl:stylesheet>    