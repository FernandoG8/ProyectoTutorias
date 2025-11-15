import { Button } from "@/components/ui/Button";
import type { LucideIcon } from "lucide-react";

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <Icon className="mb-4 h-16 w-16 text-slate-400" />
      <h3 className="mb-2 text-lg font-semibold text-text">{title}</h3>
      <p className="mb-6 max-w-md text-sm text-slate-600">{description}</p>
      {action && (
        <Button onClick={action.onClick} variant="primary">
          {action.label}
        </Button>
      )}
    </div>
  );
}

