package com.attend.service;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// ponytail: demo-only token store (in-memory). Upgrade: JWT/DB sessions.
@Service
public class AuthService {
  private final Map<String,Long> tokens = new ConcurrentHashMap<>();
  private final Map<Long,String> roles = new ConcurrentHashMap<>();
  public String issue(Long id, String role){ String t="tok_"+id+"_"+System.nanoTime()%100000; tokens.put(t,id); roles.put(id,role); return t; }
  public Long userId(String t){ return t==null?null:tokens.get(t); }
  public String role(Long id){ return roles.get(id); }
  // Re-hydrated on login; for restart-safe demo the userId is also parsed from tok_{id}_*
  public Long parseId(String t){ try{ return Long.parseLong(t.split("_")[1]); }catch(Exception e){ return null; } }

  // BCrypt: one-way hash at seed time, constant-time verify at login time.
  public String hash(String plain){ return BCrypt.hashpw(plain, BCrypt.gensalt(10)); }
  public boolean verify(String plain, String stored){
    if(plain==null || stored==null) return false;
    // Guard: legacy plaintext rows (no $2a$ prefix) -> reject instead of throwing.
    if(!stored.startsWith("$2")) return false;
    try{ return BCrypt.checkpw(plain, stored); }catch(Exception e){ return false; }
  }
}
