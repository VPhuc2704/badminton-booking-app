package org.badmintonchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.AvailabilitySlotDTO;
import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.model.dto.PageResponse;
import org.badmintonchain.service.CourtService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class CourtController {
    @Autowired
    private CourtService courtService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    // --- USER API ---
    @GetMapping("/api/courts")
    public ResponseEntity<ApiResponse<PageResponse<CourtDTO>>> getAllCourts(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            HttpServletRequest request) {
        PageResponse<CourtDTO> courts = courtService.getAllActiveCourts(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get active courts successfully",
                        HttpStatus.OK.value(),
                        courts,
                        request.getRequestURI()
                )
        );
    }


    @GetMapping("/api/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> getCourtById(@PathVariable Long id,
                                                              HttpServletRequest request) {
        CourtDTO court = courtService.getCourtIfAvailable(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get court successfully",
                        HttpStatus.OK.value(),
                        court,
                        request.getRequestURI()
                )
        );
    }

    // --- ADMIN API ---
    @GetMapping("/api/admin/courts")
    public ResponseEntity<ApiResponse<PageResponse<CourtDTO>>> getAllAdminCourts(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size,
                                                                                 HttpServletRequest request) {
        PageResponse<CourtDTO> courts = courtService.getAllCourts(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get all courts successfully",
                        HttpStatus.OK.value(),
                        courts,
                        request.getRequestURI()
                )
        );
    }

    @GetMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> getAdminCourtById(@PathVariable Long id,
                                                                   HttpServletRequest request) {
        CourtDTO court = courtService.getCourtById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Get court successfully",
                        HttpStatus.OK.value(),
                        court,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/api/admin/courts")
    public ResponseEntity<ApiResponse<CourtDTO>> createCourt(@RequestPart("courtDTO") String courtJson,
                                                             @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                                             HttpServletRequest request) {
        try {
            CourtDTO courtDTO = objectMapper.readValue(courtJson, CourtDTO.class);

            String imagePath = saveCourtImage(imageFile);
            if (courtDTO.getImages() == null) {
                courtDTO.setImages(new ArrayList<>());
            }
            if (imagePath != null) {
                courtDTO.getImages().add(imagePath);
            }

            CourtDTO created = courtService.createCourt(courtDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(
                            new ApiResponse<>(
                                    "Court created successfully",
                                    HttpStatus.CREATED.value(),
                                    created,
                                    request.getRequestURI()
                            )
                    );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ApiResponse<>(
                                    "Error processing image: " + e.getMessage(),
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    null,
                                    request.getRequestURI()
                            )
                    );
        }
    }

    @PatchMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<CourtDTO>> updateCourt(@PathVariable Long id,
                                                             @RequestPart(value = "courtDTO", required = false) String courtJson,
                                                             @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                                             HttpServletRequest request) {

        try {
            CourtDTO courtDTO = null;
            if(courtJson != null) {
                courtDTO = objectMapper.readValue(courtJson, CourtDTO.class);
            }

            if (courtDTO == null) {
                courtDTO = new CourtDTO();
            }
            if (courtDTO.getImages() == null) {
                courtDTO.setImages(new ArrayList<>());
            }

            String imagePath = saveCourtImage(imageFile);
            if(imagePath != null && courtDTO != null) {
                courtDTO.getImages().add(imagePath);
            }

            CourtDTO updated = courtService.updateCourt(id, courtDTO);
            return ResponseEntity.ok(
                    new ApiResponse<>(
                            "Court updated successfully",
                            HttpStatus.OK.value(),
                            updated,
                            request.getRequestURI()
                    )
            );

        }catch  (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            "Error processing image: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            null,
                            request.getRequestURI()
                    )
                    );
        }

    }

    @DeleteMapping("/api/admin/courts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(@PathVariable Long id,
                                                         HttpServletRequest request) {
        courtService.deleteCourtById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Court deleted successfully",
                        HttpStatus.NO_CONTENT.value(),
                        null,
                        request.getRequestURI())
        );
    }



    private String saveCourtImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String uploadPath = System.getProperty("user.dir") + "/uploads/courtImg/";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String newFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, newFileName);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/court/img/" + newFileName;
    }

    @GetMapping("/api/courts/{courtId}/availability")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long courtId,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                     @RequestParam LocalTime startTime,
                                                     @RequestParam LocalTime endTime) {
        boolean available = courtService.isCourtAvailable(courtId, date, startTime, endTime);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/api/courts/{courtId}/availabilitySlots")
    public ResponseEntity<List<AvailabilitySlotDTO>> getAvailabilitySlots(@PathVariable Long courtId,
                                                                          @RequestParam String date) {
        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        List<AvailabilitySlotDTO> slots = courtService.getAvailableSlots(courtId, targetDate);
        return ResponseEntity.ok(slots);
    }
}
