<%@ page isELIgnored="false"%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/fn.tld" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="sm"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ include file="../parameters.jsp"%>

<fmt:setLocale value="${lang}"/>
<fmt:setBundle basename="com.oosphere.silverpeasmobile.multilang.silverpeasmobile"/>

<!DOCTYPE html>
<html>
	<head>
	<title><fmt:message key="pageTitle"/></title>
	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0b1/jquery.mobile-1.0b1.min.css" />
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.1.min.js"></script>
	<script type="text/javascript" src="http://code.jquery.com/mobile/1.0b1/jquery.mobile-1.0b1.min.js"></script>

	<script type="text/javascript">
		function selectComponent(spaceId, componentId) {
			var form = document.forms["form"];
			form.elements["spaceId"].value = spaceId;
			form.elements["componentId"].value = componentId;
			form.submit();
		}
	</script>
</head>

<body>

<div  data-role="page">
	<div  data-role="header">Services</div>
	<div  data-role="content">
<nav>


				<ul data-role="listview" data-inset="true" data-theme="c" data-dividertheme="b">
	                        <c:forEach items="${spaces}" var="space">
					<li data-role="list-divider">${space.name}</li>
					<c:forEach items="${space.components}" var="component">
                                            <li><a href="javascript:selectComponent('${space.id}', '${component.id}')">${component.name}</a></li>
                                        </c:forEach>
                                </c:forEach>

				</ul>

			</nav>
                <form name="form" action="${pageContext.request.contextPath}/index.html" method="post">
                        <input type="hidden" name="action" value="kmelia"/>
                        <input type="hidden" name="subAction" value="component"/>
                        <input type="hidden" name="userId" value="${userId}"/>
                        <input type="hidden" name="spaceId" value=""/>
                        <input type="hidden" name="componentId" value=""/>
                </form>

	</div>
	<div  data-role="footer">Copyright Silverpeas 1999-2011</div>
</div>

</body>
</html>
