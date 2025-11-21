import type { ReactNode } from "react";
import { FileX, Search, Users, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface EmptyStateProps {
  icon?: "search" | "file" | "users" | "alert" | ReactNode;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  className?: string;
}

/**
 * Componente para estados vacíos
 * 
 * Muestra un mensaje cuando no hay datos para mostrar
 */
export const EmptyState = ({
  icon = "file",
  title,
  description,
  action,
  className = ""
}: EmptyStateProps) => {
  const getIcon = () => {
    if (typeof icon !== "string") {
      return icon;
    }

    const iconClasses = "w-12 h-12 text-gray-400 mx-auto mb-4";
    
    switch (icon) {
      case "search":
        return <Search className={iconClasses} />;
      case "users":
        return <Users className={iconClasses} />;
      case "alert":
        return <AlertCircle className={iconClasses} />;
      default:
        return <FileX className={iconClasses} />;
    }
  };

  return (
    <div className={`text-center py-12 ${className}`}>
      {getIcon()}
      
      <h3 className="text-lg font-medium text-gray-900 mb-2">
        {title}
      </h3>
      
      {description && (
        <p className="text-gray-500 mb-6 max-w-md mx-auto">
          {description}
        </p>
      )}
      
      {action && (
        <Button onClick={action.onClick} variant="secondary">
          {action.label}
        </Button>
      )}
    </div>
  );
};
