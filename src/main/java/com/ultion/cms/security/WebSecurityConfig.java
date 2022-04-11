package com.ultion.cms.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login","register").permitAll()
//                .antMatchers("/index").authenticated()
//                .antMatchers("/user").hasRole("ADMIN")
                .and()
                .formLogin()
                .loginPage("/login")
                .successForwardUrl("/index")
                .failureForwardUrl("/login")
                .permitAll()
                .and()
                .logout();

    }

    @Bean
    public BCryptPasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring()

                .antMatchers("/resources/**")
                .antMatchers("/resources/templates/**")
                .antMatchers("/css/**")
                .antMatchers("/vendor/**")
                .antMatchers("/favicon*/**")
                .antMatchers("/img/**")
                .antMatchers("/static/**")
        ;
    }

}