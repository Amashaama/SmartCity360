<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
            <c:set var="pageTitle" value="Municipal Policy Rules Engine" />
            <c:set var="activePage" value="interpreter" />
            <jsp:include page="layout/header.jsp" />

            <!-- Real-Time Municipal Policy Evaluator -->
            <div class="card mb-4 border-primary shadow-sm" id="evaluator">
                <div class="card-header bg-primary text-white d-flex flex-wrap justify-content-between align-items-center gap-2 py-3">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-cpu-fill me-2"></i>Real-Time Municipal Policy Evaluator
                    </h5>
                    <span class="badge bg-white text-primary fw-semibold px-3 py-2"><i
                            class="bi bi-activity me-1"></i>Live Zone Monitoring</span>
                </div>
                <div class="card-body p-4">
                    <p class="text-muted small mb-3">
                        Select active environmental telemetry and zone signals to test municipal policy compliance
                        across the urban sector.
                    </p>
                    <form action="${pageContext.request.contextPath}/interpreter" method="post">
                        <input type="hidden" name="action" value="evaluateConditions">

                        <div class="row g-3 mb-4">
                            <c:forEach var="kw" items="${keywords}">
                                <c:set var="sensorLabel" value="${kw}" />
                                <c:set var="sensorDesc" value="Environmental sensor signal" />
                                <c:set var="sensorIcon" value="bi-broadcast-pin" />
                                <c:choose>
                                    <c:when test="${kw == 'HIGH_NOISE'}">
                                        <c:set var="sensorLabel" value="High Noise Level (>85 dB)" />
                                        <c:set var="sensorDesc" value="Acoustic sensor threshold exceeded" />
                                        <c:set var="sensorIcon" value="bi-soundwave" />
                                    </c:when>
                                    <c:when test="${kw == 'RESTRICTED_HOURS'}">
                                        <c:set var="sensorLabel" value="Nighttime Restricted Hours" />
                                        <c:set var="sensorDesc"
                                            value="Designated municipal quiet period (10 PM - 6 AM)" />
                                        <c:set var="sensorIcon" value="bi-moon-stars-fill" />
                                    </c:when>
                                    <c:when test="${kw == 'HAZARDOUS_SMOKE'}">
                                        <c:set var="sensorLabel" value="Smoke / Emission Alert" />
                                        <c:set var="sensorDesc" value="Air quality and particulate warning" />
                                        <c:set var="sensorIcon" value="bi-cloud-haze2-fill" />
                                    </c:when>
                                    <c:when test="${kw == 'ILLEGAL_PARKING'}">
                                        <c:set var="sensorLabel" value="Unauthorized Roadside Parking" />
                                        <c:set var="sensorDesc" value="Vehicle obstruction in red or fire lane" />
                                        <c:set var="sensorIcon" value="bi-sign-stop-fill" />
                                    </c:when>
                                    <c:when test="${kw == 'EMERGENCY_ZONE'}">
                                        <c:set var="sensorLabel" value="Emergency Priority Zone" />
                                        <c:set var="sensorDesc" value="Hospital or civil defense transit corridor" />
                                        <c:set var="sensorIcon" value="bi-shield-exclamation" />
                                    </c:when>
                                </c:choose>

                                <div class="col-md-6 col-xl">
                                    <div
                                        class="card h-100 border sensor-card <c:if test='${sessionScope.sensorContext != null && sessionScope.sensorContext.contains(kw)}'>sensor-card-active border-primary</c:if>">
                                        <label
                                            class="card-body p-3 d-flex flex-column justify-content-between h-100 cursor-pointer mb-0"
                                            for="kw_${kw}">
                                            <div class="d-flex align-items-center justify-content-between mb-2">
                                                <div class="rounded-circle p-2 bg-primary-subtle text-primary d-inline-flex align-items-center justify-content-center"
                                                    style="width: 36px; height: 36px;">
                                                    <i class="bi ${sensorIcon}"></i>
                                                </div>
                                                <input class="form-check-input mt-0 fs-5 sensor-checkbox"
                                                    type="checkbox" name="activeKeywords" value="${kw}" id="kw_${kw}"
                                                    <c:if
                                                    test="${sessionScope.sensorContext != null && sessionScope.sensorContext.contains(kw)}">checked
                                                </c:if>>
                                            </div>
                                            <div>
                                                <div class="fw-bold text-dark fs-6">${sensorLabel}</div>
                                                <div class="text-muted small mt-1"
                                                    style="font-size: 0.76rem; line-height: 1.3;">${sensorDesc}</div>
                                            </div>
                                        </label>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                            <button type="submit" class="btn btn-primary px-4 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-play-circle-fill me-1"></i> Evaluate Active Rules
                            </button>
                            <c:if test="${not empty sessionScope.sensorContext}">
                                <div class="d-flex align-items-center flex-wrap gap-2 p-2 bg-light rounded border">
                                    <span class="text-secondary small fw-bold text-uppercase me-1"
                                        style="letter-spacing: 0.5px;">Active Sensor Signals:</span>
                                    <c:set var="hasAnySignal" value="false" />
                                    <c:forEach var="kw" items="${keywords}">
                                        <c:if test="${sessionScope.sensorContext.contains(kw)}">
                                            <c:set var="hasAnySignal" value="true" />
                                            <span
                                                class="badge rounded-pill bg-primary text-white px-3 py-2 fw-medium shadow-sm">
                                                <c:choose>
                                                    <c:when test="${kw == 'HIGH_NOISE'}"><i
                                                            class="bi bi-soundwave me-1"></i>High Noise Level (&gt;85
                                                        dB)</c:when>
                                                    <c:when test="${kw == 'RESTRICTED_HOURS'}"><i
                                                            class="bi bi-moon-stars-fill me-1"></i>Nighttime Restricted
                                                        Hours</c:when>
                                                    <c:when test="${kw == 'HAZARDOUS_SMOKE'}"><i
                                                            class="bi bi-cloud-haze2-fill me-1"></i>Smoke / Emission
                                                        Alert</c:when>
                                                    <c:when test="${kw == 'ILLEGAL_PARKING'}"><i
                                                            class="bi bi-sign-stop-fill me-1"></i>Unauthorized Roadside
                                                        Parking</c:when>
                                                    <c:when test="${kw == 'EMERGENCY_ZONE'}"><i
                                                            class="bi bi-shield-exclamation me-1"></i>Emergency Priority
                                                        Zone</c:when>
                                                    <c:otherwise>${kw}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hasAnySignal}">
                                        <span class="badge rounded-pill bg-secondary-subtle text-secondary px-3 py-2">No
                                            active signals</span>
                                    </c:if>
                                </div>
                            </c:if>
                        </div>
                    </form>

                    <!-- Evaluation Results Display -->
                    <c:if test="${not empty sessionScope.evalResults}">
                        <div class="mt-4 pt-3 border-top">
                            <h6 class="fw-bold mb-3 text-secondary text-uppercase small" style="letter-spacing: 0.5px;">
                                <i class="bi bi-clipboard-check-fill me-2 text-primary"></i>Compliance Evaluation
                                Results
                            </h6>
                            <div class="row g-3">
                                <c:forEach var="res" items="${sessionScope.evalResults}">

                                    <div class="col-md-6 col-xl-4">
                                        <div
                                            class="card h-100 border-${res.violationTriggered ? 'danger' : 'success'} shadow-sm">
                                            <div class="card-body">
                                                <div class="d-flex justify-content-between align-items-center mb-2">
                                                    <h6 class="fw-bold mb-0 text-dark">${res.ruleName}</h6>
                                                    <span
                                                        class="badge ${res.violationTriggered ? 'bg-danger' : 'bg-success'} px-2 py-1">
                                                        <i
                                                            class="bi ${res.violationTriggered ? 'bi-exclamation-triangle-fill' : 'bi-check-circle-fill'} me-1"></i>
                                                        ${res.violationTriggered ? 'VIOLATION' : 'COMPLIANT'}
                                                    </span>
                                                </div>
                                                <div
                                                    class="small text-muted mb-2 d-flex align-items-center gap-1 flex-wrap">
                                                    <span class="fw-semibold text-secondary me-1">Trigger
                                                        Conditions:</span>
                                                    <jsp:include page="layout/rule-badges.jsp">
                                                        <jsp:param name="expr" value="${res.ruleText}" />
                                                    </jsp:include>
                                                </div>
                                                <div
                                                    class="small ${res.violationTriggered ? 'text-danger' : 'text-success'} fw-medium">
                                                    ${res.explanation}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                        <c:remove var="evalResults" scope="session" />
                    </c:if>
                </div>
            </div>

            <!-- Main Two-Column Layout (38% Left Form, 62% Right Table) -->
            <div class="policy-main-grid mb-4">
                <!-- Configure New Policy Rule (Left Form Card) -->
                <div class="card shadow-sm">
                    <div class="card-header bg-white py-3">
                        <h5 class="mb-0 fw-bold"><i class="bi bi-file-earmark-plus me-2 text-primary"></i>Configure New
                            Policy Rule</h5>
                    </div>
                    <div class="card-body policy-form-body">
                        <form action="${pageContext.request.contextPath}/interpreter" method="post" id="policyRuleForm">
                            <input type="hidden" name="action" value="addRule">

                            <div class="form-group">
                                <label class="form-label fw-semibold" for="ruleNameInput">Rule Title</label>
                                <input type="text" id="ruleNameInput" name="ruleName" class="form-control"
                                    placeholder="e.g. Hazardous Fire Alert Rule" required maxlength="100"
                                    pattern=".*\S.*" title="Please enter a rule title."
                                    value="<c:out value='${param.ruleName}'/>">
                            </div>

                            <div class="form-group">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <label class="form-label fw-semibold mb-0" for="ruleTextInput">Rule
                                        Expression</label>
                                    <button type="button"
                                        class="btn btn-link btn-sm text-decoration-none p-0 text-muted"
                                        onclick="clearRuleExpression()" title="Clear input">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Clear
                                    </button>
                                </div>
                                <input type="text" id="ruleTextInput" name="ruleText" class="form-control"
                                    placeholder="e.g. (HAZARDOUS_SMOKE OR HIGH_NOISE) AND EMERGENCY_ZONE" required
                                    maxlength="300" pattern=".*\S.*"
                                    title="Please build a rule using the conditions and operators below."
                                    autocomplete="off" value="<c:out value='${param.ruleText}'/>">
                                <div class="form-text text-muted small mt-1">
                                    Supported logical operators: <span
                                        class="badge bg-secondary-subtle text-secondary-emphasis">AND</span>, <span
                                        class="badge bg-secondary-subtle text-secondary-emphasis">OR</span>, <span
                                        class="badge bg-secondary-subtle text-secondary-emphasis">NOT</span>, and
                                    parentheses <span class="badge bg-secondary-subtle text-secondary-emphasis">(
                                        )</span>.
                                </div>
                            </div>

                            <!-- Clickable Builder Section -->
                            <div class="policy-builder">
                                <span class="policy-builder-title">
                                    <i class="bi bi-cursor-fill text-primary me-1"></i>Click to insert a condition or
                                    operator
                                </span>

                                <div class="chip-group">
                                    <button type="button" class="builder-chip" onclick="insertToken('HIGH_NOISE')">
                                        <i class="bi bi-plus-lg"></i>High Noise Level
                                    </button>
                                    <button type="button" class="builder-chip"
                                        onclick="insertToken('RESTRICTED_HOURS')">
                                        <i class="bi bi-plus-lg"></i>Nighttime Restricted Hours
                                    </button>
                                    <button type="button" class="builder-chip"
                                        onclick="insertToken('HAZARDOUS_SMOKE')">
                                        <i class="bi bi-plus-lg"></i>Smoke / Emission Alert
                                    </button>
                                    <button type="button" class="builder-chip"
                                        onclick="insertToken('ILLEGAL_PARKING')">
                                        <i class="bi bi-plus-lg"></i>Unauthorized Parking
                                    </button>
                                    <button type="button" class="builder-chip"
                                        onclick="insertToken('EMERGENCY_ZONE')">
                                        <i class="bi bi-plus-lg"></i>Emergency Priority Zone
                                    </button>
                                </div>

                                <!-- Operators: five separate, evenly spaced pill buttons -->
                                <div class="operator-row">
                                    <span class="operator-row-label">Operators:</span>
                                    <div class="operator-pills">
                                        <button type="button" class="operator-pill"
                                            onclick="insertToken('AND')">AND</button>
                                        <button type="button" class="operator-pill"
                                            onclick="insertToken('OR')">OR</button>
                                        <button type="button" class="operator-pill"
                                            onclick="insertToken('NOT')">NOT</button>
                                        <button type="button" class="operator-pill"
                                            onclick="insertToken('(')">(</button>
                                        <button type="button" class="operator-pill"
                                            onclick="insertToken(')')">)</button>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label fw-semibold" for="descriptionInput">Description</label>
                                <textarea id="descriptionInput" name="description" class="form-control" rows="2"
                                    maxlength="500"
                                    placeholder="Describe the municipal regulation and enforcement action..."><c:out value='${param.description}'/></textarea>
                            </div>

                            <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold shadow-sm">
                                <i class="bi bi-check-lg me-1"></i> Save Policy Rule
                            </button>
                        </form>

                        <!-- Inline error toast: only rendered when errorMessage != null -->
                        <c:if test="${errorMessage != null}">
                            <div class="error-toast" role="alert"
                                style="display: flex; align-items: flex-start; gap: 10px; margin-top: 16px; padding: 12px 14px; background-color: #fef2f2; border: 1px solid #fca5a5; border-radius: 8px; color: #991b1b; font-size: 0.875rem; font-weight: 500; line-height: 1.45;">
                                <i class="bi bi-exclamation-circle-fill error-toast-icon" style="color: #dc2626; font-size: 1rem; line-height: 1.4; flex-shrink: 0;"></i>
                                <div class="error-toast-text"><c:out value="${errorMessage}" /></div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <!-- Configured Policy Rules (Right Table Card) -->
                <div class="card shadow-sm">
                    <div class="card-header bg-white py-3 d-flex flex-wrap justify-content-between align-items-center gap-2">
                        <h5 class="mb-0 fw-bold"><i class="bi bi-journal-text me-2 text-primary"></i>Configured Policy
                            Rules</h5>
                        <span class="badge bg-light text-dark border px-3 py-2 fw-semibold">${rules.size()} Active
                            Policies</span>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive table-scroll-vertical" style="max-height: 460px;">
                            <table class="table table-hover align-middle mb-0 policy-table">
                                <thead class="table-light">
                                    <tr>
                                        <th class="col-name ps-3" style="width: 30%; white-space: nowrap;">RULE NAME</th>
                                        <th class="col-conditions" style="width: 45%; white-space: nowrap;">TRIGGER CONDITIONS</th>
                                        <th class="col-status text-center" style="width: 10%; white-space: nowrap;">STATUS</th>
                                        <th class="col-actions pe-3 text-end" style="width: 15%; white-space: nowrap;">ACTIONS</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${rules}">
                                        <tr>
                                            <td class="col-name-cell ps-3" style="width: 30%; word-break: break-word;">
                                                <div class="fw-bold text-dark" style="line-height: 1.35;">
                                                    <c:out value="${r.ruleName}" /></div>
                                                <c:if test="${not empty r.description}">
                                                    <div class="small text-muted mt-1"
                                                        style="font-size: 0.78rem; line-height: 1.35;">
                                                        <c:out
                                                            value="${fn:replace(r.description, 'Composite rule combining', 'Multi-condition rule combining')}" />
                                                    </div>
                                                </c:if>
                                            </td>
                                            <td style="width: 45%;">
                                                <c:set var="spacedExpr" value="${fn:replace(fn:replace(r.ruleText, '(', ' ( '), ')', ' ) ')}" />
                                                <div class="cond-badges">
                                                    <c:forTokens items="${spacedExpr}" delims=" " var="tok">
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
                                            </td>
                                            <td class="text-center" style="width: 10%; white-space: nowrap;">
                                                <span
                                                    class="badge ${r.active ? 'bg-success' : 'bg-secondary'} px-2 py-1 fw-semibold"
                                                    style="font-size: 0.75rem;">
                                                    ${r.active ? 'Active' : 'Disabled'}
                                                </span>
                                            </td>
                                            <td class="pe-3 text-end" style="width: 15%; white-space: nowrap;">
                                                <div class="action-buttons">
                                                    <form action="${pageContext.request.contextPath}/interpreter"
                                                        method="post">
                                                        <input type="hidden" name="action" value="toggleRule">
                                                        <input type="hidden" name="id" value="${r.id}">
                                                        <input type="hidden" name="active" value="${r.active}">
                                                        <button type="submit"
                                                            class="btn btn-sm btn-outline-${r.active ? 'warning' : 'success'}"
                                                            title="${r.active ? 'Deactivate' : 'Activate'}">
                                                            <i
                                                                class="bi bi-${r.active ? 'pause-fill' : 'play-fill'}"></i>
                                                        </button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/interpreter"
                                                        method="post"
                                                        data-confirm-title="Delete Policy Rule"
                                                        data-confirm-message="Are you sure you want to delete the policy rule '${fn:escapeXml(r.ruleName)}'? This action cannot be undone."
                                                        data-confirm-btn="Delete Rule">
                                                        <input type="hidden" name="action" value="deleteRule">
                                                        <input type="hidden" name="id" value="${r.id}">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                                            title="Delete">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <script>
                function insertToken(token) {
                    const input = document.getElementById('ruleTextInput');
                    if (!input) return;
                    let val = input.value;

                    if (token === '(') {
                        if (val.length > 0 && !val.endsWith(' ') && !val.endsWith('(')) {
                            val += ' ';
                        }
                        val += '(';
                    } else if (token === ')') {
                        val = val.trimEnd() + ')';
                    } else if (token === 'AND' || token === 'OR') {
                        val = val.trimEnd();
                        if (val.length > 0) {
                            val += ' ' + token + ' ';
                        } else {
                            val = token + ' ';
                        }
                    } else if (token === 'NOT') {
                        val = val.trimEnd();
                        if (val.length > 0 && !val.endsWith('(')) {
                            val += ' NOT ';
                        } else if (val.endsWith('(')) {
                            val += 'NOT ';
                        } else {
                            val = 'NOT ';
                        }
                    } else {
                        // Sensor keyword (HIGH_NOISE, etc.)
                        val = val.trimEnd();
                        if (val.length > 0 && !val.endsWith('(')) {
                            val += ' ' + token;
                        } else if (val.endsWith('(')) {
                            val += token;
                        } else {
                            val = token;
                        }
                    }
                    input.value = val;
                    input.focus();
                }

                function clearRuleExpression() {
                    const input = document.getElementById('ruleTextInput');
                    if (input) {
                        input.value = '';
                        input.focus();
                    }
                }

                function bindSensorCheckboxes() {
                    document.querySelectorAll('.sensor-checkbox').forEach(function (checkbox) {
                        checkbox.onchange = function () {
                            const card = this.closest('.sensor-card');
                            if (card) {
                                if (this.checked) {
                                    card.classList.add('sensor-card-active', 'border-primary');
                                } else {
                                    card.classList.remove('sensor-card-active', 'border-primary');
                                }
                            }
                        };
                    });
                }
                bindSensorCheckboxes();
            </script>

            <jsp:include page="layout/footer.jsp" />
