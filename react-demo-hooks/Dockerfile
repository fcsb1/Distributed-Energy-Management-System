# --- Stage 1: Build the React App ---
FROM node:18-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the app's source code
COPY . .

# Build the production app
RUN npm run build


# --- Stage 2: Serve the App with NGINX ---
FROM nginx:1.25-alpine

# Copy the built static files from the 'builder' stage
COPY --from=builder /app/build /usr/share/nginx/html

# Copy our custom NGINX configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80 (NGINX default port)
EXPOSE 80

# Default command to start NGINX
CMD ["nginx", "-g", "daemon off;"]