import { useEffect } from "react";
import { Calendar } from "lucide-react";
import { useSemestreStore } from "@/store/semestre-store";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";

export function SemestreSelector() {
  const { semestreActivo, isLoading, error, hasAttemptedFetch, fetchSemestreActivo } = useSemestreStore();

  useEffect(() => {
    // Only fetch if we haven't attempted yet
    if (!hasAttemptedFetch && !isLoading) {
      fetchSemestreActivo();
    }
  }, [hasAttemptedFetch, isLoading, fetchSemestreActivo]);

  if (isLoading) {
    return (
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-white/50" />
        <Skeleton className="h-5 w-32" />
      </div>
    );
  }

  if (error || !semestreActivo) {
    return (
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-amber-300" />
        <span className="text-sm text-amber-200">Sin semestre activo</span>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <Calendar className="h-4 w-4 text-white/70" />
      <div className="flex flex-col">
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-white">{semestreActivo.codigo}</span>
          {semestreActivo.activo && (
            <Badge variant="success" className="text-xs bg-green-500/20 text-green-200 border-green-400">
              Activo
            </Badge>
          )}
        </div>
        <span className="text-xs text-white/60">{semestreActivo.nombre}</span>
      </div>
    </div>
  );
}

