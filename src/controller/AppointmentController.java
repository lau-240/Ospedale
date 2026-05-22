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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class AppointmentController {

    private DataStore dataStore;

    public AppointmentController() {
        this.dataStore = DataStore.getInstance();
    }

    private Response validateDateTime(String dateStr, String timeStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return new Response(400, "La fecha no puede estar vacía");
        }
        if (timeStr == null || timeStr.isBlank()) {
            return new Response(400, "La hora no puede estar vacía");
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return new Response(400, "La fecha debe seguir el formato AAAA-MM-DD");
        }
        if (!timeStr.matches("([01]\\d|2[0-3]):(00|15|30|45)")) {
            return new Response(400, "La hora debe seguir el formato hh:mm con minutos en 00, 15, 30 o 45");
        }
        return null;
    }

    private String generateAppointmentId(Patient patient) {
        long patientId = patient.getId();
        int count = 0;
        for (Appointment a : dataStore.getAppointments()) {
            if (a.getPatient().getId() == patientId) {
                count++;
            }
        }
        return String.format("A-%d-%04d", patientId, count);
    }

    private boolean isDoctorAvailable(Doctor doctor, LocalDateTime datetime) {
        for (Appointment a : dataStore.getAppointments()) {
            if (a.getDoctor().getId() == doctor.getId()
                    && a.getStatus() != AppointmentStatus.CANCELED) {
                LocalDateTime start = a.getDatetime();
                LocalDateTime end = start.plusMinutes(15);
                if (!datetime.isBefore(start) && datetime.isBefore(end)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Response requestAppointment(long patientId, String dateStr, String timeStr,
            String reason, boolean byDoctor, long doctorId, String specialtyStr) {

        Response dateValidation = validateDateTime(dateStr, timeStr);
        if (dateValidation != null) {
            return dateValidation;
        }

        User userPatient = dataStore.findUserById(patientId);
        if (userPatient == null || !(userPatient instanceof Patient)) {
            return new Response(404, "Paciente no encontrado");
        }

        Patient patient = (Patient) userPatient;
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime datetime = LocalDateTime.of(date, time);

        Doctor doctor = null;
        Specialty specialty = null;

        if (byDoctor) {
            User userDoctor = dataStore.findUserById(doctorId);
            if (userDoctor == null || !(userDoctor instanceof Doctor)) {
                return new Response(404, "Doctor no encontrado");
            }
            doctor = (Doctor) userDoctor;
            specialty = doctor.getSpecialty();
            if (!isDoctorAvailable(doctor, datetime)) {
                return new Response(409, "El doctor no tiene disponibilidad en ese horario");
            }
        } else {
            try {
                specialty = Specialty.valueOf(specialtyStr.toUpperCase().replaceAll(" ", "_").replaceAll(" &", ""));
            } catch (IllegalArgumentException e) {
                return new Response(400, "Especialidad no válida");
            }
            for (User u : dataStore.getUsers()) {
                if (u instanceof Doctor) {
                    Doctor d = (Doctor) u;
                    if (d.getSpecialty() == specialty && isDoctorAvailable(d, datetime)) {
                        doctor = d;
                        break;
                    }
                }
            }
            if (doctor == null) {
                return new Response(404, "No hay doctores disponibles con esa especialidad en ese horario");
            }
        }

        String id = generateAppointmentId(patient);
        Appointment appointment = new Appointment(id, patient, doctor, specialty, datetime, reason, byDoctor);
        dataStore.addAppointment(appointment);
        patient.addAppointment(appointment);
        doctor.addAppointment(appointment);
        return new Response(201, "Cita solicitada exitosamente", id);
    }

    public Response acceptAppointment(String appointmentId) {
        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null) {
            return new Response(404, "Cita no encontrada");
        }
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            return new Response(400, "Solo se pueden aceptar citas en estado REQUESTED");
        }
        appointment.setStatus(AppointmentStatus.PENDING);
        return new Response(200, "Cita aceptada exitosamente");
    }

    public Response completeAppointment(String appointmentId) {
        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null) {
            return new Response(404, "Cita no encontrada");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return new Response(400, "Solo se pueden completar citas en estado PENDING");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return new Response(200, "Cita completada exitosamente");
    }

    public Response cancelAppointment(String appointmentId) {
        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null) {
            return new Response(404, "Cita no encontrada");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return new Response(400, "No se puede cancelar una cita ya completada");
        }
        appointment.setStatus(AppointmentStatus.CANCELED);
        return new Response(200, "Cita cancelada exitosamente");
    }

    public Response rescheduleAppointment(String appointmentId, String newTimeStr, String rescheduleReason) {
        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null) {
            return new Response(404, "Cita no encontrada");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELED) {
            return new Response(400, "No se puede reagendar una cita completada o cancelada");
        }
        if (!newTimeStr.matches("([01]\\d|2[0-3]):(00|15|30|45)")) {
            return new Response(400, "La hora debe seguir el formato hh:mm con minutos en 00, 15, 30 o 45");
        }

        LocalDate originalDate = appointment.getDatetime().toLocalDate();
        LocalTime newTime = LocalTime.parse(newTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime newDatetime = LocalDateTime.of(originalDate, newTime);

        if (!isDoctorAvailable(appointment.getDoctor(), newDatetime)) {
            return new Response(409, "El doctor no tiene disponibilidad en ese horario");
        }

        appointment.setDatetime(newDatetime);
        appointment.setReason(appointment.getDatetime() + " - " + rescheduleReason);
        return new Response(200, "Cita reagendada exitosamente");
    }
    



public Response prescribeMedication(String appointmentId, String medicationName,
            double dose, String adminRoute, int duration, String instructions, int frecuency) {
        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null) {
            return new Response(404, "Cita no encontrada");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return new Response(400, "Solo se pueden prescribir medicamentos en citas en estado PENDING");
        }

        new Prescription(appointment, medicationName, dose, adminRoute, duration, instructions, frecuency);
        return new Response(200, "Medicamento prescrito exitosamente");
    }

    public Response getPatientAppointments(long patientId) {
        User user = dataStore.findUserById(patientId);
        if (user == null || !(user instanceof Patient)) {
            return new Response(404, "Paciente no encontrado");
        }
        Patient patient = (Patient) user;
        ArrayList<Appointment> sorted = new ArrayList<>(patient.getAppointments());
        sorted.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));
        return new Response(200, "Citas obtenidas", sorted.stream()
                .map(a -> a.getId() + " | " + a.getDatetime() + " | " + a.getStatus())
                .toArray());
    }

    public Response getDoctorAppointments(long doctorId, boolean onlyPending) {
        User user = dataStore.findUserById(doctorId);
        if (user == null || !(user instanceof Doctor)) {
            return new Response(404, "Doctor no encontrado");
        }
        Doctor doctor = (Doctor) user;
        ArrayList<Appointment> sorted = new ArrayList<>(doctor.getAppointments());
        if (onlyPending) {
            sorted.removeIf(a -> a.getStatus() != AppointmentStatus.PENDING);
        }
        sorted.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));
        return new Response(200, "Citas obtenidas", sorted.stream()
                .map(a -> a.getId() + " | " + a.getDatetime() + " | " + a.getStatus())
                .toArray());
    }

}
