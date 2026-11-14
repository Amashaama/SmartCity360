<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="System Dashboard" />
<c:set var="activePage" value="home" />
<jsp:include page="layout/header.jsp" />

<c:if test="${errorMessage != null}">
    <div class="mb-4"><jsp:include page="layout/error-banner.jsp" /></div>
</c:if>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
    <div>
        <h3 class="fw-bold mb-1">SmartCity360 Overview</h3>
        <p class="text-muted mb-0">UrbanTech Solutions </p>
    </div>
</div>


<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">Citizen Complaints</div>
                        <h3 class="fw-bold my-1">${totalComplaints}</h3>
                        <span class="badge bg-primary-subtle text-primary">Active Queue</span>
                    </div>
                    <div class="p-3 bg-primary-subtle text-primary rounded-circle">
                        <i class="bi bi-signpost-split fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">Street Lights</div>
                        <h3 class="fw-bold my-1">${totalLights}</h3>
                        <span class="badge bg-success-subtle text-success">${distinctTypes} Hardware Models</span>
                    </div>
                    <div class="p-3 bg-success-subtle text-success rounded-circle">
                        <i class="bi bi-lightbulb fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">Policy Rules</div>
                        <h3 class="fw-bold my-1">${totalRules}</h3>
                        <span class="badge bg-info-subtle text-info">Active Policies</span>
                    </div>
                    <div class="p-3 bg-info-subtle text-info rounded-circle">
                        <i class="bi bi-code-square fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">Smart Meters</div>
                        <h3 class="fw-bold my-1">Connected</h3>
                        <span class="badge bg-warning-subtle text-warning">Encrypted Link</span>
                    </div>
                    <div class="p-3 bg-warning-subtle text-warning rounded-circle">
                        <i class="bi bi-shield-lock fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Live Infrastructure & Operations Analytics Graphs -->
<div class="row g-4 mb-4">
    <!-- Graph 1: City Grid Power Load by District (Bar Graph) -->
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span class="fw-bold text-dark"><i class="bi bi-bar-chart-line me-2 text-success"></i>City Grid Power Load by District</span>
                <span class="badge bg-success-subtle text-success border border-success-subtle">${totalCityPower} kWh Total</span>
            </div>
            <div class="card-body d-flex flex-column justify-content-center">
                <c:choose>
                    <c:when test="${city != null && not empty city.children}">
                        <c:forEach var="district" items="${city.children}" varStatus="loop">
                            <c:set var="pct" value="${totalCityPower > 0 ? (district.powerUsage * 100) / totalCityPower : 0}" />
                            <div class="mb-3">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <div>
                                        <span class="fw-semibold text-dark small">${district.zoneName}</span>
                                        <span class="text-muted" style="font-size: 0.75rem;">(${district.children.size()} streets)</span>
                                    </div>
                                    <div class="small">
                                        <span class="fw-bold text-dark">${district.powerUsage} kWh</span>
                                        <span class="badge bg-light text-secondary border ms-1" style="font-size: 0.7rem;">
                                            ${Math.round(pct.doubleValue())}%
                                        </span>
                                    </div>
                                </div>
                                <div class="progress" style="height: 12px; border-radius: 6px; background-color: #edf2f7;">
                                    <div class="progress-bar ${loop.index % 3 == 0 ? 'bg-success' : (loop.index % 3 == 1 ? 'bg-primary' : 'bg-info')}"
                                         role="progressbar"
                                         style="width: ${pct}%; border-radius: 6px; transition: width 0.5s ease;"
                                         aria-valuenow="${district.powerUsage}" aria-valuemin="0" aria-valuemax="${totalCityPower}">
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <div class="d-flex justify-content-between align-items-center pt-2 border-top mt-auto small text-muted">
                            <span><i class="bi bi-diagram-3 me-1"></i>Source: Composite Zone Hierarchy (${city.zoneName})</span>
                            <a href="${pageContext.request.contextPath}/composite" class="text-decoration-none fw-semibold">Manage Grid &rarr;</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center text-muted py-4">No active city districts found.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Graph 2: Citizen Complaints by Severity Tier (Donut Chart) -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span class="fw-bold text-dark"><i class="bi bi-pie-chart me-2 text-primary"></i>Complaints by Severity Tier</span>
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">${resolvedCount}/${totalComplaints} Resolved</span>
            </div>
            <div class="card-body d-flex flex-column justify-content-between">
                <div class="row align-items-center my-auto py-2">
                    <div class="col-5 d-flex justify-content-center">
                        <div id="complaintDonutChart"
                             data-s1="${sev1Count}" data-s2="${sev2Count}" data-s3="${sev3Count}" data-s4="${sev4Count}"
                             style="width: 130px; height: 130px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #e2e8f0; position: relative;">
                            <div style="width: 84px; height: 84px; border-radius: 50%; background: #ffffff; display: flex; flex-direction: column; align-items: center; justify-content: center; box-shadow: inset 0 1px 3px rgba(0,0,0,0.06);">
                                <span class="fw-bold fs-4 lh-1 text-dark">${totalComplaints}</span>
                                <span class="text-muted" style="font-size: 0.68rem;">Total Cases</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-7">
                        <div class="d-flex justify-content-between align-items-center mb-2 small">
                            <span><span class="d-inline-block rounded-circle me-2" style="width:10px;height:10px;background:#0dcaf0;"></span>Level 1 (Minor)</span>
                            <span class="fw-bold">${sev1Count}</span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-2 small">
                            <span><span class="d-inline-block rounded-circle me-2" style="width:10px;height:10px;background:#ffc107;"></span>Level 2 (Moderate)</span>
                            <span class="fw-bold">${sev2Count}</span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-2 small">
                            <span><span class="d-inline-block rounded-circle me-2" style="width:10px;height:10px;background:#dc3545;"></span>Level 3 (Critical)</span>
                            <span class="fw-bold">${sev3Count}</span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center small">
                            <span><span class="d-inline-block rounded-circle me-2" style="width:10px;height:10px;background:#6c757d;"></span>Level 4 (Review)</span>
                            <span class="fw-bold">${sev4Count}</span>
                        </div>
                    </div>
                </div>
                <div class="d-flex justify-content-between align-items-center pt-2 border-top mt-2 small text-muted">
                    <span><i class="bi bi-funnel me-1"></i>Source: Escalation Chain Log</span>
                    <a href="${pageContext.request.contextPath}/chain" class="text-decoration-none fw-semibold">View Pipeline &rarr;</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
(function() {
    var el = document.getElementById('complaintDonutChart');
    if (!el) return;
    var s1 = parseInt(el.getAttribute('data-s1') || '0', 10);
    var s2 = parseInt(el.getAttribute('data-s2') || '0', 10);
    var s3 = parseInt(el.getAttribute('data-s3') || '0', 10);
    var s4 = parseInt(el.getAttribute('data-s4') || '0', 10);
    var total = s1 + s2 + s3 + s4;
    if (total > 0) {
        var p1 = (s1 / total) * 360;
        var p2 = p1 + (s2 / total) * 360;
        var p3 = p2 + (s3 / total) * 360;
        el.style.background = 'conic-gradient(#0dcaf0 0deg ' + p1 + 'deg, #ffc107 ' + p1 + 'deg ' + p2 + 'deg, #dc3545 ' + p2 + 'deg ' + p3 + 'deg, #6c757d ' + p3 + 'deg 360deg)';
    }
})();
</script>

<jsp:include page="layout/footer.jsp" />
