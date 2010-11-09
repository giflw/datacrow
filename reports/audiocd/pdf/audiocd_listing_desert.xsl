<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no"
		indent="yes" />
	<xsl:param name="versionParam" select="'1.0'" />


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
					<fo:block text-align="right" font-size="10pt">
						page
						<fo:page-number />
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<fo:block font-size="10pt">

						<fo:table table-layout="fixed" width="100%"
							background-color="#FFFFEB">

							<fo:table-column column-width="25%" />
							<fo:table-column column-width="30%" />
							<fo:table-column column-width="20%" />
							<fo:table-column column-width="10%" />
							<fo:table-column column-width="15%" />

							<fo:table-header>
								<fo:table-row height="1cm">

									<fo:table-cell background-color="#FFFFCC"
										border-bottom-style="solid" border-bottom-color="#FFEA96"
										border-before-width="1" padding-top="2" padding-left="2">
										<fo:block font-weight="bold">Artists</fo:block>
									</fo:table-cell>

									<fo:table-cell background-color="#FFFFCC"
										border-bottom-style="solid" border-bottom-color="#FFEA96"
										border-before-width="1" padding-top="2" padding-left="2">
										<fo:block font-weight="bold">Title</fo:block>
									</fo:table-cell>

									<fo:table-cell background-color="#FFFFCC"
										border-bottom-style="solid" border-bottom-color="#FFEA96"
										border-before-width="1" padding-top="2" padding-left="2">
										<fo:block font-weight="bold">Genres</fo:block>
									</fo:table-cell>

									<fo:table-cell background-color="#FFFFCC"
										border-bottom-style="solid" border-bottom-color="#FFEA96"
										border-before-width="1" padding-top="2" padding-left="2">
										<fo:block font-weight="bold">Year</fo:block>
									</fo:table-cell>

									<fo:table-cell background-color="#FFFFCC"
										border-bottom-style="solid" border-bottom-color="#FFEA96"
										border-before-width="1" padding-top="2" padding-left="2">
										<fo:block font-weight="bold">Cover</fo:block>
									</fo:table-cell>

								</fo:table-row>
							</fo:table-header>

							<fo:table-body>
								<xsl:for-each select="data-crow-objects/audio-cd">
									<fo:table-row height="2cm">

										<fo:table-cell border-bottom-style="solid"
											border-bottom-color="#FFEA96" border-before-width="1"
											padding-top="2" padding-left="2">
											<fo:block>
												<xsl:for-each select="artists/artist">
													<xsl:value-of select="name" />
													<xsl:if test="position()!=last()">
														<xsl:text>,&#160;</xsl:text>
													</xsl:if>
												</xsl:for-each>
											</fo:block>
										</fo:table-cell>

										<fo:table-cell border-bottom-style="solid"
											border-bottom-color="#FFEA96" border-before-width="1"
											padding-top="2" padding-left="2">
											<fo:block>
												<xsl:value-of select="title" />
											</fo:block>
										</fo:table-cell>

										<fo:table-cell border-bottom-style="solid"
											border-bottom-color="#FFEA96" border-before-width="1"
											padding-top="2" padding-left="2">
											<fo:block>
												<xsl:for-each select="genres/music-genre">
													<xsl:value-of select="name" />
													<xsl:if test="position()!=last()">
														<xsl:text>,&#160;</xsl:text>
													</xsl:if>
												</xsl:for-each>
											</fo:block>
										</fo:table-cell>

										<fo:table-cell border-bottom-style="solid"
											border-bottom-color="#FFEA96" border-before-width="1"
											padding-top="2" padding-left="2">
											<fo:block>
												<xsl:value-of select="year" />
											</fo:block>
										</fo:table-cell>

										<fo:table-cell border-bottom-style="solid"
											border-bottom-color="#FFEA96" border-before-width="1"
											padding-top="2" padding-left="2">
											<fo:block>
												<xsl:if test="picture-front != ''">
													<fo:external-graphic content-width="2cm"
														content-height="2cm" scaling="uniform">
														<xsl:attribute name="src">url('<xsl:value-of
															select="picture-front" />')</xsl:attribute>
													</fo:external-graphic>
												</xsl:if>

												<xsl:if test="picture-front = ''">
													&#x00A0;
												</xsl:if>
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
