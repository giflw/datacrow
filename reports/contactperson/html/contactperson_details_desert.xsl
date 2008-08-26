<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>

  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Contact Persons</h1>
    
    <xsl:for-each select="data-crow-objects/*">
    
        <h1><xsl:value-of select="name"/></h1>

        <table width="600" style="border:0;" cellspacing="0" cellpadding="10">

            <colgroup valign="top" align="left">
                <col width="100" />
                <col width="500" />
            </colgroup>

            <tr><th>Description</th>
                <td><xsl:value-of select="description"/></td></tr>
            <tr><th>Category</th>
                <td><xsl:value-of select="category"/></td></tr>
            <tr><th>Email</th>
                <td><xsl:value-of select="email"/></td></tr>
            <tr><th>Address</th>
                <td><xsl:value-of select="address"/></td></tr>
            <tr><th>Phone (home)</th>
                <td><xsl:value-of select="phone-home"/></td></tr>
            <tr><th>Phone (work)</th>
                <td><xsl:value-of select="phone-work"/></td></tr>
        </table>
        
        <br />
        
        <table style="border:0;">
          <tr>
            <td>
              <xsl:if test="photo != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="photo" /></xsl:attribute></img>
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
