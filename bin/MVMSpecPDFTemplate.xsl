<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                      xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:fox="http://xml.apache.org/fop/extensions"
                      xmlns:xi="http://www.w3.org/2001/Xinclude" xmlns:foa="http://fabio"
                      xmlns:xalan="org.apache.xalan.lib.Extensions" xmlns:redirect="org.apache.xalan.lib.Redirect"
                      xmlns:svg="http://www.w3.org/2000/svg"
                      xmlns:lxslt="http://xml.apache.org/xslt" extension-element-prefixes="redirect" exclude-result-prefixes="redirect lxslt">

<xsl:variable name="max_letter_col_1"><xsl:value-of select="root/Max_Cols_Size/@Col_1"/></xsl:variable><!--Format-->
<xsl:variable name="max_letter_col_2"><xsl:value-of select="root/Max_Cols_Size/@Col_2"/></xsl:variable><!--Zone-->
<xsl:variable name="max_letter_col_3"><xsl:value-of select="root/Max_Cols_Size/@Col_3"/></xsl:variable><!--Position-->
<xsl:variable name="max_letter_col_4"><xsl:value-of select="root/Max_Cols_Size/@Col_4"/></xsl:variable><!--Denotation-->
<xsl:variable name="max_letter_col_5"><xsl:value-of select="root/Max_Cols_Size/@Col_5"/></xsl:variable><!--Nomination-->
<xsl:variable name="max_letter_col_6"><xsl:value-of select="root/Max_Cols_Size/@Col_6"/></xsl:variable><!--Quantity-->
<xsl:variable name="max_letter_col_7"><xsl:value-of select="root/Max_Cols_Size/@Col_7"/></xsl:variable><!--Notes-->

<xsl:variable name="s_table_header_height">14.6mm</xsl:variable>
<xsl:variable name="s_table_body_height">7.74mm</xsl:variable>


<xsl:template match="/">

<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <!-- defines the layout master -->
  <fo:layout-master-set>
        <fo:simple-page-master master-name="First-A4_spec_typeA-page"
                               page-height="29.7cm"
                               page-width="21cm"
                               margin-top="0cm"
                               margin-bottom="0cm"
                               margin-left="0cm"
                               margin-right="0cm">
<!--background-color="yellow"-->
        <fo:region-body region-name="spec-table" margin-left="2.0cm" margin-right="0.5cm" margin-top="0.5cm" margin-bottom="0.5cm + 40mm"/>
        <fo:region-before region-name="first-region-before" extent="0.5cm"/>
        <fo:region-after  region-name="first-region-after"  extent="0.5cm"/>
        <fo:region-start  region-name="first-region-start"  extent="0.5cm"/>
        <fo:region-end    region-name="first-region-end"    extent="0.5cm"/>
    </fo:simple-page-master>


    <fo:simple-page-master master-name="Other-A4_spec_typeA-page"
                           page-height="29.7cm"
                           page-width="21cm"
                           margin-top="0cm"
                           margin-bottom="0cm"
                           margin-left="0cm"
                           margin-right="0cm">

        <fo:region-body region-name="spec-table" margin-left="2.0cm" margin-right="0.5cm" margin-top="0.5cm" margin-bottom="0.5cm + 15mm"/>
        <fo:region-before  region-name="other-region-before" extent="0.5cm"/>
        <fo:region-after   region-name="other-region-after"  extent="0.5cm"/>
        <fo:region-start   region-name="other-region-start"  extent="0.5cm"/>
        <fo:region-end     region-name="other-region-end"    extent="0.5cm"/>
    </fo:simple-page-master>


    <fo:page-sequence-master master-name="main-sequence">
        <fo:repeatable-page-master-alternatives maximum-repeats="no-limit">
            <fo:conditional-page-master-reference master-reference="First-A4_spec_typeA-page" page-position="first"/>
            <fo:conditional-page-master-reference master-reference="Other-A4_spec_typeA-page"/>
        </fo:repeatable-page-master-alternatives>
    </fo:page-sequence-master>

  </fo:layout-master-set>




  <fo:page-sequence font-family="arial" master-reference="main-sequence">

        <fo:static-content flow-name="first-region-start">
           <!--fo:block-container border-color="black" border-right-style="solid" border-right="0.3mm" position="absolute" top="5mm" bottom="5mm" left="0mm" right="0mm">
             <fo:block/>
           </fo:block-container-->
           <!--fo:block-container border-color="black" border-right-style="solid" border-right="0.3mm" position="absolute" top="5mm" bottom="5mm" left="0mm" right="-15mm+0.15mm">
             <fo:block>TEXT</fo:block>
           </fo:block-container-->
		   <fo:block-container position="absolute" top="5mm" bottom="178mm" left="0mm" right="0mm" reference-orientation="90">
		     <!-- fo:block font-style="normal" font-size="2.18mm"><xsl:value-of select="root/Ip_data/@Ip_classification"/></fo:block -->
			 <fo:block padding-top="1mm" font-family="arial" text-align="center" font-style="normal" font-size="5mm"><xsl:value-of select="root/Izdelie_osnovnai_nadpis/@BASEDOC"/></fo:block>
		   </fo:block-container>
           <fo:block-container position="absolute"  reference-orientation="90"  top="5mm" left="8mm" height="12mm" width="114mm">
                <xsl:call-template name="Add_minor_stamp">
                   <xsl:with-param name="PervPrimen" select="root/Izdelie_osnovnai_nadpis/@PERVPRIM"/>
                </xsl:call-template>
           </fo:block-container>
		   
		   <fo:block-container position="absolute"  reference-orientation="90"  top="5mm" left="8mm" height="10mm" width="142mm">
                <xsl:call-template name="Add_center_stamp">
                   <xsl:with-param name="prjName" select="root/Izdelie_osnovnai_nadpis/@PROJECTNAME"/>
				   <xsl:with-param name="spCode" select="root/Izdelie_osnovnai_nadpis/@SPCODE"/>
                </xsl:call-template>
           </fo:block-container>
		   
		   

           <fo:block-container position="absolute"  reference-orientation="90"  top="147mm" left="8mm" height="12mm" width="145mm">
                <xsl:call-template name="Add_main_stamp_gost_2104_68"/>
           </fo:block-container>

        </fo:static-content>


        <fo:static-content flow-name="first-region-end">
		<!--
           <fo:block-container border-color="black" border-left-style="solid" border-left="0.3mm" position="absolute" top="5mm" bottom="5mm" left="0mm-0.15mm" right="0mm">
             <fo:block background-color="yellow"><fo:inline font-style ="normal">Копировал</fo:inline></fo:block>
           </fo:block-container>
		   -->
		   <fo:block-container border-color="black" border-left-style="solid" border-left="0.5mm" position="absolute" top="5mm" bottom="5mm" left="0.25mm" right="0mm">
             <fo:block/>
           </fo:block-container>
		   <fo:block-container position="absolute" top="5mm" bottom="10mm" left="1.41mm" right="0mm" reference-orientation="90">
		     <fo:block font-style="normal" font-size="2.18mm"><xsl:value-of select="root/Ip_data/@Ip_classification"/></fo:block>
		   </fo:block-container>
		   <fo:block-container position="absolute" top="5mm" bottom="40mm" left="1.41mm" right="0mm" reference-orientation="90">
		     <fo:block font-style="normal" font-size="2.18mm"><xsl:value-of select="root/Ip_data/@Plant_address"/></fo:block>
		   </fo:block-container>
        </fo:static-content>


        <fo:static-content flow-name="first-region-before">
           <fo:block-container top="1mm" position="absolute">
           <fo:block font-family="arial" text-align="right" font-size="3mm">
             <fo:inline font-style ="normal"></fo:inline>
           </fo:block>
           </fo:block-container>
           <fo:block-container border-color="black" border-bottom-style="solid" border-bottom="0.3mm" position="absolute" top="0mm" bottom="0mm" left="10mm" right="0mm">
             <fo:block/>
           </fo:block-container>

           <fo:block-container position="absolute" top="5mm" left="15mm" right="0mm" height="247mm">
                <xsl:call-template name="Empty_table">
                  <xsl:with-param name="count" select="29"/>
                </xsl:call-template>
           </fo:block-container>

        </fo:static-content>


        <fo:static-content flow-name="first-region-after">
           <fo:block-container top="1mm"  position="absolute">
               <fo:block font-family="arial" text-align="right" font-size="3mm">
                 <fo:inline font-style ="normal">Формат А4</fo:inline>
               </fo:block>
           </fo:block-container>
           <fo:block-container top="1mm" position="absolute">
               <fo:block font-family="arial" text-align="center" font-size="3mm">
                 <fo:inline font-style ="normal">Копировал</fo:inline>
               </fo:block>
           </fo:block-container>
           <fo:block-container border-color="black" border-top-style="solid" border-top="0.3mm" position="absolute" top="0mm" bottom="0mm" left="3mm" right="0mm">
                <fo:block/>
           </fo:block-container>


           <fo:block-container position="absolute" top="-40mm" left="15mm" right="0mm" height="48mm">
                <xsl:call-template name="Gross_stamp">
                  <xsl:with-param name="Zavod" select="root/Izdelie_osnovnai_nadpis/@ZAVOD"/>
                  <xsl:with-param name="Oboznach" select="root/Izdelie_osnovnai_nadpis/@OBOZNACH"/>
                  <xsl:with-param name="Naimen"   select="root/Izdelie_osnovnai_nadpis/@NAIMEN"/>
                  <xsl:with-param name="Razr"     select="root/Izdelie_osnovnai_nadpis/@RAZR"/>
                  <xsl:with-param name="Prov"     select="root/Izdelie_osnovnai_nadpis/@PROV"/>
				  <xsl:with-param name="Norm"     select="root/Izdelie_osnovnai_nadpis/@NORM"/>
				  <xsl:with-param name="Utv"     select="root/Izdelie_osnovnai_nadpis/@UTV"/>
                  <xsl:with-param name="Izm"     select="root/Izdelie_osnovnai_nadpis/@ST_IZM"/>
                  <xsl:with-param name="Dokum"     select="root/Izdelie_osnovnai_nadpis/@ST_DOKUM"/>
                  <xsl:with-param name="UdList"   select="root/Izdelie_osnovnai_nadpis/@ST_UDLIST"/>
				  <xsl:with-param name="Litera1"   select="root/Izdelie_osnovnai_nadpis/@LITERA1"/>
				  <xsl:with-param name="Litera2"   select="root/Izdelie_osnovnai_nadpis/@LITERA2"/>
				  <xsl:with-param name="Litera3"   select="root/Izdelie_osnovnai_nadpis/@LITERA3"/>
				  <xsl:with-param name="AddChecker"   select="root/Izdelie_osnovnai_nadpis/@ADDCHECKER"/>
				  <xsl:with-param name="AddCheckerDate"   select="root/Izdelie_osnovnai_nadpis/@ADDCHECKERDATE"/>
				  <xsl:with-param name="AddCheckerPost"   select="root/Izdelie_osnovnai_nadpis/@ADDCHECKERPOST"/>
				  
				  <xsl:with-param name="crtDate"   select="root/Izdelie_osnovnai_nadpis/@CRTDATE"/>
				  <xsl:with-param name="chkDate"   select="root/Izdelie_osnovnai_nadpis/@CHKDATE"/>
				  <xsl:with-param name="ctrlDate"   select="root/Izdelie_osnovnai_nadpis/@CTRLDATE"/>
				  <xsl:with-param name="aprDate"   select="root/Izdelie_osnovnai_nadpis/@APRDATE"/>
				  <xsl:with-param name="pageQty"   select="root/Izdelie_osnovnai_nadpis/@PAGEQTY"/>
				  
				  <!-- xsl:with-param name="prjName"   select="root/Izdelie_osnovnai_nadpis/@PROJECTNAME"/>
				  <xsl:with-param name="spCode"   select="root/Izdelie_osnovnai_nadpis/@SPCODE"/ -->
                </xsl:call-template>
           </fo:block-container>

        </fo:static-content>




        <fo:static-content flow-name="other-region-start">
           <!--fo:block-container border-color="black" border-right-style="solid" border-right="0.3mm" position="absolute" top="5mm" bottom="5mm" left="0mm" right="0mm">
             <fo:block/>
           </fo:block-container-->
           <fo:block-container border-color="black" border-right-style="solid" border-right="0.3mm" position="absolute" top="5mm" bottom="5mm" left="0mm" right="-15mm+0.15mm">
             <fo:block/>
           </fo:block-container>

           <fo:block-container position="absolute"  reference-orientation="90"  top="147mm" left="8mm" height="12mm" width="145mm">
                <xsl:call-template name="Add_main_stamp_gost_2104_68"/>
           </fo:block-container>
        </fo:static-content>


        <fo:static-content flow-name="other-region-end">
           <fo:block-container border-color="black" border-left-style="solid" border-left="0.5mm" position="absolute" top="5mm" bottom="5mm" left="0.25mm" right="0mm">
             <fo:block/>
           </fo:block-container>
		   <fo:block-container position="absolute" top="5mm" bottom="10mm" left="1.41mm" right="0mm" reference-orientation="90">
		     <fo:block font-style="normal" font-size="2.18mm"><xsl:value-of select="root/Ip_data/@Ip_classification"/></fo:block>
		   </fo:block-container>
		   <fo:block-container position="absolute" top="5mm" bottom="40mm" left="1.41mm" right="0mm" reference-orientation="90">
		     <fo:block font-style="normal" font-size="2.18mm"><xsl:value-of select="root/Ip_data/@Plant_address"/></fo:block>
		   </fo:block-container>
        </fo:static-content>


        <fo:static-content flow-name="other-region-before" master-reference="First-A4_spec_typeA-page">
           <fo:block-container top="1mm" position="absolute">
               <fo:block font-family="arial" text-align="right" font-size="3mm">
                 <fo:inline font-style ="normal">ГОСТ 2.108-68 Форма 1а</fo:inline>
               </fo:block>
           </fo:block-container>
           <fo:block-container border-color="black" border-bottom-style="solid" border-bottom="0.3mm" position="absolute" top="0mm" bottom="0mm" left="15mm" right="0mm">
             <fo:block/>
           </fo:block-container>

           <fo:block-container position="absolute" top="5mm" left="15mm" right="0mm" height="272mm">
                <xsl:call-template name="Empty_table">
                  <xsl:with-param name="count" select="32"/>
                </xsl:call-template>
           </fo:block-container>
        </fo:static-content>


        <fo:static-content flow-name="other-region-after">
           <fo:block-container top="1mm"  position="absolute">
           <fo:block font-family="arial" text-align="right" font-size="3mm">
             <fo:inline font-style ="normal">Формат А4</fo:inline>
           </fo:block>
           </fo:block-container>
           <fo:block-container top="1mm" position="absolute">
           <fo:block font-family="arial" text-align="center" font-size="3mm">
             <fo:inline font-style ="normal">Копировал</fo:inline>
           </fo:block>
           </fo:block-container>
           <fo:block-container border-color="black" border-top-style="solid" border-top="0.3mm" position="absolute" top="0mm" bottom="0mm" left="3mm" right="0mm">
             <fo:block/>
           </fo:block-container>

           <fo:block-container position="absolute" top="-15mm" left="15mm" right="0mm" height="15mm">
                <xsl:call-template name="Little_stamp">
                  <xsl:with-param name="Oboznach" select="root/Izdelie_osnovnai_nadpis/@OBOZNACH"/>
                  <xsl:with-param name="Izm" select="root/Izdelie_osnovnai_nadpis/@ST_IZM"/>
                  <xsl:with-param name="Dokum" select="root/Izdelie_osnovnai_nadpis/@ST_DOKUM"/>
                </xsl:call-template>
           </fo:block-container>
           

        </fo:static-content>


    <fo:flow flow-name="spec-table">
    <xsl:apply-templates select="root"/> 
    <fo:block keep-together="always" id="LastPage"></fo:block>


    </fo:flow>
  </fo:page-sequence>
</fo:root>


</xsl:template>



<!--******************************************************************************-->

<xsl:template match="root">

    <fo:table table-layout="fixed" font-style="normal" width="100%" border-collapse="collapse">
      <fo:table-column column-width="6mm"/>
      <fo:table-column column-width="6mm"/>
      <fo:table-column column-width="8mm"/>
      <fo:table-column column-width="70mm"/>
      <fo:table-column column-width="63mm"/>
      <fo:table-column column-width="10mm"/>
      <fo:table-column column-width="22mm"/>

      <fo:table-header>
        <fo:table-row line-height="{$s_table_header_height}"  height="{$s_table_header_height}">
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block-container height="4.8mm" reference-orientation="90" inline-progression-dimension="from-parent(line-height)" display-align="center" line-height="4.5mm"><fo:block text-align="center" font-size="3.5mm">Формат</fo:block></fo:block-container></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block-container height="4.8mm" reference-orientation="90" inline-progression-dimension="from-parent(line-height)" display-align="center" line-height="4.5mm"><fo:block text-align="center" font-size="3.5mm">Зона</fo:block></fo:block-container></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block-container height="6.8mm" reference-orientation="90" inline-progression-dimension="from-parent(line-height)" display-align="center" line-height="4.5mm"><fo:block text-align="center" font-size="3.5mm">Поз.</fo:block></fo:block-container></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Обозначение</fo:block></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Наименование</fo:block></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block-container height="9.8mm" reference-orientation="90" inline-progression-dimension="from-parent(line-height)" display-align="center" line-height="4mm"><fo:block text-align="center" font-size="4mm">Кол.</fo:block></fo:block-container></fo:table-cell>
          <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block-container height="14.6mm" line-height="6.7mm" display-align="center"><fo:block text-align="center">Приме-</fo:block><fo:block text-align="center">чание</fo:block></fo:block-container></fo:table-cell>
        </fo:table-row>
      </fo:table-header>


      <fo:table-body line-height="{$s_table_body_height}" height="{$s_table_body_height}"  border-width="0.25mm 0.5mm 0.25mm 0.5mm">
        <xsl:for-each select="Block"> 

            <xsl:apply-templates select="Occurrence"/>

        </xsl:for-each>


      </fo:table-body>
    </fo:table>

</xsl:template>


<xsl:template match="Occurrence">
       <fo:table-row   height='from-parent(height)' border-width='from-parent(border-width)'>
	   <xsl:variable name="merge" select="@merge"/>
       
       <xsl:if test="(parent::Block[@end_page='false']) or (position()!=1)">
        <xsl:attribute name="keep-with-previous">always</xsl:attribute>
       </xsl:if>

          <fo:table-cell border-width='from-parent(border-width)' border-style="solid" wrap-option="no-wrap">
                <xsl:call-template name="Occurrence-attrs">
                  <xsl:with-param name="col" select="Col_1"/>
                  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_1"/></xsl:with-param>
                  <xsl:with-param name="cell_width">6mm</xsl:with-param>          
                </xsl:call-template>
          </fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap" font-size="3.48mm">
                <xsl:call-template name="Occurrence-attrs">
                  <xsl:with-param name="col" select="Col_2"/>
                  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_2"/></xsl:with-param>
                  <xsl:with-param name="cell_width">6mm</xsl:with-param>
                </xsl:call-template>
          </fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap">
                <xsl:call-template name="Occurrence-attrs">
                  <xsl:with-param name="col" select="Col_3"/>
                  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_3"/></xsl:with-param>
                  <xsl:with-param name="cell_width">8mm</xsl:with-param>
                </xsl:call-template>
          </fo:table-cell>
		  <!--xsl:choose>
			  <xsl:when test="contains($merge,'true')">
				  <fo:table-cell border-width='from-parent(border-width)' background-color="red" border-style="solid" wrap-option="no-wrap" padding-start="2mm">
						<xsl:call-template name="Occurrence-attrs">
						  <xsl:with-param name="col" select="Col_4"/>
						  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_4"/></xsl:with-param>
						  <xsl:with-param name="cell_width">123mm</xsl:with-param>
						</xsl:call-template>
				  </fo:table-cell>
				  <fo:table-cell border-width='from-parent(border-width)' background-color="blue" border-style="solid" wrap-option="no-wrap" padding-start="2mm">
						<xsl:call-template name="Occurrence-attrs">
						  <xsl:with-param name="col" select="Col_5"/>
						  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_5"/></xsl:with-param>
						  <xsl:with-param name="cell_width">10mm</xsl:with-param>         
						</xsl:call-template>    
				  </fo:table-cell>
			  </xsl:when>
			  <xsl:otherwise-->
					<fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap" padding-start="2mm">
						<xsl:call-template name="Occurrence-attrs">
						  <xsl:with-param name="col" select="Col_4"/>
						  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_4"/></xsl:with-param>
						  <xsl:with-param name="cell_width">70mm</xsl:with-param>
						</xsl:call-template>
				  </fo:table-cell>
				  <fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap" padding-start="2mm">
						<xsl:call-template name="Occurrence-attrs">
						  <xsl:with-param name="col" select="Col_5"/>
						  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_5"/></xsl:with-param>
						  <xsl:with-param name="cell_width">63mm</xsl:with-param>         
						</xsl:call-template>    
				  </fo:table-cell>
			  <!--/xsl:otherwise>
		  </xsl:choose-->
          <!--fo:table-cell border-width='from-parent(border-width)' border-style="solid" wrap-option="no-wrap" padding-end="1.5mm"-->
		  <fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap">
                <xsl:call-template name="Occurrence-attrs">
                  <xsl:with-param name="col" select="Col_6"/>
                  <xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_6"/></xsl:with-param>
                  <xsl:with-param name="cell_width">10mm</xsl:with-param>
                </xsl:call-template>
          </fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="none" wrap-option="no-wrap" padding-start="1mm" font-size="3.48mm">
                <xsl:call-template name="Occurrence-attrs">
                  <xsl:with-param name="col" select="Col_7"/>
                  <!--xsl:with-param name="max_letter_count"><xsl:value-of select=" $max_letter_col_7"/></xsl:with-param-->
                  <!--xsl:with-param name="cell_width">22mm</xsl:with-param-->
                </xsl:call-template>
          </fo:table-cell>
       </fo:table-row> 
</xsl:template>



<xsl:template name="Occurrence-attrs">
  <xsl:param name="col"/>
  <xsl:param name="max_letter_count">2</xsl:param>
  <xsl:param name="cell_width"/>
  
  <xsl:variable name="font" select="@font"/>
  <xsl:variable name="unite" select="@unite"/>
  <xsl:variable name="mm">mm</xsl:variable>
  <!--xsl:variable name="cell_width_txt">  <xsl:value-of select="concat(substring($cell_width,0,string-length($cell_width)-1)-1.4,  $mm)"/> </xsl:variable-->

          <fo:block>
            <xsl:if test="contains($font,'underline')"> <xsl:attribute name="text-decoration">underline</xsl:attribute></xsl:if>
            <xsl:if test="contains($font,'bold')"> <xsl:attribute name="font-weight">bold</xsl:attribute></xsl:if>
			<xsl:if test="contains($font,'italic')"> <xsl:attribute name="font-style">italic</xsl:attribute></xsl:if>
			<xsl:if test="contains($col/@warning,'true')"> <xsl:attribute name="color">red</xsl:attribute></xsl:if>
            
			<xsl:if test="contains($unite,'true')">
				<xsl:attribute name="background-color">white</xsl:attribute>
				<xsl:attribute name="margin-top">0.15mm</xsl:attribute>
				<xsl:attribute name="padding-bottom">-0.05mm</xsl:attribute>
				<xsl:attribute name="padding-right">0.026cm</xsl:attribute>
				<xsl:attribute name="padding-left">-6.772cm</xsl:attribute>
			</xsl:if>
            
			<!--xsl:if test="contains($font,'bold')"> <xsl:attribute name="font-weight">bold</xsl:attribute></xsl:if-->
			
			<xsl:choose>
              <xsl:when test="$col/@align='center'"> <xsl:attribute name="text-align">center</xsl:attribute></xsl:when>
              <xsl:when test="$col/@align='right'"> <xsl:attribute name="text-align">right</xsl:attribute></xsl:when>
              <xsl:when test="$col/@align='left'"> <xsl:attribute name="text-align">left</xsl:attribute></xsl:when>
            </xsl:choose>
            <xsl:value-of select="$col"/>
          </fo:block>

</xsl:template>



<xsl:template name="Empty_table">
    <xsl:param name="count" />
              <fo:table table-layout="fixed" font-style="normal" width="100%" border-collapse="collapse">
                 <fo:table-column column-width="6mm"/>
                 <fo:table-column column-width="6mm"/>
                 <fo:table-column column-width="8mm"/>
                 <fo:table-column column-width="70mm"/>
                 <fo:table-column column-width="63mm"/>
                 <fo:table-column column-width="10mm"/>
                 <fo:table-column column-width="22mm"/>
              <fo:table-header>
                 <fo:table-row line-height="{$s_table_header_height}" height="{$s_table_header_height}">
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                 </fo:table-row>
              </fo:table-header>
              <fo:table-body line-height="{$s_table_body_height}" height="{$s_table_body_height}"  border-width="0.25mm 0.5mm 0.25mm 0.5mm">
                <xsl:call-template name="Empty_row">
                  <xsl:with-param name="count" select="$count"/>
                </xsl:call-template>
              </fo:table-body>
              </fo:table>
</xsl:template>

<xsl:template name="Empty_row">
    <xsl:param name="depth" select="1" />
    <xsl:param name="count" />
    <xsl:if test="$depth &lt;= $count">
        <fo:table-row  height='from-parent(height)' border-width='from-parent(border-width)'>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell> 
          <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
        </fo:table-row>
      <xsl:call-template name="Empty_row">
        <xsl:with-param name="depth" select="$depth + 1"/>
        <xsl:with-param name="count" select="$count"/>
      </xsl:call-template>
    </xsl:if>
</xsl:template>

<xsl:template name="Little_stamp">
    <xsl:param name="Oboznach"/>
    <xsl:param name="Izm"/>
    <xsl:param name="Dokum"/>
              <fo:table table-layout="fixed" width="100%" border-collapse="collapse" font-style="normal" line-height="4.45mm">
                <fo:table-column column-width="7mm"/>
                <fo:table-column column-width="10mm"/>
                <fo:table-column column-width="23mm"/>
                <fo:table-column column-width="15mm"/>
                <fo:table-column column-width="10mm"/>
                <fo:table-column column-width="110mm"/>
                <fo:table-column column-width="10mm"/>

                <fo:table-body font-size="3mm"  height="4.45mm">
                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-rows-spanned="3"><fo:block-container height="14mm" display-align="center"><fo:block text-align="center" font-size="6mm"><xsl:value-of select="$Oboznach"/></fo:block></fo:block-container></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-rows-spanned="3">

                     <fo:table table-layout="fixed" width="100%" border-collapse="collapse" line-height="1.5 * 4.45mm">
                         <fo:table-body>
                             <fo:table-row height="6.670mm">
                             <fo:table-cell border-width="0.0mm 0.0mm 0.5mm 0.0mm" border-style="solid"><fo:block text-align="center">Лист</fo:block></fo:table-cell>
                             </fo:table-row>
                             <fo:table-row height="6.670mm">
                             <fo:table-cell border-width="0.5mm 0.0mm 0.0mm 0.0mm" border-style="solid"><fo:block text-align="center"><fo:page-number/></fo:block></fo:table-cell>
                             </fo:table-row>
                         </fo:table-body>
                     </fo:table>

                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/><xsl:if test="not(contains($Izm,'00'))"><fo:block text-align="center"><xsl:value-of select="$Izm"/></fo:block></xsl:if></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><xsl:value-of select="$Dokum"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="3"  -->
                </fo:table-row>
                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Изм.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Лист</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">№ докум.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Подп.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Дата</fo:block></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="3"  -->
                </fo:table-row>
                </fo:table-body>
              </fo:table>
</xsl:template>


<xsl:template name="Gross_stamp">
    <xsl:param name="Zavod"/>
    <xsl:param name="Oboznach"/>
    <xsl:param name="Naimen"/>
    <xsl:param name="Prov"/>
    <xsl:param name="Razr"/>
	<xsl:param name="Norm"/>
    <xsl:param name="Utv"/>
    <xsl:param name="Izm"/>
    <xsl:param name="Dokum"/>
    <xsl:param name="UdList"/>
	<xsl:param name="Litera1"/>
	<xsl:param name="Litera2"/>
	<xsl:param name="Litera3"/>
	<xsl:param name="AddChecker"/>
	<xsl:param name="AddCheckerPost"/>
	<xsl:param name="AddCheckerDate"/>
	
	<xsl:param name="crtDate"/>
	<xsl:param name="chkDate"/>
	<xsl:param name="ctrlDate"/>
	<xsl:param name="aprDate"/>
	<xsl:param name="pageQty"/>
	

              <fo:table table-layout="fixed" width="100%" border-collapse="collapse" font-style="normal" line-height="(40.0mm div 8) - 0.5mm">
                <fo:table-column column-width="7mm"/>
                <fo:table-column column-width="10mm"/>
                <fo:table-column column-width="23mm"/>
                <fo:table-column column-width="15mm"/>
                <fo:table-column column-width="10mm"/>
                <fo:table-column column-width="70mm"/>
                <fo:table-column column-width="5mm"/>
                <fo:table-column column-width="5mm"/>
                <fo:table-column column-width="5mm"/>
                <fo:table-column column-width="15mm"/>
                <fo:table-column column-width="20mm"/>

                <fo:table-body font-size="3mm" height="(40.0mm div 8) - 0.5mm">

                <!--fo:table-row height="7.6mm">
                  <fo:table-cell border-width="0.5mm" border-style="invisible" number-columns-spanned="5"><fo:block/></fo:table-cell> 
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="6"><fo:block-container height="7.0mm" display-align="center"><fo:block text-align="right" margin-right="2mm" font-size="4mm">Действует с: <xsl:value-of select="$UdList"/></fo:block></fo:block-container></fo:table-cell> 

                </fo:table-row-->

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="6" number-rows-spanned="3"><fo:block-container height="14mm" display-align="center"><fo:block text-align="center" font-size="6mm"><xsl:value-of select="$Oboznach"/></fo:block></fo:block-container></fo:table-cell> 
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/><xsl:if test="not(contains($Izm,'00'))"><fo:block text-align="center"><xsl:value-of select="$Izm"/></fo:block></xsl:if></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><xsl:value-of select="$Dokum"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Изм.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Лист</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">№ докум.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Подп.</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Дата</fo:block></fo:table-cell>
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="6" number-rows-spanned="3" -->
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="2"><fo:block>Разраб.</fo:block></fo:table-cell>
                  <!-- Spanned cell -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$Razr"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$crtDate"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-rows-spanned="5" > <xsl:call-template name="Text_container"><xsl:with-param name="Text" select="$Naimen"/><xsl:with-param name="Height">24mm</xsl:with-param><xsl:with-param name="Width">70mm</xsl:with-param></xsl:call-template></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="3"><fo:block text-align="center">Лит.</fo:block></fo:table-cell>
                  <!-- Spanned sells number-columns-spanned="3" -->
                  <!-- Spanned sells number-columns-spanned="3" -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Лист</fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">Листов</fo:block></fo:table-cell>
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="2"><fo:block>Пров.</fo:block></fo:table-cell>
                  <!-- Spanned cells number-columns-spanned="2" -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$Prov"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$chkDate"/></fo:block></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="5" -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><xsl:value-of select="$Litera1"/></fo:block></fo:table-cell>
				  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><xsl:value-of select="$Litera2"/></fo:block></fo:table-cell>
				  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><xsl:value-of select="$Litera3"/></fo:block></fo:table-cell>
                  <!--fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell-->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center">
                        <xsl:if test = "$pageQty>'1'">
                            <fo:page-number/>
                        </xsl:if>
                  </fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block text-align="center"><fo:page-number-citation ref-id="LastPage"/></fo:block></fo:table-cell>
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <!-- Доп. проверяющий -->
				  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="2"><fo:block><xsl:value-of select="$AddCheckerPost"/></fo:block></fo:table-cell>
                  <!-- Spanned cells number-columns-spanned="2" -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$AddChecker"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$AddCheckerDate"/></fo:block></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="5" -->
				  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="5" number-rows-spanned="3"><fo:block-container height="14mm" display-align="center"><fo:block text-align="center" font-size="4.5mm"><fo:external-graphic content-width="49mm" content-height="14mm" overflow="hidden" scaling="uniform" src="iconMVM.jpg"/>
                        </fo:block></fo:block-container></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- fo:table-cell border-width="0.5mm" border-style="solid"  number-columns-spanned="2"  number-rows-spanned="3" ><fo:block/></fo:table-cell -->
                  <!-- Spanned cells number-rows-spanned="2"  number-rows-spanned="3"  -->
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="2"><fo:block>Н.контр.</fo:block></fo:table-cell>
                  <!-- Spanned cells number-columns-spanned="2" -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$Norm"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$ctrlDate"/></fo:block></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="5" -->
                  <!-- Spanned cells number-rows-spanned="3" -->
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="2"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="2"  number-rows-spanned="3"  -->
                </fo:table-row>

                <fo:table-row height='from-parent(height)'>
                  <fo:table-cell border-width="0.5mm" border-style="solid" number-columns-spanned="2"><fo:block>Утв.</fo:block></fo:table-cell>
                  <!-- Spanned cell -->
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$Utv"/></fo:block></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block/></fo:table-cell>
                  <fo:table-cell border-width="0.5mm" border-style="solid"><fo:block font-size="2.5mm"><xsl:value-of select="$aprDate"/></fo:block></fo:table-cell>
                  <!-- Spanned cells number-rows-spanned="5" -->
                  <!-- Spanned cells number-rows-spanned="3" -->
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="3"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="2"  number-rows-spanned="3"  -->
                  <!-- Spanned cells number-rows-spanned="2"  number-rows-spanned="3"  -->
                </fo:table-row>

                </fo:table-body>
              </fo:table>
</xsl:template>

<!-- xsl:template name="Company_sign">
    <xsl:param name="width"/>
    <xsl:param name="height"/>
    <xsl:param name="unit">mm</xsl:param>

    <fo:instream-foreign-object>
        <svg:svg width="{$width}{$unit}"  height="{$height}{$unit}">
             <svg:circle cx="7.5mm" cy="7.5mm" r="5mm" fill="black"/>
             <svg:circle cx="7.5mm" cy="7.5mm" r="4.5mm" fill="white"/>
             <svg:circle cx="7.5mm" cy="7.5mm" r="3mm" fill="black"/>
        </svg:svg> 
    </fo:instream-foreign-object>
</xsl:template  -->

<xsl:template name="Add_minor_stamp">
    <xsl:param name="PervPrimen"/>

    <fo:table table-layout="fixed" font-style="normal" width="100%" border-collapse="collapse">
      <fo:table-column column-width="57mm"/>
      <fo:table-column column-width="57mm"/>
              <fo:table-body  border-width="0.5mm 0.5mm 0.5mm 0.5mm">
                 <fo:table-row  height="4.5mm" border-width="0.5mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Справ. №</fo:block></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Перв. примен.</fo:block></fo:table-cell>
                 </fo:table-row>
                 <fo:table-row  height="6.5mm" border-width="0.5mm">
                   <fo:table-cell wrap-option="no-wrap" border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell wrap-option="no-wrap" border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" font-size="5.0mm" margin-top="0.25mm" margin-bottom="0.25mm"><xsl:value-of select="$PervPrimen"/></fo:block></fo:table-cell>
                 </fo:table-row>
              </fo:table-body>
    </fo:table>
</xsl:template>


<xsl:template name="Add_center_stamp">
    <xsl:param name="prjName"/>
	<xsl:param name="spCode"/>
	
    <!--fo:table background-color="yellow" table-layout="fixed" font-style="italic" width="100%" border-collapse="collapse">
      <fo:table-column column-width="12mm"/>
      <fo:table-column column-width="12mm"/>
              <fo:table-body  border-width="0.5mm 0.5mm 0.5mm 0.5mm">
                 <fo:table-row  height="4.5mm" border-width="0mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm"><xsl:value-of select="$prjName"/></fo:block></fo:table-cell>
                 </fo:table-row>
                 <fo:table-row  height="6.5mm" border-width="0mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" font-size="4mm" margin-top="0.75mm" margin-bottom="0.75mm"><xsl:value-of select="$spCode"/></fo:block></fo:table-cell>
                 </fo:table-row>
              </fo:table-body>
    </fo:table-->
	<fo:table table-layout="fixed" font-style="italic" width="100%" border-collapse="collapse">
      <fo:table-column column-width="28mm"/>
              <fo:table-body >
                 <fo:table-row  height="4.5mm" border-width="0mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block margin-top="1mm" text-align="center" font-size="3mm"><fo:inline font-style="italic"><xsl:value-of select="$prjName"/></fo:inline></fo:block></fo:table-cell>
                 </fo:table-row>
                 <fo:table-row  height="6.5mm" border-width="0mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block margin-top="2.2mm" text-align="center" font-size="3mm"><fo:inline font-style="italic"><xsl:value-of select="$spCode"/></fo:inline></fo:block></fo:table-cell>
                 </fo:table-row>
              </fo:table-body>
    </fo:table>
</xsl:template>


<xsl:template name="Add_main_stamp_gost_2104_68">
    <fo:table table-layout="fixed" font-style="normal" width="100%" border-collapse="collapse">
      <fo:table-column column-width="25mm"/>
      <fo:table-column column-width="35mm"/>
      <fo:table-column column-width="25mm"/>
      <fo:table-column column-width="25mm"/>
      <fo:table-column column-width="35mm"/>
              <fo:table-body  border-width="0.5mm 0.5mm 0.5mm 0.5mm">
                 <fo:table-row  height="4.5mm" border-width="0.5mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Инв. № подл.</fo:block></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Подп. и дата</fo:block></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Взам. инв. №</fo:block></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Инв. № дубл.</fo:block></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block text-align="center" margin-top="0.5mm" font-size="3mm">Подп. и дата</fo:block></fo:table-cell>
                 </fo:table-row>
                 <fo:table-row  height="6.5mm" border-width="0.5mm">
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                   <fo:table-cell border-width='from-parent(border-width)' border-style="solid"><fo:block/></fo:table-cell>
                 </fo:table-row>
              </fo:table-body>
    </fo:table>
</xsl:template>




<xsl:template name="Text_container">
    <xsl:param name="Height"/>
    <xsl:param name="Width"/>
    <xsl:param name="Text"/>

    <xsl:variable name="mm">mm</xsl:variable>
    <xsl:variable name="text_length"> <xsl:value-of select="string-length($Text)"/></xsl:variable>
    <xsl:variable name="h"> <xsl:value-of select="substring($Height,0,string-length($Height)-1)"/></xsl:variable>
    <xsl:variable name="w"><xsl:value-of select="substring($Width,0,string-length($Width)-1)"/></xsl:variable>
    <xsl:variable name="lh">
        <xsl:choose>
          <xsl:when test="not(contains($Text, ' '))">
              <xsl:value-of select="$w div (0.8 * $text_length)"/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:call-template name="sqrt"><xsl:with-param name="number" select="($w * $h) div (1.15 * $text_length)"/></xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="hh"><xsl:value-of select="(1.3 * $lh)"/></xsl:variable>
    <xsl:variable name="lh_norm">
        <xsl:choose>
          <xsl:when test="$lh &gt; 6">
              <xsl:value-of select="6"/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:value-of select="$lh"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

        <fo:block-container height="{$Height}" line-height="{$hh}{$mm}" display-align="center">
                  <fo:block text-align="center" font-size="{$lh_norm}{$mm}"><xsl:value-of select="$Text"/></fo:block>
         </fo:block-container>
</xsl:template>


<xsl:template name="sqrt">
  <xsl:param name="number" select="0"/>
  <xsl:param name="try" select="1"/>
  <xsl:param name="iter" select="1"/>
  <xsl:param name="maxiter" select="20"/>

  <xsl:choose>
    <xsl:when test="$try * $try = $number or $iter > $maxiter">
        <xsl:value-of select="$try"/>
    </xsl:when>
    <xsl:otherwise>
        <xsl:call-template name="sqrt">
        <xsl:with-param name="number" select="$number"/>
        <xsl:with-param name="try" select="$try - (($try * $try - $number) div (2 * $try))"/>
        <xsl:with-param name="iter" select="$iter + 1"/>
        <xsl:with-param name="maxiter" select="$maxiter"/>
        </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>

