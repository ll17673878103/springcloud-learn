package com.learn.orderservice.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则配置
 *
 * 生产环境中，规则通常通过 Sentinel Dashboard 或 Nacos 动态推送，不需要写在这里。
 * 这里用代码配置是为了学习和本地测试方便。
 *
 * 配置了两种规则：
 * 1. 流控规则（FlowRule）：限制 QPS，防止流量打垮服务
 * 2. 熔断降级规则（DegradeRule）：错误率/慢调用达到阈值时熔断
 *
 * @author MangoPie
 */
@Slf4j
@Configuration
public class SentinelRuleConfig {

    /**
     * 初始化流控规则
     * 限制对 user-service 的调用频率
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 限制 user-service 的 QPS 不超过 5
        // 作为第二道防线：Gateway 放行 10/s，这里限制对 user-service 的调用为 5/s
        // 超出限制的请求会触发 Feign 降级（UserClientFallbackFactory）
        FlowRule userRule = new FlowRule();
        userRule.setResource("GET:http://user-service/user/{id}");
        userRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userRule.setCount(5);  // 每秒最多 5 次请求
        userRule.setLimitApp("default");
        rules.add(userRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel 流控规则加载完成，共 {} 条", rules.size());
    }

    /**
     * 初始化熔断降级规则
     * 当 user-service 调用异常率达到阈值时，自动熔断
     */
    @PostConstruct
    public void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 慢调用比例熔断规则
        // 50% 的调用超过 500ms → 熔断 10 秒
        // minRequestAmount=5，在 Gateway 放行 10/s 的场景下，1 秒内即可积累足够样本
        DegradeRule slowRule = new DegradeRule("GET:http://user-service/user/{id}");
        slowRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);   // 慢调用比例
        slowRule.setCount(500);                             // 慢调用阈值：500ms（正常调用不应超过此值）
        slowRule.setSlowRatioThreshold(0.5);                // 慢调用比例阈值：50%
        slowRule.setTimeWindow(10);                         // 熔断持续时长：10秒
        slowRule.setMinRequestAmount(5);                    // 最小请求数：5
        slowRule.setStatIntervalMs(5000);                   // 统计时长：5秒
        rules.add(slowRule);

        // 异常比例熔断规则
        // 50% 的调用抛出异常 → 熔断 10 秒
        // 与慢调用熔断共享统计窗口，任一条件满足即触发
        DegradeRule errorRule = new DegradeRule("GET:http://user-service/user/{id}");
        errorRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);  // 异常比例
        errorRule.setCount(0.5);                                          // 异常比例阈值：50%
        errorRule.setTimeWindow(10);                                      // 熔断持续时长：10秒
        errorRule.setMinRequestAmount(5);                                 // 最小请求数：5
        errorRule.setStatIntervalMs(5000);                                // 统计时长：5秒
        rules.add(errorRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Sentinel 熔断降级规则加载完成，共 {} 条", rules.size());
    }
}
