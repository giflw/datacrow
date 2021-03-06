<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"
 %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;CHARSET=iso-8859-1" />
  <title>Data Crow Web Interface</title>
  <link rel="stylesheet" type="text/css" href="css/datacrow.css" />
</head>
<f:view>
<body>
	<div class="divmessages">
		<h:panelGrid styleClass="menupart" rendered="#{not empty facesContext.maximumSeverity}">
			<h:column>
				<h:messages />
			</h:column>
		</h:panelGrid>
	</div>

  	<div id="divmenu">
    	<f:subview id="menu">
      		<tiles:insert attribute="menu" flush="false" />
    	</f:subview>
  	</div>
  	<div id="level0">
    	<div id="level1">
       		<div id="divheader">
          		<f:subview id="header">
            		<tiles:insert attribute="header" flush="false"/>
          		</f:subview>
       		</div>
       		<div id="divcontent">
         		<f:subview id="content">
            		<tiles:insert attribute="body" flush="false"/>
         		</f:subview>
      		</div>
    	</div>
	</div>
</body>
</f:view>
</html>