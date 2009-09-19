<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">

  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 
  
    <!-- Variables used by desert layout -->
    <xsl:variable name="fontSize">9pt</xsl:variable>
    <xsl:variable name="labelColor">#FFFFCC</xsl:variable>
    <xsl:variable name="textColor">#FFFFEB</xsl:variable>
    <xsl:variable name="borderBottomColor">#FFEA96</xsl:variable>
    <xsl:variable name="borderBottomWidth">1</xsl:variable>
    <xsl:variable name="pictureSize">5cm</xsl:variable>
    <xsl:variable name="pictureSizeSmall">2cm</xsl:variable>
    <xsl:variable name="rowHeight">0.50cm</xsl:variable>
    <xsl:variable name="paddingTop">2</xsl:variable>
    <xsl:variable name="paddingLeft">2</xsl:variable>
  
    <!-- ..................................................... -->
    <!-- New variables for inim layout -->
    <!-- ..................................................... -->
    
    <!-- ............................... -->
    <!-- Labels for text subject to I18N -->
    <!-- ............................... -->
    
    <!-- Labels in the first non-picture colum -->
    <xsl:variable name="label-subtitle">
    	<xsl:text>Untertitel: </xsl:text>
    </xsl:variable>
    <xsl:variable name="label-audio-language">
    	<xsl:text>Audio: </xsl:text>
    </xsl:variable>

    <!-- Labels for the overall columns -->
    <xsl:variable name="label-image-column">
    	<xsl:text>Poster</xsl:text>
    </xsl:variable>
    <xsl:variable name="label-title-column">
    	<xsl:text>Title</xsl:text>
    </xsl:variable>
    <xsl:variable name="label-content-column">
    	<xsl:text>Content</xsl:text>
    </xsl:variable>
    <xsl:variable name="label-technical-column">
    	<xsl:text>Technical</xsl:text>
    </xsl:variable>

    <!-- Labels for page numbering -->
    <xsl:variable name="label-page-page">
	<xsl:text>Page </xsl:text>
    </xsl:variable>
    
    <xsl:variable name="label-page-of">
	<xsl:text> of </xsl:text>
    </xsl:variable>
    
    <!-- Etc. -->
    <xsl:variable name="label-spacer">
	<xsl:text>&#x00A0;</xsl:text>
    </xsl:variable>

    <!-- .......... -->
    <!-- Font sizes -->
    <!-- .......... -->

    <xsl:variable name="font-size-colum-title-title">10pt</xsl:variable>
    <xsl:variable name="font-size-colum-title-lang">6pt</xsl:variable>
    <xsl:variable name="font-size-colum-content">8pt</xsl:variable>
    <xsl:variable name="font-size-colum-technical">6pt</xsl:variable>

    <!-- ............. -->
    <!-- Control flow  -->
    <!-- ............. -->
    
    <!-- If the audio language is equal to this variable, and there is exactly
         one audio language, output of language is suppressed
    -->

    <xsl:variable name="i18n-audio-language-default">German</xsl:variable>
    
    <!-- DC generates locale dependent output in the "rating" field.
         "8 von 10" if you run the GUI in German, "8 of 10" if you run it
         in English. Other languages I dunno. Please adapt this String to
         the value DC inserts between the numbers in your locale
    -->
    <xsl:variable name="i18n-rating-x-of-y"> of </xsl:variable>

    <!-- ..................................................... -->


  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body/>
          <fo:region-after/>
        </fo:simple-page-master>
      </fo:layout-master-set>
           
      <fo:page-sequence master-reference="A4">

        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="right" font-size="{$fontSize}">
          	<xsl:copy-of select="$label-page-page"/>
          	<fo:page-number/>
          	<xsl:copy-of select="$label-page-of"/>
          	<fo:page-number-citation ref-id="EndOfDocument"/> 
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="{$fontSize}">

            <fo:table table-layout="fixed" width="100%" background-color="{$textColor}">

              <fo:table-column column-width="15%" />
              <fo:table-column column-width="31%" /> 
              <fo:table-column column-width="31%" />
              <fo:table-column column-width="23%" />

              <fo:table-header>
                <fo:table-row height="{$rowHeight}">

                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block>
                    	<xsl:copy-of select="$label-image-column"/>
                    </fo:block>
                  </fo:table-cell>

                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block>
                    	<xsl:copy-of select="$label-title-column"/>
                    </fo:block>
                  </fo:table-cell>
                        
		    <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block>
                    	<xsl:copy-of select="$label-content-column"/>
                    </fo:block>
		    </fo:table-cell>
      
                  <fo:table-cell background-color="{$labelColor}" border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    <fo:block>
                    	<xsl:copy-of select="$label-technical-column"/>
                    </fo:block>
                  </fo:table-cell>
                        
                </fo:table-row>
              </fo:table-header>

              <fo:table-body>
                <xsl:for-each select="data-crow-objects/movie"> 
                  <fo:table-row height="{$pictureSizeSmall}">
                  
		      <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
			<fo:block>
			  <xsl:if test="picture-front != ''">
			    <fo:external-graphic content-width="{$pictureSizeSmall}" content-height="{$pictureSizeSmall}" scaling="uniform">
			      <xsl:attribute name="src">url('<xsl:value-of select="picture-front" />')</xsl:attribute>
			    </fo:external-graphic> 
			  </xsl:if>
			  <xsl:if test="picture-front = ''">
			      	<xsl:copy-of select="$label-spacer"/>
			  </xsl:if>
			</fo:block>
		      </fo:table-cell>

                  
                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      <fo:block font-weight="bold" font-size="$font-size-colum-title-title">
                      	<xsl:value-of select="title-local"/>
                      </fo:block>
                      <fo:block font-size="4">
			<xsl:copy-of select="$label-spacer"/>
		       </fo:block>
		        <xsl:if test="
		        	(
		        		count(audio-languages/language/name) = 1
		        		and audio-languages/language/name/text() != $i18n-audio-language-default
		        	)
		        	or count(audio-languages/language/name) > 1
		        
		        ">
				<fo:block font-size="{$font-size-colum-title-lang}">
				<xsl:copy-of select="$label-audio-language"/>
			        <xsl:choose>
				        <xsl:when test="count(audio-languages/language/name) = 1">
							<xsl:if test="audio-languages/language/name/text() != 	$i18n-audio-language-default">
								<xsl:value-of select="audio-languages"/>
							</xsl:if>
					        </xsl:when>
					        <xsl:otherwise>
							<xsl:for-each select="audio-languages/language/name">
								<xsl:choose>
									<xsl:when test="position() = last()">
										<xsl:value-of select="."/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="."/>
										<xsl:text>, </xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:otherwise>
				</xsl:choose>
				</fo:block>
			</xsl:if>
			
		        <xsl:if test="count(subtitle-languages/language/name) != 0">
				<fo:block font-size="{$font-size-colum-title-lang}">		
					<xsl:copy-of select="$label-subtitle"/>
					<xsl:for-each select="subtitle-languages/language/name">
						<xsl:choose>
							<xsl:when test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="."/>
								<xsl:text>, </xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</fo:block>
		        </xsl:if>
                    </fo:table-cell>


                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                    
                    <fo:block font-size="{$font-size-colum-content}">
                      <fo:block>
				<xsl:choose>
					<xsl:when test="count(directors/director/name) = 1">
						<xsl:value-of select="directors/director/name"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- Only list first 2 directors to keep layout happy -->
						<xsl:for-each select="directors/director/name[position()&lt;3]">
							<xsl:if test="position() = 1">
								<xsl:value-of select="."/>	
							</xsl:if>
							<xsl:if test="position() = 2">
								<xsl:text> / </xsl:text>
								<xsl:value-of select="."/>	
							</xsl:if>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>

			</fo:block>
		      <fo:block>
		      	<xsl:choose>
		      		<xsl:when test="count(countries/country/name) = 1">
		      			<xsl:value-of select="countries/country/name"/>
		      		</xsl:when>
		      		<xsl:otherwise>
		      			<!-- Only list first 2 countries to keep layout happy -->
		      			<xsl:for-each select="countries/country/name[position()&lt;3]">
		      				<xsl:if test="position() = 1">
			      				<xsl:value-of select="."/>	
		      				</xsl:if>
		      				<xsl:if test="position() = 2">
		      					<xsl:text>/</xsl:text>
			      				<xsl:value-of select="."/>	
		      				</xsl:if>
		      			</xsl:for-each>
		      		</xsl:otherwise>
		      	</xsl:choose>
		      	<xsl:text>, </xsl:text>
			<xsl:value-of select="year"/>
		      	<xsl:text>, </xsl:text>
			<xsl:value-of select="playlength"/>
		      	<xsl:text> h</xsl:text>
		      </fo:block>
                      <fo:block>&#8222;<xsl:value-of select="substring-before(title, ' (')"/>&#8220;</fo:block>
		      <fo:block>
		      	<xsl:copy-of select="$label-spacer"/>
		      </fo:block>
                      <fo:block>
                      	<xsl:value-of select="genres"/>
                      </fo:block>
		    </fo:block>
                    </fo:table-cell>

                    <fo:table-cell border-bottom-style="solid" border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
                      
                      <fo:block font-size="{$font-size-colum-technical}"> 
			      <fo:block>
				<xsl:text>V: </xsl:text>
				<xsl:value-of select="video-codec"/>
				<xsl:text> [</xsl:text>
				<xsl:value-of select="video-bitrate"/>
				<xsl:text> kbps]</xsl:text>
			      </fo:block>
			      <fo:block>
				<xsl:text>A: </xsl:text>
				<xsl:value-of select="audio-codec"/>
				<xsl:text> [</xsl:text>
				<xsl:value-of select="audio-bitrate"/>
				<xsl:text> kbps]</xsl:text>
			      </fo:block>
			      <fo:block>
				<xsl:text>W/H: </xsl:text>
				<xsl:value-of select="width"/>
				<xsl:text> x </xsl:text>
				<xsl:value-of select="height"/>
			      </fo:block>
			      <fo:block>
				<xsl:text>HD: </xsl:text>
				<xsl:value-of select="container"/>
			      </fo:block>
			      <fo:block font-size="{$font-size-colum-technical}">
				<xsl:value-of select="webpage"/>
			      </fo:block>
			      <fo:block>
				<xsl:copy-of select="$label-spacer"/>
			      </fo:block>
			      <fo:block>
<!--				<xsl:text>IMDB: </xsl:text>
				<xsl:value-of select="rating"/> "
-->
                      		<xsl:call-template name="format-ratings">
                      			<xsl:with-param name="rating" select="number(substring(rating, 0, 2))"/>
				</xsl:call-template>
			      </fo:block>
			 </fo:block>
			</fo:table-cell>
                  </fo:table-row>
                </xsl:for-each>
              </fo:table-body>
            </fo:table>
          </fo:block>
	  <fo:block id="EndOfDocument"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <!-- ................................................................................... -->
  <!--                             Subroutines                                             -->
  <!-- ................................................................................... -->

  <xsl:template name="format-ratings">
  	<xsl:param name="rating"/>
  	<xsl:text>R: </xsl:text>
  	<xsl:value-of select="$rating"/>
  	<xsl:text>/10    </xsl:text>  	
  	<!--
  	<xsl:call-template name="rating-as-unicode-circled-numbers">
	  	<xsl:with-param name="rating" select="$rating"/>
  	</xsl:call-template>
  	-->
  	<xsl:call-template name="rating-as-unicode-stars">
	  	<xsl:with-param name="rating" select="$rating"/>
  	</xsl:call-template>
  </xsl:template>


  <xsl:template name="rating-as-unicode-circled-numbers">
  	<xsl:param name="rating"/>
  	<fo:inline font-family="ZapfDingbats">
		<xsl:choose>
			<xsl:when test="$rating = 0">
		<xsl:text>&#x2780;&#x2781;&#x2782;&#x2783;&#x2784;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>                      		
			<xsl:when test="$rating = 1">
		<xsl:text>&#x278a;&#x2781;&#x2782;&#x2783;&#x2784;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 2">
		<xsl:text>&#x278a;&#x278b;&#x2782;&#x2783;&#x2784;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 3">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x2783;&#x2784;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 4">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x2784;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 5">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x2785;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 6">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x278f;&#x2786;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 7">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x278f;&#x2790;&#x2787;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 8">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x278f;&#x2790;&#x2791;&#x2788;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 9">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x278f;&#x2790;&#x2791;&#x2792;&#x2789;</xsl:text>                     		
			</xsl:when>
			<xsl:when test="$rating = 10">
		<xsl:text>&#x278a;&#x278b;&#x278c;&#x278d;&#x278e;&#x278f;&#x2790;&#x2791;&#x2792;&#x2793;</xsl:text>                     		
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>  	
	</fo:inline>
  </xsl:template>

  <xsl:template name="rating-as-unicode-stars">
  	<xsl:param name="rating"/>
  	<fo:inline font-family="ZapfDingbats">
		<xsl:choose>
			<xsl:when test="$rating = 0">
		<xsl:text>&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>                      		
			<xsl:when test="$rating = 1">
		<xsl:text>&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 2">
		<xsl:text>&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 3">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 4">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 5">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 6">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 7">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 8">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 9">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2729;</xsl:text>
			</xsl:when>
			<xsl:when test="$rating = 10">
		<xsl:text>&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;&#x2605;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>  	
	</fo:inline>
  </xsl:template>
  
</xsl:stylesheet>
