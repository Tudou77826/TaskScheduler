package com.example.demo.task;

import org.springframework.stereotype.Component;

@Component
public class LogInitTask implements InitializationTask {

    @Override
    public void execute() {
        System.out.println("开始初始化日志系统...");
        try {
            // 模拟日志系统初始化过程
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("日志系统初始化完成。");
    }

    @Override
    public String getName() {
        return "LogInit";
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"ConfigLoad"}; // 日志初始化依赖于配置加载
    }

    @Override
    public boolean isBlockingStartup() {
        return true; // 日志初始化应该阻塞服务启动
    }
}