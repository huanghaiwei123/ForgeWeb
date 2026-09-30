import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';

const root = process.cwd();
const ua = path.join(root, '.ua');
const base = JSON.parse(fs.readFileSync(path.join(ua, 'intermediate', 'batch-0.json'), 'utf8'));
const fileNodes = base.nodes.filter(n => ['file','config','document','service','pipeline','schema','resource','endpoint'].includes(n.type));
const layers = [
  {id:'layer:project-guidance', name:'项目说明与构建配置', description:'项目学习规则、README、Maven 配置及其他根目录配置。', match:n => n.type === 'document' || n.type === 'config'},
  {id:'layer:bootstrap-server', name:'启动与服务器基础设施', description:'应用入口、类扫描、Netty 服务器和通用异常处理。', match:n => /src\/main\/java\/web\/(ApplicationServer|core|server|exception)\//.test(n.filePath || '') || /src\/main\/java\/web\/ApplicationServer\.java$/.test(n.filePath || '')},
  {id:'layer:ioc-aop', name:'IOC 与 AOP 容器', description:'Bean 定义、工厂、作用域、依赖注入、切面匹配和代理执行。', match:n => /src\/main\/java\/web\/(ioc|aop)\//.test(n.filePath || '')},
  {id:'layer:mvc', name:'MVC 请求处理', description:'路由映射、请求分发、参数解析和 MVC 注解。', match:n => /src\/main\/java\/web\/mvc\//.test(n.filePath || '')},
  {id:'layer:rpc', name:'RPC 通信', description:'RPC 请求响应模型、编解码、代理、注册和服务调用。', match:n => /src\/main\/java\/web\/rpc\//.test(n.filePath || '')},
  {id:'layer:orm', name:'ORM 数据访问', description:'实体注解、元数据和 SQL 生成及查询更新服务。', match:n => /src\/main\/java\/web\/orm\//.test(n.filePath || '')},
  {id:'layer:demo-tests', name:'示例与测试', description:'示例应用、控制器、实体、切面和测试代码。', match:n => /src\/(main\/java\/test|test\/)/.test(n.filePath || '')},
  {id:'layer:other', name:'其他项目文件', description:'未归入核心运行层的项目文件。', match:() => true},
];
const assigned = new Set();
for (const layer of layers) {
  layer.nodeIds = [];
  for (const n of fileNodes) {
    if (!assigned.has(n.id) && layer.match(n)) { layer.nodeIds.push(n.id); assigned.add(n.id); }
  }
  delete layer.match;
}
const tours = [
  {order:1,title:'项目入口与整体结构',description:'从 README、MiniSpringboot 和 ApplicationServer 了解框架启动方式。',nodeIds:['document:README.md','file:src/main/java/test/MiniSpringboot.java','file:src/main/java/web/ApplicationServer.java']},
  {order:2,title:'IOC Bean 生命周期',description:'学习 BeanDefinitionReader、DefaultBeanFactory 和应用上下文如何扫描、创建、缓存 Bean。',nodeIds:['file:src/main/java/web/ioc/reader/BeanDefinitionReader.java','file:src/main/java/web/ioc/pojo/BeanDefinition.java','file:src/main/java/web/ioc/factory/DefaultBeanFactory.java','file:src/main/java/web/ioc/AnnotationConfigApplicationContext.java']},
  {order:3,title:'AOP 代理链',description:'理解 PointcutMatcher、AopProxy 和 ProceedingJoinPoint 如何执行 Before、Around、After。',nodeIds:['file:src/main/java/web/aop/PointcutMatcher.java','file:src/main/java/web/aop/AopProxy.java','file:src/main/java/web/aop/ProceedingJoinPoint.java']},
  {order:4,title:'MVC 请求链路',description:'跟踪路由注册、请求分发、参数解析和控制器执行。',nodeIds:['file:src/main/java/web/mvc/HandleMapping.java','file:src/main/java/web/mvc/Dispatcher.java','file:src/main/java/web/mvc/ArgumentResolver.java','file:src/main/java/test/controller/HelloController.java']},
  {order:5,title:'扩展模块：RPC 与 ORM',description:'最后查看 RPC 调用和 ORM 元数据、SQL 生成模块，理解框架未来扩展方向。',nodeIds:['file:src/main/java/web/rpc/RpcInvoke.java','file:src/main/java/web/rpc/ServiceRegistry.java','file:src/main/java/web/orm/SqlGenerate.java']},
];
const validIds = new Set(base.nodes.map(n => n.id));
for (const layer of layers) layer.nodeIds = layer.nodeIds.filter(id => validIds.has(id));
for (const tour of tours) tour.nodeIds = tour.nodeIds.filter(id => validIds.has(id));
const graph = {
  version:'1.0.0',
  project:{name:'hhw-web-wheel',languages:['java','xml','markdown','json','properties'],frameworks:['Netty','Maven','CGLIB','Jackson'],description:'基于 Netty 手写的 MiniSpringBoot Web 框架，包含 IOC、AOP、MVC、RPC 和 ORM 学习模块。',analyzedAt:new Date().toISOString(),gitCommitHash:execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim()},
  nodes:base.nodes, edges:base.edges, layers, tour:tours
};
fs.writeFileSync(path.join(ua,'intermediate','assembled-graph.json'),JSON.stringify(graph,null,2));
fs.writeFileSync(path.join(ua,'knowledge-graph.json'),JSON.stringify(graph,null,2));
console.log(JSON.stringify({nodes:graph.nodes.length,edges:graph.edges.length,layers:layers.map(x=>[x.id,x.nodeIds.length]),tour:tours.length},null,2));
