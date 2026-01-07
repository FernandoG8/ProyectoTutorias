import type { ButtonHTMLAttributes, ReactNode } from "react";
import { Loader2 } from "lucide-react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "tertiary" | "danger" | "ghost";
  size?: "sm" | "md" | "lg";
  icon?: ReactNode;
  iconPosition?: "left" | "right";
  loading?: boolean;
  fullWidth?: boolean;
}

export const Button = ({
  className = "",
  variant = "primary",
  size = "md",
  icon,
  iconPosition = "left",
  loading = false,
  fullWidth = false,
  children,
  disabled,
  ...props
}: ButtonProps) => {
  const baseStyles =
    "inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed";

  const sizes: Record<typeof size, string> = {
    sm: "px-3 py-1.5 text-xs",
    md: "px-4 py-2 text-sm",
    lg: "px-6 py-3 text-base",
  } as const;

  const variants: Record<typeof variant, string> = {
    primary:
      "bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500/40 focus:ring-offset-white",
    secondary:
      "border border-gray-300 bg-white text-gray-900 hover:bg-gray-50 focus:ring-blue-500/30",
    tertiary:
      "bg-gray-100 text-gray-900 hover:bg-gray-200 focus:ring-gray-500/30",
    danger:
      "bg-red-600 text-white hover:bg-red-700 focus:ring-red-500/40 focus:ring-offset-white",
    ghost: "text-gray-700 hover:bg-gray-100 focus:ring-gray-500/30",
  } as const;

  const widthClass = fullWidth ? "w-full" : "";

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${widthClass} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <Loader2 className="h-4 w-4 animate-spin" />
      ) : icon && iconPosition === "left" ? (
        icon
      ) : null}
      {children && <span>{children}</span>}
      {icon && iconPosition === "right" && !loading ? icon : null}
    </button>
  );
};
