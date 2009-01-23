<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>

<h:form>

    <t:dataTable rendered="#{security.loggedIn}"
                 value="#{modules.modules}" 
                  rowOnMouseOver="this.style.backgroundColor='#FFFFCC'"
                 rowOnMouseOut="this.style.backgroundColor='White'"
                 var="module" 
                 columnClasses="module_column,module_column"
                 rowClasses="module_row1,module_row2"
                 styleClass="menupart"
                 renderedIfEmpty="false"
                 cellpadding="0"
                 cellspacing="0">

        <t:column style="text-align:right;">
            <t:graphicImage value="#{module.icon32}" rendered="true" border="0"/>
        </t:column>    

        <t:column>
            <h:commandLink action="#{itemSearch.search}">
                <h:outputText value="#{module.label}" />
                <f:param name="moduleId" value="#{module.index}" />
            </h:commandLink>
        </t:column>
    </t:dataTable>

    <h:outputText value="<br>" escape="false" rendered="#{webObjects.module != 0}" />

    <t:dataTable rendered="#{webObjects.module != 0 and security.loggedIn}"
                 renderedIfEmpty="false" 
                 value="#{webObjects.filterFields}" 
                 var="field" 
                 styleClass="menupart">

        <t:column>
            <h:outputText value="#{field.label}"/>
        </t:column>                

        <t:column>
            <t:inputText value="#{field.value}" rendered="#{field.textfield}" styleClass="searchtextfield" />
            <t:selectBooleanCheckbox value="#{field.value}" rendered="#{field.checkbox}" immediate="true" styleClass="checkbox" />

            <t:selectOneMenu value="#{field.value}" rendered="#{field.dropDown}" immediate="true" styleClass="searchtextfield">
                <f:selectItem itemValue="" itemLabel="" />
                <t:selectItems value="#{field.references}" var="reference" itemLabel="#{reference.label}" itemValue="#{reference.id}" />
            </t:selectOneMenu>
        </t:column>
    </t:dataTable>

    <h:outputText value="<br>" escape="false" rendered="#{webObjects.module != 0}" />

    <h:commandButton styleClass="button" 
                     rendered="#{webObjects.module != 0 and security.loggedIn}" 
                     value="#{resources.map['lblSearch']}" 
                     action="#{itemSearch.search}"/>

    <h:outputText value="&nbsp;" escape="false" />

    <h:commandLink action="#{advancedFind.open}" rendered="#{webObjects.module != 0 and security.loggedIn}">
        <h:outputText value="#{resources.map['lblAdvanced']}" />
    </h:commandLink>

    <h:outputText value="<br><br>" escape="false" />

    <t:panelGrid rendered="#{security.loggedIn}" columns="1" styleClass="menupart">                     
        <t:column width="10%" style="border-bottom:1px;border-bottom:solid;border-color:Black;"
                  rendered="#{security.loggedIn}" >
            <h:outputText value="#{resources.map['lblWelcome']} #{security.username}! " />
            <t:commandLink action="#{security.logoff}">
                <h:outputText value="#{resources.map['lblLogoff']}" />
            </t:commandLink>
        </t:column>
    </t:panelGrid>
                     
</h:form>