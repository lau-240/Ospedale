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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PatientController {
    private DataStore dataStore;

    public PatientController() {
        this.dataStore = DataStore.getInstance();
    }

    private Response validatePatientData(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String email, String birthdateStr, String genderStr,
            String phoneStr, String address) {

        // Validar ID
        if (idStr == null || idStr.isBlank())
            return new Response(400, "El ID no puede estar vacío");
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return new Response(400, "El ID debe ser numérico");
        }
        if (String.valueOf(id).length() != 12 || id <= 0)
            return new Response(400, "El ID debe tener exactamente 12 dígitos y ser mayor que 0");

        // Validar username
        if (username == null || username.isBlank())
            return new Response(400, "El nombre de usuario no puede estar vacío");

        // Validar nombre y apellido
        if (firstname == null || firstname.isBlank())
            return new Response(400, "El nombre no puede estar vacío");
        if (lastname == null || lastname.isBlank())
            return new Response(400, "El apellido no puede estar vacío");

        // Validar contraseña
        if (password == null || password.isBlank())
            return new Response(400, "La contraseña no puede estar vacía");
        if (!password.equals(confirmPassword))
            return new Response(400, "Las contraseñas no coinciden");

        // Validar email
        if (email == null || !email.matches("[^@]+@[^@]+\\.com"))
            return new Response(400, "El email debe seguir el formato XXXXX@XXXXX.com");

        // Validar fecha de nacimiento
        if (birthdateStr == null || birthdateStr.isBlank())
            return new Response(400, "La fecha de nacimiento no puede estar vacía");
        try {
            LocalDate.parse(birthdateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return new Response(400, "La fecha debe seguir el formato AAAA-MM-DD");
        }

        // Validar teléfono
        if (phoneStr == null || !phoneStr.matches("\\d{10}"))
            return new Response(400, "El teléfono debe tener exactamente 10 dígitos");

        // Validar dirección
        if (address == null || address.isBlank())
            return new Response(400, "La dirección no puede estar vacía");

        return null; // sin errores
    }

    public Response registerPatient(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String email, String birthdateStr, String genderStr,
            String phoneStr, String address) {

        Response validation = validatePatientData(idStr, username, firstname, lastname,
                password, confirmPassword, email, birthdateStr, genderStr, phoneStr, address);
        if (validation != null) return validation;

        long id = Long.parseLong(idStr);

        if (dataStore.findUserById(id) != null)
            return new Response(409, "Ya existe un usuario con ese ID");
        if (dataStore.findUserByUsername(username) != null)
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");

        LocalDate birthdate = LocalDate.parse(birthdateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean gender = genderStr.equalsIgnoreCase("M");
        long phone = Long.parseLong(phoneStr);

        Patient patient = new Patient(id, username, firstname, lastname, password,
                email, birthdate, gender, phone, address);
        dataStore.addUser(patient);

        return new Response(201, "Paciente registrado exitosamente", id);
    }

    public Response updatePatient(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String email, String birthdateStr, String genderStr,
            String phoneStr, String address) {

        Response validation = validatePatientData(idStr, username, firstname, lastname,
                password, confirmPassword, email, birthdateStr, genderStr, phoneStr, address);
        if (validation != null) return validation;

        long id = Long.parseLong(idStr);
        User user = dataStore.findUserById(id);

        if (user == null || !(user instanceof Patient))
            return new Response(404, "Paciente no encontrado");

        User existingUsername = dataStore.findUserByUsername(username);
        if (existingUsername != null && existingUsername.getId() != id)
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");

        Patient patient = (Patient) user;
        patient.setUsername(username);
        patient.setFirstname(firstname);
        patient.setLastname(lastname);
        patient.setPassword(password);
        patient.setEmail(email);
        patient.setBirthdate(LocalDate.parse(birthdateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        patient.setGender(genderStr.equalsIgnoreCase("M"));
        patient.setPhone(Long.parseLong(phoneStr));
        patient.setAddress(address);

        return new Response(200, "Paciente actualizado exitosamente", id);
    }
}
