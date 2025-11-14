import { useEffect } from "react";
import { Calendar } from "lucide-react";
import { useSemestreStore } from "@/store/semestre-store";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";

export function SemestreSelector() {
  const { semestreActivo, isLoading, error, fetchSemestreActivo } = useSemestreStore();

  useEffect(() => {
    if (!semestreActivo && !isLoading && !error) {
      fetchSemestreActivo();
    }
  }, [semestreActivo, isLoading, error, fetchSemestreActivo]);

  if (isLoading) {
    return (
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-slate-400" />
        <Skeleton className="h-5 w-32" />
      </div>
    );
  }

  if (error || !semestreActivo) {
    return (
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-amber-500" />
        <span className="text-sm text-amber-600">Sin semestre activo</span>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <Calendar className="h-4 w-4 text-slate-500" />
      <div className="flex flex-col">
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-text">{semestreActivo.codigo}</span>
          {semestreActivo.activo && (
            <Badge variant="success" className="text-xs">
              Activo
            </Badge>
          )}
        </div>
        <span className="text-xs text-slate-500">{semestreActivo.nombre}</span>
      </div>
    </div>
  );
}

