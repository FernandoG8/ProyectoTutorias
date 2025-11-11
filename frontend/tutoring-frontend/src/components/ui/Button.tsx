import type { ButtonHTMLAttributes } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost";
  loading?: boolean;
}

export const Button = ({
  className = "",
  variant = "primary",
  loading = false,
  children,
  disabled,
  ...props
}: ButtonProps) => {
  const baseStyles =
    "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-offset-2";

  const variants: Record<typeof variant, string> = {
    primary:
      "bg-primary text-white hover:bg-primary/90 focus:ring-primary/40 focus:ring-offset-white",
    secondary:
      "border border-border bg-white text-text hover:bg-slate-50 focus:ring-primary/30 focus:ring-offset-white",
    ghost: "text-text hover:bg-slate-100 focus:ring-primary/30 focus:ring-offset-white",
  } as const;

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
      )}
      <span>{children}</span>
    </button>
  );
};
