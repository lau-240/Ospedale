/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.*;
import java.util.ArrayList;

public interface IDataStore {

    ArrayList<User> getUsers();

    ArrayList<Appointment> getAppointments();

    ArrayList<Hospitalization> getHospitalizations();

    void addUser(User user);

    void addAppointment(Appointment appointment);

    void addHospitalization(Hospitalization hospitalization);

    User findUserByUsername(String username);

    User findUserById(long id);

    Appointment findAppointmentById(String id);

    Hospitalization findHospitalizationById(String id);
}
