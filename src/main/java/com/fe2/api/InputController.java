package com.fe2.api;

import com.fe2.service.MapGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class InputController {

    @Autowired
    public MapGenerator mapGenerator;

    /*
    Example: http://localhost:8080/overview?lat=49.646412071556114&lng=10.564397866729674
     */
    @GetMapping("/overview")
    public ResponseEntity<Object> overview(@RequestParam(value = "lat") double lat,
                                           @RequestParam(value = "lng") double lng,
                                           @RequestParam(value="size") Optional<String> size,
                                           @RequestParam(value="identifier") Optional<String> identifier)
    {
        return mapGenerator.generateMap("overview", lat, lng, size, identifier);
    }

    /*
    Example: http://localhost:8080/detail?lat=49.646412071556114&lng=10.564397866729674
    */
    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam(value = "lat") double lat,
                                         @RequestParam(value = "lng") double lng,
                                         @RequestParam(value="size") Optional<String> size,
                                         @RequestParam(value="identifier") Optional<String> identifier)
    {
        return mapGenerator.generateMap("detail", lat, lng, size, identifier);
    }

    /*
    Example: http://localhost:8080/route?lat=49.646412071556114&lng=10.564397866729674
     */
    @GetMapping("/route")
    public ResponseEntity<Object> route(@RequestParam(value = "lat") double lat,
                                        @RequestParam(value = "lng") double lng,
                                        @RequestParam(value="size") Optional<String> size,
                                        @RequestParam(value="identifier") Optional<String> identifier)
    {
        return mapGenerator.generateMap("route", lat, lng, size, identifier);
    }

}
