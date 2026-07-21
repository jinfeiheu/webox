package com.webox.admin;

import com.webox.admin.dto.AdminDishView;
import com.webox.admin.dto.DishFormRequest;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.common.money.Moneys;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import com.webox.menu.DishSpecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DishAdminService {

    private static final String IMAGE_URL_PREFIX = "/images/dishes/";

    private final DishRepository dishRepository;
    private final Path uploadDir;

    public DishAdminService(DishRepository dishRepository,
                            @Value("${app.upload.dir}") String uploadDir) {
        this.dishRepository = dishRepository;
        this.uploadDir = Path.of(uploadDir, "images", "dishes");
    }

    @Transactional(readOnly = true)
    public Page<AdminDishView> list(String q, Category category, Pageable pageable) {
        return dishRepository.findAll(DishSpecs.adminSearch(q, category), pageable)
                .map(AdminDishView::of);
    }

    @Transactional
    @CacheEvict(cacheNames = "menuItems", allEntries = true)
    public AdminDishView create(DishFormRequest request) {
        Dish dish = new Dish();
        applyForm(dish, request);
        dish.setActive(true);
        return AdminDishView.of(dishRepository.save(dish));
    }

    @Transactional
    @CacheEvict(cacheNames = {"menuItems", "dishDetail"}, allEntries = true)
    public AdminDishView update(Long id, DishFormRequest request) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Dish not found."));
        applyForm(dish, request);
        return AdminDishView.of(dishRepository.save(dish));
    }

    @Transactional
    @CacheEvict(cacheNames = {"menuItems", "dishDetail"}, allEntries = true)
    public AdminDishView setActive(Long id, boolean active) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Dish not found."));
        dish.setActive(active);
        return AdminDishView.of(dishRepository.save(dish));
    }

    @Transactional
    @CacheEvict(cacheNames = {"menuItems", "dishDetail"}, allEntries = true)
    public String uploadImage(Long id, MultipartFile file) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Dish not found."));
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "Please choose an image file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "Only image files are accepted.");
        }
        String ext = ".jpg";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        String filename = "dish-" + id + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(filename), file.getBytes());
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to save the image.");
        }
        String url = IMAGE_URL_PREFIX + filename;
        dish.setImageUrl(url);
        dishRepository.save(dish);
        return url;
    }

    private void applyForm(Dish dish, DishFormRequest request) {
        dish.setName(request.name().trim());
        dish.setDescription(request.description().trim());
        dish.setPrice(Moneys.of(request.price()));
        dish.setCategory(request.category());
        dish.setProtein(request.protein().trim());
        dish.setSpiceLevel(request.spiceLevel());
        List<Allergen> allergens = request.allergens() == null ? List.of() : request.allergens();
        dish.setAllergens(allergens);
        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            dish.setImageUrl(request.imageUrl());
        } else if (dish.getImageUrl() == null) {
            dish.setImageUrl(IMAGE_URL_PREFIX + "dish-01.jpg"); // fallback until an image is uploaded
        }
    }
}
