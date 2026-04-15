export const config = {
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080',
  environment: (import.meta.env.VITE_ENV as string | undefined) ?? 'local',
} as const;
