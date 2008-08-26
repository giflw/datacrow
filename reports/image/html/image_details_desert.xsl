<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>

  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Image</h1>
    
    <xsl:for-each select="data-crow-objects/image">
    
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
            <tr><th>Dimension</th>
                <td><xsl:value-of select="width"/>*<xsl:value-of select="height"/></td></tr>
            <tr><th>Date</th>
                <td><xsl:value-of select="date"/></td></tr>
            <tr><th>State</th>
                <td><xsl:value-of select="state"/></td></tr>
        </table>
        
        <br />
        
        <table style="border:0;">
          <tr>
            <td>
              <xsl:if test="image != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="image" /></xsl:attribute></img>
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
