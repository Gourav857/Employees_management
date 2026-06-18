// js/auth.js - Enterprise Production Authentication Engine

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault(); // Page refresh rokne ke liye

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    console.log("Attempting JWT-based login pipeline for:", email);

    // HIGH INDUSTRY CLIENT-SIDE VALIDATION
    if (!email || !password) {
        window.showToast("Please enter both email and password credentials.", "warning");
        return;
    }

    // Email Pattern Structural Regex Check
    const cleanEmailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!cleanEmailRegex.test(email)) {
        window.showToast("Please provide a valid corporate email structure.", "warning");
        return;
    }

    try {
        // PRODUCTION FIX: Target cloud Render server route engine
        const response = await fetch('https://employees-management-lzfy.onrender.com/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();
        console.log("Backend Authorization Response Data:", data);

        if (response.ok) {
            window.showToast("Authentication successful! Loading your dashboard...", "success");

            // Multi-tab conflict safe allocation
            sessionStorage.setItem("token", data.token);
            sessionStorage.setItem("userEmail", data.email);
            sessionStorage.setItem("userRole", data.role);

            const userRole = data.role ? data.role.trim() : "";

            // Delayed redirection to let user see the success animation cleanly
            setTimeout(() => {
                if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
                    window.location.href = './admin-dashboard.html';
                } else if (userRole === 'ROLE_EMPLOYEE' || userRole === 'EMPLOYEE') {
                    window.location.href = './emp-dashboard.html';
                } else {
                    console.error("Unknown role framework received:", userRole);
                    window.showToast("Security Exception: Role not recognized by the system.", "error");
                }
            }, 800);

        } else {
            window.showToast(data.error || "Invalid username or password configuration.", "error");
        }
    } catch (error) {
        console.error('Fetch System Error Context:', error);
        window.showToast("Network Error: Server communication channel dropped.", "error");
    }
});