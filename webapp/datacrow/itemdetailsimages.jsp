<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<t:aliasBeansScope>

    <t:aliasBean alias="#{itemBean}" value="#{itemDetailsImages}" />

    <f:subview id="images">
        <jsp:include page="images.jsp"/>
    </f:subview>

</t:aliasBeansScope>