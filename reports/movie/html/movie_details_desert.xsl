<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<html>

			<head>
				<link type="text/css" rel="stylesheet" href="desert.css" />
			</head>

			<body>
				<h1>Movies</h1>

				<xsl:for-each select="data-crow-objects/movie">

					<h1>
						<xsl:value-of select="title" />
					</h1>

					<table width="600" style="border:0;" cellspacing="0"
						cellpadding="10">

						<colgroup valign="top" align="left">
							<col width="100" />
							<col width="500" />
						</colgroup>

						<tr>
							<th>Description</th>
							<td>
								<xsl:value-of select="description" />
							</td>
						</tr>
						<tr>
							<th>Genres</th>
							<td>
								<xsl:for-each select="genres/genre">
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
							<th>Actors</th>
							<td>
								<xsl:for-each select="actors/actor">
									<xsl:value-of select="name" />
                                    <xsl:if test="position()!=last()">
                                        <xsl:text>, </xsl:text> 
                                    </xsl:if> 									
								</xsl:for-each>
							</td>
						</tr>
						<tr>
							<th>Directors</th>
							<td>
								<xsl:for-each select="directors/director">
									<xsl:value-of select="name" />
                                    <xsl:if test="position()!=last()">
                                        <xsl:text>, </xsl:text> 
                                    </xsl:if> 									
								</xsl:for-each>
							</td>
						</tr>
						<tr>
							<th>Playlength</th>
							<td>
								<xsl:value-of select="playlength" />
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
							<th>State</th>
							<td>
								<xsl:value-of select="state" />
							</td>
						</tr>
						<tr>
							<th>Rating</th>
							<td>
								<xsl:value-of select="rating" />
							</td>
						</tr>
					</table>

					<br />

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

							<td>
								<xsl:if test="picture-cd != ''">
									<img alt="">
										<xsl:attribute name="src"><xsl:value-of
											select="picture-cd" /></xsl:attribute>
									</img>
								</xsl:if>
							</td>

							<td>
								<xsl:if test="picture-back != ''">
									<img alt="">
										<xsl:attribute name="src"><xsl:value-of
											select="picture-back" /></xsl:attribute>
									</img>
								</xsl:if>
							</td>
						</tr>
					</table>
					<br />
					<br />
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
