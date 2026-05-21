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

public class HospitalizationController {
    private DataStore dataStore;

    public HospitalizationController() {
        this.dataStore = DataStore.getInstance();
    }

    private String generateHospitalizationId(Patient patient) {
        long patientId = patient.getId();
        int count = 0;
        for (Hospitalization h : dataStore.getHospitalizations()) {
            if (h.getPatient().getId() == patientId) count++;
        }
        return String.format("H-%d-%04d", patientId, count);
    }

    public Response requestHospitalization(long patientId, long doctorId,
            String dateStr, String reason, String roomTypeStr, String observations) {

        if (dateStr == null || dateStr.isBlank())
            return new Response(400, "La fecha no puede estar vacía");
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return new Response(400, "La fecha debe seguir el formato AAAA-MM-DD");
        }

        User userPatient = dataStore.findUserById(patientId);
        if (userPatient == null || !(userPatient instanceof Patient))
            return new Response(404, "Paciente no encontrado");

        User userDoctor = dataStore.findUserById(doctorId);
        if (userDoctor == null || !(userDoctor instanceof Doctor))
            return new Response(404, "Doctor no encontrado");

        RoomType roomType;
        try {
            roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Response(400, "Tipo de habitación no válido");
        }

        Patient patient = (Patient) userPatient;
        Doctor doctor = (Doctor) userDoctor;
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String id = generateHospitalizationId(patient);

        Hospitalization hospitalization = new Hospitalization(id, patient, doctor,
                date, reason, roomType, observations);
        dataStore.addHospitalization(hospitalization);

        return new Response(201, "Hospitalización solicitada exitosamente", id);
    }

    public Response approveHospitalization(String hospitalizationId) {
        Hospitalization h = dataStore.findHospitalizationById(hospitalizationId);
        if (h == null)
            return new Response(404, "Hospitalización no encontrada");
        if (h.getStatus() != HospitalizationStatus.REQUESTED)
            return new Response(400, "Solo se pueden aprobar hospitalizaciones en estado REQUESTED");
        h.setStatus(HospitalizationStatus.ONGOING);
        return new Response(200, "Hospitalización aprobada exitosamente");
    }

    public Response cancelHospitalization(String hospitalizationId) {
        Hospitalization h = dataStore.findHospitalizationById(hospitalizationId);
        if (h == null)
            return new Response(404, "Hospitalización no encontrada");
        if (h.getStatus() == HospitalizationStatus.ONGOING)
            return new Response(400, "No se puede cancelar una hospitalización en curso");
        h.setStatus(HospitalizationStatus.CANCELED);
        return new Response(200, "Hospitalización cancelada exitosamente");
    }

    public Response hospitalizeFromAppointment(String appointmentId, long doctorId,
            String reason, String roomTypeStr, String observations) {

        Appointment appointment = dataStore.findAppointmentById(appointmentId);
        if (appointment == null)
            return new Response(404, "Cita no encontrada");
        if (appointment.getStatus() != AppointmentStatus.PENDING)
            return new Response(400, "La cita debe estar en estado PENDING");

        User userDoctor = dataStore.findUserById(doctorId);
        if (userDoctor == null || !(userDoctor instanceof Doctor))
            return new Response(404, "Doctor no encontrado");

        RoomType roomType;
        try {
            roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Response(400, "Tipo de habitación no válido");
        }

        Patient patient = appointment.getPatient();
        Doctor doctor = (Doctor) userDoctor;
        String id = generateHospitalizationId(patient);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Hospitalization hospitalization = new Hospitalization(id, patient, doctor,
                LocalDate.now(), reason, roomType, observations,
                HospitalizationStatus.ONGOING);
        dataStore.addHospitalization(hospitalization);

        return new Response(201, "Paciente hospitalizado exitosamente", id);
    }
}
