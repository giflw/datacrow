<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<h:panelGroup id="body">
	
	<h:form id="frmLogin">
		<t:panelGrid columns="2" columnClasses="columnLabelsLogin,columnFields">
			<t:column><t:outputText value="&nbsp;" escape="false" /></t:column>
			<t:column><t:outputText value="&nbsp;" escape="false" /></t:column>
			
			<t:column width="">
				<t:outputText value="Username" />
			</t:column>
			<t:column>
				<t:inputText id="fldUsername" value="#{user.username}" styleClass="searchtextfield" />
				<h:message for="fldUsername" id="fldUsernameError" />
			</t:column>
			<t:column>
				<t:outputText value="Password" />
			</t:column>
			<t:column>
				<t:inputSecret id="fldPassword" value="#{user.password}" styleClass="searchtextfield" />
			</t:column>
		</t:panelGrid>
	
		<h:commandButton styleClass="button" value="Login" action="#{security.login}"/>
	
	</h:form>
	
</h:panelGroup>