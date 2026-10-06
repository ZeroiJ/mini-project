package com.attend.web;
import com.attend.entity.*;
import com.attend.repo.*;
import com.attend.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController
@CrossOrigin(origins="*")
public class ApiController {
  private final UserRepo users; private final SessionRepo sessions; private final RecordRepo records;
  private final AuthService auth; private final GeoService geo;
  @Value("${app.frontend-url:http://localhost:8000}") String frontendUrl;
  public ApiController(UserRepo u, SessionRepo s, RecordRepo r, AuthService a, GeoService g){
    users=u; sessions=s; records=r; auth=a; geo=g; }
  private User me(String token){
    if(token==null) return null;
    Long id = auth.userId(token); if(id==null) id = auth.parseId(token);
    return id==null?null:users.findById(id).orElse(null);
  }
  @PostMapping("/api/auth/login")
  public ResponseEntity<?> login(@RequestBody Map<String,String> b){
    var u = users.findByIdentifier(b.getOrDefault("identifier","")).orElse(null);
    if(u==null || !u.passwordHash.equals(b.getOrDefault("password","")))
      return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
    return ResponseEntity.ok(Map.of("token",auth.issue(u.id,u.role),"id",u.id,"name",u.name,"role",u.role));
  }
  @PostMapping("/api/sessions")
  public ResponseEntity<?> create(@RequestHeader(value="X-User-Token",required=false) String tok,
      @RequestBody Map<String,Object> b){
    User u = me(tok);
    if(u==null) return ResponseEntity.status(401).body(Map.of("error","login required"));
    if(!"PROFESSOR".equals(u.role)) return ResponseEntity.status(403).body(Map.of("error","professor only"));
    Session s = new Session();
    s.token = UUID.randomUUID().toString().replace("-","").substring(0,12);
    s.subject = String.valueOf(b.getOrDefault("subject","Class"));
    s.classroomLat = Double.parseDouble(String.valueOf(b.getOrDefault("lat","19.0760")));
    s.classroomLng = Double.parseDouble(String.valueOf(b.getOrDefault("lng","72.8777")));
    s.radiusMeters = Integer.parseInt(String.valueOf(b.getOrDefault("radius","100")));
    int mins = Integer.parseInt(String.valueOf(b.getOrDefault("durationMinutes","10")));
    s.endsAt = Instant.now().plusSeconds(mins*60L);
    s.createdBy = u.id;
    sessions.save(s);
    return ResponseEntity.ok(Map.of("id",s.id,"token",s.token,
      "qrUrl",frontendUrl+"/student.html?s="+s.token,"endsAt",s.endsAt.toString()));
  }
  @GetMapping("/api/sessions/{token}")
  public Object info(@PathVariable String token){
    var s = sessions.findByToken(token).orElse(null);
    if(s==null) return ResponseEntity.status(404).body(Map.of("error","no such session"));
    boolean expired = Instant.now().isAfter(s.endsAt);
    return Map.of("subject",s.subject,"endsAt",s.endsAt.toString(),
      "serverNow",Instant.now().toString(),"expired",expired);
  }
  @PostMapping("/api/attendance")
  public ResponseEntity<?> mark(@RequestHeader(value="X-User-Token",required=false) String tok,
      @RequestBody Map<String,Object> b){
    User u = me(tok);
    if(u==null) return ResponseEntity.status(401).body(Map.of("error","login required"));
    if(!"STUDENT".equals(u.role)) return ResponseEntity.status(403).body(Map.of("error","student only"));
    var s = sessions.findByToken(String.valueOf(b.getOrDefault("sessionToken",""))).orElse(null);
    if(s==null) return ResponseEntity.status(404).body(Map.of("error","no such session"));
    if(Instant.now().isAfter(s.endsAt)) return ResponseEntity.status(410).body(Map.of("error","session expired"));
    if(records.existsBySessionIdAndStudentId(s.id,u.id))
      return ResponseEntity.status(409).body(Map.of("error","already marked"));
    double lat=Double.parseDouble(String.valueOf(b.get("lat"))), lng=Double.parseDouble(String.valueOf(b.get("lng")));
    double d = geo.meters(s.classroomLat,s.classroomLng,lat,lng);
    if(d > s.radiusMeters) return ResponseEntity.status(422).body(Map.of("error","out of range","distanceMeters",d));
    Record r = new Record(); r.sessionId=s.id; r.studentId=u.id; r.lat=lat; r.lng=lng; r.distanceMeters=d;
    try{ records.save(r); }catch(Exception e){ return ResponseEntity.status(409).body(Map.of("error","already marked")); }
    return ResponseEntity.ok(Map.of("ok",true,"distanceMeters",d));
  }
  @GetMapping("/api/sessions/{id}/records")
  public ResponseEntity<?> list(@RequestHeader(value="X-User-Token",required=false) String tok,
      @PathVariable Long id,
      @RequestParam(value="format",required=false) String format){
    User u = me(tok);
    if(u==null) return ResponseEntity.status(401).body(Map.of("error","login required"));
    if(!"PROFESSOR".equals(u.role)) return ResponseEntity.status(403).body(Map.of("error","professor only"));
    var out = new ArrayList<Map<String,Object>>();
    for(var r: records.findBySessionId(id)){
      var st = users.findById(r.studentId).orElse(null);
      out.add(Map.of("student",st==null?"?":st.name,"identifier",st==null?"?":st.identifier,
        "distanceMeters",Math.round(r.distanceMeters*10)/10.0,"submittedAt",r.submittedAt.toString()));
    }
    if("csv".equals(format)){
      StringBuilder sb = new StringBuilder("student,identifier,distance_m,submitted_at\n");
      for(var m: out) sb.append(m.get("student")).append(",").append(m.get("identifier")).append(",")
        .append(m.get("distanceMeters")).append(",").append(m.get("submittedAt")).append("\n");
      return ResponseEntity.ok().header("Content-Disposition","attachment; filename=attendance_"+id+".csv")
        .contentType(MediaType.parseMediaType("text/csv")).body(sb.toString());
    }
    return ResponseEntity.ok(out);
  }
}
