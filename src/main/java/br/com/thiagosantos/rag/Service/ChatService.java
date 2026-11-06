package br.com.thiagosantos.rag.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final DocumentService documentService;

    public ChatService(
            ChatClient.Builder builder,
            DocumentService documentService) {

        this.chatClient = builder.build();
        this.documentService = documentService;
    }

    public String pergunta(String pergunta) throws  Exception{

        List<String> documentos = documentService.buscarContexto(pergunta);

        if(documentos.isEmpty()){
            return "Não encontrei informações relevantes nos PDFs.";
        }
        String contexto = String.join("\n\n",documentos);
        return chatClient
                .prompt()
                .system("""
                    Você é um professor de Inteligência Artificial.

                    Sua função é ajudar um estudante universitário.

                    Responda utilizando exclusivamente as
                    informações fornecidas no contexto.

                    Explique os conceitos de maneira simples,
                    utilizando exemplos quando possível.

                    Caso não encontre a resposta no contexto,
                    informe que não encontrou a informação.
                    """)
                .user("""
                    Contexto dos materiais da faculdade:

                    %s

                    Pergunta do estudante:

                    %s
                    """.formatted(contexto,pergunta))
                .call()
                .content().replaceAll("\n\n", " ");
    }
}
