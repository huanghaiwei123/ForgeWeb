import fs from 'node:fs';
import {execFileSync} from 'node:child_process';
const ua='.ua';
const graph=JSON.parse(fs.readFileSync(`${ua}/knowledge-graph.json`,'utf8'));
const scan=JSON.parse(fs.readFileSync(`${ua}/intermediate/scan-result.json`,'utf8'));
const plan=JSON.parse(fs.readFileSync(`${ua}/intermediate/incremental-plan.json`,'utf8'));
const extract=JSON.parse(fs.readFileSync(`${ua}/intermediate/incremental-extract-results.json`,'utf8'));
const changed=new Set(plan.filesToReanalyze);
const allScan=new Map(scan.files.map(f=>[f.path,f]));
const typeFor=f=>f.fileCategory==='docs'?'document':f.fileCategory==='config'?'config':f.fileCategory==='infra'?'service':f.fileCategory==='data'?'schema':'file';
const fid=f=>`${typeFor(f)}:${f.path}`;
const ids=new Set();
const nodes=[];
for(const n of graph.nodes){if(!changed.has(n.filePath)){nodes.push(n);ids.add(n.id);}}
const edges=graph.edges.filter(e=>!changed.has(e.source.split(':').slice(1).join(':'))&&!changed.has(e.target.split(':').slice(1).join(':')));
const edgeKeys=new Set(edges.map(e=>`${e.source}|${e.target}|${e.type}`));
function addNode(n){if(!ids.has(n.id)){nodes.push(n);ids.add(n.id);}}
function addEdge(source,target,type,weight){if(ids.has(source)&&ids.has(target)&&source!==target){const k=`${source}|${target}|${type}`;if(!edgeKeys.has(k)){edges.push({source,target,type,direction:'forward',weight});edgeKeys.add(k);}}}
function tags(f){const p=f.path.toLowerCase();const t=new Set(['java',f.fileCategory]);if(p.includes('aop'))t.add('aop');if(p.includes('ioc'))t.add('ioc');if(p.includes('factory'))t.add('factory');if(p.includes('applicationserver'))t.add('entry-point');return [...t].slice(0,5)}
for(const f of scan.files.filter(f=>changed.has(f.path))){addNode({id:fid(f),type:typeFor(f),name:f.path.split('/').pop(),filePath:f.path,summary:`增量更新后的 ${f.path} 源码文件。`,tags:tags(f),complexity:f.sizeLines>200?'complex':f.sizeLines>50?'moderate':'simple'});}
for(const r of extract.results){const f=allScan.get(r.path);if(!f)continue;const file=fid(f);for(const c of r.classes||[]){const id=`class:${r.path}:${c.name}`;if((c.methods?.length||0)>=2||(c.endLine-c.startLine+1)>=20||(r.exports||[]).some(e=>e.name===c.name)){addNode({id,type:'class',name:c.name,filePath:r.path,lineRange:[c.startLine,c.endLine],summary:`${c.name} 类的最新结构定义。`,tags:tags(f),complexity:'moderate'});addEdge(file,id,'contains',1)}}for(const fn of r.functions||[]){const id=`function:${r.path}:${fn.name}`;if((fn.endLine-fn.startLine+1)>=10||(r.exports||[]).some(e=>e.name===fn.name)){addNode({id,type:'function',name:fn.name,filePath:r.path,lineRange:[fn.startLine,fn.endLine],summary:`${fn.name} 方法的最新实现。`,tags:tags(f),complexity:'simple'});addEdge(file,id,'contains',1)}}}
for(const f of scan.files.filter(f=>changed.has(f.path))){const source=fid(f);for(const targetPath of scan.importMap?.[f.path]||[]){const t=allScan.get(targetPath);if(t)addEdge(source,fid(t),'imports',.7);}}
const head=execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim();graph.nodes=nodes;graph.edges=edges;graph.project.gitCommitHash=head;graph.project.analyzedAt=new Date().toISOString();fs.writeFileSync(`${ua}/knowledge-graph.json`,JSON.stringify(graph,null,2));console.log(JSON.stringify({nodes,edges:edges.length,head},null,2));
