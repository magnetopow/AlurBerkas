/* ============================================
   AlurBerkas — Application JavaScript
   ============================================ */

// Sidebar toggle
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('open');
    }
}

// Close sidebar when clicking outside on mobile
document.addEventListener('click', function(e) {
    const sidebar = document.getElementById('sidebar');
    const toggle = document.querySelector('.mobile-toggle');
    if (sidebar && sidebar.classList.contains('open') && 
        !sidebar.contains(e.target) && !toggle.contains(e.target)) {
        sidebar.classList.remove('open');
    }
});

// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(function() { alert.remove(); }, 300);
        }, 5000);
    });

    // Animate stats on scroll
    const statValues = document.querySelectorAll('.stat-value');
    statValues.forEach(function(el) {
        const target = parseInt(el.textContent);
        if (isNaN(target)) return;
        el.textContent = '0';
        animateCounter(el, target, 800);
    });

    // Animate pipeline counts
    const pipelineCounts = document.querySelectorAll('.pipeline-count');
    pipelineCounts.forEach(function(el) {
        const target = parseInt(el.textContent);
        if (isNaN(target)) return;
        el.textContent = '0';
        animateCounter(el, target, 600);
    });
});

// Counter animation
function animateCounter(element, target, duration) {
    const start = 0;
    const startTime = performance.now();
    
    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        // Ease out cubic
        const eased = 1 - Math.pow(1 - progress, 3);
        const current = Math.floor(eased * target);
        element.textContent = current;
        
        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.textContent = target;
        }
    }
    
    requestAnimationFrame(update);
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        e.target.classList.remove('show');
    }
});

// Close modal with Escape key
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal.show').forEach(function(modal) {
            modal.classList.remove('show');
        });
    }
});

// Notification polling (every 30 seconds)
setInterval(function() {
    fetch('/notifications/count')
        .then(function(response) { return response.text(); })
        .then(function(count) {
            const badges = document.querySelectorAll('.menu-badge, .notif-badge');
            badges.forEach(function(badge) {
                const num = parseInt(count);
                if (num > 0) {
                    badge.textContent = count;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            });
        })
        .catch(function() { /* silently ignore */ });
}, 30000);

// Form validation enhancement
document.querySelectorAll('form').forEach(function(form) {
    form.addEventListener('submit', function(e) {
        const requiredFields = form.querySelectorAll('[required]');
        let valid = true;
        requiredFields.forEach(function(field) {
            if (!field.value.trim()) {
                field.style.borderColor = '#EF5350';
                valid = false;
            } else {
                field.style.borderColor = '';
            }
        });
        if (!valid) {
            e.preventDefault();
        }
    });
});

// Smooth page transitions
document.querySelectorAll('.card, .stat-card, .timeline-item').forEach(function(el, i) {
    el.style.animationDelay = (i * 0.05) + 's';
    el.style.animation = 'fadeInUp 0.4s ease forwards';
    el.style.opacity = '0';
    setTimeout(function() { el.style.opacity = '1'; }, i * 50 + 100);
});
