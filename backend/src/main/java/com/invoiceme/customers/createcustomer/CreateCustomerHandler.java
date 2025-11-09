package com.invoiceme.customers.createcustomer;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.User;
import com.invoiceme.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Customer handle(CreateCustomerCommand command) {
        // Check if email already exists
        if (customerRepository.existsByEmail(command.getEmail().getValue())) {
            throw new IllegalArgumentException("Customer with email " + command.getEmail().getValue() + " already exists");
        }
        
        // Create customer using factory method
        Customer customer = Customer.create(
            command.getCompanyName(),
            command.getEmail(),
            command.getCustomerType()
        );
        
        // Set optional fields
        if (command.getContactName() != null) {
            customer.update(
                customer.getCompanyName(),
                command.getContactName(),
                command.getPhone(),
                command.getAddress(),
                customer.getCustomerType()
            );
        } else if (command.getPhone() != null || command.getAddress() != null) {
            customer.update(
                customer.getCompanyName(),
                null,
                command.getPhone(),
                command.getAddress(),
                customer.getCustomerType()
            );
        }
        
        // Save customer
        Customer savedCustomer = customerRepository.save(customer);
        
        // Auto-create user account for customer (if email provided and user doesn't exist)
        if (command.getEmail() != null && command.getEmail().getValue() != null) {
            String email = command.getEmail().getValue();
            
            // Check if user already exists with this email
            if (userRepository.findByEmail(email).isEmpty()) {
                // Determine full name: use contactName if available, otherwise companyName
                String fullName = command.getContactName() != null && !command.getContactName().trim().isEmpty()
                    ? command.getContactName()
                    : command.getCompanyName();
                
                // Create active user account with default password "test1234"
                String passwordHash = passwordEncoder.encode("test1234");
                User customerUser = User.createActive(
                    email,
                    passwordHash,
                    fullName,
                    User.UserRole.CUSTOMER,
                    savedCustomer.getId()
                );
                
                userRepository.save(customerUser);
            }
        }
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedCustomer);
        
        return savedCustomer;
    }
}

