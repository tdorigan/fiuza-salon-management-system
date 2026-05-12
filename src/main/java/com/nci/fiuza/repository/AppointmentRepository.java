package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    //this is like doing a select * from appointment where service_id = XXXX
    boolean existsByServiceId(Long serviceId);

    //this is like doing a select * from appointment where customer_id = XXXX
    boolean existsByCustomerId(Long customerId);

    //this is like doing a select * from appointments where customer = XXXX
    List<Appointment> findByCustomerId(Long customerId);

    //this is like doing a select * from appointments where date = XXXX and status = XXXX
    List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status);

}
