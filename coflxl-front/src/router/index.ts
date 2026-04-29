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
import SysCommonSearchManage from '../views/yzt/SysCommonSearchManage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/workflow/form',
      name: 'FormManage',
      component: FormManage,
      meta: { title: '流程管理>表单设计' }
    },
    {
      path: '/workflow/def',
      name: 'WfDefManage',
      component: WfDefManage,
      meta: { title: '流程管理>流程定义管理' }
    },
    {
      path: '/workflow/designer',
      name: 'ProcessDesigner',
      component: ProcessDesigner,
      meta: { title: '流程管理>流程设计器' }
    },
    {
      path: '/workflow/start',
      name: 'ProcessStart',
      component: ProcessStart,
      meta: { title: '流程管理>发起流程' }
    },
    {
      path: '/workflow/tasks',
      name: 'MyTasks',
      component: MyTasks,
      meta: { title: '流程管理>我的待办' }
    },
    {
      path: '/iframe',
      name: 'IFrameView',
      component: IFrameView,
      meta: { title: 'Iframe嵌入页面>内嵌页面' }
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
      component: Home,
      meta: { title: '首页概览' }
    },
    {
      path: '/system-manage',
      name: 'SystemManage',
      component: SystemManage,
      meta: { title: '系统管理>系统管理' }
    },
    {
      path: '/api-manage',
      name: 'ApiManage',
      component: ApiManage,
      meta: { title: 'API开放平台>接口管理' }
    },
    {
      path: '/data-source-manage',
      name: 'DataSourceManage',
      component: DataSourceManage,
      meta: { title: '系统管理>数据源管理' }
    },
    {
      path: '/call-log',
      name: 'CallLog',
      component: CallLog,
      meta: { title: '系统管理>调用日志' }
    },
    {
      path: '/sql-workbench',
      name: 'SqlWorkbench',
      component: SqlWorkbench,
      meta: { title: 'API开放平台>SQL 智慧工作台' }
    },
    {
      path: '/sys-user',
      name: 'UserManage',
      component: UserManage,
      meta: { title: '系统管理>用户管理' }
    },
    {
      path: '/sys-role',
      name: 'RoleManage',
      component: RoleManage,
      meta: { title: '系统管理>角色管理' }
    },
    {
      path: '/sys-menu',
      name: 'MenuManage',
      component: MenuManage,
      meta: { title: '系统管理>菜单管理' }
    },
    {
      path: '/sys-dept',
      name: 'DeptManage',
      component: DeptManage,
      meta: { title: '系统管理>部门管理' }
    },
    {
      path: '/code-gen',
      name: 'CodeGen',
      component: CodeGen,
      meta: { title: '系统管理>代码生成' }
    },
    {
      path: '/report-template',
      name: 'ReportTemplateManage',
      component: ReportTemplateManage,
      meta: { title: '报表生成器>报表模板管理' }
    },
    {
      path: '/report-instance',
      name: 'ReportInstanceManage',
      component: ReportInstanceManage,
      meta: { title: '报表生成器>报表实例管理' }
    },
    {
      path: '/report-designer',
      name: 'ReportDesigner',
      component: ReportDesigner,
      meta: { title: '报表生成器>报表设计器' }
    },
    {
      path: '/report/view/:id',
      name: 'ReportViewer',
      component: ReportViewer,
      meta: { title: '报表生成器>报表查看器' }
    },
    {
      path: '/yzt/sysCommonSearch',
      name: 'SysCommonSearchManage',
      component: SysCommonSearchManage,
      meta: { title: '一张图>SQL公共查询' }
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
