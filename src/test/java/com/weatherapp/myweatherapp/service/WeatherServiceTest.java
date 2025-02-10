package com.weatherapp.myweatherapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.model.CityInfo.CurrentConditions;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private VisualcrossingRepository mockRepository;

    @InjectMocks
    private WeatherService weatherService;

    private CityInfo cityInfo1;
    private CityInfo cityInfo2;

    @BeforeEach
    void setup() {
        // Set up CityInfo objects for tests

        // CityInfo #1
        cityInfo1 = new CityInfo();
        CurrentConditions conditions1 = new CurrentConditions();
        // Example sunrise/sunset times
        conditions1.setSunrise("06:30");
        conditions1.setSunset("18:30");
        conditions1.setConditions("Rain");
        cityInfo1.setCurrentConditions(conditions1);

        // CityInfo #2
        cityInfo2 = new CityInfo();
        CurrentConditions conditions2 = new CurrentConditions();
        conditions2.setSunrise("07:00");
        conditions2.setSunset("19:00");
        conditions2.setConditions("Clear");
        cityInfo2.setCurrentConditions(conditions2);
    }

    @Test
    void testForecastByCity_Success() {
        // given
        String cityName = "TestCity";
        when(mockRepository.getByCity(cityName)).thenReturn(cityInfo1);

        // when
        CityInfo result = weatherService.forecastByCity(cityName);

        // then
        assertNotNull(result);
        assertEquals(cityInfo1, result);
        verify(mockRepository, times(1)).getByCity(cityName);
    }

    @Test
    void testForecastByCity_NotFound() {
        // given
        String cityName = "UnknownCity";
        when(mockRepository.getByCity(cityName)).thenReturn(null);

        // when
        CityInfo result = weatherService.forecastByCity(cityName);

        // then
        assertNull(result);
        verify(mockRepository, times(1)).getByCity(cityName);
    }
    @Test
    void testDaylightHours_ValidTimes() {
        // "06:30" to "18:30" => 12 hours => 720 minutes
        Long minutes = weatherService.daylightHours("06:30", "18:30");
        assertEquals(720L, minutes);
    }

    @Test
    void testDaylightHours_InvalidTime() {
        // If either string is invalid or parse fails => 0L
        Long minutes = weatherService.daylightHours("invalid", "19:00");
        assertEquals(0L, minutes);
    }

    @Test
    void testDaylightHours_NullValues() {
        Long minutes = weatherService.daylightHours(null, null);
        assertEquals(0L, minutes);
    }

    @Test
    void testCompareDaylightHours_City1MoreDaylight() {
        // given
        // cityInfo1 => Sunrise 06:30, Sunset 18:30 => 12h
        // cityInfo2 => Sunrise 07:00, Sunset 19:00 => 12h as well, but let's modify cityInfo2 to have less daylight

        CurrentConditions cc2 = cityInfo2.getCurrentConditions();
        cc2.setSunrise("08:00");
        cc2.setSunset("18:00"); // => 10h total
        cityInfo2.setCurrentConditions(cc2);

        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        // when
        String result = weatherService.compareDaylightHours("City1", "City2");

        // then
        assertEquals("City1 has more day light than City2", result);
    }

    @Test
    void testCompareDaylightHours_City2MoreDaylight() {
        // Make City1 have less daylight
        CurrentConditions cc1 = cityInfo1.getCurrentConditions();
        cc1.setSunrise("07:00");
        cc1.setSunset("17:00"); // => 10h total
        cityInfo1.setCurrentConditions(cc1);

        // City2 => 07:00 to 19:00 => 12h
        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareDaylightHours("City1", "City2");

        assertEquals("City2 has more day light than City1", result);
    }

    @Test
    void testCompareDaylightHours_SameDaylight() {
        // cityInfo1 => 06:30 to 18:30 => 12h
        // cityInfo2 => 07:00 to 19:00 => 12h as well => difference is also 12h
        // Let's make them both 12:00 total so it's the same
        CurrentConditions cc2 = cityInfo2.getCurrentConditions();
        cc2.setSunrise("06:30");
        cc2.setSunset("18:30");
        cityInfo2.setCurrentConditions(cc2);

        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareDaylightHours("City1", "City2");

        assertEquals("Both City1 City2 have the same daylight hours", result);
    }

    @Test
    void testCompareDaylightHours_NullCityInfo() {
        // One or both returns null
        when(mockRepository.getByCity("City1")).thenReturn(null);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareDaylightHours("City1", "City2");
        assertEquals("Sunrise and Sunset data not available for one or both cities", result);
    }

   
    @Test
    void testCompareRain_BothRaining() {
        // cityInfo1 => "Rain"
        // cityInfo2 => "Rain"
        cityInfo2.getCurrentConditions().setConditions("Rain");

        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareRain("City1", "City2");
        assertEquals("Both City1 City2 are raining", result);
    }

    @Test
    void testCompareRain_OnlyCity1Raining() {
        // cityInfo1 => "Rain"
        // cityInfo2 => "Clear"
        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareRain("City1", "City2");
        assertEquals("It is currently raining in City1", result);
    }

    @Test
    void testCompareRain_OnlyCity2Raining() {
        // cityInfo1 => "Clear"
        cityInfo1.getCurrentConditions().setConditions("Clear");
        // cityInfo2 => "Rain"
        cityInfo2.getCurrentConditions().setConditions("Rain");

        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareRain("City1", "City2");
        assertEquals("It is currently raining in City2", result);
    }

    @Test
    void testCompareRain_NeitherRaining() {
      cityInfo1.getCurrentConditions().setConditions("Clear");
      cityInfo2.getCurrentConditions().setConditions("Cloudy");

      when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
      when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

      String result = weatherService.compareRain("City1", "City2");
      assertEquals("Neither City1 or City2 are raining", result);
      }


    @Test
    void testCompareRain_NullCityInfo() {
        when(mockRepository.getByCity("City1")).thenReturn(null);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareRain("City1", "City2");
        assertEquals("Weather data not available for one or both cities", result);
    }

    @Test
    void testCompareRain_NullConditions() {
        // If the currentConditions object or conditions string is null, it should return the "Weather data not available" message
        cityInfo2.setCurrentConditions(null);

        when(mockRepository.getByCity("City1")).thenReturn(cityInfo1);
        when(mockRepository.getByCity("City2")).thenReturn(cityInfo2);

        String result = weatherService.compareRain("City1", "City2");
        assertEquals("Weather data not available for one or both cities", result);
    }
}
