﻿<?xml version="1.0" encoding="UTF-8"?>
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

      <xsl:for-each select="data-crow-objects/*">      
      <fo:page-sequence master-reference="A4">
      
        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="right" font-size="{$fontSize}">page <fo:page-number/></fo:block>
        </fo:static-content>
        
        <fo:flow flow-name="xsl-region-body">
        
          <fo:block font-size="{$fontSize}" font-weight="bold"><xsl:value-of select="title"/></fo:block>
        
          <fo:block font-size="{$fontSize}">
            <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-before="10">

              <fo:table-column column-width="3cm"/>
              <fo:table-column column-width="15cm" />

              <fo:table-body>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell padding-bottom="10"><fo:block font-weight="bold">Description</fo:block></fo:table-cell>
                  <fo:table-cell padding-bottom="10"><fo:block><xsl:value-of select="description"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Category</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="category"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Year</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="year"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Developed by</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="developer"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Published by</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="publisher"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Platform</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="platform"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Location</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="location"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">State</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="state"/></fo:block></fo:table-cell>
                </fo:table-row>
                
                <fo:table-row height="{$rowHeight}">
                  <fo:table-cell><fo:block font-weight="bold">Rating</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block><xsl:value-of select="rating"/></fo:block></fo:table-cell>
               </fo:table-row>
               
              </fo:table-body>
            </fo:table>
          </fo:block>

          <xsl:if test="photo != ''">
            <fo:table table-layout="fixed" width="100%" border-collapse="separate">

              <fo:table-body>
              <fo:table-row>
                <fo:table-cell>
                  <fo:block>
                    <fo:external-graphic content-width="{$pictureSize}" content-height="{$pictureSize}" scaling="uniform">
                      <xsl:attribute name="src">url('<xsl:value-of select="photo" />')</xsl:attribute>
                    </fo:external-graphic> 
                  </fo:block>
                </fo:table-cell>

              </fo:table-row>
              </fo:table-body>
            </fo:table>
             
            &#x20;&#x200b;
             
          </xsl:if>

      </fo:flow>
    </fo:page-sequence>
    </xsl:for-each>
  </fo:root>
  
</xsl:template>
</xsl:stylesheet>
