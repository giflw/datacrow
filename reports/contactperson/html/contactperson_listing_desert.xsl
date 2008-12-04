<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  
  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Contact Persons</h1>
    
    <table cellspacing="0" cellpadding="10">
        
      <colgroup>
        <col width="225" />
        <col width="200" />
        <col width="100" />
        <col width="75" />
      </colgroup>
    
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Pnone (home)</th>
        <th>Photo</th>
      </tr>

      <xsl:for-each select="data-crow-objects/*">
        <tr>
          <td class="listing"><xsl:value-of select="name"/>&#x00A0;</td>
          <td class="listing"><xsl:value-of select="email"/>&#x00A0;</td>
          <td class="listing"><xsl:value-of select="phone-home"/>&#x00A0;</td>
          <td class="listing">
            <xsl:choose>
              <xsl:when test="photo != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="photo" /></xsl:attribute></img>&#160;
              </xsl:when>
              <xsl:otherwise>&#160;</xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
