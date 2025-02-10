package com.weatherapp.myweatherapp.controller;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WeatherController {

  @Autowired
  WeatherService weatherService;

  @GetMapping("/forecast/{city}")
  public ResponseEntity<CityInfo> forecastByCity(@PathVariable("city") String city) {

    CityInfo ci = weatherService.forecastByCity(city);

    return ResponseEntity.ok(ci);
  }

  // Compare the length of daylight hours between two cities
  @GetMapping("/daylight/compare/{city1}/{city2}")
  public ResponseEntity<String> compareDaylightHours(@PathVariable String city1, @PathVariable String city2) {
    try{
    String dayLightDif = weatherService.compareDaylightHours(city1, city2);
    return ResponseEntity.ok(dayLightDif);
  }
  catch(Exception e){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request.");
  }
  }
  // TODO: given two city names, check which city its currently raining in
  @GetMapping("/rain/compare/{city1}/{city2}")
  public ResponseEntity<String> compareRain(@PathVariable String city1, @PathVariable String city2){
    try{
   String rainDif = weatherService.compareRain(city1, city2);
    return ResponseEntity.ok(rainDif);
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request.");
    }
  }
}

