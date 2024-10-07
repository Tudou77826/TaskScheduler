package com.example.demo.task;

import org.springframework.stereotype.Component;

/**
 * DatabaseInitTask 类负责执行数据库初始化任务
 * 实现了 Task 接口
 */
@Component
public class DatabaseInitTask implements InitializationTask {
    /**
     * 执行数据库初始化逻辑
     */
    @Override
    public void execute() {
        System.out.println("start Initializing database...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }   
        System.out.println("end Initializing database...");
    }

    /**
     * 返回任务名称
     * @return 任务名称
     */
    @Override
    public String getName() {
        return "DatabaseInit";
    }

    /**
     * 获取此任务依赖的其他任务名称
     * @return 依赖任务名称的数组
     */
    @Override
    public String[] getDependencies() {
        return new String[0]; // 数据库初始化没有依赖
    }
}