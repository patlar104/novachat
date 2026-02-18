FROM mcr.microsoft.com/playwright:v1.58.2-noble

ENV NODE_ENV=production

RUN npm install -g @playwright/mcp@0.0.68 @upstash/context7-mcp@1.0.31 \
    && npm cache clean --force
