import { ref } from "vue";

const STORAGE_KEY = "notificationSoundEnabled";
const soundEnabled = ref(true);

if (typeof window !== "undefined") {
  soundEnabled.value = localStorage.getItem(STORAGE_KEY) !== "false";
}

export const useNotificationSound = () => {
  const setSoundEnabled = (enabled: boolean) => {
    soundEnabled.value = enabled;
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_KEY, String(enabled));
    }
  };

  const toggleSound = () => setSoundEnabled(!soundEnabled.value);

  const playNotificationSound = () => {
    if (!soundEnabled.value || typeof window === "undefined") return;
    try {
      const AudioContextCtor = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioContextCtor) return;
      const context = new AudioContextCtor();
      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = "sine";
      oscillator.frequency.setValueAtTime(880, context.currentTime);
      oscillator.frequency.setValueAtTime(660, context.currentTime + 0.12);
      gain.gain.setValueAtTime(0.001, context.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.18, context.currentTime + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.001, context.currentTime + 0.32);
      oscillator.connect(gain);
      gain.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + 0.34);
      setTimeout(() => context.close().catch(() => {}), 500);
    } catch {
      // Browsers can block sound before the first user interaction.
    }
  };

  return {
    soundEnabled,
    setSoundEnabled,
    toggleSound,
    playNotificationSound,
  };
};
