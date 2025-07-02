// DevHack Animations
// This file contains JavaScript animations for the DevHack application

document.addEventListener('DOMContentLoaded', function () {
    // Add animate-on-scroll class to elements that should animate when scrolled into view
    document.querySelectorAll('.card').forEach(card => {
        if (!card.classList.contains('fade-in')) {
            card.classList.add('animate-on-scroll');
        }
    });

    // Fade in elements when they come into view
    const fadeElements = document.querySelectorAll('.fade-in');

    const fadeInObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                fadeInObserver.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1
    });

    fadeElements.forEach(element => {
        fadeInObserver.observe(element);
    });

    // Animate elements when scrolled into view
    const animateElements = document.querySelectorAll('.animate-on-scroll');

    const animateObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Add a random animation from our set
                const animations = ['fadeInUp', 'fadeInLeft', 'fadeInRight', 'zoomIn'];
                const randomAnimation = animations[Math.floor(Math.random() * animations.length)];

                entry.target.style.animationName = randomAnimation;
                entry.target.style.animationDuration = '0.6s';
                entry.target.style.animationFillMode = 'both';

                animateObserver.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1
    });

    animateElements.forEach(element => {
        animateObserver.observe(element);
    });

    // Add ripple effect to buttons
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('click', function (e) {
            const rect = button.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const ripple = document.createElement('span');
            ripple.classList.add('ripple');
            ripple.style.left = `${x}px`;
            ripple.style.top = `${y}px`;

            button.appendChild(ripple);

            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });

    // Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();

            const targetId = this.getAttribute('href');
            if (targetId === '#') return;

            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 70,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Add hover effects for cards
    document.querySelectorAll('.card.hover-shadow').forEach(card => {
        card.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 10px 20px rgba(0, 0, 0, 0.1)';
        });

        card.addEventListener('mouseleave', function () {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.08)';
        });
    });
});
