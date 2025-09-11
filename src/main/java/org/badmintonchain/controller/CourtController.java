package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.service.CourtService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CourtController {
    @Autowired
    private CourtService courtService;

    // --- USER API ---
    @GetMapping("/api/courts")
    public ResponseEntity<ApiResponse<PageResponse<CourtDTO>>> getAllCourts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        PageResponse<CourtDTO> courts = courtService.getAllActiveCourts(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>("Get active courts successfully", HttpStatus.OK.value(), courts, request.getRequestURI())
        );
    }


    @GetMapping("/api/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> getCourtById(@PathVariable Long id, HttpServletRequest request) {
        CourtDTO court = courtService.getCourtIfAvailable(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Get court successfully", HttpStatus.OK.value(), court, request.getRequestURI())
        );
    }

    // --- ADMIN API ---
    @GetMapping("/api/admin/courts")
    public ResponseEntity<ApiResponse<PageResponse<CourtDTO>>> getAllAdminCourts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        PageResponse<CourtDTO> courts = courtService.getAllCourts(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>("Get all courts successfully", HttpStatus.OK.value(), courts, request.getRequestURI())
        );
    }

    @GetMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> getAdminCourtById(@PathVariable Long id, HttpServletRequest request) {
        CourtDTO court = courtService.getCourtById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Get court successfully", HttpStatus.OK.value(), court, request.getRequestURI())
        );
    }

    @PostMapping("/api/admin/courts")
    public ResponseEntity<ApiResponse<CourtDTO>> createCourt(@RequestBody CourtDTO dto, HttpServletRequest request) {
        CourtDTO created = courtService.createCourt(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Court created successfully", HttpStatus.CREATED.value(), created, request.getRequestURI()));
    }

    @PatchMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> updateCourt(@PathVariable Long id,
                                                             @RequestBody CourtDTO dto,
                                                             HttpServletRequest request) {
        CourtDTO updated = courtService.updateCourt(id, dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Court updated successfully", HttpStatus.OK.value(), updated, request.getRequestURI())
        );
    }

    @DeleteMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(@PathVariable Long id, HttpServletRequest request) {
        courtService.deleteCourtById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Court deleted successfully", HttpStatus.NO_CONTENT.value(), null, request.getRequestURI())
        );
    }
}
