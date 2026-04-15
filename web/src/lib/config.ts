export const config = {
  // Empty string = relative URLs — the browser calls the same origin it loaded from.
  // In dev (npm run dev) the Vite proxy forwards /api/* to the backend.
  // In Docker the nginx proxy does the same. No CORS issues either way.
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '',
  environment: (import.meta.env.VITE_ENV as string | undefined) ?? 'local',
} as const;
