package com.kopo.hanagreenworld.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopo.hanagreenworld.activity.domain.Quiz;
import com.kopo.hanagreenworld.common.exception.BusinessException;
import com.kopo.hanagreenworld.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizGeneratorService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

    // JSON 응답을 담기 위한 내부 DTO 클래스
    private record QuizData(
        String question,
        List<String> options,
        int correctAnswer,
        String explanation,
        int pointsReward
    ) {}

    public Quiz generateEnvironmentQuiz() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(GEMINI_API_URL).build();

            // 프롬프트 작성
            String prompt = """
                환경 보호와 관련된 퀴즈를 다음 JSON 형식으로 생성해주세요.
                반드시 아래 형식을 정확히 지켜서 JSON만 응답해주세요:
                
                {
                    "question": "환경 관련 질문을 여기에 작성",
                    "options": ["정답", "오답1", "오답2", "오답3"],
                    "correctAnswer": 0,
                    "explanation": "정답에 대한 자세한 설명",
                    "pointsReward": 10
                }
                
                주제는 다음 중 하나를 선택해서 작성해주세요:
                1. 기후변화와 지구온난화
                2. 재활용과 자원순환
                3. 친환경 생활습관
                4. 탄소중립과 신재생에너지
                
                난이도는 일반인이 이해할 수 있는 수준으로 작성하고,
                설명은 교육적이고 실용적인 내용으로 작성해주세요.
                """;

            // 요청 본문 생성
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );

            // API 호출
            String response = webClient
                .post()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("key", apiKey)
                    .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // 응답에서 생성된 텍스트 추출
            String generatedText = extractGeneratedText(response);
            log.debug("Generated quiz JSON: {}", generatedText);
            
            // JSON 파싱
            QuizData quizData = objectMapper.readValue(generatedText, QuizData.class);

            // Quiz 엔티티 생성
            return Quiz.builder()
                .question(quizData.question())
                .options(objectMapper.writeValueAsString(quizData.options()))
                .correctAnswer(quizData.correctAnswer())
                .explanation(quizData.explanation())
                .pointsReward(quizData.pointsReward())
                .build();

        } catch (Exception e) {
            log.error("Failed to generate quiz using Gemini API", e);
            // 실패 시 기본 퀴즈 반환
            return createFallbackQuiz();
        }
    }

    private String extractGeneratedText(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response", e);
            throw new BusinessException(ErrorCode.QUIZ_GENERATION_FAILED);
        }
    }

    private Quiz createFallbackQuiz() {
        try {
            // 기본 환경 퀴즈 생성
            String[] questions = {
                "다음 중 지구온난화의 주요 원인이 아닌 것은?",
                "재활용이 가능한 플라스틱 종류 중 PET는 몇 번인가요?",
                "탄소발자국을 줄이는 방법이 아닌 것은?",
                "친환경 에너지원이 아닌 것은?"
            };

            String[][] optionsArray = {
                {"이산화탄소 배출", "산업활동", "자동차 운행", "나무 심기"},
                {"1번", "2번", "3번", "4번"},
                {"대중교통 이용", "에너지 절약", "재활용", "일회용품 사용"},
                {"태양광", "풍력", "석탄", "수력"}
            };

            int[] correctAnswers = {3, 0, 3, 2};
            
            String[] explanations = {
                "나무 심기는 오히려 이산화탄소를 흡수하여 지구온난화를 완화시킵니다.",
                "PET는 플라스틱 재활용 분류 1번으로, 음료수 병 등에 사용됩니다.",
                "일회용품 사용은 탄소발자국을 증가시키는 행동입니다.",
                "석탄은 화석연료로 이산화탄소를 많이 배출하는 에너지원입니다."
            };

            int randomIndex = (int) (Math.random() * questions.length);

            return Quiz.builder()
                .question(questions[randomIndex])
                .options(objectMapper.writeValueAsString(List.of(optionsArray[randomIndex])))
                .correctAnswer(correctAnswers[randomIndex])
                .explanation(explanations[randomIndex])
                .pointsReward(10)
                .build();
                
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.QUIZ_GENERATION_FAILED);
        }
    }
}