import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const ua = path.join(root, '.ua');
const scan = JSON.parse(fs.readFileSync(path.join(ua, 'intermediate', 'scan-result.json'), 'utf8'));
const extract = JSON.parse(fs.readFileSync(path.join(ua, 'intermediate', 'extract-results.json'), 'utf8'));
const extractByPath = new Map(extract.results.map(r => [r.path, r]));
const nodes = [];
const edges = [];
const nodeIds = new Set();

const special = new Map([
  ['src/main/java/web/ioc/AnnotationConfigApplicationContext.java', 'IOC 应用上下文，负责扫描 Bean、创建工厂、执行字段注入并把容器 Bean 注册给 RPC。'],
  ['src/main/java/web/ioc/factory/DefaultBeanFactory.java', '默认 Bean 工厂，按照 BeanDefinition 创建对象、维护单例缓存，并在需要时创建 AOP 代理。'],
  ['src/main/java/web/ioc/reader/BeanDefinitionReader.java', 'Bean 定义读取器，根据类型注解、作用域和切点匹配结果生成 BeanDefinition。'],
  ['src/main/java/web/ioc/pojo/BeanDefinition.java', '描述 Bean 名称、Java 类型、作用域和是否需要 AOP 代理。'],
  ['src/main/java/web/aop/AopProxy.java', '基于 CGLIB 的方法拦截器，执行 Before、Around 和 After 通知。'],
  ['src/main/java/web/aop/ProceedingJoinPoint.java', '封装目标对象、方法、参数和 MethodProxy，供 Around 通知调用 proceed。'],
  ['src/main/java/web/aop/PointcutMatcher.java', '匹配方法名、简单类名加方法名或全限定类名加方法名形式的切点表达式。'],
  ['src/main/java/web/ApplicationServer.java', '应用启动入口，创建 IOC 上下文、注册 MVC 路由并启动 Netty HTTP 服务器。'],
  ['src/main/java/web/mvc/HandleMapping.java', '扫描容器 Bean 的映射注解并建立 URL 到处理方法的映射。'],
  ['src/main/java/web/mvc/Dispatcher.java', '接收 HTTP 请求，查找路由、解析参数并执行控制器方法。'],
]);

function addNode(n) { if (!nodeIds.has(n.id)) { nodeIds.add(n.id); nodes.push(n); } }
function addEdge(source, target, type, weight) {
  if (!nodeIds.has(source) || !nodeIds.has(target) || source === target) return;
  const key = `${source}|${target}|${type}`;
  if (!edges.some(e => `${e.source}|${e.target}|${e.type}` === key)) edges.push({source, target, type, direction:'forward', weight});
}
function fileNodeType(f) {
  if (f.fileCategory === 'docs') return 'document';
  if (f.fileCategory === 'config') return 'config';
  if (f.fileCategory === 'infra') return 'service';
  if (f.fileCategory === 'data') return 'schema';
  return 'file';
}
function fileId(f) { return `${fileNodeType(f)}:${f.path}`; }
function tags(f) {
  const p = f.path.toLowerCase();
  const t = new Set(['java', f.fileCategory]);
  if (p.includes('ioc')) t.add('ioc');
  if (p.includes('aop')) t.add('aop');
  if (p.includes('mvc')) t.add('mvc');
  if (p.includes('rpc')) t.add('rpc');
  if (p.includes('controller')) t.add('api-handler');
  if (p.includes('factory')) t.add('factory');
  if (p.includes('annotation')) t.add('annotation');
  if (p.includes('test')) t.add('test');
  return [...t].slice(0, 5);
}
function summary(f) {
  if (special.has(f.path)) return special.get(f.path);
  const p = f.path.toLowerCase();
  if (p.includes('annotation')) return '定义框架注解，用于描述组件、路由、参数、作用域或 AOP 切点。';
  if (p.includes('exception')) return '定义项目中的异常类型，用于区分请求、容器或 RPC 错误。';
  if (p.includes('server')) return '提供服务器启动、连接初始化或请求处理相关实现。';
  if (p.includes('codec')) return '实现 RPC 请求或响应的编解码逻辑。';
  if (p.includes('entity') || p.includes('model')) return '定义业务数据模型或 ORM 元数据。';
  if (p.includes('service')) return '提供业务服务或服务接口实现。';
  if (p.endsWith('pom.xml')) return 'Maven 构建配置，声明项目依赖、Java 版本和编译插件。';
  if (f.fileCategory === 'docs') return '项目说明或开发规则文档。';
  return `项目源码文件，属于 ${f.path.split('/').slice(-2,-1)[0] || '根目录'} 模块。`;
}

for (const f of scan.files) {
  const id = fileId(f);
  addNode({id, type:fileNodeType(f), name:path.basename(f.path), filePath:f.path, summary:summary(f), tags:tags(f), complexity:f.sizeLines > 200 ? 'complex' : f.sizeLines > 50 ? 'moderate' : 'simple'});
}

for (const r of extract.results) {
  const f = scan.files.find(x => x.path === r.path);
  if (!f || f.fileCategory !== 'code') continue;
  const fid = fileId(f);
  for (const c of (r.classes || [])) {
    const id = `class:${r.path}:${c.name}`;
    if ((c.methods?.length || 0) >= 2 || (c.endLine - c.startLine + 1) >= 20 || (r.exports || []).some(e => e.name === c.name)) {
      addNode({id,type:'class',name:c.name,filePath:r.path,lineRange:[c.startLine,c.endLine],summary:`${c.name} 类，负责 ${summary(f).replace('项目源码文件，','').replace('。','')}。`,tags:tags(f),complexity:(c.endLine-c.startLine)>200?'complex':'moderate'});
      addEdge(fid,id,'contains',1.0);
    }
  }
  for (const fn of (r.functions || [])) {
    const exported = (r.exports || []).some(e => e.name === fn.name);
    if ((fn.endLine - fn.startLine + 1) >= 10 || exported) {
      const id = `function:${r.path}:${fn.name}`;
      addNode({id,type:'function',name:fn.name,filePath:r.path,lineRange:[fn.startLine,fn.endLine],summary:`${fn.name} 方法，执行该文件中的核心处理逻辑。`,tags:tags(f),complexity:(fn.endLine-fn.startLine)>80?'complex':'simple'});
      addEdge(fid,id,'contains',1.0);
      if (exported) addEdge(fid,id,'exports',0.8);
    }
  }
}

for (const f of scan.files) {
  const source = fileId(f);
  const imports = scan.importMap?.[f.path] || [];
  for (const targetPath of imports) {
    const target = scan.files.find(x => x.path === targetPath);
    if (target) addEdge(source,fileId(target),'imports',0.7);
  }
  const r = extractByPath.get(f.path);
  for (const call of (r?.callGraph || [])) {
    const caller = `function:${f.path}:${call.caller}`;
    const callee = `function:${f.path}:${call.callee}`;
    if (nodeIds.has(caller) && nodeIds.has(callee)) addEdge(caller,callee,'calls',0.8);
  }
}

const graph = {nodes, edges};
fs.writeFileSync(path.join(ua, 'intermediate', 'batch-0.json'), JSON.stringify(graph, null, 2));
console.log(JSON.stringify({nodes:nodes.length, edges:edges.length}, null, 2));
