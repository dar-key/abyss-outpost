package com.abyss.abyss_outpost.web;

import com.abyss.abyss_outpost.dto.DiveState;
import com.abyss.abyss_outpost.dto.DockConfig;
import com.abyss.abyss_outpost.dto.Report;
import com.abyss.abyss_outpost.dto.StepRequest;
import com.abyss.abyss_outpost.dto.SubSpec;
import com.abyss.abyss_outpost.facade.DiveSimulator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.abyss.abyss_outpost.dto.RouteRequest;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DiveController {

    private final DiveSimulator simulator;

    public DiveController(DiveSimulator simulator) {
        this.simulator = simulator;
    }

    @GetMapping("/ping")
    public String ping() {
        return "online";
    }

    @PostMapping("/build")
    public SubSpec build(@RequestBody DockConfig config) {
        return simulator.preview(config);
    }

    @PostMapping("/dive/start")
    public DiveState start(@RequestBody DockConfig config) {
        return simulator.start(config);
    }

    @PostMapping("/dive/step")
    public DiveState step(@RequestBody StepRequest request) {
        return simulator.step(request);
    }

        @PostMapping("/dive/route")
    public DiveState route(@RequestBody RouteRequest request) {
        return simulator.chooseRoute(request);
    }

    @GetMapping("/report")
    public Report report() {
        return simulator.report();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}