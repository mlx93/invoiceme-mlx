package com.invoiceme.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Address {
    
    @Column(name = "street", length = 255, columnDefinition = "varchar(255)")
    private String street;
    
    @Column(name = "city", length = 100, columnDefinition = "varchar(100)")
    private String city;
    
    @Column(name = "state", length = 50, columnDefinition = "varchar(50)")
    private String state;
    
    @Column(name = "zip_code", length = 20, columnDefinition = "varchar(20)")
    private String zipCode;
    
    @Column(name = "country", length = 100, columnDefinition = "varchar(100)")
    private String country;
    
    private Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country != null ? country : "USA";
        validate();
    }
    
    public static Address of(String street, String city, String state, String zipCode) {
        return new Address(street, city, state, zipCode, "USA");
    }
    
    public static Address of(String street, String city, String state, String zipCode, String country) {
        return new Address(street, city, state, zipCode, country);
    }
    
    public static AddressBuilder builder() {
        return new AddressBuilder();
    }
    
    private void validate() {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code is required");
        }
    }
    
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(street);
        if (city != null) {
            sb.append("\n").append(city);
            if (state != null) {
                sb.append(", ").append(state);
            }
            if (zipCode != null) {
                sb.append(" ").append(zipCode);
            }
        }
        if (country != null && !country.equals("USA")) {
            sb.append("\n").append(country);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toFormattedString();
    }
    
    public static class AddressBuilder {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country = "USA";
        
        public AddressBuilder street(String street) {
            this.street = street;
            return this;
        }
        
        public AddressBuilder city(String city) {
            this.city = city;
            return this;
        }
        
        public AddressBuilder state(String state) {
            this.state = state;
            return this;
        }
        
        public AddressBuilder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }
        
        public AddressBuilder country(String country) {
            this.country = country;
            return this;
        }
        
        public Address build() {
            return new Address(street, city, state, zipCode, country);
        }
    }
}

