package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.CustomerDTO;
import org.badmintonchain.model.entity.CustomerEntity;

public class CustomerMapper {
    public static CustomerDTO toCustomerDTO(CustomerEntity customerEntity) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCustomerId(customerEntity.getId());
        customerDTO.setNumberPhone(customerEntity.getNumberPhone());
        customerDTO.setEmail(customerEntity.getUsers().getEmail());
        customerDTO.setFullname(customerEntity.getUsers().getFullName());
        return  customerDTO;
    }

    public static CustomerEntity toCustomerEntity(CustomerDTO customerDTO) {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(customerDTO.getCustomerId());
        customerEntity.setNumberPhone(customerDTO.getNumberPhone());
        return  customerEntity;
    }
}
