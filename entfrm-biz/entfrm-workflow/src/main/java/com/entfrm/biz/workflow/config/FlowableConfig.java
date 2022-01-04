package com.entfrm.biz.workflow.config;

import com.entfrm.biz.workflow.parser.factory.WorkflowActivityBehaviorFactory;
import lombok.AllArgsConstructor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 *<p>
 * Flowable-6.4配置
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2022/1/4
 */
@AllArgsConstructor
@EnableConfigurationProperties(FlowableConfig.class)
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {

        // 流程图字体设置
        engineConfiguration.setActivityFontName("华文细黑");
        engineConfiguration.setAnnotationFontName("华文细黑");
        engineConfiguration.setLabelFontName("华文细黑");

        // 激活定时任务Job
        engineConfiguration.setAsyncExecutorActivate(true);
        engineConfiguration.setDatabaseSchemaUpdate("true");

        // 邮箱配置
        engineConfiguration.setMailServerHost("smtp.qq.com");
        engineConfiguration.setMailServerPort(465);
        engineConfiguration.setMailServerDefaultFrom("1827945911@qq.com");
        engineConfiguration.setMailServerUsername("1827945911@qq.com");
        engineConfiguration.setMailServerPassword("skvhlkhrartadjgc");
        engineConfiguration.setMailServerUseSSL(true);

        // 配置工作流活动行为工厂
        engineConfiguration.setActivityBehaviorFactory(new WorkflowActivityBehaviorFactory());

    }

}
