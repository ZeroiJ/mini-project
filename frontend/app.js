// QR contract: ${FRONTEND}/student.html?s=TOKEN. Backend qrUrl matches this.
async function api(path, opts={}){
  opts.headers = {...(opts.headers||{}), "Content-Type":"application/json"};
  const t = localStorage.getItem("token");
  if(t) opts.headers["X-User-Token"] = t;
  const r = await fetch(API_BASE+path, opts);
  const j = await r.json().catch(()=>({}));
  if(!r.ok) throw new Error(j.error||("HTTP "+r.status));
  return j;
}
function needLogin(role){
  const t = localStorage.getItem("token"), r = localStorage.getItem("role");
  if(!t){ location.href="login.html"; return null; }
  if(role && r!==role){ alert("Need "+role+" login"); location.href="login.html"; return null; }
  return t;
}
