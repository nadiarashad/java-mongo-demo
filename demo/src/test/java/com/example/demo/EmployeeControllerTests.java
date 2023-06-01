package com.example.demo;

import org.junit.jupiter.api.Test;

import com.example.demo.controllers.EmployeeController;
import com.example.demo.errors.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.models.Employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTests {

        @Autowired
        private MockMvc mvc;

        @MockBean
        private EmployeeController controller;

        public static String asJsonString(final Object obj) throws JsonProcessingException {

                return new ObjectMapper().writeValueAsString(obj);

        }

        @Test
        public void getAllEmployees() throws Exception {
                Employee employee = new Employee();
                employee.setId(1);
                employee.setFirstName("name");
                employee.setLastName("surname");
                employee.setEmailId("name@email.com");

                Employee employee1 = new Employee();
                employee1.setId(2);
                employee1.setFirstName("name1");
                employee1.setLastName("surname1");
                employee1.setEmailId("name1@email.com");

                List<Employee> allEmployees = new ArrayList<>();
                allEmployees.add(employee);
                allEmployees.add(employee1);

                given(controller.getAllEmployees()).willReturn(allEmployees);

                mvc.perform(MockMvcRequestBuilders.get("/api/v1/employees")).andExpectAll(
                                MockMvcResultMatchers.status().isOk(),
                                MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                                MockMvcResultMatchers.content().string(
                                                "[{\"id\":1,\"firstName\":\"name\",\"lastName\":\"surname\",\"emailId\":\"name@email.com\"},{\"id\":2,\"firstName\":\"name1\",\"lastName\":\"surname1\",\"emailId\":\"name1@email.com\"}]"));
        }

        @Test
        public void getSingleEmployee() throws Exception {
                Employee employee = new Employee();
                long l = 1;
                employee.setId(l);
                employee.setFirstName("firstName");
                employee.setLastName("surname");
                employee.setEmailId("fs@email.com");

                given(controller.getEmployeeById(l)).willReturn(ResponseEntity.ok().body(employee));

                mvc.perform(MockMvcRequestBuilders.get("/api/v1/employees/{id}", l))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(
                                                MockMvcResultMatchers.content().string(
                                                                "{\"id\":1,\"firstName\":\"firstName\",\"lastName\":\"surname\",\"emailId\":\"fs@email.com\"}"));
        }

        @Test
        public void postEmployee() throws Exception {
                Employee employee = new Employee();
                employee.setFirstName("name");
                employee.setLastName("surname");
                employee.setEmailId("ns@email.com");

                given(controller.createEmployee(employee)).willReturn(employee);

                mvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(employee)))
                                .andExpect(MockMvcResultMatchers.status().isOk());

                given(controller.getEmployeeById(employee.getId())).willReturn(ResponseEntity.ok().body(employee));
        }

        @Test
        public void amendEmployee() throws Exception {
                Employee employee = new Employee();
                employee.setId(1);
                employee.setFirstName("potato");
                employee.setLastName("surname");
                employee.setEmailId("ns@email.com");

                Employee updatedEmployee = new Employee();
                updatedEmployee.setFirstName("new-first-name");
                updatedEmployee.setLastName("surname");
                updatedEmployee.setEmailId("ns@email.com");

                given(controller.createEmployee(employee)).willReturn(employee);

                mvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(employee)))
                                .andExpect(MockMvcResultMatchers.status().isOk());

                given(controller.getEmployeeById(employee.getId())).willReturn(ResponseEntity.ok().body(employee));

                given(controller.updateEmployee(employee.getId(),
                                updatedEmployee)).willReturn(ResponseEntity.ok().body(updatedEmployee));

                mvc.perform(MockMvcRequestBuilders
                                .put("/api/v1/employees/{id}", employee.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(updatedEmployee)))
                                .andExpect(MockMvcResultMatchers.status().isOk());

                given(controller.getEmployeeById(employee.getId()))
                                .willReturn(ResponseEntity.ok().body(updatedEmployee));

        }

        @Test
        public void deleteEmployee() throws Exception {
                Employee employee = new Employee();
                employee.setId(1);
                employee.setFirstName("potato");
                employee.setLastName("surname");
                employee.setEmailId("ns@email.com");

                given(controller.createEmployee(employee)).willReturn(employee);

                mvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(employee)))
                                .andExpect(MockMvcResultMatchers.status().isOk());

                Map<String, Boolean> response = new HashMap<>();
                response.put("deleted", Boolean.TRUE);

                given(controller.deleteEmployee(employee.getId())).willReturn(response);

                mvc.perform(MockMvcRequestBuilders.delete("/api/v1/employees/{id}", employee.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(response)))
                                .andExpect(MockMvcResultMatchers.status().isOk());

        }

        @Test
        public void resourceNotFoundExceptionTest() throws Exception {

                long id = 1;

                doThrow(new ResourceNotFoundException("Employee not found for this id :: " + id)).when(controller)
                                .getEmployeeById(id);

                mvc.perform(MockMvcRequestBuilders.get("/api/v1/employees/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(MockMvcResultMatchers.status().isNotFound())
                                .andExpect(
                                                MockMvcResultMatchers.content().string(
                                                                org.hamcrest.Matchers.containsString(
                                                                                "Employee not found for this id :: 1")));

        }

}
