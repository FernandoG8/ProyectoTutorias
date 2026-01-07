export function mapBackendError(error: any): string {
  const code = error?.response?.data?.code || error?.code;
  const status = error?.response?.status;

  switch (code) {
    case "GOOGLE_NOT_LINKED":
      return 'Debes vincular tu cuenta de Google primero. Haz clic en "Vincular Google".';
    case "DRIVE_NOT_CONNECTED":
      return 'Conecta Google Drive para exportar archivos. Haz clic en "Conectar Drive".';
    case "NO_SHARED_DRIVE_ACCESS":
      return 'No tienes acceso a la unidad compartida "Tutolink Reportes". Contacta al administrador.';
    case "not_authorized":
      return "Tu cuenta no está autorizada. Vincula primero tu cuenta de Google.";
    default:
      if (status === 401) {
        return "Sesión expirada. Por favor inicia sesión nuevamente.";
      }
      return error?.response?.data?.message || "Error al exportar a Drive. Intenta nuevamente.";
  }
}

export function extractBackendCode(error: any): string | undefined {
  return error?.response?.data?.code || error?.code;
}
