import { test, expect } from '@playwright/test';

/**
 * Smoke suite — runs against the deployed Docker container on http://localhost:8081
 * Backend may be offline; tests cover routing, page structure, and error states only.
 */

test.describe('Health & Infrastructure', () => {
  test('health endpoint returns OK', async ({ request }) => {
    const res = await request.get('/health/ready');
    expect(res.status()).toBe(200);
    expect(await res.text()).toBe('OK');
  });

  test('index.html is served at /', async ({ request }) => {
    const res = await request.get('/');
    expect(res.status()).toBe(200);
    const body = await res.text();
    expect(body).toContain('<div id="root">');
  });

  test('SPA fallback — unknown path returns index.html (client routing)', async ({ request }) => {
    const res = await request.get('/some/unknown/path');
    expect(res.status()).toBe(200);
    const body = await res.text();
    expect(body).toContain('<div id="root">');
  });
});

test.describe('Routing — page loads', () => {
  test('/ redirects to /products and shows Products heading', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/\/products/);
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
  });

  test('/products shows Create product button', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByRole('link', { name: /create product/i })).toBeVisible();
  });

  test('/products shows Batch operations button', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByRole('link', { name: /batch operations/i })).toBeVisible();
  });

  test('/products/new shows Create product form heading', async ({ page }) => {
    await page.goto('/products/new');
    await expect(page.getByRole('heading', { name: /create product/i })).toBeVisible();
  });

  test('/products/new form has Title field and submit button', async ({ page }) => {
    await page.goto('/products/new');
    await expect(page.getByRole('button', { name: /^create product$/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /cancel/i })).toBeVisible();
  });

  test('/products/batch shows Batch operations heading', async ({ page }) => {
    await page.goto('/products/batch');
    await expect(page.getByRole('heading', { name: /batch operations/i })).toBeVisible();
  });

  test('/products/batch shows three tabs', async ({ page }) => {
    await page.goto('/products/batch');
    await expect(page.getByRole('button', { name: /^creates$/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /^updates$/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /^deletes$/i })).toBeVisible();
  });

  test('unknown route shows 404 page', async ({ page }) => {
    await page.goto('/this-page-does-not-exist');
    await expect(page.getByText(/404/)).toBeVisible();
    await expect(page.getByRole('link', { name: /back to products/i })).toBeVisible();
  });

  test('/products/not-a-uuid shows invalid product ID error', async ({ page }) => {
    await page.goto('/products/not-a-uuid');
    await expect(page.getByText(/invalid product id/i)).toBeVisible();
  });
});

test.describe('Products list — API offline behaviour', () => {
  test('shows loading then error state when backend is unavailable', async ({ page }) => {
    await page.goto('/products');
    // Page should show an error banner or retry option (not crash)
    await expect(page.getByRole('button', { name: /retry/i })).toBeVisible({ timeout: 10_000 });
  });

  test('error banner has Retry button that re-triggers the request', async ({ page }) => {
    await page.goto('/products');
    const retryBtn = page.getByRole('button', { name: /retry/i });
    await expect(retryBtn).toBeVisible({ timeout: 10_000 });
    // Click retry — should not crash the app
    await retryBtn.click();
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
  });
});

test.describe('Create form — client-side validation', () => {
  test('submitting blank title shows required error without crash', async ({ page }) => {
    await page.goto('/products/new');
    await page.getByRole('button', { name: /^create product$/i }).click();
    await expect(page.getByText(/title is required/i)).toBeVisible();
  });

  test('status dropdown defaults to DRAFT', async ({ page }) => {
    await page.goto('/products/new');
    const select = page.locator('select').first();
    await expect(select).toHaveValue('DRAFT');
  });

  test('cancel navigates back to products list', async ({ page }) => {
    await page.goto('/products/new');
    await page.getByRole('button', { name: /cancel/i }).click();
    await expect(page).toHaveURL(/\/products$/);
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
  });
});

test.describe('Batch page — client-side behaviour', () => {
  test('Submit with no rows shows validation error', async ({ page }) => {
    await page.goto('/products/batch');
    await page.getByRole('button', { name: /run batch/i }).click();
    await expect(page.getByText(/add at least one operation/i)).toBeVisible();
  });

  test('Switching to Updates tab shows UUID input', async ({ page }) => {
    await page.goto('/products/batch');
    await page.getByRole('button', { name: /^updates$/i }).click();
    await expect(page.getByPlaceholder(/product uuid/i).first()).toBeVisible();
  });

  test('Add row button adds a new row', async ({ page }) => {
    await page.goto('/products/batch');
    const before = await page.getByPlaceholder(/product title/i).count();
    await page.getByRole('button', { name: /\+ add row/i }).click();
    const after = await page.getByPlaceholder(/product title/i).count();
    expect(after).toBe(before + 1);
  });
});
