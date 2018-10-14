/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apimeetup;

import beans.City;
import beans.Event;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

/**
 *
 * @author Nikola
 */
public class APImeetup {

    private final static String USER_AGENT = "Mozilla/5.0";
    private final static ArrayList<City> listOfCities = new ArrayList<>();
    private final static ArrayList<Event> listOfEvents = new ArrayList<>();
    private final static String URL = "https://api.meetup.com/";
    private final static String KEY = "7831393b49c2f6d95f1735782f6f";
    private static StringBuilder response;
    private static StringBuilder response1;

    /**
     * @param args the command line arguments
     * @throws java.net.MalformedURLException
     * @throws org.json.simple.parser.ParseException
     */
    public static void main(String[] args) throws MalformedURLException, IOException, ParseException {

        String url = URL + "2/cities?country=rs";

        response = APImeetup.requestHTTP(url);
        /* Samo jednom se izvršava ovaj deo svaki sledeći put ostaje ista lista tek kad korisnik pokrene 
            naredni put aplikaciju ukoliko su se pojavili novi gradovi bice ocitani u listi, i radi se parsiranje JSON fajla*/
        System.out.println(response.toString());
        System.out.println("JSON parsing");
        JSONParser parser = new JSONParser();
        JSONObject myResponse = (JSONObject) parser.parse(response.toString());
        JSONArray results = (JSONArray) myResponse.get("results");
        for (Object grad : results) {
            City city = new City();
            JSONObject cityObj = (JSONObject) grad;
            long num = (Long) cityObj.get("ranking");
            String cityView = (String) cityObj.get("city");
            double lon = ((Number) cityObj.get("lon")).doubleValue();
            double lat = ((Number) cityObj.get("lat")).doubleValue();
            city.setNumber(num);
            city.setCity(cityView);
            city.setLat(lat);
            city.setLon(lon);
            listOfCities.add(city);
        }
        /* U ovoj petlji se ispisuju liste dogadjaja za odabrani grad, ispisuju se dogadjaji u radiusu od 50 milja od odabranog grada
            , a GET zahtev se vrši sa parametrima longitude and latitude, nakon čega se vrši parsiranje JSON fajla sa dogadjajima*/
        while (true) {
            listOfEvents.clear();
            for (City c : listOfCities) {
                System.out.println(c);
            }
            System.out.println("Enter -1 to exit");
            System.out.println("Enter your choice: ");
            Scanner scanner = new Scanner(System.in);
            long choice = scanner.nextLong();
            if (choice == -1) {
                System.out.println("The end!");
                break;
            }

            if (choice > listOfCities.size() - 1) {
                System.out.println("Index out of bound");
                continue;
            }

            double lon = -1;
            double lat = -1;
            for (City c : listOfCities) {
                if (c.getNumber() == choice) {
                    lon = c.getLon();
                    lat = c.getLat();
                    break;
                }
            }

            String url1 = URL + "find/upcoming_events?lat=" + lat + "&lon=" + lon + "&key=" + KEY;
            response1 = APImeetup.requestHTTP(url1);

            System.out.println(response1.toString());

            System.out.println("JSON parsing");
            JSONParser parser1 = new JSONParser();
            JSONObject myResponse1 = (JSONObject) parser1.parse(response1.toString());
            JSONArray results1 = (JSONArray) myResponse1.get("events");

            for (Object event : results1) {
                String address = "";
                Event event1 = new Event();
                JSONObject eventObj = (JSONObject) event;
                String name = (String) eventObj.get("name");
                String localDate = (String) eventObj.get("local_date");
                String localTime = (String) eventObj.get("local_time");
                String description = (String) eventObj.get("description");
                JSONObject object = (JSONObject) eventObj.get("venue");
                if (object != null) {
                    address = (String) object.get("address_1");
                }

                event1.setName(name);
                event1.setLocalDate(localDate);
                event1.setLocalTime(localTime);
                event1.setDescription(description);
                event1.setAddress(address);
                listOfEvents.add(event1);

            }

            for (Event e : listOfEvents) {
                System.out.println(e);
            }

        }
    }

    /**
     * @param url address on server
     * @return
     * @throws java.net.MalformedURLException
     *
     */
    public static StringBuilder requestHTTP(String url) throws MalformedURLException, IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        int responseCode = connection.getResponseCode();
        System.out.println("Sending GET request to URL: " + url);
        System.out.println("Response Code: " + responseCode);
        StringBuilder response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);

            }
        }
        return response;
    }

}
