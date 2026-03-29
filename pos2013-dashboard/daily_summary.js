const token = localStorage.getItem("token");
let userRole = "viewer";

if (!token) {
    window.location.href = "login.html";
} else {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        userRole = payload.role;
    } catch (e) {
        console.error("Token invalid:", e);
        localStorage.removeItem("token");
        window.location.href = "login.html";
    }
}

const API_BASE = "https://pos2013-backend.onrender.com/api/reports";

document.addEventListener("DOMContentLoaded", () => {
    const datePicker = document.getElementById("datePicker");
    const displayDate = document.getElementById("displayDate");

    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    datePicker.value = today;
    displayDate.innerText = today;

    loadSummary(today);

    datePicker.onchange = (e) => {
        const selectedDate = e.target.value;
        displayDate.innerText = selectedDate;
        loadSummary(selectedDate);
    };

    document.getElementById("logoutBtn").onclick = () => {
        localStorage.removeItem("token");
        window.location.href = "login.html";
    };
});

async function loadSummary(date) {
    try {
        const response = await fetch(`${API_BASE}/daily?date=${date}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        
        if (response.status === 401) {
            localStorage.removeItem("token");
            window.location.href = "login.html";
            return;
        }
        
        const data = await response.json();

        if (data.success) {
            updateSummaryUI(data.summary);
            updateCurrencyTotalsUI(data.currencyTotals);
            updateBreakdownUI(data.breakdown);
        } else {
            alert("Error: " + data.message);
        }
    } catch (error) {
        console.error("Failed to fetch summary:", error);
        alert("Failed to connect to backend");
    }
}

function updateSummaryUI(summary) {
    document.getElementById("totalCount").innerText = 
        summary.total_count;

    document.getElementById("pendingCount").innerText = 
        summary.pending_count;
}

function updateCurrencyTotalsUI(currencyTotals) {
    const tbody = document.getElementById("currencyTotalsTable");
    tbody.innerHTML = "";

    if (!currencyTotals || currencyTotals.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: #718096; padding: 20px;">No transactions found.</td></tr>`;
        return;
    }

    currencyTotals.forEach(row => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>${row.currency}</strong></td>
            <td>${row.count}</td>
            <td>${row.currency} ${(row.amount / 100).toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
    });
}

function updateBreakdownUI(breakdown) {
    const tbody = document.getElementById("breakdownTable");
    tbody.innerHTML = "";

    if (breakdown.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: #718096; padding: 20px;">No transactions found for this date.</td></tr>`;
        return;
    }

    breakdown.forEach(row => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${row.terminal_id}</td>
            <td>${row.count}</td>
            <td>${(row.amount / 100).toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
    });
}
