package com.chronicorn.backend.repositories;

import com.chronicorn.backend.models.LocalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<LocalOrder, UUID> {

    // You do not need to write custom SQL here for basic operations.
    // JpaRepository automatically provides save(), findById(), and delete() methods.
}