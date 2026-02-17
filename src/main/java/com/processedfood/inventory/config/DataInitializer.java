package com.processedfood.inventory.config;

import com.processedfood.inventory.model.Category;
import com.processedfood.inventory.model.Product;
import com.processedfood.inventory.model.Supplier;
import com.processedfood.inventory.model.User;
import com.processedfood.inventory.repository.CategoryRepository;
import com.processedfood.inventory.repository.ProductRepository;
import com.processedfood.inventory.repository.SupplierRepository;
import com.processedfood.inventory.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CategoryRepository categoryRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
        if (userRepository.findByUsername("staff").isEmpty()) {
            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setRole("STAFF");
            userRepository.save(staff);
        }
        if (userRepository.findByUsername("viewer").isEmpty()) {
            User viewer = new User();
            viewer.setUsername("viewer");
            viewer.setPassword(passwordEncoder.encode("viewer123"));
            viewer.setRole("VIEWER");
            userRepository.save(viewer);
        }

        if (categoryRepository.count() == 0) {
            String[] names = {"Biscuits", "Chips", "Pickles", "Juices", "Frozen Items", "Snacks"};
            for (String name : names) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(name + " category");
                categoryRepository.save(category);
            }
        }

        if (supplierRepository.count() == 0) {
            Supplier supplier = new Supplier();
            supplier.setName("Default Supplier");
            supplier.setPhone("+1-555-0100");
            supplier.setEmail("supplier@example.com");
            supplier.setAddress("Main Warehouse");
            supplierRepository.save(supplier);
        }

        if (productRepository.count() == 0) {
            seedSampleProducts();
        }
    }

    private void seedSampleProducts() {
        Supplier supplier = supplierRepository.findAll().get(0);
        Map<String, List<ProductSeed>> data = Map.of(
                "Biscuits", List.of(
                        new ProductSeed("Oreo Original", "Oreo", "890123100001"),
                        new ProductSeed("Parle-G Gold", "Parle", "890123100002"),
                        new ProductSeed("Good Day Cashew", "Britannia", "890123100003"),
                        new ProductSeed("Marie Gold", "Britannia", "890123100004"),
                        new ProductSeed("Hide & Seek", "Parle", "890123100005"),
                        new ProductSeed("Milk Bikis", "Britannia", "890123100006"),
                        new ProductSeed("Sunfeast Dark Fantasy", "Sunfeast", "890123100007"),
                        new ProductSeed("Bourbon", "Britannia", "890123100008"),
                        new ProductSeed("Monaco", "Parle", "890123100009"),
                        new ProductSeed("50-50 Maska Chaska", "Britannia", "890123100010")
                ),
                "Chips", List.of(
                        new ProductSeed("Lay's Magic Masala", "Lay's", "890123200001"),
                        new ProductSeed("Lay's Classic Salted", "Lay's", "890123200002"),
                        new ProductSeed("Kurkure Masala Munch", "Kurkure", "890123200003"),
                        new ProductSeed("Uncle Chipps Spicy Treat", "Uncle Chipps", "890123200004"),
                        new ProductSeed("Bingo Mad Angles", "Bingo", "890123200005"),
                        new ProductSeed("Pringles Original", "Pringles", "890123200006"),
                        new ProductSeed("Doritos Nacho Cheese", "Doritos", "890123200007"),
                        new ProductSeed("Too Yumm! Multigrain", "Too Yumm!", "890123200008"),
                        new ProductSeed("Haldiram Aloo Bhujia", "Haldiram", "890123200009"),
                        new ProductSeed("Balaji Wafers Tomato", "Balaji", "890123200010")
                ),
                "Pickles", List.of(
                        new ProductSeed("Priya Mango Pickle", "Priya", "890123300001"),
                        new ProductSeed("Mother's Recipe Lime Pickle", "Mother's Recipe", "890123300002"),
                        new ProductSeed("Pachranga Mixed Pickle", "Pachranga", "890123300003"),
                        new ProductSeed("Nilon's Green Chilli Pickle", "Nilon's", "890123300004"),
                        new ProductSeed("Bedekar Mango Pickle", "Bedekar", "890123300005"),
                        new ProductSeed("24 Mantra Organic Mango Pickle", "24 Mantra", "890123300006"),
                        new ProductSeed("Aachi Garlic Pickle", "Aachi", "890123300007"),
                        new ProductSeed("MTR Lemon Pickle", "MTR", "890123300008"),
                        new ProductSeed("Top Op Mango Thokku", "Top Op", "890123300009"),
                        new ProductSeed("Everest Mix Pickle", "Everest", "890123300010")
                ),
                "Juices", List.of(
                        new ProductSeed("Tropicana Orange Delight", "Tropicana", "890123400001"),
                        new ProductSeed("Real Mixed Fruit", "Real", "890123400002"),
                        new ProductSeed("Minute Maid Pulpy Orange", "Minute Maid", "890123400003"),
                        new ProductSeed("B Natural Guava", "B Natural", "890123400004"),
                        new ProductSeed("Paper Boat Aamras", "Paper Boat", "890123400005"),
                        new ProductSeed("Ocean Spray Cranberry", "Ocean Spray", "890123400006"),
                        new ProductSeed("Maaza Mango Drink", "Maaza", "890123400007"),
                        new ProductSeed("Frooti Mango", "Frooti", "890123400008"),
                        new ProductSeed("Slice Mango Drink", "Slice", "890123400009"),
                        new ProductSeed("Appy Fizz", "Appy", "890123400010")
                ),
                "Frozen Items", List.of(
                        new ProductSeed("McCain French Fries", "McCain", "890123500001"),
                        new ProductSeed("Safal Green Peas", "Safal", "890123500002"),
                        new ProductSeed("Godrej Yummiez Nuggets", "Godrej", "890123500003"),
                        new ProductSeed("ITC Master Chef Veg Patty", "ITC", "890123500004"),
                        new ProductSeed("Amul Frozen Paneer Cubes", "Amul", "890123500005"),
                        new ProductSeed("Venky's Chicken Salami", "Venky's", "890123500006"),
                        new ProductSeed("Sumeru Sweet Corn", "Sumeru", "890123500007"),
                        new ProductSeed("Wao Momos Veg Momos", "Wao", "890123500008"),
                        new ProductSeed("Meatzza Chicken Sausage", "Meatzza", "890123500009"),
                        new ProductSeed("Keventers Frozen Malai Tikka", "Keventers", "890123500010")
                ),
                "Snacks", List.of(
                        new ProductSeed("Haldiram Bhujia Sev", "Haldiram", "890123600001"),
                        new ProductSeed("Bikaji Bikaneri Bhujia", "Bikaji", "890123600002"),
                        new ProductSeed("Cornitos Nacho Crisps", "Cornitos", "890123600003"),
                        new ProductSeed("Too Yumm! Foxnuts", "Too Yumm!", "890123600004"),
                        new ProductSeed("Kellogg's Chocos Bites", "Kellogg's", "890123600005"),
                        new ProductSeed("ACT II Popcorn Salted", "ACT II", "890123600006"),
                        new ProductSeed("Open Secret Nutty Cookies", "Open Secret", "890123600007"),
                        new ProductSeed("RiteBite Max Protein Bar", "RiteBite", "890123600008"),
                        new ProductSeed("Soulfull Ragi Bites", "Soulfull", "890123600009"),
                        new ProductSeed("Yoga Bar Trail Mix", "Yoga Bar", "890123600010")
                )
        );

        int i = 0;
        for (Category category : categoryRepository.findAll()) {
            List<ProductSeed> seeds = data.get(category.getName());
            if (seeds == null) continue;
            for (ProductSeed seed : seeds) {
                Product product = new Product();
                product.setName(seed.name());
                product.setDescription(seed.name() + " finished product inventory item");
                product.setCategory(category);
                product.setBrand(seed.brand());
                product.setBatchNumber("BCH-" + (1000 + i));
                product.setBarcode(seed.barcode());
                product.setExpiryDate(LocalDate.now().plusDays(90 + (i % 180)));
                product.setPurchasePrice(BigDecimal.valueOf(20 + (i % 15) * 5L));
                product.setSellingPrice(BigDecimal.valueOf(30 + (i % 15) * 6L));
                product.setQuantity(12 + (i % 25));
                product.setSupplier(supplier);
                product.setImagePath("/img/placeholder.svg");
                productRepository.save(product);
                i++;
            }
        }
    }

    private record ProductSeed(String name, String brand, String barcode) {
    }
}
