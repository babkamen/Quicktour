package com.quicktour.repository;


import com.quicktour.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    public Photo findByUrl(String photoUrl);

}
