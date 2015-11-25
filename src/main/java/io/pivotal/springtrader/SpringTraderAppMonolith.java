package io.pivotal.springtrader;

import com.gemstone.gemfire.cache.Cache;
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
    LocalRegionFactoryBean<Integer, Integer> localRegionFactoryBean(final Cache cache) {
        return new LocalRegionFactoryBean<Integer, Integer>() {{
            setCache(cache);
            setName("quotes");
            setEnableGateway(false);
            setRegionName("quotes");
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
