<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:pxsl="blinded"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:vf="urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1"
    xmlns:gml="http://www.opengis.net/gml/3.2" 
    exclude-result-prefixes="pxsl">    
    <xsl:param name="output-folder" as="xs:string" required="yes" />
    <xsl:output indent="yes" />
    <xsl:mode name="stream" streamable="no" />  
    
    <xsl:template match="/vf:VymennyFormat" mode="stream">        
        <DocumentListing>            
            <xsl:apply-templates select="vf:Data/*" mode="stream" />
        </DocumentListing>
    </xsl:template>    
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Obce/vf:Obec" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:CastiObci/vf:CastObce" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:KatastralniUzemi/vf:KatastralniUzemi" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Zsj/vf:Zsj" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Ulice/vf:Ulice" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Parcely/vf:Parcela" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:SpravniObvody/vf:SpravniObvod" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:StavebniObjekty/vf:StavebniObjekt" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:AdresniMista/vf:AdresniMisto" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Momc/vf:Momc" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template match="/vf:VymennyFormat/vf:Data/vf:Mop/vf:Obec" mode="stream" pxsl:core="yes">
        <xsl:call-template name="doProcess" />
    </xsl:template>
    <xsl:template name="doProcess">    
        <xsl:variable name="data" select="copy-of(.)" />
        <xsl:variable name="identifier" select="@gml:id" as="xs:string"/>
        <xsl:variable name="file" as="xs:string"
            select="$output-folder || '/' || local-name(.) || '/' || $identifier || '.xml'"/>        
        <file collection="{local-name(.)}"
            ruian-type="{ local-name(.) }" kod="{ $identifier }"            
            href="{ $file }" />
        <xsl:result-document href="{ $file }">                    
            <xsl:sequence select="."/>
        </xsl:result-document>
    </xsl:template>
</xsl:stylesheet>
