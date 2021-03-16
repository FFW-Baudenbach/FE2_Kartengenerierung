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
                                           @RequestParam(value = "store", defaultValue = "false") boolean store,
                                           @RequestParam(value="size") Optional<String> size)
    {
        return mapGenerator.generateMap("overview", lat, lng, store, size);
    }

    /*
    Example: http://localhost:8080/detail?lat=49.646412071556114&lng=10.564397866729674
    */
    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam(value = "lat") double lat,
                                         @RequestParam(value = "lng") double lng,
                                         @RequestParam(value = "store", defaultValue = "false") boolean store,
                                         @RequestParam(value="size") Optional<String> size)
    {
        return mapGenerator.generateMap("detail", lat, lng, store, size);
    }

    /*
    Example: http://localhost:8080/route?lat=49.646412071556114&lng=10.564397866729674
     */
    @GetMapping("/route")
    public ResponseEntity<Object> route(@RequestParam(value = "lat") double lat,
                                        @RequestParam(value = "lng") double lng,
                                        @RequestParam(value = "store", defaultValue = "false") boolean store,
                                        @RequestParam(value="size") Optional<String> size)
    {
        return mapGenerator.generateMap("route", lat, lng, store, size);
    }



}
