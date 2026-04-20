import { createRouter, createWebHistory } from 'vue-router'
import ApiManage from '../views/api/ApiManage.vue'
import DataSourceManage from '../views/system/DataSourceManage.vue'
import CallLog from '../views/system/CallLog.vue'
import Login from '../views/Login.vue'
import SqlWorkbench from '../views/api/SqlWorkbench.vue'
import CodeGen from '../views/system/CodeGen.vue'

import Home from '../views/Home.vue'
import SystemManage from '../views/system/SystemManage.vue'
import UserManage from '../views/system/UserManage.vue'
import RoleManage from '../views/system/RoleManage.vue'
import MenuManage from '../views/system/MenuManage.vue'
import DeptManage from '../views/system/DeptManage.vue'

import ReportTemplateManage from '../views/report/ReportTemplateManage.vue'
import ReportInstanceManage from '../views/report/ReportInstanceManage.vue'
import ReportDesigner from '../views/report/ReportDesigner.vue'
import ReportViewer from '../views/report/ReportViewer.vue'
import IFrameView from '../views/system/IFrameView.vue'

import ProcessDesigner from '../views/workflow/ProcessDesigner.vue'
import ProcessStart from '../views/workflow/ProcessStart.vue'
import MyTasks from '../views/workflow/MyTasks.vue'
import WfDefManage from '../views/workflow/WfDefManage.vue'
import FormManage from '../views/workflow/FormManage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/workflow/form',
      name: 'FormManage',
      component: FormManage
    },
    {
      path: '/workflow/def',
      name: 'WfDefManage',
      component: WfDefManage
    },
    {
      path: '/workflow/designer',
      name: 'ProcessDesigner',
      component: ProcessDesigner
    },
    {
      path: '/workflow/start',
      name: 'ProcessStart',
      component: ProcessStart
    },
    {
      path: '/workflow/tasks',
      name: 'MyTasks',
      component: MyTasks
    },
    {
      path: '/iframe',
      name: 'IFrameView',
      component: IFrameView
    },
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
      path: '/sys-user',
      name: 'UserManage',
      component: UserManage
    },
    {
      path: '/sys-role',
      name: 'RoleManage',
      component: RoleManage
    },
    {
      path: '/sys-menu',
      name: 'MenuManage',
      component: MenuManage
    },
    {
      path: '/sys-dept',
      name: 'DeptManage',
      component: DeptManage
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
  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
