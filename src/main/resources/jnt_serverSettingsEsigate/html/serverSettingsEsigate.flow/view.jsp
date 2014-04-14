<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="esigateSettings" type="org.jahia.modules.portalFactory.esigate.EsigateSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="searchCriteria" type="org.jahia.services.usermanager.SearchCriteria"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrapSwitch.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,bootstrapSwitch.css"/>
<template:addResources type="javascript" resources="codemirror.jahia.min.js"/>
<template:addResources type="css" resources="codemirror/codemirror.css"/>

<h2>
    Esigate settings
</h2>
<p>
<c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
    <c:if test="${message.severity eq 'ERROR'}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
</c:forEach>
</p>

<div class="box-1">
    <form class="form-horizontal" name="jahiaAdmin" action='${flowExecutionUrl}' method="post">
        <div class="control-group">
            <div class="controls">
                <label for="serviceEnabled">
                    <div class="switch" data-on="success" data-off="danger">
                        <input type="checkbox" name="serviceEnabled" id="serviceEnabled"<c:if test='${esigateSettings.serviceEnabled}'> checked="checked"</c:if>/>
                    </div>
                    <input type="hidden" name="_serviceEnabled"/>
                    &nbsp;Service status
                </label>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">Configuration &nbsp;:</label>
            <div class="controls ">
                <textarea id="config" name="config"><c:out value="${functions:unescapeJava(esigateSettings.config)}" escapeXml="true"/></textarea>
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <fmt:message var="i18nReloadConfirm" key="serverSettings.esigateSettings.reloadDefaultConfirm"/><c:set var="i18nReloadConfirm" value="${functions:escapeJavaScript(i18nReloadConfirm)}"/>
                <button class="btn btn-primary" type="submit" name="_eventId_submitSettings"><i class="icon-ok icon-white"></i>&nbsp;<fmt:message key="label.save"/></button>
                <button class="btn" type="submit" onclick="return confirm('${i18nReloadConfirm}');" name="_eventId_reloadDefault"><i class="icon-circle-arrow-down"></i>&nbsp;<fmt:message key="serverSettings.esigateSettings.reloadDefault"/></button>
            </div>
        </div>
    </form>
</div>

<script type="text/javascript">
    var configCodeMirror = CodeMirror.fromTextArea(document.getElementById("config"), {mode: "properties", lineNumbers: true, matchBrackets: true, readOnly: false});
    configCodeMirror.setSize("100%", "300px");
</script>