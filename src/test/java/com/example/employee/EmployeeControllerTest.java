package com.example.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EmployeeRepository repository;

    Employee employee1;
    Employee employee2;

    @BeforeEach
    void setup() {
        employee1 = new Employee();
        employee2 = new Employee();
        employee1.setName("Anthony");
        employee1.setStartDate(LocalDate.of(2022, 9, 12));
        employee2.setName("Nik");
        employee2.setStartDate(LocalDate.of(2022, 9, 10));
    }

    @Test
    @Transactional
    @Rollback
    public void getAllEmployeesReturnsAListOfEmployees() throws Exception{
        MockHttpServletRequestBuilder request = get("/employees");

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        this.repository.save(employee1);
        this.repository.save(employee2);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name" ).value("Anthony"))
                .andExpect(jsonPath("$[1].name").value("Nik"))
                .andExpect(jsonPath("$[1].startDate").value("2022-09-10"));
    }

    @Test
    @Transactional
    @Rollback
    public void canGetEmployeeById() throws Exception{
        this.repository.save(employee1);
        MockHttpServletRequestBuilder request = get("/employees/" + employee1.getId());

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anthony"))
                .andExpect(jsonPath("$.startDate").value("2022-09-12"));

        request = get("/employees/-1");
        this.mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Unable to find Employee with that ID"));
    }

    @Test
    @Transactional
    @Rollback
    public void canCreateANewEmployee() throws Exception {
        String json = """
                {
                  "name": "Chris",
                  "startDate": "2021-07-15"
                }
                """;

        MockHttpServletRequestBuilder request = post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        this.mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Chris"))
                .andExpect(jsonPath("$.startDate").value("2021-07-15"));

        this.mvc.perform(get("/employees"))
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @Transactional
    @Rollback
    public void canDeleteEmployeeFromDb() throws Exception {
        this.repository.save(employee1);
        this.repository.save(employee2);

        MockHttpServletRequestBuilder request = delete("/employees/" + employee1.getId());
        this.mvc.perform(request)
                .andExpect(status().isNoContent());

        assertEquals(1, this.repository.count());
    }

    @Test
    @Transactional
    @Rollback
    public void canDoFullUpdates() throws Exception {
        this.repository.save(employee1);
        this.repository.save(employee2);

        String json = """
                {
                  "name": "Chris",
                  "startDate": "2021-07-15"
                }
                """;


        MockHttpServletRequestBuilder request = patch("/employees/" + employee1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chris"))
                .andExpect(jsonPath("$.startDate").value("2021-07-15"));

        this.mvc.perform(get("/employees/" + employee1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chris"));
    }


}
