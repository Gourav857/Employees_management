// js/toast.js - Modern Enterprise Toast Notification System

window.showToast = function(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    // Create Toast element wrapper
    const toast = document.createElement('div');

    // Dynamic color coding classes based on Tailwind CSS styling
    let bgClass = "bg-green-600 text-white";
    let icon = "✓";
    if (type === 'error') {
        bgClass = "bg-red-600 text-white";
        icon = "✕";
    } else if (type === 'warning') {
        bgClass = "bg-yellow-500 text-slate-900";
        icon = "⚠";
    }

    toast.className = `${bgClass} flex items-center gap-3 px-4 py-3 rounded-lg shadow-xl font-medium text-sm transform translate-x-full opacity-0 transition-all duration-300 pointer-events-auto`;

    // Structure design
    toast.innerHTML = `
        <span class="flex items-center justify-center font-bold text-base bg-white bg-opacity-20 rounded-full h-6 w-6">${icon}</span>
        <div class="flex-1">${message}</div>
    `;

    container.appendChild(toast);

    // Trigger sliding animation window context frames
    setTimeout(() => {
        toast.classList.remove('translate-x-full', 'opacity-0');
        toast.classList.add('translate-x-0', 'opacity-100');
    }, 10);

    // Auto-remove setup hook after 3.5 seconds
    setTimeout(() => {
        toast.classList.remove('translate-x-0', 'opacity-100');
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3500);
};