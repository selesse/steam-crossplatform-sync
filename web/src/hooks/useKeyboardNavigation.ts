import { useEffect, useRef } from "react";

export interface KeyboardNavigationCallbacks {
  onArrowLeft?: () => void;
  onArrowRight?: () => void;
  onT?: () => void;
}

/**
 * Custom hook for handling keyboard navigation shortcuts. A key calls the
 * callback from the latest render, so callers can pass fresh closures.
 *
 * Supported keys:
 * - ArrowLeft: Navigate to previous period
 * - ArrowRight: Navigate to next period
 * - t: Jump to current/today
 */
export function useKeyboardNavigation(callbacks: KeyboardNavigationCallbacks) {
  const latest = useRef(callbacks);
  useEffect(() => {
    latest.current = callbacks;
  });

  useEffect(() => {
    const keyPressHandler = (e: KeyboardEvent) => {
      const { onArrowLeft, onArrowRight, onT } = latest.current;
      if (e.key === "ArrowLeft" && onArrowLeft) {
        onArrowLeft();
      } else if (e.key === "ArrowRight" && onArrowRight) {
        onArrowRight();
      } else if (e.key === "t" && onT) {
        onT();
      }
    };
    window.addEventListener("keydown", keyPressHandler);
    return () => window.removeEventListener("keydown", keyPressHandler);
  }, []);
}
