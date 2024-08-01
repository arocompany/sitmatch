package com.nex.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class CommonStaticHttpUtil {
    private WebClient WEB_CLIENT = null;
    private CommonStaticHttpUtil(){}
    private static CommonStaticHttpUtil commonStaticHttpUtil = new CommonStaticHttpUtil();
    public static CommonStaticHttpUtil getInstance(){
        if(commonStaticHttpUtil.WEB_CLIENT == null) {
            log.info("commonStaticHttpUtil.WEB_CLIENT --- create --- start");
//            commonStaticHttpUtil.factory = new DefaultUriBuilderFactory();
//            commonStaticHttpUtil.factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper)))
                    .build();

            commonStaticHttpUtil.WEB_CLIENT = WebClient.builder()
                    .exchangeStrategies(exchangeStrategies)
//                    .uriBuilderFactory(commonStaticHttpUtil.factory)
                    .build();
            log.info("commonStaticHttpUtil.WEB_CLIENT --- create--- end");
        }

        return commonStaticHttpUtil;
    }
    public static <T> Mono<T> GetHttp(String baseUrl, String path, MultiValueMap<String, String> param, Class<T> t){
        return commonStaticHttpUtil.WEB_CLIENT.mutate().baseUrl(baseUrl).build().get()
                .uri(uriBuilder -> uriBuilder
                            .path(path).queryParams(param)
                            .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError()
                                || status.is5xxServerError()
                        , clientResponse ->
                                clientResponse.bodyToMono(String.class)
                                        .map(body -> new RuntimeException(body)))
                .bodyToMono(t);
    }

    public static <T> T GetHttpSync(String baseUrl, String path, MultiValueMap<String, String> param, Class<T> t){
        Mono<T> m5 = GetHttp(baseUrl, path, param, t);
        T result = m5.block();
        return result;
    }
}
