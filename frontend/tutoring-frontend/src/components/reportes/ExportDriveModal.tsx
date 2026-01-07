import { useEffect, useRef, useState } from "react";
import { getExportMasivoStatus, startExportMasivoDriveJob } from "@/services/reportesDrive";
import { mapBackendError } from "@/utils/errorMapper";
import type { ExportJobStatusResponse } from "@/types/drive";

interface Props {
  isOpen: boolean;
  onClose: () => void;
  semestre: string;
}

export const ExportDriveModal = ({ isOpen, onClose, semestre }: Props) => {
  const [formato, setFormato] = useState<"PDF" | "EXCEL">("EXCEL");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [jobId, setJobId] = useState<string | null>(null);
  const [status, setStatus] = useState<ExportJobStatusResponse | null>(null);
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);

  const stopPolling = () => {
    if (timer.current) {
      clearInterval(timer.current);
      timer.current = null;
    }
  };

  useEffect(() => {
    return () => stopPolling();
  }, []);

  const pollStatus = (id: string) => {
    stopPolling();
    timer.current = setInterval(async () => {
      try {
        const res = await getExportMasivoStatus(id);
        setStatus(res);
        if (["SUCCESS", "FAILED", "PARTIAL_SUCCESS", "CANCELLED"].includes(res.status)) {
          stopPolling();
          setLoading(false);
        }
      } catch (err) {
        stopPolling();
        setLoading(false);
        setError(mapBackendError(err));
      }
    }, 1500);
  };

  const handleExport = async () => {
    if (!semestre) {
      setError("Debes seleccionar un semestre primero.");
      return;
    }
    setLoading(true);
    setError(null);
    setStatus(null);
    try {
      const start = await startExportMasivoDriveJob({ semestre, formato });
      setJobId(start.jobId);
      pollStatus(start.jobId);
    } catch (err) {
      setError(mapBackendError(err));
      setLoading(false);
    } finally {
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="w-full max-w-md rounded-lg bg-white p-6">
        <h2 className="text-xl font-semibold text-text">Exportar a Google Drive</h2>
        <p className="mt-1 text-sm text-slate-600">
          Se exportarán todos los reportes del semestre seleccionado a la unidad compartida.
        </p>

        <div className="mt-4 space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium text-text">Formato</label>
            <select
              className="w-full rounded border px-3 py-2 text-sm"
              value={formato}
              onChange={(e) => setFormato(e.target.value as "PDF" | "EXCEL")}
            >
              <option value="EXCEL">Excel</option>
              <option value="PDF">PDF</option>
            </select>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-text">Semestre</label>
            <input
              className="w-full rounded border bg-slate-100 px-3 py-2 text-sm"
              value={semestre || ""}
              disabled
            />
          </div>

          {error && <p className="rounded bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

          {jobId && (
            <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
              <div className="flex items-center justify-between">
                <span className="font-semibold">Job ID</span>
                <span className="font-mono text-xs">{jobId}</span>
              </div>
              {status && (
                <div className="mt-2 space-y-1">
                  <div className="flex items-center justify-between">
                    <span>Estado</span>
                    <span className="font-semibold">{status.status}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Progreso</span>
                    <span>
                      {status.processed ?? 0}/{status.total ?? 0} ({status.progressPct ?? 0}%)
                    </span>
                  </div>
                  {status.folderLink && (
                    <a
                      href={status.folderLink}
                      target="_blank"
                      rel="noreferrer"
                      className="text-blue-600 hover:underline"
                    >
                      Abrir carpeta en Drive
                    </a>
                  )}
                  {status.files && status.files.length > 0 && (
                    <div className="max-h-40 overflow-y-auto text-xs">
                      {status.files.map((file, idx) => (
                        <div key={idx} className="flex items-center justify-between border-b py-1 last:border-0">
                          <span className="truncate pr-2" title={file.fileName}>
                            {file.fileName || file.entity}
                          </span>
                          <span className={file.status === "SUCCESS" ? "text-green-600" : "text-rose-600"}>
                            {file.status}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        <div className="mt-6 flex gap-3">
          <button
            className="flex-1 rounded border px-4 py-2 text-sm hover:bg-slate-50"
            onClick={onClose}
            disabled={loading}
          >
            Cancelar
          </button>
          <button
            className="flex-1 rounded bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
            onClick={handleExport}
            disabled={loading || !semestre}
          >
            {loading ? "Exportando..." : "Confirmar"}
          </button>
        </div>
      </div>
    </div>
  );
};
