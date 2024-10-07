package com.example.demo.task;

import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * TaskScheduler类负责管理和执行初始化任务
 * 它处理任务之间的依赖关系，并支持并行执行独立的任务
 */
@Component
public class TaskScheduler {
    private final Map<String, InitializationTask> tasks;
    private final Map<String, Set<String>> dependencies;
    private final ExecutorService executorService;

    /**
     * 构造函数，初始化任务映射、依赖映射和线程池
     */
    public TaskScheduler() {
        this.tasks = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.executorService = createExecutorService();
    }

    /**
     * 创建并配置线程池
     * @return 配置好的ExecutorService
     */
    private ExecutorService createExecutorService() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1000);

        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            unit,
            workQueue,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 添加任务到调度器
     * @param task 要添加的任务
     */
    public void addTask(InitializationTask task) {
        tasks.put(task.getName(), task);
        dependencies.put(task.getName(), new HashSet<>());
    }

    /**
     * 添加任务依赖关系
     * @param taskName 任务名称
     * @param dependencyNames 依赖任务的名称数组
     */
    public void addDependencies(String taskName, String... dependencyNames) {
        Set<String> taskDependencies = dependencies.computeIfAbsent(taskName, k -> new HashSet<>());
        taskDependencies.addAll(Arrays.asList(dependencyNames));
    }

    /**
     * 执行所有任务，考虑依赖关系
     * @throws InterruptedException 如果任务执行被中断
     * @throws ExecutionException 如果任务执行过程中发生异常
     */
    public void executeTasks() throws InterruptedException, ExecutionException {
        executeTasksInternal(new ArrayList<>(tasks.values()));
    }

    /**
     * 执行所有阻塞任务
     * @throws InterruptedException 如果任务执行被中断
     * @throws ExecutionException 如果任务执行过程中发生异常
     */
    public void executeBlockingTasks() throws InterruptedException, ExecutionException {
        List<InitializationTask> blockingTasks = tasks.values().stream()
                .filter(InitializationTask::isBlockingStartup)
                .collect(Collectors.toList());
        executeTasksInternal(blockingTasks);
    }

    /**
     * 执行所有非阻塞任务
     * @throws InterruptedException 如果任务执行被中断
     * @throws ExecutionException 如果任务执行过程中发生异常
     */
    public void executeNonBlockingTasks() throws InterruptedException, ExecutionException {
        List<InitializationTask> nonBlockingTasks = tasks.values().stream()
                .filter(task -> !task.isBlockingStartup())
                .collect(Collectors.toList());
        executeTasksInternal(nonBlockingTasks);
    }

    /**
     * 内部方法，执行给定的任务列表
     * @param tasksToExecute 要执行的任务列表
     * @throws InterruptedException 如果任务执行被中断
     * @throws ExecutionException 如果任务执行过程中发生异常
     */
    private void executeTasksInternal(List<InitializationTask> tasksToExecute) throws InterruptedException, ExecutionException {
        Map<String, CompletableFuture<Void>> taskFutures = new HashMap<>();

        for (InitializationTask task : tasksToExecute) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            taskFutures.put(task.getName(), future);
            executorService.submit(() -> executeTask(task, taskFutures));
        }

        CompletableFuture.allOf(taskFutures.values().toArray(new CompletableFuture[0])).join();
    }

    /**
     * 执行单个任务，确保其所有依赖都已完成
     * @param task 要执行的任务
     * @param taskFutures 所有任务的Future映射
     */
    private void executeTask(InitializationTask task, Map<String, CompletableFuture<Void>> taskFutures) {
        try {
            for (String dependencyName : task.getDependencies()) {
                CompletableFuture<Void> dependencyFuture = taskFutures.get(dependencyName);
                if (dependencyFuture != null) {
                    dependencyFuture.join();
                }
            }

            task.execute();
            taskFutures.get(task.getName()).complete(null);
        } catch (Exception e) {
            taskFutures.get(task.getName()).completeExceptionally(e);
        }
    }

    /**
     * 在Spring容器销毁前关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}