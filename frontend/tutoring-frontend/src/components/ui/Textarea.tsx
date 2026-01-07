import type { TextareaHTMLAttributes } from "react";

export const Textarea = ({
  className = "",
  ...props
}: TextareaHTMLAttributes<HTMLTextAreaElement>) => {
  return (
    <textarea
      className={`w-full rounded-lg border border-border bg-white px-3 py-2 text-sm text-text placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40 ${className}`}
      {...props}
    />
  );
};
