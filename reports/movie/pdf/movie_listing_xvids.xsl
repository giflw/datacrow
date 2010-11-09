<?xml version="1.0" encoding="UTF-8"?>

	<!--
		...................................................................
		Compact yet detailed PDF list layout for movie file collections
		Designed and coded by Ingo Macherius <inim (at) sourceforge (dot) net>
		Version 1.0.0 / 20090918
		...................................................................
		Notes:
		...................................................................
		You may use this code under the same license as Datacrow itself.
		...................................................................
		The Layout is designed and tested only with data from DC's IMDB
		Wrapper. In particular, the layout for the technical column assumes
		IMDB-URL length for best fit. Other URL lengths should be adapted by
		changing the percentage of the column widths in this script. Layout is
		for A4 page size.
		...................................................................
		"Title (local)" entries are automatically used as main title when
		present, otherwise a fallback to normal "Title" happens.
		...................................................................
		"Audio Language" is handled smartly. You can set a default Langugage
		in the variables of this script for which no audio language is printed
		done. For other languages and multiple audio languages, all are
		listed. Default "no print" audio language is "German". To switch off
		this behaviour, set the language variable to the empty String.
		................................................................
		Requires Datacrow 3.5.0 or better.
		................................................................
	-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">

	<xsl:output method="xml" version="1.0" omit-xml-declaration="no"
		indent="yes" />

	<!-- Variables used by desert layout -->
	<xsl:variable name="fontSize">
		9pt
	</xsl:variable>
	<xsl:variable name="labelColor">
		#FFFFCC
	</xsl:variable>
	<xsl:variable name="textColor">
		#FFFFEB
	</xsl:variable>
	<xsl:variable name="borderBottomColor">
		#FFEA96
	</xsl:variable>
	<xsl:variable name="borderBottomWidth">
		1
	</xsl:variable>
	<xsl:variable name="pictureSize">
		5cm
	</xsl:variable>
	<xsl:variable name="pictureSizeSmall">
		2cm
	</xsl:variable>
	<xsl:variable name="rowHeight">
		0.50cm
	</xsl:variable>
	<xsl:variable name="paddingTop">
		2
	</xsl:variable>
	<xsl:variable name="paddingLeft">
		2
	</xsl:variable>

	<!-- ..................................................... -->
	<!-- New variables for xvids layout -->
	<!-- ..................................................... -->

	<!-- ............................... -->
	<!-- Labels for text subject to I18N -->
	<!-- ............................... -->

	<!-- Labels in the first non-picture colum -->
	<xsl:variable name="label-subtitle">
		<xsl:text>Subtitles: </xsl:text>
	</xsl:variable>
	<xsl:variable name="label-audio-language">
		<xsl:text>Audio: </xsl:text>
	</xsl:variable>

	<!-- Labels for the column headings -->
	<xsl:variable name="label-image-column">
		Poster
	</xsl:variable>
	<xsl:variable name="label-title-column">
		Title
	</xsl:variable>
	<xsl:variable name="label-content-column">
		Production
	</xsl:variable>
	<xsl:variable name="label-technical-column">
		Metadata
	</xsl:variable>

	<!-- Labels for page numbering -->
	<xsl:variable name="label-page-page">
		<xsl:text>Page </xsl:text>
	</xsl:variable>

	<xsl:variable name="label-page-of">
		<xsl:text> of </xsl:text>
	</xsl:variable>

	<!-- .......... -->
	<!-- Font sizes -->
	<!-- .......... -->

	<xsl:variable name="font-size-colum-title-title">
		10pt
	</xsl:variable>
	<xsl:variable name="font-size-colum-title-lang">
		6pt
	</xsl:variable>
	<xsl:variable name="font-size-colum-content">
		8pt
	</xsl:variable>
	<xsl:variable name="font-size-colum-technical">
		6pt
	</xsl:variable>

	<!-- ............. -->
	<!-- Control flow  -->
	<!-- ............. -->

	<!--
		If the audio language is equal to this variable, and there is exactly
		one audio language, output of language is suppressed
	-->

	<xsl:variable name="i18n-audio-language-default">
		German
	</xsl:variable>

	<!-- ............. -->
	<!-- Internal use  -->
	<!-- ............. -->

	<!-- Etc. (internal constants) -->
	<xsl:variable name="label-spacer">
		<xsl:text>&#x00A0;</xsl:text>
	</xsl:variable>

	<xsl:variable name="empty-string" select="''" />

	<xsl:variable name="normalized-i18n-audio-language-default"
		select="normalize-space($i18n-audio-language-default)" />

	<!-- ..................................................... -->

	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="A4"
					page-height="29.7cm" page-width="21cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body />
					<fo:region-after />
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="A4">

				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align="right" font-size="{$fontSize}">
						<xsl:copy-of select="$label-page-page" />
						<fo:page-number />
						<xsl:copy-of select="$label-page-of" />
						<fo:page-number-citation ref-id="EndOfDocument" />
					</fo:block>
				</fo:static-content>
				<xsl:apply-templates />
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:template match="data-crow-objects">

		<fo:flow flow-name="xsl-region-body">
			<fo:block font-size="{$fontSize}">

				<fo:table table-layout="fixed" width="100%"
					background-color="{$textColor}">

					<fo:table-column column-width="15%" />
					<fo:table-column column-width="31%" />
					<fo:table-column column-width="31%" />
					<fo:table-column column-width="23%" />

					<fo:table-header>
						<fo:table-row height="{$rowHeight}">
							<xsl:call-template name="table-header-cell">
								<xsl:with-param name="label" select="$label-image-column" />
							</xsl:call-template>
							<xsl:call-template name="table-header-cell">
								<xsl:with-param name="label" select="$label-title-column" />
							</xsl:call-template>
							<xsl:call-template name="table-header-cell">
								<xsl:with-param name="label" select="$label-content-column" />
							</xsl:call-template>
							<xsl:call-template name="table-header-cell">
								<xsl:with-param name="label" select="$label-technical-column" />
							</xsl:call-template>
						</fo:table-row>
					</fo:table-header>

					<fo:table-body>
						<xsl:apply-templates />
					</fo:table-body>
				</fo:table>
			</fo:block>
			<fo:block id="EndOfDocument" />
		</fo:flow>
	</xsl:template>

	<xsl:template match="picture-front">
		<fo:block>
			<xsl:choose>
				<xsl:when test="./text() = $empty-string">
					<xsl:copy-of select="$label-spacer" />
				</xsl:when>
				<xsl:otherwise>
					<fo:external-graphic content-width="{$pictureSizeSmall}"
						content-height="{$pictureSizeSmall}" scaling="uniform">
						<xsl:attribute name="src">url('<xsl:value-of
							select="." />')</xsl:attribute>
					</fo:external-graphic>
				</xsl:otherwise>
			</xsl:choose>
		</fo:block>
	</xsl:template>

	<xsl:template match="subtitle-languages">
		<xsl:if test="count(language/name) != 0">
			<fo:block font-size="{$font-size-colum-title-lang}">
				<xsl:copy-of select="$label-subtitle" />
				<xsl:for-each select="language/name">
					<xsl:choose>
						<xsl:when test="position() = last()">
							<xsl:value-of select="." />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="." />
							<xsl:text>, </xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</fo:block>
		</xsl:if>
	</xsl:template>

	<xsl:template match="title | name">
		<!--
			Work around DC issues where IMDB imported titles have year in round
			brackets beding the actual title. Directors and actors may have "(I)"
			or other funny markers behind their name in round brackets as well.
			We cut everything behind the first round bracket for this elements.
		-->
		<xsl:choose>
			<xsl:when test="contains(text(), '(')">
				<xsl:value-of select="normalize-space(substring-before(./text(), '('))" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="." />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="directors">
		<fo:block>
			<xsl:choose>
				<xsl:when test="count(director) = 1">
					<xsl:apply-templates select="director/name" />
				</xsl:when>
				<xsl:otherwise>
					<!-- Only list first 2 directors to keep layout happy -->
					<xsl:apply-templates select="director[1]/name" />
					<xsl:text>, </xsl:text>
					<xsl:apply-templates select="director[2]/name" />
				</xsl:otherwise>
			</xsl:choose>
		</fo:block>
	</xsl:template>

	<xsl:template match="countries">
		<xsl:choose>
			<xsl:when test="count(country) = 1">
				<xsl:value-of select="country/name" />
			</xsl:when>
			<xsl:otherwise>
				<!-- Only list first 2 countries to keep layout happy -->
				<xsl:value-of select="country[1]/name" />
				<xsl:text>/</xsl:text>
				<xsl:value-of select="country[2]/name" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="audio-languages">
		<xsl:if
			test="
			   	(
					count(language) = 1
					and language/name/text() != $normalized-i18n-audio-language-default
				)
				or count(language) > 1
		        							">
			<fo:block font-size="{$font-size-colum-title-lang}">
				<xsl:copy-of select="$label-audio-language" />
				<xsl:choose>
					<xsl:when test="count(language) = 1">
						<xsl:if
							test="language/name/text() != $normalized-i18n-audio-language-default">
							<xsl:value-of select="language/name" />
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="language/name">
							<xsl:value-of select="." />
							<xsl:if test="position() != last()">
								<xsl:text>, </xsl:text>
							</xsl:if>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</fo:block>
		</xsl:if>

	</xsl:template>

	<xsl:template match="rating">
		<xsl:variable name="rating" select="number(substring-before(., ' '))" />
		<fo:block>
			<xsl:text>R: </xsl:text>
			<xsl:value-of select="$rating" />
			<xsl:text>/10    </xsl:text>
			<!--
				<xsl:call-template name="rating-as-unicode-circled-numbers">
			-->
			<xsl:call-template name="rating-as-unicode-stars">
				<xsl:with-param name="rating" select="$rating" />
			</xsl:call-template>
		</fo:block>
	</xsl:template>

	<xsl:template match="video-bitrate | audio-bitrate">
		<xsl:text> [</xsl:text>
		<xsl:value-of select="." />
		<xsl:text> kbps]</xsl:text>
	</xsl:template>

	<xsl:template match="webpage | genres">
		<fo:block>
			<xsl:value-of select="." />
		</fo:block>
	</xsl:template>

	<xsl:template match="movie">
		<fo:table-row height="{$pictureSizeSmall}">

			<!-- ............................. -->
			<!-- Column 1: Poster              -->
			<!-- ............................. -->

			<fo:table-cell border-bottom-style="solid"
				border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}"
				padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
				<xsl:apply-templates select="picture-front" />
			</fo:table-cell>

			<!-- ............................. -->
			<!-- Column 2: Title and languages -->
			<!-- ............................. -->

			<fo:table-cell border-bottom-style="solid"
				border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}"
				padding-top="{$paddingTop}" padding-left="{$paddingLeft}">

				<!--
					Use the localized title if available, else fall back to original
					title
				-->
				<fo:block font-weight="bold" font-size="$font-size-colum-title-title">
					<xsl:choose>
						<xsl:when test="normalize-space(title-local) != $empty-string">
							<xsl:value-of select="title-local" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="title" />
						</xsl:otherwise>
					</xsl:choose>
				</fo:block>
				<fo:block font-size="4">
					<xsl:copy-of select="$label-spacer" />
				</fo:block>
				<xsl:apply-templates select="audio-languages" />
				<xsl:apply-templates select="subtitle-languages" />
			</fo:table-cell>

			<!-- ...................................... -->
			<!-- Column 3: Content / Production Details -->
			<!-- ...................................... -->

			<fo:table-cell border-bottom-style="solid"
				border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}"
				padding-top="{$paddingTop}" padding-left="{$paddingLeft}">

				<fo:block font-size="{$font-size-colum-content}">
					<xsl:apply-templates select="directors" />
					<fo:block>
						<xsl:apply-templates select="countries" />
						<xsl:text>, </xsl:text>
						<xsl:value-of select="year" />
						<xsl:text>, </xsl:text>
						<xsl:value-of select="playlength" />
						<xsl:text> h</xsl:text>
					</fo:block>

					<fo:block>
						<xsl:text>&#8222;</xsl:text>
						<xsl:apply-templates select="title" />
						<xsl:text>&#8220;</xsl:text>
					</fo:block>
					<fo:block>
						<xsl:copy-of select="$label-spacer" />
					</fo:block>
					<xsl:apply-templates select="genres" />
				</fo:block>
			</fo:table-cell>

			<!-- ...................................... -->
			<!-- Column 4: Technical Details            -->
			<!-- ...................................... -->

			<fo:table-cell border-bottom-style="solid"
				border-bottom-color="{$borderBottomColor}" border-before-width="{$borderBottomWidth}"
				padding-top="{$paddingTop}" padding-left="{$paddingLeft}">

				<fo:block font-size="{$font-size-colum-technical}">
					<fo:block>
						<xsl:text>V: </xsl:text>
						<xsl:value-of select="video-codec" />
						<xsl:apply-templates select="video-bitrate" />
					</fo:block>
					<fo:block>
						<xsl:text>A: </xsl:text>
						<xsl:value-of select="audio-codec" />
						<xsl:apply-templates select="audio-bitrate" />
					</fo:block>
					<fo:block>
						<xsl:text>W/H: </xsl:text>
						<xsl:value-of select="width" />
						<xsl:text> x </xsl:text>
						<xsl:value-of select="height" />
					</fo:block>
					<fo:block>
						<xsl:text>Stores: </xsl:text>
                        <xsl:for-each select="container/container">
                            <xsl:value-of select="name" />
                            <xsl:if test="position()!=last()">
                                <xsl:text>,&#160;</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
					</fo:block>
					<xsl:apply-templates select="webpage" />
					<fo:block>
						<xsl:copy-of select="$label-spacer" />
					</fo:block>
					<xsl:apply-templates select="rating" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

	<!-- .................................. -->
	<!-- Static Subroutines for bulk text   -->
	<!-- .................................. -->

	<xsl:template name="table-header-cell">
		<xsl:param name="label" />
		<fo:table-cell background-color="{$labelColor}"
			border-bottom-style="solid" border-bottom-color="{$borderBottomColor}"
			border-before-width="{$borderBottomWidth}" padding-top="{$paddingTop}"
			padding-left="{$paddingLeft}">
			<fo:block>
				<xsl:value-of select="normalize-space($label)" />
			</fo:block>
		</fo:table-cell>
	</xsl:template>

	<xsl:template name="rating-as-unicode-circled-numbers">
		<xsl:param name="rating" />
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
