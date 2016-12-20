package mail.not.tp.conf;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created by viacheslav on 12.10.16.
 */

@Configuration
@PropertySource("classpath:db.prop.properties")
public class Config {

    @Resource
    private Environment env;

    @Bean
    public DataSource setDataSource() {
        final BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(env.getProperty("db.driver"));
        basicDataSource.setUrl(env.getProperty("db.url"));
        basicDataSource.setUsername(env.getProperty("db.username"));
        basicDataSource.setPassword(env.getProperty("db.password"));
        //basicDataSource.setMaxTotal(Integer.parseInt(env.getProperty("db.max-active")));
        basicDataSource.setInitialSize(Integer.parseInt(env.getProperty("db.initial-size")));
        return basicDataSource;
    }
}
