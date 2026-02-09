// @ts-check
/**
 * Example Playwright test for browser research and automation
 * 
 * Run with: npm run playwright:test tests/research.spec.js
 * Or use the VSCode extension to run individual tests
 */

const { test, expect } = require('@playwright/test');

test.describe('Browser Research', () => {
  test('research Android adaptive icon documentation', async ({ page }) => {
    // Navigate to Android icon design docs
    await page.goto('https://developer.android.com/develop/ui/views/launcher/icon-design');
    
    // Wait for page to load
    await page.waitForLoadState('networkidle');
    
    // Extract key information
    const title = await page.title();
    console.log('Page Title:', title);
    
    // Get all headings
    const headings = await page.$$eval('h1, h2, h3', elements => 
      elements.map(el => ({
        level: el.tagName,
        text: el.textContent?.trim(),
      }))
    );
    
    console.log('\nHeadings found:');
    headings.forEach(h => console.log(`  ${h.level}: ${h.text}`));
    
    // Search for specific content about safe zones
    const safeZoneText = await page.textContent('body');
    const hasSafeZone = safeZoneText?.includes('safe zone') || safeZoneText?.includes('72dp');
    
    console.log('\nContains safe zone information:', hasSafeZone);
    
    // Take a screenshot for reference
    await page.screenshot({ path: 'tests/screenshots/icon-docs.png', fullPage: true });
    
    expect(title).toContain('Icon');
  });

  test('research Compose BOM versions', async ({ page }) => {
    await page.goto('https://developer.android.com/jetpack/compose/bom/bom-mapping');
    await page.waitForLoadState('networkidle');
    
    const title = await page.title();
    console.log('BOM Mapping Page:', title);
    
    // Look for version dropdown or table
    const versionInfo = await page.textContent('body');
    const latestVersion = versionInfo?.match(/202[0-9]\.[0-9]{2}\.[0-9]{2}/)?.[0];
    
    console.log('Latest BOM version found:', latestVersion);
    
    await page.screenshot({ path: 'tests/screenshots/bom-mapping.png', fullPage: true });
    
    expect(title).toContain('BOM');
  });
});
