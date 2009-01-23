<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

    <h:form rendered="#{security.loggedIn}">
    
        <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
            <t:navigationMenuItems id="navitems" value="#{itemRelate.menuItems}" />
        </t:jscookMenu>

        <t:outputText value="<h1>#{resources.map['lblSelectRelatedItems']}</h1>" escape="false" />
        
        <t:selectManyCheckbox id="selone_menu_extras" value="#{references.keys}" layout="pageDirection" styleClass="selectManyCheckbox">
            <f:selectItems value="#{references.listItems}" />
        </t:selectManyCheckbox>
        
    </h:form>

</h:panelGroup>        