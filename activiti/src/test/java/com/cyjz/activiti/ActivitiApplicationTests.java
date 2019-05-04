package com.cyjz.activiti;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiApplicationTests {
    
    @Autowired
    ProcessEngine processEngine;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    /**
     * 作者 ZYL
     * 功能描述 : 生成表,整个程序只需要执行一次
     * 日期 2019/5/3 11:22
     * 参数
     * 返回值 void
     */
    @Test
    public void testGeTable(){
//        1.创建processEngineConfiguration对象
//        @Autowired
//        2.创建processEngine对象
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
//        processEngine.get

        //3.输出processEngine对象
        System.err.println(processEngine);

    }


    /**
     * 作者 ZYL
     * 功能描述 : 流程部署定义4步
     * 日期 2019/5/3 19:38  
     * 参数 
     * 返回值 void
     */
    @Test
    public void processDefine() {
        //1.获得activiti相关service
        RepositoryService repositoryService = processEngine.getRepositoryService();
//        System.err.println(repositoryService);
        //2.createDeployment方法进行部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/diagram/holiday4.png")
                .addClasspathResource("static/diagram/holiday4.bpmn")
                .name("请假申请4")
                .deploy();
        //3.输出部署的一些信息
        System.err.println("流程部署id:" + deploy.getId());
        System.err.println("流程部署名称:" + deploy.getName());
    }

    
    /**
     * 作者 ZYL
     * 功能描述 : 启动流程实例,前提是已经完成流程定义的部署工作
     * 背后影响的表：
     *  act_hi_actinst  已完成的活动实例
     *  act_hi_identitylink 参与者信息
     *  act_hi_procinst  流程实例
     *  act_hi_taskinst  任务实例
     *  act_ru_execution 执行表
     *  act_ru_identitylink 参与者信息
     *  act_ru_task 任务
     * 日期 2019/5/3 20:03  
     * 参数 
     * 返回值 void
     */
    @Test
    public void startProcessInstance(){
        //1.获取runtimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //2.根据流程定义key启动流程，此处需要知道流程定义的key
        //第一个参数指的是流程定义key,第二个参数指的是bussinessKey,此key只在act_ru_execution表里面会有，叫做业务主键
        //本质是act_ru_execution表中的businessKey的字段要存入业务标识
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday","2 ");
        //3.输出实例相关信息
        System.err.println("流程部署id:" + processInstance.getDeploymentId());
        System.err.println("流程实例id:" + processInstance.getId());
        System.err.println("流程定义id:" + processInstance.getProcessDefinitionId());
        System.err.println("流程活动id:" + processInstance.getActivityId());


    }
    /**
     * 作者 ZYL
     * 功能描述 : 查询用户的任务列表，天佑里面使用的是用户工号
     * 日期 2019/5/3 20:29  
     * 参数 
     * 返回值 void
     */
    @Test
    public void findPersonalTaskList(){
        //1.任务负责人
        String assignee = "zcj";
//        List<String> list2 = new ArrayList<>();

        //2.创建TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holiday4")
                .taskCandidateOrAssigned(assignee)
                .list();
        //任务列表的展示
        for (Task task : list) {
            System.err.println("流程实例id:" + task.getProcessInstanceId());
            System.err.println("任务id:" + task.getId());
            System.err.println("任务责任人：" + task.getAssignee());
            System.err.println("任务名称：" + task.getName());
            System.err.println("======================");
        }
    }
    /*
     * 作者 ZYL
     * 功能描述 : 处理任务完成任务
     * 如果某个流程实例被挂起，如果此时要让该实例继续执行，问题是：是否可以成功？
     * 如果不能执行，是否会抛出异常？ActivitiException: Cannot complete a suspended task
     * 操作的表：
     *  act_hi_actinst
     *  act_hi_identitylink
     *  act_hi_taskinst
     *  act_ru_execution
     *  act_ru_identitylikn
     *  act_ru_task
     * 日期 2019/5/3 20:36
     * 参数
     * 返回值 void
     */
    @Test
    public void completTask(){
        //任务id 测试为：2505,5005
        String taskId = "87511";
        //创建TaskService
        TaskService taskService = processEngine.getTaskService();
        //完成任务
        taskService.complete(taskId);
        System.err.println("完成任务id:" + taskId);
    }
    /**
     * 作者 ZYL
     * 功能描述 : zip方式部署流程
     * 日期 2019/5/3 21:24
     * 参数
     * 返回值 void
     */
    @Test
    public void deployProcessByZip(){
        //定义zip输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/diagram/holiday.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //流程部署
        Deployment deployment = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.err.println("流程部署id:" + deployment.getId());
        System.err.println("流程部署名称:" + deployment.getName());
    }
    /**
     * 作者 ZYL
     * 功能描述 : 流程定义的查询
     * 日期 2019/5/3 22:15
     * 参数
     * 返回值 void
     */
    @Test
    public void queryProceccDefinition(){
        //流程定义
        String processDefinitionKey = "holiday";
        //获取repositorySerice
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //查询流程定义
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        //遍历查询结果
        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionKey(processDefinitionKey)
                .orderByProcessDefinitionVersion().desc().list();
        for (ProcessDefinition processDefinition : list) {
            System.err.println("------------------");
            System.err.println("流程部署id:" + processDefinition.getId());
            System.err.println("流程定义名称:" + processDefinition.getName());
            System.err.println("流程定义key:" + processDefinition.getKey());
            System.err.println("流程定义版本:" + processDefinition.getVersion());
            System.err.println("流程部署的id:" + processDefinition.getDeploymentId());
        }


    }
    /**
     * 作者 ZYL
     * 功能描述 : 删除已经部署成功的流程定义
     * 日期 2019/5/3 22:32
     * 参数
     * 返回值 void
     */
    @Test
    public void deleteDeployment(){
        //流程部署id
        String deploymentId = "32501";
        //通过流程引擎获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //删除流程定义，如果该流程定义已经有流程实例启动则删除时出错
//        repositoryService.deleteDeployment(deploymentId);
        //设置true级联删除流程定义，即时该流程有流程实例启动也可以删除，设置为false非级别删除方式，如果流程
        repositoryService.deleteDeployment(deploymentId,true);
    }

    /**
     * 作者 ZYL
     * 需求：1.从activiti的act_ge_bytearray表中读取两个资源文件
     *      2.将两个资源文件保存到本地
     *      用户想要查看请假流程具体有哪些步骤要走
     * 技术方案：
     *      1.第一种：使用activiti的api来实现
     *      2.第二种：从原理层jdbc的blob或者clob类型读取
     *      3.IO流转换：最好commons-io.jar可以轻松解决
     * 功能描述 : 通过流程定义对象获取流程定义资源，获取bpmn和png
     * 日期 2019/5/3 23:15
     * 参数 null
     * 返回值
     */
    @Test
    public void getProcessResources() throws Exception{
        //流程定义id
        String processDefinitionKey = "holiday";
        //获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //流程定义对象
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        //查询到流程定义
        ProcessDefinition processDefinition = processDefinitionQuery.processDefinitionKey(processDefinitionKey).singleResult();
        //获取部署id
        String deploymentId = processDefinition.getDeploymentId();
        //获取bpmn资源的名称
        String resource_bpmn = processDefinition.getResourceName();
        //获取png图片资源的名称
        String resource_png = processDefinition.getDiagramResourceName();
//
        //通过repositoryService的方法，实现读取图片信息及bpmn文件信息（输入流）
        //getResourceAsStream，两个参数：1.表示部署id,2.表示资源名称
        InputStream pngIs = repositoryService.getResourceAsStream(deploymentId,resource_png);
        InputStream bpmnIs = repositoryService.getResourceAsStream(deploymentId,resource_bpmn);

        //构建outputStream流
        OutputStream pngOs = new FileOutputStream("E:\\BaiduNetdiskDownload\\3-1 Activiti7工作流引擎\\" + resource_png);
        OutputStream bpmnOs = new FileOutputStream("E:\\BaiduNetdiskDownload\\3-1 Activiti7工作流引擎\\" + resource_bpmn);
        //输入流，输出流的转换 commons-io的方法
        IOUtils.copy(pngIs,pngOs);
        IOUtils.copy(bpmnIs,bpmnOs);

        //关闭流
        pngIs.close();
        pngOs.close();
        bpmnIs.close();
        bpmnOs.close();
    }
    
    /**
     * 作者 ZYL
     * 功能描述 : 流程历史信息的查看
     * 描述：即时流程定义已经删除了，流程执行的历史信息通过前面的分析，依然保存在activiti的act_hi_*的相关表中，所以我们还是可以查询流程执行的历史信息，
     * 可以通过HistoryService来查看相关记录
     * 日期 2019/5/3 23:48  
     * 参数 null
     * 返回值
     */
    @Test
    public void testHistory01(){
        //获取historyService对象
        HistoryService historyService = processEngine.getHistoryService();
        //获取查询对象
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
        //添加查询条件
        query.processInstanceId("17501");
        //执行查询
        List<HistoricActivityInstance> list = query.list();
        //遍历结果
        for (HistoricActivityInstance historicActivityInstance : list) {
            System.err.println("activitiId:" + historicActivityInstance.getActivityId());
            System.err.println("activitiName:" + historicActivityInstance.getActivityName());
        }

    }
    /**
     * 作者 ZYL
     * 功能描述 : 挂起激活流程定义
     * 日期 2019/5/4 11:29
     * 参数 null
     * 返回值
     */
    @Test
    public void suspendOrActivateProcessDefinition(){
        //流程定义key
        String processDefinitionKey = "holiday";

        RepositoryService repositoryService = processEngine.getRepositoryService();
        //获得流程定义
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        query.processDefinitionKey(processDefinitionKey);
        ProcessDefinition processDefinition = query.singleResult();
        //是否暂停
        boolean suspend = processDefinition.isSuspended();
        if(suspend){
            //如果暂停则激活，这里将流程定义下的所有流程实例全部激活
            repositoryService.activateProcessDefinitionByKey(processDefinitionKey,true,null);
            System.err.println("流程定义：" + processDefinitionKey + "激活");
        }else {
            //如果激活则挂起，这里将流程定义下的所有流程实例全部挂起
            repositoryService.suspendProcessDefinitionByKey(processDefinitionKey,true,null);
            System.err.println("流程定义：" + processDefinitionKey + "挂起");
        }
    }

    /**
     * 作者 ZYL
     * 功能描述 : 单个流程实例挂起
     * 日期 2019/5/4 11:45
     * 参数 null
     * 返回值
     */
    @Test
    public void suspendOrActiveProcessInstance(){
        //流程实例id
        String processInstanceId = "30001";
        //获取RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //根据流程实例id查询流程实例
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        query.processInstanceId(processInstanceId);
        ProcessInstance processInstance = query.singleResult();
        boolean suspend = processInstance.isSuspended();
        if(suspend){
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.err.println("流程实例：" + processInstanceId + "激活");
        }else{
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.err.println("流程实例：" + processInstanceId + "挂起");
        }
    }
    //个人任务--------------------------------------------------------------------------------------------------------------------
    /**
     * 作者 ZYL
     * 功能描述 : 设置流程变量，在启动流程实例的时候设置流程变量
     * 背后影响的表：
     * 日期 2019/5/3 20:03
     * 参数
     * 返回值 void
     */
    @Test
    public void startProcessInstanceByVariables(){
        //1.获取runtimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //2.根据流程定义key启动流程，此处需要知道流程定义的key
        //第一个参数指的是流程定义key,第二个参数指的是bussinessKey,此key只在act_ru_execution表里面会有，叫做业务主键
        //定义流程变量
        Map<String,Object> variables = new HashMap<>();
        variables.put("assignee0","zyl");
        variables.put("assignee1","zcj");
        variables.put("assignee2","lby");
        variables.put("candidate0","ww");
        variables.put("candidate1","lj");
        variables.put("holidayNum","4");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday4",variables);

        //3.输出实例相关信息
        System.err.println("流程部署id:" + processInstance.getDeploymentId());
        System.err.println("流程实例id:" + processInstance.getId());
        System.err.println("流程定义id:" + processInstance.getProcessDefinitionId());
        System.err.println("流程活动id:" + processInstance.getActivityId());


    }
    /**
     * 作者 ZYL
     * 功能描述 : 监听器分配
     * create:任务创建后触发
     * assignment:任务分配后触发
     * delete:任务完成后触发
     * all:所有时间都触发
     * 日期 2019/5/4 12:42
     * 参数 null
     * 返回值
     */


    /**
     * 作者 ZYL
     * 功能描述 : 通过流程实例设置变量
     * 日期 2019/5/4 14:59
     * 参数 null
     * 返回值
     */
    @Test
    public void setGlobalVariableByExecutionId(){
        //当前流程实例执行id,通常设置为当前执行的流程实例
        String executionId = "";

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String,Object> map = new HashMap<>();
        map.put("holidayNum", 4);
        //通过流程实例id设置流程变量
        runtimeService.setVariable(executionId,"holidayNum",4);
        //一次设置多个值
//        runtimeService.setVariables(executionId,map);
    }
    /**
     * 作者 ZYL
     * 功能描述 : 通过当前任务id设置流程变量
     * 日期 2019/5/4 15:05
     * 参数 null
     * 返回值
     */
    @Test
    public void setGlobalVariableByTaskId(){
        String taskId = "";

        TaskService taskService = processEngine.getTaskService();

        Map<String,Object> map = new HashMap<>();
        map.put("holidayNum",4);
        //通过任务设置单个变量
//        taskService.setVariable(taskId,"holidayNum",4);
        //通过任务id设置多个变量
        taskService.setVariables(taskId,map);
    }

    /**
     * 作者 ZYL
     * 功能描述 : 设置local流程变量,任务办理时
     * 日期 2019/5/4 15:15
     * 参数 null
     * 返回值
     */
    @Test
    public void setLocalVariableByCompleteTask(){

        //任务id
        String taskId = "";

        TaskService taskService = processEngine.getTaskService();

        Map<String,Object> map = new HashMap<>();
        map.put("holidayNum",4);
        //设置单个key,value
//        taskService.setVariableLocal(taskId,"holidayNum",4);
        //设置多个key，value
        taskService.setVariablesLocal(taskId,map);

        taskService.complete(taskId);
    }
    /**
     * 作者 ZYL
     * 功能描述 : 组任务办理流程
     * 1.查询组任务，指定候选人，查询该候选人当前的待办任务，候选人不能办理任务
     * 2.拾取任务
     *      该组任务的所有候选人都能拾取。
     *      将候选人的组任务，变成个人任务，原来的候选人就变成了该任务的负责人
     * 3.如果拾取后不想办理该任务
     *      则需要将已经拾取的个人任务归还到组里边，将个人任务变成组任务
     *
     * 4.查询个人任务，查询方式同个人任务部分，根据assignee查询用户负责的个人任务
     *
     * 5.办理个人任务
     * 日期 2019/5/4 15:44  
     * 参数 null
     * 返回值 
     */
    @Test
    public void findGroupTaskList(){
        //1.任务负责人
        String assignee = "lj";
//        List<String> list2 = new ArrayList<>();

        //2.创建TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holiday4")
                .taskCandidateOrAssigned(assignee)
                .list();
        //任务列表的展示
        for (Task task : list) {
            System.err.println("流程实例id:" + task.getProcessInstanceId());
            System.err.println("任务id:" + task.getId());
            System.err.println("任务责任人：" + task.getAssignee());
            System.err.println("任务名称：" + task.getName());
            System.err.println("======================");
        }

    }

    /**
     * 作者 ZYL
     * 功能描述 : 拾取任务
     * 日期 2019/5/4 15:55
     * 参数 null
     * 返回值
     */
    @Test
    public void claimTask(){
        TaskService taskService = processEngine.getTaskService();
        //要拾取任务的id
        String taskId = "90002";
        //任务候选人id
        String userId = "lj";

        //拾取任务
        //即时该用户不是候选人也能拾取(建议拾取时校验是否有资格)
        //校验该用户有没有资格拾取任务
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskCandidateOrAssigned(userId)
                .singleResult();
        if(task != null){
            System.err.println("任务原责任人：" + task.getAssignee());
            taskService.unclaim(taskId);
            taskService.claim(taskId,userId);
            System.err.println(userId + "拾取任务" + task.getId());
            System.err.println("流程实例：" + task.getProcessInstanceId());
            System.err.println("任务id:" + task.getId());
            System.err.println("任务责任人：" + task.getAssignee());
            System.err.println("任务名称：" + task.getName());
        }
    }
    /**
     * 作者 ZYL
     * 功能描述 : 任务交接,前提是保证当前用户是这个任务的负责人，这时候他才可以交接任务给其他人
     * 日期 2019/5/4 16:30
     * 参数 null
     * 返回值
     */
    @Test
    public void setAssigneeToCandiateUser(){
        TaskService taskService = processEngine.getTaskService();
        //要拾取任务的id
        String taskId = "90002";
        //任务候选人id
        String userId = "lj";

        //拾取任务
        //即时该用户不是候选人也能拾取(建议拾取时校验是否有资格)
        //校验该用户有没有资格拾取任务
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskCandidateOrAssigned(userId)
                .singleResult();
        String toUser = "ww";
        if(task != null){
//            System.err.println("任务原责任人：" + task.getAssignee());
//            taskService.unclaim(taskId);
            taskService.setAssignee(taskId,toUser);//任务交接给userId
            System.err.println(userId + "交接任务" + task.getId() + "给" + toUser);
            System.err.println("流程实例：" + task.getProcessInstanceId());
            System.err.println("任务id:" + task.getId());
            System.err.println("任务责任人：" + task.getAssignee());
            System.err.println("任务名称：" + task.getName());
        }
    }
    /**
     * 作者 ZYL
     * 功能描述 : 排他网关原理，当出现多个true的时候，走的流程是  id较小的那个分支
     * 日期 2019/5/4 16:50  
     * 参数 null
     * 返回值 
     */

    /**
     * 作者 ZYL
     * 功能描述 : activiti7两个api
     * processRuntime
     * taskRuntime
     * 日期 2019/5/4 17:31
     * 参数 null
     * 返回值
     */
}
