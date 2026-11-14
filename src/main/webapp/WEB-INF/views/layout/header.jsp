<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${pageTitle != null ? pageTitle : 'Dashboard'} - SmartCity360 Management Platform</title>

                <!-- Bootstrap 5 CSS -->
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
                <!-- Bootstrap Icons -->
                <link rel="stylesheet"
                    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
                <!-- Custom Application Styling -->
                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=5">
            </head>

            <body>
                <script>
                    (function () {
                        try {
                            if (window.innerWidth >= 992 && localStorage.getItem('sc360_sidebar_collapsed') === 'true') {
                                document.body.classList.add('sidebar-collapsed');
                            }
                        } catch (e) {}
                    })();
                </script>
                <div class="container-fluid p-0">
                    <div class="app-layout">
                        <!-- Mobile Sidebar Backdrop -->
                        <div class="sidebar-backdrop" id="sidebarBackdrop" onclick="closeMobileSidebar()"></div>

                        <!-- Sidebar Navigation -->
                        <aside class="sidebar" id="appSidebar">
                            <div class="brand">
                                <div class="brand-logo d-flex align-items-center gap-2">
                                    <i class="bi bi-buildings-fill text-primary"></i>
                                    <span class="brand-text">SmartCity 360</span>
                                </div>
                                <button type="button" class="sidebar-nav-toggle" id="sidebarToggleBtn"
                                    onclick="toggleSidebar()" title="Toggle Navigation Sidebar"
                                    aria-label="Toggle Navigation Sidebar">
                                    <i class="bi bi-list fs-5"></i>
                                </button>
                            </div>

                            <div class="sidebar-menu py-3">
                                <div class="px-4 py-2 text-primary text-uppercase small fw-bold nav-section-label"
                                    style="font-size: 0.72rem; letter-spacing: 0.05rem;">
                                    Platform
                                </div>
                                <a href="${pageContext.request.contextPath}/"
                                    class="nav-link ${activePage == 'home' ? 'active' : ''}"
                                    title="Dashboard">
                                    <i class="bi bi-speedometer2"></i>
                                    <span>Dashboard</span>
                                </a>

                                <div class="px-4 pt-3 pb-2 text-uppercase text-primary small fw-bold nav-section-label"
                                    style="font-size: 0.72rem; letter-spacing: 0.05rem;">
                                    Operations
                                </div>
                                <a href="${pageContext.request.contextPath}/decorator"
                                    class="nav-link ${activePage == 'decorator' ? 'active' : ''}"
                                    title="Meter Telemetry">
                                    <i class="bi bi-shield-lock"></i>
                                    <span>Meter Telemetry</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/composite"
                                    class="nav-link ${activePage == 'composite' ? 'active' : ''}"
                                    title="Zone Grid Control">
                                    <i class="bi bi-diagram-3"></i>
                                    <span>Zone Grid Control</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/chain"
                                    class="nav-link ${activePage == 'chain' ? 'active' : ''}"
                                    title="Citizen Complaints">
                                    <i class="bi bi-signpost-split"></i>
                                    <span>Citizen Complaints</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/flyweight"
                                    class="nav-link ${activePage == 'flyweight' ? 'active' : ''}"
                                    title="Street Lighting">
                                    <i class="bi bi-lightbulb"></i>
                                    <span>Street Lighting</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/interpreter"
                                    class="nav-link ${activePage == 'interpreter' ? 'active' : ''}"
                                    title="Policy Rules">
                                    <i class="bi bi-code-square"></i>
                                    <span>Policy Rules</span>
                                </a>
                            </div>

                            <!-- Sidebar Bottom User & Logout Section -->
                            <div class="sidebar-footer">
                                <c:if test="${not empty sessionScope.loggedInUser}">
                                    <div class="sidebar-user-card">
                                        <div class="sidebar-user-avatar">
                                            <i class="bi bi-person-check-fill"></i>
                                        </div>
                                        <div class="sidebar-user-details">
                                            <div class="sidebar-user-name"><c:out value="${sessionScope.loggedInUser.fullName}" /></div>
                                            <div class="sidebar-user-role"><c:out value="${sessionScope.loggedInUser.role}" /> &bull; @<c:out value="${sessionScope.loggedInUser.username}" /></div>
                                        </div>
                                    </div>
                                </c:if>
                                <a href="${pageContext.request.contextPath}/logout"
                                    class="nav-link sidebar-logout-link"
                                    title="Log Out">
                                    <i class="bi bi-box-arrow-right"></i>
                                    <span>Log Out</span>
                                </a>
                            </div>
                        </aside>

                        <!-- Main Content Column -->
                        <div class="main-wrapper">
                            <!-- Top Navbar -->
                            <div class="top-navbar">
                                <div class="d-flex align-items-center gap-2">
                                    <button type="button" class="btn sidebar-toggle-btn d-lg-none"
                                        onclick="toggleSidebar()" title="Open Navigation Sidebar"
                                        aria-label="Open Navigation Sidebar">
                                        <i class="bi bi-list fs-5"></i>
                                    </button>
                                    <span class="badge bg-primary px-3 py-2"><i
                                            class="bi bi-shield-check me-1"></i>UrbanTech Solutions</span>
                                    <span class="text-muted small d-none d-md-inline">SmartCity360 Operations
                                        Platform</span>
                                </div>
                                <div class="d-flex align-items-center gap-3">
                                    <c:choose>
                                        <c:when test="${isFallbackInMemory}">
                                            <span class="badge bg-warning text-dark px-3 py-2"
                                                title="Autonomous standalone mode">
                                                <i class="bi bi-shield-check me-1"></i> Standalone Mode
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success px-3 py-2" title="Services fully operational">
                                                <i class="bi bi-check-circle-fill me-1"></i> System Online
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <!-- Page Content Body -->
                            <div class="content-body">
                                <!-- Flash Notification Banner if any -->
                                <c:if test="${not empty sessionScope.flashMessage}">
                                    <div class="alert alert-${sessionScope.flashType != null ? sessionScope.flashType : 'info'} alert-dismissible fade show"
                                        role="alert">
                                        <i class="bi bi-info-circle-fill me-2"></i>
                                        ${sessionScope.flashMessage}
                                        <button type="button" class="btn-close" data-bs-dismiss="alert"
                                            aria-label="Close"></button>
                                    </div>
                                    <c:remove var="flashMessage" scope="session" />
                                    <c:remove var="flashType" scope="session" />
                                </c:if>