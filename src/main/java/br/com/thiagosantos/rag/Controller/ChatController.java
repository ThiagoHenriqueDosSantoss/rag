package br.com.thiagosantos.rag.Controller;

import br.com.thiagosantos.rag.DTO.AnswerResponseDTO;
import br.com.thiagosantos.rag.DTO.QuestionRequestDTO;
import br.com.thiagosantos.rag.Service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;


    public ChatController (ChatService chatService){
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public AnswerResponseDTO perguntar(@RequestBody QuestionRequestDTO request) throws Exception {

        String response = chatService.pergunta(request.question());

        return new AnswerResponseDTO(response);
    }
}
