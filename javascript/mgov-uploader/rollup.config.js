const commonjs = require('@rollup/plugin-commonjs');

module.exports = {
  input: "src/api.js",
  output: {
    file: "dist/api.mjs",
    format: "esm"
  },
  plugins: [commonjs()]
}