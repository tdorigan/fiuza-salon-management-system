package com.nci.fiuza.service;

import com.nci.fiuza.domain.Product;
import com.nci.fiuza.repository.OrderItemRepository;
import com.nci.fiuza.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

//business logic class
@Service
public class ProductService {

    //i'll need access to the ProductRepository
    private final ProductRepository productRepo;

    //i'll need access to the OrderItemRepository
    private final OrderItemRepository orderItemRepo;

    private final ImageStorageService imageStorageService;

    //constructor injection so i can use the repos on my methods
    public ProductService(ProductRepository productRepo, OrderItemRepository orderItemRepo, ImageStorageService imageStorageService) {
        this.productRepo = productRepo;
        this.orderItemRepo = orderItemRepo;
        this.imageStorageService = imageStorageService;
    }

    //return all products
    public List<Product> listAll() {
        return productRepo.findAll();
    }

    //return all active products
    public List<Product> listActive() {
        return productRepo.findByActiveTrue();
    }

    //return a product by id
    public Product get(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("product.message.notFound"));
    }

    //save a product
    //before saving the product i need to handle the images, save them on the folder and save just the file name on the db
    @Transactional
    public void save(Product product, MultipartFile imageFile, boolean removeImage) throws IOException {

        //if the product already has an id, this is an edit, not a new product
        if (product.getId() != null) {

            //loads the current product from the db
            Product existingProduct = get(product.getId());

            //if user marked checkbox to remove current image
            if (removeImage) {

                //set image file name as null
                product.setImageFileName(null);

            //if user did not choose a new image during edit
            } else if (imageFile == null || imageFile.isEmpty()) {

                //keep the old image filename instead of replacing it with null
                product.setImageFileName(existingProduct.getImageFileName());

            }

        }

        //only saves the uploaded image if the checkbox remove current image is not checked
        if (!removeImage) {

            //saves the uploaded image in uploads/products and returns the generated filename
            String savedFileName = imageStorageService.saveImage(imageFile, "products");

            //if an image was actually uploaded
            if (savedFileName != null) {
                //stores the generated filename in the product object
                product.setImageFileName(savedFileName);
            }

        }

        //save product into the db, including the image filename
        productRepo.save(product);

    }

    //toggle active
    @Transactional
    public void toggleActive(Long id) {

        Product product = this.get(id);

        //if it's currently active, set the opposite, and vice versa
        product.setActive(!product.isActive());

        //save
        productRepo.save(product);

    }

    //delete a product
    @Transactional
    public void delete(Long id) {

        //check if product exists
        get(id);

        //before deleting checks if any order items exist for that product
        if (orderItemRepo.existsByProductId(id)) {
            throw new IllegalStateException("product.message.cannotDelete");
        }

        //if no order items attached, delete
        productRepo.deleteById(id);

    }


}
