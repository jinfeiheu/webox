package com.webox.admin;

import com.webox.admin.dto.AdminDishView;
import com.webox.admin.dto.DishFormRequest;
import com.webox.auth.RequireRole;
import com.webox.common.enums.Category;
import com.webox.common.enums.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** Console dish management (PRD §4.3) — ADMIN only. */
@RestController
@RequestMapping("/api/admin/dishes")
@RequireRole(Role.ADMIN)
public class DishAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final DishAdminService dishAdminService;

    public DishAdminController(DishAdminService dishAdminService) {
        this.dishAdminService = dishAdminService;
    }

    @GetMapping
    public Page<AdminDishView> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Category cat = (category == null || category.isBlank()) ? null : Category.from(category);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(MAX_PAGE_SIZE, Math.max(1, size)));
        return dishAdminService.list(q, cat, pageable);
    }

    @PostMapping
    public AdminDishView create(@Valid @RequestBody DishFormRequest request) {
        return dishAdminService.create(request);
    }

    @PutMapping("/{id}")
    public AdminDishView update(@PathVariable("id") long id, @Valid @RequestBody DishFormRequest request) {
        return dishAdminService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public AdminDishView setStatus(@PathVariable("id") long id, @RequestBody Map<String, Boolean> body) {
        Boolean active = body.get("active");
        if (active == null) {
            throw new com.webox.common.api.BizException(com.webox.common.api.ErrorCode.VALIDATION_ERROR,
                    "Field 'active' is required.");
        }
        return dishAdminService.setActive(id, active);
    }

    @PostMapping("/{id}/image")
    public Map<String, String> uploadImage(@PathVariable("id") long id,
                                           @RequestParam("file") MultipartFile file) {
        return Map.of("imageUrl", dishAdminService.uploadImage(id, file));
    }
}
