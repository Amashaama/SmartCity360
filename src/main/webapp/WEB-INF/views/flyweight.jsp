<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Street Lighting Network" />
<c:set var="activePage" value="flyweight" />
<jsp:include page="layout/header.jsp" />

<!-- Live Memory Metrics Cards -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="text-muted small fw-semibold">Active Street Lights</div>
                <h3 class="fw-bold my-1 text-primary">
                    <fmt:formatNumber value="${totalLights}" type="number"/>
                </h3>
                <small class="text-muted">Field Deployments</small>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="text-muted small fw-semibold">Hardware Profiles</div>
                <h3 class="fw-bold my-1 text-success">${poolSize}</h3>
                <small class="text-muted">Distinct hardware specifications</small>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="text-muted small fw-semibold">Memory Consumed</div>
                <h3 class="fw-bold my-1 text-info">
                    <fmt:formatNumber value="${memoryReport.bytesWithFlyweight / 1024}" pattern="#,##0.0"/> KB
                </h3>
                <small class="text-muted">vs <fmt:formatNumber value="${memoryReport.bytesWithoutFlyweight / 1024}" pattern="#,##0.0"/> KB unoptimized</small>
            </div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="card h-100 bg-success bg-opacity-10 border-success">
            <div class="card-body">
                <div class="text-success small fw-semibold">RAM Optimization</div>
                <h3 class="fw-bold my-1 text-success">
                    <fmt:formatNumber value="${memoryReport.percentSaved}" pattern="#0.0"/>% Saved
                </h3>
                <small class="text-success">Shared specification architecture</small>
            </div>
        </div>
    </div>
</div>

<div class="row g-4 mb-4">
    <!-- Register Light Form -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white">
                <h5 class="mb-0 fw-bold"><i class="bi bi-plus-circle me-2 text-primary"></i>Register Street Light</h5>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/flyweight" method="post">
                    <input type="hidden" name="action" value="addSingle">

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Unique Light Code</label>
                        <input type="text" name="lightCode" class="form-control" placeholder="e.g. SL-101" required maxlength="50" pattern=".*\S.*" title="Please enter a light code.">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">GPS Coordinates (Latitude, Longitude)</label>
                        <input type="text" name="gps" class="form-control" placeholder="e.g. 40.7128, -74.0060" required maxlength="60" pattern=".*\S.*" title="Please enter GPS coordinates.">
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Lighting Hardware Profile</label>
                        <select name="typeName" class="form-select" id="modelSelect" onchange="updateModelFields()">
                            <option value="LED-Standard" data-bulb="Philips-X200" data-watt="45">LED-Standard (Philips-X200, 45W)</option>
                            <option value="LED-Bright" data-bulb="Osram-B300" data-watt="60">LED-Bright (Osram-B300, 60W)</option>
                            <option value="Sodium-Classic" data-bulb="GE-S100" data-watt="70">Sodium-Classic (GE-S100, 70W)</option>
                            <option value="Solar-Eco" data-bulb="Sun-E50" data-watt="30">Solar-Eco (Sun-E50, 30W)</option>
                            <option value="Halogen-Old" data-bulb="Lumi-H75" data-watt="90">Halogen-Old (Lumi-H75, 90W)</option>
                        </select>
                        <div class="form-text">Assigns pre-calibrated municipal hardware profile.</div>
                    </div>

                    <input type="hidden" name="bulbModel" id="hiddenBulb" value="Philips-X200">
                    <input type="hidden" name="wattage" id="hiddenWatt" value="45">

                    <div class="form-check form-switch mb-3">
                        <input class="form-check-input" type="checkbox" name="isOn" id="isOnSwitch" checked>
                        <label class="form-check-label fw-semibold" for="isOnSwitch">Initial Power State (ON)</label>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">
                        <i class="bi bi-save me-1"></i> Register Street Light
                    </button>
                </form>
                <jsp:include page="layout/error-banner.jsp" />
            </div>
        </div>
    </div>

    <!-- 50,000 Lights Fleet Optimization Analysis -->
    <div class="col-lg-7">
        <div class="card h-100" id="bulkDemo">
            <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-2">
                <h5 class="mb-0 fw-bold"><i class="bi bi-speedometer me-2 text-success"></i>Fleet Capacity &amp; Memory Optimization Analysis</h5>
                <form action="${pageContext.request.contextPath}/flyweight" method="post" class="d-inline">
                    <input type="hidden" name="action" value="runBulkDemo">
                    <button type="submit" class="btn btn-sm btn-outline-success">
                        <i class="bi bi-play-fill me-1"></i> Analyze 50,000 Unit Deployment
                    </button>
                </form>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty sessionScope.bulkResult}">
                        <c:set var="res" value="${sessionScope.bulkResult}" />
                        <div class="alert alert-success">
                            <h6 class="fw-bold mb-2"><i class="bi bi-check-circle-fill me-2"></i>Analysis Results: 50,000 Nodes</h6>
                            <ul class="mb-2 small">
                                <li><strong>Provisioned:</strong> <fmt:formatNumber value="${res.lightCount}" type="number"/> street lights in <strong><fmt:formatNumber value="${res.elapsedMillis}" pattern="#,##0.00"/> ms</strong>.</li>
                                <li><strong>Hardware Profiles:</strong> exactly <strong>${res.poolSize}</strong> distinct types in memory.</li>
                                <li>
                                    <strong>Profile Reuse Identity:</strong>
                                    <span class="badge ${res.sameModelShared ? 'bg-success' : 'bg-danger'}">${res.sameModelShared ? 'VERIFIED (Shared Profile Reference)' : 'FAILED'}</span>
                                </li>
                                <li>
                                    <strong>Distinct Model Check:</strong>
                                    <span class="badge ${res.differentModelShared ? 'bg-danger' : 'bg-success'}">${res.differentModelShared ? 'FAIL' : 'VERIFIED (Distinct Hardware Model)'}</span>
                                </li>
                                <li><strong>Memory Saved:</strong> <fmt:formatNumber value="${res.memoryReport.bytesSaved / 1024}" pattern="#,##0.0"/> KB (<fmt:formatNumber value="${res.memoryReport.percentSaved}" pattern="#0.0"/>% reduction).</li>
                            </ul>
                        </div>
                        <c:remove var="bulkResult" scope="session"/>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted">
                            Click <strong>Analyze 50,000 Unit Deployment</strong> above to evaluate memory footprint and profile distribution across a 50,000-unit municipal fleet in RAM.
                        </p>
                    </c:otherwise>
                </c:choose>

                <!-- Comparison Table -->
                <div class="table-responsive mt-3">
                    <table class="table table-bordered table-sm small text-center mb-0">
                        <thead class="table-light">
                        <tr>
                            <th>Approach</th>
                            <th>Instances in RAM</th>
                            <th>Memory Consumption</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="text-start">Unoptimized (Independent Profiles)</td>
                            <td>50,000 Type + 50,000 Light Objects</td>
                            <td class="text-danger fw-bold">4,200,000 Bytes (~4.2 MB)</td>
                        </tr>
                        <tr class="table-success">
                            <td class="text-start"><strong>SmartCity360 Optimized Architecture</strong></td>
                            <td><strong>5 Shared Profiles</strong> + 50,000 Light References</td>
                            <td class="text-success fw-bold">400,420 Bytes (~0.4 MB)</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Street Light Registry Table -->
<div class="card mb-4">
    <div class="card-header bg-white d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
            <h5 class="mb-0 fw-bold"><i class="bi bi-list-ul me-2 text-primary"></i>Street Light Inventory</h5>
            <span class="badge bg-light text-dark border" id="lightCountBadge">Showing ${sampleLights.size()} Lights</span>
        </div>
        <div class="table-toolbar">
            <div class="input-group input-group-sm search-input-group">
                <span class="input-group-text bg-light"><i class="bi bi-search text-muted"></i></span>
                <input type="text" id="lightSearchInput" class="form-control"
                       placeholder="Search Code or Bulb Model..." oninput="filterAndSortLights()">
            </div>
            <select id="lightSortSelect" class="form-select form-select-sm" style="width: auto;" onchange="filterAndSortLights()">
                <option value="code-asc">Sort: Code (A &rarr; Z)</option>
                <option value="code-desc">Sort: Code (Z &rarr; A)</option>
                <option value="bulb-asc">Sort: Bulb Model (A &rarr; Z)</option>
                <option value="bulb-desc">Sort: Bulb Model (Z &rarr; A)</option>
                <option value="id-desc" selected>Sort: Latest Registered</option>
            </select>
            <button type="button" class="btn btn-sm btn-outline-primary"
                    onclick="printTableReport('streetLightTable', 'Street Light Inventory Report', 'Municipal Street Lighting Network &amp; Hardware Specifications')">
                <i class="bi bi-printer me-1"></i> Print Report
            </button>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive table-scroll-vertical">
            <table class="table table-hover align-middle mb-0" id="streetLightTable">
                <thead class="table-light">
                <tr>
                    <th style="cursor: pointer;" onclick="toggleLightSort('code')" title="Click to sort by Code">
                        Code <i class="bi bi-arrow-down-up small text-muted ms-1"></i>
                    </th>
                    <th>GPS Location</th>
                    <th>Hardware Profile</th>
                    <th style="cursor: pointer;" onclick="toggleLightSort('bulb')" title="Click to sort by Bulb Model">
                        Bulb Model <i class="bi bi-arrow-down-up small text-muted ms-1"></i>
                    </th>
                    <th>Wattage</th>
                    <th>State</th>
                    <th class="no-print" style="width: 100px;">Action</th>
                </tr>
                </thead>
                <tbody id="streetLightBody">
                <c:forEach var="light" items="${sampleLights}">
                    <tr class="light-row"
                        data-id="${light.id}"
                        data-code="${fn:escapeXml(light.lightCode)}"
                        data-bulb="${fn:escapeXml(light.bulbModel)}">
                        <td><strong>${light.lightCode}</strong></td>
                        <td><code>${light.gps}</code></td>
                        <td><span class="badge bg-primary-subtle text-primary">${light.typeName}</span></td>
                        <td>${light.bulbModel}</td>
                        <td>${light.wattage} W</td>
                        <td>
                            <span class="badge ${light.on ? 'bg-success' : 'bg-secondary'}">
                                ${light.on ? 'ON' : 'OFF'}
                            </span>
                        </td>
                        <td class="no-print">
                            <form action="${pageContext.request.contextPath}/flyweight" method="post" class="d-inline">
                                <input type="hidden" name="action" value="toggleLight">
                                <input type="hidden" name="id" value="${light.id}">
                                <input type="hidden" name="newState" value="${!light.on}">
                                <button type="submit" class="btn btn-sm btn-outline-${light.on ? 'danger' : 'success'} py-0 px-2">
                                    <i class="bi bi-power"></i> ${light.on ? 'Off' : 'On'}
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <tr id="lightEmptyRow" class="empty-search-row" style="${empty sampleLights ? '' : 'display: none;'}">
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="bi bi-inbox me-1"></i> No matching street lights found.
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
function updateModelFields() {
    const sel = document.getElementById('modelSelect');
    const opt = sel.options[sel.selectedIndex];
    document.getElementById('hiddenBulb').value = opt.getAttribute('data-bulb');
    document.getElementById('hiddenWatt').value = opt.getAttribute('data-watt');
}

function toggleLightSort(field) {
    const sel = document.getElementById('lightSortSelect');
    if (field === 'code') {
        sel.value = (sel.value === 'code-asc') ? 'code-desc' : 'code-asc';
    } else if (field === 'bulb') {
        sel.value = (sel.value === 'bulb-asc') ? 'bulb-desc' : 'bulb-asc';
    }
    filterAndSortLights();
}

function filterAndSortLights() {
    const query = (document.getElementById('lightSearchInput').value || '').trim().toLowerCase();
    const sortBy = document.getElementById('lightSortSelect').value;
    const tbody = document.getElementById('streetLightBody');
    const emptyRow = document.getElementById('lightEmptyRow');
    const rows = Array.from(tbody.querySelectorAll('tr.light-row'));

    rows.sort(function (a, b) {
        if (sortBy === 'code-asc' || sortBy === 'code-desc') {
            const cA = a.getAttribute('data-code') || '';
            const cB = b.getAttribute('data-code') || '';
            const cmp = cA.localeCompare(cB, undefined, { numeric: true, sensitivity: 'base' });
            return sortBy === 'code-asc' ? cmp : -cmp;
        } else if (sortBy === 'bulb-asc' || sortBy === 'bulb-desc') {
            const bA = a.getAttribute('data-bulb') || '';
            const bB = b.getAttribute('data-bulb') || '';
            const cmp = bA.localeCompare(bB, undefined, { numeric: true, sensitivity: 'base' });
            if (cmp !== 0) return sortBy === 'bulb-asc' ? cmp : -cmp;
            const cA = a.getAttribute('data-code') || '';
            const cB = b.getAttribute('data-code') || '';
            return cA.localeCompare(cB, undefined, { numeric: true, sensitivity: 'base' });
        } else {
            const idA = parseInt(a.getAttribute('data-id') || '0', 10);
            const idB = parseInt(b.getAttribute('data-id') || '0', 10);
            return idB - idA;
        }
    });

    let visible = 0;
    rows.forEach(function (row) {
        const code = (row.getAttribute('data-code') || '').toLowerCase();
        const bulb = (row.getAttribute('data-bulb') || '').toLowerCase();
        const text = row.innerText.toLowerCase();
        const match = !query || code.includes(query) || bulb.includes(query) || text.includes(query);
        row.style.display = match ? '' : 'none';
        if (match) visible++;
        tbody.appendChild(row);
    });

    tbody.appendChild(emptyRow);
    emptyRow.style.display = (visible === 0) ? '' : 'none';
    const badge = document.getElementById('lightCountBadge');
    if (badge) {
        badge.textContent = 'Showing ' + visible + (visible === 1 ? ' Light' : ' Lights');
    }
}
</script>

<jsp:include page="layout/footer.jsp" />
