# syntax=docker/dockerfile:1

FROM node:20-bookworm-slim AS build
WORKDIR /app

COPY package.json ./

RUN npm install --include=optional

COPY . .

ARG VITE_API_URL=https://auditor-b-olegkolokolnikov.amvera.io/api/analyze
ENV VITE_API_URL=$VITE_API_URL

RUN rm -rf node_modules package-lock.json \
    && npm install --include=optional \
    && npm run build

FROM nginx:1.27-bookworm
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]