<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

    <h:form>
       <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
            <t:navigationMenuItems id="navitems" value="#{itemBean.menuItems}" />
        </t:jscookMenu>
    </h:form>

    <h:form rendered="#{images.count > 1}">
	    <t:dataTable value="#{images.images}"
	                 var="image" 
	                 newspaperColumns="8"
	                 newspaperOrientation="horizontal">
	
	        <t:column>
	            <t:commandLink action="#{itemBean.load}">
	                <t:graphicImage url="#{image.filenameScaled}" />
	                <f:param value="#{image.fieldIdx}" name="fieldIdx" />
	            </t:commandLink>
	        </t:column>
	    </t:dataTable>
    </h:form>

    <t:panelGroup rendered="#{images.current != null}">
        <t:graphicImage url="#{images.current.filename}" />
    </t:panelGroup>
    
</h:panelGroup>