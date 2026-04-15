import { createRouter, createWebHistory } from 'vue-router'
import ApiManage from '../views/api/ApiManage.vue'
import DataSourceManage from '../views/system/DataSourceManage.vue'
import CallLog from '../views/api/CallLog.vue'
import Login from '../views/Login.vue'
import SqlWorkbench from '../views/api/SqlWorkbench.vue'
import CodeGen from '../views/system/CodeGen.vue'

import Home from '../views/Home.vue'
import SystemManage from '../views/system/SystemManage.vue'

import ReportTemplateManage from '../views/report/ReportTemplateManage.vue'
import ReportInstanceManage from '../views/report/ReportInstanceManage.vue'
import ReportDesigner from '../views/report/ReportDesigner.vue'
import ReportViewer from '../views/report/ReportViewer.vue'

const router = createRouter({
  history: createWebHistory('/platform/'),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'Home',
      component: Home
    },
    {
      path: '/system-manage',
      name: 'SystemManage',
      component: SystemManage
    },
    {
      path: '/api-manage',
      name: 'ApiManage',
      component: ApiManage
    },
    {
      path: '/data-source-manage',
      name: 'DataSourceManage',
      component: DataSourceManage
    },
    {
      path: '/call-log',
      name: 'CallLog',
      component: CallLog
    },
    {
      path: '/sql-workbench',
      name: 'SqlWorkbench',
      component: SqlWorkbench
    },
    {
      path: '/code-gen',
      name: 'CodeGen',
      component: CodeGen
    },
    {
      path: '/report-template',
      name: 'ReportTemplateManage',
      component: ReportTemplateManage
    },
    {
      path: '/report-instance',
      name: 'ReportInstanceManage',
      component: ReportInstanceManage
    },
    {
      path: '/report-designer',
      name: 'ReportDesigner',
      component: ReportDesigner
    },
    {
      path: '/report/view/:id',
      name: 'ReportViewer',
      component: ReportViewer
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 公开访问的路由白名单
  const publicRoutes = ['Login', 'ReportViewer']

  if (publicRoutes.includes(to.name as string)) {
    next()
  } else if (!token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
