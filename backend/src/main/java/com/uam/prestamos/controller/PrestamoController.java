package com.uam.prestamos.controller;

import com.uam.prestamos.dto.PrestamoRequest;
import com.uam.prestamos.dto.PrestamoResponse;
import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponse>> listar(
            @RequestParam(required = false) EstadoPrestamo estado) {
        return ResponseEntity.ok(prestamoService.listar(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse response = prestamoService.registrar(request);
        return ResponseEntity.created(URI.create("/api/prestamos/" + response.id())).body(response);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<PrestamoResponse> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.devolver(id));
    }
}
