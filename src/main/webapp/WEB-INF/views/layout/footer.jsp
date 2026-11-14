<%@ page contentType="text/html;charset=UTF-8" language="java" %>
            </div> <!-- End content-body -->

            <footer class="py-3 px-4 text-center text-muted small border-top bg-white">
                <div>
                    <strong>SmartCity360</strong> &mdash; UrbanTech Solutions Infrastructure Management Platform.
                </div>
            </footer>
        </div> <!-- End main-wrapper -->
    </div> <!-- End app-layout -->
</div> <!-- End container-fluid -->

<!-- Dedicated Print Report Container -->
<div id="printReportArea"></div>

<!-- Reusable In-App Confirmation Modal (Replaces browser localhost confirm popups) -->
<div class="modal fade" id="sc360ConfirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content border-0 shadow-lg" style="border-radius: 0.85rem;">
            <div class="modal-body p-4 text-center">
                <div id="sc360ConfirmIconWrap"
                     class="mx-auto mb-3 d-inline-flex align-items-center justify-content-center rounded-circle bg-danger-subtle text-danger"
                     style="width: 48px; height: 48px; font-size: 1.35rem;">
                    <i id="sc360ConfirmIcon" class="bi bi-trash3-fill"></i>
                </div>
                <h6 class="fw-bold text-dark mb-2" id="sc360ConfirmTitle">Confirm Action</h6>
                <p class="text-muted small mb-4" id="sc360ConfirmMessage">Are you sure you want to proceed?</p>
                <div class="d-flex gap-2 justify-content-center">
                    <button type="button" class="btn btn-sm btn-light border px-3 flex-grow-1" data-bs-dismiss="modal">
                        Cancel
                    </button>
                    <button type="button" class="btn btn-sm btn-danger px-3 flex-grow-1 fw-semibold" id="sc360ConfirmProceedBtn">
                        Delete
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap 5 Bundle with Popper JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
function toggleSidebar() {
    if (window.innerWidth < 992) {
        document.body.classList.toggle('sidebar-mobile-open');
    } else {
        const collapsed = document.body.classList.toggle('sidebar-collapsed');
        try {
            localStorage.setItem('sc360_sidebar_collapsed', collapsed ? 'true' : 'false');
        } catch (e) {}
    }
}

function closeMobileSidebar() {
    document.body.classList.remove('sidebar-mobile-open');
}

document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        closeMobileSidebar();
    }
});

window.addEventListener('resize', function () {
    if (window.innerWidth >= 992) {
        document.body.classList.remove('sidebar-mobile-open');
    }
});

function printTableReport(tableId, reportTitle, subtitle) {
    const sourceTable = document.getElementById(tableId);
    const printArea = document.getElementById('printReportArea');
    if (!sourceTable || !printArea) {
        window.print();
        return;
    }

    const clonedTable = sourceTable.cloneNode(true);
    clonedTable.removeAttribute('id');

    // Remove columns marked .no-print
    clonedTable.querySelectorAll('.no-print').forEach(function (el) {
        el.remove();
    });

    // Remove hidden rows (filtered out by search)
    let visibleCount = 0;
    clonedTable.querySelectorAll('tbody tr').forEach(function (row) {
        if (row.style.display === 'none' || row.classList.contains('empty-search-row')) {
            row.remove();
        } else {
            visibleCount++;
        }
    });

    const nowStr = new Date().toLocaleString();
    printArea.innerHTML = '';

    const headerDiv = document.createElement('div');
    headerDiv.className = 'print-header';
    headerDiv.innerHTML =
        '<div>' +
            '<div style="font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em; color: #475569; font-weight: 700;">SmartCity360 &mdash; UrbanTech Solutions</div>' +
            '<h3 style="margin: 0.15rem 0 0 0; font-weight: 700; color: #0f172a;">' + reportTitle + '</h3>' +
            (subtitle ? '<div style="font-size: 0.85rem; color: #475569; margin-top: 0.15rem;">' + subtitle + '</div>' : '') +
        '</div>' +
        '<div style="text-align: right; font-size: 0.8rem; color: #475569;">' +
            '<div><strong>Generated:</strong> ' + nowStr + '</div>' +
            '<div><strong>Total Records:</strong> ' + visibleCount + '</div>' +
        '</div>';

    printArea.appendChild(headerDiv);
    printArea.appendChild(clonedTable);

    document.body.classList.add('printing-report');
    window.print();
    setTimeout(function () {
        document.body.classList.remove('printing-report');
    }, 300);
}

/* ---------- Custom Confirmation Modal & Background AJAX Updates ---------- */
(function () {
    let pendingConfirmForm = null;
    let pendingSubmitter = null;

    const confirmModalEl = document.getElementById('sc360ConfirmModal');
    const confirmProceedBtn = document.getElementById('sc360ConfirmProceedBtn');

    if (confirmProceedBtn && confirmModalEl) {
        confirmProceedBtn.addEventListener('click', function () {
            const modalInstance = bootstrap.Modal.getInstance(confirmModalEl);
            if (modalInstance) {
                modalInstance.hide();
            }
            if (pendingConfirmForm) {
                const formToSubmit = pendingConfirmForm;
                const sub = pendingSubmitter;
                pendingConfirmForm = null;
                pendingSubmitter = null;
                submitFormAjax(formToSubmit, sub);
            }
        });
    }

    document.addEventListener('submit', function (e) {
        const form = e.target;
        if (!form || !form.closest('.content-body') || form.hasAttribute('data-no-ajax')) {
            return;
        }

        const confirmMsg = form.getAttribute('data-confirm-message');
        if (confirmMsg) {
            e.preventDefault();
            pendingConfirmForm = form;
            pendingSubmitter = e.submitter || null;

            const title = form.getAttribute('data-confirm-title') || 'Confirm Action';
            const btnText = form.getAttribute('data-confirm-btn') || 'Delete';
            const variant = form.getAttribute('data-confirm-variant') || 'danger';

            document.getElementById('sc360ConfirmTitle').textContent = title;
            document.getElementById('sc360ConfirmMessage').textContent = confirmMsg;
            confirmProceedBtn.textContent = btnText;

            const iconWrap = document.getElementById('sc360ConfirmIconWrap');
            const icon = document.getElementById('sc360ConfirmIcon');
            if (variant === 'warning') {
                confirmProceedBtn.className = 'btn btn-sm btn-warning text-dark px-3 flex-grow-1 fw-semibold';
                iconWrap.className = 'mx-auto mb-3 d-inline-flex align-items-center justify-content-center rounded-circle bg-warning-subtle text-warning';
                icon.className = 'bi bi-exclamation-triangle-fill';
            } else {
                confirmProceedBtn.className = 'btn btn-sm btn-danger px-3 flex-grow-1 fw-semibold';
                iconWrap.className = 'mx-auto mb-3 d-inline-flex align-items-center justify-content-center rounded-circle bg-danger-subtle text-danger';
                icon.className = 'bi bi-trash3-fill';
            }

            bootstrap.Modal.getOrCreateInstance(confirmModalEl).show();
            return;
        }

        e.preventDefault();
        submitFormAjax(form, e.submitter || null);
    });

    document.addEventListener('click', function (e) {
        const ajaxLink = e.target.closest('a[data-ajax-action="true"]');
        if (ajaxLink && ajaxLink.href) {
            e.preventDefault();
            fetchAndSwapContent(ajaxLink.href, { method: 'GET', credentials: 'same-origin' }, ajaxLink);
        }
    });

    function captureUiState() {
        const state = {
            winScrollY: window.scrollY,
            scrollContainers: [],
            inputs: {}
        };
        document.querySelectorAll('.table-scroll-vertical, .zone-tree-scroll').forEach(function (el, idx) {
            state.scrollContainers.push({ idx: idx, top: el.scrollTop, left: el.scrollLeft });
        });
        [
            'telemetrySearchInput', 'telemetrySortSelect',
            'zoneSearchInput', 'zoneSortSelect',
            'complaintSearchInput', 'complaintSortSelect',
            'lightSearchInput', 'lightSortSelect',
            'testSearch'
        ].forEach(function (id) {
            const el = document.getElementById(id);
            if (el) {
                state.inputs[id] = el.value;
            }
        });
        return state;
    }

    function restoreUiState(state) {
        if (!state) return;

        Object.keys(state.inputs).forEach(function (id) {
            const el = document.getElementById(id);
            if (el && state.inputs[id] !== undefined) {
                el.value = state.inputs[id];
            }
        });

        if (typeof filterAndSortTelemetry === 'function' && document.getElementById('telemetrySearchInput')) {
            filterAndSortTelemetry();
        }
        if (typeof filterAndSortZoneTree === 'function' && document.getElementById('zoneSearchInput')) {
            filterAndSortZoneTree();
        }
        if (typeof filterAndSortComplaints === 'function' && document.getElementById('complaintSearchInput')) {
            filterAndSortComplaints();
        }
        if (typeof filterAndSortLights === 'function' && document.getElementById('lightSearchInput')) {
            filterAndSortLights();
        }
        if (typeof applyTestFilters === 'function' && document.getElementById('testSearch')) {
            applyTestFilters();
        }

        const containers = document.querySelectorAll('.table-scroll-vertical, .zone-tree-scroll');
        state.scrollContainers.forEach(function (saved) {
            if (containers[saved.idx]) {
                containers[saved.idx].scrollTop = saved.top;
                containers[saved.idx].scrollLeft = saved.left;
            }
        });

        window.scrollTo({ top: state.winScrollY, behavior: 'instant' });
    }

    function cleanupOpenModals() {
        document.querySelectorAll('.content-body .modal.show').forEach(function (modalEl) {
            const inst = bootstrap.Modal.getInstance(modalEl);
            if (inst) {
                inst.hide();
            }
        });
        document.querySelectorAll('.modal-backdrop').forEach(function (bd) {
            bd.remove();
        });
        document.body.classList.remove('modal-open');
        document.body.style.removeProperty('overflow');
        document.body.style.removeProperty('padding-right');
    }

    function submitFormAjax(form, submitter) {
        const formData = new FormData(form);
        if (submitter && submitter.name) {
            formData.append(submitter.name, submitter.value);
        }
        const params = new URLSearchParams();
        for (const pair of formData.entries()) {
            params.append(pair[0], pair[1]);
        }

        const method = (form.getAttribute('method') || 'POST').toUpperCase();
        const actionUrl = form.getAttribute('action') || window.location.href;
        const btn = submitter || form.querySelector('button[type="submit"]');

        const fetchOptions = {
            method: method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        };
        if (method !== 'GET') {
            fetchOptions.body = params.toString();
        }

        fetchAndSwapContent(actionUrl, fetchOptions, btn);
    }

    function fetchAndSwapContent(url, fetchOptions, triggerBtn) {
        const uiState = captureUiState();

        if (triggerBtn) {
            triggerBtn.disabled = true;
        }

        fetch(url, fetchOptions)
            .then(function (response) {
                if (response.redirected && response.url && response.url.indexOf('/login') !== -1) {
                    window.location.href = response.url;
                    return null;
                }
                return response.text();
            })
            .then(function (html) {
                if (!html) return;
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newContent = doc.querySelector('.content-body');
                const currentContent = document.querySelector('.content-body');

                if (newContent && currentContent) {
                    cleanupOpenModals();
                    currentContent.innerHTML = newContent.innerHTML;

                    // Re-execute inline scripts inside .content-body
                    currentContent.querySelectorAll('script').forEach(function (oldScript) {
                        const newScript = document.createElement('script');
                        newScript.textContent = oldScript.textContent;
                        oldScript.parentNode.replaceChild(newScript, oldScript);
                    });

                    restoreUiState(uiState);
                } else {
                    window.location.reload();
                }
            })
            .catch(function () {
                if (triggerBtn) {
                    triggerBtn.disabled = false;
                }
            });
    }
})();
</script>
</body>
</html>

