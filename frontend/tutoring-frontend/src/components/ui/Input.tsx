import type { InputHTMLAttributes } from "react";

export const Input = ({ className = "", ...props }: InputHTMLAttributes<HTMLInputElement>) => {
  return (
    <input
      className={`w-full rounded-lg border border-border bg-white px-3 py-2 text-sm text-text placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40 ${className}`}
      {...props}
    />
  );
};
