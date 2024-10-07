package com.example.demo.task;

/**
 * Task 接口定义了所有任务应该实现的方法
 */
public interface Task {
    /**
     * 执行任务的具体逻辑
     */
    void execute();

    /**
     * 获取任务的名称
     * @return 任务名称
     */
    String getName();
}