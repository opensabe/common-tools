package io.github.opensabe.spring.cloud.parent.common.loadbalancer;

import lombok.ToString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AzBalanceUtilsTest {

    /**
     * 每个源实例生成的请求数量
     * 用于计算总请求数和负载均衡比例
     */
    private static final int EACH_INSTANCE_REQUEST_COUNT = 1000000;

    /**
     * 参数化测试：运行多个测试场景
     * 每个场景测试不同的可用区和实例数量组合
     * 对比可用区感知负载均衡与纯轮询算法的效果
     * 
     * @param scenario 测试场景，包含源和目标可用区配置
     */
    @ParameterizedTest
    @MethodSource("testDataProvider")
    @DisplayName("Comprehensive test scenarios")
    void testLoadBalancingRatio(TestScenario scenario) {
        Map<String, List<Integer>> sourceMap = createSourceMap(scenario.sourceAzConfig);
        Map<String, List<Integer>> targetMap = createTargetMap(scenario.targetAzConfig);
        
        printScenarioDiagram(scenario, sourceMap, targetMap);
        
        Map<String, Map<String, Integer>> result = AzBalanceUtils.getLoadBalancingRatio(
                toRawMap(sourceMap), toRawMap(targetMap));
        
        printResultWithDiagram(sourceMap, targetMap, result);
        compareWithRoundRobin(sourceMap, targetMap, result);
        printTargetInstanceAllocation(targetMap, result);
    }


    /**
     * 将泛型Map转换为原始类型Map，用于兼容AzBalanceUtils的方法签名
     * @param typedMap 泛型Map
     * @return 原始类型Map
     */
    @SuppressWarnings("rawtypes")
    private Map<String, List> toRawMap(Map<String, List<Integer>> typedMap) {
        Map<String, List> rawMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : typedMap.entrySet()) {
            rawMap.put(entry.getKey(), entry.getValue());
        }
        return rawMap;
    }

    /**
     * 测试数据提供器：生成各种测试场景
     * 使用不可整除的数字，更贴近实际生产环境
     */
    static List<TestScenario> testDataProvider() {
        List<TestScenario> scenarios = new ArrayList<>();
        
        /*
         * 场景1: 单可用区，单实例
         * 源: az1(1实例) = 100请求
         * 目标: az1(1实例)
         * 预期: 全部同可用区调用
         */
        scenarios.add(new TestScenario("单可用区单实例",
                Map.of("az1", 1),
                Map.of("az1", 1)));
        
        /*
         * 场景2: 单可用区，多实例（不可整除）
         * 源: az1(7实例) = 700请求
         * 目标: az1(3实例)
         * 预期: 全部同可用区调用，每个目标实例约233请求
         */
        scenarios.add(new TestScenario("单可用区多实例不可整除",
                Map.of("az1", 7),
                Map.of("az1", 3)));
        
        /*
         * 场景3: 多可用区，平衡实例（不可整除）
         * 源: az1(7实例), az2(7实例), az3(7实例) = 2100请求
         * 目标: az1(5实例), az2(5实例), az3(5实例) = 15实例
         * 预期: 优先同可用区调用，每个目标实例约140请求
         */
        scenarios.add(new TestScenario("多可用区平衡实例不可整除",
                Map.of("az1", 7, "az2", 7, "az3", 7),
                Map.of("az1", 5, "az2", 5, "az3", 5)));
        
        /*
         * 场景4: 多可用区，不平衡实例（不可整除）
         * 源: az1(13实例), az2(7实例) = 2000请求
         * 目标: az1(5实例), az2(11实例), az3(3实例) = 19实例
         * 预期: az1优先同可用区，az2优先同可用区，剩余跨可用区分配
         */
        scenarios.add(new TestScenario("多可用区不平衡实例不可整除",
                Map.of("az1", 13, "az2", 7),
                Map.of("az1", 5, "az2", 11, "az3", 3)));
        
        /*
         * 场景5: 源可用区是目标可用区的子集（不可整除）
         * 源: az1(11实例), az2(7实例) = 1800请求
         * 目标: az1(3实例), az2(5实例), az3(7实例), az4(2实例) = 17实例
         * 预期: az1和az2优先同可用区，剩余请求分配到az3和az4
         */
        scenarios.add(new TestScenario("源可用区是目标子集不可整除",
                Map.of("az1", 11, "az2", 7),
                Map.of("az1", 3, "az2", 5, "az3", 7, "az4", 2)));
        
        /*
         * 场景6: 目标可用区是源可用区的子集（不可整除）
         * 源: az1(13实例), az2(7实例), az3(11实例) = 3100请求
         * 目标: az1(5实例), az2(3实例) = 8实例
         * 预期: az1和az2优先同可用区，az3全部跨可用区调用
         */
        scenarios.add(new TestScenario("目标可用区是源子集不可整除",
                Map.of("az1", 13, "az2", 7, "az3", 11),
                Map.of("az1", 5, "az2", 3)));
        
        /*
         * 场景7: 无重叠可用区（不可整除）
         * 源: az1(17实例), az2(13实例) = 3000请求
         * 目标: az3(7实例), az4(11实例) = 18实例
         * 预期: 全部跨可用区调用，按比例分配
         */
        scenarios.add(new TestScenario("无重叠可用区不可整除",
                Map.of("az1", 17, "az2", 13),
                Map.of("az3", 7, "az4", 11)));
        
        /*
         * 场景8: 大规模多可用区（不可整除）
         * 源: az1(19实例), az2(17实例), az3(23实例), az4(13实例) = 7200请求
         * 目标: az1(7实例), az2(11实例), az3(5实例), az4(13实例), az5(3实例) = 39实例
         * 预期: 优先同可用区调用，剩余跨可用区分配
         */
        scenarios.add(new TestScenario("大规模多可用区不可整除",
                Map.of("az1", 19, "az2", 17, "az3", 23, "az4", 13),
                Map.of("az1", 7, "az2", 11, "az3", 5, "az4", 13, "az5", 3)));
        
        /*
         * 场景9: 大规模多实例（不可整除）
         * 源: az1(23实例), az2(17实例) = 4000请求
         * 目标: az1(7实例), az2(19实例), az3(3实例) = 29实例
         * 预期: 优先同可用区调用，剩余跨可用区分配
         */
        scenarios.add(new TestScenario("大规模多实例不可整除",
                Map.of("az1", 23, "az2", 17),
                Map.of("az1", 7, "az2", 19, "az3", 3)));
        
        /*
         * 场景10: 一个源可用区有大量实例（不可整除）
         * 源: az1(29实例) = 2900请求
         * 目标: az1(7实例), az2(11实例), az3(13实例) = 31实例
         * 预期: az1优先同可用区，剩余请求分配到az2和az3
         */
        scenarios.add(new TestScenario("单源可用区大量实例不可整除",
                Map.of("az1", 29),
                Map.of("az1", 7, "az2", 11, "az3", 13)));
        
        /*
         * 场景11: 一个目标可用区有大量实例（不可整除）
         * 源: az1(7实例), az2(11实例) = 1800请求
         * 目标: az1(23实例) = 23实例
         * 预期: az1优先同可用区，az2全部跨可用区调用
         */
        scenarios.add(new TestScenario("单目标可用区大量实例不可整除",
                Map.of("az1", 7, "az2", 11),
                Map.of("az1", 23)));
        
        /*
         * 场景12: 极端不平衡（不可整除）
         * 源: az1(31实例), az2(2实例) = 3300请求
         * 目标: az1(3实例), az2(19实例), az3(7实例) = 29实例
         * 预期: 测试极端不平衡情况下的负载均衡
         */
        scenarios.add(new TestScenario("极端不平衡不可整除",
                Map.of("az1", 31, "az2", 2),
                Map.of("az1", 3, "az2", 19, "az3", 7)));
        
        /*
         * 场景13: 质数组合（完全不可整除）
         * 源: az1(17实例), az2(19实例), az3(23实例) = 5900请求
         * 目标: az1(7实例), az2(11实例), az3(13实例) = 31实例
         * 预期: 使用质数确保完全不可整除，测试算法鲁棒性
         */
        scenarios.add(new TestScenario("质数组合完全不可整除",
                Map.of("az1", 17, "az2", 19, "az3", 23),
                Map.of("az1", 7, "az2", 11, "az3", 13)));
        
        /*
         * 场景14: 源实例数远大于目标实例数（不可整除）
         * 源: az1(37实例), az2(41实例) = 7800请求
         * 目标: az1(3实例), az2(5实例) = 8实例
         * 预期: 测试高负载情况下的负载均衡
         */
        scenarios.add(new TestScenario("源实例远大于目标不可整除",
                Map.of("az1", 37, "az2", 41),
                Map.of("az1", 3, "az2", 5)));
        
        /*
         * 场景15: 目标实例数远大于源实例数（不可整除）
         * 源: az1(3实例), az2(5实例) = 800请求
         * 目标: az1(17实例), az2(19实例), az3(23实例) = 59实例
         * 预期: 测试低负载高容量情况下的负载均衡
         */
        scenarios.add(new TestScenario("目标实例远大于源不可整除",
                Map.of("az1", 3, "az2", 5),
                Map.of("az1", 17, "az2", 19, "az3", 23)));
        
        return scenarios;
    }

    /**
     * 测试场景数据类
     * 包含场景名称、源可用区配置和目标可用区配置
     */
    @ToString
    static class TestScenario {
        String name;
        Map<String, Integer> sourceAzConfig;
        Map<String, Integer> targetAzConfig;

        TestScenario(String name, Map<String, Integer> sourceAzConfig, Map<String, Integer> targetAzConfig) {
            this.name = name;
            this.sourceAzConfig = sourceAzConfig;
            this.targetAzConfig = targetAzConfig;
        }
    }

    /**
     * 创建源服务实例Map
     * 使用工厂方法生成测试数据，实例ID从1开始递增
     * 
     * @param azConfig 可用区配置，key为可用区名称，value为实例数量
     * @return 源服务实例Map，key为可用区名称，value为实例ID列表
     */
    private Map<String, List<Integer>> createSourceMap(Map<String, Integer> azConfig) {
        Map<String, List<Integer>> sourceMap = new LinkedHashMap<>();
        AtomicInteger instanceId = new AtomicInteger(1);
        for (Map.Entry<String, Integer> entry : azConfig.entrySet()) {
            String azName = entry.getKey();
            int instanceCount = entry.getValue();
            List<Integer> instances = IntStream.range(0, instanceCount)
                    .mapToObj(i -> instanceId.getAndIncrement())
                    .collect(Collectors.toList());
            sourceMap.put(azName, instances);
        }
        return sourceMap;
    }

    /**
     * 创建目标服务实例Map
     * 使用工厂方法生成测试数据，实例ID从100开始递增，与源实例区分
     * 
     * @param azConfig 可用区配置，key为可用区名称，value为实例数量
     * @return 目标服务实例Map，key为可用区名称，value为实例ID列表
     */
    private Map<String, List<Integer>> createTargetMap(Map<String, Integer> azConfig) {
        Map<String, List<Integer>> targetMap = new LinkedHashMap<>();
        AtomicInteger instanceId = new AtomicInteger(100);
        for (Map.Entry<String, Integer> entry : azConfig.entrySet()) {
            String azName = entry.getKey();
            int instanceCount = entry.getValue();
            List<Integer> instances = IntStream.range(0, instanceCount)
                    .mapToObj(i -> instanceId.getAndIncrement())
                    .collect(Collectors.toList());
            targetMap.put(azName, instances);
        }
        return targetMap;
    }

    /**
     * 打印场景拓扑图
     * 使用ASCII图展示源和目标可用区的实例分布
     */
    private void printScenarioDiagram(TestScenario scenario, 
                                      Map<String, List<Integer>> sourceMap, 
                                      Map<String, List<Integer>> targetMap) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试场景: " + scenario.name);
        System.out.println("=".repeat(80));
        
        System.out.println("\n【拓扑结构图】");
        System.out.println("源服务实例分布:");
        for (Map.Entry<String, List<Integer>> entry : sourceMap.entrySet()) {
            String az = entry.getKey();
            int count = entry.getValue().size();
            int requests = count * EACH_INSTANCE_REQUEST_COUNT;
            System.out.println(String.format("  %s: [%s] (%d实例, %d请求)", 
                    az, "●".repeat(Math.min(count, 20)), count, requests));
        }
        
        System.out.println("\n目标服务实例分布:");
        for (Map.Entry<String, List<Integer>> entry : targetMap.entrySet()) {
            String az = entry.getKey();
            int count = entry.getValue().size();
            System.out.println(String.format("  %s: [%s] (%d实例)", 
                    az, "○".repeat(Math.min(count, 20)), count));
        }
    }

    /**
     * 打印负载均衡结果，使用ASCII图展示请求分布
     * 显示每个源可用区到目标可用区的请求流向
     */
    private void printResultWithDiagram(Map<String, List<Integer>> sourceMap,
                                       Map<String, List<Integer>> targetMap,
                                       Map<String, Map<String, Integer>> result) {
        System.out.println("\n【负载均衡结果图】");
        
        for (Map.Entry<String, Map<String, Integer>> entry : result.entrySet()) {
            String sourceAz = entry.getKey();
            Map<String, Integer> targetAzMap = entry.getValue();
            int sourceInstanceCount = sourceMap.get(sourceAz).size();
            int sourceTotalRequests = sourceInstanceCount * EACH_INSTANCE_REQUEST_COUNT;
            
            int allocatedTotal = targetAzMap.values().stream().mapToInt(Integer::intValue).sum();
            int sameAzRequests = targetAzMap.getOrDefault(sourceAz, 0);
            int crossAzRequests = allocatedTotal - sameAzRequests;
            
            System.out.println(String.format("\n源可用区 %s (%d实例, %d请求):", 
                    sourceAz, sourceInstanceCount, sourceTotalRequests));
            
            // 同可用区调用
            if (sameAzRequests > 0) {
                System.out.println(String.format("  ┌─> %s (同可用区): %d请求 [%s]", 
                        sourceAz, sameAzRequests, "█".repeat(Math.min(sameAzRequests / 10, 50))));
            }
            
            // 跨可用区调用
            for (Map.Entry<String, Integer> targetEntry : targetAzMap.entrySet()) {
                String targetAz = targetEntry.getKey();
                int requests = targetEntry.getValue();
                if (!targetAz.equals(sourceAz) && requests > 0) {
                    System.out.println(String.format("  └─> %s (跨可用区): %d请求 [%s]", 
                            targetAz, requests, "▓".repeat(Math.min(requests / 10, 50))));
                }
            }
            
            System.out.println(String.format("  统计: 同可用区=%d, 跨可用区=%d, 同可用区比例=%.2f%%", 
                    sameAzRequests, crossAzRequests,
                    sourceTotalRequests > 0 ? (double) sameAzRequests / sourceTotalRequests * 100 : 0));
        }
    }

    /**
     * 对比可用区感知负载均衡与纯轮询算法
     * 使用ASCII图展示对比结果
     */
    private void compareWithRoundRobin(Map<String, List<Integer>> sourceMap, Map<String, List<Integer>> targetMap, 
                                      Map<String, Map<String, Integer>> azBalanceResult) {
        int azBalanceCrossAzCalls = calculateCrossAzCalls(azBalanceResult);
        int azBalanceSameAzCalls = calculateSameAzCalls(azBalanceResult);
        
        int roundRobinCrossAzCalls = calculateRoundRobinCrossAzCalls(sourceMap, targetMap);
        
        int totalRequests = sourceMap.values().stream()
                .mapToInt(List::size)
                .sum() * EACH_INSTANCE_REQUEST_COUNT;
        
        int roundRobinSameAzCalls = totalRequests - roundRobinCrossAzCalls;
        
        int reduction = roundRobinCrossAzCalls - azBalanceCrossAzCalls;
        double reductionPercentage = roundRobinCrossAzCalls > 0 
                ? (double) reduction / roundRobinCrossAzCalls * 100 
                : 0;
        
        System.out.println("\n【算法对比图】");
        System.out.println("┌" + "─".repeat(78) + "┐");
        System.out.println("│ 对比项                    │ 可用区感知负载均衡 │ 纯轮询算法        │");
        System.out.println("├" + "─".repeat(78) + "┤");
        System.out.println(String.format("│ 跨可用区调用数            │ %-18d │ %-16d │", 
                azBalanceCrossAzCalls, roundRobinCrossAzCalls));
        System.out.println(String.format("│ 同可用区调用数            │ %-18d │ %-16d │", 
                azBalanceSameAzCalls, roundRobinSameAzCalls));
        System.out.println(String.format("│ 总请求数                 │ %-18d │ %-16d │", 
                totalRequests, totalRequests));
        System.out.println(String.format("│ 跨可用区调用减少          │ %-18d │ %-16s │", 
                reduction, "-"));
        System.out.println(String.format("│ 跨可用区调用减少比例      │ %-18.2f%% │ %-16s │", 
                reductionPercentage, "-"));
        System.out.println("└" + "─".repeat(78) + "┘");
        
        // 可视化对比
        System.out.println("\n【跨可用区调用对比可视化】");
        int maxCalls = Math.max(azBalanceCrossAzCalls, roundRobinCrossAzCalls);
        if (maxCalls > 0) {
            int azBalanceBarLength = (int) ((double) azBalanceCrossAzCalls / maxCalls * 60);
            int roundRobinBarLength = (int) ((double) roundRobinCrossAzCalls / maxCalls * 60);
            
            System.out.println("可用区感知: " + "█".repeat(azBalanceBarLength) + 
                    String.format(" %d", azBalanceCrossAzCalls));
            System.out.println("纯轮询算法: " + "█".repeat(roundRobinBarLength) + 
                    String.format(" %d", roundRobinCrossAzCalls));
            System.out.println("减少量:     " + "▓".repeat(Math.max(0, roundRobinBarLength - azBalanceBarLength)) + 
                    String.format(" %d (%.2f%%)", reduction, reductionPercentage));
        }
        
        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    /**
     * 计算可用区感知负载均衡的跨可用区调用数
     * @param result 负载均衡结果
     * @return 跨可用区调用总数
     */
    private int calculateCrossAzCalls(Map<String, Map<String, Integer>> result) {
        int crossAzCalls = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : result.entrySet()) {
            String sourceAz = entry.getKey();
            Map<String, Integer> targetAzMap = entry.getValue();
            for (Map.Entry<String, Integer> targetEntry : targetAzMap.entrySet()) {
                String targetAz = targetEntry.getKey();
                if (!sourceAz.equals(targetAz)) {
                    crossAzCalls += targetEntry.getValue();
                }
            }
        }
        return crossAzCalls;
    }

    /**
     * 计算可用区感知负载均衡的同可用区调用数
     * @param result 负载均衡结果
     * @return 同可用区调用总数
     */
    private int calculateSameAzCalls(Map<String, Map<String, Integer>> result) {
        int sameAzCalls = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : result.entrySet()) {
            String sourceAz = entry.getKey();
            Map<String, Integer> targetAzMap = entry.getValue();
            Integer sameAzRequests = targetAzMap.get(sourceAz);
            if (sameAzRequests != null) {
                sameAzCalls += sameAzRequests;
            }
        }
        return sameAzCalls;
    }

    /**
     * 计算总分配数（用于验证是否所有请求都被分配）
     * @param result 负载均衡结果
     * @return 总分配数
     */
    private int calculateTotalAllocated(Map<String, Map<String, Integer>> result) {
        int total = 0;
        for (Map<String, Integer> targetAzMap : result.values()) {
            for (Integer requests : targetAzMap.values()) {
                total += requests;
            }
        }
        return total;
    }

    /**
     * 计算纯轮询算法的跨可用区调用数
     * 轮询算法会将请求平均分配到所有目标实例，不考虑可用区
     * 
     * @param sourceMap 源服务实例分布
     * @param targetMap 目标服务实例分布
     * @return 跨可用区调用总数
     */
    private int calculateRoundRobinCrossAzCalls(Map<String, List<Integer>> sourceMap, 
                                               Map<String, List<Integer>> targetMap) {
        int totalTargetInstances = targetMap.values().stream()
                .mapToInt(List::size)
                .sum();
        
        if (totalTargetInstances == 0) {
            return 0;
        }
        
        Map<String, Integer> targetAzInstanceCount = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : targetMap.entrySet()) {
            targetAzInstanceCount.put(entry.getKey(), entry.getValue().size());
        }
        
        int crossAzCalls = 0;
        for (Map.Entry<String, List<Integer>> sourceEntry : sourceMap.entrySet()) {
            String sourceAz = sourceEntry.getKey();
            int sourceInstanceCount = sourceEntry.getValue().size();
            int sourceTotalRequests = sourceInstanceCount * EACH_INSTANCE_REQUEST_COUNT;
            
            for (Map.Entry<String, Integer> targetEntry : targetAzInstanceCount.entrySet()) {
                String targetAz = targetEntry.getKey();
                int targetInstanceCount = targetEntry.getValue();
                
                if (!sourceAz.equals(targetAz)) {
                    int requestsToCrossAz = (int) ((double) sourceTotalRequests * targetInstanceCount / totalTargetInstances);
                    crossAzCalls += requestsToCrossAz;
                }
            }
        }
        
        return crossAzCalls;
    }

    /**
     * 打印每个目标实例实际接收到的请求数量
     * 展示每个目标可用区的总请求数和平均每个实例的请求数
     */
    private void printTargetInstanceAllocation(Map<String, List<Integer>> targetMap,
                                              Map<String, Map<String, Integer>> result) {
        System.out.println("\n【目标实例请求分配详情】");
        
        // 计算每个目标可用区接收到的总请求数
        Map<String, Integer> targetAzTotalRequests = new HashMap<>();
        for (Map<String, Integer> allocation : result.values()) {
            for (Map.Entry<String, Integer> entry : allocation.entrySet()) {
                String targetAz = entry.getKey();
                int requests = entry.getValue();
                targetAzTotalRequests.put(targetAz, targetAzTotalRequests.getOrDefault(targetAz, 0) + requests);
            }
        }
        
        int totalTargetInstances = targetMap.values().stream().mapToInt(List::size).sum();
        int totalRequests = calculateTotalAllocated(result);
        int averageRequestsPerInstance = totalTargetInstances > 0 ? totalRequests / totalTargetInstances : 0;
        
        System.out.println(String.format("总请求数: %d, 目标实例数: %d, 平均每实例: %d", 
                totalRequests, totalTargetInstances, averageRequestsPerInstance));
        
        System.out.println("\n各目标可用区请求分配:");
        for (Map.Entry<String, List<Integer>> entry : targetMap.entrySet()) {
            String az = entry.getKey();
            int instanceCount = entry.getValue().size();
            int totalRequestsForAz = targetAzTotalRequests.getOrDefault(az, 0);
            int requestsPerInstance = instanceCount > 0 ? totalRequestsForAz / instanceCount : 0;
            int remainder = instanceCount > 0 ? totalRequestsForAz % instanceCount : 0;
            
            System.out.println(String.format("  %s: %d实例, 总请求=%d, 平均每实例=%d", 
                    az, instanceCount, totalRequestsForAz, requestsPerInstance));
            
            if (remainder > 0) {
                System.out.println(String.format("    (余数: %d, 将分配给部分实例)", remainder));
            }
            
            // 显示每个实例的请求数（假设均匀分配，余数分配给前几个实例）
            if (instanceCount > 0 && instanceCount <= 10) {
                // 如果实例数不多，显示每个实例的请求数
                System.out.print("    实例请求分布: ");
                for (int i = 0; i < instanceCount; i++) {
                    int instanceRequests = requestsPerInstance + (i < remainder ? 1 : 0);
                    System.out.print(String.format("实例%d=%d ", i + 1, instanceRequests));
                }
                System.out.println();
            } else if (instanceCount > 10) {
                // 如果实例数很多，只显示统计信息
                int minRequests = requestsPerInstance;
                int maxRequests = requestsPerInstance + (remainder > 0 ? 1 : 0);
                System.out.println(String.format("    实例请求范围: %d - %d", minRequests, maxRequests));
            }
        }
        
        System.out.println("\n" + "=".repeat(80) + "\n");
    }
}