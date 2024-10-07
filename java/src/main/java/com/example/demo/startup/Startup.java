package com.example.demo.startup;

import com.example.demo.task.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Startup类负责在Spring容器初始化完成后执行启动任务
 * 实现ApplicationListener<ContextRefreshedEvent>接口以监听Spring容器初始化完成事件
 */
@Component
public class Startup implements ApplicationListener<ContextRefreshedEvent> {

    // 自动注入任务调度器
    @Autowired
    private TaskScheduler taskScheduler;

    // 自动注入所有实现了InitializationTask接口的bean
    @Autowired
    private List<InitializationTask> initializationTasks;

    /**
     * 当接收到ContextRefreshedEvent事件时，此方法会被调用
     * @param event Spring容器刷新事件
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Spring容器初始化完成，开始执行启动任务...");

        try {
            initializationTasks.forEach(this::configureTask);

            CompletableFuture<Void> blockingTasks = CompletableFuture.runAsync(this::executeBlockingTasks);
            CompletableFuture<Void> nonBlockingTasks = CompletableFuture.runAsync(this::executeNonBlockingTasks);

            blockingTasks.join();
            System.out.println("阻塞任务执行完成，服务可以开始启动。");
            System.out.println("所有启动任务执行完成。");
        } catch (Exception e) {
            System.err.println("执行启动任务时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            taskScheduler.shutdown();
        }
    }

    private void configureTask(InitializationTask task) {
        taskScheduler.addTask(task);
        taskScheduler.addDependencies(task.getName(), task.getDependencies());
    }

    private void executeBlockingTasks() {
        try {
            taskScheduler.executeBlockingTasks();
        } catch (Exception e) {
            throw new RuntimeException("执行阻塞任务时发生错误", e);
        }
    }

    private void executeNonBlockingTasks() {
        try {
            taskScheduler.executeNonBlockingTasks();
        } catch (Exception e) {
            throw new RuntimeException("执行非阻塞任务时发生错误", e);
        }
    }
}