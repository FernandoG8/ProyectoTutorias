import type { ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import { X } from "lucide-react";
import { colors } from "@/constants/colors";

/**
 * Modal Component
 *
 * Generic modal/dialog wrapper using Radix UI for:
 * - Accessibility (ARIA compliant)
 * - Keyboard handling (Esc to close)
 * - Focus management
 * - Animated overlay and content
 * - Flexible content structure
 *
 * Part of Week 2 modal system
 *
 * Decision Log:
 * - Radix UI for proven accessible patterns
 * - Separates overlay, header, body, footer for flexibility
 * - Styled close button with icon
 * - Custom scrollable content area
 * - Works with Framer Motion for animations
 */

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  description?: string;
  children: ReactNode;
  size?: "sm" | "md" | "lg" | "xl";
  showCloseButton?: boolean;
  isDismissible?: boolean;
}

const sizeClasses = {
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-xl",
};

export const Modal = ({
  isOpen,
  onClose,
  title,
  description,
  children,
  size = "md",
  showCloseButton = true,
  isDismissible = true,
}: ModalProps) => {
  return (
    <Dialog.Root open={isOpen} onOpenChange={isDismissible ? onClose : undefined}>
      {/* Overlay */}
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-40"
          style={{
            backgroundColor: "rgba(0, 0, 0, 0.5)",
          }}
        />

        {/* Content */}
        <Dialog.Content
          className={`fixed left-1/2 top-1/2 z-50 w-full -translate-x-1/2 -translate-y-1/2 rounded-xl border bg-white shadow-xl ${sizeClasses[size]}`}
          style={{
            borderColor: colors.semantic.border,
          }}
          onEscapeKeyDown={isDismissible ? onClose : undefined}
        >
          {/* Header */}
          {title && (
            <div className="flex items-start justify-between border-b px-6 py-4" style={{ borderColor: colors.semantic.border }}>
              <div>
                <Dialog.Title
                  className="text-lg font-semibold"
                  style={{ color: colors.semantic.text.primary }}
                >
                  {title}
                </Dialog.Title>
                {description && (
                  <Dialog.Description
                    className="mt-1 text-sm"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    {description}
                  </Dialog.Description>
                )}
              </div>

              {showCloseButton && (
                <Dialog.Close asChild>
                  <button
                    className="rounded-lg p-1.5 hover:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2"
                    aria-label="Cerrar modal"
                  >
                    <X className="h-5 w-5" style={{ color: colors.semantic.text.muted }} />
                  </button>
                </Dialog.Close>
              )}
            </div>
          )}

          {/* Body */}
          <div className="overflow-y-auto px-6 py-4 max-h-[calc(100vh-200px)]">
            {children}
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};
