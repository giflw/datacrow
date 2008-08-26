<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body/>
          <fo:region-after />
        </fo:simple-page-master>
      </fo:layout-master-set>


      <fo:page-sequence master-reference="A4">

        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="right" font-size="10pt">page <fo:page-number/></fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">

          <xsl:for-each select="data-crow-objects/*">        
          
            <fo:block keep-together="always">
          
              <fo:block font-size="11pt" font-weight="bold" space-before="10">
                <xsl:value-of select="title"/>
              </fo:block>

              <fo:block>
                <fo:external-graphic content-width="scale-to-fit" content-height="100%" scaling="uniform">
                  <xsl:attribute name="src">url('<xsl:value-of select="image" />')</xsl:attribute>
                </fo:external-graphic> 
              </fo:block>
              
            </fo:block>
          </xsl:for-each>
        </fo:flow>
        
      </fo:page-sequence>
      
  </fo:root>
</xsl:template>
</xsl:stylesheet>
