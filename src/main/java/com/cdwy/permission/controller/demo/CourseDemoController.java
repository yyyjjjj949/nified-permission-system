package com.cdwy.permission.controller.demo;

import com.cdwy.permission.config.BusinessAccess;
import java.util.*;
import org.springframework.web.bind.annotation.*;

/** 课程资源鉴权示例；不是完整课程管理系统或已完成的外部系统对接。 */
@RestController
@RequestMapping("/api/demo/courses")
@BusinessAccess("course")
public class CourseDemoController {
  @GetMapping
  public List<Map<String, Object>> list() {
    return List.of(Map.of("id", 1, "name", "面向对象分析与设计", "example", true));
  }
}
