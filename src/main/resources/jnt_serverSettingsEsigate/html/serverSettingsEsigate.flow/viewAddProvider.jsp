<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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

<form:form modelAttribute="newProvider" cssClass="form-horizontal">
    <div class="control-group">
        <label class="control-label" for="providerKey"><fmt:message
                key="serverSettings.esigateSettings.providerKey"/></label>

        <div class="controls">
            <form:input path="providerKey" cssClass="form-control" placeholder="myapp"/>
            <form:errors path="providerKey" cssClass="help-inline btn-danger"/>
        </div>

    </div>

    <div class="control-group">
        <label class="control-label" for="remoteUrlBase"><fmt:message key="serverSettings.esigateSettings.remoteUrlBase"/></label>

        <div class="controls">
            <form:input path="remoteUrlBase" placeholder="http://localhost:8080/myapp"/>
            <form:errors path="remoteUrlBase" cssClass="help-inline btn-danger"/>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="defaultPageInclude"><fmt:message
                key="serverSettings.esigateSettings.defaultPageInclude"/></label>

        <div class="controls">
            <form:input path="defaultPageInclude" placeholder="/sites/mySite/home.html"/>
            <form:errors path="defaultPageInclude" cssClass="help-inline btn-danger"/>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="defaultFragmentReplace"><fmt:message
                key="serverSettings.esigateSettings.defaultFragmentReplace"/></label>

        <div class="controls">
            <form:input path="defaultFragmentReplace" placeholder="fragment"/>
            <form:errors path="defaultFragmentReplace" cssClass="help-inline btn-danger"/>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="crossContextApplication"><fmt:message
                key="serverSettings.esigateSettings.crossContext"/></label>

        <div class="controls">
            <form:input path="context" placeholder="/myapp"/>
            <form:errors path="context" cssClass="help-inline btn-danger"/>
        </div>
    </div>

    <button class="btn btn-primary" type="submit" name="_eventId_submit">
        <i class="icon-white"></i>
        &nbsp;<fmt:message key="serverSettings.esigateSettings.addProvider"/>
    </button>

</form:form>
