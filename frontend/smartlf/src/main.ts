import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入 Element Plus 及其 CSS 样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 🌟 修复：使用标准的 ESM 语言包路径，避免直接引用 .mjs 扩展名导致的类型缺失问题
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const app = createApp(App)

app.use(router)

// 🌟 全局注册 Element Plus 的同时，将语言设置为中文
app.use(ElementPlus, {
    locale: zhCn,
})

app.mount('#app')