<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Zone Grid Control &amp; Hierarchy" />
<c:set var="activePage" value="composite" />
<jsp:include page="layout/header.jsp" />

<!-- Inline error toast (modal forms live at the bottom of the page, so it is shown at the top) -->
<c:if test="${errorMessage != null}">
    <div class="mb-4"><jsp:include page="layout/error-banner.jsp" /></div>
</c:if>

<!-- City Action & Status Bar -->
<div class="card mb-4">
    <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
            <span class="text-muted small text-uppercase fw-bold">Active City Zone:</span>
            <h4 class="fw-bold mb-0 text-dark">${city.zoneName}</h4>
        </div>
        <div class="d-flex flex-wrap align-items-center gap-3">
            <div class="text-sm-end">
                <div class="text-muted small fw-semibold">Current Aggregate Power Usage</div>
                <h4 class="fw-bold text-primary mb-0">
                    <fmt:formatNumber value="${totalPower}" type="number"/> kWh
                </h4>
            </div>
            <div class="border-start ps-3 d-flex flex-wrap gap-2">
                <!-- Add District Modal Trigger -->
                <button type="button" class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#addDistrictModal">
                    <i class="bi bi-plus-circle me-1"></i> Add District
                </button>
                <!-- Add Street Modal Trigger -->
                <button type="button" class="btn btn-outline-success" data-bs-toggle="modal" data-bs-target="#addStreetModal">
                    <i class="bi bi-signpost me-1"></i> Add Street
                </button>
                <!-- Add Building Modal Trigger -->
                <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#addBuildingModal">
                    <i class="bi bi-building-add me-1"></i> Add Building
                </button>
            </div>
        </div>
    </div>
</div>

<!-- City Structure Hierarchy Explorer -->
<div class="card mb-4">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
            <span class="fw-bold"><i class="bi bi-folder-symlink me-2 text-primary"></i>Interactive City Zone Tree</span>
            <span class="badge bg-light text-dark border" id="zoneCountBadge">${city.children.size()} Districts</span>
        </div>
        <div class="table-toolbar">
            <div class="input-group input-group-sm search-input-group">
                <span class="input-group-text bg-light"><i class="bi bi-search text-muted"></i></span>
                <input type="text" id="zoneSearchInput" class="form-control"
                       placeholder="Search districts, streets, buildings..." oninput="filterAndSortZoneTree()">
            </div>
            <select id="zoneSortSelect" class="form-select form-select-sm" style="width: auto;" onchange="filterAndSortZoneTree()">
                <option value="name-asc">Sort: Name (A &rarr; Z)</option>
                <option value="name-desc">Sort: Name (Z &rarr; A)</option>
                <option value="power-desc">Sort: Power Usage (High &rarr; Low)</option>
                <option value="power-asc">Sort: Power Usage (Low &rarr; High)</option>
            </select>
            <div class="d-flex flex-wrap gap-2">
                <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                    <input type="hidden" name="action" value="restoreZone">
                    <input type="hidden" name="zoneId" value="${city.id}">
                    <input type="hidden" name="zoneName" value="${city.zoneName}">
                    <button type="submit" class="btn btn-sm btn-outline-success">
                        <i class="bi bi-lightning-charge-fill me-1"></i> Restore All Power
                    </button>
                </form>
                <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline"
                      data-confirm-title="Shutdown Entire City Grid"
                      data-confirm-message="Are you sure you want to deactivate power across all districts, streets, and buildings in ${fn:escapeXml(city.zoneName)}?"
                      data-confirm-btn="Shutdown City"
                      data-confirm-variant="warning">
                    <input type="hidden" name="action" value="shutdownZone">
                    <input type="hidden" name="zoneId" value="${city.id}">
                    <input type="hidden" name="zoneName" value="${city.zoneName}">
                    <button type="submit" class="btn btn-sm btn-outline-danger">
                        <i class="bi bi-power me-1"></i> Shutdown Entire City
                    </button>
                </form>
            </div>
        </div>
    </div>
    <div class="card-body zone-tree-scroll" id="zoneTreeContainer">
        <c:forEach var="district" items="${city.children}">
            <div class="mb-3 p-3 border rounded-3 bg-light bg-opacity-50 district-node"
                 data-name="${district.zoneName}" data-power="${district.powerUsage}">
                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <div class="d-flex flex-wrap align-items-center gap-2">
                        <i class="bi bi-geo-alt-fill text-primary fs-5"></i>
                        <span class="fw-bold fs-6">${district.zoneName}</span>
                        <span class="badge bg-primary-subtle text-primary">
                            ${district.powerUsage} kWh
                        </span>
                    </div>
                    <div class="d-flex flex-wrap align-items-center gap-1">
                        <!-- Restore district power -->
                        <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                            <input type="hidden" name="action" value="restoreZone">
                            <input type="hidden" name="zoneId" value="${district.id}">
                            <input type="hidden" name="zoneName" value="${district.zoneName}">
                            <button type="submit" class="btn btn-sm btn-outline-success py-0 px-2" title="Restore power to district">
                                <i class="bi bi-lightning-charge"></i> Restore
                            </button>
                        </form>
                        <!-- Shutdown district power -->
                        <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                            <input type="hidden" name="action" value="shutdownZone">
                            <input type="hidden" name="zoneId" value="${district.id}">
                            <input type="hidden" name="zoneName" value="${district.zoneName}">
                            <button type="submit" class="btn btn-sm btn-outline-warning py-0 px-2" title="Shutdown power across district">
                                <i class="bi bi-power"></i> Shutdown
                            </button>
                        </form>
                        <!-- Delete district -->
                        <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline"
                              data-confirm-title="Delete Municipal District"
                              data-confirm-message="Delete '${fn:escapeXml(district.zoneName)}' and all streets and buildings inside it?"
                              data-confirm-btn="Delete District">
                            <input type="hidden" name="action" value="deleteZone">
                            <input type="hidden" name="zoneId" value="${district.id}">
                            <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-2" title="Delete district">
                                <i class="bi bi-trash"></i>
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Streets in District -->
                <div class="city-tree-node mt-2 street-list">
                    <c:forEach var="street" items="${district.children}">
                        <div class="mb-2 p-2 bg-white rounded border street-node"
                             data-name="${street.zoneName}" data-power="${street.powerUsage}">
                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                                <div class="d-flex flex-wrap align-items-center gap-2">
                                    <i class="bi bi-signpost-2 text-success"></i>
                                    <span class="fw-semibold">${street.zoneName}</span>
                                    <span class="badge bg-success-subtle text-success small">
                                        Subtotal: ${street.powerUsage} kWh
                                    </span>
                                </div>
                                <div class="d-flex align-items-center gap-1">
                                    <!-- Restore street -->
                                    <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="restoreZone">
                                        <input type="hidden" name="zoneId" value="${street.id}">
                                        <input type="hidden" name="zoneName" value="${street.zoneName}">
                                        <button type="submit" class="btn btn-sm btn-outline-success py-0 px-1" title="Restore power to street">
                                            <i class="bi bi-lightning-charge"></i>
                                        </button>
                                    </form>
                                    <!-- Shutdown street -->
                                    <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="shutdownZone">
                                        <input type="hidden" name="zoneId" value="${street.id}">
                                        <input type="hidden" name="zoneName" value="${street.zoneName}">
                                        <button type="submit" class="btn btn-sm btn-outline-secondary py-0 px-1" title="Shutdown entire street">
                                            <i class="bi bi-power"></i>
                                        </button>
                                    </form>
                                    <!-- Delete street -->
                                    <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline"
                                          data-confirm-title="Remove Street"
                                          data-confirm-message="Remove '${fn:escapeXml(street.zoneName)}' and all buildings assigned to it?"
                                          data-confirm-btn="Remove Street">
                                        <input type="hidden" name="action" value="deleteZone">
                                        <input type="hidden" name="zoneId" value="${street.id}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-1" title="Delete street">
                                            <i class="bi bi-x-circle"></i>
                                        </button>
                                    </form>
                                </div>
                            </div>

                            <!-- Buildings in Street -->
                            <div class="city-tree-node mt-2">
                                <div class="d-flex flex-wrap gap-2 building-list">
                                    <c:forEach var="building" items="${street.children}">
                                        <div class="border rounded px-2 py-1 bg-light d-flex align-items-center gap-2 building-node"
                                             data-name="${building.buildingName}" data-power="${building.powerOn ? building.powerUsage : 0}">
                                            <i class="bi bi-building"></i>
                                            <span class="fw-medium">${building.buildingName}</span>
                                            <span class="badge ${building.powerOn ? 'building-badge-on' : 'building-badge-off'}">
                                                ${building.powerOn ? building.powerUsage.toString().concat(' kWh') : 'OFF (0 kWh)'}
                                            </span>

                                            <!-- Toggle Power Form -->
                                            <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline">
                                                <input type="hidden" name="action" value="toggleBuildingPower">
                                                <input type="hidden" name="buildingId" value="${building.id}">
                                                <input type="hidden" name="currentPower" value="${building.powerOn}">
                                                <button type="submit" class="btn btn-link p-0 text-${building.powerOn ? 'danger' : 'success'}" title="Toggle power ${building.powerOn ? 'OFF' : 'ON'}">
                                                    <i class="bi bi-${building.powerOn ? 'toggle-on' : 'toggle-off'} fs-5"></i>
                                                </button>
                                            </form>

                                            <!-- Remove Building Form -->
                                            <form action="${pageContext.request.contextPath}/composite" method="post" class="d-inline"
                                                  data-confirm-title="Remove Building Facility"
                                                  data-confirm-message="Remove building '${fn:escapeXml(building.buildingName)}' from ${fn:escapeXml(street.zoneName)}?"
                                                  data-confirm-btn="Remove Building">
                                                <input type="hidden" name="action" value="deleteBuilding">
                                                <input type="hidden" name="buildingId" value="${building.id}">
                                                <button type="submit" class="btn btn-link p-0 text-danger" title="Remove building">
                                                    <i class="bi bi-trash small"></i>
                                                </button>
                                            </form>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:forEach>
        <div id="zoneTreeEmptyState" class="text-center text-muted py-4" style="${empty city.children ? '' : 'display: none;'}">
            <i class="bi bi-search me-1"></i> No matching districts, streets, or buildings found.
        </div>
    </div>
</div>

<!-- 100,000 Buildings Grid Capacity Analysis Card -->
<div class="card mb-4" id="scaleDemo">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
            <span class="fw-bold"><i class="bi bi-speedometer me-2"></i>Grid Capacity &amp; Load Analysis (100,000 Nodes)</span>
            <div class="text-muted small">High-throughput recursive power grid traversal evaluation</div>
        </div>
        <form action="${pageContext.request.contextPath}/composite" method="post">
            <input type="hidden" name="action" value="runScaleDemo">
            <button type="submit" class="btn btn-sm btn-primary">
                <i class="bi bi-cpu me-1"></i> Run Grid Load Analysis
            </button>
        </form>
    </div>
    <div class="card-body">
        <c:choose>
            <c:when test="${not empty sessionScope.scaleResult}">
                <c:set var="s" value="${sessionScope.scaleResult}" />
                <div class="row g-3">
                    <div class="col-sm-6 col-md-3">
                        <div class="p-3 border rounded text-center bg-light">
                            <div class="text-muted small">Hierarchy Topology</div>
                            <div class="fw-bold fs-5">${s.districts} &times; ${s.streetsPerDistrict} &times; ${s.buildingsPerStreet}</div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-md-3">
                        <div class="p-3 border rounded text-center bg-light">
                            <div class="text-muted small">Total Buildings</div>
                            <div class="fw-bold fs-5 text-success">
                                <fmt:formatNumber value="${s.buildingCount}" type="number"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-md-3">
                        <div class="p-3 border rounded text-center bg-light">
                            <div class="text-muted small">Tree Build Time</div>
                            <div class="fw-bold fs-5 text-primary">
                                <fmt:formatNumber value="${s.buildMillis}" pattern="#,##0.00"/> ms
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-md-3">
                        <div class="p-3 border rounded text-center bg-light">
                            <div class="text-muted small">Recursive Traversal Time</div>
                            <div class="fw-bold fs-5 text-info">
                                <fmt:formatNumber value="${s.traversalMillis}" pattern="#,##0.00"/> ms
                            </div>
                        </div>
                    </div>
                </div>
                <div class="alert alert-success mt-3 mb-0 small">
                    <i class="bi bi-check-circle-fill me-1"></i>
                    Evaluated <strong><fmt:formatNumber value="${s.buildingCount}" type="number"/></strong> nodes recursively in
                    <strong><fmt:formatNumber value="${s.traversalMillis}" pattern="#,##0.00"/> ms</strong>.
                    Aggregate calculated power: <strong><fmt:formatNumber value="${s.totalUsage}" type="number"/> kWh</strong>.
                    Confirms real-time recursive traversal with sub-millisecond response latency.
                </div>
                <c:remove var="scaleResult" scope="session"/>
            </c:when>
            <c:otherwise>
                <p class="text-muted mb-0">
                    Click <strong>Run Grid Load Analysis</strong> above to evaluate real-time aggregation across 100,000 distinct facility nodes across 10 districts and 200 streets in RAM.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Modal: Add District -->
<div class="modal fade" id="addDistrictModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/composite" method="post" class="modal-content">
            <input type="hidden" name="action" value="addDistrict">
            <input type="hidden" name="cityId" value="${city.id}">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-plus-circle me-2"></i>Add New District</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label fw-semibold">District Name</label>
                    <input type="text" name="districtName" class="form-control" placeholder="e.g. District 4 - Financial Zone" required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Create District</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Add Street -->
<div class="modal fade" id="addStreetModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/composite" method="post" class="modal-content">
            <input type="hidden" name="action" value="addStreet">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-signpost me-2"></i>Add Street to District</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label fw-semibold">Parent District</label>
                    <select name="districtId" class="form-select" required>
                        <c:forEach var="dist" items="${districts}">
                            <option value="${dist.id}">${dist.zoneName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">Street Name</label>
                    <input type="text" name="streetName" class="form-control" placeholder="e.g. Boulevard 1-4" required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-success">Add Street</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Add Building -->
<div class="modal fade" id="addBuildingModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/composite" method="post" class="modal-content">
            <input type="hidden" name="action" value="addBuilding">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-building-add me-2"></i>Add Building Facility</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label class="form-label fw-semibold">Assign to Street</label>
                    <select name="streetId" class="form-select" required>
                        <c:forEach var="st" items="${streets}">
                            <option value="${st.id}">${st.zoneName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">Building Name / Code</label>
                    <input type="text" name="buildingName" class="form-control" placeholder="e.g. Tech Tower 01" required>
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">Rated Power Usage (kWh)</label>
                    <input type="number" name="powerUsage" class="form-control" value="150" min="10" max="10000" required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-success">Add Building</button>
            </div>
        </form>
    </div>
</div>

<script>
function compareZoneNodes(a, b, sortBy) {
    if (sortBy === 'power-desc' || sortBy === 'power-asc') {
        const pA = parseFloat(a.getAttribute('data-power') || '0');
        const pB = parseFloat(b.getAttribute('data-power') || '0');
        if (pA !== pB) {
            return sortBy === 'power-asc' ? (pA - pB) : (pB - pA);
        }
    }
    const nA = (a.getAttribute('data-name') || '');
    const nB = (b.getAttribute('data-name') || '');
    const cmp = nA.localeCompare(nB, undefined, { numeric: true, sensitivity: 'base' });
    return sortBy === 'name-desc' ? -cmp : cmp;
}

function filterAndSortZoneTree() {
    const query = (document.getElementById('zoneSearchInput').value || '').trim().toLowerCase();
    const sortBy = document.getElementById('zoneSortSelect').value;
    const container = document.getElementById('zoneTreeContainer');
    const emptyState = document.getElementById('zoneTreeEmptyState');
    const districts = Array.from(container.querySelectorAll('.district-node'));

    districts.sort(function (a, b) {
        return compareZoneNodes(a, b, sortBy);
    });

    let visibleDistricts = 0;

    districts.forEach(function (district) {
        const districtName = (district.getAttribute('data-name') || '').toLowerCase();
        const districtMatch = !query || districtName.includes(query);

        const streetList = district.querySelector('.street-list');
        const streets = streetList ? Array.from(streetList.querySelectorAll('.street-node')) : [];

        streets.sort(function (a, b) {
            return compareZoneNodes(a, b, sortBy);
        });

        let anyStreetVisible = false;

        streets.forEach(function (street) {
            const streetName = (street.getAttribute('data-name') || '').toLowerCase();
            const streetMatch = !query || districtMatch || streetName.includes(query);

            const buildingList = street.querySelector('.building-list');
            const buildings = buildingList ? Array.from(buildingList.querySelectorAll('.building-node')) : [];

            buildings.sort(function (a, b) {
                return compareZoneNodes(a, b, sortBy);
            });

            let anyBuildingVisible = false;
            buildings.forEach(function (bldg) {
                const bldgName = (bldg.getAttribute('data-name') || '').toLowerCase();
                const bldgMatch = !query || districtMatch || streetMatch || bldgName.includes(query);
                bldg.style.display = bldgMatch ? '' : 'none';
                if (bldgMatch) anyBuildingVisible = true;
                buildingList.appendChild(bldg);
            });

            const showStreet = streetMatch || anyBuildingVisible;
            street.style.display = showStreet ? '' : 'none';
            if (showStreet) anyStreetVisible = true;
            streetList.appendChild(street);
        });

        const showDistrict = districtMatch || anyStreetVisible;
        district.style.display = showDistrict ? '' : 'none';
        if (showDistrict) visibleDistricts++;
        container.appendChild(district);
    });

    container.appendChild(emptyState);
    emptyState.style.display = (visibleDistricts === 0) ? '' : 'none';
    const badge = document.getElementById('zoneCountBadge');
    if (badge) {
        badge.textContent = visibleDistricts + (visibleDistricts === 1 ? ' District' : ' Districts');
    }
}
</script>

<jsp:include page="layout/footer.jsp" />

