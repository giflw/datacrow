<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<t:aliasBeansScope>
	<t:aliasBean alias="#{item}" value="#{webObject}" />
	<t:aliasBean alias="#{itemBean}" value="#{itemDetails}" />
	<f:subview id="itemDetails">
   		<jsp:include page="form.jsp"/>
	</f:subview>
</t:aliasBeansScope>