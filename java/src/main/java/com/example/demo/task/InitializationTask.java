package com.example.demo.task;

/**
 * InitializationTask 接口定义了所有初始化任务应该实现的方法
 * 它扩展了 Task 接口，添加了获取依赖任务的方法
 */
public interface InitializationTask extends Task {
    /**
     * 获取此任务依赖的其他任务名称
     * @return 依赖任务名称的数组
     */
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * 指示此任务是否应该阻塞服务启动
     * @return 如果任务应该阻塞服务启动则返回true，否则返回false
     */
    default boolean isBlockingStartup() {
        return false;
    }
}