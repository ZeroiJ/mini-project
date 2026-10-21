// QR contract: ${FRONTEND}/student.html?s=TOKEN. Backend qrUrl matches this.
async function api(path, opts={}){
  opts.headers = {...(opts.headers||{}), "Content-Type":"application/json"};
  const t = localStorage.getItem("token");
  if(t) opts.headers["X-User-Token"] = t;
  const r = await fetch(API_BASE+path, opts);
  const j = await r.json().catch(()=>({}));
  if(!r.ok){ const err=new Error(j.error||("HTTP "+r.status)); err.status=r.status; err.data=j; throw err; }
  return j;
}
// Bounded geolocation. Without a finite timeout getCurrentPosition waits forever for a
// high-accuracy fix (spec default is Infinity), which is what left "Locating…" stuck.
// High accuracy first; on timeout fall back to the fast network/cached fix.
function getPosition(){
  const once = o => new Promise((res, rej) => navigator.geolocation.getCurrentPosition(res, rej, o));
  return once({enableHighAccuracy:true, timeout:8000, maximumAge:30000})
    .catch(err => {
      if(err && err.code === 1) throw err;   // permission denied: retrying cannot help
      return once({enableHighAccuracy:false, timeout:6000, maximumAge:300000});
    });
}
function needLogin(role){
  const t = localStorage.getItem("token"), r = localStorage.getItem("role");
  if(!t){ location.href="login.html"; return null; }
  if(role && r!==role){ alert("Need "+role+" login"); location.href="login.html"; return null; }
  return t;
}
