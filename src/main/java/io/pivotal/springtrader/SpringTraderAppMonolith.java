package io.pivotal.springtrader;

import com.gemstone.gemfire.cache.Cache;
import io.pivotal.springtrader.domain.CompanyInfo;
import io.pivotal.springtrader.domain.Quote;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.GemfireCacheManager;

@SpringBootApplication
@EnableCaching
@EnableGemfireRepositories
public class SpringTraderAppMonolith {

    @Bean
    CacheFactoryBean cacheFactoryBean() {
        return new CacheFactoryBean();
    }

    @Bean
    LocalRegionFactoryBean<String, Quote> quotesRegionFactoryBean(final Cache cache) {
        return new LocalRegionFactoryBean<String, Quote>() {{
            setName("quotes");
            setCache(cache);
            setEnableGateway(false);
            setRegionName("quotes");
        }};
    }

    @Bean
    LocalRegionFactoryBean<String, CompanyInfo> companiesRegionFactoryBean(final Cache cache) {
        return new LocalRegionFactoryBean<String, CompanyInfo>() {{
            setName("companies");
            setEnableGateway(false);
            setRegionName("companies");
            setCache(cache);
        }};
    }

    @Bean
    GemfireCacheManager cacheManager(final Cache gemfireCache) {
        return new GemfireCacheManager() {{
            setCache(gemfireCache);
        }};
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringTraderAppMonolith.class, args);
    }
}
