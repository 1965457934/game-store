package com.gamestore.controller;

import com.gamestore.service.DashboardService;
import com.gamestore.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/stats")
    public Result<?> getStats() {
        Map<String, Object> stats = dashboardService.getStats();
        return Result.success(stats);
    }
}
