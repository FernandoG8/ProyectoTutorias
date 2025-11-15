import { useEffect, useReducer, useCallback, useRef } from "react";

export const AsyncStatus = {
  Idle: "idle",
  Loading: "loading",
  Success: "success",
  Error: "error",
} as const;

type AsyncStatusType = typeof AsyncStatus[keyof typeof AsyncStatus];

export interface AsyncState<T> {
  status: AsyncStatusType;
  data?: T;
  error?: Error;
}

type AsyncAction<T> =
  | { type: "LOADING" }
  | { type: "SUCCESS"; payload: T }
  | { type: "ERROR"; payload: Error }
  | { type: "RESET" };

/**
 * Custom Hook: useAsync
 *
 * Handles async operations with loading, success, and error states
 *
 * Usage:
 * ```tsx
 * const { data, status, error, execute } = useAsync(fetchData);
 *
 * useEffect(() => {
 *   execute();
 * }, [execute]);
 *
 * if (status === "loading") return <Skeleton />;
 * if (status === "error") return <Error message={error.message} />;
 * return <Content data={data} />;
 * ```
 *
 * Decision Log:
 * - Uses reducer pattern for reliable state management
 * - Status enum prevents string typos
 * - Ref to prevent re-execution on component re-render
 * - Generic type support for any async operation
 * - Important: Pass a memoized/stable asyncFunction to avoid infinite loops
 */
export const useAsync = <T,>(
  asyncFunction: () => Promise<T>,
  immediate = false
) => {
  const [state, dispatch] = useReducer(
    (state: AsyncState<T>, action: AsyncAction<T>): AsyncState<T> => {
      switch (action.type) {
        case "LOADING":
          return { status: "loading" };
        case "SUCCESS":
          return { status: "success", data: action.payload };
        case "ERROR":
          return { status: "error", error: action.payload };
        case "RESET":
          return { status: "idle" };
        default:
          return state;
      }
    },
    { status: "idle" }
  );

  const isMounted = useRef(true);

  const execute = useCallback(async () => {
    // Check if component is still mounted before starting
    if (!isMounted.current) return;

    dispatch({ type: "LOADING" });
    try {
      const response = await asyncFunction();
      // Only update state if component is still mounted
      if (isMounted.current) {
        dispatch({ type: "SUCCESS", payload: response });
      }
    } catch (error) {
      // Only update state if component is still mounted
      if (isMounted.current) {
        dispatch({
          type: "ERROR",
          payload: error instanceof Error ? error : new Error(String(error)),
        });
      }
    }
  }, [asyncFunction]);

  const reset = useCallback(() => {
    // Only reset if component is still mounted
    if (isMounted.current) {
      dispatch({ type: "RESET" });
    }
  }, []);

  useEffect(() => {
    // Reset isMounted flag on mount
    isMounted.current = true;

    if (immediate) {
      execute();
    }

    // Cleanup on unmount
    return () => {
      isMounted.current = false;
    };
  }, [execute, immediate]);

  return { ...state, execute, reset };
};
