package com.nci.fiuza.service;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.AppointmentStatus;
import com.nci.fiuza.repository.AppointmentRepository;
import com.nci.fiuza.repository.ServiceRepository;
import com.nci.fiuza.util.BusinessHours;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//business logic class
@Service
public class AppointmentService {

    //i'll need access to the AppointmentRepository
    private final AppointmentRepository appointmentRepo;

    private final ServiceRepository serviceRepository;

    //constructor injection so i can use AppointmentRepository on my methods
    public AppointmentService(AppointmentRepository appointmentRepo, ServiceRepository serviceRepository) {
        this.appointmentRepo = appointmentRepo;
        this.serviceRepository = serviceRepository;
    }

    //get the list of appointments of a user
    public List<Appointment> listAppointmentsByCustomerId(Long customerId) {
        return appointmentRepo.findByCustomerId(customerId);
    }

    //get the list of all appointments
    public List<Appointment> listAllAppointments(){
        return appointmentRepo.findAll();
    }

    //save appointment
    @Transactional
    public void save(Appointment appointment) {
        appointmentRepo.save(appointment);
    }

    //cancel appointment by customer and id
    @Transactional
    public void cancelByCustomerAndId(Long customerId, Long appointmentId) {

        //i need the orElseThrow otherwise it gives me compilation error, because findById returns an Optional
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(() -> new IllegalStateException("appointment.message.notFound"));

        //this is to avoid canceling appointments from another user via url, check if the appointment id belongs to the logged customer
        if (appointment.getCustomer() == null || !appointment.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("appointment.message.notYours");
        }

        //only cancel appointments in BOOKED status, no point cancelling if already cancelled or if completed
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("appointment.message.cannotCancelStatus");
        }

        //set the status to cancelled
        appointment.setStatus(AppointmentStatus.CANCELLED);

        //and save
        appointmentRepo.save(appointment);

    }

    //cancel appointment by admin and appointmentId
    @Transactional
    public void cancelByAdminAndId(Long appointmentId) {

        //i need the orElseThrow otherwise it gives me compilation error, because findById returns an Optional
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(() -> new IllegalStateException("appointment.message.notFound"));

        //only cancel appointments in BOOKED status, no point cancelling if already cancelled or if completed
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("appointment.message.cannotCancelStatus");
        }

        //set the status to cancelled
        appointment.setStatus(AppointmentStatus.CANCELLED);

        //and save
        appointmentRepo.save(appointment);

    }

    //mark appointment as completed by admin and appointmentId
    @Transactional
    public void completeByAdminAndId(Long appointmentId) {

        //i need the orElseThrow otherwise it gives me compilation error, because findById returns an Optional
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow(() -> new IllegalStateException("appointment.message.notFound"));

        //only complete appointments in BOOKED status, no point completing if already completed or if cancelled
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("appointment.message.cannotCompleteStatus");
        }

        //set the status to completed
        appointment.setStatus(AppointmentStatus.COMPLETED);

        //and save
        appointmentRepo.save(appointment);

    }

    //return the possible values for appointment status
    public AppointmentStatus[] listAppointmentStatuses() {

        //return the values from the enum
        return AppointmentStatus.values();

    }

    //list all appointments by filters
    public List<Appointment> listAllAppointmentsByFilters(LocalDate startDate, LocalDate endDate, AppointmentStatus appointmentStatus, Long serviceId, String customerNameOrEmail) {

        //adapted from https://medium.com/@AlexanderObregon/search-filters-in-spring-boot-apis-without-complex-query-builders-dcb69a0453c9
        Specification<Appointment> spec = Specification.where((root, query, cb) -> cb.conjunction());

        //filter by dates
        if (startDate != null && endDate != null) { //if both are not null, filter between

            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("date"), startDate, endDate)
            );

        } else if (startDate != null && endDate == null) { //if only start date not null, filter greaterThanOrEqualTo

            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("date"), startDate)
            );

        } else if (startDate == null && endDate != null) { //if only end date not null, filter lessThanOrEqualTo

            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("date"), endDate)
            );

        }

        //filter by status
        if (appointmentStatus != null) {

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), appointmentStatus)
            );

        }

        //filter by service
        if (serviceId != null) {

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("service").get("id"), serviceId)
            );

        }

        //filter by customer name or email
        if (customerNameOrEmail != null && !customerNameOrEmail.isBlank()) {

            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("customer").get("fullName"), "%" + customerNameOrEmail.toLowerCase() + "%"),
                            cb.like(root.get("customer").get("email"), "%" + customerNameOrEmail.toLowerCase() + "%")
                    )
            );

        }

        return appointmentRepo.findAll(spec);


    }

    //validate appointment booking times
    public void validateBooking(Appointment appointment) {

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusYears(1);
        LocalDate appointmentDate = appointment.getDate();
        LocalTime startTime = appointment.getTime();
        int durationMinutes = appointment.getService().getEstimatedTimeMinutes();
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        //appointment can't be booked for before than today
        if (appointmentDate.isBefore(today)) {
            throw new IllegalStateException("appointment.message.cantBookBeforeToday");
        }

        //appointment can't be booked for more than one year ahead
        if (appointmentDate.isAfter(maxDate)) {
            throw new IllegalStateException("appointment.message.cantBookAfterOneYear");
        }

        //check if the salon is open on the selected day
        if (!BusinessHours.OPEN_DAYS.contains(appointmentDate.getDayOfWeek())) {
            throw new IllegalStateException("appointment.message.closedDay");
        }

        //check if the appointment was booked in APPOINTMENT_INTERVAL_MINUTES (15) minute interval
        if (startTime.getMinute() % BusinessHours.APPOINTMENT_INTERVAL_MINUTES != 0) {
            throw new IllegalStateException("appointment.message.invalidInterval");
        }

        //check if the booked appointment is within working hours
        if (startTime.isBefore(BusinessHours.OPENING_TIME) || endTime.isAfter(BusinessHours.CLOSING_TIME)) {
            throw new IllegalStateException("appointment.message.outsideWorkingHours");
        }

        //check if new appointment overlaps any existing one
        if (hasOverlappingAppointment(appointmentDate, startTime, endTime)) {
            throw new IllegalStateException("appointment.message.slotTaken");
        }

    }

    //check if new appointment overlaps any existing BOOKED appointment
    private boolean hasOverlappingAppointment(LocalDate date, LocalTime newStart, LocalTime newEnd){

        //get the existing appointments booked for the date
        List<Appointment> existingAppointments = appointmentRepo.findByDateAndStatus(date, AppointmentStatus.BOOKED);

        for(Appointment existingAppointment : existingAppointments) {

            LocalTime existingStart = existingAppointment.getTime();
            LocalTime existingEnd = existingStart.plusMinutes(existingAppointment.getService().getEstimatedTimeMinutes());

            //if start time of the new appointment is before an existing end, and the end of the new appointment is after an existing start, means it overlapped
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true;
            }

        }

        return false;
    }

    //generate all available time slots for a selected service and date
    public List<String> getAvailableTimeSlots(Long serviceId, LocalDate date) {

        //create list that will be returned to the controller/API
        List<String> availableSlots = new ArrayList<>();

        //if service or date are null, return empty list
        if (serviceId == null || date == null) {
            return availableSlots;
        }

        //get today's date
        LocalDate today = LocalDate.now();

        //set the maximum date allowed (one year ahead), same rule used in the appointment form
        LocalDate maxDate = today.plusYears(1);

        //if selected date is before today, return empty list
        if (date.isBefore(today)) {
            return availableSlots;
        }

        //if selected date is after max date, return empty list
        if (date.isAfter(maxDate)) {
            return availableSlots;
        }

        //if the salon is closed on the selected date, return empty list
        if (!BusinessHours.OPEN_DAYS.contains(date.getDayOfWeek())) {
            return availableSlots;
        }

        //get the selected service from the database
        com.nci.fiuza.domain.Service selectedService = serviceRepository.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("service.message.notFound"));

        //get the service duration in minutes
        Integer durationMinutes = selectedService.getEstimatedTimeMinutes();

        //if the service duration is missing, return empty list
        if (durationMinutes == null) {
            return availableSlots;
        }

        //formatter used to return times like 08:00, 08:15, 09:30
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        //last possible start time is closing time minus selected service duration
        LocalTime lastPossibleStartTime = BusinessHours.CLOSING_TIME.minusMinutes(durationMinutes);

        //loop through available slots for the day, from the opening time, until last possible start time, step is business' appointment interval minutes
        for (LocalTime currentSlot = BusinessHours.OPENING_TIME;
             !currentSlot.isAfter(lastPossibleStartTime);
             currentSlot = currentSlot.plusMinutes(BusinessHours.APPOINTMENT_INTERVAL_MINUTES)) {

            //slot end is current slot start plus service duration
            LocalTime slotEnd = currentSlot.plusMinutes(durationMinutes);

            //if there is no overlapping appointment, adds to available slots
            if (!hasOverlappingAppointment(date, currentSlot, slotEnd)) {
                availableSlots.add(currentSlot.format(formatter));
            }

        }

        //return available slots
        return availableSlots;
    }

}
