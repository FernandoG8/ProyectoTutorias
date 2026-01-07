import * as Dialog from "@radix-ui/react-dialog";
import { AlertCircle, Check } from "lucide-react";
import { colors } from "@/constants/colors";
import { Button } from "@/components/ui/Button";

/**
 * ConfirmationModal Component
 *
 * Specialized modal for confirmation dialogs with:
 * - Icon indication (alert, success, info)
 * - Clear action buttons
 * - Loading state support
 * - Destructive action styling
 *
 * Part of Week 2 modal system
 *
 * Usage:
 * ```tsx
 * <ConfirmationModal
 *   isOpen={isOpen}
 *   onClose={onClose}
 *   title="Eliminar alumno"
 *   description="Esta acción no se puede deshacer"
 *   actionLabel="Eliminar"
 *   onConfirm={handleDelete}
 *   variant="destructive"
 *   isLoading={isDeleting}
 * />
 * ```
 */

interface ConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  actionLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  variant?: "default" | "destructive" | "success";
  isLoading?: boolean;
  isDismissible?: boolean;
}

export const ConfirmationModal = ({
  isOpen,
  onClose,
  title,
  description,
  actionLabel = "Confirmar",
  cancelLabel = "Cancelar",
  onConfirm,
  variant = "default",
  isLoading = false,
  isDismissible = true,
}: ConfirmationModalProps) => {
  const iconColor =
    variant === "destructive"
      ? colors.danger[400]
      : variant === "success"
        ? colors.success[400]
        : colors.info[400];

  const buttonColor =
    variant === "destructive"
      ? colors.danger[400]
      : variant === "success"
        ? colors.success[400]
        : colors.primary[400];

  const Icon =
    variant === "success" ? Check : variant === "destructive" ? AlertCircle : AlertCircle;

  return (
    <Dialog.Root open={isOpen} onOpenChange={isDismissible ? onClose : undefined}>
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-40"
          style={{ backgroundColor: "rgba(0, 0, 0, 0.5)" }}
        />

        <Dialog.Content
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-xl border bg-white shadow-xl p-6"
          style={{ borderColor: colors.semantic.border }}
          onEscapeKeyDown={isDismissible ? onClose : undefined}
        >
          {/* Icon */}
          <div className="flex justify-center mb-4">
            <div
              className="flex h-12 w-12 items-center justify-center rounded-full"
              style={{
                backgroundColor:
                  variant === "destructive" ? colors.danger[50] : colors.info[50],
              }}
            >
              <Icon className="h-6 w-6" style={{ color: iconColor }} />
            </div>
          </div>

          {/* Content */}
          <div className="space-y-2 text-center mb-6">
            <Dialog.Title
              className="text-lg font-semibold"
              style={{ color: colors.semantic.text.primary }}
            >
              {title}
            </Dialog.Title>
            {description && (
              <Dialog.Description
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                {description}
              </Dialog.Description>
            )}
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <button
              onClick={onClose}
              disabled={isLoading}
              className="flex-1 rounded-lg border px-4 py-2.5 text-sm font-medium transition-colors hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              style={{
                borderColor: colors.semantic.border,
                color: colors.semantic.text.primary,
              }}
            >
              {cancelLabel}
            </button>
            <Button
              onClick={onConfirm}
              loading={isLoading}
              disabled={isLoading}
              className="flex-1"
              style={{ backgroundColor: buttonColor }}
            >
              {actionLabel}
            </Button>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};
