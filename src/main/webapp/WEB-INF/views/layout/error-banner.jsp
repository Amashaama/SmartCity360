<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Inline error toast. Renders only when a servlet sets the "errorMessage" request attribute. --%>
<c:if test="${errorMessage != null}">
    <div class="error-toast" role="alert">
        <i class="bi bi-exclamation-circle-fill error-toast-icon"></i>
        <div class="error-toast-text"><c:out value="${errorMessage}" /></div>
    </div>
</c:if>

