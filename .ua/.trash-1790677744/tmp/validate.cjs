const fs=require('fs');
const g=JSON.parse(fs.readFileSync(process.argv[2],'utf8'));
const issues=[],warnings=[],ids=new Set(),seen=new Set();
for(const n of g.nodes||[]){
  if(!n.id||!n.type||!n.name||!n.summary||!Array.isArray(n.tags)||!n.tags.length) issues.push(`invalid node ${n.id||'<missing>'}`);
  if(ids.has(n.id)) issues.push(`duplicate node ${n.id}`); ids.add(n.id);
}
for(const e of g.edges||[]){if(!ids.has(e.source)||!ids.has(e.target))issues.push(`dangling edge ${e.source}->${e.target}`);}
const fileTypes=new Set(['file','config','document','service','pipeline','table','schema','resource','endpoint']);
for(const l of g.layers||[]){for(const id of l.nodeIds||[]){if(!ids.has(id))issues.push(`layer dangling ${id}`);if(seen.has(id))issues.push(`layer duplicate ${id}`);seen.add(id);}}
for(const n of g.nodes.filter(n=>fileTypes.has(n.type))){if(!seen.has(n.id))issues.push(`unassigned file ${n.id}`);}
for(const t of g.tour||[]){for(const id of t.nodeIds||[]){if(!ids.has(id))issues.push(`tour dangling ${id}`);}}
for(const n of g.nodes){if(!g.edges.some(e=>e.source===n.id||e.target===n.id))warnings.push(`orphan ${n.id}`);}
const out={issues,warnings,stats:{nodes:g.nodes.length,edges:g.edges.length,layers:g.layers.length,tour:g.tour.length}};
fs.writeFileSync(process.argv[3],JSON.stringify(out,null,2)); console.log(JSON.stringify(out,null,2)); process.exit(issues.length?1:0);
