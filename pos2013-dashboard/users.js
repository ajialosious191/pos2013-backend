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

if (userRole !== "owner" && userRole !== "admin") {
    alert("Access denied. Owners only.");
    window.location.href = "index.html";
}

const API_BASE = "https://pos2013-backend.onrender.com/api/users";

document.addEventListener("DOMContentLoaded", () => {
    const addUserBtn = document.getElementById("addUserBtn");
    const messageText = document.getElementById("message");

    addUserBtn.onclick = async () => {
        const email = document.getElementById("newEmail").value.trim();
        const password = document.getElementById("newPassword").value.trim();
        const role = document.getElementById("newRole").value;

        if (!email || !password) {
            messageText.innerText = "Email and password are required";
            messageText.style.color = "red";
            return;
        }

        addUserBtn.disabled = true;
        addUserBtn.innerText = "Creating...";

        try {
            const response = await fetch(`${API_BASE}/register`, {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ email, password, role })
            });

            const data = await response.json();

            if (data.success) {
                messageText.innerText = "User created successfully!";
                messageText.style.color = "green";
                document.getElementById("newEmail").value = "";
                document.getElementById("newPassword").value = "";
            } else {
                messageText.innerText = data.message || "Failed to create user";
                messageText.style.color = "red";
            }
        } catch (error) {
            console.error("Add user error:", error);
            messageText.innerText = "Connection failed";
            messageText.style.color = "red";
        } finally {
            addUserBtn.disabled = false;
            addUserBtn.innerText = "Create User";
        }
    };

    document.getElementById("logoutBtn").onclick = () => {
        localStorage.removeItem("token");
        window.location.href = "login.html";
    };
});
