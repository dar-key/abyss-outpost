package com.abyss.abyss_outpost.web;

import com.abyss.abyss_outpost.facade.DiveSimulator;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DiveController {

    private final DiveSimulator sim;

    public DiveController(DiveSimulator sim) { this.sim = sim; }

    @PostMapping("/build")
    public Map<String, Object> build(@RequestBody Map<String, Object> config) {
        return sim.preview(config);
    }

    @PostMapping("/dive/start")
    public Map<String, Object> start(@RequestBody Map<String, Object> config) {
        return sim.start(config);
    }

    @PostMapping("/dive/step")
    public Map<String, Object> step(@RequestBody Map<String, String> body) {
        return sim.step(body.get("action"));
    }

    @GetMapping("/report")
    public Map<String, Object> report() {
        return sim.report();
    }
}