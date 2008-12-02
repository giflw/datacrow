<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

    <h:form rendered="#{webObjects.module != 0 and security.loggedIn}">

        <t:panelGroup styleClass="divnavmenu">
            <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
                <t:navigationMenuItems id="navitems" value="#{itemSearch.menuItems}" />
            </t:jscookMenu>
        </t:panelGroup>

        <t:outputText value="<h1>#{webObjects.name}</h1>" escape="false" />

        <t:dataTable id="data"
                     styleClass="datatable"
                     headerClass="datatable_header"
                     footerClass="datatable_header"
                     rowClasses="datatable_row1,datatable_row2"
                     columnClasses="datatable_column,*"
                     rowOnMouseOver="this.style.backgroundColor='#FFFFCC'"
                     rowOnMouseOut="this.style.backgroundColor='#FFFFE5'"
                     rowOnClick="this.style.backgroundColor='#FFED7A'"
                     rowOnDblClick="this.style.backgroundColor='#FFED7A'"
                     var="row"
                     value="#{webObjects.data}"
                     preserveDataModel="false"
                     rows="10"
                     sortColumn="#{webObjects.sort}"
                     sortAscending="#{webObjects.ascending}"
                     sortable="true"
                     preserveSort="true"
                     cellspacing="0"
                     cellpadding="0">
                     
            <t:columns id="columns" value="#{webObjects.columnHeaders}" var="columnHeader" style="width:#{webObjects.columnWidth}px">
                <f:facet name="header">
                    <t:commandSortHeader columnName="#{columnHeader.label}" arrow="false" immediate="false">
                        <f:facet name="ascending">
                            <t:graphicImage value="images/ascending-arrow.gif" rendered="true" border="0"/>
                        </f:facet>
                        <f:facet name="descending">
                            <t:graphicImage value="images/descending-arrow.gif" rendered="true" border="0"/>
                        </f:facet>
                        
                        <h:outputText value="#{columnHeader.label}" />
                    </t:commandSortHeader>
                </f:facet>

                <h:commandLink rendered="#{not empty webObjects.columnValue and webObjects.linkToDetails}" action="#{itemDetails.open}">
                    <h:outputText value="#{webObjects.columnValue}" />
                </h:commandLink>

                <h:outputLink rendered="#{not empty webObjects.columnValue and webObjects.url}" value="#{webObjects.columnValue}" target="new">
                    <h:outputText rendered="#{not empty webObjects.columnValue}" value="link"/>
                </h:outputLink>

                <h:outputLink rendered="#{not empty webObjects.columnValue and webObjects.file}" value="file://#{webObjects.columnValue}" target="new">
                    <h:outputText rendered="#{not empty webObjects.columnValue}" value="link"/>
                </h:outputLink>

                <h:outputText rendered="#{not empty webObjects.columnValue and webObjects.text}" value="#{webObjects.columnValue}" />
                <t:graphicImage rendered="#{not empty webObjects.columnValue and webObjects.image}" url="#{webObjects.columnValue}" />

                <h:outputText rendered="#{empty webObjects.columnValue}" value="&nbsp;" escape="false" />

            </t:columns>

            <t:column styleClass="datatable_column">
                <h:commandLink action="#{itemDetails.open}">
                    <t:graphicImage url="images/open.png" />
                </h:commandLink>
            </t:column>

        </t:dataTable>

        <h:panelGrid rendered="#{webObjects.module != 0}">
            <t:dataScroller id="scroller"
                            for="data"
                            fastStep="10"
                            pageCountVar="pageCount"
                            pageIndexVar="pageIndex"
                            styleClass="scroller"
                            paginator="true"
                            paginatorMaxPages="10"
                            paginatorTableClass="paginator"
                            immediate="false"
                            paginatorActiveColumnStyle="font-weight:bold;">

                <f:facet name="first" >
                    <t:graphicImage url="images/arrow-first.gif" border="1" />
                </f:facet>

                <f:facet name="last">
                    <t:graphicImage url="images/arrow-last.gif" border="1" />
                </f:facet>

                <f:facet name="previous">
                    <t:graphicImage url="images/arrow-previous.gif" border="1" />
                </f:facet>

                <f:facet name="next">
                    <t:graphicImage url="images/arrow-next.gif" border="1" />
                </f:facet>

                <f:facet name="fastforward">
                    <t:graphicImage url="images/arrow-ff.gif" border="1" />
                </f:facet>

                <f:facet name="fastrewind">
                    <t:graphicImage url="images/arrow-fr.gif" border="1" />
                </f:facet>

            </t:dataScroller>
        </h:panelGrid>
    </h:form>
</h:panelGroup>
