package ek.zhou.ekspider.entity;

import lombok.Data;

@Data
public class RedisEntity {
    private String name;
    private Integer port = 6379;
    private String hostname = "127.0.0.1";
    private Integer database = 0;
    private String password = "";
    private Integer timeout = 10000;
    private Integer maxIdle = 100;
    private Integer maxTotal = 10;
    private Integer maxWaitMillis = 100000;
    private Integer minEvictableIdleTimeMillis = 300000;
    private Integer numTestsPerEvictionRun = 1024;
    private Integer timeBetweenEvictionRunsMillis = 30000;
    private Boolean testOnBorrow = true;
    private Boolean testWhileIdle = true;
    private Long expireTime = 7*24*60*60L;
}
