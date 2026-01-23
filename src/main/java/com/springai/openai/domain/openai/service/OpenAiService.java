package com.springai.openai.domain.openai.service;

import com.springai.openai.domain.openai.dto.CityResponseDTO;
import com.springai.openai.domain.openai.entity.ChatEntity;
import com.springai.openai.domain.openai.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class OpenAiService {

    // 10~20개 정도의 멀티턴 응답을 위한 챗메모리
    private final ChatMemoryRepository chatMemoryRepository;
    // 전체 채팅을 id, page별 저장하기 위한 리포지토리
    private final ChatRepository chatRepository;

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    //1. ai api 응답을 하나의 완성으로 받음
    public CityResponseDTO generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 메시지
        SystemMessage systemMessage = new SystemMessage("");
        UserMessage userMessage = new UserMessage(text);
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // openAiChatModel 호출
//        ChatResponse response = openAiChatModel.call(prompt);

        // ChatResponse에서 결과 아웃풋 문자로 꺼내기
//        return response.getResult().getOutput().getText();

        // 문자 -> structure 자동추론으로 DTO에 넣어줌
        return chatClient.prompt(prompt)
                .call()
                .entity(CityResponseDTO.class);
    }

    //2. ai api 응답을 stream으로 받음
    public Flux<String> generateStream(String text) {

        // ChatClient로 openAiChatModel 랩핑함
        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 유저&페이지별 ChatMemory를 관리하기 위한 Key (우선은 명시적으로)
        String userId = "xxxjjhhh1" + "_" + "4";

        // 전체 대화를 저장
        ChatEntity chatUserEntity = new ChatEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(text);

        // 챗메모리 생성
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        // 신규 질문 등록
        chatMemory.add(userId, new UserMessage(text));

        // 메세지
//        SystemMessage systemMessage = new SystemMessage("");
//        UserMessage userMessage = new UserMessage(text);
//        AssistantMessage assistantMessage = new AssistantMessage("");

        //옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 응답 메세지를 저장할 임시 버퍼
        StringBuffer responseBuffer = new StringBuffer();

        // openAiChatModel 호출
//        return openAiChatModel
//                .stream(prompt)
//                .mapNotNull(response -> {
//                    String token = response.getResult().getOutput().getText();
//                    responseBuffer.append(token);
//                    return token;
//                }) // Flux<String>으로 map 됨
//                .doOnComplete(() -> {
//                    // 챗 메모리에 응답값을 아이디로 저장하고 레퍼지토리에 저장한다.
//                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
//                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));
//
//                    // 전체 응답 저장
//                    ChatEntity chatAssistantEntity = new ChatEntity();
//                    chatAssistantEntity.setUserId(userId);
//                    chatAssistantEntity.setType(MessageType.ASSISTANT);
//                    chatAssistantEntity.setContent(responseBuffer.toString());
//
//                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
//                });

        // 랩핑한 chatClient로 호출하기 -> 이유  1. tools, 2. advisors: RAG, 3. 다른 구현체에도 변경 없이 OCP 원칙 수렴
        return chatClient.prompt(prompt)
                .tools(new ChatTools())
                .stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    // 챗메모리 저장
                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    // 전체 대화 저장
                    ChatEntity chatAssistantEntity = new ChatEntity();
                    chatAssistantEntity.setUserId(userId);
                    chatAssistantEntity.setType(MessageType.ASSISTANT);
                    chatAssistantEntity.setContent(responseBuffer.toString());

                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
                });
    }

    // text요청 벡터 임베딩 응답
    public List<float[]> generateEmbedding(List<String> texts, String model) {
        // 옵션
        EmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .build();
        // 프롬프트
        EmbeddingRequest prompt = new EmbeddingRequest(texts, options);
        // 요청 응답
        EmbeddingResponse response = openAiEmbeddingModel.call(prompt);

        return response.getResults().stream()
                .map(Embedding::getOutput)
                .toList();
    }

    // 이미지 생성 Dall-e
    public List<String> generateImage(String text, int count, int height, int width) {
        // 옵션
        OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
                .quality("hd")
                .N(count)
                .height(height)
                .width(width)
                .build();
        // 프롬프트
        ImagePrompt prompt = new ImagePrompt(text, imageOptions);
        // 요청 및 응답
        ImageResponse response = openAiImageModel.call(prompt);
        return response
                .getResults()
                .stream()
                .map(image -> image.getOutput().getUrl())
                .toList();
    }

    // TTS
    public byte[] tts(String text) {

        // 옵션
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        // 프롬프트
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, speechOptions);

        // 요청 및 응답
        TextToSpeechResponse response = openAiAudioSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }

    // STT
    public String stt(Resource audioFile) {

        // 옵션
        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .language("ko") // 인식할 언어
                .prompt("Ask not this, but ask that") // 음성 인식 전 참고할 텍스트 프롬프트
                .temperature(0f)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .responseFormat(responseFormat) // 결과 타입 지정 VTT 자막형식
                .build();

        // 프롬프트
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile,
                transcriptionOptions);

        // 요청 및 응답
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
