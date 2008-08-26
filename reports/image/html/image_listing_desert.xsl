<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  
  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Images</h1>
    
    <table cellspacing="0" cellpadding="10">
        
      <colgroup>
        <col width="200" />
          <col width="50" />
          <col width="100" />
          <col width="100" />
          <col width="75" />
          <col width="75" />
      </colgroup>
    
      <tr>
        <th>Title</th>
        <th>Date</th>
        <th>State</th>
        <th>Dimension</th>
        <th>Rating</th>
        <th>Image</th>
      </tr>

      <xsl:for-each select="data-crow-objects/image">
        <tr>
          <td class="listing"><xsl:value-of select="title"/>&#x00A0;</td>
          <td class="listing"><xsl:value-of select="date"/>&#x00A0;</td>
          <td class="listing"><xsl:value-of select="state"/>&#x00A0;</td>
          <td class="listing"><xsl:value-of select="width"/>*<xsl:value-of select="height"/></td>
          <td class="listing"><xsl:value-of select="rating"/>&#x00A0;</td>
          <td class="listing">
            <xsl:choose>
              <xsl:when test="image != ''">
                <img alt=""><xsl:attribute name="src"><xsl:value-of select="image" /></xsl:attribute></img>
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
