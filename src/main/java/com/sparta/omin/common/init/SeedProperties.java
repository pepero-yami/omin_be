package com.sparta.omin.common.init;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    //true일 때만 더미데이터를 주입함. 운영(prod)에서는 반드시 false로 수정하기!
    private boolean enabled = false;

}