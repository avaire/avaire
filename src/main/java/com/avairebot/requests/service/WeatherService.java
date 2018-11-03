/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.requests.service;

import com.avairebot.language.I18n;

import java.util.List;

@SuppressWarnings("unused")
public class WeatherService {

    private String id;
    private String name;
    private String cod;
    private long dt;
    private Location sys;
    private Wind wind;
    private Temperature main;
    private List<Weather> weather;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCod() {
        return cod;
    }

    public long getDt() {
        return dt;
    }

    public Location getLocation() {
        return sys;
    }

    public Wind getWind() {
        return wind;
    }

    public Temperature getTemperature() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public class Location {

        private String id;
        private String message;
        private String country;
        private long sunrise;
        private long sunset;

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public String getCountry() {
            return country;
        }

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }
    }

    public class Wind {

        private double speed;
        private double deg;

        public double getSpeed() {
            return speed;
        }

        public double getDeg() {
            return deg;
        }
    }

    public class Temperature {

        private double temp;
        private double pressure;
        private double humidity;
        private double temp_min;
        private double temp_max;

        public double getTemp() {
            return temp;
        }

        public double getPressure() {
            return pressure;
        }

        public double getHumidity() {
            return humidity;
        }

        public double getTempMin() {
            return temp_min;
        }

        public double getTempMax() {
            return temp_max;
        }
    }

    public class Weather {

        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() {
            return id;
        }

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public String getIconUrl() {
            return I18n.format("https://openweathermap.org/img/w/{0}.png", icon);
        }
    }
}
