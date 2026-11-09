import React, { useEffect, useRef } from 'react';
import { useLauncherContext } from '../../stores';

interface Flake {
  x: number;
  y: number;
  radius: number;
  speed: number;
  wind: number;
  alpha: number;
}

export const Snowfall: React.FC = () => {
  const { settings } = useLauncherContext();
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!settings.snowfall || settings.reducedMotion) {
      return;
    }

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', handleResize);

    const count =
      settings.snowfallIntensity === 'light' ? 28 : settings.snowfallIntensity === 'heavy' ? 85 : 50;

    const flakes: Flake[] = Array.from({ length: count }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      radius: Math.random() * 2 + 1,
      speed: Math.random() * 0.7 + 0.35,
      wind: Math.random() * 0.4 - 0.2,
      alpha: Math.random() * 0.5 + 0.25,
    }));

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      for (let i = 0; i < flakes.length; i++) {
        const f = flakes[i];
        f.y += f.speed;
        f.x += f.wind + Math.sin(f.y * 0.01) * 0.25;

        if (f.y > height) {
          f.y = -5;
          f.x = Math.random() * width;
        }
        if (f.x > width) f.x = 0;
        if (f.x < 0) f.x = width;

        ctx.fillStyle = `rgba(248, 250, 252, ${f.alpha})`;
        ctx.beginPath();
        ctx.arc(f.x, f.y, f.radius, 0, Math.PI * 2);
        ctx.fill();
      }

      animId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(animId);
    };
  }, [settings.snowfall, settings.snowfallIntensity, settings.reducedMotion]);

  if (!settings.snowfall || settings.reducedMotion) {
    return null;
  }

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        pointerEvents: 'none',
        zIndex: 900,
        opacity: 0.85,
      }}
    />
  );
};
