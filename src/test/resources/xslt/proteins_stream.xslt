<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pxsl="blinded"
    exclude-result-prefixes="pxsl">
    <xsl:output indent="yes" method="xml"/>
    <xsl:mode name="stream" streamable="yes"/>
    
    <xsl:template match="/ProteinDatabase" mode="#all">
        <ProteinDatabase>
            <xsl:apply-templates select="ProteinEntry" mode="#current"/>
        </ProteinDatabase>        
    </xsl:template>
    
    <xsl:template match="/ProteinDatabase/ProteinEntry" mode="#all" pxsl:core="yes">
        <ProteinEntry id="{@id}">
            <xsl:apply-templates mode="#current"/>            
        </ProteinEntry>
    </xsl:template>
    
    <xsl:template match="ProteinEntry/protein/name" mode="#all">
        <name>
            <xsl:value-of select="replace(., '^\s*(.+?)\s*$', '$1')" />
        </name>
    </xsl:template>
    <xsl:template match="ProteinEntry/sequence" mode="#all">
        <sequence>
            <xsl:value-of select="normalize-space(.)" />
        </sequence>
    </xsl:template>
    
    <xsl:template match="text()" mode="#all"/>
</xsl:stylesheet>