package com.invoiceme.integration;

import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.common.Email;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomerCrudIntegrationTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Test
    void testCreateCustomer() {
        // Create customer
        Customer customer = Customer.create(
            "Acme Corporation",
            Email.of("contact@acme.com"),
            CustomerType.COMMERCIAL
        );
        
        customer = customerRepository.save(customer);
        
        // Verify
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getCompanyName()).isEqualTo("Acme Corporation");
        assertThat(customer.getEmail().getValue()).isEqualTo("contact@acme.com");
        assertThat(customer.getCustomerType()).isEqualTo(CustomerType.COMMERCIAL);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(customer.getCreditBalance().isZero()).isTrue();
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testGetCustomerById() {
        // Create and save customer
        Customer customer = Customer.create(
            "Test Company",
            Email.of("test@company.com"),
            CustomerType.RESIDENTIAL
        );
        customer = customerRepository.save(customer);
        UUID customerId = customer.getId();
        
        // Retrieve by ID
        Optional<Customer> retrieved = customerRepository.findById(customerId);
        
        // Verify
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(customerId);
        assertThat(retrieved.get().getCompanyName()).isEqualTo("Test Company");
        assertThat(retrieved.get().getEmail().getValue()).isEqualTo("test@company.com");
    }
    
    @Test
    void testUpdateCustomer() {
        // Create customer
        Customer customer = Customer.create(
            "Original Name",
            Email.of("original@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
        
        // Update customer details
        Address newAddress = Address.of(
            "123 Main St",
            "San Francisco",
            "CA",
            "94102",
            "USA"
        );
        
        customer.update(
            "Updated Company Name",
            "John Doe",
            "555-1234",
            newAddress,
            CustomerType.RESIDENTIAL
        );
        
        customer = customerRepository.save(customer);
        
        // Verify updates
        assertThat(customer.getCompanyName()).isEqualTo("Updated Company Name");
        assertThat(customer.getContactName()).isEqualTo("John Doe");
        assertThat(customer.getPhone()).isEqualTo("555-1234");
        assertThat(customer.getAddress()).isNotNull();
        assertThat(customer.getAddress().getStreet()).isEqualTo("123 Main St");
        assertThat(customer.getAddress().getCity()).isEqualTo("San Francisco");
        assertThat(customer.getCustomerType()).isEqualTo(CustomerType.RESIDENTIAL);
    }
    
    @Test
    void testDeleteCustomer() {
        // Create customer
        Customer customer = Customer.create(
            "To Be Deleted",
            Email.of("delete@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
        UUID customerId = customer.getId();
        
        // Soft delete (mark as inactive)
        customer.markAsInactive();
        customer = customerRepository.save(customer);
        
        // Verify status changed
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        
        // Verify still retrievable but marked inactive
        Optional<Customer> retrieved = customerRepository.findById(customerId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getStatus()).isEqualTo(CustomerStatus.INACTIVE);
    }
    
    @Test
    void testListAllCustomers() {
        // Create multiple customers
        Customer customer1 = Customer.create(
            "Company A",
            Email.of("a@company.com"),
            CustomerType.COMMERCIAL
        );
        Customer customer2 = Customer.create(
            "Company B",
            Email.of("b@company.com"),
            CustomerType.RESIDENTIAL
        );
        Customer customer3 = Customer.create(
            "Company C",
            Email.of("c@company.com"),
            CustomerType.COMMERCIAL
        );
        
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
        
        // List all customers (paginated)
        Page<Customer> page = customerRepository.findAll(PageRequest.of(0, 10));
        
        // Verify
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(page.getContent()).extracting("companyName")
            .contains("Company A", "Company B", "Company C");
    }
    
    @Test
    void testListActiveCustomersOnly() {
        // Create customers with different statuses
        Customer activeCustomer = Customer.create(
            "Active Company",
            Email.of("active@test.com"),
            CustomerType.COMMERCIAL
        );
        activeCustomer = customerRepository.save(activeCustomer);
        
        Customer inactiveCustomer = Customer.create(
            "Inactive Company",
            Email.of("inactive@test.com"),
            CustomerType.COMMERCIAL
        );
        inactiveCustomer.markAsInactive();
        inactiveCustomer = customerRepository.save(inactiveCustomer);
        
        // List only active customers
        Page<Customer> activePage = customerRepository.findByStatus(
            CustomerStatus.ACTIVE,
            PageRequest.of(0, 10)
        );
        
        // Verify
        assertThat(activePage.getContent()).extracting("companyName")
            .contains("Active Company")
            .doesNotContain("Inactive Company");
    }
    
    @Test
    void testReactivateCustomer() {
        // Create and deactivate customer
        Customer customer = Customer.create(
            "Reactivate Test",
            Email.of("reactivate@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
        customer.markAsInactive();
        customer = customerRepository.save(customer);
        
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        
        // Reactivate
        customer.markAsActive();
        customer = customerRepository.save(customer);
        
        // Verify
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }
}

