<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Smart Meter Telemetry" />
<c:set var="activePage" value="decorator" />
<jsp:include page="layout/header.jsp" />

<c:if test="${errorMessage != null}">
    <div class="mb-4"><jsp:include page="layout/error-banner.jsp" /></div>
</c:if>

<div class="row g-4 mb-4">
    <!-- Left Card: Transmit Meter Data -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span class="fw-bold text-dark"><i class="bi bi-broadcast me-2 text-primary"></i>Transmit Meter Data</span>
                <span class="badge bg-primary-subtle text-primary">Telemetry Input</span>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/decorator">
                    <div class="mb-3">
                        <label class="form-label fw-semibold small">Smart Meter Identifier</label>
                        <select name="meterId" id="meterIdSelect" class="form-select" onchange="updateMeterSample(this.value)">
                            <option value="METER-101" ${activeMeterId == 'METER-101' ? 'selected' : ''}>METER-101 &mdash; Downtown Substation</option>
                            <option value="METER-204" ${activeMeterId == 'METER-204' ? 'selected' : ''}>METER-204 &mdash; Harbour Commercial Grid</option>
                            <option value="METER-309" ${activeMeterId == 'METER-309' ? 'selected' : ''}>METER-309 &mdash; Central Hospital Sector</option>
                            <option value="METER-412" ${activeMeterId == 'METER-412' ? 'selected' : ''}>METER-412 &mdash; East Industrial Park</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold small">Raw Reading Payload</label>
                        <textarea name="reading" id="readingInput" class="form-control font-monospace small" rows="2" required><c:out value="${activeReading != null ? activeReading : 'Meter 101:    420.5 kWh    at   10:30   [Normal Load]'}"/></textarea>

                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold small d-block">Security &amp; Optimization Controls</label>

                        <div class="form-check mb-2">
                            <input class="form-check-input" type="checkbox" name="compress" id="chkCompress"
                                   ${trace == null || trace.options.compress ? 'checked' : ''}>
                            <label class="form-check-label small" for="chkCompress">
                                <span class="fw-semibold text-dark">Data Compression</span>
<%--                                <span class="text-muted d-block">Removes redundant whitespace to reduce bandwidth</span>--%>
                            </label>
                        </div>

                        <div class="form-check mb-2">
                            <input class="form-check-input" type="checkbox" name="encrypt" id="chkEncrypt"
                                   ${trace == null || trace.options.encrypt ? 'checked' : ''}>
                            <label class="form-check-label small" for="chkEncrypt">
                                <span class="fw-semibold text-dark">Payload Encryption (AES-128)</span>
<%--                                <span class="text-muted d-block">Encrypts transmission payload using 128-bit AES Base64 cipher</span>--%>
                            </label>
                        </div>

                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="authenticate" id="chkAuth"
                                   ${trace == null || trace.options.authenticate ? 'checked' : ''}>
                            <label class="form-check-label small" for="chkAuth">
                                <span class="fw-semibold text-dark">Token Authentication (HMAC-SHA256)</span>
<%--                                <span class="text-muted d-block">Automatically attaches meter hardware signature token</span>--%>
                            </label>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 fw-semibold">
                        <i class="bi bi-send me-1"></i> Transmit Meter Data
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Right Card: Telemetry Transmission Log (5-Stage Table) -->
    <div class="col-lg-7" id="pipelineResult">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span class="fw-bold text-dark"><i class="bi bi-Sliders me-2 text-success"></i>Telemetry Transmission Log</span>
                <span class="badge bg-success-subtle text-success">Security Pipeline</span>
            </div>
            <div class="card-body">
                <c:if test="${trace != null}">
                    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 small">
                        <span>Active Meter: <strong class="text-primary">${activeMeterId}</strong></span>
                        <span>Auto Device Token: <code class="bg-light px-2 py-1 rounded border">${activeToken}</code></span>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-bordered align-middle mb-0 small">
                            <thead class="table-light">
                            <tr>
                                <th style="width: 195px;">Pipeline Stage</th>
                                <th>Processed Payload</th>
                                <th style="width: 90px;" class="text-center">Length</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td class="fw-semibold">1. Raw Payload</td>
                                <td class="font-monospace text-muted" style="white-space: pre-wrap;"><c:out value="${trace.original}" /></td>
                                <td class="text-center">${trace.original.length()} chars</td>
                            </tr>
                            <tr>
                                <td class="fw-semibold">2. After Compression</td>
                                <td class="font-monospace">
                                    <c:choose>
                                        <c:when test="${trace.options.compress}">
                                            <span class="text-primary"><c:out value="${trace.compressed}" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary-subtle text-secondary me-1">Bypassed</span>
                                            <span class="text-muted"><c:out value="${trace.compressed}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">${trace.compressed.length()} chars</td>
                            </tr>
                            <tr>
                                <td class="fw-semibold">3. After AES Encryption</td>
                                <td class="font-monospace text-break">
                                    <c:choose>
                                        <c:when test="${trace.options.encrypt}">
                                            <span class="text-warning-emphasis"><c:out value="${trace.encrypted}" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary-subtle text-secondary me-1">Bypassed</span>
                                            <span class="text-muted"><c:out value="${trace.encrypted}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">${trace.encrypted.length()} chars</td>
                            </tr>
                            <tr>
                                <td class="fw-semibold">4. Transmitted Stream</td>
                                <td class="font-monospace text-break">
                                    <c:if test="${!trace.options.authenticate}">
                                        <span class="badge bg-secondary-subtle text-secondary me-1">No Token</span>
                                    </c:if>
                                    <span class="text-dark"><c:out value="${trace.finalSent}" /></span>
                                </td>
                                <td class="text-center">${trace.finalSent.length()} chars</td>
                            </tr>
                            <tr class="table-success">
                                <td class="fw-bold text-success-emphasis">5. Ingested &amp; Decoded</td>
                                <td class="font-monospace fw-bold text-success-emphasis"><c:out value="${trace.readBack}" /></td>
                                <td class="text-center fw-bold">${trace.readBack.length()} chars</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<!-- Middle Card: Stream Access & Token Verification (1-Click Verification, No Manual Typing) -->
<div class="card mb-4" id="tamperTest">
    <div class="card-header d-flex justify-content-between align-items-center">
        <span class="fw-bold text-dark"><i class="bi bi-shield-lock me-2 text-warning"></i>Stream Access &amp; Token Verification</span>
        <span class="badge bg-warning-subtle text-warning-emphasis">Access Control</span>
    </div>
    <div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
            <div class="small text-muted">
                Test reading the latest telemetry stream using the meter's registered hardware signature
                (<code>${activeToken}</code>) versus an unauthorized request.
            </div>
            <div class="d-flex gap-2">
                <form method="post" action="${pageContext.request.contextPath}/decorator" class="d-inline">
                    <input type="hidden" name="action" value="verifyToken">
                    <input type="hidden" name="mode" value="AUTHORIZED">
                    <input type="hidden" name="meterId" value="${activeMeterId}">
                    <input type="hidden" name="reading" value="<c:out value='${activeReading}'/>">
                    <c:if test="${trace == null || trace.options.compress}"><input type="hidden" name="compress" value="on"></c:if>
                    <c:if test="${trace == null || trace.options.encrypt}"><input type="hidden" name="encrypt" value="on"></c:if>
                    <input type="hidden" name="authenticate" value="on">
                    <button type="submit" class="btn btn-sm btn-success">
                        <i class="bi bi-check-circle me-1"></i> Verify Authorized Stream
                    </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/decorator" class="d-inline">
                    <input type="hidden" name="action" value="verifyToken">
                    <input type="hidden" name="mode" value="TAMPERED">
                    <input type="hidden" name="meterId" value="${activeMeterId}">
                    <input type="hidden" name="reading" value="<c:out value='${activeReading}'/>">
                    <c:if test="${trace == null || trace.options.compress}"><input type="hidden" name="compress" value="on"></c:if>
                    <c:if test="${trace == null || trace.options.encrypt}"><input type="hidden" name="encrypt" value="on"></c:if>
                    <input type="hidden" name="authenticate" value="on">
                    <button type="submit" class="btn btn-sm btn-outline-danger">
                        <i class="bi bi-x-octagon me-1"></i> Simulate Unauthorized Read
                    </button>
                </form>
            </div>
        </div>

        <c:if test="${verificationResult != null}">
            <div class="mt-3">
                <c:choose>
                    <c:when test="${verificationResult.success}">
                        <div class="alert alert-success mb-0 py-2 px-3 small">
                            <i class="bi bi-check-circle-fill me-1"></i>
                            <strong>Access Granted: Device Signature Verified (${verificationResult.candidateToken})</strong> &mdash;
                            Decoded Meter Reading: <code class="ms-1"><c:out value="${verificationResult.decodedData}" /></code>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-danger mb-0 py-2 px-3 small">
                            <i class="bi bi-exclamation-triangle-fill me-1"></i>
                            <strong><c:out value="${wrongTokenMessage}" /></strong>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>
</div>

<!-- Bottom Card: Telemetry Transmission History -->
<div class="card">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div class="d-flex align-items-center gap-2">
            <span class="fw-bold text-dark"><i class="bi bi-clock-history me-1 text-secondary"></i>Telemetry Transmission History</span>
            <span class="badge bg-secondary-subtle text-secondary">${history.size()} Logs</span>
        </div>
        <div class="d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width: 210px;">
                <span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span>
                <input type="text" id="telemetrySearchInput" class="form-control" placeholder="Search by Meter ID..." onkeyup="filterAndSortTelemetryTable()">
            </div>
            <select id="telemetrySortSelect" class="form-select form-select-sm" style="width: auto;" onchange="filterAndSortTelemetryTable()">
                <option value="id-desc">Sort: Newest (#ID Desc)</option>
                <option value="id-asc">Sort: Oldest (#ID Asc)</option>
                <option value="meter-asc">Sort: Meter ID (A &rarr; Z)</option>
                <option value="meter-desc">Sort: Meter ID (Z &rarr; A)</option>
            </select>
            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="printTableReport('telemetryHistoryTable', 'Telemetry Transmission History')">
                <i class="bi bi-printer me-1"></i> Print Report
            </button>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0 small" id="telemetryHistoryTable">
                <thead class="table-light">
                <tr>
                    <th style="width: 65px;">ID</th>
                    <th style="width: 120px;">Meter ID</th>
                    <th>Original Reading</th>
                    <th style="width: 175px;">Active Controls</th>
                    <th>Stored Payload (Encrypted)</th>
                    <th>Decoded Read-Back</th>
                </tr>
                </thead>
                <tbody id="telemetryTableBody">
                <c:forEach var="item" items="${history}">
                    <tr class="telemetry-row" data-id="${item.id}" data-meter="${item.meterId}">
                        <td class="text-muted">#${item.id}</td>
                        <td class="fw-bold">${item.meterId}</td>
                        <td class="font-monospace text-muted"><c:out value="${item.originalReading}" /></td>
                        <td>
                            <c:if test="${item.useCompression}"><span class="badge bg-info-subtle text-info-emphasis me-1">Comp</span></c:if>
                            <c:if test="${item.useEncryption}"><span class="badge bg-warning-subtle text-warning-emphasis me-1">Enc</span></c:if>
                            <c:if test="${item.useAuth}"><span class="badge bg-success-subtle text-success-emphasis">Auth</span></c:if>
                            <c:if test="${!item.useCompression && !item.useEncryption && !item.useAuth}">
                                <span class="badge bg-secondary-subtle text-secondary">Plain</span>
                            </c:if>
                        </td>
                        <td class="font-monospace text-muted">
                            <div class="text-truncate" style="max-width: 240px;" title="<c:out value='${item.finalSent}'/>">
                                <c:out value="${item.finalSent}" />
                            </div>
                        </td>
                        <td class="font-monospace text-success-emphasis"><c:out value="${item.readBack}" /></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
const METER_SAMPLE_READINGS = {
    'METER-101': 'Meter 101:    420.5 kWh    at   10:30   [Normal Load]',
    'METER-204': 'Meter 204:    685.2 kWh    at   11:15   [Harbour Peak]',
    'METER-309': 'Meter 309:    910.0 kWh    at   12:00   [Hospital Grid]',
    'METER-412': 'Meter 412:    540.8 kWh    at   14:45   [Industrial Zone]'
};

function updateMeterSample(meterId) {
    if (METER_SAMPLE_READINGS[meterId]) {
        document.getElementById('readingInput').value = METER_SAMPLE_READINGS[meterId];
    }
}

function filterAndSortTelemetryTable() {
    const searchVal = (document.getElementById('telemetrySearchInput').value || '').toLowerCase().trim();
    const sortMode = document.getElementById('telemetrySortSelect').value;
    const tbody = document.getElementById('telemetryTableBody');
    const rows = Array.from(tbody.querySelectorAll('.telemetry-row'));

    rows.forEach(row => {
        const meter = (row.getAttribute('data-meter') || '').toLowerCase();
        const text = row.innerText.toLowerCase();
        row.style.display = (!searchVal || meter.includes(searchVal) || text.includes(searchVal)) ? '' : 'none';
    });

    rows.sort((a, b) => {
        const idA = parseInt(a.getAttribute('data-id') || '0', 10);
        const idB = parseInt(b.getAttribute('data-id') || '0', 10);
        const mA = (a.getAttribute('data-meter') || '').toLowerCase();
        const mB = (b.getAttribute('data-meter') || '').toLowerCase();
        if (sortMode === 'id-asc') return idA - idB;
        if (sortMode === 'id-desc') return idB - idA;
        if (sortMode === 'meter-asc') return mA.localeCompare(mB);
        if (sortMode === 'meter-desc') return mB.localeCompare(mA);
        return 0;
    });

    rows.forEach(row => tbody.appendChild(row));
}
</script>

<jsp:include page="layout/footer.jsp" />
