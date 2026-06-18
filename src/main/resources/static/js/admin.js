// js/admin.js - Enterprise Admin Controller JavaScript Pipeline with Pagination Support

// GLOBAL STATE FOR PAGINATION ENGINE
let currentPage = 0;
const pageSize = 10;

// Page load hote hi data fetch karne ke liye trigger
document.addEventListener('DOMContentLoaded', () => {
    console.log("--- Admin Dashboard Loaded! Fetching Data... ---");
    fetchEmployees();
    fetchLeaves();
    setupAddEmployeeForm();

    const email = sessionStorage.getItem("userEmail") || "admin@ems.com";

    try {
        const eventSource = new EventSource(`/api/notifications/subscribe/${email}`);

        eventSource.addEventListener("REFRESH", (event) => {
            console.log("⚡ [REAL-TIME EVENT] Database changed! Syncing data...");
            fetchEmployees();
            fetchLeaves();
        });

        eventSource.addEventListener("INIT", (event) => {
            console.log("🚀 SSE Connection Initialized:", event.data);
        });

        eventSource.onerror = () => {
            console.warn("SSE connection dropped, silent fallback mode active.");
        };
    } catch(err) {
        console.error("SSE stream layer initialization crash:", err);
    }
});

// 1. PAGINATED EMPLOYEES FETCH FUNCTION
async function fetchEmployees() {
    try {
        const token = sessionStorage.getItem("token");
        const cacheBuster = new Date().getTime();

        // HIGH INDUSTRY FIX: Request endpoints now transmit pagination metadata
        const response = await fetch(`/api/admin/employees?page=${currentPage}&size=${pageSize}&t=${cacheBuster}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error(`HTTP Error! Status: ${response.status}`);

        // Spring Page Object contains server metrics wrap
        const pageData = await response.json();

        // Extract array from standard dynamic container wrapper 'content'
        const employees = pageData.content || [];

        const allTables = document.querySelectorAll('table');
        if (allTables.length === 0) return;

        const tbody = allTables[0].querySelector('tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (employees.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-slate-500">No employees found in database.</td></tr>`;
            return;
        }

        employees.forEach(emp => {
            const tr = document.createElement('tr');
            tr.className = "border-b border-slate-100 hover:bg-slate-50 text-slate-700 text-sm text-center";
            tr.innerHTML = `
                <td class="py-3 px-4 font-semibold">${emp.id}</td>
                <td class="py-3 px-4">${emp.user ? (emp.user.firstName + ' ' + emp.user.lastName) : 'N/A'}</td>
                <td class="py-3 px-4">${emp.user ? emp.user.email : 'N/A'}</td>
                <td class="py-3 px-4">${emp.department || 'N/A'}</td>
                <td class="py-3 px-4">${emp.designation || 'N/A'}</td>
                <td class="py-3 px-4">₹${emp.salary || '0'}</td>
                <td class="py-3 px-4">
                    <button onclick="deleteEmployee(${emp.id})" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs transition duration-150 cursor-pointer">
                        Delete
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error("Error in fetchEmployees:", error);
        window.showToast("Failed to fetch employee list from server.", "error");
    }
}

// 2. ALL LEAVES FETCH FUNCTION
async function fetchLeaves() {
    try {
        const token = sessionStorage.getItem("token");
        const cacheBuster = new Date().getTime();

        const response = await fetch(`/api/admin/leaves?t=${cacheBuster}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error(`HTTP Error! Status: ${response.status}`);

        const leaves = await response.json();
        const allTables = document.querySelectorAll('table');
        if (allTables.length < 2) return;

        const tbody = allTables[1].querySelector('tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (leaves.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-slate-500">No leave applications pending.</td></tr>`;
            return;
        }

        leaves.forEach(leave => {
            const tr = document.createElement('tr');
            tr.className = "border-b border-slate-100 hover:bg-slate-50 text-slate-700 text-sm text-center";

            let statusColor = "text-yellow-600 bg-yellow-100";
            if (leave.status === 'APPROVED') statusColor = "text-green-600 bg-green-100";
            if (leave.status === 'REJECTED') statusColor = "text-red-600 bg-red-100";

            const empName = leave.employee && leave.employee.user ?
                `${leave.employee.user.firstName} ${leave.employee.user.lastName}` : 'N/A';

            tr.innerHTML = `
                <td class="py-3 px-4 font-semibold">${leave.id}</td>
                <td class="py-3 px-4">${empName}</td>
                <td class="py-3 px-4">${leave.startDate} to ${leave.endDate}</td>
                <td class="py-3 px-4">${leave.reason}</td>
                <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-semibold ${statusColor}">${leave.status}</span></td>
                <td class="py-3 px-4 space-x-2">
                    ${leave.status === 'PENDING' ? `
                        <button onclick="updateLeave(${leave.id}, 'APPROVED')" class="bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded text-xs cursor-pointer">Approve</button>
                        <button onclick="updateLeave(${leave.id}, 'REJECTED')" class="bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded text-xs cursor-pointer">Reject</button>
                    ` : `<span class="text-xs text-slate-400">Processed</span>`}
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error("Error in fetchLeaves:", error);
        window.showToast("Failed to fetch leave logs.", "error");
    }
}

// 3. DELETE EMPLOYEE FUNCTION
async function deleteEmployee(id) {
    if (confirm("Are you sure you want to delete this employee?")) {
        try {
            const token = sessionStorage.getItem("token");
            const response = await fetch(`/api/admin/employee/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                window.showToast("Employee deleted and profile archived successfully!", "success");
                fetchEmployees();
            } else {
                const errData = await response.json();
                window.showToast(`Delete Action Blocked: ${errData.error || 'Server processing failure'}`, "error");
            }
        } catch (error) {
            console.error("Error deleting employee:", error);
            window.showToast("Network interface disconnect tracking error.", "error");
        }
    }
}

// 4. UPDATE LEAVE STATUS FUNCTION
async function updateLeave(id, status) {
    try {
        const token = sessionStorage.getItem("token");
        const response = await fetch(`/api/admin/leave/${id}?status=${status}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            window.showToast(`Leave application status updated to ${status.toLowerCase()}!`, "success");
            fetchLeaves();
        } else {
            const errData = await response.json();
            window.showToast(`Update Blocked: ${errData.error || 'Failed to process leave status'}`, "error");
        }
    } catch (error) {
        console.error("Error updating leave:", error);
        window.showToast("Network communication channel error.", "error");
    }
}

// 5. FORM EVENT HANDLER FOR DYNAMIC INSERTION (WITH ROBUST VALIDATION)
function setupAddEmployeeForm() {
    const form = document.getElementById('add-employee-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = sessionStorage.getItem("token");

        const firstName = document.getElementById('emp-firstName').value.trim();
        const lastName = document.getElementById('emp-lastName').value.trim();
        const email = document.getElementById('emp-email').value.trim();
        const password = document.getElementById('emp-password').value;
        const department = document.getElementById('emp-dept').value.trim();
        const designation = document.getElementById('emp-desg').value.trim();
        const salaryVal = document.getElementById('emp-salary').value;

        if (!firstName || !lastName || !email || !password || !department || !designation || !salaryVal) {
            window.showToast("All fields are mandatory. Form execution halted.", "warning");
            return;
        }

        const cleanEmailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!cleanEmailRegex.test(email)) {
            window.showToast("Please provide a valid corporate email structure.", "warning");
            return;
        }

        if (password.length < 6) {
            window.showToast("Security Policy: Password must be at least 6 characters long.", "warning");
            return;
        }

        const salary = parseFloat(salaryVal);
        if (isNaN(salary) || salary <= 0) {
            window.showToast("Financial Policy: Salary amount must be a positive number.", "warning");
            return;
        }

        const payload = { firstName, lastName, email, password, department, designation, salary };

        try {
            const response = await fetch('/api/admin/employee/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                window.showToast("New Employee record synchronized successfully!", "success");
                form.reset();
                fetchEmployees();
            } else {
                const data = await response.json();
                window.showToast(`Operation Blocked: ${data.error || 'Failed to sync record'}`, "error");
            }
        } catch (err) {
            console.error("Failed post fetch:", err);
            window.showToast("Network transaction channel timed out.", "error");
        }
    });
}

function logout() {
    sessionStorage.clear();
    window.location.href = '/login.html';
}