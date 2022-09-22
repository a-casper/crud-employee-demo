package com.example.employee;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id).orElseThrow();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }

    @PatchMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Map<String, String> body) {
        //get the employee from the database
        Employee employee = employeeRepository.findById(id).orElseThrow();

        //iterate over my map, and update fields as necessary
        body.forEach((k, v) -> {
            if(k.equals("name")) {
                employee.setName(v);
            } else if (k.equals("startDate")) {
                employee.setStartDate(LocalDate.parse(v));
            }
        });

        //save the employee again
        return employeeRepository.save(employee);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String employeeNotFound() {
        return "Unable to find Employee with that ID";
    }


}
