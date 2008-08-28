<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  
  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>
  
  <body>
    <h1>Movies</h1>
    
    <table cellspacing="0" cellpadding="10">
        
        <colgroup>
            <col width="500" />
            <col width="100" />
        </colgroup>
    
        <tr>
            <th>Title</th>
            <th>Container</th>
        </tr>

        <xsl:for-each select="data-crow-objects/movie">
            <tr>
              <td class="listing"><xsl:value-of select="title"/>&#x00A0;</td>
              <td class="listing"><xsl:value-of select="container"/>&#x00A0;</td>
            </tr>
        </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>