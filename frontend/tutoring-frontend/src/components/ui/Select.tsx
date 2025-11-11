import type { SelectHTMLAttributes } from "react";

export const Select = ({ className = "", ...props }: SelectHTMLAttributes<HTMLSelectElement>) => {
  return (
    <select
      className={`w-full rounded-lg border border-border bg-white px-3 py-2 text-sm text-text focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40 ${className}`}
      {...props}
    />
  );
};
