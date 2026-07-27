document.addEventListener("DOMContentLoaded", () => {
    const footerCopy = document.getElementById("footer-copy");

    if (footerCopy) {
        const currentYear = new Date().getFullYear();
        footerCopy.textContent = `Best Rookies team showcase page for the portfolio_manager course project. ${currentYear}`;
    }
});