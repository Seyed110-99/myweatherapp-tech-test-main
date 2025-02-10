package com.weatherapp.myweatherapp.service;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

  @Autowired
  VisualcrossingRepository weatherRepo;

  public CityInfo forecastByCity(String city) {

    return weatherRepo.getByCity(city);
  }

  public Long daylightHours(String sunrise, String sunset){

    if (sunrise == null || sunset == null){
      return 0L;
    }
    try{
    LocalTime sunriseTime = LocalTime.parse(sunrise);
    LocalTime sunsetTime = LocalTime.parse(sunset);
    Duration daylight = Duration.between(sunriseTime, sunsetTime);
    return daylight.toMinutes();
    }catch(Exception e){
      return 0L;
    }
  }

  public String compareDaylightHours(String city1, String city2){
    try{
    CityInfo cityinfo1 = weatherRepo.getByCity(city1);
    CityInfo cityinfo2 = weatherRepo.getByCity(city2);
    if(cityinfo1 == null || cityinfo2 == null || cityinfo1.getCurrentConditions() == null || cityinfo2.getCurrentConditions() == null){
      return "Sunrise and Sunset data not available for one or both cities";
    }

    long daylight1 = daylightHours(cityinfo1.getCurrentConditions().getSunrise(), cityinfo1.getCurrentConditions().getSunset());
    long daylight2 = daylightHours(cityinfo2.getCurrentConditions().getSunrise(), cityinfo2.getCurrentConditions().getSunset());

    if (daylight1 > daylight2){
      return city1 + " has more day light than "+ city2;}
    else if(daylight2 > daylight1){
      return city2 + " has more day light than "+ city1;}
    else{
      return "Both " + city1 + " " + city2 + " have the same daylight hours";}
  }
  catch(Exception e){
    return "Error processing request.";
  }
}

public String compareRain(String city1, String city2) {
  try {
      CityInfo cityinfo1 = weatherRepo.getByCity(city1);
      CityInfo cityinfo2 = weatherRepo.getByCity(city2);

      if (cityinfo1 == null || cityinfo2 == null || 
          cityinfo1.getCurrentConditions() == null || 
          cityinfo2.getCurrentConditions() == null || 
          cityinfo1.getCurrentConditions().getConditions() == null || 
          cityinfo2.getCurrentConditions().getConditions() == null) {
          return "Weather data not available for one or both cities";
      }

      boolean RainingCity1 = cityinfo1.getCurrentConditions().getConditions().toLowerCase().contains("rain");
      boolean RainingCity2 = cityinfo2.getCurrentConditions().getConditions().toLowerCase().contains("rain");

      if (RainingCity1 && RainingCity2) {
          return "Both " + city1 + " " + city2 + " are raining";
      } else if (RainingCity1) {
          return "It is currently raining in " + city1;
      } else if (RainingCity2) {
          return "It is currently raining in " + city2;
      } else {
          return "Neither " + city1 + " or " + city2 + " are raining";
      }
  } catch (Exception e) {
      return "Error processing request.";
  }
  }
}
