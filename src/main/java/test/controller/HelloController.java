package test.controller;

import test.entity.User;
import web.ioc.annotation.Type.RestController;
import web.mvc.annotation.method.GetMapping;
import web.mvc.annotation.method.PostMapping;
import web.mvc.annotation.parameter.PathVariable;
import web.mvc.annotation.parameter.RequestBody;
import web.mvc.annotation.parameter.RequestParam;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello web wheel!";
    }

    //访问:/hello2?name=xx&age=18
    @GetMapping("/hello2")
    public String hello2(@RequestParam("name") String name,
                         @RequestParam("age") Integer age) {
        return "hello " + name + ", age=" + age;
    }

    //访问:/user/123
    @GetMapping("/user/{id}")
    public String user(@PathVariable("id") Integer id) {
        return "user id=" + id;
    }

    //POST JSON body: /user 请求体 {"name":"zhangsan","age":18}
    @PostMapping("/user")
    public User saveUser(@RequestBody User user) {
        return user;
    }
}