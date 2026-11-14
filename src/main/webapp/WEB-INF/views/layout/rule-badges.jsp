<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Renders a rule expression (param "expr") as compact inline badges with muted AND / OR connectors. --%>
<c:set var="spaced" value="${fn:replace(fn:replace(param.expr, '(', ' ( '), ')', ' ) ')}" />
<div class="cond-badges">
    <c:forTokens items="${spaced}" delims=" " var="tok">
        <c:choose>
            <c:when test="${tok == 'AND' || tok == 'OR' || tok == 'NOT'}"><span class="cond-op">${tok}</span></c:when>
            <c:when test="${tok == '(' || tok == ')'}"><span class="cond-paren">${tok}</span></c:when>
            <c:when test="${tok == 'HIGH_NOISE'}"><span class="cond-badge">High Noise</span></c:when>
            <c:when test="${tok == 'RESTRICTED_HOURS'}"><span class="cond-badge">Restricted Hours</span></c:when>
            <c:when test="${tok == 'ILLEGAL_PARKING'}"><span class="cond-badge">Unauthorized Parking</span></c:when>
            <c:when test="${tok == 'HAZARDOUS_SMOKE'}"><span class="cond-badge">Smoke Alert</span></c:when>
            <c:when test="${tok == 'EMERGENCY_ZONE'}"><span class="cond-badge">Emergency Zone</span></c:when>
            <c:otherwise><span class="cond-badge"><c:out value="${fn:replace(tok, '_', ' ')}" /></span></c:otherwise>
        </c:choose>
    </c:forTokens>
</div>

