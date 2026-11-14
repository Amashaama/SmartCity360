<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Citizen Grievances &amp; Escalation" />
<c:set var="activePage" value="chain" />
<jsp:include page="layout/header.jsp" />

<div class="row g-4 mb-4">
    <!-- Complaint Submission Form -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white">
                <h5 class="mb-0 fw-bold"><i class="bi bi-pencil-square me-2 text-primary"></i>Lodge Resident Complaint</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/chain" method="post">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Complaint Subject / Title</label>
                        <input type="text" name="title" class="form-control"  required maxlength="150" pattern=".*\S.*" title="Please enter a complaint title.">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Resident Name</label>
                        <input type="text" name="residentName" class="form-control"  required maxlength="100" pattern=".*\S.*" title="Please enter the resident name.">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Location / Zone</label>
                        <input type="text" name="location" class="form-control"  required maxlength="150" pattern=".*\S.*" title="Please enter a location.">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Detailed Description</label>
                        <textarea name="description" class="form-control" rows="3" placeholder="Describe the incident..."></textarea>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Severity Rating</label>
                        <select name="severity" class="form-select" required>
                            <option value="1">Level 1 - Minor / Low (Handled by Local Staff)</option>
                            <option value="2">Level 2 - Moderate / Medium (Escalated to Supervisor)</option>
                            <option value="3">Level 3 - Critical / High (Escalated to City Manager)</option>
                            <option value="4">Level 4 - Special / Unclassified (Municipal Oversight Board)</option>
                        </select>
                        <div class="form-text">Automated operational escalation routes based on priority rating.</div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">
                        <i class="bi bi-send-fill me-1"></i> Submit Complaint
                    </button>
                </form>
                <jsp:include page="layout/error-banner.jsp" />
            </div>
        </div>
    </div>

    <!-- Active Escalation Pipeline Representation -->
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
                <h5 class="mb-0 fw-bold"><i class="bi bi-diagram-2 me-2 text-success"></i>Escalation Pipeline Architecture</h5>
                <span class="badge bg-light text-dark border">Automated Routing</span>
            </div>
            <div class="card-body">
                <div class="d-flex flex-column gap-3">
                    <div class="p-3 border rounded bg-light d-flex flex-wrap align-items-center justify-content-between gap-2">
                        <div class="d-flex align-items-center gap-3">
                            <span class="badge bg-primary rounded-circle p-2 fs-6">1</span>
                            <div>
                                <h6 class="fw-bold mb-0">Local Staff Handler</h6>
                                <small class="text-muted">Processes Level 1: Minor maintenance, localized noise, rubbish collection.</small>
                            </div>
                        </div>
                        <span class="badge bg-primary-subtle text-primary">Severity 1</span>
                    </div>

                    <div class="text-center text-muted"><i class="bi bi-arrow-down fs-5"></i></div>

                    <div class="p-3 border rounded bg-light d-flex flex-wrap align-items-center justify-content-between gap-2">
                        <div class="d-flex align-items-center gap-3">
                            <span class="badge bg-warning text-dark rounded-circle p-2 fs-6">2</span>
                            <div>
                                <h6 class="fw-bold mb-0">Supervisor Handler</h6>
                                <small class="text-muted">Processes Level 2: Traffic signal failures, localized flooding, zone disputes.</small>
                            </div>
                        </div>
                        <span class="badge bg-warning-subtle text-warning">Severity 2</span>
                    </div>

                    <div class="text-center text-muted"><i class="bi bi-arrow-down fs-5"></i></div>

                    <div class="p-3 border rounded bg-light d-flex flex-wrap align-items-center justify-content-between gap-2">
                        <div class="d-flex align-items-center gap-3">
                            <span class="badge bg-danger rounded-circle p-2 fs-6">3</span>
                            <div>
                                <h6 class="fw-bold mb-0">City Manager Handler</h6>
                                <small class="text-muted">Processes Level 3: Grid blackout, hazardous chemical spills, hospital emergencies.</small>
                            </div>
                        </div>
                        <span class="badge bg-danger-subtle text-danger">Severity 3</span>
                    </div>

                    <div class="text-center text-muted"><i class="bi bi-arrow-down fs-5"></i></div>

                    <div class="p-3 border rounded bg-secondary-subtle d-flex flex-wrap align-items-center justify-content-between gap-2">
                        <div class="d-flex align-items-center gap-3">
                            <span class="badge bg-dark rounded-circle p-2 fs-6">4</span>
                            <div>
                                <h6 class="fw-bold mb-0">Municipal Review Board</h6>
                                <small class="text-muted">Special review queue ensuring all unclassified incidents are audited and assigned.</small>
                            </div>
                        </div>
                        <span class="badge bg-secondary">Review Board</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Complaint History & Audit Trail Table -->
<div class="card mb-4">
    <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
            <h5 class="mb-0 fw-bold"><i class="bi bi-clock-history me-2 text-info"></i>Escalation History &amp; Audit Log</h5>
            <span class="badge bg-light text-dark border" id="complaintCountBadge">${complaints.size()} Records</span>
        </div>
        <div class="table-toolbar">
            <div class="input-group input-group-sm search-input-group">
                <span class="input-group-text bg-light"><i class="bi bi-search text-muted"></i></span>
                <input type="text" id="complaintSearchInput" class="form-control"
                       placeholder="Search complaints..." oninput="filterAndSortComplaints()">
            </div>
            <select id="complaintSortSelect" class="form-select form-select-sm" style="width: auto;" onchange="filterAndSortComplaints()">
                <option value="id-desc" selected>Sort: Newest (#ID Desc)</option>
                <option value="id-asc">Sort: Oldest (#ID Asc)</option>
                <option value="severity-desc">Sort: Severity (High &rarr; Low)</option>
                <option value="severity-asc">Sort: Severity (Low &rarr; High)</option>
                <option value="title-asc">Sort: Subject (A &rarr; Z)</option>
                <option value="resident-asc">Sort: Resident (A &rarr; Z)</option>
                <option value="status-asc">Sort: Status</option>
            </select>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive table-scroll-vertical">
            <table class="table table-hover align-middle mb-0" id="complaintsTable">
                <thead class="table-light">
                <tr>
                    <th style="width: 70px;">ID</th>
                    <th>Subject</th>
                    <th>Resident</th>
                    <th>Location</th>
                    <th>Severity</th>
                    <th>Handled By</th>
                    <th>Resolution Status</th>
                    <th style="width: 130px;">Audit Trail</th>
                </tr>
                </thead>
                <tbody id="complaintsTableBody">
                <c:forEach var="c" items="${complaints}">
                    <tr class="complaint-row ${sessionScope.lastProcessedId == c.id ? 'table-warning' : ''}"
                        data-id="${c.id}"
                        data-severity="${c.severity}"
                        data-title="${fn:escapeXml(c.title)}"
                        data-resident="${fn:escapeXml(c.residentName)}"
                        data-status="${fn:escapeXml(c.status)}">
                        <td><strong>#${c.id}</strong></td>
                        <td class="fw-semibold">${c.title}</td>
                        <td>${c.residentName}</td>
                        <td><small class="text-muted">${c.location}</small></td>
                        <td>
                            <c:choose>
                                <c:when test="${c.severity == 1}">
                                    <span class="badge bg-primary">Low (1)</span>
                                </c:when>
                                <c:when test="${c.severity == 2}">
                                    <span class="badge bg-warning text-dark">Medium (2)</span>
                                </c:when>
                                <c:when test="${c.severity == 3}">
                                    <span class="badge bg-danger">High (3)</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">Level ${c.severity}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <span class="badge bg-light text-dark border">
                                <i class="bi bi-person-badge me-1"></i>${c.handledBy}
                            </span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${c.status == 'Resolved'}">
                                    <span class="badge bg-success"><i class="bi bi-check me-1"></i>Resolved</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-warning text-dark"><i class="bi bi-exclamation-triangle me-1"></i>${c.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <button type="button" class="btn btn-sm btn-outline-info" data-bs-toggle="modal" data-bs-target="#trailModal${c.id}">
                                <i class="bi bi-list-check me-1"></i> Trail
                            </button>
                        </td>
                    </tr>
                </c:forEach>
                <tr id="complaintsEmptyRow" class="empty-search-row" style="${empty complaints ? '' : 'display: none;'}">
                    <td colspan="8" class="text-center text-muted py-4">
                        <i class="bi bi-inbox me-1"></i> No matching complaint records found.
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Audit Trail Modals (placed outside table for valid HTML and scroll-container compatibility) -->
<c:forEach var="c" items="${complaints}">
    <div class="modal fade" id="trailModal${c.id}" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Audit Trail for Complaint #${c.id}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <h6 class="fw-bold text-primary mb-2">${c.title}</h6>
                    <p class="small text-muted mb-3">Submitted by ${c.residentName} (${c.location})</p>
                    <ul class="timeline">
                        <c:forEach var="step" items="${c.auditTrail}">
                            <li class="timeline-item">
                                <div class="timeline-marker ${step.contains('resolved') ? 'resolved' : (step.contains('cannot handle') ? 'passed' : '')}"></div>
                                <div class="fw-medium small">${step}</div>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</c:forEach>

<script>
function filterAndSortComplaints() {
    const query = (document.getElementById('complaintSearchInput').value || '').trim().toLowerCase();
    const sortBy = document.getElementById('complaintSortSelect').value;
    const tbody = document.getElementById('complaintsTableBody');
    const emptyRow = document.getElementById('complaintsEmptyRow');
    const rows = Array.from(tbody.querySelectorAll('tr.complaint-row'));

    rows.sort(function (a, b) {
        if (sortBy === 'severity-desc' || sortBy === 'severity-asc') {
            const sA = parseInt(a.getAttribute('data-severity') || '0', 10);
            const sB = parseInt(b.getAttribute('data-severity') || '0', 10);
            if (sA !== sB) return sortBy === 'severity-asc' ? (sA - sB) : (sB - sA);
        } else if (sortBy === 'title-asc') {
            return (a.getAttribute('data-title') || '').localeCompare(b.getAttribute('data-title') || '', undefined, { sensitivity: 'base' });
        } else if (sortBy === 'resident-asc') {
            return (a.getAttribute('data-resident') || '').localeCompare(b.getAttribute('data-resident') || '', undefined, { sensitivity: 'base' });
        } else if (sortBy === 'status-asc') {
            return (a.getAttribute('data-status') || '').localeCompare(b.getAttribute('data-status') || '', undefined, { sensitivity: 'base' });
        }
        const idA = parseInt(a.getAttribute('data-id') || '0', 10);
        const idB = parseInt(b.getAttribute('data-id') || '0', 10);
        return sortBy === 'id-asc' ? (idA - idB) : (idB - idA);
    });

    let visible = 0;
    rows.forEach(function (row) {
        const text = row.innerText.toLowerCase();
        const match = !query || text.includes(query);
        row.style.display = match ? '' : 'none';
        if (match) visible++;
        tbody.appendChild(row);
    });

    tbody.appendChild(emptyRow);
    emptyRow.style.display = (visible === 0) ? '' : 'none';
    const badge = document.getElementById('complaintCountBadge');
    if (badge) {
        badge.textContent = visible + (visible === 1 ? ' Record' : ' Records');
    }
}
</script>

<c:remove var="lastProcessedId" scope="session"/>
<jsp:include page="layout/footer.jsp" />

