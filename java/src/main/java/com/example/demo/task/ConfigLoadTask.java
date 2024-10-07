package com.example.demo.task;

import org.springframework.stereotype.Component;

@Component
public class ConfigLoadTask implements InitializationTask {

    @Override
    public void execute() {
        System.out.println("开始加载配置...");
        try {
            // 模拟配置加载过程
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("配置加载完成。");
    }

    @Override
    public String getName() {
        return "ConfigLoad";
    }

    @Override
    public String[] getDependencies() {
        return new String[0]; // 配置加载通常是最先执行的任务之一，没有依赖
    }

    @Override
    public boolean isBlockingStartup() {
        return true; // 配置加载应该阻塞服务启动
    }
}