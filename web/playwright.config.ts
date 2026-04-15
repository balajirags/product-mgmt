import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  tsconfig: './tsconfig.e2e.json',
  timeout: 15_000,
  retries: 1,
  reporter: [['list'], ['json', { outputFile: 'e2e/results.json' }]],
  use: {
    baseURL: 'http://localhost:8081',
    headless: true,
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
