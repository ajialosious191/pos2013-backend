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

// RBAC UI Enforcement
document.addEventListener("DOMContentLoaded", () => {
    if (userRole === "viewer") {
        const downloadCsvBtn = document.getElementById("downloadCsvBtn");
        if (downloadCsvBtn) downloadCsvBtn.style.display = "none";
    }
    
    if (userRole !== "owner" && userRole !== "admin") {
        const pairTerminalBtn = document.getElementById("pairTerminalBtn");
        if (pairTerminalBtn) pairTerminalBtn.style.display = "none";
    }

    if (userRole === "owner" || userRole === "admin") {
        const userManagementBtn = document.getElementById("userManagementBtn");
        if (userManagementBtn) userManagementBtn.style.display = "inline-block";
    }
});

const BASE_URL = "https://pos2013-backend.onrender.com/api/transactions";
const AUTH_URL = "https://pos2013-backend.onrender.com/api/auth";
let currentRows = [];
let filteredRows = [];
let currentPage = 1;
let pageSize = 10;

async function loadTransactions() {
    try {
        const res = await fetch(BASE_URL, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.status === 401) {
            localStorage.removeItem("token");
            window.location.href = "login.html";
            return;
        }
        const data = await res.json();
        currentRows = data;
        filteredRows = data;
        currentPage = 1;
        renderPage();
        updateStats(data);
    } catch (err) {
        console.error('Failed to load transactions:', err);
        alert('Could not connect to backend. Make sure the server is running on port 10000.');
    }
}

function renderTable(rows) {
    const tableBody = document.getElementById('transactionTableBody');
    tableBody.innerHTML = '';

    if (rows.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 40px; color: #666;">No transactions found</td></tr>';
        return;
    }

    rows.forEach(tx => {
        const row = document.createElement('tr');
        const createdAt = new Date(tx.created_at).toLocaleString();
        const amount = (tx.amount_minor / 100).toFixed(2);
        const statusClass = tx.status.toLowerCase() === 'synced' ? 'status-synced' : 'status-pending';

        row.innerHTML = `
            <td>${createdAt}</td>
            <td><strong>${tx.code}</strong></td>
            <td>${amount}</td>
            <td>${tx.currency || 'AED'}</td>
            <td>${tx.terminal_id}</td>
            <td><span class="status-badge ${statusClass}">${tx.status}</span></td>
            <td><button class="btn-receipt">Invoice</button></td>
        `;
        
        row.querySelector('.btn-receipt').onclick = () => generateInvoicePDF(tx);
        
        tableBody.appendChild(row);
    });
}

async function generateInvoicePDF(tx) { 
    const { jsPDF } = window.jspdf; 
 
    try {
        // Fetch merchant branding info
        const merchantRes = await fetch(`${AUTH_URL}/register-terminal`, {
            method: "POST",
            headers: { 
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ 
                merchantId: tx.merchant_id,
                terminalId: tx.terminal_id
            })
        });
        const merchantData = await merchantRes.json();
        const branding = merchantData.branding || { name: tx.merchant_id, brand_color: '#1976d2', logo_url: '' };

        // Load the HTML template 
        const response = await fetch("invoice_template.html"); 
        let htmlContent = await response.text(); 
 
        // Replace branding placeholders 
        htmlContent = htmlContent
            .replace("{{logoUrl}}", branding.logo_url || "")
            .replace("{{brandColor}}", branding.brand_color || "#1976d2")
            .replace("{{merchantName}}", branding.name || tx.merchant_id);

        // Currency Mapping
        const currencySymbols = {
            "USD": "$", "EUR": "€", "GBP": "£", "JPY": "¥", "CHF": "Fr", "CAD": "$", "AUD": "$", "NZD": "$",
            "CNY": "¥", "HKD": "$", "SGD": "$", "INR": "₹", "AED": "د.إ", "SAR": "ر.س", "QAR": "ر.ق",
            "KWD": "د.ك", "BHD": ".د.ب", "OMR": "ر.ع.", "EGP": "£", "SEK": "kr", "NOK": "kr", "DKK": "kr",
            "RUB": "₽", "TRY": "₺", "THB": "฿", "PHP": "₱", "MYR": "RM", "KRW": "₩", "IDR": "Rp",
            "ARS": "$", "BRL": "R$", "MXN": "$", "CLP": "$", "COP": "$", "PEN": "S/", "ZAR": "R",
            "NGN": "₦", "KES": "KSh"
        };
        const symbol = currencySymbols[tx.currency || 'AED'] || (tx.currency || 'AED');
        
        // Data Formatting
        const subtotal = ((tx.subtotal_minor || tx.amount_minor) / 100).toFixed(2);
        const tax = ((tx.tax_minor || 0) / 100).toFixed(2);
        const discount = ((tx.discount_minor || 0) / 100).toFixed(2);
        const total = (tx.amount_minor / 100).toFixed(2);
        const dateStr = new Date(tx.created_at).toLocaleString();

        // Create a temporary container
        const container = document.createElement('div');
        container.style.position = 'fixed';
        container.style.top = '-10000px';
        container.innerHTML = htmlContent;
        document.body.appendChild(container);

        // Inject Header Data
        container.querySelector('#invoiceId').innerText = tx.id || '-';
        container.querySelector('#date').innerText = dateStr;

        // Build item rows 
        const items = tx.items || [];
        const itemsContainer = container.querySelector('#itemsContainer');
        itemsContainer.innerHTML = "";
        
        if (items.length === 0) {
            // Default row if no items
            itemsContainer.innerHTML = `
                <div class="item-row">
                    <span>Transaction ${tx.code}</span>
                    <span>1</span>
                    <span>${symbol} ${total}</span>
                </div>
            `;
        } else {
            items.forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'item-row';
                itemDiv.innerHTML = `
                    <span>${item.name}</span>
                    <span>${item.qty}</span>
                    <span>${symbol} ${(item.price / 100).toFixed(2)}</span>
                `;
                itemsContainer.appendChild(itemDiv);
            });
        }

        // Inject Totals
        container.querySelector('#subtotal').innerText = `${symbol} ${subtotal}`;
        container.querySelector('#tax').innerText = `${symbol} ${tax}`;
        container.querySelector('#discount').innerText = `${symbol} ${discount}`;
        container.querySelector('#total').innerText = `${symbol} ${total}`;

        // RTL Support
        if (branding.language === "ar") {
            container.querySelector('body').setAttribute('dir', 'rtl');
        }

        // Create PDF 
        const pdf = new jsPDF({ 
            unit: "pt", 
            format: [320, 800] 
        }); 
 
        await pdf.html(container, { 
            x: 0, 
            y: 0, 
            width: 320,
            windowWidth: 320
        }); 
 
        pdf.save(`invoice_${tx.code}.pdf`); 
        
        // Cleanup
        document.body.removeChild(container);
    } catch (err) {
        console.error('Failed to generate invoice:', err);
        alert('Error generating invoice PDF.');
    }
} 

function renderPage() { 
    const start = (currentPage - 1) * pageSize; 
    const end = start + pageSize; 
 
    const pageRows = filteredRows.slice(start, end); 
    const totalPages = Math.ceil(filteredRows.length / pageSize) || 1;

    renderTable(pageRows); 
 
    document.getElementById("pageInfo").innerText = `Page ${currentPage} of ${totalPages}`;
    document.getElementById("prevPageBtn").disabled = currentPage === 1;
    document.getElementById("nextPageBtn").disabled = currentPage === totalPages;
} 

function updateStats(rows) {
    const totalCount = rows.length;
    const totalAmount = rows.reduce((sum, tx) => sum + (tx.amount_minor / 100), 0).toFixed(2);

    document.getElementById('totalCount').textContent = totalCount;
    document.getElementById('totalAmount').textContent = `AED ${totalAmount}`;
}

function convertToCSV(rows) { 
    const header = [ 
        "Code", 
        "Amount", 
        "Currency", 
        "Merchant", 
        "Terminal", 
        "Status", 
        "Created At" 
    ]; 

    const csvRows = [header.join(",")]; 

    rows.forEach(tx => { 
        const row = [ 
            tx.code, 
            (tx.amount_minor / 100).toFixed(2), 
            tx.currency || 'AED',
            tx.merchant_id, 
            tx.terminal_id, 
            tx.status, 
            new Date(tx.created_at).toLocaleString() 
        ]; 
        csvRows.push(row.join(",")); 
    }); 

    return csvRows.join("\n"); 
} 

function downloadCSV(rows) { 
    const csv = convertToCSV(rows); 
    const blob = new Blob([csv], { type: "text/csv" }); 
    const url = URL.createObjectURL(blob); 

    const a = document.createElement("a"); 
    a.href = url; 
    a.download = "transactions.csv"; 
    a.click(); 

    URL.revokeObjectURL(url); 
} 

// Event Listeners
document.getElementById('refreshBtn').onclick = () => {
    loadTransactions();
};

document.getElementById("searchInput").oninput = (e) => { 
    const q = e.target.value.toLowerCase(); 
 
    filteredRows = currentRows.filter(tx => 
        tx.code.toLowerCase().includes(q) || 
        tx.merchant_id.toLowerCase().includes(q) || 
        tx.terminal_id.toLowerCase().includes(q) || 
        tx.status.toLowerCase().includes(q) 
    ); 
 
    currentPage = 1; 
    renderPage(); 
}; 

document.getElementById("nextPageBtn").onclick = () => { 
    const maxPage = Math.ceil(filteredRows.length / pageSize); 
    if (currentPage < maxPage) { 
        currentPage++; 
        renderPage(); 
    } 
}; 
 
document.getElementById("prevPageBtn").onclick = () => { 
    if (currentPage > 1) { 
        currentPage--; 
        renderPage(); 
    } 
}; 

document.getElementById("pageSizeSelect").onchange = (e) => { 
    pageSize = parseInt(e.target.value); 
    currentPage = 1; 
    renderPage(); 
}; 

document.getElementById("downloadCsvBtn").onclick = () => { 
    if (currentRows.length === 0) {
        alert('No data to download.');
        return;
    }
    downloadCSV(currentRows); 
}; 

document.getElementById("pairTerminalBtn").onclick = async () => { 
    try {
        const res = await fetch(`${AUTH_URL}/register-terminal`, { 
            method: "POST", 
            headers: { "Content-Type": "application/json" }, 
            body: JSON.stringify({ 
                merchantId: "AL_RKN_AL_RAQY", 
                terminalId: "T001" // In a real app, this could be dynamic
            }) 
        }); 

        const data = await res.json(); 

        const payload = { 
            merchantId: data.merchantId, 
            terminalId: data.terminalId, 
            apiKey: data.apiKey,
            branding: data.branding
        }; 

        document.getElementById("qrModal").style.display = "block"; 

        new QRCode(document.getElementById("qrcode"), { 
            text: JSON.stringify(payload), 
            width: 200, 
            height: 200 
        }); 
    } catch (err) {
        console.error('Failed to generate pairing QR:', err);
        alert('Could not connect to backend to register terminal.');
    }
}; 

document.getElementById("closeQrBtn").onclick = () => { 
    document.getElementById("qrModal").style.display = "none"; 
    document.getElementById("qrcode").innerHTML = ""; 
}; 

document.getElementById('logoutBtn').onclick = () => {
    localStorage.removeItem("token");
    window.location.href = "login.html";
};

// Initial load
loadTransactions();
