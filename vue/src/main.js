// import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

// import App from './App.vue'
import App from './components/mytable.vue'


const app = createApp(App)

app.use(ElementPlus)
app.use(createPinia())

app.mount('#app')

const matchMediaDark = window.matchMedia("(prefers-color-scheme: dark)")
matchMediaDark.addEventListener("change",()=>{
  const htmlclass = document.getElementsByTagName('html')[0].classList
  if (matchMediaDark.matches) {
    console.log('dark')
    htmlclass.add('dark')
  }else{
    console.log('light');
    htmlclass.remove('dark')
  }
});
matchMediaDark.dispatchEvent(new Event('change'))


console.log('remove loading_dark');
document.getElementById('loading_dark').remove()
