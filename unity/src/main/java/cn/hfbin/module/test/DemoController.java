package cn.hfbin.module.test;

import cn.hfbin.auth.context.UserContext;
import cn.hfbin.common.token.model.JwtUserInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo")
public class DemoController {
    @RequestMapping("getUser")
    public JwtUserInfo getUser() {
        return UserContext.getUser();
    }
}
