<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

    <h:form rendered="#{security.loggedIn}">
        
        <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
            <t:navigationMenuItems id="navitems" value="#{itemBean.menuItems}" />
        </t:jscookMenu>
    
        <t:outputText value="<h1>#{resources.map['lblInformation']}</h1>" escape="false" rendered="#{item.tab == 1}" />
        
        <t:dataTable rendered="#{item.tab == 1}" value="#{item.fields}" 
                     var="field" columnClasses="columnLabels,columnFields">
                     
            <t:column>
                <h:outputText value="<b>" escape="false" rendered="#{field.required}" />
                <h:outputText value="#{field.label}" />
                <h:outputText value="</b>" escape="false" rendered="#{field.required}" />
            </t:column>                
            
            <t:column>
                <jsp:include page="input.jsp" />
            </t:column>
            
        </t:dataTable>
        
        <t:outputText value="<h1>#{resources.map['lblTechnicalInfo']}</h1>" escape="false" rendered="#{item.tab == 2}" />
        
        <t:dataTable value="#{item.technicalFields}" rendered="#{item.tab == 2}" 
                     var="field" columnClasses="columnLabels,columnFields">
                     
            <t:column>
                <h:outputText value="<b>" escape="false" rendered="#{field.required}" />
                <h:outputText value="#{field.label}" />
                <h:outputText value="</b>" escape="false" rendered="#{field.required}" />
            </t:column>                
            
            <t:column>
                <jsp:include page="input.jsp" />
            </t:column>
                
        </t:dataTable>
        

        <t:outputText value="<h1>#{item.childrenLabel}</h1>" escape="false" rendered="#{item.tab == 3}" />
        
        <t:dataTable id="data"
         styleClass="datatable"
         headerClass="datatable_header"
         footerClass="datatable_header"
         rowClasses="datatable_row1,datatable_row2"
         columnClasses="datatable_children_column,*"
         rowOnMouseOver="this.style.backgroundColor='#FFFFCC'"
         rowOnMouseOut="this.style.backgroundColor='#FFFFE5'"
         rowOnClick="this.style.backgroundColor='#FFED7A'"
         rowOnDblClick="this.style.backgroundColor='#FFED7A'"
         var="row"
         value="#{item.children}"
         preserveDataModel="false"
         rows="100"
         cellspacing="0"
         cellpadding="0"
         rendered="#{item.tab == 3}">
                     
            <t:columns id="columns" value="#{item.childrenColumnHeaders}" var="hdr" style="width:#{item.childrenColumnWidth}px;">
            
                <f:facet name="header">
                    <t:commandSortHeader columnName="#{hdr.label}" arrow="false" immediate="false">
                        <f:facet name="ascending">
                            <t:graphicImage value="images/ascending-arrow.gif" rendered="true" border="0"/>
                        </f:facet>
                        <f:facet name="descending">
                            <t:graphicImage value="images/descending-arrow.gif" rendered="true" border="0"/>
                        </f:facet>
                        
                        <h:outputText value="#{hdr.label}" />
                    </t:commandSortHeader>
                </f:facet>
    
                <h:commandLink rendered="#{not empty item.childrenColumnValue and item.linkToChildDetails}" action="#{childDetails.open}">
                    <h:outputText value="#{item.childrenColumnValue}" />
                </h:commandLink>
    
                <h:outputText rendered="#{not empty item.childrenColumnValue and not item.linkToChildDetails}" value="#{item.childrenColumnValue}" />

                <h:outputText rendered="#{empty item.childrenColumnValue}" value="&nbsp;" escape="false" />
                
            </t:columns>

            <t:column styleClass="datatable_children_column">
                <h:commandLink action="#{childDetails.open}">
                    <t:graphicImage url="images/open.png" />
                </h:commandLink>
            </t:column>
            
        </t:dataTable>
        
        
        <t:outputText value="<h1>#{resources.map['lblPictures']}</h1>" escape="false" rendered="#{item.tab == 4}" />
        
        <t:dataTable value="#{item.pictureFields}"
                     rendered="#{item.tab == 4}" 
                     var="field" 
                     newspaperColumns="2"
                     newspaperOrientation="horizontal">
                     
            <t:column rendered="#{not empty field.value}">
                <h:outputText value="#{field.label}" />
                <h:outputText value="<br>" escape="false" />
                <t:commandLink action="#{itemImage.open}">
                    <t:graphicImage url="#{field.value}" />
                    <f:param value="#{field.index}" name="fieldIdx" />
                </t:commandLink>
            </t:column>
            
        </t:dataTable>
        
        <t:dataTable value="#{item.pictureFields}"
                     rendered="#{item.tab == 4 and itemImage.allowUpload}" 
                     var="field"
                     columnClasses="columnLabels,columnFields">
                     
            <t:column>
                <t:commandLink action="#{itemImage.open}">
                    <h:outputText value="Edit #{field.label}" />
                    <f:param value="#{field.index}" name="fieldIdx" />
                    <f:param value="true" name="allowUpload" />
                   </t:commandLink>
            </t:column>                
        </t:dataTable>
    
        <h:outputText value="<br>" escape="false" />
    
        <h:commandButton styleClass="button" value="#{resources.map['lblCreateNew']}" action="#{childDetails.create}" rendered="#{item.tab == 3}" />
        
    </h:form>
</h:panelGroup>