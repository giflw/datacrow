				<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
				
<t:inputText readonly="#{field.readonly}" value="#{field.value}" rendered="#{field.textfield}" styleClass="textfield" />
    <t:selectBooleanCheckbox readonly="#{field.readonly}" value="#{field.value}" rendered="#{field.checkbox}" immediate="true" styleClass="checkbox" />
<t:inputTextarea rows="10" readonly="#{field.readonly}" value="#{field.value}" rendered="#{field.longTextfield}" />

<t:panelGroup rendered="#{field.multiRelate}">
	<t:inputTextarea rows="1" value="#{field.value}" readonly="true" styleClass="relationfield" />
	<t:commandLink action="#{itemRelate.open}" rendered="#{not webObject.new}">
		<t:outputText value="edit"  />
		<f:param value="#{field.index}" name="fieldIdx" />
		<f:param value="#{item.child}" name="isChild" />
	</t:commandLink>
</t:panelGroup>

<t:inputCalendar rendered="#{field.date}" 
				 monthYearRowClass="yearMonthHeader" 
				 weekRowClass="weekHeader" 
				 popupButtonStyleClass="datebutton"
    			 currentDayCellClass="currentDayCell" value="#{field.value}" 
    			 renderAsPopup="true"
    			 popupTodayString="today" 
    			 popupWeekString="week" 
    			 helpText="MM/DD/YYYY" 
    			 readonly="#{field.readonly}"
    			 style="width:275px;"/>

<t:selectOneMenu readonly="#{field.readonly}" value="#{field.value}" rendered="#{field.dropDown}" immediate="true" styleClass="textfield">
    <f:selectItem itemValue="" itemLabel="" />
    <t:selectItems value="#{field.references}" var="reference" itemLabel="#{reference.label}" itemValue="#{reference.id}" />
</t:selectOneMenu>