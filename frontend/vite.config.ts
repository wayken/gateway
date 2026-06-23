import { resolve } from 'path'
import { defineConfig, loadEnv } from 'vite'
import { createHtmlPlugin } from 'vite-plugin-html'
import vue from '@vitejs/plugin-vue'
import eslintPlugin from 'vite-plugin-eslint'
import AutoImport from "unplugin-auto-import/vite"
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'

const loadConfig = ({ mode }) => {
  const {
    VITE_APP_TITLE,
    VITE_APP_PORT
  } = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      AutoImport ({
        imports: ['vue', 'pinia', 'vue-router', 'vue-i18n'],
        eslintrc: {
          enabled: true
        },
        dts: "src/auto-import.d.ts"
      }),
      createSvgIconsPlugin({
        iconDirs: [resolve(process.cwd(), 'src/assets/icons')],
        symbolId: 'icon-[dir]-[name]',
      }),
      eslintPlugin({
        include: ['src/**/*.js', 'src/**/*.ts', 'src/**/*.vue', 'src/*.js', 'src/*.vue']
      }),
      createHtmlPlugin({
        minify: true,
        inject: {
          data: {
            title: VITE_APP_TITLE
          }
        }
      })
    ],
    server: {
      host: false,
      port: Number(VITE_APP_PORT),
      open: false
    },
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
        'vue-i18n': 'vue-i18n/dist/vue-i18n.cjs.js'
      }
    },
    build: {
      target: 'es2015',
      outDir: resolve(__dirname, 'dist'),
      assetsDir: 'assets',
      assetsInlineLimit: 8192,
      emptyOutDir: true,
      chunkSizeWarningLimit: 1024,
      rollupOptions: {
        input: resolve(__dirname, 'index.html'),
        output: {
          chunkFileNames: 'js/[name].[hash].js',
          entryFileNames: 'js/[name].[hash].js',
          assetFileNames: '[ext]/[name].[hash].[ext]',
          manualChunks(id) {
            if (id.includes('node_modules')) {
              const modules = id.toString().split('node_modules/')[1].split('/')
              switch(modules[0]) {
                case '@vue':
                case 'vue-router':
                case 'vue-i18n':
                case 'axios':
                case 'monaco-editor':
                case 'mockjs':
                case 'element-plus':
                  return modules[0]
                default:
                  return 'vendor'
              }
            }
          }
        }
      }
    }
  }
}

// https://vitejs.dev/config/
export default defineConfig(loadConfig)
