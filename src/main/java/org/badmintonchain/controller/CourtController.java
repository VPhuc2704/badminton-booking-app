package org.badmintonchain.controller;


import org.badmintonchain.model.dto.CourtDTO;
import org.badmintonchain.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CourtController {
    @Autowired
    private CourtService courtService;

    @GetMapping("/api/courts")
    public ResponseEntity<List<CourtDTO>> getAllCourts() {
        return ResponseEntity.ok(courtService.getAllActiveCourts());
    }

    @GetMapping("/api/courts/{id}")
    public ResponseEntity<CourtDTO> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @GetMapping("/api/admin/courts")
    public ResponseEntity<List<CourtDTO>> getAllAdminCourts() {
        return ResponseEntity.ok(courtService.getAllCourts());
    }

    @PostMapping("/api/admin/courts")
    public ResponseEntity<CourtDTO> createCourt(@RequestBody CourtDTO dto) {
        return ResponseEntity.ok(courtService.createCourt(dto));
    }

    @PatchMapping("/api/admin/courts/{id}")
    public ResponseEntity<CourtDTO> updateCourt(@PathVariable Long id,
                                                @RequestBody CourtDTO dto) {
        return ResponseEntity.ok(courtService.updateCourt(id, dto));
    }

    @DeleteMapping("/api/admin/courts/{id}")
    public ResponseEntity<Void> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourtById(id);
        return ResponseEntity.noContent().build();
    }
}
