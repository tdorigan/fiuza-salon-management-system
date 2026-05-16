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
    //before saving the product, this method handles product image upload/removal in Cloudflare R2
    @Transactional
    public void save(Product product, MultipartFile imageFile, boolean removeImage) throws IOException {

        //checks if this product is being edited, because existing products already have an id
        boolean isEdit = product.getId() != null;

        //creates a variable to hold the existing product from the database when editing
        Product existingProduct = null;

        //if this is an edit operation
        if (isEdit) {

            //loads the current product from the database
            existingProduct = get(product.getId());
        }

        //if user marked the checkbox to remove the current image
        if (removeImage) {

            //if this is an existing product and it currently has an image
            if (isEdit && existingProduct.getImageFileName() != null) {

                //deletes the current image from Cloudflare R2
                imageStorageService.deleteImageFromR2(existingProduct.getImageFileName());
            }

            //removes the image reference from the product object
            product.setImageFileName(null);

        //if user did not mark the remove image checkbox
        } else {

            //uploads the new image to Cloudflare R2, if a new file was selected
            String savedImageKey = imageStorageService.saveImageToR2(imageFile, "products");

            //if a new image was uploaded
            if (savedImageKey != null) {

                //if this is an edit and the product already had an old image
                if (isEdit && existingProduct.getImageFileName() != null) {

                    //deletes the old image from Cloudflare R2 because it is being replaced
                    imageStorageService.deleteImageFromR2(existingProduct.getImageFileName());
                }

                //stores the new image key in the product object
                product.setImageFileName(savedImageKey);

                //if no new image was uploaded and this is an edit
            } else if (isEdit) {

                //keeps the existing image reference from the database
                product.setImageFileName(existingProduct.getImageFileName());
            }
        }

        //saves the product into the database
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

        //loads the product from the database, or throws an error if it does not exist
        Product product = get(id);

        //before deleting, checks if any order items exist for that product
        if (orderItemRepo.existsByProductId(id)) {

            //if the product is already used in orders, do not delete the product or its image
            throw new IllegalStateException("product.message.cannotDelete");
        }

        //deletes the product image from Cloudflare R2, if the product has an image
        imageStorageService.deleteImageFromR2(product.getImageFileName());

        //if no order items are attached, deletes the product from the database
        productRepo.delete(product);

    }

    //builds the product image URL only if the image physically exists in Cloudflare R2
    public String buildProductImageUrl(Product product) {

        //if product is null, there is no image URL
        if (product == null) {
            return null;
        }

        //checks if the product image exists in Cloudflare R2
        boolean imageExists = imageStorageService.r2ImageExists(product.getImageFileName());

        if (!imageExists) {
            return null;
        }

        //if the image exists, return the public Cloudflare R2 image URL
        return imageStorageService.buildR2ImageUrl(product.getImageFileName());

    }


}
