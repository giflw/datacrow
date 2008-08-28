<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>

  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Software Items</h1>
    
    <xsl:for-each select="data-crow-objects/software-item">
    
        <h1><xsl:value-of select="title"/></h1>

        <table width="600" style="border:0;" cellspacing="0" cellpadding="10">

            <colgroup valign="top" align="left">
                <col width="100" />
                <col width="500" />
            </colgroup>

            <tr><th>Description</th>
                <td><xsl:value-of select="description"/></td></tr>
            <tr><th>Category</th>
                <td><xsl:value-of select="category"/></td></tr>
            <tr><th>Year</th>
                <td><xsl:value-of select="year"/></td></tr>
            <tr><th>Developed by</th>
                <td><xsl:for-each select="developers/developer"><xsl:value-of select="name"/>&#160;</xsl:for-each></td></tr>
            <tr><th>Published by</th>
                <td><xsl:for-each select="publishers/publisher"><xsl:value-of select="name"/>&#160;</xsl:for-each></td></tr>
            <tr><th>Platform</th>
                <td><xsl:value-of select="platform"/></td></tr>
            <tr><th>Container</th>
                <td><xsl:value-of select="container"/></td></tr>
            <tr><th>State</th>
                <td><xsl:value-of select="state"/></td></tr>
            <tr><th>Rating</th>
                <td><xsl:value-of select="rating"/></td></tr>
        </table>
        
        <br />
        
        <table style="border:0;">
          <tr>
            <td>
              <xsl:if test="picture-front != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="picture-front" /></xsl:attribute></img>
              </xsl:if>
            </td>

            <td>
              <xsl:if test="screenshot-one != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="screenshot-one" /></xsl:attribute></img>
              </xsl:if>
            </td>

            <td>
              <xsl:if test="screenshot-two != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="screenshot-two" /></xsl:attribute></img>
              </xsl:if>
            </td>
          </tr>
        </table>
      <br /><br />
    </xsl:for-each>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
