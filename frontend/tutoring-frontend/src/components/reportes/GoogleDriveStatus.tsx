import { useEffect, useState } from "react";
import { getGoogleStatus, startDriveConnect, startGoogleLink, type GoogleStatus } from "@/services/googleAuth";

export const GoogleDriveStatus = () => {
  const [status, setStatus] = useState<GoogleStatus | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStatus();
  }, []);

  async function loadStatus() {
    try {
      const data = await getGoogleStatus();
      setStatus(data);
    } catch (err) {
      console.error("Error cargando estado de Google/Drive", err);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return <div className="h-10 w-48 animate-pulse rounded bg-slate-200" />;
  }

  if (!status) return null;

  return (
    <div className="flex flex-wrap items-center gap-3 rounded-lg border bg-white p-3">
      <div className="flex items-center gap-2">
        {status.driveConnected ? (
          <span className="rounded bg-emerald-100 px-2 py-1 text-xs font-semibold text-emerald-700">
            ✓ Drive conectado
          </span>
        ) : status.linked ? (
          <span className="rounded bg-amber-100 px-2 py-1 text-xs font-semibold text-amber-700">
            Google vinculado
          </span>
        ) : (
          <span className="rounded bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
            No vinculado
          </span>
        )}
        {status.googleEmail && <span className="text-sm text-slate-600">{status.googleEmail}</span>}
      </div>

      {!status.linked && (
        <button
          onClick={startGoogleLink}
          className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Vincular Google
        </button>
      )}

      {status.linked && !status.driveConnected && (
        <button
          onClick={startDriveConnect}
          className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Conectar Drive
        </button>
      )}

      {status.driveConnected && (
        <button
          onClick={startDriveConnect}
          className="rounded border px-3 py-1 text-sm text-slate-700 hover:bg-slate-50"
        >
          Reconectar
        </button>
      )}
    </div>
  );
};

