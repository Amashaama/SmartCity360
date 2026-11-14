<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Sign In - SmartCity360 Management Platform</title>


            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
            <!-- Bootstrap Icons -->
            <link rel="stylesheet"
                href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
            <!-- Custom Application Styling -->
            <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=5">
        </head>

        <body class="login-page-body">
            <div class="login-wrapper">
                <div class="login-card card border-0 shadow-lg">

                    <div class="login-card-header text-center">
                        <div class="login-brand-icon mx-auto mb-2">
                            <i class="bi bi-buildings-fill"></i>
                        </div>
                        <h4 class="fw-bold text-white mb-1">SmartCity 360</h4>
                        <div class="small text-white-50">UrbanTech Solutions &mdash; Operations Console</div>
                    </div>

                    <!-- Form Body -->
                    <div class="card-body p-4 p-sm-4">
                        <h5 class="fw-bold text-dark mb-1">Sign In</h5>


                        <!-- <c:if test="${param.logout == '1'}">
                <div class="alert alert-info py-2 px-3 small d-flex align-items-center gap-2" role="alert">
                    <i class="bi bi-check-circle-fill text-info"></i>
                    <span>You have been signed out safely.</span>
                </div>
            </c:if> -->

                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger py-2 px-3 small d-flex align-items-center gap-2"
                                role="alert">
                                <i class="bi bi-exclamation-triangle-fill text-danger"></i>
                                <span>
                                    <c:out value="${errorMessage}" />
                                </span>
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="on">
                            <div class="mb-3">
                                <label for="usernameInput" class="form-label small fw-semibold">Username</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light text-muted"><i
                                            class="bi bi-person"></i></span>
                                    <input type="text" id="usernameInput" name="username" class="form-control"
                                        placeholder="Enter username" required autofocus maxlength="60">
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="passwordInput" class="form-label small fw-semibold">Password</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light text-muted"><i class="bi bi-lock"></i></span>
                                    <input type="password" id="passwordInput" name="password" class="form-control"
                                        placeholder="Enter password" required maxlength="100">
                                    <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePasswordVisibility()" title="Show/Hide Password"
                                        aria-label="Show/Hide Password">
                                        <i class="bi bi-eye" id="togglePwdIcon"></i>
                                    </button>
                                </div>
                            </div>

                            <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold mt-1">
                                <i class="bi bi-box-arrow-in-right me-1"></i> Sign In to Console
                            </button>
                        </form>


                    </div>
                </div>
            </div>

            <script>
                function togglePasswordVisibility() {
                    const pwd = document.getElementById('passwordInput');
                    const icon = document.getElementById('togglePwdIcon');
                    if (pwd.type === 'password') {
                        pwd.type = 'text';
                        icon.className = 'bi bi-eye-slash';
                    } else {
                        pwd.type = 'password';
                        icon.className = 'bi bi-eye';
                    }
                }

                function fillCredentials(user, pass) {
                    document.getElementById('usernameInput').value = user;
                    document.getElementById('passwordInput').value = pass;
                    document.getElementById('passwordInput').focus();
                }
            </script>
        </body>

        </html>