<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:import href="../../_stylesheets/pdf_desert.xsl" />

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

			<xsl:for-each select="data-crow-objects/audio-cd">
				<fo:page-sequence master-reference="A4">

					<fo:static-content flow-name="xsl-region-after">
						<fo:block text-align="right" font-size="{$fontSize}">
							page
							<fo:page-number />
						</fo:block>
					</fo:static-content>

					<fo:flow flow-name="xsl-region-body">

						<fo:block font-size="{$fontSize}" font-weight="bold">
							<xsl:value-of select="title" />
						</fo:block>

						<fo:block font-size="{$fontSize}">

							<fo:table table-layout="fixed" width="14cm"
								border-collapse="separate" background-color="{$textColor}">

								<fo:table-column column-width="9cm" />
								<fo:table-column column-width="5cm" />

								<fo:table-body>
									<fo:table-row>

										<fo:table-cell>
											<fo:block>
												<fo:table background-color="{$textColor}"
													table-layout="fixed" width="9cm" border-collapse="separate">

													<fo:table-column column-width="2cm" />
													<fo:table-column column-width="9cm" />

													<fo:table-body>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">Artists</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:for-each select="artists/artist">
																		<xsl:value-of select="name" />
																		<xsl:if test="position()!=last()">
																			<xsl:text>,&#160;</xsl:text>
																		</xsl:if>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">Genres</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:for-each select="genres/music-genre">
																		<xsl:value-of select="name" />
																		<xsl:if test="position()!=last()">
																			<xsl:text>,&#160;</xsl:text>
																		</xsl:if>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">Year</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:value-of select="year" />
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">State</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:value-of select="state" />
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">Container</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:value-of select="container" />
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

														<fo:table-row height="{$rowHeight}">
															<fo:table-cell background-color="{$labelColor}"
																padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
																<fo:block font-weight="bold">Rating</fo:block>
															</fo:table-cell>
															<fo:table-cell padding-top="{$paddingTop}"
																padding-left="{$paddingLeft}">
																<fo:block>
																	<xsl:value-of select="rating" />
																</fo:block>
															</fo:table-cell>
														</fo:table-row>

													</fo:table-body>
												</fo:table>
											</fo:block>
										</fo:table-cell>

										<fo:table-cell>
											<fo:block>
												<xsl:if test="picture-front != ''">
													<fo:table table-layout="fixed" width="{$pictureSize}"
														border-collapse="separate">
														<fo:table-body>
															<fo:table-row>
																<fo:table-cell>
																	<fo:block>
																		<fo:external-graphic
																			content-width="{$pictureSize}" content-height="{$pictureSize}"
																			scaling="uniform">
																			<xsl:attribute name="src">url('<xsl:value-of
																				select="picture-front" />')</xsl:attribute>
																		</fo:external-graphic>
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
														</fo:table-body>
													</fo:table>
												</xsl:if>
											</fo:block>
										</fo:table-cell>
									</fo:table-row>
								</fo:table-body>
							</fo:table>
						</fo:block>

						<xsl:if test="boolean(audio-tracks/*)">
							<fo:block font-size="{$fontSize}" space-before="20">
								<fo:table background-color="{$textColor}" table-layout="fixed"
									width="14cm" border-collapse="separate">

									<fo:table-column column-width="2cm" />
									<fo:table-column column-width="10cm" />
									<fo:table-column column-width="2cm" />

									<fo:table-header>
										<fo:table-row height="{$rowHeight}">
											<fo:table-cell background-color="{$labelColor}"
												padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
												<fo:block font-weight="bold">Nr</fo:block>
											</fo:table-cell>
											<fo:table-cell background-color="{$labelColor}"
												padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
												<fo:block font-weight="bold">Title</fo:block>
											</fo:table-cell>
											<fo:table-cell background-color="{$labelColor}"
												padding-top="{$paddingTop}" padding-left="{$paddingLeft}">
												<fo:block font-weight="bold">Length</fo:block>
											</fo:table-cell>
										</fo:table-row>
									</fo:table-header>

									<fo:table-body>
										<xsl:for-each select="audio-tracks/audio-track">
											<fo:table-row height="{$rowHeight}">
												<fo:table-cell padding-top="{$paddingTop}"
													padding-left="{$paddingLeft}">
													<fo:block>
														<xsl:value-of select="track" />
													</fo:block>
												</fo:table-cell>
												<fo:table-cell padding-top="{$paddingTop}"
													padding-left="{$paddingLeft}">
													<fo:block>
														<xsl:value-of select="title" />
													</fo:block>
												</fo:table-cell>
												<fo:table-cell padding-top="{$paddingTop}"
													padding-left="{$paddingLeft}">
													<fo:block>
														<xsl:value-of select="playlength" />
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
										</xsl:for-each>
									</fo:table-body>
								</fo:table>
							</fo:block>
						</xsl:if>
             
          &#x20;&#x200b;
          
        </fo:flow>
      </fo:page-sequence>
      </xsl:for-each>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
