import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入 Element Plus 及其 CSS 样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus) // 全局注册 Element Plus

app.mount('#app')