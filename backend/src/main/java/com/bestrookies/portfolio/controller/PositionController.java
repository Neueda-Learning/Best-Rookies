package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.dto.PositionCreateRequest;
import com.bestrookies.portfolio.dto.PositionResponse;
import com.bestrookies.portfolio.dto.PositionUpdateRequest;
import com.bestrookies.portfolio.service.PositionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public PositionResponse createPosition(@Valid @RequestBody PositionCreateRequest request) {
        return positionService.createPosition(request);
    }

    @GetMapping
    public List<PositionResponse> listPositions(@RequestParam(required = false) Long portfolioId) {
        return positionService.listPositions(portfolioId);
    }

    @PatchMapping("/{id}")
    public PositionResponse updatePosition(@PathVariable Long id, @Valid @RequestBody PositionUpdateRequest request) {
        return positionService.updatePosition(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
    }
}

