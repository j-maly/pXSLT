<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes" method="xml"/>
    <xsl:mode name="stream" streamable="no"/>

    <xsl:template match="/BookStore" mode="#all">
        <BookStoreProcessed>
            <xsl:apply-templates />
        </BookStoreProcessed>
    </xsl:template>

    <xsl:template match="/BookStore/Book[Date &gt; 1970]" mode="#all">
        <Book>
            <xsl:copy-of select="Title" />
            <xsl:copy-of select="Author" />
        </Book>
    </xsl:template>

    <xsl:template match="text()|@*" mode="#all" />
</xsl:stylesheet>