<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

    <h:form>
       <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
            <t:navigationMenuItems id="navitems" value="#{itemImages.menuItems}" />
        </t:jscookMenu>
    </h:form>

    
</h:panelGroup>