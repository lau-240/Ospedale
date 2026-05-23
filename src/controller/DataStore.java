/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author Laura
 */
import model.*;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;

public class DataStore implements Observable, IDataStore {

    private static DataStore instance;
    private ArrayList<User> users;
    private ArrayList<Appointment> appointments;
    private ArrayList<Hospitalization> hospitalizations;
    private ArrayList<Observer> observers = new ArrayList<>();

    private DataStore() {
        users = new ArrayList<>();
        appointments = new ArrayList<>();
        hospitalizations = new ArrayList<>();
        // Admin por defecto
        loadUsersFromJSON();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void loadUsersFromJSON() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("json/users.json")));
            JSONObject root = new JSONObject(content);
            JSONArray usersArray = root.getJSONArray("users");

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject u = usersArray.getJSONObject(i);
                String type = u.getString("type");
                long id = u.getLong("id");
                String username = u.getString("username");
                String firstname = u.getString("firstname");
                String lastname = u.getString("lastname");
                String password = u.getString("password");

                switch (type) {
                    case "admin":
                        users.add(new Administrator(id, username, firstname, lastname, password));
                        break;
                    case "patient":
                        String email = u.getString("email");
                        LocalDate birthdate = LocalDate.parse(u.getString("birthdate"));
                        boolean gender = u.getBoolean("gender");
                        long phone = u.getLong("phone");
                        String address = u.getString("address");
                        users.add(new Patient(id, username, firstname, lastname, password,
                                email, birthdate, gender, phone, address));
                        break;
                    case "doctor":
                        String specStr = u.getString("specialty");

                        if (specStr.equals("ORTHOPEDICS")) {
                            specStr = "TRAUMATOLOGY_ORTHOPEDICS";
                        }
                        if (specStr.equals("GYNECOLOGY")) {
                            specStr = "GYNECOLOGY_OBSTETRICS";
                        }
                        Specialty specialty = Specialty.valueOf(specStr);
                        String licenceNumber = u.getString("licenceNumber");
                        String assignedOffice = u.getString("assignedOffice");
                        users.add(new Doctor(id, username, firstname, lastname, password,
                                specialty, licenceNumber, assignedOffice));
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar users.json: " + e.getMessage());
            // Admin por defecto si no se puede cargar el JSON
            users.add(new Administrator(0, "admin", "Admin", "Admin", "admin123"));
        }
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public ArrayList<Hospitalization> getHospitalizations() {
        return hospitalizations;
    }

    public void addUser(User user) {
        users.add(user);
        notifyObservers();
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        notifyObservers();
    }

    public void addHospitalization(Hospitalization hospitalization) {
        hospitalizations.add(hospitalization);
        notifyObservers();
    }

    public User findUserByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public User findUserById(long id) {
        for (User u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public Appointment findAppointmentById(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public Hospitalization findHospitalizationById(String id) {
        for (Hospitalization h : hospitalizations) {
            if (h.getId().equals(id)) {
                return h;
            }
        }
        return null;
    }
}
