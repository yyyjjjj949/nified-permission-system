# 验证记录

- 后端：`mvn verify`，21个集成验收测试，失败0、错误0、跳过0。
- 前端：vue-tsc和Vite生产构建通过，静态资源位于`src/main/resources/static/ui`。
- 浏览器：独立无头Edge完成15项界面检查，无JavaScript异常；[记录与截图](../design/ui-verification.json)。
- 图：7张图同时生成PNG/SVG，自动布局检查与实际PNG目视检查通过。
- 数据库：10张持久化表，12外键；MySQL脚本的隔离兼容结构测试通过，未连接真实MySQL或服务器。
- OpenAPI：47个操作与Spring MVC实际路由逐项集合对比，内部引用完整。

| JaCoCo类型 | 已覆盖 | 未覆盖 | 比例 |
|---|---:|---:|---:|
| line | 729 | 122 | 85.66% |
| branch | 179 | 79 | 69.38% |
| instruction | 3123 | 637 | 83.06% |
| method | 291 | 64 | 81.97% |
| class | 69 | 4 | 94.52% |

当前报告为本分支重新测得，不沿用归档版本的测试数量或覆盖率。统计包括注解处理器生成的CloneFactory类；不代表100%路径、并发或真实MySQL已验证。

构建生成的完整JaCoCo报告：`target/site/jacoco/index.html`；可复核指标见[verification.json](verification.json)。

重现顺序：`scripts/build-first-review.ps1` → `python scripts/build-review-diagrams.py` → `python scripts/build-review-docs.py`。浏览器检查在临时内存库进行，截图不是预置生产数据。
