<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">

	<h:form>
	   <t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
	        <t:navigationMenuItems id="navitems" value="#{itemImage.menuItems}" />
	    </t:jscookMenu>
	</h:form>

	<h:form id="imageForm" rendered="#{security.loggedIn}" enctype="multipart/form-data">


		<t:panelGroup rendered="#{itemImage.allowUpload}">
			<t:outputText value="<h1>" escape="false" />
			<t:outputText value="#{image.name}" />
			<t:outputText value="</h1>" escape="false" />
		
			<t:outputText value="<br>" escape="false" />
	
	        <h:outputText value="Upload image" />
	        <t:inputFileUpload id="fileupload"
	                           accept="image/*"
	                           value="#{itemImage.upFile}"
	                           storage="file"
	                           required="true"
	                           maxlength="200000"
	                           rendered="#{itemImage.allowUpload}" />
	
	        <h:message for="fileupload" showDetail="true" />
	        <h:outputText value="&nbsp;" escape="false"   />
	        <h:commandButton value="Load" action="#{itemImage.upload}" styleClass="button" />
	    </t:panelGroup>

		<t:panelGrid rendered="#{not empty image.filename}">
			<t:column>
				<t:graphicImage url="#{image.filename}" />
			</t:column>
		</t:panelGrid>
	    
	</h:form>
	
</h:panelGroup>