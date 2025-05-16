package cn.hfbin.mine.controller;

import cn.hfbin.common.token.model.AuthUserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {
    public TestController() {
    }

    @GetMapping("getUser")
    public String getUser() {
        return "hello";
    }
}
