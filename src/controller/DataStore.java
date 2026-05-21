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
import java.util.ArrayList;

public class DataStore {
    private static DataStore instance;
    private ArrayList<User> users;
    private ArrayList<Appointment> appointments;
    private ArrayList<Hospitalization> hospitalizations;

    private DataStore() {
        users = new ArrayList<>();
        appointments = new ArrayList<>();
        hospitalizations = new ArrayList<>();
        // Admin por defecto
        users.add(new Administrator(0, "admin", "Admin", "Admin", "admin123"));
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public ArrayList<User> getUsers() { return users; }
    public ArrayList<Appointment> getAppointments() { return appointments; }
    public ArrayList<Hospitalization> getHospitalizations() { return hospitalizations; }

    public void addUser(User user) { users.add(user); }
    public void addAppointment(Appointment appointment) { appointments.add(appointment); }
    public void addHospitalization(Hospitalization hospitalization) { hospitalizations.add(hospitalization); }

    public User findUserByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    public User findUserById(long id) {
        for (User u : users) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    public Appointment findAppointmentById(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    public Hospitalization findHospitalizationById(String id) {
        for (Hospitalization h : hospitalizations) {
            if (h.getId().equals(id)) return h;
        }
        return null;
    }
}