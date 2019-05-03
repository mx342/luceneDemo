package com.cyjz.activiti;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.List;
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
     * 功能描述 : 流程定义4步骤
     * 日期 2019/5/3 19:38  
     * 参数 
     * 返回值 void
     */
    @Test
    public void processDef() {
        //1.获得activiti相关service
        RepositoryService repositoryService = processEngine.getRepositoryService();
//        System.err.println(repositoryService);
        //2.createDeployment方法进行部署
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/diagram/holiday.png")
                .addClasspathResource("static/diagram/holiday.bpmn")
                .name("请假申请")
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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday");
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
        String assignee = "zhangsan";
        //2.创建TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holiday")
                .taskAssignee(assignee)
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
        String taskId = "10002";
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

}
