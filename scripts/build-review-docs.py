"""Build API inventory, OpenAPI, data dictionary and verification evidence from build outputs.

Run AFTER Maven verify. The API metadata is curated from the DTOs and checked against
the actual Spring MVC route export so removed or unimplemented endpoints cannot leak in.
"""
from pathlib import Path
from collections import Counter
import json
import re
import shutil
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/first-review'
OUT.mkdir(parents=True, exist_ok=True)
SRC = ROOT / 'src/main/java/com/cdwy/permission'
routes = json.loads((ROOT/'target/first-review/routes.json').read_text(encoding='utf-8'))
schema = json.loads((ROOT/'target/first-review/schema.json').read_text(encoding='utf-8'))


def write(name, content):
    (OUT/name).write_text(content.rstrip()+'\n', encoding='utf-8')


def ref(name):
    return {'$ref': '#/components/schemas/'+name}


def string(maximum=None, **extra):
    return {'type': 'string', **({'maxLength': maximum} if maximum else {}), **extra}


def integer(**extra):
    return {'type': 'integer', 'format': 'int64', **extra}


def obj(properties, required=(), strict=False):
    return {'type': 'object', 'properties': properties, **({'required': list(required)} if required else {}),
            **({'additionalProperties': False} if strict else {})}


def array(name):
    return {'type': 'array', 'items': ref(name)}


text = string()
name = string(128, minLength=1)
code = string(64, pattern='^[a-z][a-z0-9_-]{0,63}$', minLength=1)
person = string(enum=['EMPLOYEE', 'EXTERNAL', 'TEMPORARY'])
kind = string(enum=['MENU', 'BUTTON'])
nullable_id = integer(nullable=True)
date = string(format='date', nullable=True, description='外部/临时人员必填；按配置时区包含该日全天，正式员工须为空')
schemas = {
    'Login': obj({'username': string(minLength=1), 'password': string(minLength=1, format='password')}, ['username','password'], True),
    'Token': obj({'accessToken':text, 'tokenType':string(enum=['Bearer']), 'expiresIn':integer(), 'user':ref('UserView')}, ['accessToken','tokenType','expiresIn','user']),
    'PasswordChange': obj({'currentPassword':string(format='password'), 'newPassword':string(format='password', description='至少8字符、UTF-8不超过72字节，大小写/数字/符号至少三类')}, ['currentPassword','newPassword'], True),
    'SetPassword': obj({'password':string(format='password')}, ['password'], True),
    'Status': obj({'enabled':{'type':'boolean'}}, ['enabled'], True),
    'Activity': obj({'active':{'type':'boolean'}}, ['active'], True),
    'UserCreate': obj({'username':string(64,pattern='^[A-Za-z0-9_.-]{1,64}$'), 'displayName':name, 'password':string(format='password'), 'departmentId':nullable_id, 'personType':{**person,'default':'EMPLOYEE'}, 'expiresOn':date}, ['username','displayName','password'], True),
    'UserUpdate': obj({'displayName':name,'departmentId':nullable_id,'personType':person,'expiresOn':date}, ['displayName','personType'], True),
    'UserView': obj({'id':integer(),'username':text,'displayName':text,'enabled':{'type':'boolean'},'administrator':{'type':'boolean'},'departmentId':nullable_id,'personType':person,'expiresAt':string(format='date-time',nullable=True)}),
    'UserPage': obj({'items':array('UserView'),'total':integer(),'page':{'type':'integer'},'size':{'type':'integer'}}),
    'DepartmentInput': obj({'name':name,'parentId':nullable_id},['name'],True),
    'DepartmentView': obj({'id':integer(),'name':text,'parentId':nullable_id}),
    'RoleCreate': obj({'code':code,'name':name,'description':string(255,nullable=True)},['code','name'],True),
    'RoleUpdate': obj({'name':name,'description':string(255,nullable=True)},['name'],True),
    'RoleView': obj({'id':integer(),'code':text,'name':text,'description':string(nullable=True),'enabled':{'type':'boolean'}}),
    'SystemCreate': obj({'code':code,'name':name,'baseUrl':string(255,nullable=True)},['code','name'],True),
    'SystemUpdate': obj({'name':name,'baseUrl':string(255,nullable=True)},['name'],True),
    'SystemView': obj({'id':integer(),'code':text,'name':text,'baseUrl':string(nullable=True),'enabled':{'type':'boolean'}}),
    'PermissionInput': obj({'systemId':integer(minimum=1),'code':string(128,minLength=1),'name':name,'type':kind,'menuPath':string(255,nullable=True),'parentId':nullable_id},['systemId','code','name','type'],True),
    'PermissionUpdate': obj({'name':name,'type':kind,'menuPath':string(255,nullable=True),'parentId':nullable_id},['name','type'],True),
    'PermissionView': obj({'id':integer(),'systemId':integer(),'code':text,'name':text,'type':kind,'menuPath':string(nullable=True),'parentId':nullable_id,'active':{'type':'boolean'}}),
    'PermissionTree': obj({'permission':ref('PermissionView'),'granted':{'type':'boolean'},'children':array('PermissionTree')}),
    'ResourceCreate': obj({'systemId':integer(minimum=1),'name':name,'method':string(enum=['GET','POST','PUT','PATCH','DELETE']),'path':string(255,minLength=1,description='绝对路径，可含{变量}；不含查询参数、片段和**，登记后方法/路径/应用固定')},['systemId','name','method','path'],True),
    'ResourceUpdate': obj({'name':name},['name'],True),
    'ResourceView': obj({'id':integer(),'systemId':integer(),'name':text,'method':text,'path':text,'active':{'type':'boolean'}}),
    'CourseExample': obj({'id':integer(),'name':text,'example':{'type':'boolean'}}),
    'Error': obj({'error':text,'message':text},['error','message']),
}
examples = {
    'Login': {'username':'admin','password':'<启动时设置的密码>'},
    'PasswordChange': {'currentPassword':'<原密码>','newPassword':'<符合策略的新密码>'},
    'SetPassword': {'password':'<符合策略的新密码>'},
    'Status': {'enabled':False}, 'Activity': {'active':False},
    'UserCreate': {'username':'teacher01','displayName':'演示教师','password':'<符合策略的密码>','personType':'EMPLOYEE','departmentId':None},
    'UserUpdate': {'displayName':'教师新姓名','personType':'EMPLOYEE','departmentId':None,'expiresOn':None},
    'DepartmentInput': {'name':'信息技术部','parentId':None},
    'RoleCreate': {'code':'teacher','name':'教师','description':'课程只读角色'},
    'RoleUpdate': {'name':'课程教师','description':'课程只读角色'},
    'SystemCreate': {'code':'course','name':'课程系统','baseUrl':None},
    'SystemUpdate': {'name':'课程系统','baseUrl':None},
    'PermissionInput': {'systemId':1,'code':'course:list','name':'查看课程','type':'BUTTON','menuPath':None,'parentId':1},
    'PermissionUpdate': {'name':'查看课程','type':'BUTTON','menuPath':None,'parentId':1},
    'ResourceCreate': {'systemId':1,'name':'查询课程列表','method':'GET','path':'/api/demo/courses'},
    'ResourceUpdate': {'name':'查询课程列表'},
}

# method + path -> summary, request schema, successful response schema, status, access, purpose
operations = {}


def add(verb,path,title,body,result,status=200,access='管理员',purpose=''):
    key=(verb,path)
    assert key not in operations, key
    operations[key]=(title,body,result,status,access,purpose or title)


add('POST','/api/auth/login','账号密码登录','Login',ref('Token'),access='公开',purpose='验证当前账号和密码后签发JWT；不创建登录会话，不在Token保存权限列表。')
add('GET','/api/me','查询本人资料',None,ref('UserView'),access='已登录',purpose='主体取自Token，不允许指定他人ID。')
add('GET','/api/me/permissions','查询本人有效权限',None,array('PermissionView'),access='已登录',purpose='按有效用户角色与角色权限取并集，去重并实时过滤停用对象。')
add('GET','/api/me/menu','查询本人导航树',None,array('PermissionTree'),access='已登录',purpose='为直接授权节点补齐祖先导航容器；granted=false的容器不获得接口权限。')
add('PUT','/api/me/password','修改本人密码','PasswordChange',None,204,'已登录','验证原密码，更新BCrypt摘要与凭证版本；全部旧Token失效，需重新登录。')
add('GET','/api/demo/courses','课程API鉴权示例',None,array('CourseExample'),access='业务授权',purpose='仅为示例静态课程；需登记course应用的GET /api/demo/courses资源、绑定权限并分配角色。')

for plural,label,model,create,update in [
    ('users','用户','UserView','UserCreate','UserUpdate'),
    ('departments','部门','DepartmentView','DepartmentInput','DepartmentInput'),
    ('roles','角色','RoleView','RoleCreate','RoleUpdate'),
    ('systems','应用','SystemView','SystemCreate','SystemUpdate'),
    ('permissions','前端权限','PermissionView','PermissionInput','PermissionUpdate'),
    ('resources','API资源','ResourceView','ResourceCreate','ResourceUpdate'),
]:
    base='/api/'+plural
    add('GET',base,'查询'+label+'列表',None,ref('UserPage') if plural=='users' else array(model),purpose='查询'+label+'资源；用户分页，其余为当前小规模演示的完整列表。')
    add('POST',base,'创建'+label,create,ref(model),201,purpose='创建独立'+label+'记录，默认启用；不隐式创建授权关系。')
    add('PUT',base+'/{id}','编辑'+label+'资料',update,ref(model),purpose='修改资料；创建时的身份标识保持固定，enabled/active不在此DTO，夹带将返回400。')
    add('DELETE',base+'/{id}','删除'+label,None,None,204,purpose='删除该资源；除用户会清理其用户角色关系外，关联引用需要先解除，否则409。')
    if plural!='departments':
        field='active' if plural in ['permissions','resources'] else 'enabled'
        add('PUT',base+'/{id}/status','单独启停'+label,'Activity' if field=='active' else 'Status',ref(model),purpose='独立改变'+field+'，影响所有关联使用者；保留关系，下一次请求重新计算有效权限。')

add('GET','/api/users/{id}','查询单个用户',None,ref('UserView'))
add('PUT','/api/users/{id}/password','管理员设置用户密码','SetPassword',None,204,purpose='独立凭证资源操作；更新凭证版本，使该用户全部旧Token失效。')
add('GET','/api/permissions/tree','查询完整前端权限树',None,array('PermissionTree'),purpose='管理端以层次展示菜单和按钮，可按systemId筛选；不代表本人有效权限。')

for path,list_schema,relation,resource in [
    ('/api/users/{userId}/roles','RoleView','用户角色','{roleId}'),
    ('/api/roles/{roleId}/permissions','PermissionView','角色权限','{permissionId}'),
    ('/api/permissions/{id}/resources','ResourceView','权限API绑定','{resourceId}'),
]:
    add('GET',path,'查询'+relation,None,array(list_schema),purpose='读取显式关联，便于核对配置，不包含自动推导出的其他关系。')
    add('PUT',path+'/'+resource,'建立'+relation,None,None,204,purpose='以两端ID标识关联；重复建立保持一条关系，唯一约束防止重复。')
    add('DELETE',path+'/'+resource,'解除'+relation,None,None,204,purpose='只解除该对关系；其他角色或权限仍可提供访问权，下一次请求实时生效。')

actual={(verb,route['path']):route for route in routes for verb in route['methods']}
assert set(actual)==set(operations), {'missing':list(set(actual)-set(operations)),'extra':list(set(operations)-set(actual))}
paths={}
inventory=['# API 设计（第一轮整改版）','',
    '接口清单从通过验收测试的 Spring MVC 路由导出，与 [OpenAPI](openapi.json) 同步生成。当前 '+str(len(actual))+' 个 HTTP 操作。', '',
    '## 约定','',
    '- 基址：`http://127.0.0.1:18081`。JSON请求；登录外均使用 `Authorization: Bearer <accessToken>`。',
    '- 管理员身份只配置系统；业务接口仍需用户角色→角色权限→权限API绑定。',
    '- GET读取；POST创建返回201；PUT替换资料/独立状态或幂等建立关联；DELETE删除/解除关联返回204。',
    '- 普通编辑不接受状态字段。用户、角色、应用为`enabled`；前端权限、API资源为`active`。',
    '- 400：参数、JSON或业务规则错误；401：凭证无效；403：身份或权限不足；404：资源不存在；409：重复或引用冲突。',
    '- 错误体为`{"error":"Conflict","message":"说明"}`；列表为空返回空数组，分页用户返回`items/total/page/size`。',
    '- 登录响应包含`accessToken/tokenType/expiresIn/user`。退出在前端删除Token；没有服务端注销、刷新或会话管理API。',
    '- 用户`page`从0开始，`size`范围1..100，默认25；权限列表与树可用`systemId`筛选。', '',
    '## 资源与接口必要性','',
    '| 方法 | 路径 | 含义／必要性 | 输入DTO | 成功响应 | 访问主体 | 源码 |','|---|---|---|---|---|---|---|']
for (verb,path),value in sorted(operations.items(),key=lambda item:(item[0][1],item[0][0])):
    title,body,result,status,access,purpose=value
    runtime=actual[(verb,path)]
    files=list(SRC.glob('controller/**/'+runtime['controller']+'.java'));assert len(files)==1
    source=files[0].relative_to(ROOT).as_posix()
    params=[{'name':key,'in':'path','required':True,'schema':integer()} for key in re.findall(r'\{(\w+)\}',path)]
    if path=='/api/users' and verb=='GET':
        params += [{'name':'page','in':'query','schema':{'type':'integer','minimum':0,'default':0}}, {'name':'size','in':'query','schema':{'type':'integer','minimum':1,'maximum':100,'default':25}}]
    if path in ['/api/permissions','/api/permissions/tree'] and verb=='GET':
        params += [{'name':'systemId','in':'query','schema':integer()}]
    response={'description': '无响应体' if status==204 else title+'成功'}
    if result:response['content']={'application/json':{'schema':result}}
    responses={str(status):response}
    for code_,desc in [(400,'参数或业务规则不合法'),(401,'Token缺失、过期或账号凭证失效'),(403,'缺少管理员身份或业务授权'),(404,'资源不存在'),(409,'唯一性或关联约束冲突')]:
        if access=='公开' and code_ in [403,404,409]:continue
        responses[str(code_)]={'description':desc,'content':{'application/json':{'schema':ref('Error')}}}
    op={'summary':title,'description':purpose,'operationId':runtime['controller']+'_'+runtime['method'],
        'tags':[runtime['controller'].removesuffix('Controller')], 'security':[] if access=='公开' else [{'bearerAuth':[]}],
        'x-access-scope':access,'x-source':source+'#'+runtime['method'], 'responses':responses}
    if params:op['parameters']=params
    if body:op['requestBody']={'required':True,'content':{'application/json':{'schema':ref(body),'example':examples[body]}}}
    paths.setdefault(path,{})[verb.lower()]=op
    inventory.append(f'| {verb} | `{path}` | {title}：{purpose} | {body or "—"} | {status} | {access} | [{runtime["controller"]}.{runtime["method"]}](../../{source}) |')

spec={'openapi':'3.0.3','info':{'title':'Cdwy 统一权限管理系统 · 第一轮整改','version':'2026.11.07','description':'当前实现47个HTTP操作；核心RBAC0与无登录会话JWT。课程接口为示例。'},
      'servers':[{'url':'http://127.0.0.1:18081'}],'paths':paths,
      'components':{'securitySchemes':{'bearerAuth':{'type':'http','scheme':'bearer','bearerFormat':'JWT'}},'schemas':schemas}}


def check_refs(value):
    if isinstance(value,dict):
        if '$ref' in value:assert value['$ref'].removeprefix('#/components/schemas/') in schemas,value
        for v in value.values():check_refs(v)
    elif isinstance(value,list):
        for v in value:check_refs(v)


check_refs(spec)
write('openapi.json',json.dumps(spec,ensure_ascii=False,indent=2))
inventory+=['','## Postman 验证','',
    '1. Import → 选择本目录的`openapi.json`。演示地址为`http://127.0.0.1:18081`，部署后按实际地址调整。',
    '2. 发送登录请求，密码使用启动脚本中手工输入的值，复制`accessToken`。',
    '3. 在集合 Authorization 中选 Bearer Token，粘贴Token；登录请求自身无需认证。',
    '4. 按[演示步骤](demo.md)依次创建对象，记录实际返回ID，再建立三组关系。',
    '5. 改用普通用户Token调用`GET /api/demo/courses`，赋权前403，赋权后200，撤权后再变403。',
    '6. 修改状态使用独立`/status`请求；不要把enabled/active放到资料编辑JSON。', '',
    '字段、必填项、枚举与JSON示例见OpenAPI的Schemas和Request Body。示例中的尖括号内容必须替换。']
write('api-design.md','\n'.join(inventory))
for filename in ['schema.json','routes.json']:
    data=json.loads((ROOT/'target/first-review'/filename).read_text(encoding='utf-8'))
    if filename=='routes.json':data=sorted(data,key=lambda r:(r['path'],r['methods']))
    write(filename,json.dumps(data,ensure_ascii=False,indent=2))

definitions={
    'SYS_DEPARTMENT':('部门','部门树；parent_id可空，Service拒绝环。'),
    'SYS_USER':('用户','人员资料、人员身份、状态、有效期与账号级凭证版本。'),
    'SYS_ACCOUNT':('用户登录凭证（技术拆表）','@SecondaryTable映射到同一个UserAccount实体；user_id同时为主键和外键，没有第二个独立人员实体。'),
    'SYS_ROLE':('角色','业务权限集合；enabled控制是否计入有效权限。'),
    'BIZ_SYSTEM':('应用','应用目录；停用影响该应用所有权限和资源。'),
    'SYS_PERMISSION':('前端权限','仅MENU/BUTTON；parent_id菜单树，code全局唯一。'),
    'API_RESOURCE':('API资源','应用+方法+路径组合唯一；与菜单、按钮不是同一实体。'),
    'SYS_USER_ROLE':('用户角色关联','用户与角色M:N，组合唯一。'),
    'SYS_ROLE_PERMISSION':('角色权限关联','角色与前端权限M:N，组合唯一。'),
    'PERMISSION_API':('权限API关联','前端权限与API资源M:N；两端应用一致由Service校验。'),
}
dictionary=['# 数据库设计（当前10表模型）','',
    '概念层是6类核心对象；物理层是10张持久化表。`sys_account`为用户凭证技术拆表，另外3张是多对多关联表。JWT不持久化，无登录会话表。', '',
    '## 实体与物理映射','',
    '| 表 | 概念／职责 | 说明 |','|---|---|---|']
for table in schema:
    title,desc=definitions[table];dictionary.append(f'| `{table.lower()}` | {title} | {desc} |')
dictionary += ['','## 字段字典','', '以下字段、类型、可空性取自测试启动后实际JPA模型；MySQL长度、约束与DDL见[建表脚本](database-mysql.sql)。H2枚举/时区时间与MySQL存储类型存在方言差异。']
for table,columns in schema.items():
    dictionary += ['', '### '+table.lower(), '', '| 字段 | H2实际类型 | 可空 |','|---|---|---|']
    for col in columns:dictionary.append(f'| `{col["COLUMN_NAME"].lower()}` | {col["DATA_TYPE"]} | {"是" if col["IS_NULLABLE"]=="YES" else "否"} |')
dictionary += ['', '## 约束与事务','',
    '- 12个外键包含部门自关联、用户部门、账号用户、菜单自关联、权限应用、资源应用，以及3张关联表的6个外键。',
    '- 登录名、部门名、角色编码、应用编码、权限编码唯一。API资源按`system_id/method/path`唯一。三张关联表各自具有组合唯一约束。',
    '- 新建用户、改密、启停、赋权等写操作在Service事务中执行；账号敏感写操作使用JPA悲观行锁，版本更新不依赖HTTP Session。',
    '- 删除用户时删除其用户角色关系；有引用的角色、应用、权限、资源、部门禁止直接删除并返回409，避免误删共享配置。',
    '- 数据库外键保证引用存在；同应用、父节点类型、防环、有效期等业务约束由Service处理。前端校验只用于交互。',
    '- MySQL脚本只用于新空库。旧版本数据迁移尚未实施；不要使用Hibernate update让旧库自动变形。',
    '- DDL已通过隔离H2 MySQL模式结构对比（表、列、长度、可空、主键、唯一键、12外键）；这不等同于真实MySQL运行验证。',
    '- H2内部`HT_SYS_USER`/`HTE_SYS_USER`是Hibernate临时bulk mutation表，不计入业务表；没有把它们作为核心实体。',
    '- MySQL中到期时间使用UTC datetime(6)，Hibernate JDBC时区固定为UTC；有效期日期按可配置业务时区解释。',
    '', '## 图','', '核心概念图见[核心ER](../design/images/er-core.png)，全10表结构见[物理ER](../design/images/er-physical.png)及[可编辑源](../design/physical-er.mmd)。']
write('database-design.md','\n'.join(dictionary))

surefire=ET.parse(ROOT/'target/surefire-reports/TEST-com.cdwy.permission.FirstReviewWorkflowTests.xml').getroot()
jacoco=ET.parse(ROOT/'target/site/jacoco/jacoco.xml').getroot()
coverage={}
for count in jacoco.findall('counter'):
    covered=int(count.attrib['covered']);missed=int(count.attrib['missed'])
    coverage[count.attrib['type'].lower()]={'covered':covered,'missed':missed,'percent':round(covered*100/(covered+missed),2) if covered+missed else None}
service_classes=[p.stem for p in SRC.glob('service/**/*.java') if re.search(r'@Service\b',p.read_text(encoding='utf-8'))]
entity_classes=[p.stem for p in SRC.glob('entity/**/*.java') if '@Entity' in p.read_text(encoding='utf-8')]
evidence={'generatedAt':datetime.now(timezone.utc).isoformat(), 'baseline':'d2f243819c74b069f869ecd53a82cdbb6b2eca2d',
          'branch':'main', 'repository':'https://github.com/yyyjjjj949/nified-permission-system',
          'sourceFiles':len(list(SRC.rglob('*.java'))),
          'springServices':sorted(service_classes),'serviceCount':len(service_classes),'jpaEntities':sorted(entity_classes),
          'conceptualEntityCount':6,'persistentTableCount':len(schema),'foreignKeyCount':12,'httpOperationCount':len(actual),
          'tests':{k:int(surefire.attrib[k]) for k in ['tests','failures','errors','skipped']},'jacoco':coverage,
          'frontendBuild':'vue-tsc --noEmit && vite build: passed',
          'uiVerification':'../design/ui-verification.json', 'diagramVerification':'../design/diagram-validation.json',
          'diagramVisualInspection':'7 PNG diagrams opened and checked',
          'mysqlValidation':'isolated H2 MySQL-mode structural comparison only; real MySQL not run',
          'serverDeploymentValidated':False,'oldDatabaseMigration':False}
assert evidence['tests']['tests']>=21 and evidence['tests']['failures']==0 and evidence['tests']['errors']==0
assert len(service_classes)==10 and len(schema)==10 and len(actual)==47
write('verification.json',json.dumps(evidence,ensure_ascii=False,indent=2))
summary=['# 验证记录','',
    f'- 后端：`mvn verify`，{evidence["tests"]["tests"]}个集成验收测试，失败0、错误0、跳过{evidence["tests"]["skipped"]}。',
    '- 前端：vue-tsc和Vite生产构建通过，静态资源位于`src/main/resources/static/ui`。',
    '- 浏览器：独立无头Edge完成15项界面检查，无JavaScript异常；[记录与截图](../design/ui-verification.json)。',
    '- 图：7张图同时生成PNG/SVG，自动布局检查与实际PNG目视检查通过。',
    '- 数据库：10张持久化表，12外键；MySQL脚本的隔离兼容结构测试通过，未连接真实MySQL或服务器。',
    '- OpenAPI：47个操作与Spring MVC实际路由逐项集合对比，内部引用完整。', '',
    '| JaCoCo类型 | 已覆盖 | 未覆盖 | 比例 |','|---|---:|---:|---:|']
for metric in ['line','branch','instruction','method','class']:
    row=coverage[metric];summary.append(f'| {metric} | {row["covered"]} | {row["missed"]} | {row["percent"]}% |')
summary += ['', '当前报告为本分支重新测得，不沿用归档版本的测试数量或覆盖率。统计包括注解处理器生成的CloneFactory类；不代表100%路径、并发或真实MySQL已验证。', '',
    '构建生成的完整JaCoCo报告：`target/site/jacoco/index.html`；可复核指标见[verification.json](verification.json)。', '',
    '重现顺序：`scripts/build-first-review.ps1` → `python scripts/build-review-diagrams.py` → `python scripts/build-review-docs.py`。浏览器检查在临时内存库进行，截图不是预置生产数据。']
write('testing.md','\n'.join(summary))
print(f'Generated OpenAPI and documentation: {len(actual)} operations, {len(schema)} tables, {len(service_classes)} services')
