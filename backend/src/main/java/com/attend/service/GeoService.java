package com.attend.service;
import org.springframework.stereotype.Service;
@Service
public class GeoService {
  // Haversine, meters. ponytail: edge-correct over naive Euclidean, same size.
  public double meters(double lat1,double lon1,double lat2,double lon2){
    double R=6371000, dLat=Math.toRadians(lat2-lat1), dLon=Math.toRadians(lon2-lon1);
    double a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2*R*Math.asin(Math.sqrt(a));
  }
}
