import globals from "globals";
import tseslint from "typescript-eslint";

// Note: eslint-config-google was not extended because it references valid-jsdoc,
// which was removed in ESLint 9+. Original .eslintrc did not extend Google either.

export default tseslint.config(
  { ignores: ["lib/**", "generated/**", "eslint.config.ts"] },
  ...tseslint.configs.recommended,
  {
    files: ["src/**/*.ts"],
    languageOptions: {
      parserOptions: {
        sourceType: "module",
        ecmaVersion: 2020,
      },
      globals: {
        ...globals.node,
      },
    },
    rules: {
      quotes: ["error", "double"],
      "no-unused-vars": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
        },
      ],
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-floating-promises": "warn",
      "@typescript-eslint/no-misused-promises": "warn",
    },
  }
);
