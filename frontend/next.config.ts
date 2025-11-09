import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Let Amplify handle SSR - no export needed
  output: 'standalone',  // Optimized for serverless deployment
};

export default nextConfig;
