(function () {
    'use strict';

    const isTouch = matchMedia('(pointer: coarse)').matches;
    const prefersReduced = matchMedia('(prefers-reduced-motion: reduce)').matches;
    let isSmallScreen = window.innerWidth < 480;

    // Elements (may not exist on all pages)
    const bgImg = document.querySelector('#bg-layer img');
    const athlete = document.getElementById('athlete');
    const athleteRudisha = document.getElementById('athlete-rudisha');
    const content = document.querySelector('.content');
    const particles = document.querySelectorAll('.particles span');

    // Parallax animation variables
    let targetBx = 0, targetBy = 0, targetAx = 0, targetAy = 0;
    let currentBx = 0, currentBy = 0, currentAx = 0, currentAy = 0;
    let isAnimating = false;

    const lerp = (a, b, t) => a + (b - a) * t;

    // Mouse move handler (only if bgImg or athlete exist)
    function onMove(e) {
        if (isSmallScreen || (!bgImg && !athlete)) return;

        const { innerWidth: w, innerHeight: h } = window;
        const cx = (e.clientX ?? w / 2) / w;
        const cy = (e.clientY ?? h / 2) / h;

        const nx = (cx - 0.5) * 2;
        const ny = (cy - 0.5) * 2;

        if (bgImg) {
            targetBx = nx * 5;
            targetBy = ny * 5;
        }
        if (athlete) {
            targetAx = -nx * 8;
            targetAy = -ny * 4.8; // 60% vertical
        }
    }

    function animate() {
        if (!isAnimating) return;

        if (bgImg) {
            currentBx = lerp(currentBx, targetBx, 0.08);
            currentBy = lerp(currentBy, targetBy, 0.08);
            bgImg.style.transform = `translate3d(${currentBx}px, ${currentBy}px, 0) scale(1.02)`;
        }

        if (athlete) {
            currentAx = lerp(currentAx, targetAx, 0.12);
            currentAy = lerp(currentAy, targetAy, 0.12);
            athlete.style.transform = `translate3d(${currentAx}px, ${currentAy}px, 0)`;
        }

        if (athleteRudisha) {
            // Move Kilian with a slightly smaller offset for depth
            athleteRudisha.style.transform = `translate3d(${currentAx * 0.8}px, ${currentAy * 0.8}px, 0)`;
        }


        requestAnimationFrame(animate);
    }

    function startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animate();
        }
    }
    function stopAnimation() {
        isAnimating = false;
        targetAx = targetAy = targetBx = targetBy = 0;
    }

    // Particles
    function animateParticles() {
        if (prefersReduced || !particles.length) return;
        particles.forEach((p, i) => {
            p.style.animationDelay = `${i * 0.5}s`;
            p.style.animationDuration = `${8 + (i % 3) * 2}s`;
        });
    }

    // Content animation
    function animateContent() {
        if (prefersReduced || !content) return;
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) entry.target.style.animation = 'riseIn 0.6s ease-out both';
            });
        }, { threshold: 0.1 });
        observer.observe(content);
    }

    // Buttons
    function enhanceButtons() {
        document.querySelectorAll('.btn').forEach(btn => {
            btn.addEventListener('mouseenter', () => {
                if (prefersReduced) return;
                btn.style.transform = 'translateY(-2px) scale(1.02)';
            });
            btn.addEventListener('mouseleave', () => {
                if (prefersReduced) return;
                btn.style.transform = 'translateY(0) scale(1)';
            });
        });
    }

    function handleScroll() {
        if (prefersReduced || !bgImg) return;
        const rate = window.pageYOffset * -0.5;
        bgImg.style.transform = `translate3d(0, ${rate}px, 0) scale(1.05)`;
    }

    function handleResize() {
        const newSmall = window.innerWidth < 480;
        if (newSmall !== isSmallScreen) {
            isSmallScreen = newSmall;
            if (isSmallScreen) stopAnimation();
            else if (!isTouch && !prefersReduced) startAnimation();
        }
    }

    function init() {
        if (!isSmallScreen && !prefersReduced && (bgImg || athlete)) startAnimation();
        window.addEventListener('mousemove', onMove, { passive: true });
        window.addEventListener('resize', handleResize);
        window.addEventListener('scroll', handleScroll, { passive: true });

        animateParticles();
        animateContent();
        enhanceButtons();

        window.addEventListener('beforeunload', stopAnimation);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else init();

    window.runnaLogDebug = {
        startAnimation,
        stopAnimation,
        isAnimating: () => isAnimating,
        currentValues: () => ({ currentBx, currentBy, currentAx, currentAy })
    };
})();
