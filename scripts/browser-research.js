#!/usr/bin/env node
/**
 * Browser automation script for research and web scraping
 * 
 * Usage:
 *   node scripts/browser-research.js <url>
 *   npm run browser:research <url>
 * 
 * Example:
 *   node scripts/browser-research.js "https://developer.android.com/develop/ui/views/launcher/icon-design"
 */

const { chromium } = require('playwright');
const path = require('path');

const url = process.argv[2];

if (!url) {
  console.error('Usage: node scripts/browser-research.js <url>');
  process.exit(1);
}

// Determine Chrome executable path based on platform
let executablePath;
if (process.platform === 'darwin') {
  executablePath = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
} else if (process.platform === 'win32') {
  executablePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
} else {
  executablePath = '/usr/bin/google-chrome';
}

async function research() {
  console.log(`🌐 Navigating to: ${url}`);
  
  // Launch browser with Chrome
  const browser = await chromium.launch({
    headless: false, // Show browser window
    channel: 'chrome', // Try to use system Chrome first
    // Fallback to explicit path if channel doesn't work
    executablePath: executablePath,
  });

  const context = await browser.newContext({
    viewport: { width: 1280, height: 720 },
  });

  const page = await context.newPage();

  try {
    // Navigate to the URL
    await page.goto(url, { waitUntil: 'networkidle' });
    
    console.log('✅ Page loaded successfully');
    console.log(`📄 Title: ${await page.title()}`);
    
    // Get page content (accessibility snapshot style)
    const content = await page.evaluate(() => {
      return {
        title: document.title,
        url: window.location.href,
        headings: Array.from(document.querySelectorAll('h1, h2, h3, h4, h5, h6')).map(h => ({
          level: h.tagName,
          text: h.textContent?.trim(),
        })),
        links: Array.from(document.querySelectorAll('a[href]')).slice(0, 20).map(a => ({
          text: a.textContent?.trim(),
          href: a.href,
        })),
        mainContent: document.querySelector('main, article, [role="main"]')?.textContent?.substring(0, 2000) || 
                    document.body.textContent?.substring(0, 2000),
      };
    });

    console.log('\n📋 Page Structure:');
    console.log('='.repeat(60));
    console.log(`Title: ${content.title}`);
    console.log(`URL: ${content.url}`);
    console.log('\nHeadings:');
    content.headings.forEach(h => {
      console.log(`  ${h.level}: ${h.text}`);
    });
    
    console.log('\nKey Links:');
    content.links.slice(0, 10).forEach(link => {
      if (link.text) {
        console.log(`  - ${link.text}: ${link.href}`);
      }
    });

    console.log('\n📝 Main Content Preview:');
    console.log('-'.repeat(60));
    console.log(content.mainContent);
    console.log('-'.repeat(60));

    // Keep browser open for 30 seconds so you can see it
    console.log('\n⏳ Keeping browser open for 30 seconds... (Press Ctrl+C to close early)');
    await page.waitForTimeout(30000);

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await browser.close();
    console.log('\n✅ Browser closed');
  }
}

research().catch(console.error);
