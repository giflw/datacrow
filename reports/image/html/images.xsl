<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>

  <head>
    <link type="text/css" rel="stylesheet" href="desert.css" />
  </head>

  <body>

    <xsl:for-each select="data-crow-objects/*">

      <h1><xsl:value-of select="title" /></h1>
    
      <xsl:if test="image != ''">
        <img alt=""><xsl:attribute name="src"><xsl:value-of select="image" /></xsl:attribute></img>
      </xsl:if>

      <br /><br />
      
    </xsl:for-each>
    
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
