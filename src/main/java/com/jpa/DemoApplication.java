package com.jpa;

import com.jpa.entity.ProductEntity;
import com.jpa.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    final ProductRepository productRepository;

    @Override
    public void run(String... args) {
//        INSERT INTO `product` (`ID`, `QTY`, `NAME`, `DESCRIPTION`, `OWNER`, `CATEGORY`) VALUES
//                (1, 1, 'Personalized 1', 'This is a metal 1', 'John Brown 1', 'jewellery'),
//                (2, 2, 'Personalized 2', 'This is a metal 2', 'John Brown 2', 'jewellery'),
//        (3, 3, 'Personalized 3', 'This is a metal 3', 'John Brown 3', 'jewellery'),
//        (4, 4, 'Personalized 4', 'This is a metal 4', 'John Brown 4', 'jewellery'),
//        (5, 5, 'Personalized 5', 'This is a metal 5', 'John Brown 5', 'jewellery'),
//        (6, 6, 'Personalized 6', 'This is a metal 6', 'John Brown 6', 'jewellery');
        var data = new ArrayList<ProductEntity>();
        for (int i = 1; i <= 6; i++) {
            data.add(new ProductEntity()
                    .setQty(i)
                    .setName("Personalized " + i)
                    .setDescription("This is a metal " + i)
                    .setOwner("John Brown " + i)
                    .setCategory("jewellery"));
        }
        productRepository.saveAll(data);
    }

}
