package com.nci.fiuza.service;

//import com.nci.fiuza.domain.Service;
import com.nci.fiuza.repository.AppointmentRepository;
import com.nci.fiuza.repository.ServiceRepository;
import com.nci.fiuza.util.BusinessHours;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

//business logic class
@Service
public class ServiceService {

    //i'll need access to the UserRepository
    private final ServiceRepository serviceRepo;

    //i'll need access to the AppointmentRepository
    private final AppointmentRepository appointmentRepo;

    private final ImageStorageService imageStorageService;

    //constructor injection so i can use the repos on my methods
    public ServiceService(ServiceRepository serviceRepo, AppointmentRepository appointmentRepo, ImageStorageService imageStorageService){
        this.serviceRepo = serviceRepo;
        this.appointmentRepo = appointmentRepo;
        this.imageStorageService = imageStorageService;
    }

    //i have to use "com.nci.fiuza.domain.Service" instead of just "Service" because I'm already importing a Service class from another package (import org.springframework.stereotype.Service;)
    //so java doesnt  allow to import 2 classes with the same name... that is why the import from the domain class at the top of the code is commented
    //and here I'm specifying the whole path for my domain/entity class
    //public List<Service> listAll() {
    public List<com.nci.fiuza.domain.Service> listAll() {
        return serviceRepo.findAll();
    }

    //same as above method
    //public Service get(Long id) {
    public com.nci.fiuza.domain.Service get(Long id) {
        //find Service by id, if not found throws exception
        return serviceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("service.message.notFound"));
    }

    //same as above method
    //public void save(Service service) {
    @Transactional
    public void save(com.nci.fiuza.domain.Service service,
                     MultipartFile imageFile,
                     boolean removeImage) throws IOException {

        //validate service duration, it should be in APPOINTMENT_INTERVAL_MINUTES (15) minutes interval
        if (service.getEstimatedTimeMinutes() == null ||
                service.getEstimatedTimeMinutes() < BusinessHours.APPOINTMENT_INTERVAL_MINUTES ||
                service.getEstimatedTimeMinutes() % BusinessHours.APPOINTMENT_INTERVAL_MINUTES != 0) {
            throw new IllegalStateException("service.message.invalidDuration");
        }

        //checks if the service has an id, meaning it is an edit and not a new service
        if (service.getId() != null) {

            //loads the existing service from the database
            com.nci.fiuza.domain.Service existingService = get(service.getId());

            //if the remove image checkbox was selected
            if (removeImage) {

                //remove the image reference from the service
                service.setImageFileName(null);

            //if no new image was uploaded
            } else if (imageFile == null || imageFile.isEmpty()) {

                //keep the previous image filename
                service.setImageFileName(existingService.getImageFileName());
            }
        }

        //only save a new uploaded image if the user did not choose to remove the current image
        if (!removeImage) {

            //saves the image inside uploads/services and returns the generated filename
            String savedFileName = imageStorageService.saveImage(imageFile, "services");

            //if a file was uploaded and saved
            if (savedFileName != null) {

                //stores the generated filename in the service object
                service.setImageFileName(savedFileName);
            }
        }

        //saves the service in the database
        serviceRepo.save(service);

    }

    //delete a service
    @Transactional
    public void delete(Long id) {

        //check if service exists
        get(id);

        //before deleting checks if any appointments exist for that service
        if (appointmentRepo.existsByServiceId(id)) {
            throw new IllegalStateException("service.message.cannotDelete");
        }

        //if no appointments attached, delete
        serviceRepo.deleteById(id);
    }

    //toggle service active
    @Transactional
    public void toggleActive(Long id) {

        //same thing as above
        //Service service = get(id);
        com.nci.fiuza.domain.Service service = get(id);
        //if it's currently active, set the opposite, and vice versa
        service.setActive(!service.isActive());
        serviceRepo.save(service);

    }

    //list all active services
    public List<com.nci.fiuza.domain.Service> listActive() {
        return serviceRepo.findByActiveTrue();
    }

}
