import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'export',  // Enable static export for AWS Amplify
  images: {
    unoptimized: true,  // Required for static export
  },
  trailingSlash: true,  // Helps with routing on static hosts
};

export default nextConfig;
