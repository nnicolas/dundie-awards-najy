package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.service.ActivityService;
import com.ninjaone.dundie_awards.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class IndexController {

    private final EmployeeService employeeService;

    private final ActivityService activityService;

    public IndexController(EmployeeService employeeService, ActivityService activityService) {
        this.employeeService = employeeService;
        this.activityService = activityService;
    }

    @GetMapping()
    public String getIndex(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("activities", activityService.getAllActivities());
        return "index";
    }
}
