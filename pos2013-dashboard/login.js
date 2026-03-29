const API_BASE = "https://pos2013-backend.onrender.com/api/users";

document.getElementById("loginBtn").onclick = async () => {
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const errorText = document.getElementById("error");
    const loginBtn = document.getElementById("loginBtn");

    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();

    if (!email || !password) {
        errorText.innerText = "Please enter both email and password";
        return;
    }

    errorText.innerText = "";
    loginBtn.disabled = true;
    loginBtn.innerText = "Logging in...";

    try {
        console.log("Attempting login to:", `${API_BASE}/login`);
        
        // Add a 10 second timeout
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000);

        const response = await fetch(`${API_BASE}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
            signal: controller.signal
        });

        clearTimeout(timeoutId);
        console.log("Response received, status:", response.status);
        const data = await response.json();
        console.log("Response data:", data);

        if (data.success) {
            console.log("Login successful, storing token...");
            localStorage.setItem("token", data.token);
            localStorage.setItem("userEmail", email);
            window.location.href = "index.html";
        } else {
            console.warn("Login failed:", data.message);
            errorText.innerText = data.message || "Invalid email or password";
            loginBtn.disabled = false;
            loginBtn.innerText = "Login";
        }
    } catch (error) {
        console.error("Critical Login Error:", error);
        errorText.innerHTML = "Connection failed. <br><small>Check console (F12) for details. Note: If this is the first try, wait 60 seconds for Render to wake up.</small>";
        loginBtn.disabled = false;
        loginBtn.innerText = "Login";
    }
};

// Allow login with Enter key
document.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        document.getElementById("loginBtn").click();
    }
});
