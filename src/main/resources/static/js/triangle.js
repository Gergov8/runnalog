const canvas = document.getElementById('cornerParticles');
const ctx = canvas.getContext('2d');

function resize() {
    canvas.width = canvas.offsetWidth * window.devicePixelRatio;
    canvas.height = canvas.offsetHeight * window.devicePixelRatio;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
}
resize();
window.addEventListener('resize', resize);

class Triangle {
    constructor() {
        this.reset();
    }
    reset() {
        this.x = Math.random() * 250;
        this.y = 200 + Math.random() * 100;
        this.size = 3 + Math.random() * 4;
        this.speedY = 0.1 + Math.random() * 0.25;
        this.speedX = 0.05 + Math.random() * 0.15;
        this.angle = Math.random() * Math.PI * 2;
        this.rotation = (Math.random() - 0.5) * 0.01;
        this.opacity = 0.4 + Math.random() * 0.4;
    }
    update() {
        this.y -= this.speedY;
        this.x += this.speedX;
        this.angle += this.rotation;
        if (this.y < -20 || this.x > 300) this.reset();
    }
    draw(ctx) {
        ctx.save();
        ctx.translate(this.x, this.y);
        ctx.rotate(this.angle);
        ctx.beginPath();
        ctx.moveTo(0, -this.size / 1.2);
        ctx.lineTo(this.size / 1.2, this.size / 1.2);
        ctx.lineTo(-this.size / 1.2, this.size / 1.2);
        ctx.closePath();
        const grad = ctx.createLinearGradient(0, 0, this.size, this.size);
        grad.addColorStop(0, `rgba(255, 233, 175, ${this.opacity})`);
        grad.addColorStop(1, `rgba(212, 173, 86, ${this.opacity * 0.8})`);
        ctx.fillStyle = grad;
        ctx.fill();
        ctx.restore();
    }
}

const triangles = Array.from({ length: 20 }, () => new Triangle());

function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    triangles.forEach(t => {
        t.update();
        t.draw(ctx);
    });
    requestAnimationFrame(animate);
}
animate();
