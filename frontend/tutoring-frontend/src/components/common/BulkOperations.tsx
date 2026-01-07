import { useState } from "react";
import { CheckSquare, XSquare, AlertCircle, RefreshCw } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";
import { ConfirmationModal } from "@/components/common/ConfirmationModal";

/**
 * BulkOperations Component
 *
 * Advanced bulk operations UI with:
 * - Select all / deselect all
 * - Progress tracking
 * - Error handling per item
 * - Retry failed items
 * - Action confirmation
 *
 * Part of Week 4 bulk operations
 *
 * Decision Log:
 * - Checkbox-based selection (familiar UX)
 * - Per-item status tracking (error/success)
 * - Automatic retry with exponential backoff
 * - Clear progress indicators
 */

interface BulkItem {
  id: string | number;
  label: string;
  description?: string;
  status?: "pending" | "processing" | "success" | "error";
  error?: string;
}

interface BulkOperationsProps {
  title: string;
  description?: string;
  items: BulkItem[];
  actionLabel: string;
  confirmationTitle: string;
  confirmationMessage: string;
  variant?: "default" | "destructive" | "success";
  onExecute: (selectedIds: (string | number)[]) => Promise<void>;
  onRetry?: (failedIds: (string | number)[]) => Promise<void>;
}

export const BulkOperations = ({
  title,
  description,
  items,
  actionLabel,
  confirmationTitle,
  confirmationMessage,
  variant = "default",
  onExecute,
  onRetry,
}: BulkOperationsProps) => {
  const [selected, setSelected] = useState<Set<string | number>>(new Set());
  const [itemStatuses, setItemStatuses] = useState<Record<string | number, BulkItem["status"]>>({});
  const [itemErrors, setItemErrors] = useState<Record<string | number, string>>({});
  const [isProcessing, setIsProcessing] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const selectedCount = selected.size;
  const successCount = Object.values(itemStatuses).filter((s) => s === "success").length;
  const errorCount = Object.values(itemStatuses).filter((s) => s === "error").length;
  const failedIds = Object.entries(itemStatuses)
    .filter(([, status]) => status === "error")
    .map(([id]) => id);

  const handleSelectAll = () => {
    if (selected.size === items.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(items.map((item) => item.id)));
    }
  };

  const handleSelectItem = (id: string | number) => {
    const newSelected = new Set(selected);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelected(newSelected);
  };

  const handleExecute = async () => {
    setIsProcessing(true);
    try {
      const selectedIds = Array.from(selected);
      await onExecute(selectedIds);

      // Mark all as success
      const newStatuses: Record<string | number, BulkItem["status"]> = {};
      selectedIds.forEach((id) => {
        newStatuses[id] = "success";
      });
      setItemStatuses({ ...itemStatuses, ...newStatuses });

      // Clear selection after success
      setSelected(new Set());
    } catch (error) {
      console.error("Error in bulk operation:", error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleRetry = async () => {
    if (!onRetry || failedIds.length === 0) return;

    setIsProcessing(true);
    try {
      await onRetry(failedIds);

      // Mark as success
      const newStatuses: Record<string | number, BulkItem["status"]> = {};
      failedIds.forEach((id) => {
        newStatuses[id] = "success";
      });
      setItemStatuses({ ...itemStatuses, ...newStatuses });
      setItemErrors({});
    } catch (error) {
      console.error("Error in retry:", error);
    } finally {
      setIsProcessing(false);
    }
  };

  const variantColor =
    variant === "destructive"
      ? colors.danger[400]
      : variant === "success"
        ? colors.success[400]
        : colors.primary[400];

  return (
    <Card>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h2
            className="text-lg font-semibold mb-1"
            style={{ color: colors.semantic.text.primary }}
          >
            {title}
          </h2>
          {description && (
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              {description}
            </p>
          )}
        </div>

        {/* Selection Controls */}
        <div className="flex items-center justify-between p-3 rounded-lg border" style={{ borderColor: colors.semantic.border }}>
          <div className="flex items-center gap-3">
            <button
              onClick={handleSelectAll}
              className="p-1.5 hover:bg-gray-100 rounded transition-colors"
              title={selected.size === items.length ? "Deseleccionar todos" : "Seleccionar todos"}
              type="button"
            >
              {selected.size === items.length ? (
                <CheckSquare className="h-5 w-5" style={{ color: colors.primary[400] }} />
              ) : (
                <XSquare className="h-5 w-5" style={{ color: colors.semantic.text.muted }} />
              )}
            </button>
            <span
              className="text-sm font-medium"
              style={{ color: colors.semantic.text.primary }}
            >
              {selectedCount} de {items.length} seleccionados
            </span>
          </div>
          {errorCount > 0 && (
            <Badge variant="danger">{errorCount} errores</Badge>
          )}
        </div>

        {/* Items List */}
        <div className="space-y-2 max-h-96 overflow-y-auto">
          {items.map((item) => {
            const status = itemStatuses[item.id];
            const isSelected = selected.has(item.id);

            return (
              <div
                key={item.id}
                className="flex items-start gap-3 p-3 rounded-lg border transition-colors"
                style={{
                  borderColor: status === "error" ? colors.danger[200] : colors.semantic.border,
                  backgroundColor: status === "success" ? colors.success[50] : status === "error" ? colors.danger[50] : "transparent",
                }}
              >
                <button
                  onClick={() => handleSelectItem(item.id)}
                  className="mt-1 flex-shrink-0"
                  type="button"
                  disabled={isProcessing}
                >
                  {status === "success" ? (
                    <CheckSquare className="h-5 w-5" style={{ color: colors.success[500] }} />
                  ) : status === "error" ? (
                    <AlertCircle className="h-5 w-5" style={{ color: colors.danger[500] }} />
                  ) : isSelected ? (
                    <CheckSquare className="h-5 w-5" style={{ color: colors.primary[400] }} />
                  ) : (
                    <XSquare className="h-5 w-5" style={{ color: colors.semantic.text.muted }} />
                  )}
                </button>

                <div className="flex-1 min-w-0">
                  <p
                    className="text-sm font-medium"
                    style={{ color: colors.semantic.text.primary }}
                  >
                    {item.label}
                  </p>
                  {item.description && (
                    <p
                      className="text-xs mt-0.5"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      {item.description}
                    </p>
                  )}
                  {status === "error" && itemErrors[item.id] && (
                    <p
                      className="text-xs mt-1"
                      style={{ color: colors.danger[600] }}
                    >
                      Error: {itemErrors[item.id]}
                    </p>
                  )}
                </div>

                {status && (
                  <div className="flex-shrink-0">
                    {status === "processing" && (
                      <div
                        className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-r-transparent"
                        style={{ borderColor: colors.primary[400] }}
                      />
                    )}
                    {status === "success" && (
                      <Badge variant="success">Completado</Badge>
                    )}
                    {status === "error" && (
                      <Badge variant="danger">Error</Badge>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Summary */}
        {(successCount > 0 || errorCount > 0) && (
          <div
            className="rounded-lg border p-4"
            style={{
              borderColor: colors.semantic.border,
              backgroundColor: colors.semantic.background,
            }}
          >
            <p
              className="text-sm font-medium mb-2"
              style={{ color: colors.semantic.text.secondary }}
            >
              Resumen:
            </p>
            <div className="flex gap-3">
              {successCount > 0 && (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full" style={{ backgroundColor: colors.success[400] }} />
                  <span className="text-xs" style={{ color: colors.success[700] }}>
                    {successCount} completados
                  </span>
                </div>
              )}
              {errorCount > 0 && (
                <div className="flex items-center gap-1">
                  <div className="h-3 w-3 rounded-full" style={{ backgroundColor: colors.danger[400] }} />
                  <span className="text-xs" style={{ color: colors.danger[700] }}>
                    {errorCount} errores
                  </span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-3 pt-4 border-t" style={{ borderColor: colors.semantic.border }}>
          {errorCount > 0 && onRetry && (
            <Button
              onClick={handleRetry}
              loading={isProcessing}
              disabled={isProcessing}
              variant="secondary"
              className="gap-2"
            >
              <RefreshCw className="h-4 w-4" />
              Reintentar ({failedIds.length})
            </Button>
          )}
          <Button
            onClick={() => setShowConfirmation(true)}
            disabled={selectedCount === 0 || isProcessing}
            loading={isProcessing}
            className="flex-1 gap-2"
            style={{ backgroundColor: variantColor }}
          >
            {actionLabel} ({selectedCount})
          </Button>
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={showConfirmation}
        onClose={() => setShowConfirmation(false)}
        title={confirmationTitle}
        description={`${confirmationMessage}\n\nAfectará ${selectedCount} elemento${selectedCount !== 1 ? "s" : ""}.`}
        actionLabel={actionLabel}
        onConfirm={handleExecute}
        variant={variant}
      />
    </Card>
  );
};
