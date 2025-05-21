package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.entity.FoodEntity;
import in.foodies.foodiesapi.io.FoodRequest;
import in.foodies.foodiesapi.io.FoodResponse;
import in.foodies.foodiesapi.repositary.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class FoodServiceImpl implements FoodService{
    @Autowired
    private S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Autowired
    private FoodRepository foodRepository;



    @Override
    public String uploadFile(MultipartFile file) {

        String fileNameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
        String key = UUID.randomUUID().toString()+"."+fileNameExtension;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            if(response.sdkHttpResponse().isSuccessful()){
                return "https://"+bucketName+".s3.amazonaws.com/"+key;
            }
            else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"A Error Occured");
            }

        }catch (IOException ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"An error occured While Loading the file");

        }


    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToFoodEntity(request);
        String imageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        FoodEntity newFoods = foodRepository.save(newFoodEntity);
        return convertToFoodResponse(newFoods);


    }

    @Override
    public List<FoodResponse> readFoods() {
        return foodRepository.findAll()
                .stream()
                .map(object->convertToFoodResponse(object))
                .collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(Long id) {
        return convertToFoodResponse(foodRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("Food is not found")));
    }

    @Override
    public boolean deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        return true;
    }

    @Override
    public void deleteFood(Long id) {
        FoodResponse response = readFood(id);
        String imageUrl = response.getImageUrl();
        String fileName  = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
        boolean isFileDelete = deleteFile(fileName);
        if(isFileDelete){
            foodRepository.deleteById(response.getId());
        }
    }

    private FoodResponse convertToFoodResponse(FoodEntity entity){
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    private FoodEntity convertToFoodEntity(FoodRequest request){
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }
}
