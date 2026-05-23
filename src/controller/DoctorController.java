/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.*;

public class DoctorController {

    private IDataStore dataStore;

    public DoctorController() {
        this.dataStore = DataStore.getInstance();
    }

    private Specialty parseSpecialty(String specialty) {
        String specStr;
        switch (specialty) {
            case "Gynecology & Obstetrics":
                specStr = "GYNECOLOGY_OBSTETRICS";
                break;
            case "Traumatology & Orthopedics":
                specStr = "TRAUMATOLOGY_ORTHOPEDICS";
                break;
            case "General Medicine":
                specStr = "GENERAL_MEDICINE";
                break;
            case "Internal Medicine":
                specStr = "INTERNAL_MEDICINE";
                break;
            default:
                specStr = specialty.toUpperCase().replaceAll(" ", "_");
                break;
        }
        return Specialty.valueOf(specStr);
    }

    private Response validateDoctorData(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String specialty, String licenceNumber, String assignedOffice) {

        if (idStr == null || idStr.isBlank()) {
            return new Response(400, "El ID no puede estar vacío");
        }
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return new Response(400, "El ID debe ser numérico");
        }
        if (String.valueOf(id).length() != 12 || id <= 0) {
            return new Response(400, "El ID debe tener exactamente 12 dígitos y ser mayor que 0");
        }
        if (username == null || username.isBlank()) {
            return new Response(400, "El nombre de usuario no puede estar vacío");
        }
        if (firstname == null || firstname.isBlank()) {
            return new Response(400, "El nombre no puede estar vacío");
        }
        if (lastname == null || lastname.isBlank()) {
            return new Response(400, "El apellido no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            return new Response(400, "La contraseña no puede estar vacía");
        }
        if (!password.equals(confirmPassword)) {
            return new Response(400, "Las contraseñas no coinciden");
        }
        if (licenceNumber == null || !licenceNumber.matches("L-\\d{10} MTL")) {
            return new Response(400, "El número de licencia debe seguir el formato L-XXXXXXXXXX MTL");
        }
        if (assignedOffice == null || !assignedOffice.matches("O-\\d{3}")) {
            return new Response(400, "La oficina debe seguir el formato O-XXX");
        }
        if (specialty == null || specialty.isBlank() || specialty.equals("Select one")) {
            return new Response(400, "La especialidad no puede estar vacía");
        }
        try {
            parseSpecialty(specialty);
        } catch (IllegalArgumentException e) {
            return new Response(400, "Especialidad no válida");
        }
        return null;
    }

    public Response registerDoctor(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String specialty, String licenceNumber, String assignedOffice) {

        Response validation = validateDoctorData(idStr, username, firstname, lastname,
                password, confirmPassword, specialty, licenceNumber, assignedOffice);
        if (validation != null) {
            return validation;
        }

        long id = Long.parseLong(idStr);
        if (dataStore.findUserById(id) != null) {
            return new Response(409, "Ya existe un usuario con ese ID");
        }
        if (dataStore.findUserByUsername(username) != null) {
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");
        }

        Specialty spec = parseSpecialty(specialty);
        Doctor doctor = new Doctor(id, username, firstname, lastname, password,
                spec, licenceNumber, assignedOffice);
        dataStore.addUser(doctor);
        return new Response(201, "Doctor registrado exitosamente", id);
    }

    public Response updateDoctor(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String specialty, String licenceNumber, String assignedOffice) {

        Response validation = validateDoctorData(idStr, username, firstname, lastname,
                password, confirmPassword, specialty, licenceNumber, assignedOffice);
        if (validation != null) {
            return validation;
        }

        long id = Long.parseLong(idStr);
        User user = dataStore.findUserById(id);
        if (user == null || !(user instanceof Doctor)) {
            return new Response(404, "Doctor no encontrado");
        }

        User existingUsername = dataStore.findUserByUsername(username);
        if (existingUsername != null && existingUsername.getId() != id) {
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");
        }

        Specialty spec = parseSpecialty(specialty);
        Doctor doctor = (Doctor) user;
        doctor.setUsername(username);
        doctor.setFirstname(firstname);
        doctor.setLastname(lastname);
        doctor.setPassword(password);
        doctor.setSpecialty(spec);
        doctor.setLicenceNumber(licenceNumber);
        doctor.setAssignedOffice(assignedOffice);
        return new Response(200, "Doctor actualizado exitosamente", id);
    }

    public DoctorDTO getDoctorDTO(long id) {
        User user = dataStore.findUserById(id);
        if (user == null || !(user instanceof Doctor)) {
            return null;
        }
        Doctor doctor = (Doctor) user;
        return new DoctorDTO(
                doctor.getId(),
                doctor.getUsername(),
                doctor.getFirstname(),
                doctor.getLastname(),
                doctor.getSpecialty().name(),
                doctor.getLicenceNumber(),
                doctor.getAssignedOffice()
        );
    }
}
