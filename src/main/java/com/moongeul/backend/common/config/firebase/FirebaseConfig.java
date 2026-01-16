package com.moongeul.backend.common.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${fcm.certification}")
    private String credentialPath;

    @PostConstruct // 3. 의존성 주입이 완료된 후, 딱 한 번만 실행되도록 보장
    public void initialize() {
        try {
            // 4. 리소스 폴더의 인증 파일을 읽어옴
            ClassPathResource resource = new ClassPathResource(credentialPath);
            InputStream inputStream = resource.getInputStream();

            // 5. Firebase 옵션 설정 (인증 정보 세팅)
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            // 6. FirebaseApp이 이미 초기화되어 있는지 확인 후 초기화 진행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("FCM 초기화 성공");
            }
        } catch (IOException e) {
            // 초기화 실패 시 서버 실행 시점에 에러를 파악할 수 있게 로깅함
            System.err.println("FCM 초기화 실패: " + e.getMessage());
        }
    }
}
