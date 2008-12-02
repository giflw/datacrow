<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<t:aliasBeansScope>
    <t:aliasBean alias="#{item}" value="#{webChildObject}" />
    <t:aliasBean alias="#{itemBean}" value="#{childDetails}" />
    <f:subview id="childDetails">
        <jsp:include page="form.jsp"/>
    </f:subview>
</t:aliasBeansScope>