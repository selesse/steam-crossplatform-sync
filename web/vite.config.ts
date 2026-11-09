import path from "node:path";
import react from "@vitejs/plugin-react-swc";
import { defineConfig } from "vite";

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      // Force all @babel/runtime imports to the top-level copy (7.29+),
      // which includes the esm/callSuper helper that react-big-calendar needs.
      "@babel/runtime": path.resolve("./node_modules/@babel/runtime"),
    },
  },
  build: {
    target: "esnext",
    commonjsOptions: {
      transformMixedEsModules: true,
    },
  },
  plugins: [react()],
});
