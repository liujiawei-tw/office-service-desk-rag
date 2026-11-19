FROM node:22-alpine AS build
WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
ARG VITE_HELPDESK_API_BASE_URL=
ARG VITE_TICKETS_API_BASE_URL=
ENV VITE_HELPDESK_API_BASE_URL=$VITE_HELPDESK_API_BASE_URL
ENV VITE_TICKETS_API_BASE_URL=$VITE_TICKETS_API_BASE_URL
RUN npm run build

FROM nginx:alpine AS runtime
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
