package com.coflxl.api.admin.controller.wf;

import com.coflxl.api.common.response.ApiResponse;
import com.coflxl.api.core.datasource.DynamicDataSourceContextHolder;
import com.coflxl.api.core.domain.entity.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.Task;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/wf")
public class WorkflowController {

    @Autowired
    private SqlToyLazyDao sqlToyLazyDao;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    // 1. 获取流程定义列表
    @GetMapping("/def/list")
    public ApiResponse<Page<SysWfDef>> listDefs(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String typeCode
    ) {
        org.sagacity.sqltoy.model.Page<SysWfDef> page = new org.sagacity.sqltoy.model.Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, name, type_code as typeCode, status, created_at as createdAt from sys_wf_def " +
                "where 1=1 " +
                "#[and name like :name] " +
                "#[and type_code = :typeCode] " +
                "order by id desc";
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("name", org.springframework.util.StringUtils.hasText(name) ? "%" + name.trim() + "%" : null);
        params.put("typeCode", org.springframework.util.StringUtils.hasText(typeCode) ? typeCode.trim() : null);
        page = sqlToyLazyDao.findPageBySql(page, sql, params, SysWfDef.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(page);
    }

    // 2. 获取单个流程定义 XML
    @GetMapping("/def/detail/{id}")
    public ApiResponse<SysWfDef> getDefDetail(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        SysWfDef sysWfDef = new SysWfDef();
        sysWfDef.setId(id);
        SysWfDef def = sqlToyLazyDao.load(sysWfDef);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(def);
    }

    // 3. 保存/部署流程
    @PostMapping("/def/save")
    public ApiResponse<Boolean> saveDef(@RequestBody SysWfDef def) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        if (def.getCreatedAt() == null) {
            def.setCreatedAt(new Date());
            def.setStatus("DRAFT");
        }
        sqlToyLazyDao.saveOrUpdate(def);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    // 3.1 切换流程发布状态
    @PostMapping("/def/publish")
    public ApiResponse<Boolean> publishDef(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = (String) params.get("status");
        DynamicDataSourceContextHolder.set("PRIMARY");
        SysWfDef sysWfDef = new SysWfDef();
        sysWfDef.setId(id);
        SysWfDef def = sqlToyLazyDao.load(sysWfDef);
        if (def != null) {
            def.setStatus(status);
            sqlToyLazyDao.update(def);

            // 如果是发布状态，则真正部署到 Flowable
            if ("ACTIVE".equals(status)) {
                repositoryService.createDeployment()
                        .name(def.getName())
                        .addString("process_" + def.getId() + ".bpmn20.xml", def.getXmlData())
                        .deploy();
            }
        }
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    // 3.2 删除流程
    @PostMapping("/def/delete/{id}")
    public ApiResponse<Boolean> deleteDef(@PathVariable("id") Long id) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        SysWfDef sysWfDef = new SysWfDef();
        sysWfDef.setId(id);
        sqlToyLazyDao.delete(sysWfDef);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    // 4. 发起任意流程
    @PostMapping("/process/start")
    public ApiResponse<Boolean> startProcess(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> params) {
        Long defId = Long.valueOf(params.get("defId").toString());
        String formId = (String) params.get("formId"); // 关联表单定义
        Object dataContentObj = params.get("dataContent");
        String dataContent = "";
        try {
            dataContent = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dataContentObj);
        } catch (Exception e) {}

        DynamicDataSourceContextHolder.set("PRIMARY");

        // 1. 获取流程定义
        SysWfDef sysWfDef = new SysWfDef();
        sysWfDef.setId(defId);
        SysWfDef def = sqlToyLazyDao.load(sysWfDef);
        if (def == null || !"ACTIVE".equals(def.getStatus())) {
            DynamicDataSourceContextHolder.clear();
            return ApiResponse.error(500, "流程定义不存在或未发布");
        }

        // 2. 创建业务实例
        LcBusinessInstance instance = new LcBusinessInstance();
        instance.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        instance.setFormId(formId);
        instance.setDataContent(dataContent);
        instance.setStatus("RUNNING");
        instance.setCreator(String.valueOf(userId));
        instance.setCreateTime(new Date());

        String processKey = extractProcessId(def.getXmlData());
        if (processKey == null) {
            processKey = "process_" + defId;
        }

        // 3. 将 JSON 作为变量传入 Flowable，以支持网关条件判断 (比如 ${amount > 1000})
        Map<String, Object> variables = new HashMap<>();
        variables.put("starterId", String.valueOf(userId));
        variables.put("businessId", instance.getId());
        if (dataContentObj instanceof Map) {
            variables.putAll((Map<String, Object>) dataContentObj);
        }

        // 4. 交给 Flowable 引擎创建流程实例
        org.flowable.engine.runtime.ProcessInstance procInst = runtimeService.startProcessInstanceByKey(processKey, instance.getId(), variables);
        instance.setProcInsId(procInst.getId());

        sqlToyLazyDao.save(instance);

        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    private String extractProcessId(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            NodeList processes = doc.getElementsByTagNameNS("*", "process");
            if (processes == null || processes.getLength() == 0) {
                processes = doc.getElementsByTagName("process");
            }
            if (processes != null && processes.getLength() > 0) {
                Element process = (Element) processes.item(0);
                return process.getAttribute("id");
            }
        } catch (Exception e) {}
        return null;
    }

    // 5. 查看我的代办任务
    @GetMapping("/task/my")
    public ApiResponse<Page<Map>> myTasks(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String typeCode
    ) {
        Page<Map> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        DynamicDataSourceContextHolder.set("PRIMARY");
        String userIdStr = String.valueOf(userId);

        // 获取用户所属的角色/部门(作为组)
        String groupSql = "select r.code from sys_role r join sys_user_role ur on r.id = ur.role_id where ur.user_id = :userId " +
                "union select cast(dept_id as char) from sys_user where id = :userId";
        java.util.Map<String, Object> groupParams = new java.util.HashMap<>();
        groupParams.put("userId", userId);
        List<String> userGroups = sqlToyLazyDao.findBySql(groupSql, groupParams, String.class);
        if (userGroups == null) userGroups = new java.util.ArrayList<>();

        // 联合查询 Flowable 原生表和 lc_business_instance 业务表
        String sql = "select t.ID_ as taskId, t.NAME_ as taskName, 'PENDING' as taskStatus, t.CREATE_TIME_ as taskTime, " +
                "b.id as businessId, b.data_content as dataContent, b.form_id as formId, fd.name as formName, u.username as starterName " +
                "from ACT_RU_TASK t " +
                "JOIN ACT_RU_EXECUTION e ON t.PROC_INST_ID_ = e.PROC_INST_ID_ AND e.PARENT_ID_ IS NULL " +
                "join lc_business_instance b on e.BUSINESS_KEY_ = b.id " +
                "left join lc_form_definition fd on b.form_id = fd.id " +
                "left join sys_user u on b.creator = cast(u.id as char) " +
                "where (t.ASSIGNEE_ = :userIdStr " +
                "  or exists (select 1 from ACT_RU_IDENTITYLINK l where l.TASK_ID_ = t.ID_ and l.TYPE_ = 'candidate' and l.USER_ID_ = :userIdStr) " +
                "  #[or exists (select 1 from ACT_RU_IDENTITYLINK l where l.TASK_ID_ = t.ID_ and l.TYPE_ = 'candidate' and l.GROUP_ID_ in (:userGroups))] " +
                ") " +
                "#[and t.NAME_ like :taskName] " +
                "order by t.CREATE_TIME_ desc";

        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userIdStr", userIdStr);
        params.put("userGroups", userGroups.isEmpty() ? null : userGroups);
        params.put("taskName", org.springframework.util.StringUtils.hasText(taskName) ? "%" + taskName.trim() + "%" : null);

        Page<Map> result = sqlToyLazyDao.findPageBySql(page, sql, params, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    // 6. 审批操作
    @PostMapping("/task/complete")
    public ApiResponse<Boolean> completeTask(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> params) {
        String taskId = params.get("taskId").toString();
        String outcome = (String) params.get("outcome"); // "APPROVE" or "REJECT"
        String comment = (String) params.get("comment");

        // 调用 Flowable 的 TaskService 来处理
        Map<String, Object> variables = new HashMap<>();
        variables.put("outcome", outcome);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ApiResponse.error(500, "当前任务已失效或已处理");
        }

        String processInstanceId = task.getProcessInstanceId();
        org.flowable.engine.runtime.ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        // 签收保护
        if (task.getAssignee() == null) {
            taskService.claim(taskId, String.valueOf(userId));
        }

        // 写入审计日志
        DynamicDataSourceContextHolder.set("PRIMARY");
        String businessId = processInstance.getBusinessKey();

        LcAuditLog log = new LcAuditLog();
        log.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        log.setBusinessId(businessId);
        log.setTaskId(taskId);
        log.setTaskName(task.getName());
        log.setAssignee(String.valueOf(userId));
        log.setAction(outcome);
        log.setComment(comment);
        log.setCreateTime(new Date());
        sqlToyLazyDao.save(log);

        taskService.complete(taskId, variables);

        LcBusinessInstance instance = new LcBusinessInstance();
        instance.setId(businessId);
        instance = sqlToyLazyDao.load(instance);

        // 如果是驳回，直接终止 Flowable 任务
        if ("REJECT".equals(outcome)) {
            runtimeService.deleteProcessInstance(processInstanceId, "Rejected by user");
            instance.setStatus("REJECTED");
            sqlToyLazyDao.update(instance);
        } else {
            // 检查流程是否结束
            long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count();
            if (count == 0) {
                // 流程走到尽头
                instance.setStatus("COMPLETED");
                sqlToyLazyDao.update(instance);
            }
        }

        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(true);
    }

    // 7. 获取可用的审批人(管理员和任意用户列表)，为了在表单中选择
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> getUsers() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, username, nickname from sys_user order by id asc";
        List result = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    // 8. 获取当前登录人的所有流程申请记录
    @GetMapping("/process/my")
    public ApiResponse<Page<Map>> myProcesses(
            @RequestParam(defaultValue = "1") Long pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String formName
    ) {
        Page<Map<String, Object>> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select b.id as businessId, b.data_content as dataContent, b.status, b.create_time as createTime, fd.name as formName, b.proc_ins_id as procInsId " +
                "from lc_business_instance b " +
                "left join lc_form_definition fd on b.form_id = fd.id " +
                "where b.creator = cast(:userId as char) " +
                "#[and fd.name like :formName] " +
                "order by b.create_time desc";

        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        params.put("formName", org.springframework.util.StringUtils.hasText(formName) ? "%" + formName.trim() + "%" : null);

        Page<Map> result = sqlToyLazyDao.findPageBySql(page, sql, params, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    // 9. 获取Active流程定义
    @GetMapping("/def/active")
    public ApiResponse<List<Map<String, Object>>> activeDefs() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, name, type_code as typeCode from sys_wf_def where status = 'ACTIVE' order by id desc";
        List result = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    // 10. 获取表单定义列表
    @GetMapping("/form/list")
    public ApiResponse<List<Map<String, Object>>> listForms() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, name, schema_json as schemaJson from lc_form_definition order by id desc";
        List result = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

    // 11. 流程流转审计日志
    @GetMapping("/audit/history")
    public ApiResponse<List<LcAuditLog>> auditHistory(@RequestParam("businessId") String businessId) {
        DynamicDataSourceContextHolder.set("PRIMARY");
        String sql = "select id, business_id as businessId, task_id as taskId, task_name as taskName, " +
                "assignee, action, comment, create_time as createTime " +
                "from lc_audit_log where business_id = :businessId order by create_time asc";
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("businessId", businessId);
        List<LcAuditLog> logs = sqlToyLazyDao.findBySql(sql, params, LcAuditLog.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(logs);
    }

    // 12. 流程图及高亮
    @GetMapping("/process/viewer")
    public ApiResponse<Map<String, Object>> processViewer(@RequestParam("procInsId") String procInsId) {
        if (procInsId == null || procInsId.isEmpty()) {
            return ApiResponse.error(400, "procInsId 不能为空");
        }

        // 1. 先查运行中的实例
        org.flowable.engine.runtime.ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery().processInstanceId(procInsId).singleResult();

        String processDefId;
        List<String> activeIds = new java.util.ArrayList<>(); // 活跃节点

        if (processInstance != null) {
            processDefId = processInstance.getProcessDefinitionId();
            // 运行中，可以获取活跃节点
            activeIds = runtimeService.getActiveActivityIds(procInsId);
        } else {
            // 2. 运行中查不到，说明已结束，去历史记录拿
            org.flowable.engine.history.HistoricProcessInstance history =
                    historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId).singleResult();
            if (history == null) {
                return ApiResponse.error(404, "流程实例不存在");
            }
            processDefId = history.getProcessDefinitionId();
            // 已结束流程，活跃节点为空列表即可
        }

        // 3. 安全获取 BPMN XML (逻辑不变)
        org.flowable.bpmn.model.BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
        org.flowable.bpmn.converter.BpmnXMLConverter converter = new org.flowable.bpmn.converter.BpmnXMLConverter();
        byte[] xmlBytes = converter.convertToXML(bpmnModel);
        String xml = new String(xmlBytes);

        // 4. 获取已完成的节点 (从历史表中拿，无论运行中还是已结束都适用)
        List<org.flowable.engine.history.HistoricActivityInstance> historicActivityInstances =
                historyService.createHistoricActivityInstanceQuery().processInstanceId(procInsId).list();
        List<String> finishedIds = new java.util.ArrayList<>();
        for (org.flowable.engine.history.HistoricActivityInstance act : historicActivityInstances) {
            if (act.getEndTime() != null) {
                finishedIds.add(act.getActivityId());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("xml", xml);
        result.put("activeNodes", activeIds);
        result.put("completedNodes", finishedIds);

        return ApiResponse.success(result);
    }

    @GetMapping("/roles")
    // 在 WorkflowController.java 中添加
    // @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> getRoles() {
        DynamicDataSourceContextHolder.set("PRIMARY");
        // 查询所有角色的名称和代码
        String sql = "select code as id, name as name, code as id from sys_role order by id asc";
        List result = sqlToyLazyDao.findBySql(sql, null, Map.class);
        DynamicDataSourceContextHolder.clear();
        return ApiResponse.success(result);
    }

}
