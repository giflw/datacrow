<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
<xsl:import href="../../_stylesheets/pdf_desert.xsl" />

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
          <fo:block text-align="right" font-size="{$fontSize}">page <fo:page-number/></fo:block>
        </fo:static-content>
        
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="{$fontSize}">

            <fo:table table-layout="fixed" width="100%" background-color="{$textColor}">

              <fo:table-column column-width="35%" /> 
              <fo:table-column column-width="30%" />
              <fo:table-column column-width="20%" />
              <fo:table-column column-width="15%" />

              <fo:table-header>
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block font-weight="bold">Name</fo:block>
                  </fo:table-cell>
                        
                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block font-weight="bold">Email</fo:block>
                  </fo:table-cell>
                        
                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block font-weight="bold">Phone (home)</fo:block>
                  </fo:table-cell>
                        
                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block font-weight="bold">Photo</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>
                <xsl:for-each select="data-crow-objects/*"> 
                  <fo:table-row height="{$pictureSizeSmall}">
                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      <fo:block><xsl:value-of select="name"/></fo:block>
                    </fo:table-cell>

                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      <fo:block><xsl:value-of select="email"/></fo:block>
                    </fo:table-cell>

                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      <fo:block><xsl:value-of select="phone-home"/></fo:block>
                    </fo:table-cell>

                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      <fo:block>
                        <xsl:if test="photo != ''">
                          <fo:external-graphic content-width="{$pictureSizeSmall}" content-height="{$pictureSizeSmall}" scaling="uniform">
                            <xsl:attribute name="src">url('<xsl:value-of select="photo" />')</xsl:attribute>
                          </fo:external-graphic> 
                        </xsl:if>
                        <xsl:if test="photo = ''">&#x00A0;</xsl:if>
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>
            </fo:table>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
