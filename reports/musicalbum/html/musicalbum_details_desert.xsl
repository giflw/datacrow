<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<html>

			<head>
				<link type="text/css" rel="stylesheet" href="desert.css" />
			</head>

			<body>
				<h1>Music Albums</h1>

				<xsl:for-each select="data-crow-objects/music-album">

					<h1>
						<xsl:value-of select="title" />
					</h1>

					<table width="600" cellspacing="0" class="layout">
						<colgroup valign="top" align="left">
							<col width="500" />
							<col width="100" />
						</colgroup>

						<tr class="layout">
							<td class="layout">
								<table style="border:0;" cellspacing="0" cellpadding="10">
									<colgroup valign="top" align="left">
										<col width="100" />
										<col width="200" />
									</colgroup>

									<tr>
										<th>Artists</th>
										<td>
											<xsl:for-each select="artists/artist">
												<xsl:value-of select="name" />
		                                        <xsl:if test="position()!=last()">
		                                            <xsl:text>, </xsl:text> 
		                                        </xsl:if>   
											</xsl:for-each>
										</td>
									</tr>

									<tr>
										<th>Genres</th>
										<td>
											<xsl:for-each select="genres/music-genre">
												<xsl:value-of select="name" />
                                                <xsl:if test="position()!=last()">
                                                    <xsl:text>, </xsl:text> 
                                                </xsl:if>   
											</xsl:for-each>
										</td>
									</tr>

									<tr>
										<th>Year</th>
										<td>
											<xsl:value-of select="year" />
										</td>
									</tr>


									<tr>
										<th>State</th>
										<td>
											<xsl:value-of select="state" />
										</td>
									</tr>

									<tr>
										<th>Containers</th>
										<td>
                                            <xsl:for-each select="container/container">
                                                <xsl:value-of select="name" />
                                                <xsl:if test="position()!=last()">
                                                    <xsl:text>, </xsl:text> 
                                                </xsl:if>   
                                            </xsl:for-each>
										</td>
									</tr>

									<tr>
										<th>Rating</th>
										<td>
											<xsl:value-of select="rating" />
										</td>
									</tr>
								</table>
							</td>

							<td class="layout">
								<table style="border:0;">
									<tr>
										<td>
											<xsl:if test="picture-front != ''">
												<img alt="">
													<xsl:attribute name="src"><xsl:value-of
														select="picture-front" /></xsl:attribute>
												</img>
											</xsl:if>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>

					<br />

					<xsl:if test="music-tracks/*">
						<table style="border:0;" width="600" cellspacing="0"
							cellpadding-left="2">
							<colgroup valign="top" align="left">
								<col width="20" />
								<col width="440" />
								<col width="40" />
							</colgroup>

							<tr>
								<th>Nr</th>
								<th>Title</th>
								<th>Playlength</th>
							</tr>

							<xsl:for-each select="music-tracks/music-track">
								<tr>
									<td>
										<xsl:value-of select="track" />
									</td>
									<td>
										<xsl:value-of select="title" />
									</td>
									<td>
										<xsl:value-of select="playlength" />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</xsl:if>

					<br />
					<br />
					<br />

				</xsl:for-each>

			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
