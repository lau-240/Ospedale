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

public class DoctorController {
    private DataStore dataStore;

    public DoctorController() {
        this.dataStore = DataStore.getInstance();
    }

    private Response validateDoctorData(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String specialty, String licenceNumber, String assignedOffice) {

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

        // Validar número de licencia: L-XXXXXXXXXX MTL
        if (licenceNumber == null || !licenceNumber.matches("L-\\d{10} MTL"))
            return new Response(400, "El número de licencia debe seguir el formato L-XXXXXXXXXX MTL");

        // Validar oficina: O-XXX
        if (assignedOffice == null || !assignedOffice.matches("O-\\d{3}"))
            return new Response(400, "La oficina debe seguir el formato O-XXX");

        // Validar especialidad
        if (specialty == null || specialty.isBlank())
            return new Response(400, "La especialidad no puede estar vacía");
        try {
            Specialty.valueOf(specialty.toUpperCase().replaceAll(" ", "_").replaceAll(" &", ""));
        } catch (IllegalArgumentException e) {
            return new Response(400, "Especialidad no válida");
        }

        return null; // sin errores
    }

    public Response registerDoctor(String idStr, String username, String firstname,
            String lastname, String password, String confirmPassword,
            String specialty, String licenceNumber, String assignedOffice) {

        Response validation = validateDoctorData(idStr, username, firstname, lastname,
                password, confirmPassword, specialty, licenceNumber, assignedOffice);
        if (validation != null) return validation;

        long id = Long.parseLong(idStr);

        if (dataStore.findUserById(id) != null)
            return new Response(409, "Ya existe un usuario con ese ID");
        if (dataStore.findUserByUsername(username) != null)
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");

        Specialty spec = Specialty.valueOf(specialty.toUpperCase().replaceAll(" ", "_").replaceAll(" &", ""));
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
        if (validation != null) return validation;

        long id = Long.parseLong(idStr);
        User user = dataStore.findUserById(id);

        if (user == null || !(user instanceof Doctor))
            return new Response(404, "Doctor no encontrado");

        User existingUsername = dataStore.findUserByUsername(username);
        if (existingUsername != null && existingUsername.getId() != id)
            return new Response(409, "Ya existe un usuario con ese nombre de usuario");

        Specialty spec = Specialty.valueOf(specialty.toUpperCase().replaceAll(" ", "_").replaceAll(" &", ""));
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
}
