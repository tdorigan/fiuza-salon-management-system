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

    //saves a service
    //before saving the service, this method handles service image upload/removal in Cloudflare R2
    @Transactional
    public void save(com.nci.fiuza.domain.Service service,
                     MultipartFile imageFile,
                     boolean removeImage) throws IOException {

        //validates service duration, it must be at least 15 minutes and follow the appointment interval
        if (service.getEstimatedTimeMinutes() == null ||
                service.getEstimatedTimeMinutes() < BusinessHours.APPOINTMENT_INTERVAL_MINUTES ||
                service.getEstimatedTimeMinutes() % BusinessHours.APPOINTMENT_INTERVAL_MINUTES != 0) {

            //throws translated validation message key
            throw new IllegalStateException("service.message.invalidDuration");
        }

        //checks if this service is being edited, because existing services already have an id
        boolean isEdit = service.getId() != null;

        //creates a variable to store the existing service from the database when editing
        com.nci.fiuza.domain.Service existingService = null;

        //if this is an edit operation
        if (isEdit) {

            //loads the current service from the database
            existingService = get(service.getId());
        }

        //if user marked the checkbox to remove the current image
        if (removeImage) {

            //if this is an existing service and it currently has an image
            if (isEdit && existingService.getImageFileName() != null) {

                //deletes the current image from Cloudflare R2
                imageStorageService.deleteImageFromR2(existingService.getImageFileName());
            }

            //removes the image reference from the service object
            service.setImageFileName(null);

        //if user did not mark the remove image checkbox
        } else {

            //uploads the new image to Cloudflare R2, if a new file was selected
            String savedImageKey = imageStorageService.saveImageToR2(imageFile, "services");

            //if a new image was uploaded
            if (savedImageKey != null) {

                //if this is an edit and the service already had an old image
                if (isEdit && existingService.getImageFileName() != null) {

                    //deletes the old image from Cloudflare R2 because it is being replaced
                    imageStorageService.deleteImageFromR2(existingService.getImageFileName());
                }

                //stores the new image key in the service object
                service.setImageFileName(savedImageKey);

            //if no new image was uploaded and this is an edit
            } else if (isEdit) {

                //keeps the existing image reference from the database
                service.setImageFileName(existingService.getImageFileName());
            }
        }

        //saves the service into the database
        serviceRepo.save(service);
    }

    //delete a service
    @Transactional
    public void delete(Long id) {

        //loads the service from the database, or throws an error if it does not exist
        com.nci.fiuza.domain.Service service = get(id);

        //before deleting, checks if any appointments exist for that service
        if (appointmentRepo.existsByServiceId(id)) {

            //if the service is already used in appointments, do not delete the service or its image
            throw new IllegalStateException("service.message.cannotDelete");
        }

        //deletes the service image from Cloudflare R2, if the service has an image
        imageStorageService.deleteImageFromR2(service.getImageFileName());

        //if no appointments are attached, deletes the service from the database
        serviceRepo.delete(service);

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

    //builds the service image URL only if the image physically exists in Cloudflare R2
    public String buildServiceImageUrl(com.nci.fiuza.domain.Service service) {

        //if service is null, there is no image URL
        if (service == null) {
            return null;
        }

        //checks if the service image exists in Cloudflare R2
        boolean imageExists = imageStorageService.r2ImageExists(service.getImageFileName());

        //if the image does not exist, return null so the template shows the placeholder
        if (!imageExists) {
            return null;
        }

        //if the image exists, return the public Cloudflare R2 image URL
        return imageStorageService.buildR2ImageUrl(service.getImageFileName());
    }

}
