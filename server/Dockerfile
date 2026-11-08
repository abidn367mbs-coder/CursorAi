FROM node:20-alpine
WORKDIR /app
COPY package.json index.js ./
ENV NODE_ENV=production
EXPOSE 8080
USER node
CMD ["npm", "start"]
