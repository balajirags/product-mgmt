import { test, expect, type Page } from '@playwright/test';

/**
 * Functional E2E — requires backend at http://localhost:8080 (proxied via nginx at :8081/api/).
 * All tests run serially in one worker so they share the same TITLE and database state.
 */

const TITLE = `E2E Test Product ${Date.now()}`;
const EDITED_TITLE = `${TITLE} — edited`;

/** Wait until the products list has finished loading (table or empty-state is visible). */
async function waitForList(page: Page) {
  await page.waitForFunction(
    () => {
      const body = document.body.innerText;
      return document.querySelector('table') !== null ||
             body.includes('No products') ||
             body.includes('Failed to load');
    },
    { timeout: 10_000 }
  );
}

// Serial: all these tests share database state — run in one worker in order.
test.describe.serial('Product CRUD — full lifecycle', () => {
  test('products list loads without crash', async ({ page }) => {
    await page.goto('/products');
    await waitForList(page);
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
  });

  test('create a new product', async ({ page }) => {
    await page.goto('/products/new');
    await expect(page.getByRole('heading', { name: /create product/i })).toBeVisible();

    await page.locator('form input').first().fill(TITLE);
    await page.getByRole('button', { name: /^create product$/i }).click();

    // Redirect to detail page on success
    await expect(page).toHaveURL(/\/products\/[0-9a-f-]{36}$/, { timeout: 12_000 });
    // Title appears in the h1 heading (not breadcrumb)
    await expect(page.getByRole('heading', { level: 1, name: TITLE })).toBeVisible();
  });

  test('product appears in the list', async ({ page }) => {
    await page.goto('/products');
    await waitForList(page);
    // Title is in a table cell — use row locator to avoid matching breadcrumb text
    await expect(page.locator('table').getByText(TITLE)).toBeVisible({ timeout: 8_000 });
  });

  test('view product detail shows all sections', async ({ page }) => {
    await page.goto('/products');
    await waitForList(page);
    await page.locator('table').getByText(TITLE).click();
    await expect(page).toHaveURL(/\/products\/[0-9a-f-]{36}$/);
    await expect(page.getByRole('heading', { level: 1, name: TITLE })).toBeVisible();
    await expect(page.getByText('DRAFT')).toBeVisible();
    await expect(page.getByRole('link', { name: /edit/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /^delete$/i })).toBeVisible();
  });

  test('edit product title', async ({ page }) => {
    await page.goto('/products');
    await waitForList(page);
    await page.locator('table').getByText(TITLE).click();
    await page.getByRole('link', { name: /edit/i }).click();

    await expect(page.getByRole('heading', { name: /edit product/i })).toBeVisible();

    await page.locator('form input').first().fill(EDITED_TITLE);
    await page.getByRole('button', { name: /save changes/i }).click();

    await expect(page).toHaveURL(/\/products\/[0-9a-f-]{36}$/, { timeout: 10_000 });
    await expect(page.getByRole('heading', { level: 1, name: EDITED_TITLE })).toBeVisible();
  });

  test('delete product with confirmation', async ({ page }) => {
    await page.goto('/products');
    await waitForList(page);
    await page.locator('table').getByText(EDITED_TITLE).click();

    await page.getByRole('button', { name: /^delete$/i }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await page.getByRole('dialog').getByRole('button', { name: /^delete$/i }).click();

    await expect(page).toHaveURL(/\/products$/, { timeout: 10_000 });
    // Product no longer in table
    await expect(page.locator('table').getByText(EDITED_TITLE)).not.toBeVisible({ timeout: 5_000 });
  });
});

test.describe.serial('Batch create', () => {
  test('batch create two products and see results', async ({ page }) => {
    await page.goto('/products/batch');

    const t1 = `Batch A ${Date.now()}`;
    const t2 = `Batch B ${Date.now() + 1}`;

    const rows = page.getByPlaceholder(/product title/i);
    await rows.first().fill(t1);
    await page.getByRole('button', { name: /\+ add row/i }).click();
    await rows.nth(1).fill(t2);

    await page.getByRole('button', { name: /run batch/i }).click();

    await expect(page.getByText(/succeeded/i)).toBeVisible({ timeout: 12_000 });
    await expect(page.getByText(/\b0 failed/i)).toBeVisible();
  });
});
