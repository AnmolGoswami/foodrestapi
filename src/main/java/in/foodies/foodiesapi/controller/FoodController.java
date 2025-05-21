package in.foodies.foodiesapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.foodies.foodiesapi.io.FoodRequest;
import in.foodies.foodiesapi.io.FoodResponse;
import in.foodies.foodiesapi.service.FoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin("*")
public class FoodController {
    @Autowired
    private FoodService foodService;

    @PostMapping
    public FoodResponse addFood(@RequestPart("food")String foodString,
                                @RequestPart("file")MultipartFile file){
        ObjectMapper objectMapper = new ObjectMapper();
        FoodRequest foodRequest = null;
        try {
            foodRequest = objectMapper.readValue(foodString, FoodRequest.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Json format");
        }
        FoodResponse response = foodService.addFood(foodRequest,file);
        return response;
    }

    @GetMapping
    public List<FoodResponse> readFoods(){
        return foodService.readFoods();
    }
    @GetMapping("/{id}")
    public FoodResponse readFood(@PathVariable Long id){
        return foodService.readFood(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable Long id){
        foodService.deleteFood(id);
    }

}
