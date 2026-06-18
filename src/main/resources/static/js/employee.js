// js/employee.js - Enterprise Employee Portal Control Pipeline

let currentEmployeeId = null;

document.addEventListener('DOMContentLoaded', () => {
    console.log("--- Employee Dashboard Loaded! Fetching Profile... ---");
    fetchProfile();

    const email = sessionStorage.getItem("userEmail") || "rahul@ems.com";

    try {
        const eventSource = new EventSource(`/api/notifications/subscribe/${email}`);

        eventSource.addEventListener("REFRESH", (event) => {
            console.log("⚡ [REAL-TIME EVENT] Admin processed action! Syncing history layout dynamically...");
            fetchLeaveHistory();
        });

        eventSource.addEventListener("INIT", (event) => {
            console.log("🚀 SSE Channel Active for Employee:", event.data);
        });

        eventSource.onerror = () => {
            console.warn("Employee portal SSE connection dropped, running on standard execution thread.");
        };
    } catch (err) {
        console.error("SSE registration error context:", err);
    }
});

// 1. FETCH PROFILE FUNCTION
async function fetchProfile() {
    try {
        const email = sessionStorage.getItem("userEmail") || "rahul@ems.com";
        const token = sessionStorage.getItem("token");

        const response = await fetch(`/api/employee/profile?email=${email}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error(`HTTP Error! Status: ${response.status}`);

        const emp = await response.json();
        currentEmployeeId = emp.id;

        if (document.getElementById('profile-name')) {
            document.getElementById('profile-name').textContent = emp.user ? `${emp.user.firstName} ${emp.user.lastName}` : 'N/A';
            document.getElementById('profile-email').textContent = emp.user ? emp.user.email : 'N/A';
            document.getElementById('profile-dept').textContent = emp.department || 'N/A';
            document.getElementById('profile-desg').textContent = emp.designation || 'N/A';
            document.getElementById('profile-salary').textContent = `₹${emp.salary || '0'}`;
        }

        fetchLeaveHistory();
    } catch (error) {
        console.error("Error in fetchProfile:", error);
        window.showToast("Failed to initialize user security profile data.", "error");
    }
}

// 2. SUBMIT LEAVE APPLICATION (WITH ADVANCED INPUT DATE VALIDATION)
const leaveForm = document.getElementById('leave-form');
if (leaveForm) {
    leaveForm.replaceWith(leaveForm.cloneNode(true));

    document.getElementById('leave-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const reason = document.getElementById('reason').value.trim();
        const token = sessionStorage.getItem("token");

        // HIGH INDUSTRY INPUT DATE COGNITIVE VALIDATION
        if (!startDate || !endDate || !reason) {
            window.showToast("All application parameters are mandatory.", "warning");
            return;
        }

        const startObj = new Date(startDate);
        const endObj = new Date(endDate);
        const todayObj = new Date();
        todayObj.setHours(0,0,0,0); // Clear timestamp limits for structural checking

        if (startObj < todayObj) {
            window.showToast("Time Validation Error: Leave start date cannot be in the past.", "warning");
            return;
        }

        if (endObj < startObj) {
            window.showToast("Time Validation Error: End date cannot be chronologically before the start date.", "warning");
            return;
        }

        const leaveData = {
            startDate: startDate,
            endDate: endDate,
            reason: reason,
            status: "PENDING"
        };

        try {
            const response = await fetch('/api/employee/leave/apply', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(leaveData)
            });

            if (response.ok) {
                window.showToast("Leave application submitted successfully for review!", "success");
                document.getElementById('leave-form').reset();
                fetchLeaveHistory();
            } else {
                // Intercept unified ErrorResponse objects from centralized backend handler
                const data = await response.json();
                window.showToast(`Request Rejected: ${data.error || 'Server validation failed'}`, "error");
            }
        } catch (error) {
            console.error("Error submitting leave:", error);
            window.showToast("Network Error: Failed to transmit data payload.", "error");
        }
    });
}

// 3. FETCH LEAVE HISTORY
async function fetchLeaveHistory() {
    try {
        const token = sessionStorage.getItem("token");
        if (!token) return;

        const cacheBuster = new Date().getTime();

        const response = await fetch(`/api/employee/leave/history?t=${cacheBuster}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error(`HTTP Error Status: ${response.status}`);

        const leaves = await response.json();
        const tbody = document.querySelector('#leave-history-table tbody') || document.querySelectorAll('table')[0].querySelector('tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (!leaves || leaves.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-center py-4 text-slate-500">No leave history found.</td></tr>`;
            return;
        }

        leaves.forEach(leave => {
            const tr = document.createElement('tr');
            tr.className = "border-b border-slate-100 hover:bg-slate-50 text-slate-700 text-sm text-center";

            let statusColor = "text-yellow-600 bg-yellow-100";
            if (leave.status === 'APPROVED') statusColor = "text-green-600 bg-green-100";
            if (leave.status === 'REJECTED') statusColor = "text-red-600 bg-red-100";

            tr.innerHTML = `
                <td class="py-3 px-4">${leave.startDate} to ${leave.endDate}</td>
                <td class="py-3 px-4">${leave.reason}</td>
                <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-semibold ${statusColor}">${leave.status}</span></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error("Error in fetchLeaveHistory sync:", error);
        window.showToast("Failed to refresh leave logs.", "error");
    }
}

function logout() {
    sessionStorage.clear();
    window.location.href = '/login.html';
}