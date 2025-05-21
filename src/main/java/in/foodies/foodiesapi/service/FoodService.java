package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.io.FoodRequest;
import in.foodies.foodiesapi.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {

    public String uploadFile(MultipartFile file);

    FoodResponse addFood(FoodRequest request , MultipartFile file);

    List<FoodResponse> readFoods();

    FoodResponse readFood(Long id);

    boolean deleteFile(String fileName);

    void deleteFood(Long id);
}
