package com.example.demo.task;

import org.springframework.stereotype.Component;

/**
 * CacheWarmupTask 类负责执行缓存预热任务
 * 实现了 Task 接口
 */
@Component
public class CacheWarmupTask implements InitializationTask {
    /**
     * 执行缓存预热逻辑
     */
    @Override
    public void execute() {
        System.out.println("start warming up cache...");    
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end warming up cache...");
    }

    /**
     * 返回任务名称
     * @return 任务名称
     */
    @Override
    public String getName() {
        return "CacheWarmup";
    }

    /**
     * 获取此任务依赖的其他任务名称
     * @return 依赖任务名称的数组
     */
    @Override
    public String[] getDependencies() {
        return new String[]{"DatabaseInit"}; // 缓存预热依赖于数据库初始化
    }
}