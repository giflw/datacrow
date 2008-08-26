<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<t:panelGroup id="body">

	<h:form rendered="#{security.loggedIn}">

		<t:jscookMenu id="menu" layout="hbr" theme="ThemeOffice" styleLocation="css/jscookmenu">
        	<t:navigationMenuItems id="navitems" value="#{advancedFind.menuItems}" />
    	</t:jscookMenu>
		
		<h:outputText value="<h1>Advanced Find</h1>" escape="false" />
		
		<t:panelGrid styleClass="filterentriespanel"> 
			<t:dataTable var="entry"
						 value="#{advancedFilter.entries}"
	                     var="entry"
	                     styleClass="datatable"
		                 headerClass="datatable_header"
		                 footerClass="datatable_header"
		                 rowClasses="datatable_row1,datatable_row2"
		                 columnClasses="advancedfiltertable_column,*"
		                 rowOnMouseOver="this.style.backgroundColor='#FFFFCC'"
		                 rowOnMouseOut="this.style.backgroundColor='#FFFFE5'"
		                 rowOnClick="this.style.backgroundColor='#FFED7A'"
		                 rowOnDblClick="this.style.backgroundColor='#FFED7A'"
	                     styleClass="advancedfiltertable"
	                     cellspacing="0"
		                 cellpadding="0">
	                     
				<t:column>
					<h:outputText value="#{entry}" />
				</t:column>
	
				<t:column>
					<t:commandLink action="#{advancedFind.deleteEntry}">
						<h:graphicImage url="images/delete.png" /> 
						<f:param value="#{entry.ID}" name="index" />
					</t:commandLink>
					<t:commandLink action="#{advancedFind.editEntry}">
						<h:graphicImage url="images/edit.png" /> 
						<f:param value="#{entry.ID}" name="index" />
					</t:commandLink>
				</t:column>
			</t:dataTable>
		</t:panelGrid>


		<t:panelGrid columns="6">
			<t:column>
				<h:outputText value="And / or" />
			</t:column>

			<t:column>
				<h:outputText value="Module" />
			</t:column>	

			<t:column>
				<h:outputText value="Field" />
			</t:column>	

			<t:column>
				<h:outputText value="Operator" />
			</t:column>	

			<t:column>
				<h:outputText value="Value" rendered="#{advancedFilter.needsValue}"/>
			</t:column>	

			<t:column />
		
			<t:column>	
	             <t:selectOneMenu value="#{advancedFilter.andOr}" styleClass="filterfield">
	                 <t:selectItems value="#{advancedFilter.andOrList}" var="andOr" itemLabel="#{andOr}" itemValue="#{andOr}" />
	             </t:selectOneMenu>
			</t:column>		

			<t:column>	
	             <t:selectOneMenu value="#{advancedFilter.module}" valueChangeListener="#{advancedFilter.selectModule}" styleClass="filterfield" onchange="submit()" immediate="true">
	                 <t:selectItems value="#{advancedFilter.modules}" var="module" itemLabel="#{module.label}" itemValue="#{module.index}" />
	             </t:selectOneMenu>
			</t:column>		

			<t:column>	
	             <t:selectOneMenu value="#{advancedFilter.fieldIdx}" valueChangeListener="#{advancedFilter.selectField}" styleClass="filterfield" onchange="submit()" immediate="true">
	                 <t:selectItems value="#{advancedFilter.fields}" var="field" itemLabel="#{field.label}" itemValue="#{field.index}" />
	             </t:selectOneMenu>
			</t:column>		

			<t:column>	
	             <t:selectOneMenu value="#{advancedFilter.operator}" styleClass="filterfield">
	                 <t:selectItems value="#{advancedFilter.operators}" var="operator" itemLabel="#{operator.name}" itemValue="#{operator.index}" />
	             </t:selectOneMenu>
			</t:column>		
		
			<t:column>	
				<t:inputText value="#{advancedFilter.value}" rendered="#{advancedFilter.field.textfield}" styleClass="searchtextfield" />
	            <t:selectBooleanCheckbox value="#{advancedFilter.value}" rendered="#{advancedFilter.field.checkbox}" immediate="true" styleClass="checkbox" />
	          	<t:inputCalendar rendered="#{advancedFilter.field.date}" 
	          				   	 monthYearRowClass="yearMonthHeader" 
	          				 	 weekRowClass="weekHeader" 
	          				 	 popupButtonStyleClass="datebutton"
	              			 	 currentDayCellClass="currentDayCell" value="#{advancedFilter.value}" 
	              			 	 renderAsPopup="true"
	              			 	 popupTodayString="today" 
	              			 	 popupWeekString="week" 
	              			 	 helpText="MM/DD/YYYY" 
	              			 	 style="width:275px;" />
	             <t:selectOneMenu value="#{advancedFilter.value}" rendered="#{advancedFilter.field.dropDown}" immediate="true" styleClass="searchtextfield">
	                 <t:selectItems value="#{advancedFilter.field.references}" var="reference" itemLabel="#{reference.label}" itemValue="#{reference.id}" />
	             </t:selectOneMenu>
			</t:column>
			
			<t:commandLink action="#{advancedFind.addEntry}">
				<h:graphicImage url="images/add.png" /> 
			</t:commandLink>
		</t:panelGrid>
		
		<h:outputText value="<br>" escape="false" />
		
		<h:commandButton value="Search" action="#{advancedFind.search}" styleClass="button" />
		
	</h:form>
</t:panelGroup>	