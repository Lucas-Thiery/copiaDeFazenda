package com.ufs.gestaofazenda.Controller;

import com.ufs.gestaofazenda.DTO.DashboardDTO;
import com.ufs.gestaofazenda.Service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO.DashboardResponse> consultar(
            @RequestParam LocalDate dtaInicio, @RequestParam LocalDate dtaFim) {
        return ResponseEntity.ok(dashboardService.consultar(dtaInicio, dtaFim));
    }
}
