package br.com.thiagosantos.rag.Service;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private final List<String> chunks = new ArrayList<>();

    @PostConstruct
    public void carregarDocumentos() throws Exception {

        String[] arquivos = {
                "documents/2 - Agentes Inteligentes.pdf",
                "documents/3 - Busca de Soluções.pdf",
                "documents/4 - busca cega.pdf",
                "documents/5 - Busca Heuristica .pdf",
                "documents/6 - Sistemas Especialistas (1).pdf",
                "documents/7 - Aprednizado de máquina  (1).pdf",
                "documents/8-Perceptron-e-MLP (1).pdf",
                "documents/9-Redes-neurais-Fundamentacoes-New (1).pdf",

        };

        for (String arquivo : arquivos) {

            try (InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream(arquivo)) {

                if (input == null) {
                    System.out.println("Arquivo não encontrado: " + arquivo);
                    continue;
                }

                try (PDDocument pdf = Loader.loadPDF(input.readAllBytes())) {

                    PDFTextStripper stripper = new PDFTextStripper();

                    String texto = stripper.getText(pdf);

                    dividirEmChunks(texto);

                    System.out.println("PDF carregado: " + arquivo);
                }
            }
        }

        System.out.println("Total de chunks: " + chunks.size());
    }

    private void dividirEmChunks(String texto) {

        int tamanho = 1000;
        int sobreposicao = 200;
        int passo = tamanho - sobreposicao;

        for (int inicio = 0; inicio < texto.length(); inicio += passo) {

            int fim = Math.min(inicio + tamanho, texto.length());

            chunks.add(texto.substring(inicio, fim));

            if (fim == texto.length()) {
                break;
            }
        }
    }

    public List<String> buscarContexto(String pergunta) {

        String[] palavras = pergunta.toLowerCase().split("\\W+");

        return chunks.stream()
                .map(chunk -> new ChunkResultado(
                        chunk,
                        calcularPontuacao(chunk, palavras)
                ))
                .filter(resultado -> resultado.pontuacao() > 0)
                .sorted((a, b) ->
                        Integer.compare(b.pontuacao(), a.pontuacao()))
                .limit(3)
                .map(ChunkResultado::texto)
                .toList();
    }

    private int calcularPontuacao(String chunk, String[] palavras) {

        String texto = chunk.toLowerCase();
        int pontuacao = 0;

        for (String palavra : palavras) {

            if (palavra.length() > 3 && texto.contains(palavra)) {
                pontuacao++;
            }
        }

        return pontuacao;
    }

    private record ChunkResultado(String texto, int pontuacao) {
    }
}
