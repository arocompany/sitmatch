package com.nex.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController // JSON 형태 결과값을 반환해줌 (@ResponseBody가 필요없음)
@RequiredArgsConstructor // final 객체를 Constructor Injection 해줌. (Autowired 역할)
@RequestMapping("/test")
public class TestController {

    private final ResourceLoader resourceLoader;

    private final RestTemplate restTemplate;


    @GetMapping("/image")
    public void test() {
        byte[] imageBytes = null;
        String imageUrl = "https://www.usnews.com/object/image/00000151-844d-d104-a7f5-aeedb78a0000/bc16-south-korea-crop-editorial.jpg?update-time=1452285597833&size=superhero-medium";
        //RestTemplate restTemplate = new RestTemplate();

        if (imageUrl != null) {
            log.debug(" ### imageUrl ### : {} ", imageUrl);
            Resource resource = resourceLoader.getResource(imageUrl);
            log.debug(" ### resource ### : {} ", resource);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                log.debug(" ### imageBytes ### : {} ", imageBytes);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

}