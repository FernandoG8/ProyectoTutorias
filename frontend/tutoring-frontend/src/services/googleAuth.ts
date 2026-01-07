import { api, apiBaseURL } from "@/lib/api";

export interface GoogleStatus {
  linked: boolean;
  driveConnected: boolean;
  googleEmail?: string;
}

export async function getGoogleStatus(): Promise<GoogleStatus> {
  const { data } = await api.get<GoogleStatus>("/auth/google/status");
  return data;
}

export function startGoogleLink(): void {
  window.location.href = `${apiBaseURL}/auth/google/link`;
}

export function startDriveConnect(): void {
  window.location.href = `${apiBaseURL}/auth/google/connect-drive`;
}

export async function disconnectDrive(): Promise<void> {
  await api.post("/auth/google/disconnect-drive");
}
