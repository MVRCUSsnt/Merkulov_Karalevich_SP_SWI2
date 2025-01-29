package com.example.demo.repositories;

import com.example.demo.Room;
import com.example.demo.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByUsersContaining(Users user, Pageable pageable);

}
